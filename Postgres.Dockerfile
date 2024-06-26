FROM postgres:16-alpine

ARG POSTGRES_DB
ARG POSTGRES_USER
ARG POSTGRES_PASSWORD

WORKDIR /var/lib/postgresql

COPY dbdata/part_* .
RUN cat part_* > db-data.tar.gz
RUN tar -xzvf db-data.tar.gz -C .
RUN rm part_*
RUN rm db-data.tar.gz

RUN chown -R postgres:postgres data

COPY dbdata/init-user.sh /docker-entrypoint-initdb.d/init-user.sh
RUN chmod +x /docker-entrypoint-initdb.d/init-user.sh
USER postgres
RUN /docker-entrypoint-initdb.d/init-user.sh

CMD ["postgres", "-c", "listen_addresses=*"]
