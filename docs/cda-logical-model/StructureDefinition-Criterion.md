# Criterion (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Criterion (CDA Class)**

## Logical Model: Criterion (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Criterion | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Criterion |

 
Criterion (CDA Class) 

**Usages:**

* Use this Logical Model: [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md), [Precondition (CDA Class)](StructureDefinition-Precondition.md) and [Precondition2 (CDA Class)](StructureDefinition-Precondition2.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Criterion)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Criterion.csv), [Excel](StructureDefinition-Criterion.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Criterion",
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
      "valueString" : "criterion"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Criterion",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Criterion",
  "title" : "Criterion (CDA Class)",
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
  "description" : "Criterion (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Criterion",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Criterion",
        "path" : "Criterion",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Criterion.classCode",
        "path" : "Criterion.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "OBS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation"
        }
      },
      {
        "id" : "Criterion.moodCode",
        "path" : "Criterion.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "EVN.CRT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActMoodPredicate"
        }
      },
      {
        "id" : "Criterion.code",
        "path" : "Criterion.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActCode"
        }
      },
      {
        "id" : "Criterion.text",
        "path" : "Criterion.text",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "Criterion.value",
        "path" : "Criterion.value",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CV"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SC"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/MO"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ]
      }
    ]
  }
}

```
