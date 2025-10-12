# AWS_MapReduce

Project Overview
This project demonstrates a full Big Data pipeline on AWS. It processes the 335MB MovieLens (https://grouplens.org/datasets/movielens/latest/) dataset using a custom Hadoop MapReduce application to derive key movie analytics. The entire infrastructure, an AWS EMR cluster, is provisioned automatically using Terraform.

The core analytical tasks are:

Compute the average rating for each movie.

For each genre, find the Top-10 movies with the highest average rating, considering only movies with at least 50 ratings.

The project encompasses Infrastructure as Code (IaC), distributed data processing, and AWS cloud operations.

Architecture & Workflow
Infrastructure Provisioning: Terraform scripts create a scalable, multi-node AWS EMR cluster.

Data & Code Storage: The MovieLens dataset and the compiled MapReduce JAR file are uploaded to a dedicated S3 bucket.

Distributed Processing: The analytics job is executed on the EMR cluster using Hadoop, leveraging the MapReduce paradigm for parallel computation.

Results & Validation: The final output, containing average ratings and genre-based top movies, is generated and can be retrieved from HDFS.

Key Features
Infrastructure as Code (IaC): Fully automated, repeatable EMR cluster deployment using Terraform.

Multi-Step MapReduce Logic:

Job 1: Calculates the average rating and total number of ratings for each movie.

Job 2: Uses the output from Job 1 to rank movies within each genre and select the top 10 with a minimum of 50 ratings.

Secure AWS Configuration: EMR cluster is launched in a dedicated VPC with appropriate security groups.

End-to-End Automation: Scripts and commands provided for every step, from cluster creation to job execution.

Technologies Used
Cloud & Infrastructure: AWS (EMR, EC2, S3, IAM), Terraform

Big Data Processing: Apache Hadoop, MapReduce (Java)

Languages: Java, HCL (Terraform)

Prerequisites
An AWS account with appropriate permissions (EMR, EC2, S3, IAM).

AWS CLI configured on your local machine.

Terraform installed locally.

Java JDK and Hadoop for local development/testing (optional).
