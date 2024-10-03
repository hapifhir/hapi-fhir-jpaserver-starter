#!/bin/bash

curl -X 'POST' \
  'http://localhost:8502/metadata_endpoint/add-one' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "ura_number": 12345678,
  "data_domain": "beeldbank",
  "endpoint": "http://host.docker.internal:8080/fhir",
  "request_type": "GET",
  "parameters": []
}' > /dev/null


curl -X POST http://host.docker.internal:8080/fhir \
   -H "Content-Type: application/json" \
   --data @fhir_examples/bundle.json > /dev/null