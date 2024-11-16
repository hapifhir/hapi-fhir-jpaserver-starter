# Validation of FHIR resources

matchbox can validate FHIR resources if they are conform to the FHIR R4/R4B or R5 specification
and conform to the requirements of specific implementation guides.

a tutorial how to validate FHIR resources and to what error message you have to look out for is availabe [here](validation-tutorial.md).

Validation is based on the official [HL7 Java reference validator](https://confluence.hl7.org/display/FHIR/Using+the+FHIR+Validator) in accordance with the provided terminologies. An external terminology server can also be configured, and you can validate your implementation through the $validate operation with the FHIR API or through a simple GUI.

## matchbox engine

see [matchbox-engine](matchbox-engine.md) for using the validation functionality as a library.

## matchbox

### FHIR API

For the $validate operation on the server, see the OperationDefinition for validation support:
[[server]/$validate](https://test.ahdis.ch/matchboxv3/fhir/OperationDefinition/-s-validate) for checking FHIR 
resources conforming to the loaded implementation guides.

| Parameter IN | Card | Description                                                                                                                                                                                                                                |
|--------------|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| resource     | 1..1 | The resource (or logical instance) to validate, needs to be provided in the HTTP Body, the HTTP Content-Type header needs to be indicated if it is application/fhir+json or application/fhir+xml                                           |
| profile      | 1..1 | profile to validate the resources against. Within the profile parameter, the targetProfile returns all profiles possible to use for validation, with the canonical url, and with the canonical url and the business version of the profile |
| ig           | 0..1 | the Implementation Guide and version which should be used for validation, eg. ch.fhir.ig.ch-core#3.0.0                                                                                                                                     |

The operation returns an array of OperationOutcome with severity defined as fatal, error, warning or information.

The first entry will contain the information in what configuration the validation was performed, e.g:

```bash
Validation for profile http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient|3.0.0 (2018-10-15T00:00:00+10:00) with packages: hl7.fhir.xver-extensions#0.0.13, hl7.fhir.r4.core#4.0.1, hl7.terminology#5.1.0, ihe.formatcode.fhir#1.1.0, ch.fhir.ig.ch-epr-term#2.0.8, ch.fhir.ig.ch-core#3.0.0 No Issues detected. Total: 4430ms powered by matchbox-engine 3.1.0, hapi-fhir 6.2.5 and org.hl7.fhir.core 5.6.92 validation parameters Parameters {doNative=false, hintAboutNonMustSupport=false, recursive=false, doDebug=false, assumeValidRestReferences=false, canDoNative=false, noExtensibleBindingMessages=false, noUnicodeBiDiControlChars=false, noInvariants=false, wantInvariantsInMessages=false, txServer='http://tx.fhir.org', lang='null', snomedCT='900000000000207008', targetVer='null', ig=ch.fhir.ig.ch-core#3.0.0, questionnaireMode=CHECK, level=HINTS, mode=VALIDATION, securityChecks=false, crumbTrails=false, forPublication=false, jurisdiction=urn:iso:std:iso:3166#US, allowExampleUrls=false, locale='English', locations={}}
```

### configuration parameters

Default validation parameters can be set directly in provided application.yaml

```yaml
hapi:
  fhir:
    implementationguides:
      fhir_r4_core:
        name: hl7.fhir.r4.core
        version: 4.0.1
        url: classpath:/hl7.fhir.r4.core.tgz
      fhir_terminology:
        name: hl7.terminology
        version: 5.4.0
        url: classpath:/hl7.terminology#5.4.0.tgz
      fhir_extensions:
        name: hl7.fhir.uv.extensions.r4
        version: 1.0.0
        url: classpath:/hl7.fhir.uv.extensions.r4#1.0.0.tgz
      cda:
        name: ch.fhir.ig.cda-fhir-maps
        version: 0.3.0
        url: https://fhir.ch/ig/cda-fhir-maps/package.tgz
      chemd:
        name: ch.fhir.ig.ch-emed
        version: 4.0.1
matchbox:
  fhir:
    context:
      fhirVersion: 4.0.1
      txServer: n/a
      #      onlyOneEngine: true
      #      xVersion : false
      #      igsPreloaded: ch.fhir.ig.ch-core#4.0.0-ballot
      #      autoInstallMissingIgs: true
      suppressWarnInfo:
        hl7.fhir.r4.core#4.0.1:
        #- "Constraint failed: dom-6:"
```

| Parameter             | Card  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|-----------------------|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| implementationguides  | 0..\* | the Implementation Guide and version which with which matchbox will be configured, you can provide by classpath, file, http address, if none is specified the FHIR package servers will be used (need to be online)                                                                                                                                                                                                                                                               |
| txServer              | 0..1  | txServer to use, n/a if none (default), http://localhost:8080/fhir for internal (if server.port in application.yaml is 8080), http://tx.fhir.org for hl7 validator                                                                                                                                                                                                                                                                                                                |
| txUseEcosystem        | 0..1  | boolean true if none (default), if true asks tx servers for which CodeSystem they are authorative                                                                                                                                                                                                                                                                                                                                                                                 |
| txServerCache         | 0..1  | boolean if respones are cached, true  if none (default)                                                                                                                                                                                                                                                                                                                                                                                                                           |
| txLog                 | 0..1  | string indicating file location of log (end either in .txt or .html, default no logging                                                                                                                                                                                                                                                                                                                                                                                           |
| igsPreloaded          | 0..\* | For each mentioned ImplementationGuide (comma separated) an engine will be created, which will be cached in memory as long the application is running. Other IG's will created on demand and will be cached for an hour for subsequent calls. Tradeoff between memory consumption and first response time (creating of engine might have duration of half a minute). Default no igs are preloaded.                                                                                |
| onlyOneEngine         | 0..1  | Implementation Guides can have multiple versions with different dependencies. Matchbox creates for transformation and validation an own engine for each Implementation Guide and its dependencies (default setting). You can switch this behavior, e.g. if you are using it in development and want to create and update resources or transform maps. Set the setting for onlyOneEngine to true. The changes are however not persisted and will be lost if matchbox is restarted. |
| httpReadOnly          | 0..1  | Whether to allow creating, modifying or deleting resources on the server via the HTTP API or not. If `true`, IGs can only be loaded through the configuration.                                                                                                                                                                                                                                                                                                                    |
| suppressWarnInfo      | 0..\* | A list of warning message to ignore while validating resources, per Implementation Guide and version.                                                                                                                                                                                                                                                                                                                                                                             |
| extensions            | 0..1  | Extensions not defined by the ImplementationgGuides which are accepted, comma separted list by url patterns, defaults to 'any'                                                                                                                                                                                                                                                                                                                                                    |
| autoInstallMissingIgs | 0..1  | Whether to automatically install IGs from the public registry if they are not installed. Default to `false`.                                                                                                                                                                                                                                                                                                                                                                      |

#### Suppress warning/information-level issues in validation

The validation client can suppress warning/information-level issues that are not relevant for the validation.
Validation issues to be suppressed are defined in the configuration file, per Implementation Guide and version.

To match by exact substring, simply add the message to the list.
To match by regex pattern, add the prefix `regex:` to the message.

Example of configuration file:
```yaml
matchbox:
  fhir:
    context:
      suppressWarnInfo:
        hl7.fhir.r4.core#4.0.1:
          - "Constraint failed: dom-6:"
        ch.fhir.ig.ch-elm#1.0.0:
          - "regex:Binding for path (.+).ofType\(Coding\) has no source, so can't be checked"
```

### Gazelle EVS API

To integrate Matchbox in the [IHE Gazelle Testing Platform](https://www.ihe-europe.net/testing-IHE/gazelle), the 
EVS API has also been implemented. See the 
[validation-service-api](https://gitlab.inria.fr/gazelle/library/validation-service-api) project for the existing 
documentation.

The list of available profiles is available at
[[server]/gazelle/validation/profiles](https://test.ahdis.ch/matchboxv3/gazelle/validation/profiles), and the 
validation request is sent to `POST [server]/gazelle/validation/validate`.

To configure a Matchbox instance in the EVSClient, the following actions shall be done:

1. In `Administration → Validations services`, create a new validation service with the following parameters:
     - Validator Type: _DEFAULT_
     - Name: as you want
     - Stylesheet report location: any value will do
     - Target endpoint: _[server]/gazelle_
     - Support compression: _YES_
     - Is available: _YES_
     - [Example screenshot](assets/evsclient_validation_service.png)
2. In `Administration → Referenced standards`, create a new standard:
     - Display name: as you want
     - Validator Type: _DEFAULT_
     - Validation filter: optional, can be used to filter the profiles by their IG identifier. E.g. _ch.fhir.ig.ch-core_
     - In _Available validation services_, add the previously created validation service by clicking on the "plus" icon.
     - [Example screenshot](assets/evsclient_referenced_standard.png)
3. In `Administration → Menu Configuration`, add your new standard to an existing menu.
     - [Example screenshot](assets/evsclient_menu.png)
4. You can now validate your resource, the new standard appears in the menu.

## Terminology server

A terminology server may be used to validate resources.
It is useful for the following reasons:

- to validate that a code/coding/codeableConcept is within a complex ValueSet (see under for examples of complex 
  ValueSets);
- to validate that a code/coding/codeableConcept exists in its code system, when the code system is not locally 
  defined (e.g. SNOMED CT, LOINC, ISOs, etc.).

A complex value set (in that context) is a value set that uses filters to define its content instead of declaring 
each code. All [value set filters](http://build.fhir.org/valueset-filter-operator.html) would make a value set too 
complex for the validation client to process the value set expansion by itself.

Matchbox-server comes with an internal terminology server.
It tries to expand the value sets if possible, or simply returns success to validation requests. 
To use it, you can set the terminology server URL to the one of the matchbox-server instance, e.g. 
http://localhost:8080/fhir.

Please be aware that by using the internal terminology server, the validation may be incomplete.
Its use case is simple:

- all CodeSystems that are used are defined in the IG, or is a special CodeSystem supported by HAPI-FHIR;
- in your resources, all important code/coding/codeableConcept are bound to a simple value set, with 'required' 
  strength.

Please be aware that if you have unbound code/coding/codeableConcepts, or the binding is not 'required', the 
code/coding/codeableConcept will be considered valid if the code system is not defined locally (e.g. SNOMED CT, 
LOINC, etc).

### GUI

You can run a validation through the GUI by using the fragment in the URL.
The fragment is the part of the URL that starts with a `#` character (also called the hash or anchor).
The different parameters shall be set as if they were in the query string:
`/matchbox/validate#resource=...&profile=...`.

The following parameters are supported:

| Parameter        | Description                                                            |
|------------------|------------------------------------------------------------------------|
| `resource`       | The resource to validate, encoded as base64url. Required.              |
| `profile`        | The profile to use for validation. Optional.                           |
| Other parameters | Any parameter supported by the FHIR API or the `CliContext`. Optional. |

The only required parameter is `resource`.
If `profile` is not provided, it will default to `http://hl7.org/fhir/StructureDefinition/{resourceType}`.

E.g.
```http
GET https://test.ahdis.ch/matchboxv3/validate#profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FBundle&txServer=http://tx.fhir.org/r4&resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDxuYW1lPgogICAgPGZhbWlseSB2YWx1ZT0iVGVzdCIvPgogIDwvbmFtZT4KPC9QYXRpZW50Pg HTTP/1
```
