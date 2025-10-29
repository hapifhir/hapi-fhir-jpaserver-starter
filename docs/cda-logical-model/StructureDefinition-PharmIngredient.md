# PharmIngredient (CDA Pharm Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PharmIngredient (CDA Pharm Class)**

## Logical Model: PharmIngredient (CDA Pharm Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PharmIngredient | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CDAR2.PharmIngredient |

**Usages:**

* Use this Logical Model: [Material (CDA Class)](StructureDefinition-Material.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PharmIngredient)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PharmIngredient.csv), [Excel](StructureDefinition-PharmIngredient.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PharmIngredient",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:ihe:pharm"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmIngredient",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAR2.PharmIngredient",
  "title" : "PharmIngredient (CDA Pharm Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmIngredient",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PharmIngredient",
        "path" : "PharmIngredient",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "PharmIngredient.classCode",
        "path" : "PharmIngredient.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "ACTI",
        "fixedCode" : "ACTI",
        "mustSupport" : true
      },
      {
        "id" : "PharmIngredient.quantity",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmIngredient.quantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RTO-PQ-PQ"
          }
        ]
      },
      {
        "id" : "PharmIngredient.ingredient",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmIngredient.ingredient",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmSubstance"
          }
        ]
      }
    ]
  }
}

```
