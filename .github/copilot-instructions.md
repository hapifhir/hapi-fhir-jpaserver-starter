# HAPI FHIR JPA Server Starter - Copilot Instructions

## Repository Overview

This is the **HAPI FHIR JPA Server Starter Project**, a complete starter application for deploying a FHIR server using HAPI FHIR JPA. This is an end-user implementation project, not the core HAPI FHIR library source.

**Technology Stack:**
- **Language:** Java 17 (minimum requirement)
- **Build Tool:** Maven 3.8.3+ (verified with 3.9.11)
- **Framework:** Spring Boot 3.4.11
- **Server:** Embedded Tomcat or Jetty, deployable as WAR
- **Database:** H2 (default), PostgreSQL, MS SQL Server supported
- **Testing:** JUnit 5 (Jupiter), Testcontainers
- **FHIR Version Support:** DSTU2, DSTU3, R4, R4B, R5

**Project Size:**
- ~70 Java source files in src/main/java
- ~20 test files in src/test/java
- Main package: `ca.uhn.fhir.jpa.starter`

## Critical Build & Validation Commands

### Always Use These Commands in This Order

**1. Clean Compile (5-10 seconds):**
```bash
mvn clean compile
```
- Compiles all Java sources
- Does not run tests
- Good for quick validation after code changes

**2. Format Check (10-15 seconds):**
```bash
mvn spotless:check
```
- **CRITICAL:** This MUST pass or CI will fail
- Checks Google Java Format compliance
- If it fails, fix with: `mvn spotless:apply`
- Alternatively: `mvn clean install -DskipTests` auto-formats

**3. Unit Tests (80-90 seconds):**
```bash
mvn test
```
- Runs JUnit tests with Surefire plugin
- Tests: CustomBeanTest, CustomInterceptorTest, CustomOperationTest, MdmTest, ParallelUpdatesVersionConflictTest, FhirServerConfigCommonBinaryStorageTest
- Uses H2 in-memory database
- Safe to run frequently

**4. Package Build (20-25 seconds, without tests):**
```bash
mvn clean package -DskipTests
```
- Creates target/ROOT.war
- Skips test execution for faster builds
- Use when you need the WAR but tests aren't needed

**5. Full Build with Tests (8-10 minutes):**
```bash
mvn clean install
```
- Compiles, runs unit tests (Surefire), runs integration tests (Failsafe)
- Integration tests (ExampleServerR4IT, ElasticsearchLastNR4IT, etc.) are SLOW
- Creates target/ROOT.war
- **Important:** Integration tests expect H2 database configuration

**6. Verify (includes integration tests, 8-10 minutes):**
```bash
mvn verify
```
- Runs both Surefire (unit) and Failsafe (integration) tests
- Alternative to `mvn install` when you don't need to install to local Maven repo
- Same timing as `mvn install`

### Database Configuration Impact on Tests

**CRITICAL:** When you change database configuration from H2 to PostgreSQL/MS SQL Server in `src/main/resources/application.yaml`, integration tests WILL FAIL because they expect H2.

**Workaround options:**
1. Skip tests: `mvn clean install -DskipTests`
2. Skip only integration tests: `mvn clean package` (runs unit tests only)
3. Update test fixtures to match new database (more work)

The README explicitly documents this in the PostgreSQL configuration section:
> "Because the integration tests within the project rely on the default H2 database configuration, it is important to either explicitly skip the integration tests during the build process, i.e., `mvn install -DskipTests`, or delete the tests altogether. Failure to skip or delete the tests once you've configured PostgreSQL for the datasource.driver, datasource.url, and hibernate.dialect as outlined above will result in build errors and compilation failure."

## Project Structure & Key Files

