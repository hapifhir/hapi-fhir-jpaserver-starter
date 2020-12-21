# HAPI-FHIR-CLI
FROM maven:3.6.3-jdk-11-slim as hapi-fhir-cli

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends wget tar bzip2 \
    && apt-get autoremove --purge -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /tmp/hapi-fhir-5.2.0-cli
RUN wget https://github.com/jamesagnew/hapi-fhir/releases/download/v5.2.0/hapi-fhir-5.2.0-cli.tar.bz2
RUN tar -xvjf hapi-fhir-5.2.0-cli.tar.bz2 -C /tmp/hapi-fhir-5.2.0-cli

# HAPI FHIR WEBAPP
FROM maven:3.6.3-jdk-11-slim as build-hapi
WORKDIR /tmp/hapi-fhir-jpaserver-starter

COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src/ /tmp/hapi-fhir-jpaserver-starter/src/
RUN mvn clean package spring-boot:repackage -Pboot

#FINAL IMAGE
FROM gcr.io/distroless/java:11

COPY --from=hapi-fhir-cli /tmp/hapi-fhir-5.2.0-cli /usr/local/hapi-fhir-5.2.0-cli/
ENV PATH="/usr/local/hapi-fhir-5.2.0-cli/:${PATH}"

COPY "populate-terminology.sh" "populate-terminology.sh"

COPY --from=build-hapi /tmp/hapi-fhir-jpaserver-starter/target/terminology.war /app/terminology.war

EXPOSE 8080
WORKDIR /app
CMD ["terminology.war"]