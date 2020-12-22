# HAPI-FHIR JPA Validator Starter Project

This project is a  starter project you can use to deploy a FHIR server using HAPI FHIR JPA validaton with the npm implementation support.

- Implementation Guides can be added to hapi.properties and will be loaded during startup of the server, currently configured are the swiss implementation guides relevant for the Swiss EPR Projectathon 2020

  - ch.fhir.ig.ch-epr-term#2.0.4
  - ch.fhir.ig.ch-core#1.0.0
  - ch.fhir.ig.ch-emed#0.1.0
  - ch.fhir.ig.ch-atc#3.1.0
  - ch.fhir.ig.ch-epr-mhealth#0.1.2

- The server offers a $validate operation on the root with a profile parameter, e.g. you can do:

  ```
  POST {{host}}/$validate?profile=http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-composition-epr HTTP/1.1
  Content-Type: application/fhir+xml
  
  < ./composition_confcode.xml
  ```   

The docker file will install the implementaiton guide in a docker image. The built docker image does not need to download the implementation guides afterwards.

## Prerequisites

In order to use this sample, you should have:

- [This project](https://github.com/hapifhir/hapi-fhir-jpavalidator-starter) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
- Oracle Java (JDK) installed: Minimum JDK8 or newer.
- Apache Maven build tool (newest version)

## Running locally

The easiest way to run this server entirely depends on your environment requirements. At least, the following 4 ways are supported:

### Using spring-boot
```bash
mvn clean install -DskipTests spring-boot:run
```
to debug:

```
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```


Then, browse to the following link to use the server:

[http://localhost:8080/hapi-fhir-jpavalidator/](http://localhost:8080/hapi-fhir-jpavalidator/)


## building with Docker

```bash
docker build -t hapi-fhir-jpavalidator-starter .
docker run -d --name hapi-fhir-jpavalidator-starter -p 8080:8080 hapi-fhir-jpavalidator-starter
```
Server will then be accessible at http://localhost:8888/hapi-fhir-jpavalidator-starter and eg. http://localhost:8888/fhir/metadata. Remember to adjust you overlay configuration in the application.yaml to eg.

### making container available
```
docker tag hapi-fhir-jpavalidator-starter eu.gcr.io/fhir-ch/hapi-fhir-jpavalidator-starter:v020
docker push eu.gcr.io/fhir-ch/hapi-fhir-jpavalidator-starter:v020
```