### Source Code Layout
```
src/main/java/ca/uhn/fhir/jpa/starter/
├── Application.java           # Spring Boot entry point
├── AppProperties.java         # Configuration properties
├── annotations/               # Custom annotations
├── cdshooks/                 # CDS Hooks implementation
├── common/                   # Shared configuration (FhirServerConfigCommon.java)
├── cr/                       # Clinical Reasoning
├── elastic/                  # Elasticsearch configuration
├── ig/                       # Implementation Guide handling
├── ips/                      # International Patient Summary
├── mcp/                      # Model Context Protocol (MCP) support
├── mdm/                      # Master Data Management
├── terminology/              # Terminology services
├── util/                     # Utilities
└── web/                      # Web/REST controllers

src/main/resources/
├── application.yaml          # Main configuration (~430+ lines)
├── application-cds.yaml      # Clinical reasoning profile
├── logback.xml              # Logging configuration
└── mdm-rules.json           # MDM matching rules

src/main/webapp/
└── WEB-INF/templates/        # Thymeleaf UI templates

src/test/java/ca/uhn/fhir/jpa/starter/
├── *IT.java                  # Integration tests (Failsafe)
├── *Test.java               # Unit tests (Surefire)
└── common/                  # Test utilities
```

### Configuration Files
- `pom.xml` - Maven build configuration (~750 lines)
- `application.yaml` - Runtime configuration, datasource, HAPI FHIR settings
- `Dockerfile` - Multi-stage build (distroless for production, tomcat for debugging)
- `docker-compose.yml` - PostgreSQL + HAPI FHIR setup
- `server.xml` - Tomcat server configuration
- `catalina.properties` - Tomcat properties
- `.editorconfig` - Editor configuration (4-space indents)

### Build & CI Files
- `.github/workflows/maven.yml` - Main CI build: `mvn -B package --file pom.xml verify`
- `.github/workflows/spotless-check.yml` - Code formatting check (runs on PRs)
- `.github/workflows/smoke-tests.yml` - HTTP-based smoke tests using IntelliJ HTTP client
- `.github/workflows/build-images.yaml` - Docker image builds
- `.github/workflows/chart-*.yaml` - Helm chart testing/release

## Common Build Issues & Solutions

### Issue 1: Spotless Check Failure
**Symptom:** PR build fails with "formatting check failed"
**Solution:**
```bash
mvn spotless:apply
# or
mvn clean install -DskipTests  # This also runs spotless:apply
```

### Issue 2: Tests Fail After Database Change
**Symptom:** Changed datasource to PostgreSQL, now tests fail
**Solution:** Skip tests during build:
```bash
mvn clean install -DskipTests
# or just package without integration tests
mvn clean package
```

### Issue 3: Out of Memory During Build
**Symptom:** Build fails with OutOfMemoryError
**Solution:** The Dockerfile uses this workaround:
```bash
mvn clean install -DskipTests -Djdk.lang.Process.launchMechanism=vfork
```

### Issue 4: Port 8080 Already in Use
**Symptom:** Cannot start server on port 8080
**Solution:** Change port in `src/main/resources/application.yaml`:
```yaml
server:
  port: 8888
```
Also update the tester configuration URL to match.

## Running the Server Locally

### Option 1: Spring Boot with Default Profile (Recommended for Development)
```bash
mvn spring-boot:run
# Or with Jetty profile:
mvn -Pjetty spring-boot:run
```
- Server: http://localhost:8080/fhir
- Metadata: http://localhost:8080/fhir/metadata
- Takes 60-80 seconds to start (includes WAR extraction, dependency resolution)

### Option 2: Bootable WAR
```bash
mvn clean package spring-boot:repackage -Pboot
java -jar target/ROOT.war
```
- Server: http://localhost:8080/fhir
- Useful for production-like testing

### Option 3: Docker
```bash
./build-docker-image.sh
docker run -p 8080:8080 hapi-fhir/hapi-fhir-jpaserver-starter
```
- Server: http://localhost:8080/fhir
- Uses multi-stage Dockerfile with Maven + OpenJDK 17

### Option 4: Docker Compose (with PostgreSQL)
```bash
docker-compose up -d --build
```
- HAPI FHIR: http://localhost:8080/fhir
- PostgreSQL on port 5432 (internal to Docker network)

