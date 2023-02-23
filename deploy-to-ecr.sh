#!/bin/bash
############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "Script used to build and deploy the docker image on ECR"
   echo
   echo "Syntax: deploy.sh [h|r|g|p]"
   echo "options:"
   echo "h     Print this Help."
   echo "r     The AWS region where the ECR is located at"
   echo "g     The ECR registry, usually in the format aws_account_id.dkr.ecr.region.amazonaws.com"
   echo "p     The ECR repository"
   echo
   echo "Example: deploy.sh -e \"production\" -g aws_account_id.dkr.ecr.region.amazonaws.com -p my-repo"
   echo
}

############################################################
# Deploy                                                   #
# FROM https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html
############################################################
Deploy()
{
   echo "Build Docker image..."
   docker build -t fhir-server .

   echo "Push image to ECR with tag '$(git rev-parse --short HEAD)'..."
   aws ecr get-login-password --region $region | docker login --username AWS --password-stdin $registry
   # tag w/ current git commit SHA
   echo "Push image to ECR with tag 'latest'..."
   docker tag fhir-server $registry/$repository:$(git rev-parse --short HEAD)
   docker push $registry/$repository:$(git rev-parse --short HEAD)
   # tag w/ latest
   docker tag fhir-server $registry/$repository:latest
   docker push $registry/$repository:latest

   echo "Done!"
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################


############################################################
# Process the input options.                               #
############################################################
# Get the options
while getopts ":hr:g:p:" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      r) # the AWS region
         region=$OPTARG;;
      g) # the ECR registry
         registry=$OPTARG;;
      p) # the ECR repository
         repository=$OPTARG;;
      \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

if [[ -z "$region" ]]; then
    echo "No AWS region specified! -r must be set to the name of the AWS region where the ECR is located at"
    exit
fi

if [[ -z "$registry" ]]; then
    echo "No registry specified! -g must be set to the name of the ECR registry"
    exit
fi

if [[ -z "$repository" ]]; then
    echo "No repository specified! -p must be set to the name of the repository within the ECR registry"
    exit
fi

Deploy
