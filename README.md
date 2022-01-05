# JuniperCDS Configured HAPI-FHIR Project

This is the JuniperCDS implementation of the HAPI Fhir server.

We have configured the server to use PostgreSQL.

## Start up

To start the server

### Dev

Copy the `application.example.yml` file

```shell
cp src/main/resources/application.example.yaml src/main/resources/application.yaml
```

#### Spining up containers

```shell
docker compose up -f docker-compose.yml -f docker-compose.dev.yml
```

(Optional) To load the default resources

```shell
chmod +x provision/provision.sh
sh provision/provision.sh
```

_Note: the shell script is currently configured to run the operations on http://localhost:8080. If you wish to change the uri please make a copy of the `provision.sh` file, calling it either `provision.dev.sh` or `provision.prod.sh`, and changing the uri there._

_Those files are ignored by git and will be safe to edit. Please do not edit the provision.sh file itself._
