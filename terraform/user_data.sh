#!/bin/bash
set -e

# Log everything
exec > >(tee /var/log/user-data.log)
exec 2>&1

echo "=========================================="
echo "BuzzLink Infrastructure Setup Starting..."
echo "=========================================="

# Update system
echo "[1/8] Updating system packages..."
apt-get update
apt-get upgrade -y

# Install required packages
echo "[2/8] Installing required packages..."
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    unzip \
    wget

# Install Docker
echo "[3/8] Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
systemctl enable docker
systemctl start docker

# Install Docker Compose
echo "[4/8] Installing Docker Compose..."
DOCKER_COMPOSE_VERSION="2.24.5"
curl -L "https://github.com/docker/compose/releases/download/v$${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# Add ubuntu user to docker group
usermod -aG docker ubuntu

# Install Java 17
echo "[5/8] Installing Java 17..."
apt-get install -y openjdk-17-jdk

# Install Node.js and npm
echo "[6/8] Installing Node.js 20..."
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs

# Clone repository
echo "[7/8] Cloning repository..."
cd /home/ubuntu
if [ -d "BuzzLink" ]; then
    rm -rf BuzzLink
fi
git clone ${github_repo} BuzzLink
chown -R ubuntu:ubuntu BuzzLink

# Setup environment files
echo "[8/8] Setting up environment files..."
cd /home/ubuntu/BuzzLink

# Backend environment
cat > backend/.env << 'EOF'
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/buzzlink
SPRING_DATASOURCE_USERNAME=buzzlink_user
SPRING_DATASOURCE_PASSWORD=${postgres_password}
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SERVER_PORT=8080
EOF

# Frontend environment
cat > frontend/.env.local << 'EOF'
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=${clerk_publishable_key}
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080
EOF

# Update docker-compose.yml with correct password
sed -i 's/POSTGRES_PASSWORD: buzzlink_password/POSTGRES_PASSWORD: ${postgres_password}/' docker-compose.yml

chown -R ubuntu:ubuntu /home/ubuntu/BuzzLink

# Create systemd service for auto-start
cat > /etc/systemd/system/buzzlink.service << 'EOF'
[Unit]
Description=BuzzLink Application
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/BuzzLink
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
User=ubuntu
Group=ubuntu

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
systemctl daemon-reload
systemctl enable buzzlink.service

# Start the application
echo "=========================================="
echo "Starting BuzzLink application..."
echo "=========================================="
cd /home/ubuntu/BuzzLink
sudo -u ubuntu docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 30

# Display status
docker-compose ps

echo "=========================================="
echo "BuzzLink Infrastructure Setup Complete!"
echo "=========================================="
echo "Frontend: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000"
echo "Backend:  http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "=========================================="
