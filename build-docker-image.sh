#!/bin/sh

mvn package spring-boot:repackage -Pboot
docker build -t hapi-fhir-service .
