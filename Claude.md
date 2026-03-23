# Matchbox Project Overview

## What is Matchbox?

Matchbox validation and mapping platform that provides:
- **FHIR validation** against profiles and implementation guides
- **FHIR mapping language** transformations (StructureMap)
- **CDA to FHIR** transformation support
- **Questionnaire** form filling capabilities
- **Terminology services** integration
- **AI-powered validation** assistance

## Project Structure

The project consists of three main components:

```
matchbox/
├── matchbox-engine/          # Core FHIR validation and transformation library
├── matchbox-server/          # Full FHIR server with REST API
├── matchbox-frontend/        # Angular web UI
└── docs/                     # MkDocs documentation
```

## Component Details

### 1. matchbox-engine (Core Library)

**Purpose**: Reusable Java library for FHIR validation and transformation

**Technology Stack**:
- Java 21
- Spring Framework 6.1
- HAPI FHIR 8.0.0
- HL7 FHIR Core 6.7.10 (official FHIR validator)

**Key Capabilities**:
- FHIR resource validation (R4, R5, R4B)
- StructureMap-based transformations
- CDA to FHIR conversion
- FHIR package loading and management


**Key Classes**:
```
ch.ahdis.matchbox.engine/
└── MatchboxEngine             # Core engine class
```

**Testing**:
- JUnit 5 for unit tests
- XMLUnit for XML comparisons
- Comprehensive validation test suites for R4/R5

**Build**:
```bash
cd matchbox-engine
mvn clean install
```

### 2. matchbox-server (FHIR Server)

**Purpose**: Production-ready FHIR API support

**Technology Stack**:
- Java 21
- Spring Boot 3.3.11
- HAPI FHIR JPA Server 8.0.0
- Embedded Tomcat 10.1.48
- H2 (default) / PostgreSQL (production)

**Key Features**:
- Full FHIR REST API (R4, R5, R4B)
- `$validate` operation with detailed outcomes
- `$transform` operation for StructureMap transformations
- `$convert` operation for CDA conversion
- `$snapshot` operation for profile expansion
- AI-powered validation (LangChain4j integration)
- Model Context Protocol (MCP) server support

**Configuration**:
- Context path: `/matchbox` or `/matchboxv3`
- Default port: 8080
- Config file: `application.yaml`
- Environment variables supported
- Static files: `/static/` (Angular frontend)

**Main Entry Point**:
- `ca.uhn.fhir.jpa.starter.Application`

**Key Packages**:
```
ch.ahdis.matchbox/
├── config/                    # Spring Boot configuration
├── providers/                 # FHIR operation providers
│   ├── ValidationProvider     # $validate operation
│   ├── StructureMapProvider   # $transform operation
│   └── ConvertProvider        # $convert operation
├── interceptors/              # Request/response processing
│   ├── MatchboxValidationInterceptor
│   ├── MappingLanguageInterceptor
│   └── HttpReadOnlyInterceptor
├── terminology/               # Terminology services
├── packages/                  # FHIR package management
├── mcp/                       # Model Context Protocol
└── util/                      # Utilities
```

**Database Configuration**:

*H2 (Default - Development)*:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./database/h2
```

*PostgreSQL (Production)*:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/matchbox
    username: matchbox
    password: matchbox
    driverClassName: org.postgresql.Driver
  jpa:
    properties:
      hibernate.dialect: ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgres94Dialect
```

**Build & Run**:
```bash
# Build with frontend
cd matchbox-frontend
npm install
npm run build

cd ../matchbox-server
mvn clean install

# Run
java -jar target/matchbox.jar

# Or with Maven
mvn spring-boot:run
```

**Docker Deployment**:
```bash
# Basic deployment
docker run -p 8080:8080 ghcr.io/ahdis/matchbox:latest

# With PostgreSQL
cd matchbox-server/with-postgres
docker-compose up

# With pre-loaded implementation guides
cd matchbox-server/with-preload
docker-compose up
```

**Available Docker Configurations**:
- `with-postgres/` - PostgreSQL database
- `with-preload/` - Pre-loaded Swiss IGs
- `with-ch/` - Swiss EPR configuration
- `with-ca/` - Canadian configuration
- `with-ips/` - International Patient Summary
- `with-cda/` - CDA transformation support
- `with-gazelle/` - IHE Gazelle integration

### 3. matchbox-frontend (Angular Web UI)

**Purpose**: Modern web interface for FHIR validation and transformation

**Technology Stack**:
- Angular 19.0.0
- TypeScript 5.6.3
- Angular Material 19.0.0
- fhir-kit-client 1.9.2
- FHIRPath.js 3.15.2
- Ace Editor 1.36.5 (code editing)
- NGX-Translate 16.0.3 (i18n)

