{{- define "nexushr.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "nexushr.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "nexushr.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
