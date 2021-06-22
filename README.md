# HAPI-FHIR JPA Validator Starter Project

This project is a  starter project you can use to deploy a FHIR server using HAPI FHIR JPA validaton with the npm implementation support.

- Optionally Implementation Guides can be added that will be loaded during startup of the server. The "with-preload" subfolder contains an example with the swiss implementation guides relevant for the Swiss EPR Projectathon 2020

  - ch.fhir.ig.ch-epr-term#2.0.4
  - ch.fhir.ig.ch-core#1.2.0
  - ch.fhir.ig.ch-emed#0.2.0
  - ch.fhir.ig.ch-vacd#0.1.0
  - ch.fhir.ig.ch-atc#3.1.0
  - ch.fhir.ig.ch-epr-mhealth#0.1.2

- The server offers a $validate operation on the root with a profile parameter, e.g. you can do:

  ```
  POST {{host}}/$validate?profile=http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-composition-epr HTTP/1.1
  Content-Type: application/fhir+xml
  
  < ./composition_confcode.xml
  ```   
The docker file will create a docker image with no preloaded implementation guides. A list of implementation guides to load can be passed as config-map.
A second docker file will create an image with fixed configuration and preloaded implementation guides.  
That  docker image does not need to download the implementation guides afterwards.

## Prerequisites

In order to use this sample, you should have:

- [This project](https://github.com/hapifhir/hapi-fhir-jpavalidator) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
- Oracle Java (JDK) installed: Minimum JDK8 or newer.
- Apache Maven build tool (newest version)

## Running locally

The easiest way to run this server entirely depends on your environment requirements. At least, the following 4 ways are supported:

### Using spring-boot
With no implementation guide:
```bash
mvn clean install -DskipTests spring-boot:run
```
Load example implementation guides:
```bash
mvn clean install -DskipTests spring-boot:run -Dspring.config.additional-location=file:with-preload/application.yaml
```

to debug:

```
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```


Then, browse to the following link to use the server:

[http://localhost:8080/hapi-fhir-jpavalidator/](http://localhost:8080/hapi-fhir-jpavalidator/)


## building with Docker

### Configurable base image:

```bash
mvn package
docker build -t hapi-fhir-jpavalidator .
docker run -d --name hapi-fhir-jpavalidator -p 8080:8080 hapi-fhir-jpavalidator
```
Server will then be accessible at http://localhost:8888/hapi-fhir-jpavalidator and eg. http://localhost:8888/fhir/metadata. 
To dynamicaly configure run in a kubernetes environment and add a kubernetes config map that provides /config/application.yaml file with
implementation guide list like in "with-preload/application.yaml" 

### Image with preloaded implementation guides

After building the base image:
```bash
cd with-preload
docker build -t hapi-fhir-jpavalidator-ihe .
docker run -d --name hapi-fhir-jpavalidator-ihe -p 8080:8080 hapi-fhir-jpavalidator-ihe
```

### making container available
```
docker tag hapi-fhir-jpavalidator-ihe eu.gcr.io/fhir-ch/hapi-fhir-jpavalidator-ihe:v121
docker push eu.gcr.io/fhir-ch/hapi-fhir-jpavalidator-ihe:v121
```