**Key Features**:
- Interactive FHIR validation with real-time feedback
- StructureMap editor and transformation testing
- Implementation guide browser
- Resource upload and management
- Questionnaire form filling
- FHIRPath expression evaluation
- Multi-language support (i18n)

**Module Structure**:
```
src/app/
├── validate/                  # Validation UI
├── transform/                 # Transformation UI
├── mapping-language/          # StructureMap editor
├── igs/                       # Implementation guides browser
├── upload/                    # Resource upload
├── settings/                  # Server settings
├── capability-statement/      # Capability statement viewer
├── shared/                    # Shared components
└── util/                      # Utility services
```

**Development Server**:
```bash
cd matchbox-frontend
npm install
npm start
# Opens on http://localhost:4200
# Proxies API calls to http://localhost:8080
```

**Build for Production**:
```bash
npm run build
# Output: ../matchbox-server/src/main/resources/static
```

**Testing**:
```bash
# Unit tests
npm test

# E2E tests
npm run e2e

# Linting
npm run lint
```

**Configuration**:
- `angular.json` - Angular CLI configuration
- `src/proxy.conf.json` - Dev server proxy
- `src/environments/` - Environment-specific settings
- `tsconfig.json` - TypeScript compiler options

## Development Workflow

### Initial Setup

```bash
# Clone repository
git clone https://github.com/ahdis/matchbox.git
cd matchbox

# Build engine
cd matchbox-engine
mvn clean install

# Build and run server
cd ../matchbox-server
mvn spring-boot:run
```

### Frontend Development

```bash
# Terminal 1: Run backend
cd matchbox-server
mvn spring-boot:run

# Terminal 2: Run frontend dev server
cd matchbox-frontend
npm install
npm start
# Visit http://localhost:4200
```

### Full Build (Backend + Frontend)

```bash
# Build frontend and copy to server resources
cd matchbox-frontend
npm install
npm run build

# Build server with embedded frontend
cd ../matchbox-server
mvn clean install

# Run
java -jar target/matchbox.jar
# Visit http://localhost:8080/matchboxv3
```

## Testing

### Backend Tests

```bash
# All tests
mvn clean test

# Specific test class
mvn -Dtest=MatchboxApiR4Test test

# Integration tests
mvn verify
```

**Key Test Classes**:
- `MatchboxApiR4Test` - R4 API tests
- `MatchboxApiR5Test` - R5 API tests
- `ValidationClient` - Validation testing utilities
- `TransformTest` - StructureMap transformation tests
- `GazelleApiR4Test` - IHE Gazelle integration tests

## Configuration

### Key Application Properties

**application.yaml** (located in `matchbox-server/src/main/resources/`):

```yaml
server:
  servlet:
    context-path: /matchboxv3
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./database/h2
    username: sa
    password: null
    driverClassName: org.h2.Driver

hapi:
  fhir:
    fhir_version: R4
    server_address: http://localhost:8080/matchboxv3/fhir
    allow_external_references: true
    delete_expunge_enabled: true
    openapi_enabled: true

matchbox:
  fhir:
    context:
      txServer: http://tx.fhir.org
      igsPreloaded:
        - hl7.fhir.r4.core#4.0.1
```

### Environment Variables

```bash
# Override server port
SERVER_PORT=8888

# PostgreSQL configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/matchbox
SPRING_DATASOURCE_USERNAME=matchbox
SPRING_DATASOURCE_PASSWORD=matchbox

# Terminology server
MATCHBOX_FHIR_CONTEXT_TXSERVER=http://tx.fhir.org

# AI/LLM configuration
MATCHBOX_FHIR_CONTEXT_LLM_PROVIDER=openai
MATCHBOX_FHIR_CONTEXT_LLM_MODELNAME=gpt-4
MATCHBOX_FHIR_CONTEXT_LLM_APIKEY=sk-...
```

### Docker Volume Mounts

```bash
docker run -d \
  -p 8080:8080 \
  -v $(pwd)/config:/config \
  -v $(pwd)/database:/database \
  ghcr.io/ahdis/matchbox:latest
```

## API Endpoints

### FHIR Operations

- `POST /matchboxv3/fhir/StructureDefinition/$validate`
  - Validate FHIR resources against profiles

- `POST /matchboxv3/fhir/StructureMap/$transform`
  - Transform resources using StructureMap

- `POST /matchboxv3/fhir/StructureDefinition/$convert`
  - Convert CDA documents to FHIR

- `GET /matchboxv3/fhir/StructureDefinition/{id}/$snapshot`
  - Generate snapshot from differential

- `GET /matchboxv3/fhir/metadata`
  - Server capability statement

### OpenAPI Documentation

- Swagger UI: `http://localhost:8080/matchboxv3/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/matchboxv3/v3/api-docs`

### Actuator Endpoints

- Health: `/matchboxv3/actuator/health`
- Metrics: `/matchboxv3/actuator/metrics`
- Info: `/matchboxv3/actuator/info`

