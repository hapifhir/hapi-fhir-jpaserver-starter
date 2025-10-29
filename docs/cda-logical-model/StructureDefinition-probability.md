# probability: Probability - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **probability: Probability**

## Data Type Profile: probability: Probability 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/probability | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:probability |

 
The probability assigned to the value, a decimal number between 0 (very uncertain) and 1 (certain). 

**Usages:**

* This Primitive Type Profile is not used by any profiles in this Implementation Guide

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/probability)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-probability.csv), [Excel](StructureDefinition-probability.xlsx), [Schematron](StructureDefinition-probability.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "probability",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "has-range"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/probability",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "probability",
  "title" : "probability: Probability",
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
  "description" : "The probability assigned to the value, a decimal number between 0 (very uncertain) and 1 (certain).",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "decimal",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/decimal",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "decimal",
        "path" : "decimal"
      },
      {
        "id" : "decimal.id",
        "path" : "decimal.id",
        "max" : "0"
      },
      {
        "id" : "decimal.extension",
        "path" : "decimal.extension",
        "max" : "0"
      },
      {
        "id" : "decimal.value",
        "path" : "decimal.value",
        "minValueDecimal" : 0.0,
        "maxValueDecimal" : 1.0
      }
    ]
  }
}

```
