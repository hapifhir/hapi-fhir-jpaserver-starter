# ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type)**

## Logical Model: ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ST-dot-r2b | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ST_dot_r2b |

 
A character string data type variant that uses a value attribute rather than element content. This datatype is primarily used to handle version numbers and other string values in CDA documents where the data is stored as an XML attribute. 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ST-dot-r2b)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ST-dot-r2b.csv), [Excel](StructureDefinition-ST-dot-r2b.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ST-dot-r2b",
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
      "valueString" : "ST.r2b"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ST-dot-r2b",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ST_dot_r2b",
  "title" : "ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type)",
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
  "description" : "A character string data type variant that uses a value attribute rather than element content. This datatype is primarily used to handle version numbers and other string values in CDA documents where the data is stored as an XML attribute.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ST_dot_r2b",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ST_dot_r2b",
        "path" : "ST_dot_r2b",
        "definition" : "A character string data type variant that uses a value attribute rather than element content.",
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
        "id" : "ST_dot_r2b.value",
        "path" : "ST_dot_r2b.value",
        "representation" : ["xmlAttr"],
        "label" : "Value",
        "definition" : "The string value stored as an XML attribute rather than element content.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      }
    ]
  }
}

```
