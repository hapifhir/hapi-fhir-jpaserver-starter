# HAPI-FHIR Starter Project

This project is a complete starter project you can use to deploy a FHIR server using HAPI FHIR JPA.

Note that this project is specifically intended for end users of the HAPI FHIR JPA server module (in other words, it helps you implement HAPI FHIR, it is not the source of the library itself). If you are looking for the main HAPI FHIR project, see here: https://github.com/hapifhir/hapi-fhir

Need Help? Please see: https://github.com/hapifhir/hapi-fhir/wiki/Getting-Help

## Prerequisites

In order to use this sample, you should have:

- [This project](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.

### and either
 - Oracle Java (JDK) installed: Minimum JDK8 or newer.
 - Apache Maven build tool (newest version)

### or
 - Docker, as the entire project can be built using multistage docker (with both JDK and maven wrapped in docker) or used directly from [Docker Hub](https://hub.docker.com/r/hapiproject/hapi)

## Running via [Docker Hub](https://hub.docker.com/r/hapiproject/hapi)

Each tagged/released version of `hapi-fhir-jpaserver` is built as a Docker image and published to Docker hub. To run the published Docker image from DockerHub:

```
docker pull hapiproject/hapi:latest
docker run -p 8080:8080 hapiproject/hapi:latest
```

This will run the docker image with the default configuration, mapping port 8080 from the container to port 8080 in the host. Once running, you can access `http://localhost:8080/` in the browser to access the HAPI FHIR server's UI or use `http://localhost:8080/fhir/` as the base URL for your REST requests.

If you change the mapped port, you need to change the configuration used by HAPI to have the correct `hapi.fhir.tester` property/value.

### Configuration via environment variables

You can customize HAPI directly from the `run` command using environment variables. For example:

```
docker run -p 8080:8080 -e hapi.fhir.default_encoding=xml hapiproject/hapi:latest
```

HAPI looks in the environment variables for properties in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file for defaults.

### Configuration via overridden application.yaml file and using Docker

You can customize HAPI by telling HAPI to look for the configuration file in a different location, eg.:

```
docker run -p 8090:8080 -v $(pwd)/yourLocalFolder:/configs -e "--spring.config.location=file:///configs/another.application.yaml" hapiproject/hapi:latest
```
Here, the configuration file (*another.application.yaml*) is placed locally in the folder *yourLocalFolder*.



```
docker run -p 8090:8080 -e "--spring.config.location=classpath:/another.application.yaml" hapiproject/hapi:latest
```
Here, the configuration file (*another.application.yaml*) is part of the compiled set of resources.

### Example using docker-compose.yml for docker-compose

```
version: '3.7'
services:
  web:
    image: "hapiproject/hapi:latest"
    ports:
      - "8090:8080"
    configs:
      - source: hapi
        target: /data/hapi/application.yaml
    volumes:
      - hapi-data:/data/hapi
    environment:
      SPRING_CONFIG_LOCATION: 'file:///data/hapi/application.yaml'
configs:
  hapi:
     external: true
volumes:
    hapi-data:
        external: true
```

## Running locally

The easiest way to run this server entirely depends on your environment requirements. At least, the following 4 ways are supported:

### Using jetty
```bash
mvn jetty:run
```


If you need to run this server on a different port (using Maven), you can change the port in the run command as follows:

```bash
mvn -Djetty.port=8888 jetty:run
```

Server will then be accessible at http://localhost:8888/ and eg. http://localhost:8888/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8888/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```

### Using Spring Boot with :run
```bash
mvn clean spring-boot:run -Pboot
```
Server will then be accessible at http://localhost:8080/ and eg. http://localhost:8080/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```

### Using Spring Boot
```bash
mvn clean package spring-boot:repackage -Pboot && java -jar target/ROOT.war
```
Server will then be accessible at http://localhost:8080/ and eg. http://localhost:8080/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```
### Using Spring Boot and Google distroless
```bash
mvn clean package com.google.cloud.tools:jib-maven-plugin:dockerBuild -Dimage=distroless-hapi && docker run -p 8080:8080 distroless-hapi
```
Server will then be accessible at http://localhost:8080/ and eg. http://localhost:8080/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```

### Using the Dockerfile and multistage build
```bash
./build-docker-image.sh && docker run -p 8080:8080 hapi-fhir/hapi-fhir-jpaserver-starter:latest
```
Server will then be accessible at http://localhost:8080/ and eg. http://localhost:8080/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```

## Configurations

Much of this HAPI starter project can be configured using the yaml file in _src/main/resources/application.yaml_. By default, this starter project is configured to use H2 as the database.

### MySql configuration

To configure the starter app to use MySQL, instead of the default H2, update the application.yaml file to have the following:

```yaml
spring:
  datasource:
    url: 'jdbc:mysql://localhost:3306/hapi_dstu3'
    username: admin
    password: admin
    driverClassName: com.mysql.jdbc.Driver
```

Also, make sure you are not setting the Hibernate dialect explicitly, in other words remove any lines similar to:

```
hibernate.dialect: {some none MySQL dialect}
```

On some systems, it might be necessary to override hibernate's default naming strategy. The naming strategy must be set using spring.jpa.hibernate.physical_naming_strategy.

```yaml
spring:
  jpa:
    hibernate.physical_naming_strategy: NAME_OF_PREFERRED_STRATEGY
```
On linux systems or when using docker mysql containers, it will be necessary to review the case-sensitive setup for
mysql schema identifiers. See  https://dev.mysql.com/doc/refman/8.0/en/identifier-case-sensitivity.html. We suggest you
set `lower_case_table_names=1` during mysql startup.

### PostgreSQL configuration

To configure the starter app to use PostgreSQL, instead of the default H2, update the application.yaml file to have the following:

```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/hapi_dstu3'
    username: admin
    password: admin
    driverClassName: org.postgresql.Driver
```

Because the integration tests within the project rely on the default H2 database configuration, it is important to either explicity skip the integration tests during the build process, i.e., `mvn install -DskipTests`, or delete the tests altogether. Failure to skip or delete the tests once you've configured PostgreSQL for the datasource.driver, datasource.url, and hibernate.dialect as outlined above will result in build errors and compilation failure.

### Microsoft SQL Server configuration

To configure the starter app to use MS SQL Server, instead of the default H2, update the application.yaml file to have the following:

```yaml
spring:
  datasource:
    url: 'jdbc:sqlserver://<server>:<port>;databaseName=<databasename>'
    username: admin
    password: admin
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
```


Because the integration tests within the project rely on the default H2 database configuration, it is important to either explicity skip the integration tests during the build process, i.e., `mvn install -DskipTests`, or delete the tests altogether. Failure to skip or delete the tests once you've configured PostgreSQL for the datasource.driver, datasource.url, and hibernate.dialect as outlined above will result in build errors and compilation failure.


NOTE: MS SQL Server by default uses a case-insensitive codepage. This will cause errors with some operations - such as when expanding case-sensitive valuesets (UCUM) as there are unique indexes defined on the terminology tables for codes.
It is recommended to deploy a case-sensitive database prior to running HAPI FHIR when using MS SQL Server to avoid these and potentially other issues.

## Customizing The Web Testpage UI

The UI that comes with this server is an exact clone of the server available at [http://hapi.fhir.org](http://hapi.fhir.org). You may skin this UI if you'd like. For example, you might change the introductory text or replace the logo with your own.

The UI is customized using [Thymeleaf](https://www.thymeleaf.org/) template files. You might want to learn more about Thymeleaf, but you don't necessarily need to: they are quite easy to figure out.

Several template files that can be customized are found in the following directory: [https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates)

## Deploying to an Application Server

Using the Maven-Embedded Jetty method above is convenient, but it is not a good solution if you want to leave the server running in the background.

Most people who are using HAPI FHIR JPA as a server that is accessible to other people (whether internally on your network or publically hosted) will do so using an Application Server, such as [Apache Tomcat](http://tomcat.apache.org/) or [Jetty](https://www.eclipse.org/jetty/). Note that any Servlet 3.0+ compatible Web Container will work (e.g Wildfly, Websphere, etc.).

Tomcat is very popular, so it is a good choice simply because you will be able to find many tutorials online. Jetty is a great alternative due to its fast startup time and good overall performance.

To deploy to a container, you should first build the project:

```bash
mvn clean install
```

This will create a file called `ROOT.war` in your `target` directory. This should be installed in your Web Container according to the instructions for your particular container. For example, if you are using Tomcat, you will want to copy this file to the `webapps/` directory.

Again, browse to the following link to use the server (note that the port 8080 may not be correct depending on how your server is configured).

[http://localhost:8080/](http://localhost:8080/)

You will then be able access the JPA server e.g. using http://localhost:8080/fhir/metadata.

If you would like it to be hosted at eg. hapi-fhir-jpaserver, eg. http://localhost:8080/hapi-fhir-jpaserver/ or http://localhost:8080/hapi-fhir-jpaserver/fhir/metadata - then rename the WAR file to ```hapi-fhir-jpaserver.war``` and adjust the overlay configuration accordingly e.g.

```yaml
    tester:
      -
          id: home
          name: Local Tester
          server_address: 'http://localhost:8080/hapi-fhir-jpaserver/fhir'
          refuse_to_fetch_third_party_urls: false
          fhir_version: R4
```


## Deploy with docker compose

Docker compose is a simple option to build and deploy container. To deploy with docker compose, you should build the project
with `mvn clean install` and then bring up the containers with `docker-compose up -d --build`. The server can be
reached at http://localhost:8080/.

In order to use another port, change the `ports` parameter
inside `docker-compose.yml` to `8888:8080`, where 8888 is a port of your choice.

The docker compose set also includes my MySQL database, if you choose to use MySQL instead of H2, change the following
properties in application.yaml:

```yaml
spring:
  datasource:
    url: 'jdbc:mysql://hapi-fhir-mysql:3306/hapi'
    username: admin
    password: admin
    driverClassName: com.mysql.jdbc.Driver
```

Also, make sure you are not setting the Hibernate Dialect explicitly, see more details in the section about MySQL.

## Running hapi-fhir-jpaserver directly from IntelliJ as Spring Boot
Make sure you run with the maven profile called ```boot``` and NOT also ```jetty```. Then you are ready to press debug the project directly without any extra Application Servers.

## Running hapi-fhir-jpaserver-example in Tomcat from IntelliJ

Install Tomcat.

Make sure you have Tomcat set up in IntelliJ.

- File->Settings->Build, Execution, Deployment->Application Servers
- Click +
- Select "Tomcat Server"
- Enter the path to your tomcat deployment for both Tomcat Home (IntelliJ will fill in base directory for you)

Add a Run Configuration for running hapi-fhir-jpaserver-example under Tomcat

- Run->Edit Configurations
- Click the green +
- Select Tomcat Server, Local
- Change the name to whatever you wish
- Uncheck the "After launch" checkbox
- On the "Deployment" tab, click the green +
- Select "Artifact"
- Select "hapi-fhir-jpaserver-example:war"
- In "Application context" type /hapi

Run the configuration.

- You should now have an "Application Servers" in the list of windows at the bottom.
- Click it.
- Select your server, and click the green triangle (or the bug if you want to debug)
- Wait for the console output to stop

Point your browser (or fiddler, or what have you) to `http://localhost:8080/hapi/baseDstu3/Patient`

It is important to use MySQL5Dialect when using MySQL version 5+.

## Enabling Subscriptions

The server may be configured with subscription support by enabling properties in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file:

- `hapi.fhir.subscription.resthook_enabled` - Enables REST Hook subscriptions, where the server will make an outgoing connection to a remote REST server

- `hapi.fhir.subscription.email.*` - Enables email subscriptions. Note that you must also provide the connection details for a usable SMTP server.

- `hapi.fhir.subscription.websocket_enabled` - Enables websocket subscriptions. With this enabled, your server will accept incoming websocket connections on the following URL (this example uses the default context path and port, you may need to tweak depending on your deployment environment): [ws://localhost:8080/websocket](ws://localhost:8080/websocket)

## Enabling CQL

Set `hapi.fhir.cql_enabled=true` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable [Clinical Quality Language](https://cql.hl7.org/) on this server.

## Enabling MDM (EMPI)

Set `hapi.fhir.mdm_enabled=true` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable MDM on this server.  The MDM matching rules are configured in [mdm-rules.json](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/mdm-rules.json).  The rules in this example file should be replaced with actual matching rules appropriate to your data. Note that MDM relies on subscriptions, so for MDM to work, subscriptions must be enabled.

## Using Elasticsearch

By default, the server will use embedded lucene indexes for terminology and fulltext indexing purposes. You can switch this to using lucene by editing the properties in [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml)

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

## Enabling LastN

Set `hapi.fhir.lastn_enabled=true` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable the $lastn operation on this server.  Note that the $lastn operation relies on Elasticsearch, so for $lastn to work, indexing must be enabled using Elasticsearch.

## Enabling Resource to be stored in Lucene Index

Set `hapi.fhir.store_resource_in_lucene_index_enabled` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to enable storing of resource json along with Lucene/Elasticsearch index mappings.

## Changing cached search results time

It is possible to change the cached search results time. The option `reuse_cached_search_results_millis` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) is 6000 miliseconds by default.
Set `reuse_cached_search_results_millis: -1` in the [application.yaml](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/application.yaml) file to ignore the cache time every search.

## Build the distroless variant of the image (for lower footprint and improved security)

The default Dockerfile contains a `release-distroless` stage to build a variant of the image
using the `gcr.io/distroless/java-debian10:11` base image:

```sh
docker build --target=release-distroless -t hapi-fhir:distroless .
```

Note that distroless images are also automatically built and pushed to the container registry,
see the `-distroless` suffix in the image tags.

## Adding custom operations

To add a custom operation, refer to the documentation in the core hapi-fhir libraries [here](https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations_operations.html).

Within `hapi-fhir-jpaserver-starter`, create a generic class (that does not extend or implement any classes or interfaces), add the `@Operation` as a method within the generic class, and then register the class as a provider using `RestfulServer.registerProvider()`.

## Enable OpenTelemetry auto-instrumentation

The container image includes the [OpenTelemetry Java auto-instrumentation](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
Java agent JAR which can be used to export telemetry data for the HAPI FHIR JPA Server. You can enable it by specifying the `-javaagent` flag,
for example by overriding the `JAVA_TOOL_OPTIONS` environment variable:

```sh
docker run --rm -it -p 8080:8080 \
  -e JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar" \
  -e OTEL_TRACES_EXPORTER="jaeger" \
  -e OTEL_SERVICE_NAME="hapi-fhir-server" \
  -e OTEL_EXPORTER_JAEGER_ENDPOINT="http://jaeger:14250" \
  docker.io/hapiproject/hapi:latest
```

You can configure the agent using environment variables or Java system properties, see <https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/> for details.
