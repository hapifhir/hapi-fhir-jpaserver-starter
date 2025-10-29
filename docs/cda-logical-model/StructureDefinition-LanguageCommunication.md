# LanguageCommunication (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **LanguageCommunication (CDA Class)**

## Logical Model: LanguageCommunication (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/LanguageCommunication | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:LanguageCommunication |

 
The language communication capabilities of an Entity. 
While it may seem on the surface that this class would be restricted in usage to only the LivingSubject subtypes, Devices also have the ability to communicate, such as automated telephony devices that transmit patient information to live operators on a triage line or provide automated laboratory results to clinicians. 
A patient who originally came from Mexico may have fluent language capabilities to speak, read and write in Spanish, and rudimentary capabilities in English. A person from Russia may have the capability to communicate equally well in spoken language in Russian, Armenian or Ukrainian, and a preference to speak in Armenian. 

**Usages:**

* Use this Logical Model: [Patient (CDA Class)](StructureDefinition-Patient.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/LanguageCommunication)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-LanguageCommunication.csv), [Excel](StructureDefinition-LanguageCommunication.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "LanguageCommunication",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-target",
      "_valueBoolean" : {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
            "valueCode" : "not-applicable"
          }
        ]
      }
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "languageCommunication"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/type-profile-style",
      "valueCode" : "cda"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/LanguageCommunication",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "LanguageCommunication",
  "title" : "LanguageCommunication (CDA Class)",
  "status" : "active",
  "experimental" : false,
  "date" : "2025-10-29T22:15:57+01:00",
  "publisher" : "Health Level 7",
  "contact" : [
    {
      "name" : "HL7 International - Structured Documents",
      "telecom" : [
        {
          "system" : "url",
          "value" : "http://www.hl7.org/Special/committees/structure"
        },
        {
          "system" : "email",
          "value" : "structdog@lists.HL7.org"
        }
      ]
    }
  ],
  "description" : "The language communication capabilities of an Entity.\n\nWhile it may seem on the surface that this class would be restricted in usage to only the LivingSubject subtypes, Devices also have the ability to communicate, such as automated telephony devices that transmit patient information to live operators on a triage line or provide automated laboratory results to clinicians.\n\nA patient who originally came from Mexico may have fluent language capabilities to speak, read and write in Spanish, and rudimentary capabilities in English. A person from Russia may have the capability to communicate equally well in spoken language in Russian, Armenian or Ukrainian, and a preference to speak in Armenian.",
  "fhirVersion" : "5.0.0",
  "mapping" : [
    {
      "identity" : "rim",
      "uri" : "http://hl7.org/v3",
      "name" : "RIM Mapping"
    }
  ],
  "kind" : "logical",
  "abstract" : false,
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/LanguageCommunication",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "LanguageCommunication",
        "path" : "LanguageCommunication",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "LanguageCommunication.languageCode",
        "path" : "LanguageCommunication.languageCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/fhir/ValueSet/all-languages"
        }
      },
      {
        "id" : "LanguageCommunication.modeCode",
        "path" : "LanguageCommunication.modeCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-LanguageAbilityMode"
        }
      },
      {
        "id" : "LanguageCommunication.proficiencyLevelCode",
        "path" : "LanguageCommunication.proficiencyLevelCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-LanguageAbilityProficiency"
        }
      },
      {
        "id" : "LanguageCommunication.preferenceInd",
        "path" : "LanguageCommunication.preferenceInd",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          }
        ]
      }
    ]
  }
}

```
