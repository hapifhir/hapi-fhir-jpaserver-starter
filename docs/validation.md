# Validation of FHIR resources

matchbox can validate FHIR resources if they are conform to the FHIR R4 specification
and conform to the requirements of specific implementation guides.

Validation is based on the official [HL7 Java reference validator](https://confluence.hl7.org/display/FHIR/Using+the+FHIR+Validator)in accordance with the provided terminologies. An external terminology server can also be configured, and you can validate your implementation through the $validate operation with the FHIR API or through a simple GUI.

## matchbox engine

see [matchbox-engine](/matchbox-engine) for using the validation functionality as a library.

## matchbox

### API

For the $validate operation on the server see the OperationDefintion for validation support: [[server]/$validate](https://test.ahdis.ch/matchboxv3/fhir/OperationDefinition/-s-validate) for checking FHIR resources conforming to the loaded implementation guides.

| Parameter IN | Card | Description                                                                                                                                                                                                                                 |
| ------------ | ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| resource     | 1..1 | The resource (or logical instance) to validate, needs to be provided in the HTTP Body, the HTTP Content-Type header needs to be indicated if it is application/fhir+json or application/fhir+xml                                            |
| profile      | 1..1 | profile to validate the resources against. Within the profile parameter, the targetProfile returns all profiles possible to use for validation, with the canonical url, and with the canonical url and the business version of the profile |
| ig           | 0..1 | the Implementation Guide and version which should be used for validation, eg. ch.fhir.ig.ch-core#3.0.0                                                                                                                                      |
| txServer     | 0..1 | txServer to use, n/a if none (default)                                                                                                                                                                                                      |

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
        version: 5.3.0
        url: classpath:/hl7.terminology#5.3.0.tgz
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
        version: 3.0.0
matchbox:
  fhir:
    context:
      txServer: n/a
      igsPreloaded: ch.fhir.ig.cda-fhir-maps#0.3.0, ch.fhir.ig.ch-emed#3.0.0
      onlyOneEngine: false
```

| Parameter            | Card  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| -------------------- | ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| implementationguides | 0..\* | the Implementation Guide and version which with which matchbox will be configured, you can provide by classpath, file, http address, if none is specified the FHIR package servers will be used (need to be online)                                                                                                                                                                                                                                                                |
| txServer             | 0..1  | txServer to use, n/a if none (default)                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| igsPreloaded         | 0..\* | For each mentioned ImplementationGuide (comma separated) an engine will be created, which will be cached in memory as long the application is running. Other IG's will created on demand and will be cached for an hour for subsequent calls. Tradeoff between memory consumption and first response time (creating of engine might have duration of half a minute). Default no igs are preloaded.                                                                                  |
| onlyOneEngine        | 0..1  | Implementation Guides can have multiple versions with different dependencies. Matchbox creates for transformation and validation an own engine for each Implementation Guide and its dependencies (default setting). You can switch this behavior, e.g. if you are using it in development and want to create and update resources or transform maps. Set the setting for onlyOneEngine to true. The changes are however not persisted and will be lost if matchbox is restarted. |
