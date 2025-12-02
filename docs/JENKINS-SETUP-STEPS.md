# Complete Jenkins Setup Guide

## Step 1: Install Jenkins on EC2

SSH to your EC2:
```bash
ssh ubuntu@184.169.147.113
```

Run these commands:
```bash
# Update system
sudo apt-get update

# Install Java 17
sudo apt-get install -y openjdk-17-jdk

# Verify Java installation
java -version

# Add Jenkins repository key
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

# Add Jenkins repository
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# Update package list
sudo apt-get update

# Install Jenkins
sudo apt-get install -y jenkins

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Check status (should say "active (running)")
sudo systemctl status jenkins
```

**Get your initial admin password:**
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```
Copy this password - you'll need it in a moment.

---

## Step 2: Open Jenkins Port in EC2 Security Group

1. Go to AWS Console → EC2 → Security Groups
2. Find your EC2 instance's security group
3. Edit Inbound Rules
4. Add Rule:
   - Type: Custom TCP
   - Port: 8080
   - Source: Your IP (or 0.0.0.0/0 for anywhere - less secure)
5. Save rules

---

## Step 3: Access Jenkins Web UI

1. Open browser: `http://184.169.147.113:8080`
2. Paste the initial admin password from Step 1
3. Click "Install suggested plugins" (wait for installation)
4. Create admin user:
   - Username: admin (or your choice)
   - Password: (choose a strong password)
   - Email: your@email.com
5. Instance Configuration: Keep default URL
6. Click "Start using Jenkins"

---

## Step 4: Install Required Plugins

1. Go to: **Manage Jenkins → Plugins → Available plugins**
2. Search and install these plugins:
   - Git plugin (should already be installed)
   - GitHub plugin
   - SSH Agent plugin
   - Pipeline plugin (should already be installed)

3. Click "Install without restart"
4. Check "Restart Jenkins when installation is complete"

---

## Step 5: Add SSH Credentials for EC2

Jenkins needs to SSH to your EC2 to deploy.

1. Go to: **Manage Jenkins → Credentials**
2. Click "System" → "Global credentials (unrestricted)"
3. Click "+ Add Credentials"
4. Configure:
   - Kind: **SSH Username with private key**
   - Scope: Global
   - ID: `buzzlink-ec2-ssh-key` (must match Jenkinsfile)
   - Username: `ubuntu`
   - Private Key: **Enter directly**
   - Click "Add" and paste your EC2 private key (.pem file content)
   - Passphrase: (leave empty if your key has no passphrase)
5. Click "Create"

**To get your EC2 private key:**
```bash
# On your local machine
cat ~/.ssh/BuzzLinkKey.pem
# Copy entire output including BEGIN and END lines
```

---

## Step 6: Create the Pipeline Job

1. From Jenkins dashboard, click **"New Item"**
2. Enter name: `BuzzLink-Deploy`
3. Select: **Pipeline**
4. Click OK
5. Configure the job:

### General Section:
- ✅ Check "GitHub project"
- Project URL: `https://github.com/YOUR_USERNAME/BuzzLink/`

### Build Triggers:
- ✅ Check "GitHub hook trigger for GITScm polling"
- ✅ Check "Poll SCM"
- Schedule: `H/2 * * * *` (polls every 2 minutes as fallback)

### Pipeline Section:
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `https://github.com/YOUR_USERNAME/BuzzLink.git`
- Credentials: (leave as "none" if public repo)
- Branch Specifier: `*/main`
- Script Path: `Jenkinsfile`

6. Click **Save**

---

## Step 7: Setup GitHub Webhook

1. Go to your GitHub repository: `https://github.com/YOUR_USERNAME/BuzzLink`
2. Click **Settings** → **Webhooks** → **Add webhook**
3. Configure:
   - Payload URL: `http://184.169.147.113:8080/github-webhook/`
   - Content type: `application/json`
   - Which events: **Just the push event**
   - ✅ Active
4. Click **Add webhook**

GitHub will test the webhook - you should see a green checkmark.

---

## Step 8: Prepare EC2 for Deployment

Make sure your EC2 is ready:

