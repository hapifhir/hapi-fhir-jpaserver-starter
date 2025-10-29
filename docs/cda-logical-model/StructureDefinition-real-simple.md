# real: Real Number - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **real: Real Number**

## Data Type Profile: real: Real Number 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/real-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:real |

 
Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers. The typical representation is decimal, where the number of significant decimal digits is known as the precision. Real numbers are needed beyond integers whenever quantities of the real world are measured, estimated, or computed from other real numbers. The term "Real number" in this specification is used to mean that fractional values are covered without necessarily implying the full set of the mathematical real numbers. 

**Usages:**

* Use this Primitive Type Profile: [MO: MonetaryAmount (V3 Data Type)](StructureDefinition-MO.md), [PQ: PhysicalQuantity (V3 Data Type)](StructureDefinition-PQ.md), [PQR: PhysicalQuantityRepresentation (V3 Data Type)](StructureDefinition-PQR.md) and [REAL: RealNumber (V3 Data Type)](StructureDefinition-REAL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/real-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-real-simple.csv), [Excel](StructureDefinition-real-simple.xlsx), [Schematron](StructureDefinition-real-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "real-simple",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/real-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "real",
  "title" : "real: Real Number",
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
  "description" : "Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers.  The typical representation is decimal, where the number of significant decimal digits is known as the precision. Real numbers are needed beyond integers whenever quantities of the real world are measured, estimated, or computed from other real numbers. The term \"Real number\" in this specification is used to mean that fractional values are covered without necessarily implying the full set of the mathematical real numbers.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "decimal",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/decimal",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "decimal.id",
        "path" : "decimal.id",
        "max" : "0"
      },
      {
        "id" : "decimal.extension",
        "path" : "decimal.extension",
        "max" : "0"
      }
    ]
  }
}

```
