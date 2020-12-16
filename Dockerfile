FROM maven:3.6.3-jdk-11-slim as build-hapi
WORKDIR /tmp/hapi-fhir-jpaserver-starter

RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends wget tar bzip2 \
    && apt-get autoremove --purge -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn -ntp dependency:go-offline

COPY src/ /tmp/hapi-fhir-jpaserver-starter/src/
RUN mvn clean package spring-boot:repackage -Pboot

FROM gcr.io/distroless/java:11

COPY hapi-fhir-5.2.0-cli/ /usr/local/hapi-fhir-5.2.0-cli/
ENV PATH="/usr/local/hapi-fhir-5.2.0-cli/:${PATH}"

# copy upload-terminogy script
COPY "populate-terminology.sh" "populate-terminology.sh"

COPY --from=build-hapi /tmp/hapi-fhir-jpaserver-starter/target/terminology.war /app/terminology.war

EXPOSE 8080
WORKDIR /app
CMD ["terminology.war"]