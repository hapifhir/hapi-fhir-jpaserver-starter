#!/bin/bash

curl -X POST \
  -H "Content-Type: application/json" \
  -d @./combined-bundles.json \
  http://localhost:8080/fhir
