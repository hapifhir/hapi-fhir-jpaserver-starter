# IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type)**

## Logical Model: IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/IVXB-INT | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:IVXB_INT |

 
An integer interval boundary containing an inclusive/exclusive flag. 

**Usages:**

* Use this Logical Model: [IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/IVXB-INT)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-IVXB-INT.csv), [Excel](StructureDefinition-IVXB-INT.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "IVXB-INT",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB-INT",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "IVXB_INT",
  "title" : "IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type)",
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
  "description" : "An integer interval boundary containing an inclusive/exclusive flag.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB_INT",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/INT",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "IVXB_INT",
        "path" : "IVXB_INT",
        "definition" : "An integer interval boundary containing an inclusive/exclusive flag.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "IVXB_INT.inclusive",
        "path" : "IVXB_INT.inclusive",
        "representation" : ["xmlAttr"],
        "definition" : "Specifies whether the limit is included in the interval (interval is closed) or excluded from the interval (interval is open).",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ],
        "defaultValueBoolean" : true
      }
    ]
  }
}

```
