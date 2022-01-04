#!/bin/bash

CURRENT_DIR=$(pwd)
JSON=/provision/combined-bundles.json
FILE=$CURRENT_DIR$JSON

curl -X POST \
  -H "Content-Type: application/json" \
  -d @$FILE \
  http://localhost:8080/fhir
