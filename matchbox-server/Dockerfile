FROM bellsoft/liberica-openjdk-alpine:latest
MAINTAINER oliver egger <oliver.egger@ahdis.ch>
EXPOSE 8080

COPY ./target/matchbox.jar /matchbox.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles

ENTRYPOINT java -Xmx1G -jar /matchbox.jar -Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:application.yaml

# docker buildx build -t eu.gcr.io/fhir-ch/matchbox:v314 -f ./Dockerfile --push --platform=linux/arm64,linux/amd64 .
# OSX: docker pull --platform linux/arm64 eu.gcr.io/fhir-ch/matchbox:v314
# Linux: docker pull --platform linux/amnd64 eu.gcr.io/fhir-ch/matchbox:v314