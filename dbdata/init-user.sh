pg_ctl -D /var/lib/postgresql/data -o "-c listen_addresses=''" -w start

psql -v ON_ERROR_STOP=1 --username "hapi" --dbname "${POSTGRES_DB}" <<-EOSQL
  CREATE USER ${POSTGRES_USER} WITH PASSWORD '${POSTGRES_PASSWORD}';
  GRANT ALL PRIVILEGES ON DATABASE "${POSTGRES_DB}" TO ${POSTGRES_USER};
  \c ${POSTGRES_DB}
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${POSTGRES_USER};
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${POSTGRES_USER};
  CREATEROLE
EOSQL

pg_ctl -D /var/lib/postgresql/data -m fast -w stop


