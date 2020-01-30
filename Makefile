.SILENT:
.PHONY: help _mvn_install build ps

# Set shell to Bash
SHELL := /usr/bin/env bash

help:
	echo Available recipes:
	cat $(MAKEFILE_LIST) | grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' | awk 'BEGIN { FS = ":.*?## " } { cnt++; a[cnt] = $$1; b[cnt] = $$2; if (length($$1) > max) max = length($$1) ; } END { for (i = 1; i <= cnt; i++) printf "  $(shell tput bold)%-*s$(shell tput sgr0) %s\n", max, a[i], b[i] }'

_mvn_install:
	mvn clean install -DskipTests

build: _mvn_install ## Build the docker containers and run them. Server should appear at http://localhost:8080/hapi-fhir-jpaserver/
	docker-compose up -d --build

ps: ## List hapi-fhir-jpaserver-start and psql containers
	docker-compose ps --filter "name=psql|hapi-fhir"