## Database Configuration Patterns

### H2 (Default - In-Memory)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test_mem
    username: sa
    password: null
    driver-class-name: org.h2.Driver
  jpa:
    properties:
      hibernate.dialect: ca.uhn.fhir.jpa.model.dialect.HapiFhirH2Dialect
```
**Use case:** Development, testing, CI

### H2 (File-based)
```yaml
spring:
  datasource:
    url: "jdbc:h2:file:./target/database/h2"
```
**Use case:** Persistent local development

### PostgreSQL
```yaml
spring:
  datasource:
    url: 'jdbc:postgresql://localhost:5432/hapi'
    username: admin
    password: admin
    driverClassName: org.postgresql.Driver
  jpa:
    properties:
      hibernate.dialect: ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgresDialect
      hibernate.search.enabled: false
      # Comment out all hibernate.search.backend.* settings
```
**Important:** Must disable Elasticsearch when using PostgreSQL unless explicitly configured
**Remember:** Integration tests will fail unless you skip them with `-DskipTests`

### MS SQL Server
```yaml
spring:
  datasource:
    url: 'jdbc:sqlserver://<server>:<port>;databaseName=<databasename>'
    username: admin
    password: admin
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
```
**Important:** 
- Do NOT set hibernate.dialect explicitly (auto-detected)
- Use case-sensitive collation to avoid errors with UCUM valuesets
- Integration tests will fail unless you skip them

## Code Style & Conventions

### Java Style
- **Indentation:** 4 spaces (see .editorconfig)
- **Format:** Google Java Format (enforced by spotless)
- **Imports:** Alphabetized, no wildcards
- **Package Structure:** Mirror main in tests (ca.uhn.fhir.jpa.starter.*)
- **Naming:**
  - Classes: `*Provider`, `*Service`, `*Config` suffixes
  - Tests: `*Test.java` (unit), `*IT.java` (integration)
  - Constructor injection with `final` fields preferred

### YAML/JSON Style
- YAML: kebab-case keys
- JSON fixtures: lower_snake_case filenames

### Testing Conventions
- Unit tests: `src/test/java/**/*Test.java` (Surefire)
- Integration tests: `src/test/java/**/*IT.java` (Failsafe)
- Test fixtures: `src/test/resources/` (colocate with tests)
- Use existing patterns from ExampleServerR4IT.java

## Making Changes

### Before Starting
1. Read AGENTS.md for repository conventions
2. Understand existing code patterns
3. Identify if your change affects configuration, Java code, or both

### Typical Change Workflow
1. **Make code changes**
2. **Format code:** `mvn spotless:apply`
3. **Compile:** `mvn clean compile` (5-10 seconds)
4. **Run relevant tests:** `mvn test` (80-90 seconds)
5. **If config changed:** Test with `mvn spring-boot:run` (80 seconds startup)
6. **Final check:** `mvn clean package -DskipTests` (20-25 seconds)
7. **Commit changes**

### For Database-Related Changes
- If testing with non-H2 database:
  - Use `mvn clean install -DskipTests` for builds
  - Document the database setup in PR description
  - Include sample configuration snippets

### For Adding Dependencies
- Update pom.xml
- Run `mvn clean compile` to validate
- Check for version conflicts: `mvn dependency:tree`

## CI/CD Pipeline

### Pull Request Checks
1. **Maven Build** (.github/workflows/maven.yml)
   - Command: `mvn -B package --file pom.xml verify`
   - Runs on: All branches, on push and PR
   - Duration: ~8-10 minutes
   - Java: 17 (Zulu distribution)

2. **Spotless Check** (.github/workflows/spotless-check.yml)
   - Command: `mvn spotless:check`
   - Runs on: PRs (opened, reopened, synchronize)
   - Duration: ~10-15 seconds
   - If fails: Run `mvn spotless:apply` locally

3. **Smoke Tests** (.github/workflows/smoke-tests.yml)
   - Builds project, starts server with Jetty
   - Runs IntelliJ HTTP client tests
   - Tests in: `src/test/smoketest/plain_server.http`
   - Duration: ~2-3 minutes

### Docker Image Build
- Workflows: build-images.yaml
- Builds both `tomcat` and `default` (distroless) images
- Published to Docker Hub as hapiproject/hapi

## Special Features & Modules

### Clinical Reasoning (CR)
- Enable: `hapi.fhir.cr.enabled=true` in application.yaml
- Alternative config: application-cds.yaml
- Dependency: cqf-fhir-cr-hapi 4.0.0

### CDS Hooks
- Enable: `hapi.fhir.cdshooks.enabled=true`
- Requires CR module also enabled
- Endpoint: /cds-services
- Test: CdsHooksServletIT.java

### MDM (Master Data Management)
- Enable: `hapi.fhir.mdm_enabled=true`
- Rules: src/main/resources/mdm-rules.json
- Requires subscriptions enabled
- Test: MdmTest.java

### MCP (Model Context Protocol)
- Enable: `spring.ai.mcp.server.enabled=true`
- Endpoint: /mcp/messages (hardcoded)
- Connect: `npx @modelcontextprotocol/inspector` via Streamable HTTP
- Tools: src/main/java/ca/uhn/fhir/jpa/starter/mcp/

### Subscriptions
- REST Hook: `hapi.fhir.subscription.resthook_enabled=true`
- Email: Configure `hapi.fhir.subscription.email.*` with SMTP details
- WebSocket: `hapi.fhir.subscription.websocket_enabled=true`
  - Endpoint: ws://localhost:8080/websocket

### Elasticsearch
- Configure in hibernate.search.backend section of application.yaml
- Alternative to Lucene for full-text indexing
- Required for $lastn operation

## Key Facts to Remember

1. **Maven profiles:**
   - Default profile: `boot` (Spring Boot Tomcat)
   - Alternative: `-Pjetty` for Jetty
   - Build profile: `-Pboot` for bootable WAR

2. **Test execution:**
   - Unit tests: automatic with `mvn test` or `mvn package`
   - Integration tests: require `mvn verify` or `mvn install`
   - Time: Unit ~80s, Integration adds ~7-8 minutes

3. **Configuration hierarchy:**
   - Default: application.yaml
   - Override via environment variables
   - Override via `--spring.config.location`
   - Override via Docker volume mount

4. **Port configuration cascades:**
   - Change server.port in application.yaml
   - Must also update hapi.fhir.tester.*.server_address
   - Document port changes in docker-compose.yml if used

5. **Common TODO markers in code:**
   - McpCdsBridge.java: CDS Hooks request JSON building
   - ToolFactory.java: MCP tool improvements
   - These are known incomplete features, not blockers

6. **War file location:**
   - Standard build: target/ROOT.war
   - Bootable version: target/ROOT.war (after spring-boot:repackage)
   - Original artifact: target/ROOT.war.original (after repackage)

## Validation Checklist

Before submitting a PR, verify:
- [ ] Code compiles: `mvn clean compile`
- [ ] Code formatted: `mvn spotless:check` (or run `mvn spotless:apply`)
- [ ] Unit tests pass: `mvn test`
- [ ] Package builds: `mvn clean package -DskipTests`
- [ ] No unintended changes to application.yaml database config
- [ ] If database config changed, documented workaround for tests
- [ ] Documented any new environment variables or config properties
- [ ] Ran server locally to verify runtime behavior

## Quick Reference

**Most common commands:**
```bash
# Format code
mvn spotless:apply

# Quick compile check
mvn clean compile

# Run tests (fast)
mvn test

# Build WAR (skip tests)
mvn clean package -DskipTests

# Full build with all tests (slow)
mvn clean install

# Start development server
mvn spring-boot:run
```

**When in doubt:** Check AGENTS.md, README.md, and existing code patterns before making changes.
