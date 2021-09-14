FROM matchbox:latest
COPY ./application.yaml /application.yaml
RUN java -Xmx1G -Xms1G -jar /app.jar --hapi.fhir.only_install_packages=true
ENTRYPOINT java -Xmx1G -Xshareclasses -Xquickstart -jar /app.jar -Dspring.config.additional-location=optional:file:/config/application.yaml,optional:file:application.yaml