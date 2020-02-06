#! /usr/bin/env bash

TIMEOUT_SECONDS=2

title () {
    echo
    echo ===== $* =====
    echo
}

loop_until_bool_or_timeout () {
    boolean_command=$1

    START_SECONDS=$SECONDS
    until [ "`$boolean_command`" = "true" ]; do
        sleep 0.1;
        if [[ $(($SECONDS - $START_SECONDS)) -ge $TIMEOUT_SECONDS ]]; then
            echo Too much time taken waiting for: $boolean_command
            exit 1
        fi
    done;
}

if [ "$1" == "run_server" ]; then
    # run the server, then we'll test if it's working

    title Starting containers, will wait $TIMEOUT_SECONDS seconds for each of them to launch

    make run

    loop_until_bool_or_timeout 'docker inspect -f {{.State.Running}} hapi-fhir-jpaserver-start'
    loop_until_bool_or_timeout 'docker inspect -f {{.State.Running}} psql_container'

    echo Containers have started.
fi

title Running Tests

###############################################################################
# Test 1
# This test isn't strictly needed for our eventual production build since we'll
# use Google Cloud SQL, but this does show that in our test setup, the postgres
# container is up and we can connect to it. We run it first so we can eliminate
# it as a failure case for hapi-fhir-jpaserver.

testName='Verify connection to hf_psql database in running psql instance in a docker container'

retry_until() {
    local retries="$1"
    local success_result="$2"
    local command="$3"
    local failures=0
    local result=`eval $command` # eval consumes quoting properly
    while [ "$result" != "$success_result" ]; do
        failures=$(( $failures + 1 ))
        (( $failures <= $retries )) || exit 1
        echo "$command" >&2
        echo " * $failures failure(s); will retry ... command returned: $result" >&2
        sleep 1
        result=`eval $command`
    done
}

# Oddly this fails when using exactly as is but with docker-compose
command="docker exec psql_container psql -c '\\c hf_psql;'"
echo $command
success_result='You are now connected to database "hf_psql" as user "postgres".'
retry_until 10 "$success_result" "$command"
echo Test \"$testName\" PASSED
echo

###############################################################################
# Test 2

testName='Sever is running'

retry() {
    local retries=$1
    shift
    local failures=0
    while ! "$@"; do
        failures=$(( $failures + 1 ))
        (( $failures <= $retries )) || exit 1
        echo "$@" >&2
        echo " * $failures failure(s), retrying..." >&2
        sleep 1
    done
}

echo curl http://localhost:8080/hapi-fhir-jpaserver
retry 10 curl http://localhost:8080/hapi-fhir-jpaserver
echo Test \"$testName\" PASSED
echo

###############################################################################
# Test 3
# TODO: write something to the DB through hapifhir and verify the right thing
# shows up in the DB (postgres for now, Google Cloud SQL later)
