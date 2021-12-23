# HAPI FHIR JPA Server Starter Helm Chart

![Version: 0.6.0](https://img.shields.io/badge/Version-0.6.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: v5.5.1](https://img.shields.io/badge/AppVersion-v5.5.1-informational?style=flat-square)

This helm chart will help you install the HAPI FHIR JPA Server in a Kubernetes environment.

## Sample usage

```sh
helm repo add hapifhir https://hapifhir.github.io/hapi-fhir-jpaserver-starter/
helm install --render-subchart-notes hapi-fhir-jpaserver hapifhir/hapi-fhir-jpaserver
```

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
| fullnameOverride | string | `""` | override the chart fullname |
| image.flavor | string | `"distroless"` | the flavor or variant of the image to use. appended to the image tag by `-`. |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.registry | string | `"docker.io"` |  |
| image.repository | string | `"hapiproject/hapi"` |  |
| image.tag | string | `""` | defaults to `Chart.appVersion` |
| imagePullSecrets | list | `[]` | image pull secrets to use when pulling the image |
| ingress.annotations | object | `{}` | provide any additional annotations which may be required. Evaluated as a template. |
| ingress.enabled | bool | `false` | whether to create an Ingress to expose the FHIR server HTTP endpoint |
| ingress.hosts[0].host | string | `"fhir-server.127.0.0.1.nip.io"` |  |
| ingress.hosts[0].pathType | string | `"ImplementationSpecific"` |  |
| ingress.hosts[0].paths[0] | string | `"/"` |  |
| ingress.tls | list | `[]` | ingress TLS config |
| nameOverride | string | `""` | override the chart name |
| networkPolicy.allowedFrom | list | `[]` | Additional allowed NetworkPolicyPeer specs Evaluated as a template so you could do: Example: allowedFrom:   - podSelector:       matchLabels:         app.kubernetes.io/name: {{ $.Release.Name }} |
| networkPolicy.enabled | bool | `false` | enable NetworkPolicy |
| networkPolicy.explicitNamespacesSelector | object | `{}` | a Kubernetes LabelSelector to explicitly select namespaces from which ingress traffic could be allowed |
| nodeSelector | object | `{}` | node selector for the pod |
| podAnnotations | object | `{}` | annotations applied to the server pod |
| podSecurityContext | object | `{}` | pod security context |
| postgresql.containerSecurityContext.allowPrivilegeEscalation | bool | `false` |  |
| postgresql.containerSecurityContext.capabilities.drop[0] | string | `"ALL"` |  |
| postgresql.enabled | bool | `true` | enable an included PostgreSQL DB. see <https://github.com/bitnami/charts/tree/master/bitnami/postgresql> for details if set to `false`, the values under `externalDatabase` are used |
| postgresql.existingSecret | string | `""` | Name of existing secret to use for PostgreSQL passwords. The secret has to contain the keys `postgresql-password` which is the password for `postgresqlUsername` when it is different of `postgres`, `postgresql-postgres-password` which will override `postgresqlPassword`, `postgresql-replication-password` which will override `replication.password` and `postgresql-ldap-password` which will be sed to authenticate on LDAP. The value is evaluated as a template. |
| postgresql.postgresqlDatabase | string | `"fhir"` | name of the database to create see: <https://github.com/bitnami/bitnami-docker-postgresql/blob/master/README.md#creating-a-database-on-first-run> |
| readinessProbe.failureThreshold | int | `5` |  |
| readinessProbe.initialDelaySeconds | int | `30` |  |
| readinessProbe.periodSeconds | int | `20` |  |
| readinessProbe.successThreshold | int | `1` |  |
| readinessProbe.timeoutSeconds | int | `20` |  |
| replicaCount | int | `1` | number of replicas to deploy |
| resources | object | `{}` | configure the FHIR server's resource requests and limits |
| securityContext.allowPrivilegeEscalation | bool | `false` |  |
| securityContext.capabilities.drop[0] | string | `"ALL"` |  |
| securityContext.readOnlyRootFilesystem | bool | `true` |  |
| securityContext.runAsNonRoot | bool | `true` |  |
| securityContext.runAsUser | int | `65532` |  |
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |
| startupProbe.failureThreshold | int | `10` |  |
| startupProbe.initialDelaySeconds | int | `60` |  |
| startupProbe.periodSeconds | int | `30` |  |
| startupProbe.successThreshold | int | `1` |  |
| startupProbe.timeoutSeconds | int | `30` |  |
| tolerations | list | `[]` | pod tolerations |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.5.0](https://github.com/norwoodj/helm-docs/releases/v1.5.0)
