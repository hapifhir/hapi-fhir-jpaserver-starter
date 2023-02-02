# Matchbox container

you can download Matchbox as a docker container:

```
docker pull eu.gcr.io/fhir-ch/matchbox:v313
```

## Configurable base image:

```bash
docker run -d --name matchbox -p 8080:8080 -v /Users/oliveregger/apps/:/apps/ matchbox
```

Server will then be accessible at http://localhost:8080/matchboxv3/fhir/metadata.

The local volume /Users/oliveregger/apps/ will be mapped inside the container and Matchbox will serve the content
if is requested via http://localhost:8080/matchboxv3/apps/ (allows you to add own html apsps)

## Live and Readyness checks

To check if the container is live and ready you can check the health:

```http
GET https://test.ahdis.ch/matchboxv3/actuator/health HTTP/1.1
Accept: application/vnd.spring-boot.actuator.v3+json

HTTP/1.1 200
Content-Type: application/vnd.spring-boot.actuator.v3+json
Transfer-Encoding: chunked
Date: Thu, 02 Feb 2023 15:55:12 GMT
Via: 1.1 google
Alt-Svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000
Connection: close

{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

You can also use actuator/health/liveness or actuator/health/readiness.

## Using docker-compose with a persistent postgreSQL database

To use docker-compose with Matchbox you need to checkout Matchbox from [github](https://github.com/ahdis/matchbox).

The database will be stored in the "data" directory. The configuration can be found in the "with-postgres" directory or in the "with-preload" directory.

Change to either with-posgres directory or the with-preload directory

For the first time, you might need to do

```
mkdir data
docker-compose up matchbox-db
```

that the database gets initialized before Matchbox is starting up (needs a fix)

```
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
