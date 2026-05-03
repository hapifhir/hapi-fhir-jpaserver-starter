{{/*
Expand the name of the chart.
*/}}
{{- define "hapi-fhir-jpaserver.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "hapi-fhir-jpaserver.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "hapi-fhir-jpaserver.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "hapi-fhir-jpaserver.labels" -}}
helm.sh/chart: {{ include "hapi-fhir-jpaserver.chart" . }}
{{ include "hapi-fhir-jpaserver.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "hapi-fhir-jpaserver.selectorLabels" -}}
app.kubernetes.io/name: {{ include "hapi-fhir-jpaserver.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "hapi-fhir-jpaserver.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "hapi-fhir-jpaserver.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create a default fully qualified postgresql name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.fullname" -}}
{{- $name := default "postgres" .Values.postgres.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Get the Postgresql credentials secret name.
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.secretName" -}}
{{- if .Values.postgres.enabled -}}
    {{- if .Values.postgres.auth.existingSecret -}}
        {{- printf "%s" .Values.postgres.auth.existingSecret -}}
    {{- else -}}
        {{- printf "%s" (include "hapi-fhir-jpaserver.postgresql.fullname" .) -}}
    {{- end -}}
{{- else }}
    {{- if .Values.externalDatabase.existingSecret -}}
        {{- printf "%s" .Values.externalDatabase.existingSecret -}}
    {{- else -}}
        {{ printf "%s-%s" (include "hapi-fhir-jpaserver.fullname" .) "external-db" }}
    {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Get the Postgresql credentials secret key.
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.secretKey" -}}
{{- if .Values.postgres.enabled -}}
    {{- if .Values.postgres.auth.username -}}
        {{- printf "%s" .Values.postgres.auth.secretKeys.userPasswordKey -}}
    {{- else -}}
        {{- printf "%s" .Values.postgres.auth.secretKeys.adminPasswordKey -}}
    {{- end -}}
{{- else }}
    {{- if .Values.externalDatabase.existingSecret -}}
        {{- printf "%s" .Values.externalDatabase.existingSecretKey -}}
    {{- else -}}
        {{- printf "postgres-password" -}}
    {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.host" -}}
{{- ternary (include "hapi-fhir-jpaserver.postgresql.fullname" .) .Values.externalDatabase.host .Values.postgres.enabled -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.user" -}}
{{- if .Values.postgres.enabled -}}
    {{- printf "%s" .Values.postgres.auth.username | default "postgres" -}}
{{- else -}}
    {{- printf "%s" .Values.externalDatabase.user -}}
{{- end -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.name" -}}
{{- ternary .Values.postgres.auth.database .Values.externalDatabase.database .Values.postgres.enabled -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.port" -}}
{{- ternary "5432" .Values.externalDatabase.port .Values.postgres.enabled -}}
{{- end -}}

{{/*
Create the JDBC URL from the host, port and database name.
*/}}
{{- define "hapi-fhir-jpaserver.database.jdbcUrl" -}}
{{- $host := (include "hapi-fhir-jpaserver.database.host" .) -}}
{{- $port := (include "hapi-fhir-jpaserver.database.port" .) -}}
{{- $name := (include "hapi-fhir-jpaserver.database.name" .) -}}
{{- $appName := .Release.Name -}}
{{ printf "jdbc:postgresql://%s:%d/%s?ApplicationName=%s" $host (int $port) $name $appName }}
{{- end -}}

{{/*
Get the container image for the wait-for-db init container t
*/}}
{{- define "hapi-fhir-jpaserver.waitForDatabaseInitContainerImage" -}}
{{- $registry := .Values.waitForDatabaseInitContainer.image.registry -}}
{{- $repository := .Values.waitForDatabaseInitContainer.image.repository -}}
{{- $tag := .Values.waitForDatabaseInitContainer.image.tag -}}
{{ printf "%s/%s:%s" $registry $repository $tag}}
{{- end -}}

{{/*
Return a resource request/limit object based on a given preset.
These presets are for basic testing and not meant to be used in production
{{ include "common.resources.preset" (dict "type" "nano") -}}

Based on: <https://github.com/bitnami/charts/blob/main/bitnami/common/templates/_resources.tpl>
*/}}
{{- define "common.resources.preset" -}}
{{/* The limits are the requests increased by 50% (except ephemeral-storage and xlarge/2xlarge sizes)*/}}
{{- $presets := dict
  "nano" (dict
      "requests" (dict "cpu" "100m" "memory" "128Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "150m" "memory" "192Mi" "ephemeral-storage" "2Gi")
   )
  "micro" (dict
      "requests" (dict "cpu" "250m" "memory" "256Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "375m" "memory" "384Mi" "ephemeral-storage" "2Gi")
   )
  "small" (dict
      "requests" (dict "cpu" "500m" "memory" "512Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "750m" "memory" "768Mi" "ephemeral-storage" "2Gi")
   )
  "medium" (dict
      "requests" (dict "cpu" "500m" "memory" "1024Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "750m" "memory" "1536Mi" "ephemeral-storage" "2Gi")
   )
  "large" (dict
      "requests" (dict "cpu" "1.0" "memory" "2048Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "1.5" "memory" "3072Mi" "ephemeral-storage" "2Gi")
   )
  "xlarge" (dict
      "requests" (dict "cpu" "1.0" "memory" "3072Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "3.0" "memory" "6144Mi" "ephemeral-storage" "2Gi")
   )
  "2xlarge" (dict
      "requests" (dict "cpu" "1.0" "memory" "3072Mi" "ephemeral-storage" "50Mi")
      "limits" (dict "cpu" "6.0" "memory" "12288Mi" "ephemeral-storage" "2Gi")
   )
 }}
{{- if hasKey $presets .type -}}
{{- index $presets .type | toYaml -}}
{{- else -}}
{{- printf "ERROR: Preset key '%s' invalid. Allowed values are %s" .type (join "," (keys $presets)) | fail -}}
{{- end -}}
{{- end -}}
