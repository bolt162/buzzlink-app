# BuzzLink Deployment Guide

This guide covers the CI/CD setup and deployment process for BuzzLink.

## Table of Contents

1. [OpenAPI Documentation](#openapi-documentation)
2. [Jenkins CI/CD Setup](#jenkins-cicd-setup)
3. [GitHub Actions Alternative](#github-actions-alternative)
4. [Manual Deployment](#manual-deployment)
5. [Troubleshooting](#troubleshooting)

---

## OpenAPI Documentation

### Accessing the API Documentation

The BuzzLink API is documented using OpenAPI 3.0 specification.

**Local Development:**
```bash
# Serve the Swagger UI locally
cd docs
python3 -m http.server 8000

# Access at: http://localhost:8000/swagger-ui.html
```

**Production:**
```
http://184.169.147.113:8000/swagger-ui.html
```

### Files

- `docs/openapi.yaml` - OpenAPI 3.0 specification
- `docs/swagger-ui.html` - Interactive API documentation dashboard

### Features

- 📚 Complete API reference with all endpoints
- 🧪 Interactive testing (Try it out!)
- 📋 Request/response examples
- 🔐 Authentication configuration (Clerk JWT)
- 🏷️ Organized by tags (DMs, Workspaces, Analytics, etc.)

---

## Jenkins CI/CD Setup

### Overview

Jenkins is configured to automatically deploy BuzzLink to EC2 whenever code is pushed to the `main` branch.

### Prerequisites

1. **Jenkins Server** (can be installed on EC2 or separate server)
2. **SSH Access** to EC2 instance
3. **GitHub Repository** access
4. **Docker** installed on EC2

### Installation Steps

#### 1. Install Jenkins

**On Ubuntu/Debian:**
```bash
# Run the setup script
chmod +x scripts/setup-jenkins.sh
./scripts/setup-jenkins.sh
```

**Or manually:**
```bash
# Add Jenkins repository
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb http://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'

# Install Java and Jenkins
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk jenkins

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

#### 2. Configure Jenkins

1. **Access Jenkins**: http://YOUR_SERVER:8080
2. **Install Plugins**:
   - Git plugin
   - GitHub plugin
   - SSH Agent plugin
   - Docker Pipeline plugin
   - Pipeline Stage View
   - Credentials Binding plugin

3. **Add EC2 SSH Credentials**:
   - Go to: `Manage Jenkins → Credentials → System → Global credentials`
   - Click: `Add Credentials`
   - Type: `SSH Username with private key`
   - ID: `buzzlink-ec2-ssh-key`
   - Username: `ubuntu`
   - Private Key: Enter your EC2 SSH private key

#### 3. Create Pipeline Job

1. **New Item** → Name: `BuzzLink-Deploy` → Type: `Pipeline`

2. **Configure Pipeline**:
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/YOUR_USERNAME/BuzzLink.git`
   - **Credentials**: (if private repo)
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`

3. **Build Triggers**:
   - ✅ GitHub hook trigger for GITScm polling
   - ✅ Poll SCM: `H/2 * * * *` (every 2 minutes as fallback)

#### 4. Setup GitHub Webhook

1. Go to your **GitHub repository** → Settings → Webhooks
2. Click **Add webhook**
3. Configure:
   - **Payload URL**: `http://YOUR_JENKINS_SERVER:8080/github-webhook/`
   - **Content type**: `application/json`
   - **Events**: Just the push event
   - **Active**: ✅

#### 5. Prepare EC2 Instance

**On your EC2 instance:**

```bash
# Install Docker and Docker Compose
sudo apt-get update
sudo apt-get install -y docker.io docker-compose

# Add ubuntu user to docker group
sudo usermod -aG docker ubuntu
newgrp docker

# Clone repository
cd /home/ubuntu
git clone https://github.com/YOUR_USERNAME/BuzzLink.git
cd BuzzLink

# Create .env file with production settings
nano .env
```

**Example .env file:**
```env
# Database
POSTGRES_PASSWORD=your_secure_password

# Clerk Authentication
CLERK_PUBLIC_KEY=your_clerk_public_key
CLERK_SECRET_KEY=your_clerk_secret_key
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=your_clerk_publishable_key

# Email
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# URLs
APP_BASE_URL=http://184.169.147.113:3000
NEXT_PUBLIC_API_URL=http://184.169.147.113:8080
NEXT_PUBLIC_WS_URL=http://184.169.147.113:8080/ws
```

**Make deployment script executable:**
```bash
chmod +x scripts/deploy.sh
```

### Pipeline Workflow

1. **Trigger**: Push to `main` branch
2. **Checkout**: Pull latest code from GitHub
3. **Build Check**: Validate project structure
4. **Deploy**: SSH to EC2 and run deployment script
5. **Health Check**: Verify services are running
6. **Cleanup**: Remove old Docker resources
7. **Notify**: Report success/failure

### Deployment Script

The `scripts/deploy.sh` script handles:
- ✅ Pulling latest code
- ✅ Stopping old containers
- ✅ Building new Docker images
- ✅ Starting new containers
- ✅ Health checks
- ✅ Logging and error handling

---

## GitHub Actions Alternative

If you prefer GitHub Actions over Jenkins, use the provided workflow file.

### Setup

1. **Add SSH Key as GitHub Secret**:
   - Go to: Repository → Settings → Secrets and variables → Actions
   - Click: New repository secret
   - Name: `EC2_SSH_KEY`
   - Value: Your EC2 private SSH key

2. **Enable GitHub Actions**:
   - The workflow file is already in `.github/workflows/deploy.yml`
   - It will trigger automatically on push to `main`

3. **Monitor Deployments**:
   - Go to: Repository → Actions
   - View deployment logs and status

### Workflow Features

- ✅ Triggers on push to `main` or manual dispatch
- ✅ SSH deployment to EC2
- ✅ Docker container rebuild
- ✅ Health checks
- ✅ Automatic rollback on failure

---

## Manual Deployment

If you need to deploy manually:

```bash
# SSH to EC2
ssh ubuntu@184.169.147.113

# Navigate to project
cd /home/ubuntu/BuzzLink

# Run deployment script
./scripts/deploy.sh
```

Or step by step:

```bash
# Pull latest code
git pull origin main

# Stop containers
docker-compose down

# Build and start
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

---

## Troubleshooting

### Jenkins Issues

**Problem**: Jenkins can't connect to EC2
```bash
# Verify SSH credentials in Jenkins
# Check EC2 security group allows SSH from Jenkins server
# Test SSH manually: ssh ubuntu@184.169.147.113
```

**Problem**: Build fails with permission denied
```bash
# On EC2, check docker permissions:
sudo usermod -aG docker ubuntu
newgrp docker
```

### Docker Issues

**Problem**: Containers won't start
```bash
# Check logs
docker-compose logs

# Check disk space
df -h

# Restart Docker
sudo systemctl restart docker
```

**Problem**: Port already in use
```bash
# Find and stop conflicting containers
docker ps -a
docker stop <container_id>

# Or kill process using port
sudo lsof -ti:8080 | xargs kill -9
```

### Health Check Failures

**Backend fails health check:**
```bash
# Check backend logs
docker-compose logs backend

# Check database connection
docker-compose exec postgres psql -U buzzlink_user -d buzzlink

# Verify environment variables
docker-compose exec backend env | grep SPRING
```

**Frontend fails health check:**
```bash
# Check frontend logs
docker-compose logs frontend

# Verify build completed
docker-compose exec frontend ls -la

# Check environment variables
docker-compose exec frontend env | grep NEXT_PUBLIC
```

### GitHub Webhook Not Triggering

1. Check webhook delivery in GitHub settings
2. Verify Jenkins URL is accessible from internet
3. Check Jenkins GitHub plugin configuration
4. Use polling as fallback: `H/2 * * * *`

---

## Monitoring

### Check Application Status

```bash
# On EC2
docker-compose ps
docker-compose logs --tail=50

# Health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:3000
```

### View Deployment Logs

```bash
# Jenkins logs: Available in Jenkins UI
# Deployment script logs: /tmp/buzzlink-deploy-*.log
# Docker logs: docker-compose logs -f
```

---

## Security Best Practices

1. **Never commit .env files** - Use environment variables
2. **Secure SSH keys** - Store in Jenkins credentials, never in code
3. **Use HTTPS** - Configure SSL/TLS for production
4. **Firewall rules** - Limit access to necessary ports only
5. **Update dependencies** - Regularly update Docker images and packages
6. **Rotate secrets** - Change passwords and keys periodically

---

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Jenkins build logs
3. Check Docker container logs
4. Review GitHub Actions workflow runs (if using GitHub Actions)

---

## Summary

✅ OpenAPI documentation available at `docs/swagger-ui.html`
✅ Jenkins pipeline configured in `Jenkinsfile`
✅ Deployment script at `scripts/deploy.sh`
✅ GitHub Actions workflow at `.github/workflows/deploy.yml`
✅ Automatic deployment on push to `main` branch
✅ Health checks and rollback capability
