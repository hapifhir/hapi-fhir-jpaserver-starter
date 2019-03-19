# HAPI-FHIR Starter Project

This project is a complete starter project you can use to deploy a FHIR server using HAPI FHIR JPA.

# Prerequisites

In order to use this sample, you should have:

* [This project](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) checked out. You may wish to create a GitHub Fork of the project and check that out instead so that you can customize the project and save the results to GitHub.
* Oracle Java (JDK) installed: Minimum JDK8 or newer.
* Apache Maven build tool (newest version)

# Running Locally

The easiest way to run this server is to run it directly in Maven using a built-in Jetty server. To do this, change `src/main/resources/hapi.properties` `server_address` and `server.base` with the values commented out as *For Jetty, use this* and then execute the following command:

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

The UI that comes with this server is an exact clone of the server available at [http://hapi.fhir.org](http://hapi.fhir.org). You may skin this UI if you'd like. For example, you might change the introductory text or replace the logo with your own.

The UI is customized using [Thymeleaf](https://www.thymeleaf.org/) template files. You might want to learn more about Thymeleaf, but you don't necessarily need to: they are quite easy to figure out.

Several template files that can be customized are found in the following directory: [https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates)

# Configuration

Much of this HAPI starter project can be configured using the properties file in *src/main/resources/hapi.properties*. By default, this starter project is configured to use Derby as the database.

## MySql

To configure the starter app to use MySQL, instead of the default Derby, update the hapi.properties file to have the following:

* datasource.driver=com.mysql.jdbc.Driver
* datasource.url=jdbc:mysql://localhost:3306/hapi_dstu3
* hibernate.dialect=org.hibernate.dialect.MySQL5Dialect

It is important to use MySQL5Dialect when using MySQL version 5+.

## Multi-Tenancy

To enable schema-based multi-tenancy, change the following settings:

* Switch to MySQL service, as described above, and create a database `hapi_test` (the default database)
* For each tenant to use, create an additional database `hapi_tenantid`
* Import the following [schema](https://bitbucket.org/hspconsortium/reference-api/src/d5d7eb46d2399213633e9f6de0415774871321f3/reference-api-mysql/src/main/resources/db/mysql/hspc_8_schema_empty.sql?at=develop "hspc_8_schema_empty.sql") to each of the `hapi_*` databases
* In `hapi.properties` enable and configure all elements in the `Multi-Tenancy` section
* In order to perform a query on specific tenant, add the HTTP-Header `X-TenantID` to your request.

Relevant discussions on this topic:

* https://groups.google.com/forum/#!topic/hapi-fhir/F37kfAHYXnc
* https://groups.google.com/forum/#!topic/hapi-fhir/IWyd29s_slU

Todo/Caveat/Remarks:

* Database schemas are not dynamically created, support for this should be implemented
* Problems concerning Lucene are mentioned on the google groups - what is the state on this?
* The TenantFilter should probably be a TenantInterceptor - does this matter?
* Is there a better way to handle the DataSource, than to have a specific DataSourceHolder class?
