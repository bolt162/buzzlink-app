#!/bin/bash

# Setup script for AWS EC2 Ubuntu instance
# Run this script on your EC2 instance after SSH connection

set -e

echo "========================================="
echo "BuzzLink EC2 Setup Script"
echo "========================================="
echo ""

# Update system packages
echo "Step 1: Updating system packages..."
sudo apt-get update -y
sudo apt-get upgrade -y

# Install Docker
echo ""
echo "Step 2: Installing Docker..."
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up Docker repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add current user to docker group
sudo usermod -aG docker $USER

# Install Docker Compose (standalone)
echo ""
echo "Step 3: Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Git
echo ""
echo "Step 4: Installing Git..."
sudo apt-get install -y git

# Verify installations
echo ""
echo "Step 5: Verifying installations..."
docker --version
docker-compose --version
git --version

echo ""
echo "========================================="
echo "Setup Complete!"
echo "========================================="
echo ""
echo "IMPORTANT: You need to log out and log back in for Docker group changes to take effect."
echo "After logging back in, run: docker ps"
echo "If it works without sudo, you're ready to deploy!"
echo ""
echo "Next steps:"
echo "1. Log out: exit"
echo "2. Log back in: ssh -i your-key.pem ubuntu@your-ec2-ip"
echo "3. Clone your repository: git clone <your-repo-url>"
echo "4. Run the deploy script: cd BuzzLink && chmod +x scripts/deploy.sh && ./scripts/deploy.sh"
