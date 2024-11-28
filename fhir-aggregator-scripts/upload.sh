#!/bin/bash


if [ -z "$1" ]; then
  echo "Usage: $0 <directory>"
  exit 1
fi

# remove trailing /
DIRECTORY="${1%/}"

CMD_PREFIX='aws s3api put-object --bucket fhir-aggregator-public'
CMD_SUFFIX='--content-type application/fhir+ndjson'

for file in $(find $DIRECTORY -name '*.ndjson'); do
  # key=$(echo $file | sed "s|$DIRECTORY/||")
  key=$file
  echo "Uploading $file to s3://fhir-aggregator-public/$key"
  $CMD_PREFIX --key $key --body $file $CMD_SUFFIX
done

