#!/bin/sh

docker build -t hapi-fhir/hapi-fhir-jpaserver-dc4h -t 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-dc4h:6.1.0-0.1.0 .
docker push 548749374595.dkr.ecr.eu-west-2.amazonaws.com/hapi-fhir-jpaserver-dc4h:6.1.0-0.1.0