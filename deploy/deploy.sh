#!/usr/bin/env sh

set -eux

# Create the namespace
kubectl create namespace $NAMESPACE || true

# Upgrade charts
helm upgrade \
    --install \
    --atomic \
    --debug \
    --create-namespace \
    --namespace "${NAMESPACE}" \
    --set image.repository="${ECR_REGISTRY}/${ECR_REPOSITORY}" \
    --set image.tag="${GITHUB_SHA}" \
    --set externalDatabase.host="${DB_HOST}" \
    --set externalDatabase.user="${DB_USER}" \
    --set externalDatabase.password="${DB_PASSWORD}" \
    --set externalDatabase.database="${DB_NAME}" \
    --set gateway.host="${DOMAIN_NAME}" \
    --set auth.username="${HAPI_API_USERNAME}" \
    --set auth.password="${HAPI_API_PASSWORD}" \
    --set ingress.enabled=false \
    --version="0.17.3" \
    --wait \
    --timeout 3600s \
    -f ../charts/hapi-fhir-jpaserver/values.yaml \
    -f ../charts/hapi-fhir-jpaserver/instances/${ENVIRONMENT}.yaml \
    hapi-fhir-${ENVIRONMENT} \
    ../charts/hapi-fhir-jpaserver