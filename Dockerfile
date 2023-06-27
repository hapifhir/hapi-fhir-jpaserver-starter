FROM maven:3.8-openjdk-17-slim as build-hapi
WORKDIR /tmp/hapi-fhir-jpaserver-starter

COPY pom.xml .
COPY server.xml .
ADD libs /root/.m2/repository/

COPY src/ /tmp/hapi-fhir-jpaserver-starter/src/
RUN mvn  -T 12 clean install -DskipTests -Djdk.lang.Process.launchMechanism=vfork -Dmaven.artifact.threads=30

FROM build-hapi AS build-distroless
RUN mvn package spring-boot:repackage -Pboot,replication
RUN mkdir /app && cp /tmp/hapi-fhir-jpaserver-starter/target/replicationRoot.war /app/main.war


########### bitnami tomcat version is suitable for debugging and comes with a shell
########### it can be built using eg. `docker build --target tomcat .`
FROM bitnami/tomcat:9.0 as tomcat

RUN rm -rf /opt/bitnami/tomcat/webapps/replicationRoot && \
    rm -rf /opt/bitnami/tomcat/webapps_default/replicationRoot && \
    mkdir -p /opt/bitnami/hapi/data/hapi/lucenefiles && \
    chmod 775 /opt/bitnami/hapi/data/hapi/lucenefiles

USER root
RUN mkdir -p /target && chown -R 1001:1001 target
USER 1001

COPY --chown=1001:1001 catalina.properties /opt/bitnami/tomcat/conf/catalina.properties
COPY --chown=1001:1001 server.xml /opt/bitnami/tomcat/conf/server.xml
COPY --from=build-hapi --chown=1001:1001 /tmp/hapi-fhir-jpaserver-starter/target/replicationRoot.war /opt/bitnami/tomcat/webapps_default/replicationRoot.war

ENV ALLOW_EMPTY_PASSWORD=yes

########### distroless brings focus on security and runs on plain spring boot - this is the default image
FROM gcr.io/distroless/java17:nonroot as default
COPY --chown=nonroot:nonroot --from=build-distroless /app /app
COPY --chown=65532:65532 images /app/images
# 65532 is the nonroot user's uid
# used here instead of the name to allow Kubernetes to easily detect that the container
# is running as a non-root (uid != 0) user.
USER 65532:65532
WORKDIR /app
CMD ["/app/main.war"]
