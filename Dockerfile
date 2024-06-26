FROM docker.io/library/maven:3.9.7-eclipse-temurin-17 AS build-hapi
WORKDIR /tmp/hapi-fhir-server

ARG OPENTELEMETRY_JAVA_AGENT_VERSION=1.33.3
RUN curl -LSsO https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar

COPY pom.xml .
COPY server.xml .
RUN mvn -ntp dependency:go-offline

COPY src/ /tmp/hapi-fhir-server/src/
RUN mvn clean install -DskipTests -Djdk.lang.Process.launchMechanism=vfork

FROM build-hapi AS build-distroless
RUN mvn package -DskipTests spring-boot:repackage -Pboot
RUN mkdir /app && cp /tmp/hapi-fhir-server/target/ROOT.war /app/main.war

FROM gcr.io/distroless/java17-debian12:nonroot AS default

USER 65532:65532
WORKDIR /app

COPY --chown=nonroot:nonroot --from=build-distroless /app /app
COPY --chown=nonroot:nonroot --from=build-hapi /tmp/hapi-fhir-server/opentelemetry-javaagent.jar /app

EXPOSE 8080
ENTRYPOINT ["java", "--class-path", "/app/main.war", "-Dloader.path=main.war!/WEB-INF/classes/,main.war!/WEB-INF/,/app/extra-classes", "org.springframework.boot.loader.PropertiesLauncher"]
