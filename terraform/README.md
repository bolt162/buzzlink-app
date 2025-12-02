# BuzzLink Infrastructure as Code (Terraform)

This directory contains Terraform configuration for automated infrastructure setup and deployment of BuzzLink on AWS.

## 🏗️ Architecture

The Terraform configuration provisions:

- **VPC** with public subnet and internet gateway
- **EC2 Instance** (t2.micro) running Ubuntu 22.04
- **Security Group** with appropriate inbound/outbound rules
- **Elastic IP** for persistent public IP address
- **Automated deployment** via user data script

## 📋 Prerequisites

1. **AWS Account** with appropriate permissions
2. **Terraform** installed (>= 1.0)
   ```bash
   # macOS
   brew install terraform

   # Linux
   wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
   unzip terraform_1.6.0_linux_amd64.zip
   sudo mv terraform /usr/local/bin/
   ```

3. **AWS CLI** configured with credentials
   ```bash
   aws configure
   ```

4. **SSH Key Pair** generated
   ```bash
   ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
   ```

5. **GitHub Repository** with your BuzzLink code pushed

## 🚀 Quick Start

### Step 1: Configure Variables

Copy the example variables file and edit it with your values:

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:

```hcl
# AWS Configuration
aws_region    = "us-east-1"
project_name  = "buzzlink"
instance_type = "t2.micro"

# SSH Configuration
public_key_path  = "~/.ssh/id_rsa.pub"
allowed_ssh_cidr = ["YOUR_IP_ADDRESS/32"]  # Get your IP from https://whatismyip.com

# Application Configuration
github_repo = "https://github.com/YOUR_USERNAME/BuzzLink.git"

# Clerk Configuration
clerk_publishable_key = "pk_test_xxxxx"  # From Clerk Dashboard

# Database Configuration
postgres_password = "change_this_to_secure_password"
```

### Step 2: Initialize Terraform

```bash
cd terraform
terraform init
```

This downloads the required providers and modules.

### Step 3: Plan Infrastructure

Preview the changes Terraform will make:

```bash
terraform plan
```

Review the output to ensure everything looks correct.

### Step 4: Deploy Infrastructure

Apply the Terraform configuration:

```bash
terraform apply
```

Type `yes` when prompted to confirm.

Alternatively, use the automated deployment script:

```bash
cd ..
./scripts/terraform-deploy.sh
```

### Step 5: Wait for Deployment

The EC2 instance will automatically:
1. Install Docker and Docker Compose
2. Clone your GitHub repository
3. Set up environment files
4. Build and start the application containers

This takes approximately **3-5 minutes**.

### Step 6: Access Your Application

After deployment completes, Terraform will output:

```
instance_public_ip = "54.123.45.67"
frontend_url = "http://54.123.45.67:3000"
backend_url = "http://54.123.45.67:8080"
ssh_command = "ssh -i ~/.ssh/id_rsa ubuntu@54.123.45.67"
```

Visit the frontend URL in your browser!

## 🔧 Useful Commands

### View Outputs

```bash
terraform output
terraform output instance_public_ip
```

### SSH into Instance

```bash
ssh -i ~/.ssh/id_rsa ubuntu@$(terraform output -raw instance_public_ip)
```

### Check Application Status

```bash
# SSH into instance first
docker ps
docker-compose ps
docker-compose logs -f
```

### Update Application

```bash
# SSH into instance
cd /home/ubuntu/BuzzLink
git pull origin main
docker-compose down
docker-compose up -d --build
```

### Destroy Infrastructure

**Warning:** This will delete all resources!

```bash
terraform destroy
```

## 📁 File Structure

```
terraform/
├── main.tf                 # Main infrastructure configuration
├── variables.tf            # Variable definitions
├── outputs.tf              # Output definitions
├── user_data.sh            # EC2 initialization script
├── terraform.tfvars.example # Example variables file
└── README.md               # This file
```

## 🔐 Security Group Rules

The security group allows the following inbound traffic:

| Port | Protocol | Source | Purpose |
|------|----------|--------|---------|
| 22 | TCP | Your IP | SSH access |
| 80 | TCP | 0.0.0.0/0 | HTTP |
| 443 | TCP | 0.0.0.0/0 | HTTPS |
| 3000 | TCP | 0.0.0.0/0 | Next.js Frontend |
| 8080 | TCP | 0.0.0.0/0 | Spring Boot Backend |
| 5432 | TCP | Your IP | PostgreSQL (admin only) |

## 🐛 Troubleshooting

### Application Not Starting

1. **SSH into the instance:**
   ```bash
   ssh -i ~/.ssh/id_rsa ubuntu@$(terraform output -raw instance_public_ip)
   ```

2. **Check user data logs:**
   ```bash
   sudo cat /var/log/user-data.log
   ```

3. **Check Docker containers:**
   ```bash
   docker ps -a
   docker-compose logs
   ```

### Can't SSH into Instance

1. **Check security group allows your IP:**
   ```bash
   curl https://checkip.amazonaws.com
   ```
   Update `allowed_ssh_cidr` in `terraform.tfvars` with this IP.

2. **Apply changes:**
   ```bash
   terraform apply
   ```

### Port Already in Use

If you see "port already allocated" errors:

```bash
# SSH into instance
sudo lsof -i :3000  # Check what's using port 3000
docker-compose down
docker-compose up -d
```

### Database Connection Issues

1. **Check PostgreSQL container:**
   ```bash
   docker logs buzzlink-postgres
   ```

2. **Verify environment variables:**
   ```bash
   cat /home/ubuntu/BuzzLink/backend/.env
   ```

3. **Restart services:**
   ```bash
   docker-compose restart backend
   ```

## 💰 Cost Estimation

With **t2.micro** (AWS Free Tier eligible):

- **EC2 Instance:** Free for first 750 hours/month (first 12 months)
- **Elastic IP:** Free when attached to running instance
- **Data Transfer:** 1 GB/month free, then $0.09/GB

**Estimated cost:** $0/month (free tier) or ~$8-10/month after free tier.

## 🔄 CI/CD Integration

To integrate with GitHub Actions:

1. Add AWS credentials to GitHub Secrets:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`

2. Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to AWS

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Configure AWS Credentials
        uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v2

      - name: Terraform Apply
        working-directory: terraform
        run: |
          terraform init
          terraform apply -auto-approve
```

## 📚 Additional Resources

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS EC2 User Guide](https://docs.aws.amazon.com/ec2/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## 🎯 Production Recommendations

For production deployments, consider:

1. **Use RDS for PostgreSQL** instead of container
2. **Add Application Load Balancer** for HTTPS and auto-scaling
3. **Use S3 + CloudFront** for static assets
4. **Enable VPC Flow Logs** for network monitoring
5. **Use Parameter Store/Secrets Manager** for sensitive values
6. **Set up CloudWatch** for logging and monitoring
7. **Implement Auto Scaling Groups** for high availability
8. **Use Route53** for custom domain name
9. **Enable SSL/TLS** with ACM certificates
10. **Set up automated backups** for database

## 📝 License

This infrastructure code is part of the BuzzLink project.
