FROM openjdk:17-slim

RUN mkdir /app

COPY ./target/ROOT.war /app/

EXPOSE 8080

CMD echo "Starting java process (JAVA_OPTS=$JAVA_OPTS)(PATH=$PATH)..."; java $JAVA_OPTS -jar /app/ROOT.war