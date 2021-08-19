FROM adoptopenjdk/openjdk11-openj9:alpine-slim
MAINTAINER oliver egger <oliver.egger@ahdis.ch>
EXPOSE 8080

COPY ./target/matchbox-validator.jar /app.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles

# for testing matchbox order
COPY ./with-preload/application.yaml /application.yaml
# end for testing

ENTRYPOINT java -Xmx1G -Xshareclasses -Xquickstart -jar /app.jar -Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:application.yaml

# docker build -t hapi-fhir-jpavalidator .
# docker run -p 8080:8080 hapi-fhir-jpavalidator:latest

