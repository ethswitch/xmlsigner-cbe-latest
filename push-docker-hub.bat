#!/bin/bash

# Exit on error
set -e

# ==== CONFIGURATION ====
DOCKER_USERNAME="your_dockerhub_username"
IMAGE_NAME="springboot-war-app"
IMAGE_TAG="latest"  # Or use a version tag like v1.0
FULL_IMAGE_NAME="$DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG"
# ========================

# Build the Docker image
echo "Building Docker image..."
docker build -t $FULL_IMAGE_NAME .

# Authenticate with Docker Hub
echo "Logging in to Docker Hub..."
docker login -u $DOCKER_USERNAME

# Push the image
echo "Pushing image to Docker Hub: $FULL_IMAGE_NAME"
docker push $FULL_IMAGE_NAME

echo "✅ Image pushed successfully!"
