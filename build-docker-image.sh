#!/bin/sh

docker build -t hapi-fhir/hapi-fhir-jpaserver-ods -t 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-ods:6.1.0-0.1.0 .
docker push 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-ods:6.1.0-0.1.0