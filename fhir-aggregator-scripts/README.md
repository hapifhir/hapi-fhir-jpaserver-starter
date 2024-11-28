# quickstart

This is a quickstart guide to get you up and running with the FHIR Aggregator.

## Installation

* setup a virtual environment
```bash
cd fhir-aggregator-scripts
pip install -r requirements.txt
python -m venv venv
source venv/bin/activate
```

* start the server in the background
```bash
docker compose up --detach
```

* check the server is running
```bash
docker compose ps
NAME                        IMAGE                     COMMAND                  SERVICE                     CREATED         STATUS         PORTS
hapi-fhir-jpaserver-start   hapiproject/hapi:v7.4.0   "java --class-path /…"   hapi-fhir-jpaserver-start   8 minutes ago   Up 8 minutes   0.0.0.0:8080->8080/tcp
hapi-fhir-postgres          postgres:15-alpine        "docker-entrypoint.s…"   hapi-fhir-postgres          8 minutes ago   Up 8 minutes   5432/tcp
```

* query the server

```bash
 curl -s 'http://localhost:8080/fhir/metadata' > /dev/null && echo 'OK: server running'
OK: server running

```

* start a job to load from a public bucket

  * see https://smilecdr.com/docs/bulk/fhir_bulk_import.html#triggering-a-bulk-import
  > storageDetail.type; Must be https. Other input mechanisms will be added in the future. Note that Smile CDR will accept non-HTTPS URLs as a data source in order to simplify testing, however this should not be used for production / PHI / PII scenarios.

```bash
curl -vvvv --header "Content-Type: application/fhir+json" --header "Prefer: respond-async"  -X POST 'http://localhost:8080/fhir/$import' --data @bulk-import-request-TCGA-KIRC.json 
```
*Note:*
> The first time this command is run after restarting the server, it may take a few ( well more than a few ) minutes  to respond. Subsequent runs will be faster.

* check the status of the job

```bash
# where XXXX came from the response of the previous command
curl 'http://localhost:8080/fhir/$import-poll-status?_jobId=XXXX'

```

* check the status of the server

Navigate to project root dir to run docker compose commands.
Use standard docker compose commands, e.g. 

* show running
```bash
docker compose ps
NAME                        IMAGE                     COMMAND                  SERVICE                     CREATED          STATUS          PORTS
hapi-fhir-jpaserver-start   hapiproject/hapi:v7.4.0   "java --class-path /…"   hapi-fhir-jpaserver-start   27 minutes ago   Up 27 minutes   0.0.0.0:8080->8080/tcp
hapi-fhir-postgres          postgres:15-alpine        "docker-entrypoint.s…"   hapi-fhir-postgres          27 minutes ago   Up 27 minutes   5432/tcp
```

* show logs
```bash
# show the last 10 lines of the logs and wait for more ...
docker compose logs --tail 10 -f
```

* show services running
```bash
docker compose stats
```

* get the counts of data loaded
```bash
python fhir-aggregator-scripts/fhir-util.py count-resources 
```

## Prepare new data for upload

* upload to public bucket
```bash
./upload.sh
```

* create a new bulk import request
```bash
python create-bulk-import-request.py
```