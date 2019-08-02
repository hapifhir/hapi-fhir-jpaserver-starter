# Using maven with JDK 8
FROM maven:3.6.1-jdk-8 AS build

# Copy pom and download dependencies. This is done here
# so that docker caches the dependencies and they don't have to be
# re-downloaded on the next run, unless the pom file changes.
COPY pom.xml .
RUN /usr/local/bin/mvn-entrypoint.sh mvn verify clean --fail-never

# Copy all of the source code to the image and build it
COPY . .
RUN mvn package

FROM jetty:9-jre8-alpine

COPY --from=build ./target/hapi-fhir-jpaserver.war /var/lib/jetty/webapps/hapi-fhir-jpaserver.war

# Copy the default config file to the config directory location. It might be overridden by the docker host.
COPY --from=build ./src/main/resources/hapi.properties /hapi-config/hapi.properties

USER jetty:jetty
EXPOSE 8080
CMD ["java","-Dhapi.properties=/hapi-config/hapi.properties","-jar","/usr/local/jetty/start.jar"]