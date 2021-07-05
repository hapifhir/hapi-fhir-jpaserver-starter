# Matchbox Validator 

This project is a starter project you can use to deploy a FHIR server using HAPI FHIR JPA validation for supporting FHIR implementation guides.

- The server offers a $validate operation on the root with a profile parameter, e.g. you can do:

  ```
  POST {{host}}/$validate?profile=http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-composition-epr HTTP/1.1
  Content-Type: application/fhir+xml
  
  < ./composition_confcode.xml
  ```   

- Optionally Implementation Guides can be added that will be loaded during startup of the server. The "with-preload" subfolder contains an example with the swiss implementation guides relevant for the Swiss EPR Projectathon 2021

The docker file will create a docker image with no preloaded implementation guides. A list of implementation guides to load can be passed as config-map.

A second docker file will create an image with fixed configuration and preloaded implementation guides.  
That docker image does not need to download the implementation guides afterwards.

## Prerequisites

1. to develop with matchbox-validator you need to check out the **dev** branches of the forked [org.hl7.fhir.core](https://github.com/ahdis/org.hl7.fhir.core/tree/dev) and [hapi-fhir](https://github.com/ahdis/hapi-fhir/tree/dev) project
2. run mvn clean install -DskipTests in org.hl7.fhir.core and hapi-fhir (this will install local maven snapshots in your system)

- [This project](https://github.com/ahdis/matchbox-validator) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
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
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.arguments=--spring.config.additional-location=file:with-preload/application.yaml
```
or
```
java -Dspring.config.additional-location=file:with-preload/application.yaml -jar target/matchbox-validator.jar
```

```
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```


Then, browse to the following link to use the server:

[http://localhost:8080/matchbox-validator/](http://localhost:8080/matchbox-validator/)


## building with Docker

### Configurable base image:

```bash
mvn package -DskipTests
docker build -t matchbox-validator .
docker run -d --name matchbox-validator -p 8080:8080 matchbox-validator
```
Server will then be accessible at http://localhost:8080/matchbox-validator/fhir/metadata. 

To dynamically configure run in a kubernetes environment and add a kubernetes config map that provides /config/application.yaml file with implementation guide list like in "with-preload/application.yaml" 

### Image with preloaded implementation guides

After building the base image:
```bash
cd with-preload
docker build -t matchbox-validator-swissepr .
docker run -d --name matchbox-validator-swissepr -p 8080:8080 matchbox-validator-swissepr
```

### making container available
```
docker tag matchbox-validator eu.gcr.io/fhir-ch/matchbox-validator:v130
docker tag matchbox-validator-swissepr eu.gcr.io/fhir-ch/matchbox-validator-swissepr:v130

docker push eu.gcr.io/fhir-ch/matchbox-validator:v130
docker push eu.gcr.io/fhir-ch/matchbox-validator-swissepr:v130
```
