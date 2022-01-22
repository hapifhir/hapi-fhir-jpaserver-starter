#!/bin/bash

CURRENT_DIR=$(pwd)
RESOURCES="/provision/resources/*"
FILES=$CURRENT_DIR$RESOURCES

for file in $FILES
do
  curl -X POST \
    -H "Content-Type: application/json" \
    -d @"$file" \
    http://localhost:8080/fhir
done
