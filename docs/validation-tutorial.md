# How to validate FHIR resources

This article shows, how FHIR resource can be validated and analyzed.

Validation is a powerful tool to verify that you produce conformant FHIR resources, however the amount of error messages can be overwhelming if you have complex FHIR data structures like a FHIR IPS document (see validation result [here](https://gazelle.ihe.net/evs/report.seam?oid=1.3.6.1.4.1.12559.11.11.4.17466&standard=31)).

## What does a validator check

The validation tooling checks the following to verify that your FHIR resource is conformant on the following levels:

1. Structure (Syntax) 
2. Semantics  
3. Constraints by the FHIR Specification 
4. Constraints by FHIR Implementation Guides  
   - Additional rules for FHIR implementation guides 
   - Slicing in FHIR documents

You should fix first the structure errors, then the semantics error before you tackle the constraint errors.

We will go through the different type of error messages to explain what possibilities you have to identify the errors.

## Using matchbox as a validation tool

You can use matchbox as a validation tool with a GUI, a test instance is located at: [http://test.ahdis.ch/matchboxv3/fhir](http://test.ahdis.ch/matchboxv3/fhir). Alternatively matchbox offers a $validate operation and you can call this directly with a client. In this article we will use VSCode together with the RESTClient extension to show the different validation behaviors, you can open this file locally in your VS Code instance and then you can click on `Send Request`. As an alternative here is also a link for each validation provided which triggers the validation on the matchbox test instance.  

To check if you have set it up correctly, perform a first successful validation: This should validate the XML Patient resource with a family name test against the Patient profile of the FHIR specification, in the test instance, the default validation is to FHIR Release R4:

Validation Request with [matchbox](https://test.ahdis.ch/matchboxv3/?resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDxuYW1lPgogICAgPGZhbWlseSB2YWx1ZT0iVGVzdCIvPgogIDwvbmFtZT4KPC9QYXRpZW50Pg%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate):
```http
@host = https://test.ahdis.ch/matchboxv3/fhir
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Accept: application/fhir+json
Content-Type: application/fhir+xml

<Patient xmlns="http://hl7.org/fhir">
  <name>
    <family value="Test"/>
  </name>
</Patient>
```

Validation response by matchbox:
```json
{
  "resourceType": "OperationOutcome",
  "id": "75e89102282782b6c16df8acfb99310150a261ee35eed42c3b181b14d00a051e",
  "issue": [
    {
      "severity": "information",
      "code": "informational",
      "diagnostics": "Validation for profile http://hl7.org/fhir/StructureDefinition/Patient|4.0.1 (2019-11-01T09:29:23+11:00). Loaded packages: hl7.fhir.r4.core#4.0.1, hl7.fhir.xver-extensions#4.0, hl7.terminology#5.4.0, hl7.fhir.uv.extensions.r4#1.0.0. Duration: 0.015s. powered by matchbox 3.9.3, hapi-fhir 7.4.0 and org.hl7.fhir.core 6.3.24. Validation parameters: CliContext{doNative=false, extensions=[any], hintAboutNonMustSupport=false, recursive=false, showMessagesFromReferences=false, doDebug=false, assumeValidRestReferences=false, canDoNative=false, noExtensibleBindingMessages=false, noUnicodeBiDiControlChars=false, noInvariants=false, displayIssuesAreWarnings=true, wantInvariantsInMessages=false, doImplicitFHIRPathStringConversion=false, htmlInMarkdownCheck=WARNING, txServer='https://test.ahdis.ch/matchboxv3/fhir', txServerCache='true', txLog='null', txUseEcosystem=true, lang='null', snomedCT='null', fhirVersion='4.0.1', ig='hl7.fhir.r4.core#4.0.1', questionnaireMode=CHECK, level=HINTS, mode=VALIDATION, securityChecks=false, crumbTrails=false, forPublication=false, allowExampleUrls=true, locale='en', locations={}, jurisdiction='urn:iso:std:iso:3166#US', igsPreloaded=[ch.fhir.ig.ch-elm#1.4.0], onlyOneEngine=false, xVersion=false, httpReadOnly=false}"
    },
    {
      "severity": "information",
      "code": "informational",
      "diagnostics": "No fatal or error issues detected, the validation has passed"
    }
  ]
}
```

The validation response always contains a FHIR OperationOutcome Resource, with a list of different issues encountered during validation. One issue with severity information and code information will be always present with which configuration parameters the validation has been performed. Those parameters can change by different requests. The GUI of matchbox shows the exact same information in the results. If you have no fatal or error issues you will get another information issue with "No fatal or error issues detected, the validation has passed", meaning you passed the validation according to the specified profile!

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| information    | information     | No fatal or error issues detected, the validation has passed. |   |


## Structure (Syntax) issues

You have to provide your FHIR resource in the right structure and syntax (xml or json) and also in the right FHIR version. If the validator is not able to parse your message as a FHIR resource (or as FHIR logical model instance, e.g. a CDA instance), you will get issues with a type of severity error or fatal.

e.g. you will get back the following error for requesting an [invalid Patient xml resource validation](https://test.ahdis.ch/matchboxv3/?resource=SGVsbG8gdmFsaWRhdG9y&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate) (just sending text instead):

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Accept: application/fhir+json
Content-Type: application/fhir+xml

Hello validator
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| fatal    | invalid     | Content is not allowed in prolog. | (xml) |
| error    | invalid     | The XML encoding is invalid (must be UTF-8) | XML |

or an invalid Patient in a json validation:


```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Accept: application/fhir+json
Content-Type: application/fhir+json

Hello validator
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| fatal    | invalid     | Error parsing JSON: Error parsing JSON source: Unexpected content at start of JSON: String at Line 1 (path=[Hello]) | |

### Common additional XML Structure issues

#### Not providing an xml namespace

[example no xml namespace](https://test.ahdis.ch/matchboxv3/?resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDxnZW5kZXIgdmFsdWU9Im1hbGUiIC8%2BCiAgPG5hbWU%2BCiAgICA8ZmFtaWx5IHZhbHVlPSJUZXN0IiAvPgogIDwvbmFtZT4KPC9QYXRpZW50Pg%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate)

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Accept: application/fhir+json
Content-Type: application/fhir+xml

<Patient>
  <name>
    <family value="Test"/>
  </name>
</Patient>
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| fatal    | structure     | This content cannot be parsed (unknown or unrecognized XML Root element namespace/name 'noNamespace::Patient') | "Patient" |

#### Wrong XML element test (not allowed)

[example undefined xml element](https://test.ahdis.ch/matchboxv3/?resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDx0ZXN0IHZhbHVlPSJ0cnVlIiAvPgo8L1BhdGllbnQ%2B&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate)
```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+xml

<Patient xmlns="http://hl7.org/fhir">
  <test value="true" />
</Patient>
```
| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | structure   | Undefined element 'test' at /f:Patient" | "f:/Patient" |

#### Wrong order of xml elements

[example wrong order](https://test.ahdis.ch/matchboxv3/?resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDxnZW5kZXIgdmFsdWU9Im1hbGUiIC8%2BCiAgPG5hbWU%2BCiAgICA8ZmFtaWx5IHZhbHVlPSJUZXN0IiAvPgogIDwvbmFtZT4KPC9QYXRpZW50Pg%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate)
```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+xml

<Patient xmlns="http://hl7.org/fhir">
  <gender value="male" />
  <name>
    <family value="Test" />
  </name>
</Patient>
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | invalid   | As specified by profile http://hl7.org/fhir/StructureDefinition/Patient|4.0.1, Element 'name' is out of order (found after gender) | Patient.name[0] |

the gender element must be after the name.


### common JSON Structure error messages

#### JSON Array for 0..*

If an element has a 0..* cardinality this needs in json representation an array. 

```json
{
  "resourceType": "Patient",
  "active" : true,
  "name": [
    {
      "family": "Test"
    }
  ]
}
```

sending it as as a key, object will give an [error](https://test.ahdis.ch/matchboxv3/?resource=PFBhdGllbnQgeG1sbnM9Imh0dHA6Ly9obDcub3JnL2ZoaXIiPgogIDx0ZXN0IHZhbHVlPSJ0cnVlIiAvPgo8L1BhdGllbnQ%2B&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate):

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
	"resourceType": "Patient",
	"name": {
      "family": "Test"
	}
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | invalid   |The property name must be a JSON Array, not an Object (at Patient) | Patient.name |


#### boolean or numbers not quoted

in JSON booleans or numbers must not be quoted as objects [example](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKCSJhY3RpdmUiIDogInRydWUiCiAgfQ%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate):

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
  "resourceType": "Patient",
  "active" : "true"
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | invalid   | Error parsing JSON: the primitive value must be a boolean | Patient.active |

### Cardinality

The validator checks that the cardinality of all properties are correct, there can be min & max requirements, 
e.g. Patient.communication.language is required if you provide Patient.communication [example](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKCSJjb21tdW5pY2F0aW9uIiA6IFsgewoJCSJwcmVmZXJyZWQiOiB0cnVlCgkgIH0KCV0KICB9&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate):

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
  "resourceType": "Patient",
  "communication" : [ {
      "preferred": true
    }
  ]
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | structure   | Patient.communication.language: minimum required = 1, but only found 0 (from http://hl7.org/fhir/StructureDefinition/Patient|4.0.1) | communication[0]|


### Value Domain

The validator checks that all values of all properties conform to the rules for the specified types (including checking that enumerated codes are valid).

#### invalid content for boolean
```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
  "resourceType": "Patient",
  "active" : trueorfalse
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | invalid   | The JSON property 'active' has no quotes around the value of the property 'trueorfalse'| Patient |
| error    | invalid   | Error parsing JSON: the primitive value must be a boolean | Patient.active |
| error    | invalid   | Boolean values must be 'true' or 'false' | Patient.active |


## Semantic validation

The validator checks also the validity of the codes and if the codes are appropriate for the bounded ValueSet according to the binding strength.
For this a validator needs the support of a terminology server which provides a [terminology service](https://hl7.org/fhir/terminology-service.html#4.7). 

matchbox can use different terminology severs:
- tx.fhir.org which is the default terminology sever used by the FHIR validator
- Ontoserver as of version 6.15.0 
- matchbox provides an internal terminology server, but this internal terminology server has only limited terminology capabilities, e.g. it will not validate display names or it can't expand intensional ValueSets.

### invalid code for gender

[example](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKCSJnZW5kZXIiIDogIj8iCiAgfQ%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate)
```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
  "resourceType": "Patient",
  "gender" : "?"
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | code-invalid   | The value provided ('?') was not found in the value set 'AdministrativeGender' (http://hl7.org/fhir/ValueSet/administrative-gender|4.0.1), and a code is required from this value set  (error message = The System URI could not be determined for the code '?' in the ValueSet 'http://hl7.org/fhir/ValueSet/administrative-gender|4.0.1'; The provided code '#?' was not found in the value set 'http://hl7.org/fhir/ValueSet/administrative-gender|4.0.1') | Patient.gender |


### intensional ValueSet

ValueSets can be [intensional](https://hl7.org/fhir/valueset.html#int-ext) which requires a terminology server to expand them or verify if a code is valid within. E.g. see [Caveat Condition](https://fhir.ch/ig/ch-rad-order/ValueSet-ch-rad-order-caveat-condition.html) where the ValueSet includes codes from http://snomed.info/sct where concept is-a 397578001 (Device in situ (finding)).

validation with [tx.fhir.org](https://test.ahdis.ch/matchboxv3/?resource=ew0KICAicmVzb3VyY2VUeXBlIiA6ICJDb25kaXRpb24iLA0KICAiaWQiIDogIkNhdmVhdERldmljZUNhcmRpYWNQYWNlbWFrZXIiLA0KICAibWV0YSIgOiB7DQogICAgInByb2ZpbGUiIDogWyJodHRwOi8vZmhpci5jaC9pZy9jaC1yYWQtb3JkZXIvU3RydWN0dXJlRGVmaW5pdGlvbi9jaC1yYWQtb3JkZXItY2F2ZWF0LWNvbmRpdGlvbiJdDQogIH0sDQogICJ0ZXh0IiA6IHsNCiAgICAic3RhdHVzIiA6ICJleHRlbnNpb25zIiwNCiAgICAiZGl2IiA6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU6IENvbmRpdGlvbjwvYj48YSBuYW1lPVwiQ2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlclwiPiA8L2E%2BPGEgbmFtZT1cImhjQ2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlclwiPiA8L2E%2BPC9wPjxkaXYgc3R5bGU9XCJkaXNwbGF5OiBpbmxpbmUtYmxvY2s7IGJhY2tncm91bmQtY29sb3I6ICNkOWUwZTc7IHBhZGRpbmc6IDZweDsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkICM4ZGExYjQ7IGJvcmRlci1yYWRpdXM6IDVweDsgbGluZS1oZWlnaHQ6IDYwJVwiPjxwIHN0eWxlPVwibWFyZ2luLWJvdHRvbTogMHB4XCI%2BUmVzb3VyY2VDb25kaXRpb24gJnF1b3Q7Q2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlciZxdW90OyA8L3A%2BPHAgc3R5bGU9XCJtYXJnaW4tYm90dG9tOiAwcHhcIj5Qcm9maWxlOiA8YSBocmVmPVwiU3RydWN0dXJlRGVmaW5pdGlvbi1jaC1yYWQtb3JkZXItY2F2ZWF0LWNvbmRpdGlvbi5odG1sXCI%2BQ0ggUkFELU9yZGVyIENhdmVhdCBDb25kaXRpb248L2E%2BPC9wPjwvZGl2PjxwPjxiPkNIIFJBRC1PcmRlciBDYXZlYXQgVHlwZTwvYj46IERldmljZSBpbiBzaXR1IChmaW5kaW5nKSAoRGV0YWlsczogU05PV01FRCBDVCBjb2RlIDM5NzU3ODAwMSA9ICdEZXZpY2UgaW4gc2l0dSAoZmluZGluZyknLCBzdGF0ZWQgYXMgJ0RldmljZSBpbiBzaXR1IChmaW5kaW5nKScpPC9wPjxwPjxiPkNIIFJBRC1PcmRlciBDYXZlYXQgVHlwZTwvYj46IFByZXNlbnQgKHF1YWxpZmllciB2YWx1ZSkgKERldGFpbHM6IFNOT1dNRUQgQ1QgY29kZSA1MjEwMTAwNCA9ICdQcmVzZW50Jywgc3RhdGVkIGFzICdQcmVzZW50IChxdWFsaWZpZXIgdmFsdWUpJyk8L3A%2BPHA%2BPGI%2BY2xpbmljYWxTdGF0dXM8L2I%2BOiBBY3RpdmUgPHNwYW4gc3R5bGU9XCJiYWNrZ3JvdW5kOiBMaWdodEdvbGRlblJvZFllbGxvdzsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkIGtoYWtpXCI%2BICg8YSBocmVmPVwiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvNS41LjAvQ29kZVN5c3RlbS1jb25kaXRpb24tY2xpbmljYWwuaHRtbFwiPkNvbmRpdGlvbiBDbGluaWNhbCBTdGF0dXMgQ29kZXM8L2E%2BI2FjdGl2ZSk8L3NwYW4%2BPC9wPjxwPjxiPmNhdGVnb3J5PC9iPjogUHJvYmxlbSBMaXN0IEl0ZW0gPHNwYW4gc3R5bGU9XCJiYWNrZ3JvdW5kOiBMaWdodEdvbGRlblJvZFllbGxvdzsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkIGtoYWtpXCI%2BICg8YSBocmVmPVwiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvNS41LjAvQ29kZVN5c3RlbS1jb25kaXRpb24tY2F0ZWdvcnkuaHRtbFwiPkNvbmRpdGlvbiBDYXRlZ29yeSBDb2RlczwvYT4jcHJvYmxlbS1saXN0LWl0ZW0pPC9zcGFuPjwvcD48cD48Yj5jb2RlPC9iPjogQ2FyZGlhYyBwYWNlbWFrZXIgaW4gc2l0dSA8c3BhbiBzdHlsZT1cImJhY2tncm91bmQ6IExpZ2h0R29sZGVuUm9kWWVsbG93OyBtYXJnaW46IDRweDsgYm9yZGVyOiAxcHggc29saWQga2hha2lcIj4gKDxhIGhyZWY9XCJodHRwczovL2Jyb3dzZXIuaWh0c2RvdG9vbHMub3JnL1wiPlNOT1dNRUQgQ1Q8L2E%2BIzQ0MTUwOTAwMik8L3NwYW4%2BPC9wPjxwPjxiPnN1YmplY3Q8L2I%2BOiA8YSBocmVmPVwiUGF0aWVudC1TVWZmZXJlci5odG1sXCI%2BUGF0aWVudC9TVWZmZXJlcjwvYT4gJnF1b3Q7IFVGRkVSRVImcXVvdDs8L3A%2BPC9kaXY%2BIg0KICB9LA0KICAiZXh0ZW5zaW9uIiA6IFt7DQogICAgInVybCIgOiAiaHR0cDovL2ZoaXIuY2gvaWcvY2gtcmFkLW9yZGVyL1N0cnVjdHVyZURlZmluaXRpb24vY2gtcmFkLW9yZGVyLWNhdmVhdC10eXBlIiwNCiAgICAidmFsdWVDb2RpbmciIDogew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsDQogICAgICAiY29kZSIgOiAiMzk3NTc4MDAxIiwNCiAgICAgICJkaXNwbGF5IiA6ICJEZXZpY2UgaW4gc2l0dSAoZmluZGluZykiDQogICAgfQ0KICB9LA0KICB7DQogICAgInVybCIgOiAiaHR0cDovL2ZoaXIuY2gvaWcvY2gtcmFkLW9yZGVyL1N0cnVjdHVyZURlZmluaXRpb24vY2gtcmFkLW9yZGVyLXF1YWxpZmllci12YWx1ZSIsDQogICAgInZhbHVlQ29kaW5nIiA6IHsNCiAgICAgICJzeXN0ZW0iIDogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLA0KICAgICAgImNvZGUiIDogIjUyMTAxMDA0IiwNCiAgICAgICJkaXNwbGF5IiA6ICJQcmVzZW50IChxdWFsaWZpZXIgdmFsdWUpIg0KICAgIH0NCiAgfV0sDQogICJjbGluaWNhbFN0YXR1cyIgOiB7DQogICAgImNvZGluZyIgOiBbew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvQ29kZVN5c3RlbS9jb25kaXRpb24tY2xpbmljYWwiLA0KICAgICAgImNvZGUiIDogImFjdGl2ZSINCiAgICB9XQ0KICB9LA0KICAiY2F0ZWdvcnkiIDogW3sNCiAgICAiY29kaW5nIiA6IFt7DQogICAgICAic3lzdGVtIiA6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL2NvbmRpdGlvbi1jYXRlZ29yeSIsDQogICAgICAiY29kZSIgOiAicHJvYmxlbS1saXN0LWl0ZW0iLA0KICAgICAgImRpc3BsYXkiIDogIlByb2JsZW0gTGlzdCBJdGVtIg0KICAgIH1dDQogIH1dLA0KICAiY29kZSIgOiB7DQogICAgImNvZGluZyIgOiBbew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsDQogICAgICAiY29kZSIgOiAiNDQ0NDQ0NDQ0IiwNCiAgICAgICJkaXNwbGF5IiA6ICJub3RpbnZhbHVlc2V0Ig0KICAgIH1dDQogIH0sDQogICJzdWJqZWN0IiA6IHsNCiAgICAicmVmZXJlbmNlIiA6ICJQYXRpZW50L1NVZmZlcmVyIg0KICB9DQp9&profile=http%3A%2F%2Ffhir.ch%2Fig%2Fch-rad-order%2FStructureDefinition%2Fch-rad-order-caveat-condition&txServer=http%3A%2F%2Ftx.fhir.org#/validate):


| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | code-invalid   | Unknown code '444444444' in the CodeSystem 'http://snomed.info/sct' version 'http://snomed.info/sct/900000000000207008/version/20240801' | Condition.code.coding[0].code |
| error    | code-invalid   | None of the codings provided are in the value set 'Caveat Condition' (http://fhir.ch/ig/ch-rad-order/ValueSet/ch-rad-order-caveat-condition|2.0.0-ballot), and a coding from this value set is required) (codes = http://snomed.info/sct#444444444)" | Condition.code |

validation with matchbox's internal terminology server will not return an error, because the intensional ValueSet will be [ignored](https://test.ahdis.ch/matchboxv3/?resource=ew0KICAicmVzb3VyY2VUeXBlIiA6ICJDb25kaXRpb24iLA0KICAiaWQiIDogIkNhdmVhdERldmljZUNhcmRpYWNQYWNlbWFrZXIiLA0KICAibWV0YSIgOiB7DQogICAgInByb2ZpbGUiIDogWyJodHRwOi8vZmhpci5jaC9pZy9jaC1yYWQtb3JkZXIvU3RydWN0dXJlRGVmaW5pdGlvbi9jaC1yYWQtb3JkZXItY2F2ZWF0LWNvbmRpdGlvbiJdDQogIH0sDQogICJ0ZXh0IiA6IHsNCiAgICAic3RhdHVzIiA6ICJleHRlbnNpb25zIiwNCiAgICAiZGl2IiA6ICI8ZGl2IHhtbG5zPVwiaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbFwiPjxwPjxiPkdlbmVyYXRlZCBOYXJyYXRpdmU6IENvbmRpdGlvbjwvYj48YSBuYW1lPVwiQ2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlclwiPiA8L2E%2BPGEgbmFtZT1cImhjQ2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlclwiPiA8L2E%2BPC9wPjxkaXYgc3R5bGU9XCJkaXNwbGF5OiBpbmxpbmUtYmxvY2s7IGJhY2tncm91bmQtY29sb3I6ICNkOWUwZTc7IHBhZGRpbmc6IDZweDsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkICM4ZGExYjQ7IGJvcmRlci1yYWRpdXM6IDVweDsgbGluZS1oZWlnaHQ6IDYwJVwiPjxwIHN0eWxlPVwibWFyZ2luLWJvdHRvbTogMHB4XCI%2BUmVzb3VyY2VDb25kaXRpb24gJnF1b3Q7Q2F2ZWF0RGV2aWNlQ2FyZGlhY1BhY2VtYWtlciZxdW90OyA8L3A%2BPHAgc3R5bGU9XCJtYXJnaW4tYm90dG9tOiAwcHhcIj5Qcm9maWxlOiA8YSBocmVmPVwiU3RydWN0dXJlRGVmaW5pdGlvbi1jaC1yYWQtb3JkZXItY2F2ZWF0LWNvbmRpdGlvbi5odG1sXCI%2BQ0ggUkFELU9yZGVyIENhdmVhdCBDb25kaXRpb248L2E%2BPC9wPjwvZGl2PjxwPjxiPkNIIFJBRC1PcmRlciBDYXZlYXQgVHlwZTwvYj46IERldmljZSBpbiBzaXR1IChmaW5kaW5nKSAoRGV0YWlsczogU05PV01FRCBDVCBjb2RlIDM5NzU3ODAwMSA9ICdEZXZpY2UgaW4gc2l0dSAoZmluZGluZyknLCBzdGF0ZWQgYXMgJ0RldmljZSBpbiBzaXR1IChmaW5kaW5nKScpPC9wPjxwPjxiPkNIIFJBRC1PcmRlciBDYXZlYXQgVHlwZTwvYj46IFByZXNlbnQgKHF1YWxpZmllciB2YWx1ZSkgKERldGFpbHM6IFNOT1dNRUQgQ1QgY29kZSA1MjEwMTAwNCA9ICdQcmVzZW50Jywgc3RhdGVkIGFzICdQcmVzZW50IChxdWFsaWZpZXIgdmFsdWUpJyk8L3A%2BPHA%2BPGI%2BY2xpbmljYWxTdGF0dXM8L2I%2BOiBBY3RpdmUgPHNwYW4gc3R5bGU9XCJiYWNrZ3JvdW5kOiBMaWdodEdvbGRlblJvZFllbGxvdzsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkIGtoYWtpXCI%2BICg8YSBocmVmPVwiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvNS41LjAvQ29kZVN5c3RlbS1jb25kaXRpb24tY2xpbmljYWwuaHRtbFwiPkNvbmRpdGlvbiBDbGluaWNhbCBTdGF0dXMgQ29kZXM8L2E%2BI2FjdGl2ZSk8L3NwYW4%2BPC9wPjxwPjxiPmNhdGVnb3J5PC9iPjogUHJvYmxlbSBMaXN0IEl0ZW0gPHNwYW4gc3R5bGU9XCJiYWNrZ3JvdW5kOiBMaWdodEdvbGRlblJvZFllbGxvdzsgbWFyZ2luOiA0cHg7IGJvcmRlcjogMXB4IHNvbGlkIGtoYWtpXCI%2BICg8YSBocmVmPVwiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvNS41LjAvQ29kZVN5c3RlbS1jb25kaXRpb24tY2F0ZWdvcnkuaHRtbFwiPkNvbmRpdGlvbiBDYXRlZ29yeSBDb2RlczwvYT4jcHJvYmxlbS1saXN0LWl0ZW0pPC9zcGFuPjwvcD48cD48Yj5jb2RlPC9iPjogQ2FyZGlhYyBwYWNlbWFrZXIgaW4gc2l0dSA8c3BhbiBzdHlsZT1cImJhY2tncm91bmQ6IExpZ2h0R29sZGVuUm9kWWVsbG93OyBtYXJnaW46IDRweDsgYm9yZGVyOiAxcHggc29saWQga2hha2lcIj4gKDxhIGhyZWY9XCJodHRwczovL2Jyb3dzZXIuaWh0c2RvdG9vbHMub3JnL1wiPlNOT1dNRUQgQ1Q8L2E%2BIzQ0MTUwOTAwMik8L3NwYW4%2BPC9wPjxwPjxiPnN1YmplY3Q8L2I%2BOiA8YSBocmVmPVwiUGF0aWVudC1TVWZmZXJlci5odG1sXCI%2BUGF0aWVudC9TVWZmZXJlcjwvYT4gJnF1b3Q7IFVGRkVSRVImcXVvdDs8L3A%2BPC9kaXY%2BIg0KICB9LA0KICAiZXh0ZW5zaW9uIiA6IFt7DQogICAgInVybCIgOiAiaHR0cDovL2ZoaXIuY2gvaWcvY2gtcmFkLW9yZGVyL1N0cnVjdHVyZURlZmluaXRpb24vY2gtcmFkLW9yZGVyLWNhdmVhdC10eXBlIiwNCiAgICAidmFsdWVDb2RpbmciIDogew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsDQogICAgICAiY29kZSIgOiAiMzk3NTc4MDAxIiwNCiAgICAgICJkaXNwbGF5IiA6ICJEZXZpY2UgaW4gc2l0dSAoZmluZGluZykiDQogICAgfQ0KICB9LA0KICB7DQogICAgInVybCIgOiAiaHR0cDovL2ZoaXIuY2gvaWcvY2gtcmFkLW9yZGVyL1N0cnVjdHVyZURlZmluaXRpb24vY2gtcmFkLW9yZGVyLXF1YWxpZmllci12YWx1ZSIsDQogICAgInZhbHVlQ29kaW5nIiA6IHsNCiAgICAgICJzeXN0ZW0iIDogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLA0KICAgICAgImNvZGUiIDogIjUyMTAxMDA0IiwNCiAgICAgICJkaXNwbGF5IiA6ICJQcmVzZW50IChxdWFsaWZpZXIgdmFsdWUpIg0KICAgIH0NCiAgfV0sDQogICJjbGluaWNhbFN0YXR1cyIgOiB7DQogICAgImNvZGluZyIgOiBbew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Rlcm1pbm9sb2d5LmhsNy5vcmcvQ29kZVN5c3RlbS9jb25kaXRpb24tY2xpbmljYWwiLA0KICAgICAgImNvZGUiIDogImFjdGl2ZSINCiAgICB9XQ0KICB9LA0KICAiY2F0ZWdvcnkiIDogW3sNCiAgICAiY29kaW5nIiA6IFt7DQogICAgICAic3lzdGVtIiA6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL2NvbmRpdGlvbi1jYXRlZ29yeSIsDQogICAgICAiY29kZSIgOiAicHJvYmxlbS1saXN0LWl0ZW0iLA0KICAgICAgImRpc3BsYXkiIDogIlByb2JsZW0gTGlzdCBJdGVtIg0KICAgIH1dDQogIH1dLA0KICAiY29kZSIgOiB7DQogICAgImNvZGluZyIgOiBbew0KICAgICAgInN5c3RlbSIgOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsDQogICAgICAiY29kZSIgOiAiNDQ0NDQ0NDQ0IiwNCiAgICAgICJkaXNwbGF5IiA6ICJub3RpbnZhbHVlc2V0Ig0KICAgIH1dDQogIH0sDQogICJzdWJqZWN0IiA6IHsNCiAgICAicmVmZXJlbmNlIiA6ICJQYXRpZW50L1NVZmZlcmVyIg0KICB9DQp9&profile=http%3A%2F%2Ffhir.ch%2Fig%2Fch-rad-order%2FStructureDefinition%2Fch-rad-order-caveat-condition&txServer=http%3A%2F%2Flocalhost%3A8080%2Fmatchboxv3%2Ffhir#/validate) and there is no validation of codes from SNOMED CT or LOINC (CodeSystems not provided directly through the HL7 FHIR ecosystem).

## Constraints by the FHIR Specification 

FHIR adds additional constraints to resources, e.g: https://hl7.org/fhir/patient.html#invs adds a requirement that a contact should have at least a contact's details or a reference to an organization (name.exists() or telecom.exists() or address.exists() or organization.exists()). [example](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKICAgImNvbnRhY3QiIDogW3sKICAgICJyZWxhdGlvbnNoaXAiIDogW3sKICAgICAgImNvZGluZyIgOiBbCiAgICAgIHsKICAgICAgICAic3lzdGVtIiA6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL3YyLTAxMzEiLAogICAgICAgICJjb2RlIiA6ICJOIgogICAgICB9CiAgICAgIF0KICAgIH1dCiAgfV0KfQ%3D%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate)

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
	"resourceType": "Patient",
   "contact" : [{
    "relationship" : [{
      "coding" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v2-0131",
        "code" : "N"
      }
      ]
    }]
  }]
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | invariant   | Constraint failed: pat-1: 'SHALL at least contain a contact's details or a reference to an organization' | Patient.contact[0] |


## Constraints by FHIR Implementation Guides  

### Additional rules for FHIR implementation guides 

FHIR Implementation guides can add additional rules and you can validate if those rules are checked, by specifying the profile for the validation where the rules are defined:

e.g. in FHIR CH Core it is defined that [CH Core Patient EPR](https://fhir.ch/ig/ch-core/StructureDefinition-ch-core-patient-epr.html) is required to have at least one identifier, name and birthDate. If you [validate](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50Igp9&profile=http%3A%2F%2Fhl7.org%2Ffhir%2FStructureDefinition%2FPatient#/validate) an empty patient resource against the base profile you wont get any errors:

```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/StructureDefinition/Patient HTTP/1.1
Content-Type: application/fhir+json

{
	"resourceType": "Patient"
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| information    | information     | No fatal or error issues detected, the validation has passed. |   |

however if you [validate](https://test.ahdis.ch/matchboxv3/?resource=ewoJInJlc291cmNlVHlwZSI6ICJQYXRpZW50Igp9&profile=http%3A%2F%2Ffhir.ch%2Fig%2Fch-core%2FStructureDefinition%2Fch-core-patient-epr#/validate) it against [CH Core Patient EPR ](https://fhir.ch/ig/ch-core/StructureDefinition-ch-core-patient-epr.html) you will get multiple errors:

```http
POST {{host}}/$validate?profile=http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient-epr HTTP/1.1
Content-Type: application/fhir+json

{
	"resourceType": "Patient"
}
```

| Severity | Code        |  Diagnostics                      | Expression     |
|----------|-------------|---------------------------------- |--------------------|
| error    | structure     | Patient.identifier: minimum required = 1, but only found 0 (from http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient-epr|5.0.0-ballot) |  Patient |
| error    | structure     | Patient.gender: minimum required = 1, but only found 0 (from http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient-epr|5.0.0-ballot)|  Patient |
| error    | structure     | Patient.birthDate: minimum required = 1, but only found 0 (from http://fhir.ch/ig/ch-core/StructureDefinition/ch-core-patient-epr|5.0.0-ballot)|  Patient |

### Slicing in FHIR documents

One common feature of constraining FHIR documents (FHIR resource Bundle with type document) is to specify a document profile (e.g. [IPS](https://hl7.org/fhir/uv/ips/StructureDefinition-Bundle-uv-ips.html)). The document profile will then specify the entries which are allowed to appear. 

In a FHIR document the first resource entry needs to be a FHIR Resource Composition, for a specific FHIR document there will be a profile for this Composition (e.g [IPS Composition](https://hl7.org/fhir/uv/ips/StructureDefinition-Composition-uv-ips.html), followed by other resources. 

This is expressed with [slicing](https://hl7.org/fhir/profiling.html#slicing), the Bundle.entry elements are sliced and the document profile says which resources are to be expected in this bundle.entry elements (e.g. at least that Composition defined above). Usually this slices are done on resource/profile and specifying that the Composition needs to appear. For IPS it requires also a Patient resource and defines profiles for optional resources according to the IPS requirements).

A FHIR document has different sections (usually coded by a LOINC code) defined in the Composition resource, and within those sections you will have references to structured FHIR resources, which will be contained in Bundle.entry. This means that the Composition is broken into sections, slice by pattern:code. The IPS has required slices for sectionMedications, sectionAllergies and section Problems.

A minimal IPS document validation looks like this:

[example](https://test.ahdis.ch/matchboxv3/?resource=ewogICAgInJlc291cmNlVHlwZSI6ICJCdW5kbGUiLAogICAgImlkZW50aWZpZXIiOiB7CiAgICAgICAgInN5c3RlbSI6ICJ1cm46b2lkOjIuMTYuNzI0LjQuOC4xMC4yMDAuMTAiLAogICAgICAgICJ2YWx1ZSI6ICIyOGI5NTgxNS03NmNlLTQ1N2ItYjdhZS1hOTcyZTUyN2RiNDAiCiAgICB9LAogICAgInR5cGUiOiAiZG9jdW1lbnQiLAogICAgInRpbWVzdGFtcCI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICJlbnRyeSI6IFsKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiQ29tcG9zaXRpb24iLAogICAgICAgICAgICAgICAgImlkIjogImY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICAgICAic3RhdHVzIjogImZpbmFsIiwKICAgICAgICAgICAgICAgICJ0eXBlIjogewogICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL2xvaW5jLm9yZyIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICI2MDU5MS01IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlBhdGllbnQgc3VtbWFyeSBEb2N1bWVudCIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiZGF0ZSI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICAgICAgICAgICAgICJhdXRob3IiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjQ1MjcxZjdmLTYzYWItNDk0Ni05NzBmLTNkYWFhYTA2NjM3ZiIKICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAgInRpdGxlIjogIlBhdGllbnQgU3VtbWFyeSBhcyBvZiBEZWNlbWJlciAxMSwgMjAyMCAxNDozMCIsCiAgICAgICAgICAgICAgICAiY29uZmlkZW50aWFsaXR5IjogIk4iLAogICAgICAgICAgICAgICAgInNlY3Rpb24iOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAidGl0bGUiOiAiQWN0aXZlIFByb2JsZW1zIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMTE0NTAtNCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlByb2JsZW0gbGlzdCAtIFJlcG9ydGVkIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI%2BPHVsPjxsaT48ZGl2PjxiPkNvbmRpdGlvbiBOYW1lPC9iPjogTWVub3BhdXNhbCBGbHVzaGluZzwvZGl2PjxkaXY%2BPGI%2BQ29kZTwvYj46IDxzcGFuPjE5ODQzNjAwODwvc3Bhbj48L2Rpdj48ZGl2PjxiPlN0YXR1czwvYj46IDxzcGFuPkFjdGl2ZTwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDpkMTc5MzIxZS1jMDkxLTRjZDQtODY0Mi0zYTI3NTM3ZDUwNmQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgInRpdGxlIjogIk1lZGljYXRpb24iLAogICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9sb2luYy5vcmciLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICIxMDE2MC0wIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiSGlzdG9yeSBvZiBNZWRpY2F0aW9uIHVzZSBOYXJyYXRpdmUiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48dWw%2BPGxpPjxkaXY%2BPGI%2BTWVkaWNhdGlvbiBOYW1lPC9iPjogT3JhbCBhbmFzdHJvem9sZSAxbWcgdGFibGV0PC9kaXY%2BPGRpdj48Yj5Db2RlPC9iPjogPHNwYW4%2BPC9zcGFuPjwvZGl2PjxkaXY%2BPGI%2BU3RhdHVzPC9iPjogPHNwYW4%2BQWN0aXZlLCBzdGFydGVkIE1hcmNoIDIwMTU8L3NwYW4%2BPC9kaXY%2BPGRpdj5JbnN0cnVjdGlvbnM6IFRha2UgMSB0aW1lIHBlciBkYXk8L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDplMTI3MWVmZC0xOGZmLTQ2NTQtOWVlNy00NWY0MDAxOWM0NTMiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgInRpdGxlIjogIkFsbGVyZ2llcyBhbmQgSW50b2xlcmFuY2VzIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiNDg3NjUtMiIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIkFsbGVyZ2llcyBhbmQgYWR2ZXJzZSByZWFjdGlvbnMgRG9jdW1lbnQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48dWw%2BPGxpPjxkaXY%2BPGI%2BQWxsZXJneSBOYW1lPC9iPjogUGVuY2lsbGluczwvZGl2PjxkaXY%2BPGI%2BVmVyaWZpY2F0aW9uIFN0YXR1czwvYj46IENvbmZpcm1lZDwvZGl2PjxkaXY%2BPGI%2BUmVhY3Rpb248L2I%2BOiA8c3Bhbj5ubyBpbmZvcm1hdGlvbjwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDo3NDg2MTMxNi1mNjlkLTQ2NTItOWZiMS04NTEyYTIwYzc5MjciCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKICAgICAgICAgICAgICAgICJpZCI6ICIyNDRhZDdjMy1iZWViLTQxZDEtOGEyZi1jNzZiOGNmNzIwYWQiLAogICAgICAgICAgICAgICAgIm5hbWUiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAiZmFtaWx5IjogIkRlTGFyb3NhIiwKICAgICAgICAgICAgICAgICAgICAgICAgImdpdmVuIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgIk1hcnRoYSIKICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICAiYmlydGhEYXRlIjogIjE5NzItMDUtMDEiCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6NDUyNzFmN2YtNjNhYi00OTQ2LTk3MGYtM2RhYWFhMDY2MzdmIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJQcmFjdGl0aW9uZXIiLAogICAgICAgICAgICAgICAgImlkIjogIjQ1MjcxZjdmLTYzYWItNDk0Ni05NzBmLTNkYWFhYTA2NjM3ZiIsCiAgICAgICAgICAgICAgICAibmFtZSI6IFsKICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICJmYW1pbHkiOiAidmFuIEh1bHAiLAogICAgICAgICAgICAgICAgICAgICAgICAiZ2l2ZW4iOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiQmVldGplIgogICAgICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgXQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmQxNzkzMjFlLWMwOTEtNGNkNC04NjQyLTNhMjc1MzdkNTA2ZCIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiQ29uZGl0aW9uIiwKICAgICAgICAgICAgICAgICJpZCI6ICJkMTc5MzIxZS1jMDkxLTRjZDQtODY0Mi0zYTI3NTM3ZDUwNmQiLAogICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vc25vbWVkLmluZm8vc2N0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogIjE5ODQzNjAwOCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGlzcGxheSI6ICJNZW5vcGF1c2FsIGZsdXNoaW5nIChmaW5kaW5nKSIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfQogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDplMTI3MWVmZC0xOGZmLTQ2NTQtOWVlNy00NWY0MDAxOWM0NTMiLAogICAgICAgICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjogIk1lZGljYXRpb25TdGF0ZW1lbnQiLAogICAgICAgICAgICAgICAgImlkIjogImUxMjcxZWZkLTE4ZmYtNDY1NC05ZWU3LTQ1ZjQwMDE5YzQ1MyIsCiAgICAgICAgICAgICAgICAic3RhdHVzIjogImFjdGl2ZSIsCiAgICAgICAgICAgICAgICAibWVkaWNhdGlvblJlZmVyZW5jZSI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjk1ZGI3YzkyLTU2NmEtNGRlZC04OTZiLTIyMjBhYjI0NGE5ZSIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiZWZmZWN0aXZlUGVyaW9kIjogewogICAgICAgICAgICAgICAgICAgICJzdGFydCI6ICIyMDE1LTAzIgogICAgICAgICAgICAgICAgfQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOjk1ZGI3YzkyLTU2NmEtNGRlZC04OTZiLTIyMjBhYjI0NGE5ZSIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiTWVkaWNhdGlvbiIsCiAgICAgICAgICAgICAgICAiaWQiOiAiOTVkYjdjOTItNTY2YS00ZGVkLTg5NmItMjIyMGFiMjQ0YTllIiwKICAgICAgICAgICAgICAgICJjb2RlIjogewogICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL3Nub21lZC5pbmZvL3NjdCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICIxMDg3NzQwMDAiLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiUHJvZHVjdCBjb250YWluaW5nIGFuYXN0cm96b2xlIChtZWRpY2luYWwgcHJvZHVjdCkiCiAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6NzQ4NjEzMTYtZjY5ZC00NjUyLTlmYjEtODUxMmEyMGM3OTI3IiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJBbGxlcmd5SW50b2xlcmFuY2UiLAogICAgICAgICAgICAgICAgImlkIjogIjc0ODYxMzE2LWY2OWQtNDY1Mi05ZmIxLTg1MTJhMjBjNzkyNyIsCiAgICAgICAgICAgICAgICAiY2xpbmljYWxTdGF0dXMiOiB7CiAgICAgICAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vdGVybWlub2xvZ3kuaGw3Lm9yZy9Db2RlU3lzdGVtL2FsbGVyZ3lpbnRvbGVyYW5jZS1jbGluaWNhbCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICJhY3RpdmUiCiAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vc25vbWVkLmluZm8vc2N0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogIjM3MzI3MDAwNCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGlzcGxheSI6ICJTdWJzdGFuY2Ugd2l0aCBwZW5pY2lsbGluIHN0cnVjdHVyZSBhbmQgYW50aWJhY3RlcmlhbCBtZWNoYW5pc20gb2YgYWN0aW9uIChzdWJzdGFuY2UpIgogICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJwYXRpZW50IjogewogICAgICAgICAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIgogICAgICAgICAgICAgICAgfQogICAgICAgICAgICB9CiAgICAgICAgfQogICAgXQp9&profile=http%3A%2F%2Fhl7.org%2Ffhir%2Fuv%2Fips%2FStructureDefinition%2FBundle-uv-ips#/validate)
```http
POST {{host}}/$validate?profile=http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips HTTP/1.1
Content-Type: application/fhir+json

{
    "resourceType": "Bundle",
    "identifier": {
        "system": "urn:oid:2.16.724.4.8.10.200.10",
        "value": "28b95815-76ce-457b-b7ae-a972e527db40"
    },
    "type": "document",
    "timestamp": "2020-12-11T14:30:00+01:00",
    "entry": [
        {
            "fullUrl": "urn:uuid:f40b07e3-37e8-48c3-bf1c-ae70fe12dab0",
            "resource": {
                "resourceType": "Composition",
                "id": "f40b07e3-37e8-48c3-bf1c-ae70fe12dab0",
                "status": "final",
                "type": {
                    "coding": [
                        {
                            "system": "http://loinc.org",
                            "code": "60591-5",
                            "display": "Patient summary Document"
                        }
                    ]
                },
                "subject": {
                    "reference": "urn:uuid:244ad7c3-beeb-41d1-8a2f-c76b8cf720ad"
                },
                "date": "2020-12-11T14:30:00+01:00",
                "author": [
                    {
                        "reference": "urn:uuid:45271f7f-63ab-4946-970f-3daaaa06637f"
                    }
                ],
                "title": "Patient Summary as of December 11, 2020 14:30",
                "confidentiality": "N",
                "section": [
                    {
                        "title": "Active Problems",
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "11450-4",
                                    "display": "Problem list - Reported"
                                }
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul><li><div><b>Condition Name</b>: Menopausal Flushing</div><div><b>Code</b>: <span>198436008</span></div><div><b>Status</b>: <span>Active</span></div></li></ul></div>"
                        },
                        "entry": [
                            {
                                "reference": "urn:uuid:d179321e-c091-4cd4-8642-3a27537d506d"
                            }
                        ]
                    },
                    {
                        "title": "Medication",
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "10160-0",
                                    "display": "History of Medication use Narrative"
                                }
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul><li><div><b>Medication Name</b>: Oral anastrozole 1mg tablet</div><div><b>Code</b>: <span></span></div><div><b>Status</b>: <span>Active, started March 2015</span></div><div>Instructions: Take 1 time per day</div></li></ul></div>"
                        },
                        "entry": [
                            {
                                "reference": "urn:uuid:e1271efd-18ff-4654-9ee7-45f40019c453"
                            }
                        ]
                    },
                    {
                        "title": "Allergies and Intolerances",
                        "code": {
                            "coding": [
                                {
                                    "system": "http://loinc.org",
                                    "code": "48765-2",
                                    "display": "Allergies and adverse reactions Document"
                                }
                            ]
                        },
                        "text": {
                            "status": "generated",
                            "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><ul><li><div><b>Allergy Name</b>: Pencillins</div><div><b>Verification Status</b>: Confirmed</div><div><b>Reaction</b>: <span>no information</span></div></li></ul></div>"
                        },
                        "entry": [
                            {
                                "reference": "urn:uuid:74861316-f69d-4652-9fb1-8512a20c7927"
                            }
                        ]
                    }
                ]
            }
        },
        {
            "fullUrl": "urn:uuid:244ad7c3-beeb-41d1-8a2f-c76b8cf720ad",
            "resource": {
                "resourceType": "Patient",
                "id": "244ad7c3-beeb-41d1-8a2f-c76b8cf720ad",
                "name": [
                    {
                        "family": "DeLarosa",
                        "given": [
                            "Martha"
                        ]
                    }
                ],
                "birthDate": "1972-05-01"
            }
        },
        {
            "fullUrl": "urn:uuid:45271f7f-63ab-4946-970f-3daaaa06637f",
            "resource": {
                "resourceType": "Practitioner",
                "id": "45271f7f-63ab-4946-970f-3daaaa06637f",
                "name": [
                    {
                        "family": "van Hulp",
                        "given": [
                            "Beetje"
                        ]
                    }
                ]
            }
        },
        {
            "fullUrl": "urn:uuid:d179321e-c091-4cd4-8642-3a27537d506d",
            "resource": {
                "resourceType": "Condition",
                "id": "d179321e-c091-4cd4-8642-3a27537d506d",
                "code": {
                    "coding": [
                        {
                            "system": "http://snomed.info/sct",
                            "code": "198436008",
                            "display": "Menopausal flushing (finding)"
                        }
                    ]
                },
                "subject": {
                    "reference": "urn:uuid:244ad7c3-beeb-41d1-8a2f-c76b8cf720ad"
                }
            }
        },
        {
            "fullUrl": "urn:uuid:e1271efd-18ff-4654-9ee7-45f40019c453",
            "resource": {
                "resourceType": "MedicationStatement",
                "id": "e1271efd-18ff-4654-9ee7-45f40019c453",
                "status": "active",
                "medicationReference": {
                    "reference": "urn:uuid:95db7c92-566a-4ded-896b-2220ab244a9e"
                },
                "subject": {
                    "reference": "urn:uuid:244ad7c3-beeb-41d1-8a2f-c76b8cf720ad"
                },
                "effectivePeriod": {
                    "start": "2015-03"
                }
            }
        },
        {
            "fullUrl": "urn:uuid:95db7c92-566a-4ded-896b-2220ab244a9e",
            "resource": {
                "resourceType": "Medication",
                "id": "95db7c92-566a-4ded-896b-2220ab244a9e",
                "code": {
                    "coding": [
                        {
                            "system": "http://snomed.info/sct",
                            "code": "108774000",
                            "display": "Product containing anastrozole (medicinal product)"
                        }
                    ]
                }
            }
        },
        {
            "fullUrl": "urn:uuid:74861316-f69d-4652-9fb1-8512a20c7927",
            "resource": {
                "resourceType": "AllergyIntolerance",
                "id": "74861316-f69d-4652-9fb1-8512a20c7927",
                "clinicalStatus": {
                    "coding": [
                        {
                            "system": "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical",
                            "code": "active"
                        }
                    ]
                },
                "code": {
                    "coding": [
                        {
                            "system": "http://snomed.info/sct",
                            "code": "373270004",
                            "display": "Substance with penicillin structure and antibacterial mechanism of action (substance)"
                        }
                    ]
                },
                "patient": {
                    "reference": "urn:uuid:244ad7c3-beeb-41d1-8a2f-c76b8cf720ad"
                }
            }
        }
    ]
}
```

#### Invalid IPS document: missing medication section

If we remove the medication section and medication entries (which are required by the [IPS Composition profile](https://hl7.org/fhir/uv/ips/2024Sep/StructureDefinition-Composition-uv-ips.html)), we get an error and additional information:

[example](https://test.ahdis.ch/matchboxv3/?resource=ewogICAgInJlc291cmNlVHlwZSI6ICJCdW5kbGUiLAogICAgImlkZW50aWZpZXIiOiB7CiAgICAgICAgInN5c3RlbSI6ICJ1cm46b2lkOjIuMTYuNzI0LjQuOC4xMC4yMDAuMTAiLAogICAgICAgICJ2YWx1ZSI6ICIyOGI5NTgxNS03NmNlLTQ1N2ItYjdhZS1hOTcyZTUyN2RiNDAiCiAgICB9LAogICAgInR5cGUiOiAiZG9jdW1lbnQiLAogICAgInRpbWVzdGFtcCI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICJlbnRyeSI6IFsKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiQ29tcG9zaXRpb24iLAogICAgICAgICAgICAgICAgImlkIjogImY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICAgICAic3RhdHVzIjogImZpbmFsIiwKICAgICAgICAgICAgICAgICJ0eXBlIjogewogICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL2xvaW5jLm9yZyIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICI2MDU5MS01IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlBhdGllbnQgc3VtbWFyeSBEb2N1bWVudCIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiZGF0ZSI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICAgICAgICAgICAgICJhdXRob3IiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjQ1MjcxZjdmLTYzYWItNDk0Ni05NzBmLTNkYWFhYTA2NjM3ZiIKICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAgInRpdGxlIjogIlBhdGllbnQgU3VtbWFyeSBhcyBvZiBEZWNlbWJlciAxMSwgMjAyMCAxNDozMCIsCiAgICAgICAgICAgICAgICAiY29uZmlkZW50aWFsaXR5IjogIk4iLAogICAgICAgICAgICAgICAgInNlY3Rpb24iOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAidGl0bGUiOiAiQWN0aXZlIFByb2JsZW1zIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMTE0NTAtNCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlByb2JsZW0gbGlzdCAtIFJlcG9ydGVkIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI%2BPHVsPjxsaT48ZGl2PjxiPkNvbmRpdGlvbiBOYW1lPC9iPjogTWVub3BhdXNhbCBGbHVzaGluZzwvZGl2PjxkaXY%2BPGI%2BQ29kZTwvYj46IDxzcGFuPjE5ODQzNjAwODwvc3Bhbj48L2Rpdj48ZGl2PjxiPlN0YXR1czwvYj46IDxzcGFuPkFjdGl2ZTwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDpkMTc5MzIxZS1jMDkxLTRjZDQtODY0Mi0zYTI3NTM3ZDUwNmQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgInRpdGxlIjogIkFsbGVyZ2llcyBhbmQgSW50b2xlcmFuY2VzIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiNDg3NjUtMiIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIkFsbGVyZ2llcyBhbmQgYWR2ZXJzZSByZWFjdGlvbnMgRG9jdW1lbnQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48dWw%2BPGxpPjxkaXY%2BPGI%2BQWxsZXJneSBOYW1lPC9iPjogUGVuY2lsbGluczwvZGl2PjxkaXY%2BPGI%2BVmVyaWZpY2F0aW9uIFN0YXR1czwvYj46IENvbmZpcm1lZDwvZGl2PjxkaXY%2BPGI%2BUmVhY3Rpb248L2I%2BOiA8c3Bhbj5ubyBpbmZvcm1hdGlvbjwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDo3NDg2MTMxNi1mNjlkLTQ2NTItOWZiMS04NTEyYTIwYzc5MjciCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKICAgICAgICAgICAgICAgICJpZCI6ICIyNDRhZDdjMy1iZWViLTQxZDEtOGEyZi1jNzZiOGNmNzIwYWQiLAogICAgICAgICAgICAgICAgIm5hbWUiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAiZmFtaWx5IjogIkRlTGFyb3NhIiwKICAgICAgICAgICAgICAgICAgICAgICAgImdpdmVuIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgIk1hcnRoYSIKICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICAiYmlydGhEYXRlIjogIjE5NzItMDUtMDEiCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6NDUyNzFmN2YtNjNhYi00OTQ2LTk3MGYtM2RhYWFhMDY2MzdmIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJQcmFjdGl0aW9uZXIiLAogICAgICAgICAgICAgICAgImlkIjogIjQ1MjcxZjdmLTYzYWItNDk0Ni05NzBmLTNkYWFhYTA2NjM3ZiIsCiAgICAgICAgICAgICAgICAibmFtZSI6IFsKICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICJmYW1pbHkiOiAidmFuIEh1bHAiLAogICAgICAgICAgICAgICAgICAgICAgICAiZ2l2ZW4iOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiQmVldGplIgogICAgICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgXQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmQxNzkzMjFlLWMwOTEtNGNkNC04NjQyLTNhMjc1MzdkNTA2ZCIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiQ29uZGl0aW9uIiwKICAgICAgICAgICAgICAgICJpZCI6ICJkMTc5MzIxZS1jMDkxLTRjZDQtODY0Mi0zYTI3NTM3ZDUwNmQiLAogICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vc25vbWVkLmluZm8vc2N0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogIjE5ODQzNjAwOCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGlzcGxheSI6ICJNZW5vcGF1c2FsIGZsdXNoaW5nIChmaW5kaW5nKSIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfQogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3NDg2MTMxNi1mNjlkLTQ2NTItOWZiMS04NTEyYTIwYzc5MjciLAogICAgICAgICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjogIkFsbGVyZ3lJbnRvbGVyYW5jZSIsCiAgICAgICAgICAgICAgICAiaWQiOiAiNzQ4NjEzMTYtZjY5ZC00NjUyLTlmYjEtODUxMmEyMGM3OTI3IiwKICAgICAgICAgICAgICAgICJjbGluaWNhbFN0YXR1cyI6IHsKICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vYWxsZXJneWludG9sZXJhbmNlLWNsaW5pY2FsIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogImFjdGl2ZSIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMzczMjcwMDA0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlN1YnN0YW5jZSB3aXRoIHBlbmljaWxsaW4gc3RydWN0dXJlIGFuZCBhbnRpYmFjdGVyaWFsIG1lY2hhbmlzbSBvZiBhY3Rpb24gKHN1YnN0YW5jZSkiCiAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgInBhdGllbnQiOiB7CiAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDoyNDRhZDdjMy1iZWViLTQxZDEtOGEyZi1jNzZiOGNmNzIwYWQiCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgIH0KICAgICAgICB9CiAgICBdCn0%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2Fuv%2Fips%2FStructureDefinition%2FBundle-uv-ips#/validate)

>error [structure]: line 1, column 2, in Bundle:
Slice 'Bundle.entry:composition': a matching slice is required, but not found (from http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot). Note that other slices are allowed in addition to this required slice

This error says that the IPS composition could not be found, we have to look for further information why this was not found: 

>information [informational]: line 10, column 10, in Bundle.entry[0]:
>This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
> 1. Bundle.entry[0]: Bundle.entry[0].resource: Composition.section: minimum required = 3, but only found 2 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Composition-uv-ips|2.0.0-ballot)
> 2. Bundle.entry[0]: Bundle.entry[0].resource: Slice 'Composition.section:sectionMedications': a matching slice is required, but not found (from http://hl7.org/fhir/uv/ips/StructureDefinition/Composition-uv-ips|2.0.0-ballot). Note that other slices are allowed in addition to this required slice

The information indicates that the slicing conditions (minimum 3 sections are not met) and that the sectionMedications is required but not found.

#### Invalid IPS document: missing Patient.birthDate

[example](https://test.ahdis.ch/matchboxv3/?resource=ewogICAgInJlc291cmNlVHlwZSI6ICJCdW5kbGUiLAogICAgImlkZW50aWZpZXIiOiB7CiAgICAgICAgInN5c3RlbSI6ICJ1cm46b2lkOjIuMTYuNzI0LjQuOC4xMC4yMDAuMTAiLAogICAgICAgICJ2YWx1ZSI6ICIyOGI5NTgxNS03NmNlLTQ1N2ItYjdhZS1hOTcyZTUyN2RiNDAiCiAgICB9LAogICAgInR5cGUiOiAiZG9jdW1lbnQiLAogICAgInRpbWVzdGFtcCI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICJlbnRyeSI6IFsKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiQ29tcG9zaXRpb24iLAogICAgICAgICAgICAgICAgImlkIjogImY0MGIwN2UzLTM3ZTgtNDhjMy1iZjFjLWFlNzBmZTEyZGFiMCIsCiAgICAgICAgICAgICAgICAic3RhdHVzIjogImZpbmFsIiwKICAgICAgICAgICAgICAgICJ0eXBlIjogewogICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzeXN0ZW0iOiAiaHR0cDovL2xvaW5jLm9yZyIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICI2MDU5MS01IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlBhdGllbnQgc3VtbWFyeSBEb2N1bWVudCIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAic3ViamVjdCI6IHsKICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjI0NGFkN2MzLWJlZWItNDFkMS04YTJmLWM3NmI4Y2Y3MjBhZCIKICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiZGF0ZSI6ICIyMDIwLTEyLTExVDE0OjMwOjAwKzAxOjAwIiwKICAgICAgICAgICAgICAgICJhdXRob3IiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAicmVmZXJlbmNlIjogInVybjp1dWlkOjQ1MjcxZjdmLTYzYWItNDk0Ni05NzBmLTNkYWFhYTA2NjM3ZiIKICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAgInRpdGxlIjogIlBhdGllbnQgU3VtbWFyeSBhcyBvZiBEZWNlbWJlciAxMSwgMjAyMCAxNDozMCIsCiAgICAgICAgICAgICAgICAiY29uZmlkZW50aWFsaXR5IjogIk4iLAogICAgICAgICAgICAgICAgInNlY3Rpb24iOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAidGl0bGUiOiAiQWN0aXZlIFByb2JsZW1zIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMTE0NTAtNCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlByb2JsZW0gbGlzdCAtIFJlcG9ydGVkIgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICAgICAgICAgInRleHQiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3RhdHVzIjogImdlbmVyYXRlZCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGl2IjogIjxkaXYgeG1sbnM9XCJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hodG1sXCI%2BPHVsPjxsaT48ZGl2PjxiPkNvbmRpdGlvbiBOYW1lPC9iPjogTWVub3BhdXNhbCBGbHVzaGluZzwvZGl2PjxkaXY%2BPGI%2BQ29kZTwvYj46IDxzcGFuPjE5ODQzNjAwODwvc3Bhbj48L2Rpdj48ZGl2PjxiPlN0YXR1czwvYj46IDxzcGFuPkFjdGl2ZTwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDpkMTc5MzIxZS1jMDkxLTRjZDQtODY0Mi0zYTI3NTM3ZDUwNmQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgInRpdGxlIjogIk1lZGljYXRpb24iLAogICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RpbmciOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9sb2luYy5vcmciLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZSI6ICIxMDE2MC0wIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImRpc3BsYXkiOiAiSGlzdG9yeSBvZiBNZWRpY2F0aW9uIHVzZSBOYXJyYXRpdmUiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48dWw%2BPGxpPjxkaXY%2BPGI%2BTWVkaWNhdGlvbiBOYW1lPC9iPjogT3JhbCBhbmFzdHJvem9sZSAxbWcgdGFibGV0PC9kaXY%2BPGRpdj48Yj5Db2RlPC9iPjogPHNwYW4%2BPC9zcGFuPjwvZGl2PjxkaXY%2BPGI%2BU3RhdHVzPC9iPjogPHNwYW4%2BQWN0aXZlLCBzdGFydGVkIE1hcmNoIDIwMTU8L3NwYW4%2BPC9kaXY%2BPGRpdj5JbnN0cnVjdGlvbnM6IFRha2UgMSB0aW1lIHBlciBkYXk8L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDplMTI3MWVmZC0xOGZmLTQ2NTQtOWVlNy00NWY0MDAxOWM0NTMiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgInRpdGxlIjogIkFsbGVyZ2llcyBhbmQgSW50b2xlcmFuY2VzIiwKICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vbG9pbmMub3JnIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiNDg3NjUtMiIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIkFsbGVyZ2llcyBhbmQgYWR2ZXJzZSByZWFjdGlvbnMgRG9jdW1lbnQiCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAidGV4dCI6IHsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiZ2VuZXJhdGVkIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXYiOiAiPGRpdiB4bWxucz1cImh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWxcIj48dWw%2BPGxpPjxkaXY%2BPGI%2BQWxsZXJneSBOYW1lPC9iPjogUGVuY2lsbGluczwvZGl2PjxkaXY%2BPGI%2BVmVyaWZpY2F0aW9uIFN0YXR1czwvYj46IENvbmZpcm1lZDwvZGl2PjxkaXY%2BPGI%2BUmVhY3Rpb248L2I%2BOiA8c3Bhbj5ubyBpbmZvcm1hdGlvbjwvc3Bhbj48L2Rpdj48L2xpPjwvdWw%2BPC9kaXY%2BIgogICAgICAgICAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgICAgICAgICAiZW50cnkiOiBbCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDo3NDg2MTMxNi1mNjlkLTQ2NTItOWZiMS04NTEyYTIwYzc5MjciCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJQYXRpZW50IiwKICAgICAgICAgICAgICAgICJpZCI6ICIyNDRhZDdjMy1iZWViLTQxZDEtOGEyZi1jNzZiOGNmNzIwYWQiLAogICAgICAgICAgICAgICAgIm5hbWUiOiBbCiAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAiZmFtaWx5IjogIkRlTGFyb3NhIiwKICAgICAgICAgICAgICAgICAgICAgICAgImdpdmVuIjogWwogICAgICAgICAgICAgICAgICAgICAgICAgICAgIk1hcnRoYSIKICAgICAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgfQogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo0NTI3MWY3Zi02M2FiLTQ5NDYtOTcwZi0zZGFhYWEwNjYzN2YiLAogICAgICAgICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjogIlByYWN0aXRpb25lciIsCiAgICAgICAgICAgICAgICAiaWQiOiAiNDUyNzFmN2YtNjNhYi00OTQ2LTk3MGYtM2RhYWFhMDY2MzdmIiwKICAgICAgICAgICAgICAgICJuYW1lIjogWwogICAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgICAgImZhbWlseSI6ICJ2YW4gSHVscCIsCiAgICAgICAgICAgICAgICAgICAgICAgICJnaXZlbiI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJCZWV0amUiCiAgICAgICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6ZDE3OTMyMWUtYzA5MS00Y2Q0LTg2NDItM2EyNzUzN2Q1MDZkIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJDb25kaXRpb24iLAogICAgICAgICAgICAgICAgImlkIjogImQxNzkzMjFlLWMwOTEtNGNkNC04NjQyLTNhMjc1MzdkNTA2ZCIsCiAgICAgICAgICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMTk4NDM2MDA4IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIk1lbm9wYXVzYWwgZmx1c2hpbmcgKGZpbmRpbmcpIgogICAgICAgICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgICAgICAgXQogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJzdWJqZWN0IjogewogICAgICAgICAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIgogICAgICAgICAgICAgICAgfQogICAgICAgICAgICB9CiAgICAgICAgfSwKICAgICAgICB7CiAgICAgICAgICAgICJmdWxsVXJsIjogInVybjp1dWlkOmUxMjcxZWZkLTE4ZmYtNDY1NC05ZWU3LTQ1ZjQwMDE5YzQ1MyIsCiAgICAgICAgICAgICJyZXNvdXJjZSI6IHsKICAgICAgICAgICAgICAgICJyZXNvdXJjZVR5cGUiOiAiTWVkaWNhdGlvblN0YXRlbWVudCIsCiAgICAgICAgICAgICAgICAiaWQiOiAiZTEyNzFlZmQtMThmZi00NjU0LTllZTctNDVmNDAwMTljNDUzIiwKICAgICAgICAgICAgICAgICJzdGF0dXMiOiAiYWN0aXZlIiwKICAgICAgICAgICAgICAgICJtZWRpY2F0aW9uUmVmZXJlbmNlIjogewogICAgICAgICAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6OTVkYjdjOTItNTY2YS00ZGVkLTg5NmItMjIyMGFiMjQ0YTllIgogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJzdWJqZWN0IjogewogICAgICAgICAgICAgICAgICAgICJyZWZlcmVuY2UiOiAidXJuOnV1aWQ6MjQ0YWQ3YzMtYmVlYi00MWQxLThhMmYtYzc2YjhjZjcyMGFkIgogICAgICAgICAgICAgICAgfSwKICAgICAgICAgICAgICAgICJlZmZlY3RpdmVQZXJpb2QiOiB7CiAgICAgICAgICAgICAgICAgICAgInN0YXJ0IjogIjIwMTUtMDMiCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICAgImZ1bGxVcmwiOiAidXJuOnV1aWQ6OTVkYjdjOTItNTY2YS00ZGVkLTg5NmItMjIyMGFiMjQ0YTllIiwKICAgICAgICAgICAgInJlc291cmNlIjogewogICAgICAgICAgICAgICAgInJlc291cmNlVHlwZSI6ICJNZWRpY2F0aW9uIiwKICAgICAgICAgICAgICAgICJpZCI6ICI5NWRiN2M5Mi01NjZhLTRkZWQtODk2Yi0yMjIwYWIyNDRhOWUiLAogICAgICAgICAgICAgICAgImNvZGUiOiB7CiAgICAgICAgICAgICAgICAgICAgImNvZGluZyI6IFsKICAgICAgICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAgICAgICAgInN5c3RlbSI6ICJodHRwOi8vc25vbWVkLmluZm8vc2N0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogIjEwODc3NDAwMCIsCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGlzcGxheSI6ICJQcm9kdWN0IGNvbnRhaW5pbmcgYW5hc3Ryb3pvbGUgKG1lZGljaW5hbCBwcm9kdWN0KSIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgfQogICAgICAgIH0sCiAgICAgICAgewogICAgICAgICAgICAiZnVsbFVybCI6ICJ1cm46dXVpZDo3NDg2MTMxNi1mNjlkLTQ2NTItOWZiMS04NTEyYTIwYzc5MjciLAogICAgICAgICAgICAicmVzb3VyY2UiOiB7CiAgICAgICAgICAgICAgICAicmVzb3VyY2VUeXBlIjogIkFsbGVyZ3lJbnRvbGVyYW5jZSIsCiAgICAgICAgICAgICAgICAiaWQiOiAiNzQ4NjEzMTYtZjY5ZC00NjUyLTlmYjEtODUxMmEyMGM3OTI3IiwKICAgICAgICAgICAgICAgICJjbGluaWNhbFN0YXR1cyI6IHsKICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly90ZXJtaW5vbG9neS5obDcub3JnL0NvZGVTeXN0ZW0vYWxsZXJneWludG9sZXJhbmNlLWNsaW5pY2FsIiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlIjogImFjdGl2ZSIKICAgICAgICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICAgICAgIF0KICAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICAiY29kZSI6IHsKICAgICAgICAgICAgICAgICAgICAiY29kaW5nIjogWwogICAgICAgICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICAgICAgICAic3lzdGVtIjogImh0dHA6Ly9zbm9tZWQuaW5mby9zY3QiLAogICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGUiOiAiMzczMjcwMDA0IiwKICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkaXNwbGF5IjogIlN1YnN0YW5jZSB3aXRoIHBlbmljaWxsaW4gc3RydWN0dXJlIGFuZCBhbnRpYmFjdGVyaWFsIG1lY2hhbmlzbSBvZiBhY3Rpb24gKHN1YnN0YW5jZSkiCiAgICAgICAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgICAgICBdCiAgICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAgInBhdGllbnQiOiB7CiAgICAgICAgICAgICAgICAgICAgInJlZmVyZW5jZSI6ICJ1cm46dXVpZDoyNDRhZDdjMy1iZWViLTQxZDEtOGEyZi1jNzZiOGNmNzIwYWQiCiAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgIH0KICAgICAgICB9CiAgICBdCn0%3D&profile=http%3A%2F%2Fhl7.org%2Ffhir%2Fuv%2Fips%2FStructureDefinition%2FBundle-uv-ips#/validate)

If we remove the patient birhtDate (which is required by the [IPS Patient profile](https://hl7.org/fhir/uv/ips/2024Sep/StructureDefinition-Patient-uv-ips.html)), we get two errors and a huge list of information issues:

> error [structure]: line 1, column 2, in Bundle:
Slice 'Bundle.entry:composition': a matching slice is required, but not found (from http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot). Note that other slices are allowed in addition to this required slice

This error says that the IPS composition could not be found, we have to look for further information why this was not found. 

>error [structure]: line 1, column 2, in Bundle:
Slice 'Bundle.entry:patient': a matching slice is required, but not found (from http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot). Note that other slices are allowed in addition to this required slice

This indicates that the validator did not find a corresponding patient but we have to look further why this is not the case.

>information [informational]: line 10, column 10, in Bundle.entry[0]:
This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
>1. Bundle.entry[0]: Bundle.entry[0].resource.subject: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)
>2. Bundle.entry[0]: Bundle.entry[0].resource.section[0].entry[0]: Bundle.entry[0].resource.section[0].entry[0]: Bundle.entry[3].resource.subject: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)
>3. Bundle.entry[0]: Bundle.entry[0].resource.section[1].entry[0]: Bundle.entry[0].resource.section[1].entry[0]: Bundle.entry[4].resource.subject: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)
>4. Bundle.entry[0]: Bundle.entry[0].resource.section[2].entry[0]: Bundle.entry[0].resource.section[2].entry[0]: Bundle.entry[6].resource.patient: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)

Here we see why the Composition did not match the slice, Composition.subject points to Patient, where the birthDate is not provided, in additions also in the referenced resources out of the sections have references to the patient which is not conforming to the IPS Patient profile.

>information [informational]: line 103, column 10, in Bundle.entry[1]:
>This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
>1. Bundle.entry[1]: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)

Here we see that the Patient would need to have birthDate that is missing to conform to the profile.

>information [informational]: line 133, column 10, in Bundle.entry[3]:
>This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
>1. Bundle.entry[3]: Bundle.entry[3].resource.subject: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)

>information [informational]: line 152, column 10, in Bundle.entry[4]:
>This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
>1. Bundle.entry[4]: Bundle.entry[4].resource.subject: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)

>information [informational]: line 185, column 10, in Bundle.entry[6]:
>This element does not match any known slice defined in the profile http://hl7.org/fhir/uv/ips/StructureDefinition/Bundle-uv-ips|2.0.0-ballot (this may not be a problem, but you should check that it's not intended to match a slice)
> 1. Bundle.entry[6]: Bundle.entry[6].resource.patient: Bundle.entry[1].resource/*Patient/244ad7c3-beeb-41d1-8a2f-c76b8cf720ad*/: Patient.birthDate: minimum required = 1, but only found 0 (from http://hl7.org/fhir/uv/ips/StructureDefinition/Patient-uv-ips|2.0.0-ballot)

The validator can us not tell directly that Patient.birthDate is missing, it could have other Patient in the Bundle. The validator tells us that a Composition and a Patient according to the IPS definitions could not be found and we have to figure out ourself why they did not match.