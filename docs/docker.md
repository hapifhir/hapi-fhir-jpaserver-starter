# Matchbox container

you can download Matchbox as a docker container:

```
docker pull eu.gcr.io/fhir-ch/matchbox:v220
```

## Configurable base image:

```bash
docker run -d --name matchbox -p 8080:8080 -v /Users/oliveregger/apps/:/apps/ matchbox
```

Server will then be accessible at http://localhost:8080/matchbox/fhir/metadata. 

The local volume  /Users/oliveregger/apps/ will be mapped inside the container and Matchbox will serve the content
if is requested via  http://localhost:8080/matchbox/apps/ (allows you to add own html apsps)


## Using docker-compose with a persistent postgreSQL database

To use docker-compose with Matchbox you need to checkout Matchbox from [github](https://github.com/ahdis/matchbox).

The database will be stored in the "data" directory. The configuration can be found in the "with-postgres" directory or in the "with-preload" directory.

Change to either with-posgres directory or the with-preload directory (contains a list of swiss ig's).

For the first time, you might need to do 

```
mkdir data
docker-compose up matchbox-db
```
that the database gets initialized before Matchbox is starting up (needs a fix)

```
docker-compose up
```

Matchbox will be available at [http://localhost:8080/matchbox/fhir](http://localhost:8080/matchbox/fhir)
Matchbox-gui will be available at [http://localhost:8080/matchbox/#/](http://localhost:8080/matchbox/#/)

Export the DB data:
```
docker-compose exec -T matchbox-test-db pg_dump -Fc -U matchbox matchbox > mydump
```

Reimport the DB data:
```
docker-compose exec -T matchbox-test-db pg_restore -c -U matchbox -d matchbox < mydump
```