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
RUN mvn clean install -DskipTests

# download hapi-fhir-cli
RUN mkdir /tmp/hapi-fhir-5.2.0-cli
RUN wget https://github.com/jamesagnew/hapi-fhir/releases/download/v5.2.0/hapi-fhir-5.2.0-cli.tar.bz2
RUN tar -xvjf hapi-fhir-5.2.0-cli.tar.bz2 -C /tmp/hapi-fhir-5.2.0-cli

FROM tomcat:9.0.38-jdk11-openjdk-slim-buster

RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles
COPY --from=build-hapi /tmp/hapi-fhir-jpaserver-starter/target/ROOT.war /usr/local/tomcat/webapps/terminology.war

# copy hapi-fhir-cli
COPY --from=build-hapi /tmp/hapi-fhir-5.2.0-cli /usr/local/hapi-fhir-5.2.0-cli/
ENV PATH="/usr/local/hapi-fhir-5.2.0-cli/:${PATH}"

# copy upload-terminogy script
COPY "populate-terminology.sh" "populate-terminology.sh"

EXPOSE 8080

CMD ["catalina.sh", "run"]