## Important Files & Locations

### Backend

| File/Directory | Purpose |
|----------------|---------|
| `pom.xml` | Parent Maven configuration |
| `matchbox-engine/pom.xml` | Engine build configuration |
| `matchbox-server/pom.xml` | Server build configuration |
| `matchbox-server/src/main/resources/application.yaml` | Main configuration |
| `matchbox-server/src/main/resources/logback.xml` | Logging configuration |
| `matchbox-server/src/main/java/ca/uhn/fhir/jpa/starter/Application.java` | Main entry point |
| `matchbox-server/Dockerfile` | Docker image definition |
| `matchbox-server/with-*/` | Docker Compose examples |

### Frontend

| File/Directory | Purpose |
|----------------|---------|
| `matchbox-frontend/package.json` | npm dependencies |
| `matchbox-frontend/angular.json` | Angular CLI configuration |
| `matchbox-frontend/tsconfig.json` | TypeScript configuration |
| `matchbox-frontend/src/main.ts` | Frontend entry point |
| `matchbox-frontend/src/app/app.module.ts` | Root Angular module |
| `matchbox-frontend/src/proxy.conf.json` | Dev server proxy |
| `matchbox-frontend/src/assets/` | Static assets, i18n |

### Documentation

| File/Directory | Purpose |
|----------------|---------|
| `README.md` | Project overview |
| `docs/` | MkDocs documentation |
| `mkdocs.yml` | Documentation configuration |
| `docs/validation-tutorial.md` | Validation guide |
| `docs/api.md` | API documentation |

## CI/CD Pipeline

GitHub Actions workflows (`.github/workflows/`):

- **maven.yml** - Maven build and test
- **integration_tests.yml** - Integration tests
- **angular_build.yml** - Frontend build
- **angular_test.yml** - Frontend tests
- **documentation.yml** - Docs deployment
- **googleregistry.yml** - Docker image publishing
- **central_repository.yml** - Maven Central publishing

## Release Process

1. Update versions in `pom.xml`, `package.json`, and documentation
2. Create PR and wait for tests to pass
3. Merge PR to main, but on origin (https://github.com/ahdis/matchbox.git) not on upstream
4. Wait for Angular build workflow to complete
5. Create GitHub release with tag (e.g., `v4.0.16`)
6. Automated workflows publish:
   - Docker image to Google Artifact Registry
   - Maven artifacts to Maven Central

## Dependencies Management

### Major Dependencies

**Backend**:
- HAPI FHIR 8.0.0 - FHIR server framework
- HL7 FHIR Core 6.7.10 - Official validator
- Spring Boot 3.3.11 - Application framework
- Jackson 2.17.1 - JSON processing
- Hibernate - JPA/ORM
- LangChain4j 1.0.0-beta1 - AI/LLM integration

**Frontend**:
- Angular 19.0.0 - Web framework
- Angular Material 19.0.0 - UI components
- fhir-kit-client 1.9.2 - FHIR client
- FHIRPath.js 3.15.2 - FHIRPath evaluation
- Ace Editor 1.36.5 - Code editor

### Update Dependencies

```bash
# Backend
mvn versions:display-dependency-updates

# Frontend
npm outdated
```

## Troubleshooting

### Common Issues

**Issue**: Port 8080 already in use
```bash
# Solution: Change port
SERVER_PORT=8888 java -jar matchbox.jar
```

**Issue**: Out of memory errors
```bash
# Solution: Increase heap size
java -Xmx4096M -jar matchbox.jar
```

**Issue**: Frontend build fails
```bash
# Solution: Clear node_modules and reinstall
cd matchbox-frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

**Issue**: Database connection errors
```bash
# Solution: Check database is running and config is correct
# For H2, ensure database directory exists and is writable
mkdir -p database
```

### Logging

Enable debug logging:
```yaml
logging:
  level:
    ch.ahdis.matchbox: DEBUG
    ca.uhn.fhir: DEBUG
```

Or via environment variable:
```bash
LOGGING_LEVEL_CH_AHDIS_MATCHBOX=DEBUG
```

## Resources

- **GitHub Repository**: https://github.com/ahdis/matchbox
- **Documentation**: https://ahdis.github.io/matchbox/
- **Docker Images**: https://github.com/ahdis/matchbox/pkgs/container/matchbox
- **Maven Central**: https://central.sonatype.com/artifact/ch.ahdis/matchbox-engine
- **FHIR Specification**: https://hl7.org/fhir/
- **HAPI FHIR**: https://hapifhir.io/

## Support & Contributing

- **Issues**: https://github.com/ahdis/matchbox/issues
- **Discussions**: https://github.com/ahdis/matchbox/discussions
- **Contributing Guide**: See `matchbox-frontend/CONTRIBUTING.md`

## License

Apache License 2.0
