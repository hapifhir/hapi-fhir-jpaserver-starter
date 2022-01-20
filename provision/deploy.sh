#!/bin/bash

# Stop and remove container
docker stop hapi_fhir_server
# shellcheck disable=SC2046
docker container rm $(docker container ls -aq) -f

# Login to aws
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 105227342372.dkr.ecr.us-east-1.amazonaws.com

# Pull latest image
docker pull 105227342372.dkr.ecr.us-east-1.amazonaws.com/junipercds-hapi-fhir:latest

# Create container
docker run -p 8080:8080 -d --name hapi_fhir_server 105227342372.dkr.ecr.us-east-1.amazonaws.com/junipercds-hapi-fhir:latest

# Remove dangling images (cleanup)
# shellcheck disable=SC2046
docker image rm $(docker image ls -f "dangling=true" -q) -f

