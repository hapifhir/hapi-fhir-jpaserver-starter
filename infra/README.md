# Metriport FHIR Server Infrastructure

This package is used to build and update the infrastructure required for Metriport's FHIR Server.

It uses AWS CDK + TypeScript.

## Deploy to AWS

It requires AWS CLI setup properly.

Run these commands on the terminal from the root folder of this project:

1. `cd infra`
1. `cdk bootstrap -c env=<env>` (only needs to be run once)
1. `cdk deploy -c env=<env> BaseStack`
1. `cd ../`
1. `./deploy.sh -e <env> -g <ecr-registry> -p <ecr-repository>`
  - this will build the Docker image and deploy it to AWS ECR
1. `cdk deploy -c env=<env> FHIRServerStack`