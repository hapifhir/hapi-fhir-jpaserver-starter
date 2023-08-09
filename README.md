# Matchbox

[Matchbox](https://matchbox.health) is a FHIR server based on the [hapifhir/hapi-fhir-jpaserver-starter](https://github.com/hapifhir/hapi-fhir-jpaserver-starter)

- (pre-)load FHIR implementation guides from the package server for conformance resources (StructureMap, Questionnaire, CodeSystem, ValueSet, ConceptMap, NamingSystem, StructureDefinition). The "with-preload" subfolder contains an example with the implementation guides provided for the [public test server](https://test.ahdis.ch/matchbox/fhir).
- validation support: [server]/$validate for checking FHIR resources conforming to the loaded implementation guides
- FHIR Mapping Language endpoints for creation of StructureMaps and support for the [StructureMap/$transform](https://www.hl7.org/fhir/operation-structuremap-transform.html) operation
- SDC (Structured Data Capture) [extraction](https://build.fhir.org/ig/HL7/sdc/extraction.html#map-extract) support based on the FHIR Mapping language and [Questionnaire/$extract](http://build.fhir.org/ig/HL7/sdc/OperationDefinition-QuestionnaireResponse-extract.html)

a public test server is hosted at [https://test.ahdis.ch/matchboxv3/fhir](https://test.ahdis.ch/matchboxv3/fhir) with a corresponding gui [https://test.ahdis.ch/matchboxv3/](https://test.ahdis.ch/matchboxv3/#)

see also additional [documentation](https://ahdis.github.io/matchbox)

## containers

The docker file will create a docker image with no preloaded implementation guides. A list of implementation guides to load can be passed as config-map.

## Prerequisites

- [This project](https://github.com/ahdis/matchbox) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub. Check out the main branch (master is kept in sync with [hapi-fhir-jpaserver-starter](https://github.com/hapifhir/hapi-fhir-jpaserver-starter)
- Oracle Java (JDK) installed: Minimum JDK11 or newer.
- Apache Maven build tool (newest version)

## Running locally

The easiest way to run this server entirely depends on your environment requirements. At least, the following 4 ways are supported:

### Using spring-boot

With no implementation guide:

```bash
mvn clean install -DskipTests spring-boot:run
```

Load example implementation guides (needs postgres):

```bash
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.arguments=--spring.config.additional-location=file:with-preload/application.yaml
```

or

```
java -Dspring.config.additional-location=file:with-preload/application.yaml -jar target/matchbox.jar
```

```
mvn clean install -DskipTests spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

Then, browse to the following link to use the server:

[http://localhost:8080/matchbox/fhir](http://localhost:8080/matchboxv3/fhir)
or
[http://localhost:8080/matchbox/#/](http://localhost:8080/matchboxv3/#/)

## Using docker-compose with a persistent postgreSQL database

The database will be stored in the "data" directory. The configuration can be found in the "with-postgres" directory or in the "with-preload" directory.

Change to either with-posgres directory or the with-preload directory (contains a list of swiss ig's).

For the first time, you might need to do

```
docker-compose up matchbox-db
```

that the database gets initialized before matchbox is starting up (needs a fix)

```
mkdir data
mvn clean package -DskipTests
docker build -t matchbox .
docker-compose up
```

Matchbox will be available at [http://localhost:8080/matchboxv3/fhir](http://localhost:8080/matchboxv3/fhir)
Matchbox-gui will be available at [http://localhost:8080/matchboxv3/#/](http://localhost:8080/matchboxv3/#/)

Export the DB data:

```
docker-compose exec -T matchbox-test-db pg_dump -Fc -U matchbox matchbox > mydump
```

Reimport the DB data:

```
docker-compose exec -T matchbox-test-db pg_restore -c -U matchbox -d matchbox < mydump
```

## building with Docker

### Configurable base image:

```bash
cd matchbox-server
mvn package -DskipTests
docker build -t matchbox .
docker run -d --name matchbox -p 8080:8080 -v /Users/oliveregger/apps/:/apps/ matchbox
```

Server will then be accessible at http://localhost:8080/matchboxv3/fhir/metadata.

To dynamically configure run in a kubernetes environment and add a kubernetes config map that provides /config/application.yaml file with implementation guide list like in "with-preload/application.yaml"

### making container available

```
docker tag matchbox eu.gcr.io/fhir-ch/matchbox:v313

docker push eu.gcr.io/fhir-ch/matchbox:v313
```

### publish docs

documentation is maintained in docs folder using [mkdocs-material](https://squidfunk.github.io/mkdocs-material/):

- develop docs: mkdocs serve
- publish docs: mkdocs gh-deploy --force

docs are then available at https://ahdis.github.io/matchbox/

# Kubernetes

kubectl cp matchbox-test-0:fhir.logdir_IS_UNDEFINED ./fhir.logdir/

kubectl cp matchbox-test-app-d684cf865 ./fhir.logdir/

# MVN run unit tests

mvn -Dtest=CapabilityStatementTests test

# Making a release

1. Create a pull request that updates the version in the different files (pom.xml files, package.json, the docker pull
   command in documentation, the changelog, etc.).
2. Merge the pull request if all tests have succeeded.
3. Wait for the [Angular workflow](https://github.com/ahdis/matchbox/blob/main/.github/workflows/angular_build.yml)
   to complete. Since the package.json was modified, the Angular project is rebuilt.
4. Create a [release](https://github.com/ahdis/matchbox/releases) with the changelog (e.g. "matchbox v3.3.2") and a
   [tag](https://github.com/ahdis/matchbox/tags) (e.g. `v.3.3.2`) in GitHub.
5. It will trigger two workflows:
   1. The [Docker workflow](https://github.com/ahdis/matchbox/blob/main/.github/workflows/googleregistry.yml), that
      builds a Docker container around `matchbox-server` and publishes it to the Google Artifact registry.
   2. The [Maven workflow](https://github.com/ahdis/matchbox/blob/main/.github/workflows/central_repository.yml), that
      builds the `matchbox-engine` JAR and publishes it to the Maven Central Repository. The version used is the one
      specified in the POM.
