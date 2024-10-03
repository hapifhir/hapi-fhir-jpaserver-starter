#!/bin/bash

curl -X 'POST' \
  'http://host.docker.internal:8080/fhir/ImagingStudy' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
          "resourceType": "ImagingStudy",
          "status": "available",
          "subject": {
              "reference": "Patient/3"
          },
          "started": "2011-01-01T11:01:20+03:00",
          "series": [
              {
                  "uid": "series1",
                  "started": "2011-01-01T11:01:20+03:00",
                  "modality": {
                      "coding": [
                          {
                              "system": "https://dicom.nema.org/resources/ontology/DCM",
                              "code": "CT"
                          }
                      ]
                  },
                  "performer": [
                      {
                          "actor": {
                              "reference": "Organization/1",
                              "type": "Organization"
                          }
                      },
                      {
                          "actor": {
                              "reference": "Practitioner/2",
                              "type": "Practitioner"
                          }
                      }
                  ],
                  "bodySite": {
                      "concept": {
                          "coding": [
                              {
                                  "system": "https://snomed.info/sct",
                                  "code": "51185008",
                                  "display": "CT of Head"
                              }
                          ]
                      }
                  },
                  "instance": [
                      {
                          "uid": "instance201",
                          "sopClass": {
                              "system": "https://dicom.nema.org/resources/ontology/DCM",
                              "code": "CT01",
                              "display": "CT Head"
                          },
                          "title": "CT Head Image 1"
                      },
                      {
                          "uid": "instance202",
                          "sopClass": {
                              "system": "https://dicom.nema.org/resources/ontology/DCM",
                              "code": "CT02",
                              "display": "CT Head"
                          },
                          "title": "CT Head Image 2"
                      }
                  ]
              }
          ]
      }' > /dev/null