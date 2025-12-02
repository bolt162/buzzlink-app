output "instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.buzzlink_server.id
}

output "instance_public_ip" {
  description = "Public IP address of the EC2 instance"
  value       = aws_eip.buzzlink_eip.public_ip
}

output "instance_public_dns" {
  description = "Public DNS name of the EC2 instance"
  value       = aws_instance.buzzlink_server.public_dns
}

output "frontend_url" {
  description = "Frontend application URL"
  value       = "http://${aws_eip.buzzlink_eip.public_ip}:3000"
}

output "backend_url" {
  description = "Backend API URL"
  value       = "http://${aws_eip.buzzlink_eip.public_ip}:8080"
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh -i ~/.ssh/id_rsa ubuntu@${aws_eip.buzzlink_eip.public_ip}"
}

output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.buzzlink_vpc.id
}

output "security_group_id" {
  description = "Security Group ID"
  value       = aws_security_group.buzzlink_sg.id
}
