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

* configure the endpoint

```bash
# local
export FHIR_BASE=http://localhost:8080/fhir/
# deployed
export FHIR_BASE=https://hapi.fhir-aggregator.org/write-fhir/
# set the project name, used as a prefix for the bucket objects
export PROJECT_NAME=TCGA-CHANGEME
```

* query the server


```bash
 curl -s $FHIR_BASE'/metadata' > /dev/null && echo 'OK: server running'
OK: server running

```

* upload your data to the public bucket. See upload.sh for details.

* create a manifest file to describe the data you want to load. See create-manifest.py for details.

```bash
python create-bulk-import-request.py --help
Usage: create-bulk-import-request.py [OPTIONS] FULL_PATH PROJECT_NAME

  Create manifest for loading FHIR data from bucket.

  Arguments:

  full_path (str): The source of the FHIR ndjson files in the local file
  system. project_name (str): The path in the bucket.

Options:
  --input-source TEXT  The publicly available https:// url base
  --help               Show this message and exit.

```

* start a job to load from a `public` bucket

  * This is for open access data only

```bash
# local
unset AUTH
# deployed
export AUTH='-u USER:PASS'

curl -vvvv $AUTH --header "Content-Type: application/fhir+json" --header "Prefer: respond-async"  -X POST $FHIR_BASE'/$import' --data @bulk-import-request-PROJECT_NAME.json 
```
*Note:*
> The first time this command is run after restarting the server, it may take a few ( well more than a few ) minutes  to respond. Subsequent runs will be faster.
> See https://groups.google.com/g/hapi-fhir/c/V87IZHvlDyM/m/JIOvBvgwAQAJ

* check the status of the job

```bash
# where XXXX came from the response of the previous command
curl $FHIR_BASE'/$import-poll-status?_jobId=XXXX'

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