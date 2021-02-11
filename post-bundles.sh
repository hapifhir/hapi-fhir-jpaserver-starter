#!/bin/bash
fhir_url="http://localhost:8080/fhir"
bundle_folder="./output/fhir"
hospital_file="hospitalInformation1612272534044.json"
practitioner_file="practitionerInformation1612272534044.json"

curl -H 'Content-Type: application/fhir+json' -d @"$bundle_folder/$hospital_file" $fhir_url

curl -H 'Content-Type: application/fhir+json'  -d @"$bundle_folder/$practitioner_file" $fhir_url

i=0
for file in "$bundle_folder"/*; do
  filename="${file##*/}"
  if [ "$filename" != "$hospital_file" ] && [ "$filename" != "$practitioner_file" ]; then
    curl -H 'Content-Type: application/fhir+json' -d @"$bundle_folder/$filename" $fhir_url &
    ((i++))
    if [ "$i" -eq 20 ]; then
      break
    fi
  fi
done

wait
