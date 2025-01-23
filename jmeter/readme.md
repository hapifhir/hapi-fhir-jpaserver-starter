### Loadtest 


Install Apache JMeter on your system: https://jmeter.apache.org/

Run matchbox on port 8080, eg:

docker run -d --name matchbox-ch-elm -p 8080:80  europe-west6-docker.pkg.dev/ahdis-ch/ahdis/matchbox-ch-elm:1.7.1

Run in terminal the following commands (osx):

```
/Applications/apache-jmeter-5.6.2/bin/jmeter.sh -q ./user.properties -t ./memory.jmx -l ./memory.jtl -o ./report -e
```

The script performs a validation and checks the memory usage. two custom graphs will be plotted at the end in the report with java memory heap size and validation time.