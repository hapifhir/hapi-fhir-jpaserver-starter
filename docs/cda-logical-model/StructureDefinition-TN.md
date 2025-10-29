# TN: TrivialName (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **TN: TrivialName (V3 Data Type)**

## Logical Model: TN: TrivialName (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/TN | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:TN |

 
A restriction of entity name that is effectively a simple string used for a simple name for things and places. 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md) and [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/TN)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-TN.csv), [Excel](StructureDefinition-TN.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "TN",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/TN",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "TN",
  "title" : "TN: TrivialName (V3 Data Type)",
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
  "description" : "A restriction of entity name that is effectively a simple string used for a simple name for things and places.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/TN",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/EN",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "TN",
        "path" : "TN",
        "definition" : "A restriction of entity name that is effectively a simple string used for a simple name for things and places.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "TN.use",
        "path" : "TN.use",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAEntityNameUse"
        }
      },
      {
        "id" : "TN.item.delimiter",
        "path" : "TN.item.delimiter",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "TN.item.family",
        "path" : "TN.item.family",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "TN.item.given",
        "path" : "TN.item.given",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "TN.item.prefix",
        "path" : "TN.item.prefix",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "TN.item.suffix",
        "path" : "TN.item.suffix",
        "min" : 0,
        "max" : "0"
      }
    ]
  }
}

```
