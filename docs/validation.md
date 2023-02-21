# Validation of FHIR resources

matchbox can validate FHIR resources if they are conform to the FHIR R4 specification
and conform to the requirements of specific implememenationg guides.

Validation is based on the official [HL7 Java reference validator](https://confluence.hl7.org/display/FHIR/Using+the+FHIR+Validator)in accordance with the provided terminologies. An external terminology server can also be configured, and you can validate your implementation through the $validate operation with the FHIR API or through a simple GUI.

## matchbox engine

see [matchbox-engine](/matchbox-engine) for using the validation functionaliy as a library.

## matchbox

### API

For the $validate operation on the server see the OperationDefintion for validation support: [[server]/$validate](https://test.ahdis.ch/matchboxv3/fhir/OperationDefinition/-s-validate) for checking FHIR resources conforming to the loaded implementation guides.

| Parameter IN | Card | Description                                                                                                                                                                                                                                 |
| ------------ | ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| resource     | 1..1 | The resource (or logical instance) to validate, needs to be provided in the HTTP Body, the HTTP Content-Type header needs to be indicated if it is application/fhir+json or application/fhir+xml                                            |
| profile      | 1..1 | profile to validate the resources against. Within the profile parameter, the targetProfile returns all profiles possible to use for validation, with the canonincal url, and with the canonical url and the business version of the profile |
| ig           | 0..1 | the Implementation Guide and version which should be used for validation, eg. ch.fhir.ig.ch-core#3.0.0                                                                                                                                      |
| txServer     | 0..1 | txServer to use, n/a if none (default)                                                                                                                                                                                                      |

The oeration returns an array of OperationOutcome with severity defined as fatal, error, warning or information.

The first entry will contain the information in what configuraiton the validation was perfomed, e.g:

```bash
Validation for profile http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient|3.0.0 (2018-10-15T00:00:00+10:00) with packages: hl7.fhir.xver-extensions#0.0.13, hl7.fhir.r4.core#4.0.1, hl7.terminology#5.0.0, ihe.formatcode.fhir#1.1.0, ch.fhir.ig.ch-epr-term#2.0.8, ch.fhir.ig.ch-core#3.0.0 No Issues detected. Total: 4430ms powered by matchbox-engine 3.1.0, hapi-fhir 6.2.5 and org.hl7.fhir.core 5.6.92 validation parameters Parameters {doNative=false, hintAboutNonMustSupport=false, recursive=false, doDebug=false, assumeValidRestReferences=false, canDoNative=false, noExtensibleBindingMessages=false, noUnicodeBiDiControlChars=false, noInvariants=false, wantInvariantsInMessages=false, txServer='http://tx.fhir.org', lang='null', snomedCT='900000000000207008', targetVer='null', ig=ch.fhir.ig.ch-core#3.0.0, questionnaireMode=CHECK, level=HINTS, mode=VALIDATION, securityChecks=false, crumbTrails=false, forPublication=false, jurisdiction=urn:iso:std:iso:3166#US, allowExampleUrls=false, locale='English', locations={}}
```

### configuration parameters

Default validation parameters can be set directly in provided application.yaml

```yaml
matchbox:
  fhir:
    context:
      txServer: n/a
```
