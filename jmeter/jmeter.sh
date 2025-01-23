rm -rf ./report
rm ./memory.jtl
/Applications/apache-jmeter-5.6.2/bin/jmeter.sh -n -q ./user.properties -t ./memory.jmx -l ./memory.jtl -o ./report -e