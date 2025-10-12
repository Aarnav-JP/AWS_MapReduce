#!/bin/bash

# Build script for Hadoop MapReduce project
echo "Building Hadoop MapReduce Movie Analysis Application..."

# Create build directory
mkdir -p build
cd build

# Set Hadoop classpath
export HADOOP_CLASSPATH=$JAVA_HOME/lib/tools.jar

# Download Hadoop if not present (for Mac - using Homebrew)
if ! command -v hadoop &> /dev/null; then
    echo "Hadoop not found. Please install using: brew install hadoop"
    exit 1
fi

# Copy Java files to build directory
cp ../*.java .

# Compile Java files
echo "Compiling Java files..."
hadoop com.sun.tools.javac.Main *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
    exit 1
fi

# Create JAR file
echo "Creating JAR file..."
jar cf movie-analysis.jar *.class

if [ $? -eq 0 ]; then
    echo "JAR file created successfully: movie-analysis.jar"
else
    echo "JAR creation failed!"
    exit 1
fi

# Move JAR to parent directory
mv movie-analysis.jar ..

echo "Build completed successfully!"
echo "JAR file location: ./movie-analysis.jar"
