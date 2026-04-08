# Swiss Terminology Provider Starter Project

This project a starter project for the Swiss Terminology Provider forked from the HAPI FHIR JPA starter project. The 
Swiss Terminology Provider is intended to be used in production to serve Value Sets, Code Systems, Concept Maps as 
well as the referenced terminologies (e.g., SNOMED CT with Swiss Extension, LOINC) for the public use.

The starter project shall be compiled in conjunction with the fork of the HAPI core libraries which extends the HAPI libraries 
on FHIR operations on ValueSets, CodeSystems and Concepts maps to match the FHIR Standard. For details see the 
[HAPI FHIR fork for the Swiss Terminology Provider](https://github.com/SwissHDS/hapi-fhir/) project.

## Prerequisites

To use this starter project, you shall:
- Swiss Terminology Provider [hapi-fhir](https://github.com/SwissHDS/hapi-fhir) or a fork of it checked out.
- [This project](https://github.com/SwissHDS/swiss-hds-terminology-provider) or a fork of it checked out.
- Java JDK installed: Minimum JDK 17 or newer.
- Apache Maven build tool (newest version).

## Optional Prerequisites

The starter project may run with a H2 in memory database for testing purposes. If you want to run the server with a 
database, a PostgreSQL database server (or any other database supported by HAPI FHIR JPA Server) shall run locally 
and must be configured in the _application.yaml_ file (see below). 

## Local build 

The local build option is currently the only option to run the server. To build the server locally,

1. Check out the hapi-fhir library for the terminology provider from https://github.com/SwissHDS/hapi-fhir.
2. Set a new version in the hapi-fhir for the terminology provider directory. E.g., if the current version is `8.7.0-SNAPSHOT`, replace it with something like `8.7.0-LOCAL`.
3. Run mvn wrapper:wrapper to create a wrapper script for the build.
4. Build the hapi-fhir for the terminology provider library with `./mvnw clean install -P FASTINSTALL`.
5. Check out this repository from https://github.com/SwissHDS/swiss-hds-terminology-provider.
6. Set the used hapi-fhir version in this terminology provider repository in _pom.xml_ to the one used in step 2, e.g., `8.7.0-LOCAL`.
7. Run mvn wrapper:wrapper to create a wrapper script for the build.
8. Build the terminology provider jar with `./mvnw clean install -P FASTINSTALL`.

## Running the starter project

The easiest way to run this server entirely depends on your environment requirements. The following ways are supported:

### Using jetty

```bash
mvn -Pjetty spring-boot:run
```

The Server will be accessible at http://localhost:8080/fhir and the CapabilityStatement will be found at http://localhost:8080/fhir/metadata.

### Using Spring Boot

```bash
mvn spring-boot:run
```

The Server will be accessible at http://localhost:8080/fhir and the CapabilityStatement will be found at http://localhost:8080/fhir/metadata.

If you want to run this server on a different port, you can change the port in the `src/main/resources/application.yaml` file as follows:

```yaml
server:
#  servlet:
#    context-path: /example/path
  port: 8888
```

## Deploy with docker compose

Docker compose is a simple option to build and deploy containers. To deploy with docker compose, you should build the project
with `mvn clean install` and then bring up the containers with `docker-compose up -d --build`. The server can be
reached at http://localhost:8080/.

In order to use another port, change the `ports` parameter
inside `docker-compose.yml` to `8888:8080`, where 8888 is a port of your choice.

The docker compose set also includes PostgreSQL database, if you choose to use PostgreSQL instead of H2, change the following
properties in `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://hapi-fhir-postgres:5432/hapi'
    username: admin
    password: admin
    driverClassName: org.postgresql.Driver
jpa:
  properties:
    hibernate.dialect: ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgresDialect
    hibernate.search.enabled: false

    # Then comment all hibernate.search.backend.*
```

## Running from IntelliJ

Make sure you run with the maven profile called ```boot``` and NOT also ```jetty```. Then you are ready to press debug
the project directly without any extra Application Servers.


## Configuration
The starter project looks in the environment variables for properties in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file for defaults. 
Much of this HAPI starter project can be configured using the yaml file in _src/main/resources/application.yaml_. 
By default, this starter project is configured to use H2 in memory database.

### PostgreSQL configuration

To configure the starter app to use PostgreSQL, instead of the default H2, update the application.yaml file to have the following:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/hapi'
    username: admin
    password: admin
    driverClassName: org.postgresql.Driver
  jpa:
    properties:
      hibernate.dialect: ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgresDialect
      hibernate.search.enabled: false

      # Then comment all hibernate.search.backend.*
```

### Using Elasticsearch

By default, the server will use embedded lucene indexes for terminology and fulltext indexing purposes. You can switch 
this to using Elasticsearch by editing the properties in [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml)

For example:

```properties
elasticsearch.enabled=true
elasticsearch.rest_url=localhost:9200
elasticsearch.username=SomeUsername
elasticsearch.password=SomePassword
elasticsearch.protocol=http
elasticsearch.required_index_status=YELLOW
elasticsearch.schema_management_strategy=CREATE
```

### Enabling Resource to be stored in Lucene Index

Set `hapi.fhir.store_resource_in_lucene_index_enabled` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable storing of 
resource json along with Lucene/Elasticsearch index mappings.

### Changing cached search results time

It is possible to change the cached search results time. The option `reuse_cached_search_results_millis` in the 
[application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) is 6000 milliseconds by default. Set `reuse_cached_search_results_millis: -1` in the 
[application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to ignore the cache time every search.

## Build the distroless variant of the image (for lower footprint and improved security)

The default Dockerfile contains a `release-distroless` stage to build a variant of the image
using the `gcr.io/distroless/java-debian10:11` base image:

```sh
docker build --target=release-distroless -t hapi-fhir:distroless .
```

Note that distroless images are also automatically built and pushed to the container registry,
see the `-distroless` suffix in the image tags.

### Enable OpenTelemetry auto-instrumentation

The container image includes the [OpenTelemetry Java auto-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation) Java agent JAR which can be used to 
export telemetry data for the HAPI FHIR JPA Server. You can enable it by specifying the `-javaagent` flag, for 
example by overriding the `JAVA_TOOL_OPTIONS` environment variable:

```sh
docker run --rm -it -p 8080:8080 \
  -e JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar" \
  -e OTEL_TRACES_EXPORTER="jaeger" \
  -e OTEL_SERVICE_NAME="hapi-fhir-server" \
  -e OTEL_EXPORTER_JAEGER_ENDPOINT="http://jaeger:14250" \
  docker.io/hapiproject/hapi:latest
```

You can configure the agent using environment variables or Java system properties, 
see <https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/> for details.
