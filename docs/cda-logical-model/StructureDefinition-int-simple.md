# int: Integer Number - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **int: Integer Number**

## Data Type Profile: int: Integer Number 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/int-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:int |

 
Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable. No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity. 

**Usages:**

* Use this Primitive Type Profile: [INT_POS: Positive integer numbers (V3 Data Type)](StructureDefinition-INT-POS.md), [INT: IntegerNumber (V3 Data Type)](StructureDefinition-INT.md) and [IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/int-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-int-simple.csv), [Excel](StructureDefinition-int-simple.xlsx), [Schematron](StructureDefinition-int-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "int-simple",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/int-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "int",
  "title" : "int: Integer Number",
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
  "description" : "Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable.  No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "integer",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/integer",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "integer.id",
        "path" : "integer.id",
        "max" : "0"
      },
      {
        "id" : "integer.extension",
        "path" : "integer.extension",
        "max" : "0"
      }
    ]
  }
}

```
