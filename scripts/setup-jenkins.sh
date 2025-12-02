#!/bin/bash

###############################################################################
# Jenkins Setup Script for BuzzLink CI/CD
#
# This script helps set up Jenkins for BuzzLink project deployment.
# Run this on your Jenkins server (or EC2 instance if Jenkins is there).
###############################################################################

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  BuzzLink Jenkins Setup Script${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo -e "${YELLOW}Warning: Running as root. It's recommended to run as a regular user.${NC}"
fi

# Install Jenkins (Ubuntu/Debian)
install_jenkins_ubuntu() {
    echo -e "${GREEN}Installing Jenkins on Ubuntu/Debian...${NC}"

    # Add Jenkins repository
    wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
    sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'

    # Update and install
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk jenkins

    # Start Jenkins
    sudo systemctl start jenkins
    sudo systemctl enable jenkins

    echo -e "${GREEN}✅ Jenkins installed successfully${NC}"
    echo -e "${YELLOW}Jenkins is running on http://localhost:8080${NC}"
    echo -e "${YELLOW}Initial admin password:${NC}"
    sudo cat /var/lib/jenkins/secrets/initialAdminPassword
}

# Install required plugins
install_plugins() {
    echo -e "${GREEN}Installing required Jenkins plugins...${NC}"

    # List of required plugins
    PLUGINS=(
        "git"
        "github"
        "ssh-agent"
        "docker-workflow"
        "pipeline-stage-view"
        "slack"
        "credentials-binding"
    )

    echo "Please install the following plugins via Jenkins UI:"
    for plugin in "${PLUGINS[@]}"; do
        echo "  - $plugin"
    done
}

# Setup SSH key for EC2
setup_ssh_key() {
    echo -e "${GREEN}Setting up SSH key for EC2...${NC}"

    SSH_KEY_PATH="${1:-$HOME/.ssh/buzzlink-ec2-key}"

    if [ ! -f "$SSH_KEY_PATH" ]; then
        echo -e "${YELLOW}SSH key not found at $SSH_KEY_PATH${NC}"
        echo "Please ensure you have the EC2 SSH key at this location."
        echo ""
        echo "To add the SSH key to Jenkins:"
        echo "1. Go to Jenkins → Manage Jenkins → Credentials"
        echo "2. Click on 'System' → 'Global credentials'"
        echo "3. Click 'Add Credentials'"
        echo "4. Select 'SSH Username with private key'"
        echo "5. Set ID as 'buzzlink-ec2-ssh-key'"
        echo "6. Username: ubuntu"
        echo "7. Private Key: Enter directly or from file at $SSH_KEY_PATH"
    else
        echo -e "${GREEN}✅ SSH key found at $SSH_KEY_PATH${NC}"
        echo "Please add this key to Jenkins credentials with ID: buzzlink-ec2-ssh-key"
    fi
}

# Create Jenkins job configuration
create_job_config() {
    echo -e "${GREEN}Creating Jenkins job...${NC}"

    cat > /tmp/buzzlink-jenkins-job.xml <<'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description>BuzzLink Application Deployment Pipeline</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/2 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.90">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.10.0">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>https://github.com/YOUR_USERNAME/BuzzLink.git</url>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

    echo -e "${GREEN}✅ Job configuration saved to /tmp/buzzlink-jenkins-job.xml${NC}"
    echo "You can import this configuration when creating a new pipeline job."
}

# Print setup instructions
print_instructions() {
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}  Next Steps${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "1. Access Jenkins at: http://YOUR_SERVER:8080"
    echo ""
    echo "2. Install recommended plugins plus:"
    echo "   - Git plugin"
    echo "   - GitHub plugin"
    echo "   - SSH Agent plugin"
    echo "   - Docker Pipeline plugin"
    echo ""
    echo "3. Add EC2 SSH credentials:"
    echo "   - Go to: Manage Jenkins → Credentials"
    echo "   - Add SSH key with ID: buzzlink-ec2-ssh-key"
    echo "   - Username: ubuntu"
    echo ""
    echo "4. Create a new Pipeline job:"
    echo "   - New Item → Pipeline"
    echo "   - Name: BuzzLink-Deploy"
    echo "   - Pipeline script from SCM"
    echo "   - SCM: Git"
    echo "   - Repository URL: YOUR_GITHUB_REPO"
    echo "   - Script Path: Jenkinsfile"
    echo ""
    echo "5. Setup GitHub webhook:"
    echo "   - Go to your GitHub repository settings"
    echo "   - Webhooks → Add webhook"
    echo "   - Payload URL: http://YOUR_JENKINS_URL/github-webhook/"
    echo "   - Content type: application/json"
    echo "   - Events: Just the push event"
    echo ""
    echo "6. Test the pipeline:"
    echo "   - Push a commit to your repository"
    echo "   - Watch Jenkins automatically trigger the build"
    echo ""
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Main menu
main() {
    PS3="Select an option: "
    options=("Install Jenkins (Ubuntu)" "Setup SSH Key" "Create Job Config" "Show Instructions" "Quit")

    select opt in "${options[@]}"
    do
        case $opt in
            "Install Jenkins (Ubuntu)")
                install_jenkins_ubuntu
                ;;
            "Setup SSH Key")
                read -p "Enter SSH key path (default: ~/.ssh/buzzlink-ec2-key): " key_path
                setup_ssh_key "${key_path:-$HOME/.ssh/buzzlink-ec2-key}"
                ;;
            "Create Job Config")
                create_job_config
                ;;
            "Show Instructions")
                print_instructions
                ;;
            "Quit")
                echo "Exiting..."
                break
                ;;
            *) echo "Invalid option $REPLY";;
        esac
    done
}

# Run main menu
main
