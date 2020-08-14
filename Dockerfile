FROM ubuntu as build-hapi

ENV PATH="/tmp/apache-maven-3.6.0/bin:${PATH}"

ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /tmp
RUN apt-get update && \
    apt-get install git -y && \
    apt-get install sed -y && \
    apt-get install wget -y && \
    apt-get install openjdk-11-jdk -y

RUN wget https://downloads.apache.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz && tar xzf apache-maven-3.6.3-bin.tar.gz
RUN export PATH=/tmp/apache-maven-3.6.3/bin:${PATH}

WORKDIR /tmp/hapi-fhir-jpaserver-starter

COPY . .

RUN /tmp/apache-maven-3.6.3/bin/mvn clean install -DskipTests

FROM tomcat:9-jre11

RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles
COPY --from=build-hapi /tmp/hapi-fhir-jpaserver-starter/target/*.war /usr/local/tomcat/webapps/

EXPOSE 8080

CMD ["catalina.sh", "run"]