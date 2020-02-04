#! /usr/bin/env bash

if [ "$1" == "run_server" ]; then
    # run the server, then we'll test if it's working
    make run
    echo
    echo Containers running, taking a small nap before tests...
    echo
    sleep 2
fi

###############################################################################
# Test 1
# This test isn't strictly needed for our eventual production build since we'll
# use Google Cloud SQL, but this does show that in our test setup, the postgres
# container is up and we can connect to it.

testName='Verify connection to hf_psql database in running psql instance in a docker container'

# Quoting this properly to use test_cmd is difficult, so do this manually instead.
# Also, we want to check the result.

docker_container=`docker container ls --filter "name=psql" --format "{{.ID}}"`
echo "docker exec -i $docker_container psql -c '\\c hf_psql;'"
result=`docker exec -i $docker_container psql -c '\c hf_psql;'`
save_status=$?
# echo \$result is $result

if [ "$result" == 'You are now connected to database "hf_psql" as user "postgres".' ]; then
    echo Test \"$testName\" PASSED
else
    echo Test \"$testName\" FAILED
    exit $save_status
fi
echo

###############################################################################
# Test 2

ee () {
    echo $*
    eval $*
}

test_cmd () {
    testName=$1
    if ee "${@:2}"
    then
        echo Test \"$testName\" PASSED
    else
        save_status=$?
        echo Test \"$testName\" FAILED
        exit $save_status
    fi
    echo
}

# test_cmd 'dir foo exists' 'ls -l foo' # should fail from root repo dir
# test_cmd 'dir src exists' 'ls -l src' # should pass from root repo dir

test_cmd 'Sever is running' "curl 'http://localhost:8080/hapi-fhir-jpaserver'"

###############################################################################
# Test 3
# TODO: write something to the DB through hapifhir and verify the right thing
# shows up in the DB (postgres for now, Google Cloud SQL later)
