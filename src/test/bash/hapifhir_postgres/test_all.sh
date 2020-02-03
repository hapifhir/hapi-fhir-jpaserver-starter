#! /usr/bin/env bash

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

testName='Verify hf_psql is a table in a running psql instance'

# The next quoting on this too insane to get this right, so I'll do this manually instead.
# Also, I want to check the result here
#
# Good luck getting this to work:
# docker_container=`docker container ls --filter "name=psql" --format "{{.ID}}"`
# psql_check_cmd="echo 'psql -c '\''\\\c hf_psql;'\' | docker exec -i $docker_container bash"
# test_cmd 'Verify hf_psql is a table in a running psql instance' $psql_check_cmd
#
# seems that bash doesn't have :q like tcsh

# This tests automates this manual test:
# You should be able to run something like this on the command line
# echo 'psql -c '\''\\c hf_psql;'\' | docker exec -i 9ce5591fb88f bash
# and get a result back like this:
# You are now connected to database "hf_psql" as user "postgres".


docker_container=`docker container ls --filter "name=psql" --format "{{.ID}}"`
echo "echo 'psql -c '\''\\\c hf_psql;'\' | docker exec -i $docker_container bash"
result=`echo 'psql -c '\''\\c hf_psql;'\' | docker exec -i $docker_container bash`
save_status=$?
# echo \$result is $result

if [ "$result" == 'You are now connected to database "hf_psql" as user "postgres".' ]; then
    echo Test \"$testName\" passed
else
    save_status=$?
    echo Test \"$testName\" failed
    exit $save_status
fi
echo
