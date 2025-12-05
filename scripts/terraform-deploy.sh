#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=========================================="
echo "BuzzLink Terraform Deployment Script"
echo -e "==========================================${NC}"

# Change to terraform directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/../terraform"

cd "$TERRAFORM_DIR"

# Check if terraform is installed
if ! command -v terraform &> /dev/null; then
    echo -e "${RED}Error: Terraform is not installed!${NC}"
    echo "Install Terraform from: https://www.terraform.io/downloads"
    exit 1
fi

# Check if terraform.tfvars exists
if [ ! -f terraform.tfvars ]; then
    echo -e "${YELLOW}Warning: terraform.tfvars not found!${NC}"
    echo "Creating terraform.tfvars from example..."
    cp terraform.tfvars.example terraform.tfvars
    echo -e "${RED}Please edit terraform.tfvars with your actual values before proceeding!${NC}"
    exit 1
fi

echo -e "${YELLOW}[1/5] Initializing Terraform...${NC}"
terraform init

echo -e "${YELLOW}[2/5] Validating Terraform configuration...${NC}"
terraform validate

echo -e "${YELLOW}[3/5] Planning infrastructure changes...${NC}"
terraform plan -out=tfplan

echo ""
echo -e "${YELLOW}Review the plan above. Do you want to apply these changes? (yes/no)${NC}"
read -r response

if [[ "$response" != "yes" ]]; then
    echo -e "${RED}Deployment cancelled.${NC}"
    exit 0
fi

echo -e "${YELLOW}[4/5] Applying Terraform configuration...${NC}"
terraform apply tfplan

echo -e "${YELLOW}[5/5] Retrieving outputs...${NC}"
terraform output

echo ""
echo -e "${GREEN}=========================================="
echo "Infrastructure Deployment Complete!"
echo -e "==========================================${NC}"

# Get outputs
INSTANCE_IP=$(terraform output -raw instance_public_ip)
FRONTEND_URL=$(terraform output -raw frontend_url)
BACKEND_URL=$(terraform output -raw backend_url)
SSH_CMD=$(terraform output -raw ssh_command)

echo ""
echo -e "${GREEN}Instance Public IP:${NC} $INSTANCE_IP"
echo -e "${GREEN}Frontend URL:${NC} $FRONTEND_URL"
echo -e "${GREEN}Backend URL:${NC} $BACKEND_URL"
echo ""
echo -e "${GREEN}SSH Command:${NC}"
echo "$SSH_CMD"
echo ""
echo -e "${YELLOW}Note: Wait 2-3 minutes for the application to fully start.${NC}"
echo -e "${YELLOW}The EC2 instance is installing Docker, cloning the repo, and starting services.${NC}"
echo ""
echo -e "${GREEN}To check deployment status, SSH into the instance and run:${NC}"
echo "  docker ps"
echo "  docker-compose logs -f"
echo ""
echo -e "${GREEN}Deployment complete!${NC}"
