# Configure AWS Provider
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Configure AWS Provider with your region
provider "aws" {
  region = "ap-south-1"  
}

# Data source to get default VPC
data "aws_vpc" "default" {
  default = true
}

# Data source to get subnets in default VPC
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# IAM role for EMR service
resource "aws_iam_role" "emr_service_role" {
  name = "EMR_DefaultRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "elasticmapreduce.amazonaws.com"
        }
      }
    ]
  })
}

# Attach AWS managed policy to EMR service role
resource "aws_iam_role_policy_attachment" "emr_service_policy" {
  role       = aws_iam_role.emr_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"
}

# IAM role for EC2 instances
resource "aws_iam_role" "emr_ec2_instance_profile" {
  name = "EMR_EC2_DefaultRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Attach AWS managed policy to EC2 instance role
resource "aws_iam_role_policy_attachment" "emr_ec2_policy" {
  role       = aws_iam_role.emr_ec2_instance_profile.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceforEC2Role"
}

# Create instance profile
resource "aws_iam_instance_profile" "emr_profile" {
  name = "EMR_EC2_DefaultRole"
  role = aws_iam_role.emr_ec2_instance_profile.name
}

# Security group for EMR master
resource "aws_security_group" "emr_master" {
  name_prefix = "emr-master-"
  vpc_id      = data.aws_vpc.default.id

  # SSH access
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "EMR Master Security Group"
  }
}

# Security group for EMR workers
resource "aws_security_group" "emr_worker" {
  name_prefix = "emr-worker-"
  vpc_id      = data.aws_vpc.default.id

  # All inbound from master
  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.emr_master.id]
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "EMR Worker Security Group"
  }
}

# EMR Cluster
resource "aws_emr_cluster" "movie_analysis_cluster" {
  name          = "movie-analysis-cluster"
  release_label = "emr-6.15.0"
  applications  = ["Hadoop", "Spark"]
  
  service_role = aws_iam_role.emr_service_role.arn

  ec2_attributes {
    key_name                          = var.key_pair_name  
    subnet_id                         = tolist(data.aws_subnets.default.ids)[0]
    emr_managed_master_security_group = aws_security_group.emr_master.id
    emr_managed_slave_security_group  = aws_security_group.emr_worker.id
    instance_profile                  = aws_iam_instance_profile.emr_profile.arn
  }

  master_instance_group {
    instance_type = "m5.xlarge"
  }

  core_instance_group {
    instance_type  = "m5.xlarge"
    instance_count = 2
  }

  configurations_json = jsonencode([
    {
      "Classification" : "hadoop-env",
      "Configurations" : [
        {
          "Classification" : "export",
          "Properties" : {
            "JAVA_HOME" : "/usr/lib/jvm/java-1.8.0"
          }
        }
      ]
    }
  ])

  log_uri = "s3://${aws_s3_bucket.emr_logs.bucket}/logs/"

  tags = {
    Name        = "MovieAnalysisCluster"
    Environment = "Dev"
  }

  # Keep cluster running
  keep_job_flow_alive_when_no_steps = true
  termination_protection             = false
}

# S3 bucket for EMR logs
resource "aws_s3_bucket" "emr_logs" {
  bucket        = "${var.student_id}-emr-logs"
  force_destroy = true

  tags = {
    Name = "EMR Logs Bucket"
  }
}

# S3 bucket for data and JAR files
resource "aws_s3_bucket" "student_bucket" {
  bucket        = var.student_id
  force_destroy = true

  tags = {
    Name = "Student Data Bucket"
  }
}

# Variables
variable "student_id" {
  description = "Your student ID"
  type        = string
  default     = "MyBITSID75"  
}

variable "key_pair_name" {
  description = "AWS Key Pair name for SSH access"
  type        = string
  default     = ""  
}

# Outputs
output "cluster_id" {
  description = "EMR Cluster ID"
  value       = aws_emr_cluster.movie_analysis_cluster.id
}

output "master_public_dns" {
  description = "EMR Master node public DNS"
  value       = aws_emr_cluster.movie_analysis_cluster.master_public_dns
}

output "s3_bucket_name" {
  description = "S3 bucket name for data"
  value       = aws_s3_bucket.student_bucket.bucket
}
