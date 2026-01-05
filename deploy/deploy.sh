#!/usr/bin/env sh

set -eux

# Create the namespace
kubectl create namespace $NAMESPACE || true

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
    --set ingress.hosts[0].host="${DOMAIN_NAME}" \
    --set ingress.tls[0].hosts[0]="${DOMAIN_NAME}" \
    --set ingress.tls[0].secretName="letsencrypt-prod" \
    --version="0.17.3" \
    --wait \
    --timeout 300s \
    -f ../charts/hapi-fhir-jpaserver/values.yaml \
    -f ../charts/hapi-fhir-jpaserver/instances/${ENVIRONMENT}.yaml \
    hapi-fhir-${ENVIRONMENT} \
    ../charts/hapi-fhir-jpaserver