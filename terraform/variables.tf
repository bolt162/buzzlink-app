variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name for resource naming"
  type        = string
  default     = "buzzlink"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "public_key_path" {
  description = "Path to SSH public key"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "allowed_ssh_cidr" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = ["0.0.0.0/0"] # Restrict this in production to your IP
}

variable "github_repo" {
  description = "GitHub repository URL"
  type        = string
  default     = "https://github.com/yourusername/BuzzLink.git"
}

variable "clerk_publishable_key" {
  description = "Clerk publishable key"
  type        = string
  sensitive   = true
}

variable "postgres_password" {
  description = "PostgreSQL database password"
  type        = string
  sensitive   = true
  default     = "buzzlink_secure_password_123"
}
