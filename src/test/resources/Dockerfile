FROM ubuntu as clone
RUN apt-get update && apt-get install -y git
WORKDIR /usr/home/git
RUN git clone https://github.com/openmrs/openmrs-contrib-keycloak-smart-auth.git


FROM maven:3.6.3-jdk-11 as builder
WORKDIR /usr/home/app
COPY --from=clone /usr/home/git/openmrs-contrib-keycloak-smart-auth .
RUN mvn -e -B package -DskipTests


FROM jboss/keycloak:14.0.0
COPY --from=builder /usr/home/app/openmrs-keycloak-ear/target/openmrs-keycloak-smart-authentication-*.ear $JBOSS_HOME/standalone/deployments/
COPY keycloak/realm.json /tmp/realm.json

ENV KEYCLOAK_USER="admin"
ENV KEYCLOAK_PASSWORD="admin"
ENV KEYCLOAK_FRONTEND_URL="http://localhost:8080/auth/"
ENV DB_VENDOR="POSTGRES"
ENV DB_USER="keycloak"
ENV DB_PASSWORD="keycloak"
ENV KEYCLOAK_IMPORT="/tmp/realm.json"

