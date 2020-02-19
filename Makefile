.SILENT:
.PHONY: help _install_deps _mvn_install run ps test

# Set shell to Bash
SHELL := /usr/bin/env bash

DOCKER_COMPOSE_INSTALLED := $(shell command -v docker-compose 2> /dev/null)
PYTEST_INSTALLED := $(shell pip show pytest 2> /dev/null)

_install_deps:
ifndef DOCKER_COMPOSE_INSTALLED
	pip install docker-compose
endif
ifndef PYTEST_INSTALLED
	pip install pytest
endif

help:
	echo Available recipes:
	cat $(MAKEFILE_LIST) | grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' | awk 'BEGIN { FS = ":.*?## " } { cnt++; a[cnt] = $$1; b[cnt] = $$2; if (length($$1) > max) max = length($$1) ; } END { for (i = 1; i <= cnt; i++) printf "  $(shell tput bold)%-*s$(shell tput sgr0) %s\n", max, a[i], b[i] }'

_mvn_install:
	mvn clean install -DskipTests

run: _install_deps _mvn_install ## Build the docker containers and run them. Server should appear at http://localhost:8080/hapi-fhir-jpaserver/
	docker-compose up -d --build

ps: _install_deps ## List hapi-fhir-jpaserver-start and psql containers if running
	docker-compose ps

test: _install_deps ## execute tests which rely on server locally running on http://localhost:8080/hapi-fhir-jpaserver/
	python -m pytest --durations=0 tests
