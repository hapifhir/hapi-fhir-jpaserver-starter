#!/bin/bash
############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "Script used to build and deploy the docker image on ECR"
   echo
   echo "Syntax: deploy.sh [h|e|s|d]"
   echo "options:"
   echo "h     Print this Help."
   echo "e     The environment to which to deploy to. Must be one of production|sandbox|staging"
   echo
   echo "Example: deploy.sh -e \"production\""
   echo
}

############################################################
# Deploy                                                   #
############################################################
Deploy()
{
   # if [[ "$diff" == "true" ]]; then
   #    cmd="diff"
   # else
   #    cmd="deploy"
   # fi
   # echo "$cmd'ing to env $env"
   echo "Deploying to env $env"
   # if [[ "$env" == "staging" ]]; then
   #    npm run prep-deploy-staging
   # else
   #    npm run prep-deploy
   # fi
   # cd ../

   # FROM https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html
   docker build -t hapi-fhir:distroless .
   aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 463519787594.dkr.ecr.us-east-2.amazonaws.com
   # tag w/ current git commit SHA
   docker tag fhir-server 463519787594.dkr.ecr.us-east-2.amazonaws.com/metriport/fhir-server:$(git rev-parse --short HEAD)
   docker push 463519787594.dkr.ecr.us-east-2.amazonaws.com/metriport/fhir-server:$(git rev-parse --short HEAD)
   # tag w/ latest
   docker tag fhir-server 463519787594.dkr.ecr.us-east-2.amazonaws.com/metriport/fhir-server:latest
   docker push 463519787594.dkr.ecr.us-east-2.amazonaws.com/metriport/fhir-server:latest

   # cd infra
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
while getopts ":he:" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      e) # the environment to deploy to
         env=$OPTARG;;
      \?) # Invalid option
          echo "Error: Invalid option"
          exit;;
   esac
done

if [[ "$env" =~ ^production|sandbox|staging$ ]]; then
    Deploy
else
    echo "Invalid environment! -e must be one of production|sandbox|staging"
fi