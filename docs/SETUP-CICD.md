# Quick CI/CD Setup Guide

This is a quick reference guide to get Jenkins CI/CD up and running for BuzzLink.

## Option 1: Jenkins CI/CD (Recommended for Enterprise)

### Prerequisites
- Jenkins server (can be on EC2 or separate machine)
- SSH access to EC2 instance (184.169.147.113)
- GitHub repository access

### Quick Setup

**1. Install Jenkins** (if not already installed)
```bash
# On Ubuntu/Debian
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

**2. Configure Jenkins**
- Access Jenkins: `http://YOUR_JENKINS_SERVER:8080`
- Install plugins:
  - Git plugin
  - GitHub plugin
  - SSH Agent plugin
  - Docker Pipeline plugin
  - Pipeline Stage View

**3. Add SSH Credentials**
- Go to: `Manage Jenkins → Credentials → System → Global credentials`
- Click: `Add Credentials`
- Type: `SSH Username with private key`
- ID: `buzzlink-ec2-ssh-key`
- Username: `ubuntu`
- Private Key: Paste your EC2 private key

**4. Create Pipeline Job**
- New Item → Pipeline
- Name: `BuzzLink-Deploy`
- Pipeline script from SCM
- SCM: Git
- Repository URL: `https://github.com/YOUR_USERNAME/BuzzLink.git`
- Branch: `*/main`
- Script Path: `Jenkinsfile`

**5. Setup GitHub Webhook**
- GitHub Repo → Settings → Webhooks → Add webhook
- Payload URL: `http://YOUR_JENKINS_SERVER:8080/github-webhook/`
- Content type: `application/json`
- Events: Push events

**6. Prepare EC2**
```bash
# SSH to EC2
ssh ubuntu@184.169.147.113

# Install Docker if needed
sudo apt-get update
sudo apt-get install -y docker.io docker-compose
sudo usermod -aG docker ubuntu
newgrp docker

# Clone repository
cd /home/ubuntu
git clone https://github.com/YOUR_USERNAME/BuzzLink.git
cd BuzzLink

# Create .env file
nano .env
# Add your environment variables (see .env.example)

# Make deployment script executable
chmod +x scripts/deploy.sh
```

**7. Test It!**
```bash
# Push a commit to main branch
git add .
git commit -m "Test CI/CD"
git push origin main

# Watch Jenkins automatically deploy!
```

---

## Option 2: GitHub Actions (Simpler Alternative)

### Quick Setup

**1. Add SSH Key as Secret**
- Go to: GitHub Repo → Settings → Secrets and variables → Actions
- Click: New repository secret
- Name: `EC2_SSH_KEY`
- Value: Paste your EC2 private key

**2. Workflow is Ready!**
The workflow file is already in `.github/workflows/deploy.yml`

**3. Test It!**
```bash
# Push to main branch
git add .
git commit -m "Test GitHub Actions"
git push origin main

# Go to: Repository → Actions to see the deployment
```

---

## Files Created

### CI/CD Configuration
- `Jenkinsfile` - Jenkins pipeline configuration
- `.github/workflows/deploy.yml` - GitHub Actions workflow
- `scripts/deploy.sh` - Main deployment script for EC2
- `scripts/setup-jenkins.sh` - Jenkins setup helper script

### API Documentation
- `docs/openapi.yaml` - OpenAPI 3.0 specification
- `docs/swagger-ui.html` - Interactive API documentation
- `docs/API.md` - Updated with links to OpenAPI docs
- `docs/DEPLOYMENT.md` - Comprehensive deployment guide

---

## How It Works

### Jenkins Workflow
1. GitHub webhook triggers on push to `main`
2. Jenkins pulls latest code
3. Validates project structure
4. SSH to EC2 and runs deployment
5. Pulls latest code on EC2
6. Rebuilds Docker containers
7. Runs health checks
8. Cleans up old resources
9. Notifies success/failure

### Deployment Process (on EC2)
1. Stop existing containers
2. Pull latest code
3. Build new Docker images
4. Start new containers
5. Wait for services to be healthy
6. Verify backend at `:8080/actuator/health`
7. Verify frontend at `:3000`
8. Log deployment details

---

## View OpenAPI Documentation

### Local Development
```bash
# Serve locally
cd docs
python3 -m http.server 8000

# Access at: http://localhost:8000/swagger-ui.html
```

### Production
If you want to serve the Swagger UI from your EC2 server:

```bash
# On EC2
cd /home/ubuntu/BuzzLink/docs
python3 -m http.server 8000 &

# Access at: http://184.169.147.113:8000/swagger-ui.html
```

Or integrate it into your Spring Boot app (optional):
```gradle
// In backend/build.gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
```

---

## Manual Deployment

If you need to deploy manually:

```bash
# SSH to EC2
ssh ubuntu@184.169.147.113

# Run deployment script
cd /home/ubuntu/BuzzLink
./scripts/deploy.sh

# Or step by step
git pull origin main
docker-compose down
docker-compose up -d --build
docker-compose ps
```

---

## Monitoring

### Check Service Status
```bash
# On EC2
docker-compose ps
docker-compose logs -f

# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

### View Deployment Logs
```bash
# Jenkins: Check build logs in Jenkins UI
# Deployment script: /tmp/buzzlink-deploy-*.log
# Docker: docker-compose logs -f
```

---

## Troubleshooting

### Jenkins Can't Connect to EC2
```bash
# Verify SSH credentials in Jenkins
# Check EC2 security group allows SSH (port 22)
# Test manually: ssh -i ~/.ssh/key.pem ubuntu@184.169.147.113
```

### Docker Permission Denied
```bash
# On EC2
sudo usermod -aG docker ubuntu
newgrp docker
```

### Port Already in Use
```bash
# Find and stop conflicting containers
docker ps -a
docker stop <container_id>
```

### Health Checks Fail
```bash
# Check logs
docker-compose logs backend
docker-compose logs frontend

# Verify environment variables
docker-compose exec backend env | grep SPRING
docker-compose exec frontend env | grep NEXT_PUBLIC
```

---

## Next Steps

1. **Security**: Set up HTTPS with SSL certificates
2. **Monitoring**: Add Prometheus/Grafana for metrics
3. **Notifications**: Configure Slack notifications in Jenkinsfile
4. **Testing**: Add automated tests before deployment
5. **Staging**: Create staging environment for testing

---

## Summary

✅ **OpenAPI Documentation**: Interactive Swagger UI at [docs/swagger-ui.html](swagger-ui.html)

✅ **Jenkins CI/CD**: Automated deployment on push to `main` branch

✅ **GitHub Actions Alternative**: Simpler setup, same functionality

✅ **Deployment Script**: Robust bash script with health checks and logging

✅ **Comprehensive Documentation**: Full guides in [DEPLOYMENT.md](DEPLOYMENT.md)

**Everything is ready! Just push to `main` and watch it deploy automatically!**