```bash
# SSH to EC2
ssh ubuntu@184.169.147.113

# Verify Docker is installed
docker --version
docker-compose --version

# If not installed:
sudo apt-get update
sudo apt-get install -y docker.io docker-compose

# Add ubuntu user to docker group
sudo usermod -aG docker ubuntu
newgrp docker

# Verify project is cloned
ls -la /home/ubuntu/BuzzLink

# If not cloned:
cd /home/ubuntu
git clone https://github.com/YOUR_USERNAME/BuzzLink.git

# Make sure .env file exists
cd /home/ubuntu/BuzzLink
ls -la .env

# If .env doesn't exist, create it:
nano .env
# Add your environment variables (POSTGRES_PASSWORD, CLERK keys, etc.)

# Make deployment script executable
chmod +x scripts/deploy.sh
```

---

## Step 9: Test the Pipeline

### Option 1: Manual Trigger
1. Go to Jenkins dashboard
2. Click on **BuzzLink-Deploy** job
3. Click **"Build Now"**
4. Watch the build progress in "Build History"
5. Click on the build number → **Console Output** to see logs

### Option 2: Automatic Trigger (Recommended)
```bash
# On your local machine
cd /path/to/BuzzLink

# Make a small change
echo "# Testing CI/CD" >> README.md

# Commit and push
git add .
git commit -m "Test Jenkins deployment"
git push origin main
```

Within a few seconds:
- GitHub webhook triggers Jenkins
- Jenkins starts the build automatically
- You can watch it in Jenkins UI

---

## Step 10: Monitor the Deployment

**In Jenkins UI:**
1. Watch the "Stage View" for progress
2. Stages:
   - ✅ Checkout (pull code)
   - ✅ Build Check (validate project)
   - ✅ Deploy to EC2 (SSH and deploy)
   - ✅ Health Check (verify services)
   - ✅ Cleanup (remove old images)

**On EC2:**
```bash
# SSH to EC2
ssh ubuntu@184.169.147.113

# Watch deployment logs
tail -f /tmp/buzzlink-deploy-*.log

# Check containers
docker-compose ps

# View application logs
docker-compose logs -f
```

**Verify Deployment:**
- Frontend: http://184.169.147.113:3000
- Backend: http://184.169.147.113:8080/actuator/health

---

## Troubleshooting

### Jenkins Can't Connect to EC2
```bash
# Verify SSH credentials in Jenkins
# Check security group allows SSH (port 22) from Jenkins server
# If Jenkins is on the same EC2, it should work without issues

# Test SSH manually from Jenkins:
# Go to Jenkins → Manage Jenkins → Script Console
# Run:
def command = "ssh -o StrictHostKeyChecking=no ubuntu@184.169.147.113 'echo Connected'"
println command.execute().text
```

### Build Fails at SSH Step
- Check the SSH credentials ID matches: `buzzlink-ec2-ssh-key`
- Verify the private key is correct
- Check `/home/ubuntu/BuzzLink` exists on EC2

### Health Checks Fail
```bash
# SSH to EC2 and check:
docker-compose ps
docker-compose logs backend
docker-compose logs frontend

# Check if .env file exists and has correct values
cat /home/ubuntu/BuzzLink/.env
```

### Webhook Not Triggering
- Verify webhook URL is accessible: `http://184.169.147.113:8080/github-webhook/`
- Check GitHub webhook deliveries (Settings → Webhooks → Recent Deliveries)
- Ensure port 8080 is open in security group
- Fallback: SCM polling will work (checks every 2 minutes)

---

## Next Steps

Once everything is working:

1. **Secure Jenkins:**
   - Change admin password
   - Enable HTTPS
   - Restrict access by IP

2. **Add Notifications:**
   - Uncomment Slack notification lines in Jenkinsfile
   - Install Slack plugin
   - Configure Slack webhook

3. **Add Tests:**
   - Add test stages to Jenkinsfile before deployment
   - Run backend tests: `./gradlew test`
   - Run frontend tests: `npm test`

4. **Monitoring:**
   - Set up Prometheus/Grafana
   - Configure log aggregation
   - Add uptime monitoring

---

## Summary Checklist

- [ ] Jenkins installed on EC2
- [ ] Port 8080 open in security group
- [ ] Initial setup completed
- [ ] Required plugins installed
- [ ] SSH credentials added (ID: buzzlink-ec2-ssh-key)
- [ ] Pipeline job created
- [ ] GitHub webhook configured
- [ ] EC2 prepared (Docker, project cloned, .env file)
- [ ] Test deployment successful
- [ ] Services accessible at ports 3000 and 8080

**You're all set! Every push to `main` will now automatically deploy! 🚀**
