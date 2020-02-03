#! /usr/bin/env bash

if [ "$1" == "run_server" ]; then
    # run the server, then we'll test if it's working
    make run
    echo
    echo Containers running, taking a small nap before tests...
    echo
    sleep 2
fi

ee () {
    echo $*
    eval $*
}

test_cmd () {
    testName=$1
    if ee "${@:2}"
    then
        echo Test \"$testName\" passed
    else
        save_status=$?
        echo Test \"$testName\" failed
        exit $save_status
    fi
    echo
}

# test_cmd 'dir foo exists' 'ls -l foo' # should fail from root repo dir
# test_cmd 'dir src exists' 'ls -l src' # should pass from root repo dir

test_cmd 'Sever is running' "curl 'http://localhost:8080/hapi-fhir-jpaserver'"

###############################################################################

testName='Verify hf_psql is a table in a running psql instance in a docker container'

# Quoting this properly to use test_cmd too difficult, so do this manually instead.
# Also, we want to check the result.

# This test automates this manual command:
# You should be able to run this on the command line:
#
# [bash]
# echo 'psql -c '\''\\c hf_psql;'\' | docker exec -i 9ce5591fb88f bash
# [bash end]
#
# and get this result:
# [output]
# You are now connected to database "hf_psql" as user "postgres".
# [output end]


docker_container=`docker container ls --filter "name=psql" --format "{{.ID}}"`
echo "echo 'psql -c '\''\\\c hf_psql;'\' | docker exec -i $docker_container bash"
result=`echo 'psql -c '\''\\c hf_psql;'\' | docker exec -i $docker_container bash`
save_status=$?
# echo \$result is $result

if [ "$result" == 'You are now connected to database "hf_psql" as user "postgres".' ]; then
    echo Test \"$testName\" passed
else
    echo Test \"$testName\" failed
    exit $save_status
fi
echo

###############################################################################
