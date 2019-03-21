#!/usr/bin/env sh

FHIR_SERVER_BASE="http://localhost:8080/fhir"
SYNTHEA_PROJECT_ROOT="/home/mobile/IdeaProjects/github/synthea"
SYNTHEA_RUN="./run_synthea -P 10 California"
SYNTHEA_OUTPUT_FOLDER="${SYNTHEA_PROJECT_ROOT}/output/fhir"

TEXT_COLOR='\033[1;32m'
NO_COLOR='\033[0m'

printf "${TEXT_COLOR}\nCleaning output folder: ${SYNTHEA_OUTPUT_FOLDER}\n\n${NO_COLOR}"
rm -Rv ${SYNTHEA_OUTPUT_FOLDER}/*

printf "${TEXT_COLOR}\nGoing into synthea project root: ${SYNTHEA_PROJECT_ROOT}\n${NO_COLOR}"
cd ${SYNTHEA_PROJECT_ROOT}

printf "${TEXT_COLOR}\nRunning synthea with arguments: ${SYNTHEA_RUN}\n${NO_COLOR}"
eval ${SYNTHEA_RUN}

printf "${TEXT_COLOR}\nUploading generated data to FHIR Server : ${FHIR_SERVER_BASE}\n${NO_COLOR}"
for file in ${SYNTHEA_OUTPUT_FOLDER}/*
do
    printf "${TEXT_COLOR}\n\ncurl ${FHIR_SERVER_BASE} --data-binary \"@$file\" -H \"Content-Type: application/fhir+json\"\n${NO_COLOR}"
    curl ${FHIR_SERVER_BASE} --data-binary "@$file" -H "Content-Type: application/fhir+json"
done;
