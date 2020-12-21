#!/bin/sh

echo "hapi-fhir-cli upload-terminology -d /data/SnomedCT_InternationalRF2_PRODUCTION_20200731T120000Z.zip -v r4 -t $HAPI_FHIR_URL -u http://snomed.info/sct"
hapi-fhir-cli upload-terminology -d /data/SnomedCT_InternationalRF2_PRODUCTION_20200731T120000Z.zip -v r4 -t $HAPI_FHIR_URL -u http://snomed.info/sct

echo "hapi-fhir-cli upload-terminology -d /data/SnomedCT_InternationalRF2_PRODUCTION_20200731T120000Z.zip -v r4 -t $HAPI_FHIR_URL -u http://snomed.info/sct"
hapi-fhir-cli upload-terminology -d /data/Loinc_2.68.zip -v r4 -t $HAPI_FHIR_URL -u http://loinc.org