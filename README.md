# HAPI-FHIR Starter Project

This project is a complete starter project you can use to deploy a FHIR server using HAPI FHIR JPA.

Note that this project is specifically intended for end users of the HAPI FHIR JPA server module (in other words, it helps you implement HAPI FHIR, it is not the source of the library itself). If you are looking for the main HAPI FHIR project, see here: https://github.com/jamesagnew/hapi-fhir

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

If you need to run this server on a different port (using Maven), you can change the port in the run command as follows:
```
mvn -Djetty.port=8888 jetty:run
```
And replacing 8888 with the port of your choice.

# Configuration

Much of this HAPI starter project can be configured using the properties file in *src/main/resources/hapi.properties*. By default, this starter project is configured to use Derby as the database.

## MySql

To configure the starter app to use MySQL, instead of the default Derby, update the hapi.properties file to have the following:

* datasource.driver=com.mysql.jdbc.Driver
* datasource.url=jdbc:mysql://localhost:3306/hapi_dstu3
* hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
* datasource.username=admin
* datasource.password=admin

Because the integration tests within the project rely on the default Derby database configuration, it is important to either explicity skip the integration tests during the build process, i.e., `mvn install -DskipTests`, or delete the tests altogether. Failure to skip or delete the tests once you've configured MySQL for the datasource.driver, datasource.url, and hibernate.dialect as outlined above will result in build errors and compilation failure.

It is important to use MySQL5Dialect when using MySQL version 5+.

# Customizing The Web Testpage UI

The UI that comes with this server is an exact clone of the server available at [http://hapi.fhir.org](http://hapi.fhir.org). You may skin this UI if you'd like. For example, you might change the introductory text or replace the logo with your own.

The UI is customized using [Thymeleaf](https://www.thymeleaf.org/) template files. You might want to learn more about Thymeleaf, but you don't necessarily need to: they are quite easy to figure out.

Several template files that can be customized are found in the following directory: [https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/tree/master/src/main/webapp/WEB-INF/templates)

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

# Deploy with docker compose

Docker compose is a simple option to build and deploy container. To deploy with docker compose, you should build the project
with ```mvn clean install``` and then bring up the containers with ```docker-compose up -d --build```. The server can be 
reached at http://localhost:8080/hapi-fhir-jpaserver/. 

In order to use another port, change the `ports` parameter 
inside ``docker-compose.yml`` to ```8888:8080```, where 8888 is a port of your choice.

The docker compose set also includes my MySQL database, if you choose to use MySQL instead of derby,  change the following 
properties in hapi.properties:

* datasource.driver=com.mysql.jdbc.Driver
* datasource.url=jdbc:mysql://hapi-fhir-mysql:3306/hapi
* hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
* datasource.username=admin
* datasource.password=admin

# Running hapi-fhir-jpaserver-example in Tomcat from IntelliJ

Install Tomcat.

Make sure you have Tomcat set up in IntelliJ.

- File->Settings->Build, Execution, Deployment->Application Servers
- Click +
- Select "Tomcat Server"
- Enter the path to your tomcat deployment for both Tomcat Home (IntelliJ will fill in base directory for you)

Add a Run Configuration for running hapi-fhir-jpaserver-example under Tomcat

- Run->Edit Configurations
- Click the green +
- Select Tomcat Server, Local
- Change the name to whatever you wish
- Uncheck the "After launch" checkbox
- On the "Deployment" tab, click the green +
- Select "Artifact"
- Select "hapi-fhir-jpaserver-example:war" 
- In "Application context" type /hapi

Run the configuration.

- You should now have an "Application Servers" in the list of windows at the bottom.
- Click it.
- Select your server, and click the green triangle (or the bug if you want to debug)
- Wait for the console output to stop

Point your browser (or fiddler, or what have you) to `http://localhost:8080/hapi/baseDstu3/Patient`

It is important to use MySQL5Dialect when using MySQL version 5+.

# Enabling Subscriptions

The server may be configured with subscription support by enabling properties in the [hapi.properties](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/hapi.properties) file: 

* `subscription.resthook.enabled` - Enables REST Hook subscriptions, where the server will make an outgoing connection to a remote REST server

* `subscription.email.enabled` - Enables email subscriptions. Note that you must also provide the connection details for a usable SMTP server.

* `subscription.websocket.enabled` - Enables websocket subscriptions. With this enabled, your server will accept incoming websocket connections on the following URL (this example uses the default context path and port, you may need to tweak depending on your deployment environment): [ws://localhost:8080/hapi-fhir-jpaserver/websocket](ws://localhost:8080/hapi-fhir-jpaserver/websocket)

# Using ElasticSearch

By default, the server will use embedded lucene indexes for terminology and fulltext indexing purposes. You can switch this to using lucene by editing the properties in [hapi.properties](https://github.com/hapifhir/hapi-fhir-jpaserver-starter/blob/master/src/main/resources/hapi.properties)

For example:

```properties
elasticsearch.enabled=true
elasticsearch.rest_url=http://localhost:9200
elasticsearch.username=SomeUsername
elasticsearch.password=SomePassword
elasticsearch.required_index_status=YELLOW
elasticsearch.schema_management_strategy=CREATE
```