# PQR: PhysicalQuantityRepresentation (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PQR: PhysicalQuantityRepresentation (V3 Data Type)**

## Logical Model: PQR: PhysicalQuantityRepresentation (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PQR | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:PQR |

 
An extension of the coded value data type representating a physical quantity using a unit from any code system. Used to show alternative representation for a physical quantity. 

**Usages:**

* Use this Logical Model: [PQ: PhysicalQuantity (V3 Data Type)](StructureDefinition-PQ.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PQR)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PQR.csv), [Excel](StructureDefinition-PQR.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PQR",
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
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-binding-style",
      "valueCode" : "CDA"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PQR",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "PQR",
  "title" : "PQR: PhysicalQuantityRepresentation (V3 Data Type)",
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
  "description" : "An extension of the coded value data type representating a physical quantity using a unit from any code system. Used to show alternative representation for a physical quantity.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PQR",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/CV",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PQR",
        "path" : "PQR",
        "definition" : "A representation of a physical quantity in a unit from any code system. Used to show alternative representation for a physical quantity.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "PQR.value",
        "path" : "PQR.value",
        "representation" : ["xmlAttr"],
        "label" : "Value",
        "definition" : "The magnitude of the measurement value in terms of the unit specified by this code.",
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
