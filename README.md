# HAPI-FHIR Starter Project

This project is a complete starter project you can use to deploy a FHIR server using HAPI FHIR JPA.

# Prerequisites

In order to use this sample, you should have:

* [This project](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
* Oracle Java (JDK) installed: Minimum JDK8 or newer.
* Apache Maven build tool (newest version)

# Running Locally

The easiest way to run this server is to run it directly in Maven using a built-in Jetty server. To do this, execute the following command:

```
mvn jetty:run
```

Then, browse to the following link to use the server:

[http://localhost:8080/hapi-fhir-jpaserver/](http://localhost:8080/hapi-fhir-jpaserver/)

# Deploying to a Container

Using the Maven-Embedded Jetty method above is convenient, but it is not a good solution if you want to leave the server running in the background.

Most people who are using HAPI FHIR JPA as a server that is accessible to other people (whether internally on your network or publically hosted) will do so using an Application Server, such as [Apache Tomcat](http://tomcat.apache.org/) or [Jetty](https://www.eclipse.org/jetty/). Note that any Servlet 3.0+ compatible Web Container will work (e.g Wildfly, Websphere, etc.).

Tomcat is very popular, so it is a good choice simply because you will be able to find many tutorials online. Jetty is a great alternative due to its fast startup time and good overall performance.

To deploy to a container, you should first build the project:

```
mvn clean install
```

This will create a file called `hapi-fhir-jpaserver.war` in your `target` directory. This should be installed in your Web Container according to the instructions for your particular container. For example, if you are using Tomcat, you will want to copy this file to the `webapps/` directory.

Again, browse to the following link to use the server (note that the port 8080 may not be correct depending on how your server is configured).

[http://localhost:8080/hapi-fhir-jpaserver/](http://localhost:8080/hapi-fhir-jpaserver/)

# Customizing The Web Testpage UI

The UI that comes with 