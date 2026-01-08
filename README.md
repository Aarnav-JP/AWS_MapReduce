# 🎬 Movie Analytics with AWS MapReduce

[![AWS](https://img.shields.io/badge/AWS-EMR-FF9900?logo=amazon-aws&logoColor=white)](https://aws.amazon.com/emr/)
[![Terraform](https://img.shields.io/badge/Terraform-IaC-623CE4?logo=terraform&logoColor=white)](https://www.terraform.io/)
[![Hadoop](https://img.shields.io/badge/Hadoop-MapReduce-66CCFF?logo=apache&logoColor=white)](https://hadoop.apache.org/)
[![Java](https://img.shields.io/badge/Java-Development-007396?logo=java&logoColor=white)](https://www.java.com/)

A production-grade **Big Data analytics pipeline** that processes the 335MB MovieLens dataset using a custom Hadoop MapReduce application on AWS EMR. The entire infrastructure is provisioned automatically using **Infrastructure as Code (Terraform)**.

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Prerequisites](#-prerequisites)
- [Project Structure](#-project-structure)
- [Setup & Deployment](#-setup--deployment)
- [Usage](#-usage)
- [Analytics Outputs](#-analytics-outputs)
- [Results](#-results)
- [Clean Up](#-clean-up)
- [Demo](#-demo)
- [License](#-license)

## 🌟 Overview

This project demonstrates a complete Big Data processing pipeline using AWS cloud services. It analyzes the [MovieLens Latest Dataset](https://grouplens.org/datasets/movielens/latest/) (335MB, 33+ million ratings) to deliver actionable movie analytics through distributed computing.

### Core Analytics Tasks

1. **Average Movie Ratings**: Calculate the mean rating and total number of ratings for each movie
2. **Genre-Based Top Movies**: Identify the Top-10 highest-rated movies per genre (minimum 50 ratings threshold)

## ✨ Features

- 🏗️ **Infrastructure as Code**: Fully automated EMR cluster deployment using Terraform
- ⚡ **Multi-Step MapReduce Pipeline**: Two chained MapReduce jobs for complex analytics
- 🔒 **Secure AWS Configuration**: Dedicated VPC with properly configured security groups
- 📦 **S3 Integration**: Automated data and code storage management
- 🔄 **End-to-End Automation**: Complete scripts for building, deploying, and executing
- 📊 **Large-Scale Processing**: Handles 33M+ ratings and 86K+ movies efficiently
- 🎯 **Production-Ready**: Multi-node cluster with proper resource allocation

## 🏛️ Architecture

```
┌─────────────────┐
│   Developer     │
│   (Your PC)     │
└────────┬────────┘
         │
         ├─ terraform apply
         ├─ build.sh
         ├─ aws s3 cp
         │
    ┌────v────────────────────────────────────────┐
    │             AWS Cloud                       │
    │                                              │
    │  ┌─────────────────────────────────────┐   │
    │  │   S3 Bucket (Data & Code Storage)   │   │
    │  │  • movies.csv                        │   │
    │  │  • ratings.csv                       │   │
    │  │  • movie-analysis.jar                │   │
    │  └──────────────┬──────────────────────┘   │
    │                 │                            │
    │  ┌──────────────v──────────────────────┐   │
    │  │       EMR Cluster (Hadoop)          │   │
    │  │                                      │   │
    │  │  ┌───────────────────────────────┐  │   │
    │  │  │  Master Node (m5.xlarge)      │  │   │
    │  │  │  • YARN ResourceManager       │  │   │
    │  │  │  • HDFS NameNode              │  │   │
    │  │  └───────────────────────────────┘  │   │
    │  │                                      │   │
    │  │  ┌───────────┐   ┌───────────┐     │   │
    │  │  │  Worker 1 │   │  Worker 2 │     │   │
    │  │  │(m5.xlarge)│   │(m5.xlarge)│     │   │
    │  │  └───────────┘   └───────────┘     │   │
    │  │                                      │   │
    │  │  MapReduce Job Flow:                │   │
    │  │  Job 1: ratings.csv → avg ratings   │   │
    │  │  Job 2: movies.csv + Job1 → top10   │   │
    │  └──────────────────────────────────────┘   │
    │                                              │
    │  ┌─────────────────────────────────────┐   │
    │  │   HDFS Output                        │   │
    │  │  • /output1 (avg ratings)            │   │
    │  │  • /output2 (genre top-10)           │   │
    │  └─────────────────────────────────────┘   │
    └──────────────────────────────────────────────┘
```

### Workflow

1. **Provision Infrastructure**: Terraform creates EMR cluster, S3 buckets, IAM roles, and security groups
2. **Upload Data & Code**: Dataset and compiled JAR uploaded to S3
3. **Distributed Processing**: MapReduce jobs execute on multi-node cluster
4. **Results Retrieval**: Final analytics available in HDFS

## 🛠️ Technologies

| Category | Technologies |
|----------|-------------|
| **Cloud Platform** | AWS (EMR, EC2, S3, IAM, VPC) |
| **Infrastructure** | Terraform (HashiCorp Configuration Language) |
| **Big Data Framework** | Apache Hadoop 3.x (EMR 6.15.0) |
| **Processing Paradigm** | MapReduce |
| **Programming Language** | Java 8 |
| **Build Tools** | Bash, JAR packaging |

## ⚙️ Prerequisites

Before running this project, ensure you have:

- ✅ **AWS Account** with appropriate permissions (EMR, EC2, S3, IAM)
- ✅ **AWS CLI** configured with credentials (`aws configure`)
- ✅ **Terraform** installed (v1.0+)
- ✅ **Java JDK 8** or higher
- ✅ **Hadoop** (optional, for local development/testing)
- ✅ **SSH Key Pair** created in AWS for EMR access

### Installation Commands

```bash
# Install AWS CLI (macOS)
brew install awscli

# Install Terraform
brew install terraform

# Install Java (if needed)
brew install openjdk@8

# Install Hadoop (optional)
brew install hadoop
```

## 📁 Project Structure

```
movie-analysis-project/
├── data/                           # MovieLens dataset (335MB+)
│   ├── movies.csv                  # Movie metadata (86K movies)
│   ├── ratings.csv                 # User ratings (33M+ ratings)
│   ├── tags.csv                    # User-generated tags
│   ├── links.csv                   # Links to IMDB/TMDB
│   ├── genome-scores.csv           # Tag relevance scores
│   └── genome-tags.csv             # Tag descriptions
│
├── MovieRatingMapper.java          # Job 1: Map ratings to movies
├── MovieRatingReducer.java         # Job 1: Calculate avg ratings
├── GenreTopMoviesMapper.java       # Job 2: Map movies with ratings
├── GenreTopMoviesReducer.java      # Job 2: Find top-10 per genre
├── MovieAnalysisDriver.java        # Main driver (orchestrates jobs)
│
├── build.sh                        # Build script (compile & package)
├── movie-analysis.jar              # Compiled JAR file
├── the2.tf                         # Terraform infrastructure config
├── command.txt                     # AWS deployment commands
└── README.md                       # This file
```

## 🚀 Setup & Deployment

### Step 1: Configure Terraform Variables

Edit `the2.tf` and update the following variables:

```hcl
variable "student_id" {
  default = "your-unique-id"  # Change this to your unique identifier
}

variable "key_pair_name" {
  default = "your-key-pair"   # Change to your AWS key pair name
}
```

### Step 2: Build the Application

```bash
# Compile Java code and create JAR
chmod +x build.sh
./build.sh
```

This creates `movie-analysis.jar` in the project root.

### Step 3: Provision Infrastructure

```bash
# Initialize Terraform
terraform init

# Review infrastructure plan
terraform plan

# Deploy EMR cluster and resources
terraform apply
# Type 'yes' when prompted
```

**Note**: Cluster provisioning takes ~10-15 minutes.

### Step 4: Upload Data and Code to S3

```bash
# Set your S3 bucket name (from Terraform output)
BUCKET_NAME=$(terraform output -raw s3_bucket_name)

# Upload Java source files
aws s3 cp MovieRatingMapper.java s3://$BUCKET_NAME/src/
aws s3 cp MovieRatingReducer.java s3://$BUCKET_NAME/src/
aws s3 cp GenreTopMoviesMapper.java s3://$BUCKET_NAME/src/
aws s3 cp GenreTopMoviesReducer.java s3://$BUCKET_NAME/src/
aws s3 cp MovieAnalysisDriver.java s3://$BUCKET_NAME/src/

# Upload dataset
aws s3 cp data/ratings.csv s3://$BUCKET_NAME/input/
aws s3 cp data/movies.csv s3://$BUCKET_NAME/input/
```

## 💻 Usage

### Step 1: Connect to EMR Master Node

```bash
# Get master node DNS from Terraform
MASTER_DNS=$(terraform output -raw master_public_dns)

# SSH into master node
ssh -i your-key-pair.pem hadoop@$MASTER_DNS
```

### Step 2: Prepare Environment on EMR

```bash
# Verify cluster is ready
yarn node -list

# Download source code from S3
BUCKET_NAME="your-bucket-name"  # Use your bucket
aws s3 cp s3://$BUCKET_NAME/src/ ./ --recursive

# Compile Java code
export HADOOP_CLASSPATH=$JAVA_HOME/lib/tools.jar
hadoop com.sun.tools.javac.Main *.java

# Package into JAR
jar cf movie-analysis.jar *.class
```

### Step 3: Prepare HDFS

```bash
# Create input directory
hdfs dfs -mkdir -p /input

# Clean up any previous outputs
hdfs dfs -rm -r /output1 2>/dev/null || true
hdfs dfs -rm -r /output2 2>/dev/null || true

# Download and upload data to HDFS
aws s3 cp s3://$BUCKET_NAME/input/ratings.csv ./
aws s3 cp s3://$BUCKET_NAME/input/movies.csv ./

hdfs dfs -put ratings.csv /input/
hdfs dfs -put movies.csv /input/
```

### Step 4: Run MapReduce Jobs

```bash
# Execute the two-stage MapReduce pipeline
hadoop jar movie-analysis.jar MovieAnalysisDriver /input /output1 /output2
```

### Step 5: View Results

```bash
# View average ratings (Job 1 output)
hdfs dfs -cat /output1/part-r-00000 | head -20

# View top-10 movies per genre (Job 2 output)
hdfs dfs -cat /output2/part-r-00000 | head -50
```

## 📊 Analytics Outputs

### Job 1: Average Movie Ratings

**Format**: `MovieID\t{Title}\tAvgRating\tNumRatings`

```
1    Toy Story (1995)        3.92    247
2    Jumanji (1995)          3.43    187
356  Forrest Gump (1994)     4.16    329
```

### Job 2: Top-10 Movies Per Genre

**Format**: `Genre\tRank\t{Title}\tAvgRating\tNumRatings`

```
Action    1    The Dark Knight (2008)           4.35    1247
Action    2    Inception (2010)                 4.31    986
...
Drama     1    The Shawshank Redemption (1994)  4.49    1523
Drama     2    The Godfather (1972)             4.42    1167
```

## 🎯 Results

The MapReduce pipeline processes:
- **33+ million ratings**
- **86,000+ movies**
- **20+ genres**

And produces:
- Complete average ratings for all rated movies
- Top-10 highest-rated movies for each genre (with ≥50 ratings)
- Processing time: ~5-8 minutes on a 3-node cluster

## 🧹 Clean Up

### Destroy Infrastructure

```bash
# Terminate EMR cluster and delete all resources
terraform destroy
# Type 'yes' when prompted
```

**Important**: This will delete:
- EMR cluster
- S3 buckets and all data
- IAM roles and policies
- Security groups

## 🎥 Demo

A video demonstration of the project execution and explanation is available:

**[View Demo Videos](https://drive.google.com/drive/folders/12q7SfpATKqhqUh1wGvl55CYssQtqLEKT?usp=drive_link)**

The demo includes:
- `execution.mov` - Complete deployment and execution walkthrough
- `explanation.mov` - Technical architecture and code explanation

## 📝 License

This project is for educational purposes as part of cloud computing coursework.

---

**Dataset Credit**: [GroupLens Research - MovieLens Latest Dataset](https://grouplens.org/datasets/movielens/latest/)

**Author**: BITS Pilani Cloud Computing Assignment  
**Course**: Cloud Computing (SEM3)
