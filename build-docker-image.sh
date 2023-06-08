#!/bin/sh
# Version History
# 0.1.3    - Allows for the flag to be switched to use Offset searches only


IMAGE_NAME=jpaserver-ods
HAPI_FHIR_VERSION=6.1.0
UKS_VERSION=0.1.3   
#docker build -t hapi-fhir/hapi-fhir-jpaserver-dc4h -t 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-dc4h:6.1.0-0.1.0 .
#docker push 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-dc4h:6.1.0-0.1.0
aws ecr get-login-password --region eu-west-1 --profile dedalus-uk-sales-ansible | docker login --username AWS --password-stdin 350801433917.dkr.ecr.eu-west-1.amazonaws.com
docker build --no-cache -t 350801433917.dkr.ecr.eu-west-1.amazonaws.com/products/solutioning/dc4h-deploy/3p/hapifhir/$IMAGE_NAME:$HAPI_FHIR_VERSION-$UKS_VERSION .
docker push 350801433917.dkr.ecr.eu-west-1.amazonaws.com/products/solutioning/dc4h-deploy/3p/hapifhir/$IMAGE_NAME:$HAPI_FHIR_VERSION-$UKS_VERSION

IMAGE_NAME=jpaserver-ods-tomcat
aws ecr create-repository --profile dedalus-uk-sales-ansible --region eu-west-1  --registry-id 350801433917 --repository-name products/solutioning/dc4h-deploy/3p/hapifhir/$IMAGE_NAME
docker build   --target tomcat -t 350801433917.dkr.ecr.eu-west-1.amazonaws.com/products/solutioning/dc4h-deploy/3p/hapifhir/$IMAGE_NAME:$HAPI_FHIR_VERSION-$UKS_VERSION .
docker push 350801433917.dkr.ecr.eu-west-1.amazonaws.com/products/solutioning/dc4h-deploy/3p/hapifhir/$IMAGE_NAME:$HAPI_FHIR_VERSION-$UKS_VERSION