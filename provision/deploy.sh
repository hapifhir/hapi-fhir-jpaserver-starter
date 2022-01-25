#!/bin/bash

# login to aws
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 105227342372.dkr.ecr.us-east-1.amazonaws.com

# pull latest image
docker pull 105227342372.dkr.ecr.us-east-1.amazonaws.com/junipercds-hapi-fhir:latest

# stop container
docker stop hapi_fhir_server

# remove container
docker container rm $(docker container ls -aq) -f

# run new container
docker run -p 8080:8080 -d --name hapi_fhir_server 105227342372.dkr.ecr.us-east-1.amazonaws.com/junipercds-hapi-fhir:latest

# remove dangling images
docker image rm $(docker image ls -f dangling=true -q) -f

# reload nginx
nginx -t && service nginx reload

