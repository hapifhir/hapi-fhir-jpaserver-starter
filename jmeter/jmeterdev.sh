rm -rf ./report
rm ./memory_dev.jtl
/Applications/apache-jmeter-5.6.2/bin/jmeter.sh -n -q ./user.properties -t ./memory_dev.jmx -l ./memory_dev.jtl -o ./report -e