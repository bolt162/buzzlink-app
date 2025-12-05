#!/bin/bash

# BuzzLink Terraform Setup Helper Script
# This script helps you configure terraform.tfvars interactively

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}=========================================="
echo "BuzzLink Terraform Setup Helper"
echo -e "==========================================${NC}"
echo ""

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/../terraform"

cd "$TERRAFORM_DIR"

# Check if terraform.tfvars already exists
if [ -f terraform.tfvars ]; then
    echo -e "${YELLOW}terraform.tfvars already exists!${NC}"
    read -p "Do you want to overwrite it? (yes/no): " overwrite
    if [[ "$overwrite" != "yes" ]]; then
        echo "Exiting without changes."
        exit 0
    fi
fi

echo -e "${GREEN}Let's configure your Terraform variables...${NC}"
echo ""

# Get AWS region
echo -e "${YELLOW}AWS Configuration${NC}"
read -p "Enter AWS region [us-east-1]: " aws_region
aws_region=${aws_region:-us-east-1}

# Get project name
read -p "Enter project name [buzzlink]: " project_name
project_name=${project_name:-buzzlink}

# Get instance type
read -p "Enter EC2 instance type [t2.micro]: " instance_type
instance_type=${instance_type:-t2.micro}

echo ""
echo -e "${YELLOW}SSH Configuration${NC}"

# Get SSH key path
read -p "Enter path to your SSH public key [~/.ssh/id_rsa.pub]: " public_key_path
public_key_path=${public_key_path:-~/.ssh/id_rsa.pub}

# Expand tilde to home directory
public_key_path="${public_key_path/#\~/$HOME}"

# Check if SSH key exists
if [ ! -f "$public_key_path" ]; then
    echo -e "${RED}Warning: SSH public key not found at $public_key_path${NC}"
    echo "You can generate one with: ssh-keygen -t rsa -b 4096"
fi

# Get user's IP address
echo ""
echo -e "${YELLOW}Detecting your public IP address...${NC}"
user_ip=$(curl -s https://checkip.amazonaws.com)
if [ -z "$user_ip" ]; then
    user_ip=$(curl -s https://api.ipify.org)
fi

if [ ! -z "$user_ip" ]; then
    echo -e "${GREEN}Detected IP: $user_ip${NC}"
    read -p "Use this IP for SSH access? (yes/no) [yes]: " use_detected_ip
    use_detected_ip=${use_detected_ip:-yes}

    if [[ "$use_detected_ip" == "yes" ]]; then
        allowed_ssh_cidr="$user_ip/32"
    else
        read -p "Enter your IP address (format: x.x.x.x/32): " allowed_ssh_cidr
    fi
else
    read -p "Enter your IP address for SSH access (format: x.x.x.x/32): " allowed_ssh_cidr
fi

echo ""
echo -e "${YELLOW}Application Configuration${NC}"

# Get GitHub repo
read -p "Enter your GitHub repository URL: " github_repo
while [ -z "$github_repo" ]; do
    echo -e "${RED}GitHub repository URL is required!${NC}"
    read -p "Enter your GitHub repository URL: " github_repo
done

echo ""
echo -e "${YELLOW}Clerk Configuration${NC}"

# Get Clerk publishable key
read -p "Enter your Clerk publishable key (starts with pk_): " clerk_key
while [ -z "$clerk_key" ]; do
    echo -e "${RED}Clerk publishable key is required!${NC}"
    echo "Get it from: https://dashboard.clerk.com"
    read -p "Enter your Clerk publishable key: " clerk_key
done

echo ""
echo -e "${YELLOW}Database Configuration${NC}"

# Get PostgreSQL password
read -sp "Enter a secure PostgreSQL password: " postgres_password
echo ""
while [ -z "$postgres_password" ]; do
    echo -e "${RED}PostgreSQL password is required!${NC}"
    read -sp "Enter a secure PostgreSQL password: " postgres_password
    echo ""
done

# Create terraform.tfvars
cat > terraform.tfvars << EOF
# AWS Configuration
aws_region    = "$aws_region"
project_name  = "$project_name"
instance_type = "$instance_type"

# SSH Configuration
public_key_path  = "$public_key_path"
allowed_ssh_cidr = ["$allowed_ssh_cidr"]

# Application Configuration
github_repo = "$github_repo"

# Clerk Configuration
clerk_publishable_key = "$clerk_key"

# Database Configuration
postgres_password = "$postgres_password"
EOF

echo ""
echo -e "${GREEN}=========================================="
echo "Configuration Complete!"
echo -e "==========================================${NC}"
echo ""
echo -e "${GREEN}terraform.tfvars has been created with your settings.${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "1. Review terraform.tfvars to ensure all values are correct"
echo "2. Run: ./scripts/terraform-deploy.sh"
echo "   OR"
echo "   cd terraform && terraform init && terraform apply"
echo ""
echo -e "${GREEN}Ready to deploy!${NC}"
