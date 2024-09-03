#!/bin/sh

# Backend container entrypoint

# Function to update environment variables
update_env_var() {
  if [ -n "$(eval echo \$$1)" ]; then
    value=$(eval echo \$$1)
    echo "Updating $1 to: $value"
    eval "$1=\"$value\""
  fi
}

# Update the environment variables
update_env_var "SPRING_DATASOURCE_URL"
update_env_var "SPRING_DATASOURCE_USERNAME"
update_env_var "SPRING_DATASOURCE_PASSWORD"
update_env_var "SPRING_DATASOURCE_DRIVERCLASSNAME"
update_env_var "SPRING_JPA_DATABASEPLATFORM"
update_env_var "SPRING_JPA_HIBERNATE_DDLAUTO"
update_env_var "SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"
update_env_var "HAPI_FHIR_TESTER_HOME_NAME_SERVER_ADDRESS"


# Start the Java application
exec java --class-path=/app/main.war -Dloader.path=main.war!/WEB-INF/classes/,main.war!/WEB-INF/,/app/extra-classes org.springframework.boot.loader.PropertiesLauncher
