# BL: Boolean (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **BL: Boolean (V3 Data Type)**

## Logical Model: BL: Boolean (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/BL | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:BL |

 
The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false, or, as any other value may be NULL. 

**Usages:**

* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [LanguageCommunication (CDA Class)](StructureDefinition-LanguageCommunication.md)...Show 7 more,[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[Reference (CDA Class)](StructureDefinition-Reference.md),[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/BL)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-BL.csv), [Excel](StructureDefinition-BL.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "BL",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/BL",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "BL",
  "title" : "BL: Boolean (V3 Data Type)",
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
  "description" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false, or, as any other value may be NULL.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/BL",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "BL",
        "path" : "BL",
        "definition" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false, or, as any other value may be NULL.",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "value-null",
            "severity" : "error",
            "human" : "value and nullFlavor are mutually exclusive (one must be present)",
            "expression" : "(value | nullFlavor).count() = 1"
          }
        ]
      },
      {
        "id" : "BL.value",
        "path" : "BL.value",
        "representation" : ["xmlAttr"],
        "definition" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false, or, as any other value may be NULL.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      }
    ]
  }
}

```
