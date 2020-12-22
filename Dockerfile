# FROM maven:3.6.3-jdk-11-slim as build-hapi
# WORKDIR /tmp/hapi-fhir-jpavalidator-starter
# 
# COPY pom.xml .
# RUN mvn -ntp dependency:go-offline
# 
# COPY src/ /tmp/hapi-fhir-jpavalidator-starter/src/
# RUN mvn clean install -DskipTests
# RUN mvn test
# 
# FROM tomcat:9.0.38-jdk11-openjdk-slim-buster
FROM adoptopenjdk/openjdk11-openj9:alpine-slim
MAINTAINER oliver egger <oliver.egger@ahdis.ch>
EXPOSE 8080

COPY ./target/hapi-fhir-jpavalidator.jar /app.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles


RUN java -Xmx1G -Xms1G -jar /app.jar --hapi.fhir.only_install_packages=true 

ENTRYPOINT java -Xmx1G -Xshareclasses -Xquickstart -jar /app.jar

# docker build -t hapi-fhir-jpavalidator .
# docker run -p 8080:8080 hapi-fhir-jpavalidator:latest

