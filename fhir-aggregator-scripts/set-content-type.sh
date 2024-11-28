aws s3 cp \
       s3://fhir-aggregator-public/ \
       s3://fhir-aggregator-public/ \
       --exclude '*' \
       --include '*.ndjson' \
       --no-guess-mime-type \
       --content-type="application/fhir+ndjson" \
       --metadata-directive="REPLACE" \
       --recursive
