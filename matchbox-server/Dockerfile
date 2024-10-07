FROM bellsoft/liberica-openjdk-alpine:latest
EXPOSE 8080

COPY ./target/matchbox.jar /matchbox.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
RUN mkdir -p /data/hapi/lucenefiles && chmod 775 /data/hapi/lucenefiles
RUN apk --purge del

# Create a new user and switch to it
RUN adduser -D matchbox --disabled-password --gecos '' matchbox
ENV HOME=/home/matchbox
RUN mkdir -p /database && chown matchbox:matchbox /database
RUN mkdir -p /config && chown matchbox:matchbox /config
RUN chown matchbox:matchbox /

USER matchbox

ENTRYPOINT java -Xmx3072M -jar /matchbox.jar -Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:application.yaml

# used by .github/workflows/
