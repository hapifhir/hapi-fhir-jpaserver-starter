# Matchbox Server

Matchbox is a FHIR server that can be deployed as a microservice in your IT infrastructure.
It includes all features from Matchbox-engine, plus additional features that are API-specific.

## Configuration

The following configuration parameters are available for the Matchbox server.
They can be set in the Spring configuration (e.g. `application.properties`/`application.yml`), or as system properties.

| Parameter                                | Default value | Description                                                                                                                                                     |
|------------------------------------------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `matchbox.fhir.context.fhirVersion`      | `4.0.1`       | The FHIR version of the server.                                                                                                                                 |
| `matchbox.fhir.context.txServer`         | `n/a`         | The URL of the terminology server to use, or `n/a` not to use a terminology server.                                                                             |
| `matchbox.fhir.context.igsPreloaded`     | `[]`          | The list of IGs to always pre-load when initializing a Matchbox engine.                                                                                         |
| `matchbox.fhir.context.onlyOneEngine`    | `false`       | Forces the server to initialize only one engine. See the section [_Only one engine_](#only-one-engine) below.                                                   |
| `matchbox.fhir.context.xVersion`         | `false`       | Allows to transform resources between FHIR versions. See the section [_Transforming resources between FHIR versions_](#transform-cross-version) below.          |
| `matchbox.fhir.context.suppressWarnInfo` | `{}`          | The list of warnings/infos to suppress in validation reports. See [_Suppress warning/information-level issues in validation_](validation.md#suppress-warnings). |
| `matchbox.fhir.context.httpReadOnly`     | `false`       | Whether the server is in read-only mode or not. See the section [_Read-only mode_](#read-only) below.                                                           |
| `matchbox.fhir.context.extensions`       | `any`         | The list of domains allowed in extensions while validating resources; `any` will allow all extensions.                                                          |

See an example of configuration, to show the expected format:

```yaml
matchbox:
  fhir:
    context:
      igsPreloaded: ch.fhir.ig.ch-elm#1.4.0
      suppressWarnInfo:
        hl7.fhir.r4.core#4.0.1:
          - "Constraint failed: dom-6:"
          - "regex:Entry '(.+)' isn't reachable by traversing forwards from the Composition"
        ch.fhir.ig.ch-elm:
          - "regex:Binding for path (.+) has no source, so can't be checked"
          - "regex:None of the codings provided are in the value set 'Observation Interpretation Codes'(.*)"
```

The HAPI configuration parameters are also available.
The following example shows the use of the most commonly used parameters:

```yaml
hapi:
  fhir:
    implementationguides:
      fhir_r4_core:
        name: hl7.fhir.r4.core
        version: 4.0.1
        url: classpath:/hl7.fhir.r4.core.tgz
      ch-core:
        name: ch.fhir.ig.ch-core
        version: 4.0.1
      ch-elm:
        name: ch.fhir.ig.ch-elm
        version: 1.7.0
        url: https://build.fhir.org/ig/ahdis/ch-elm/package.tgz
```

Some more configuration parameters used by validation are described in the
[Validation](validation.md#configuration-parameters) page.

## Read-only mode {: #read-only}

When enabling `httpReadOnly`, the server will reject any operation that would modify its state.
In particular:

1. The following FHIR operations are disabled: `CREATE`, `DELETE`, `UPDATE`, `PATCH`, `UPDATE_REWRITE_HISTORY`,
   `TRANSACTION`, `BATCH`, `ADD_TAGS`, `DELETE_TAGS`, `META_ADD`, `META_DELETE`.
2. The feature [_Installing an NPM package through the operation $install-npm-package_](#install-npm-package) is 
   disabled.
3. The feature [_Auto-installation of FHIR packages in the $validate operation_](#auto-install-ig) is disabled.

It is helpful to enable this mode when the server is used in a production environment (e.g. as a validation server),
to make sure its state can't be changed through the API.

## Only one engine {: #only-one-engine}

When enabling `onlyOneEngine`, the server will initialize a single Matchbox engine, use it for all requests, and keep it
running for the whole serve life duration.
It provides the following advantages:

- It lowers the memory and CPU consumption, as a single engine is shared among all requests.
- It speeds up the response time, as the engine is already initialized and ready to use.

It is helpful to enable this mode when the server is used in a production environment (e.g. as a validation server).

Disabling this mode is recommended when more context separation is required, in particular when dealing with the 
following situations:

- when installing ImplementationGuides that use different FHIR versions, the server will need to initialize multiple 
  engines, to only load the FHIR Core needed for each IG;
- when installing multiple versions of the same ImplementationGuide, the server will also initialize separate 
  engines, with only the dependencies required.

## Installing an NPM package through the operation $install-npm-package {: #install-npm-package}

The operation `$install-npm-package` allows you to install an NPM package through the API.
It is available if and only if `httpReadOnly` is disabled.

| Parameter IN | Card | Description                                 |
|--------------|------|---------------------------------------------|
| name         | 1..1 | The name of the package.                    |
| version      | 1..1 | The version of the package.                 |
| body         | 1..1 | The content of the NPM package as HTTP body |

```http
POST /matchboxv3/fhir/$install-npm-package?name=ch.fhir.ig.ch-core&version=4.0.1 HTTP/1.1
Content-Type: application/gzip

<gzip content of the NPM package>
```

## Auto-installation of FHIR packages in the $validate operation {: #auto-install-ig}

The operation `$validate` will try to install the FHIR package if it is not already installed, if and only if
`httpReadOnly` is disabled.

The FHIR package to install will be determined by (in the implemented order):

1. The `ig` parameter, if present;
2. The `profile` parameter. Matchbox will search for IGs that contain that canonical with the
   [Simplifier API](https://app.swaggerhub.com/apis-docs/firely/Simplifier.net_FHIR_Package_API/1.0.1) and install 
   the first result.

## Transforming resources between FHIR versions {: #transform-cross-version}

If you intend to transform resources between FHIR versions (e.g. from R4 to R5), you need to enable the `xVersion` 
configuration parameter.

This mode will load the [FHIR Cross-Version Mapping Pack](https://build.fhir.org/ig/HL7/fhir-cross-version/) package,
which contains _StructureMaps_ for all FHIR Core resources.
Matchbox will also force the right version on the FHIR Core _StructureDefinitions_, to allow their use by the 
_StructureMaps_.
