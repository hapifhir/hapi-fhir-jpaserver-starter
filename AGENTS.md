# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java`: Spring Boot entry point `ca.uhn.fhir.jpa.starter.Application`, resource providers, config.
- `src/main/resources`: Application YAML, search parameter bundles, capability statements packaged with the WAR.
- `src/main/webapp`: HAPI Testpage overlay shipped for the default UI.
- `src/test/java` & `src/test/resources`: JUnit 5 suites (interceptors, MCP, MDM) plus matching fixtures grouped by FHIR version.
- `charts/`, `docker-compose.yml`, `configs/`: Deployment templates for Helm, Docker, and Tomcat/server overrides.
- `Dockerfile`, `build-docker-image.sh`: Reference container build scripts used by CI/CD.

## Build, Test, and Development Commands
- `mvn clean install`: Compile, run Surefire + Failsafe, and emit `target/ROOT.war`.
- `mvn spring-boot:run -Pboot`: Start the server on port 8080 with hot reload-friendly Boot profile.
- `mvn clean package spring-boot:repackage -Pboot && java -jar target/ROOT.war`: Build and exercise the bootable WAR.
- `docker-compose up -d --build`: Launch JPAServer + PostgreSQL using the local Dockerfile.
- `docker run -p 8080:8080 hapiproject/hapi:latest`: Compare against the upstream binary distribution.

## Coding Style & Naming Conventions
- Target Java 17, four-space indents, alphabetized imports, no wildcards.
- Keep code under `ca.uhn.fhir.jpa.starter` and mirror packages in tests.
- Prefer descriptive class suffixes (`*Provider`, `*Service`, `*Config`) and constructor injection with `final` collaborators.
- YAML keys stay kebab-case; JSON fixtures use lower_snake_case filenames.

## Testing Guidelines
- `mvn test`: Runs JUnit Jupiter unit suites such as `CustomBeanTest` and `ParallelUpdatesVersionConflictTest`.
- `mvn verify`: Adds integration coverage through Failsafe with the default H2 datasource; if you pivot to PostgreSQL, run `mvn install -DskipTests` until fixtures are updated.
- Store integration suites as `*IT.java` so Failsafe detects them and colocate datasets in `src/test/resources`.
- Leverage Testcontainers and HAPI FHIR test utilities already declared in `pom.xml`.

## Commit & Pull Request Guidelines
- Follow repository precedent: imperative summary, optional scope (`Feature/mcp`), and linked issue `(#123)` when applicable.
- Keep commits narrowly scoped and include config or fixture updates with the code they support.
- PRs should describe runtime impact (profiles, ports, env vars), reference issues, and include UI screenshots when behaviour changes.
- Run `mvn verify` or the relevant Docker workflow before review; note any skipped checks and how to reproduce the result.

## Security & Configuration Tips
- Do not commit secrets; stash overrides under `configs/` and explain required env vars in the PR.
- When enabling external services, update `src/main/resources/application.yaml` plus sample overrides and mention connection expectations for reviewers.
