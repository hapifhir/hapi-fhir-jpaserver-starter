# REAL: RealNumber (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **REAL: RealNumber (V3 Data Type)**

## Logical Model: REAL: RealNumber (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/REAL | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:REAL |

 
Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers. The typical representation is decimal, where the number of significant decimal digits is known as the precision. 

**Usages:**

* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md) and [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/REAL)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-REAL.csv), [Excel](StructureDefinition-REAL.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "REAL",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "REAL",
  "title" : "REAL: RealNumber (V3 Data Type)",
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
  "description" : "Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers. The typical representation is decimal, where the number of significant decimal digits is known as the precision.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "REAL",
        "path" : "REAL",
        "definition" : "Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers. The typical representation is decimal, where the number of significant decimal digits is known as the precision. Real numbers are needed beyond integers whenever quantities of the real world are measured, estimated, or computed from other real numbers. The term \"Real number\" in this specification is used to mean that fractional values are covered without necessarily implying the full set of the mathematical real numbers.",
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
        "id" : "REAL.value",
        "path" : "REAL.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "decimal",
            "profile" : [
              "http://hl7.org/cda/stds/core/StructureDefinition/real-simple"
            ]
          }
        ]
      }
    ]
  }
}

```
