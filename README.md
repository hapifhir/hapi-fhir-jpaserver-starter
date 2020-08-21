# HAPI-FHIR JPA Validator Starter Project

This project is a  starter project you can use to deploy a FHIR server using HAPI FHIR JPA validaton with the npm implementation support.

- Implementation Guides can be to hapi.properties and will be loaded during startup of the server, currently configured are the swiss implementation guides relevant for the Swiss EPR Projectathon 2020

  - ch.fhir.ig.ch-epr-term#2.0.4
  - ch.fhir.ig.ch-core#1.0.0
  - ch.fhir.ig.ch-emed#0.1.0
  - ch.fhir.ig.ch-atc#3.1.0
  - ch.fhir.ig.ch-epr-mhealth#0.1.0

- The server offers a $validate operation on the root with a profile parameter, eg you can do:

  ```
  POST {{host}}/$validate?profile=http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-composition-epr HTTP/1.1
  Content-Type: application/fhir+xml
  
  < ./composition_confcode.xml
  ```   

## Prerequisites

In order to use this sample, you should have:

- [This project](https://github.com/hapifhir/hapi-fhir-jpavalidator-starter) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
- Oracle Java (JDK) installed: Minimum JDK8 or newer.
- Apache Maven build tool (newest version)

## Running locally

The easiest way to run this server is to run it directly in Maven using a built-in Jetty server. To do this, change `src/main/resources/hapi.properties` `server_address` and `server.base` with the values commented out as _For Jetty, use this_ and then execute the following command:

```bash
mvn jetty:run
```

Then, browse to the following link to use the server:

[http://localhost:8080/hapi-fhir-jpavalidator/](http://localhost:8080/hapi-fhir-jpavalidator/)

If you need to run this server on a different port (using Maven), you can change the port in the run command as follows:

```bash
mvn -Djetty.port=8888 jetty:run
```

And replacing 8888 with the port of your choice.

## Configurations

Much of this HAPI starter project can be configured using the properties file in _src/main/resources/hapi.properties_. By default, this starter project is configured to use Derby as the database.

## Deploying to a Container

Using the Maven-Embedded Jetty method above is convenient, but it is not a good solution if you want to leave the server running in the background.

Most people who are using HAPI FHIR JPA as a server that is accessible to other people (whether internally on your network or publically hosted) will do so using an Application Server, such as [Apache Tomcat](http://tomcat.apache.org/) or [Jetty](https://www.eclipse.org/jetty/). Note that any Servlet 3.0+ compatible Web Container will work (e.g Wildfly, Websphere, etc.).

Tomcat is very popular, so it is a good choice simply because you will be able to find many tutorials online. Jetty is a great alternative due to its fast startup time and good overall performance.

To deploy to a container, you should first build the project:

```bash
mvn clean install
```

This will create a file called `hapi-fhir-jpavalidator.war` in your `target` directory. This should be installed in your Web Container according to the instructions for your particular container. For example, if you are using Tomcat, you will want to copy this file to the `webapps/` directory.

Again, browse to the following link to use the server (note that the port 8080 may not be correct depending on how your server is configured).

[http://localhost:8080/hapi-fhir-jpaserver/](http://localhost:8080/hapi-fhir-jpaserver/)
