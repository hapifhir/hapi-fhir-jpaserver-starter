#!/bin/bash

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
  echo "Error: gcloud is not installed."
  echo "Please install gcloud by following the instructions at: https://cloud.google.com/sdk/docs/install"
  exit 1
fi

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Error: Both FULL_PATH and PROJECT_NAME parameters are required."
  echo "FULL_PATH: The source of the FHIR ndjson files."
  echo "PROJECT_NAME: The path in the bucket."
  echo "Usage: $0 FULL_PATH PROJECT_NAME"
  exit 1
fi

FULL_PATH=$1
PROJECT_NAME=$2

gcloud storage cp ${FULL_PATH}/*.ndjson gs://fhir-aggregator-public/${PROJECT_NAME}/META/ --content-type=application/fhir+ndjson --project=ncpi-rti-p01-007-ohsu