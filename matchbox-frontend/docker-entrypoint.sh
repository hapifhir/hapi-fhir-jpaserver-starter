#!/usr/bin/env sh
set -eu

envsubst '${MAG} ${MATCHBOX} ${DOMAIN}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

exec "$@"
