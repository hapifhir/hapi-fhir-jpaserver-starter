# HAPI FHIR JPA Server Starter Helm Chart

![Version: 0.14.0](https://img.shields.io/badge/Version-0.14.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 6.8.3](https://img.shields.io/badge/AppVersion-6.8.3-informational?style=flat-square)

This helm chart will help you install the HAPI FHIR JPA Server in a Kubernetes environment.

## Sample usage

```sh
helm repo add hapifhir https://hapifhir.github.io/hapi-fhir-jpaserver-starter/
helm install hapi-fhir-jpaserver hapifhir/hapi-fhir-jpaserver
```

## Requirements

| Repository | Name | Version |
|------------|------|---------|
| oci://registry-1.docker.io/bitnamicharts | postgresql | 12.5.6 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` | pod affinity |
| deploymentAnnotations | object | `{}` | annotations applied to the server deployment |
| externalDatabase.database | string | `"fhir"` | database name |
| externalDatabase.existingSecret | string | `""` | name of an existing secret resource containing the DB password in the `existingSecretKey` key |
| externalDatabase.existingSecretKey | string | `"postgresql-password"` | name of the key inside the `existingSecret` |
| externalDatabase.host | string | `"localhost"` | external database host used with `postgresql.enabled=false` |
| externalDatabase.password | string | `""` | database password |
| externalDatabase.port | int | `5432` | database port number |
| externalDatabase.user | string | `"fhir"` | username for the external database |
| extraConfig | string | `""` | additional Spring Boot application config. Mounted as a file and automatically loaded by the application. |
| extraEnv | list | `[]` | extra environment variables to set on the server container |
| fullnameOverride | string | `""` | override the chart fullname |
| image.pullPolicy | string | `"IfNotPresent"` | image pullPolicy to use |
| image.registry | string | `"docker.io"` | registry where the HAPI FHIR server image is hosted |
| image.repository | string | `"hapiproject/hapi"` | the path inside the repository |
| image.tag | string | `"v6.8.3@sha256:6195f1116ebabfb0a608addde043b3e524c456c4d4f35b3d25025afd7dcd2e27"` | the image tag. As of v5.7.0, this is the `distroless` flavor by default, add `-tomcat` to use the Tomcat-based image. |
| imagePullSecrets | list | `[]` | image pull secrets to use when pulling the image |
| ingress.annotations | object | `{}` | provide any additional annotations which may be required. Evaluated as a template. |
| ingress.enabled | bool | `false` | whether to create an Ingress to expose the FHIR server HTTP endpoint |
| ingress.hosts[0].host | string | `"fhir-server.127.0.0.1.nip.io"` |  |
| ingress.hosts[0].pathType | string | `"ImplementationSpecific"` |  |
| ingress.hosts[0].paths[0] | string | `"/"` |  |
| ingress.tls | list | `[]` | ingress TLS config |
| metrics.service.port | int | `8081` |  |
| metrics.serviceMonitor.additionalLabels | object | `{}` | additional labels to apply to the ServiceMonitor object, e.g. `release: prometheus` |
| metrics.serviceMonitor.enabled | bool | `false` | if enabled, creates a ServiceMonitor instance for Prometheus Operator-based monitoring |
| nameOverride | string | `""` | override the chart name |
| nodeSelector | object | `{}` | node selector for the pod |
| podAnnotations | object | `{}` | annotations applied to the server pod |
| podDisruptionBudget.enabled | bool | `false` | Enable PodDisruptionBudget for the server pods. uses policy/v1/PodDisruptionBudget thus requiring k8s 1.21+ |
| podDisruptionBudget.maxUnavailable | string | `""` | maximum unavailable instances |
| podDisruptionBudget.minAvailable | int | `1` | minimum available instances |
| podSecurityContext | object | `{}` | pod security context |
| postgresql.auth.database | string | `"fhir"` | name for a custom database to create |
| postgresql.auth.existingSecret | string | `""` | Name of existing secret to use for PostgreSQL credentials `auth.postgresPassword`, `auth.password`, and `auth.replicationPassword` will be ignored and picked up from this secret The secret must contain the keys `postgres-password` (which is the password for "postgres" admin user), `password` (which is the password for the custom user to create when `auth.username` is set), and `replication-password` (which is the password for replication user). The secret might also contains the key `ldap-password` if LDAP is enabled. `ldap.bind_password` will be ignored and picked from this secret in this case. The value is evaluated as a template. |
| postgresql.enabled | bool | `true` | enable an included PostgreSQL DB. see <https://github.com/bitnami/charts/tree/master/bitnami/postgresql> for details if set to `false`, the values under `externalDatabase` are used |
| postgresql.primary.containerSecurityContext.allowPrivilegeEscalation | bool | `false` |  |
| postgresql.primary.containerSecurityContext.capabilities.drop[0] | string | `"ALL"` |  |
| postgresql.primary.containerSecurityContext.runAsNonRoot | bool | `true` |  |
| postgresql.primary.containerSecurityContext.seccompProfile.type | string | `"RuntimeDefault"` |  |
| replicaCount | int | `1` | number of replicas to deploy |
| resources | object | `{}` | configure the FHIR server's resource requests and limits |
| securityContext.allowPrivilegeEscalation | bool | `false` |  |
| securityContext.capabilities.drop[0] | string | `"ALL"` |  |
| securityContext.privileged | bool | `false` |  |
| securityContext.readOnlyRootFilesystem | bool | `true` |  |
| securityContext.runAsGroup | int | `65532` |  |
| securityContext.runAsNonRoot | bool | `true` |  |
| securityContext.runAsUser | int | `65532` |  |
| securityContext.seccompProfile.type | string | `"RuntimeDefault"` |  |
| service.port | int | `8080` | port where the server will be exposed at |
| service.type | string | `"ClusterIP"` | service type |
| serviceAccount.annotations | object | `{}` | Annotations to add to the service account |
| serviceAccount.automount | bool | `true` | Automatically mount a ServiceAccount's API credentials? |
| serviceAccount.create | bool | `false` | Specifies whether a service account should be created. |
| serviceAccount.name | string | `""` | The name of the service account to use. If not set and create is true, a name is generated using the fullname template |
| tests.resources | object | `{}` | configure the test pods resource requests and limits |
| tolerations | list | `[]` | pod tolerations |
| topologySpreadConstraints | list | `[]` | pod topology spread configuration see: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/#api |

## Development

To update the Helm chart when a new version of the `hapiproject/hapi` image is released, [values.yaml](values.yaml) `image.tag` and the [Chart.yaml](Chart.yaml)'s
`version` and optionally the `appVersion` field need to be updated. Afterwards, re-generate the [README.md](README.md)
by running:

```sh
$ helm-docs
INFO[2021-11-20T12:38:04Z] Found Chart directories [charts/hapi-fhir-jpaserver]
INFO[2021-11-20T12:38:04Z] Generating README Documentation for chart /usr/src/app/charts/hapi-fhir-jpaserver
```

## Enable Distributed Tracing based on the OpenTelemtry Java Agent

The container image includes the [OpenTelemetry Java agent JAR](https://github.com/open-telemetry/opentelemetry-java-instrumentation)
which can be used to enable distributed tracing. It can be configured entirely using environment variables,
see <https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/> for details.

Here's an example setup deploying [Jaeger](https://www.jaegertracing.io/) as a tracing backend:

```sh
# required by the Jaeger Operator
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.9.1/cert-manager.yaml
kubectl create namespace observability
kubectl create -f https://github.com/jaegertracing/jaeger-operator/releases/download/v1.37.0/jaeger-operator.yaml -n observability

cat <<EOF | kubectl apply -n observability -f -
# simple, all-in-one Jaeger installation. Not suitable for production use.
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
  name: simplest
EOF
```

Use this chart's `extraEnv` value to set the required environment variables:

```yaml
extraEnv:
  - name: JAVA_TOOL_OPTIONS
    value: "-javaagent:/app/opentelemetry-javaagent.jar"
  - name: OTEL_METRICS_EXPORTER
    value: "none"
  - name: OTEL_LOGS_EXPORTER
    value: "none"
  - name: OTEL_TRACES_EXPORTER
    value: "jaeger"
  - name: OTEL_SERVICE_NAME
    value: "hapi-fhir-jpaserver"
  - name: OTEL_EXPORTER_JAEGER_ENDPOINT
    value: "http://simplest-collector.observability.svc:14250"
```

Finally, you can open the Jaeger query UI by running:

```sh
kubectl port-forward -n observability service/simplest-query 16686:16686
```

and opening <http://localhost:16686/> in your browser.

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.11.3](https://github.com/norwoodj/helm-docs/releases/v1.11.3)
