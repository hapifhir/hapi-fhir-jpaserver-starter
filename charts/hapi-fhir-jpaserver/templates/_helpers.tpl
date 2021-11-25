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
<<<<<<< HEAD
=======
Create image tag
*/}}
{{- define "hapi-fhir-jpaserver.imageTag" -}}
{{- $version := default .Chart.AppVersion .Values.image.tag -}}
{{- if .Values.image.flavor }}
{{- printf "%s-%s" $version .Values.image.flavor }}
{{- else }}
{{- printf "%s" $version }}
{{- end }}
{{- end }}

{{/*
>>>>>>> master
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
Create a default fully qualified postgresql name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.fullname" -}}
{{- $name := default "postgresql" .Values.postgresql.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
<<<<<<< HEAD
Get the Postgresql credentials secret.
=======
Get the Postgresql credentials secret name.
>>>>>>> master
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.secretName" -}}
{{- if and (.Values.postgresql.enabled) (not .Values.postgresql.existingSecret) -}}
    {{- printf "%s" (include "hapi-fhir-jpaserver.postgresql.fullname" .) -}}
{{- else if and (.Values.postgresql.enabled) (.Values.postgresql.existingSecret) -}}
    {{- printf "%s" .Values.postgresql.existingSecret -}}
{{- else }}
    {{- if .Values.externalDatabase.existingSecret -}}
        {{- printf "%s" .Values.externalDatabase.existingSecret -}}
    {{- else -}}
<<<<<<< HEAD
        {{ printf "%s-%s" .Release.Name "externaldb" }}
=======
        {{ printf "%s-%s" (include "hapi-fhir-jpaserver.fullname" .) "external-db" }}
>>>>>>> master
    {{- end -}}
{{- end -}}
{{- end -}}

{{/*
<<<<<<< HEAD
=======
Get the Postgresql credentials secret key.
*/}}
{{- define "hapi-fhir-jpaserver.postgresql.secretKey" -}}
{{- if (.Values.externalDatabase.existingSecret) -}}
    {{- printf "%s" .Values.externalDatabase.existingSecretKey -}}
{{- else }}
    {{- printf "postgresql-password" -}}
{{- end -}}
{{- end -}}

{{/*
>>>>>>> master
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.host" -}}
{{- ternary (include "hapi-fhir-jpaserver.postgresql.fullname" .) .Values.externalDatabase.host .Values.postgresql.enabled -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.user" -}}
<<<<<<< HEAD
{{- ternary .Values.postgresql.postgresqlUsername .Values.externalDatabase.user .Values.postgresql.enabled | quote -}}
=======
{{- ternary .Values.postgresql.postgresqlUsername .Values.externalDatabase.user .Values.postgresql.enabled -}}
>>>>>>> master
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.name" -}}
{{- ternary .Values.postgresql.postgresqlDatabase .Values.externalDatabase.database .Values.postgresql.enabled -}}
{{- end -}}

{{/*
Add environment variables to configure database values
*/}}
{{- define "hapi-fhir-jpaserver.database.port" -}}
{{- ternary "5432" .Values.externalDatabase.port .Values.postgresql.enabled -}}
{{- end -}}

{{/*
Create the JDBC URL from the host, port and database name.
*/}}
{{- define "hapi-fhir-jpaserver.database.jdbcUrl" -}}
{{- $host := (include "hapi-fhir-jpaserver.database.host" .) -}}
{{- $port := (include "hapi-fhir-jpaserver.database.port" .) -}}
{{- $name := (include "hapi-fhir-jpaserver.database.name" .) -}}
<<<<<<< HEAD
{{ printf "jdbc:postgresql://%s:%d/%s" $host (int $port) $name }}
=======
{{- $appName := .Release.Name -}}
{{ printf "jdbc:postgresql://%s:%d/%s?ApplicationName=%s" $host (int $port) $name $appName }}
>>>>>>> master
{{- end -}}
