# QTY: Quantity (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **QTY: Quantity (V3 Data Type)**

## Logical Model: QTY: Quantity (V3 Data Type) ( Abstract ) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/QTY | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:QTY |

 
The quantity data type is an abstract generalization for all data types (1) whose value set has an order relation (less-or-equal) and (2) where difference is defined in all of the data type's totally ordered value subsets. The quantity type abstraction is needed in defining certain other types, such as the interval and the probability distribution. 

**Usages:**

* Derived from this Logical Model: [INT: IntegerNumber (V3 Data Type)](StructureDefinition-INT.md), [MO: MonetaryAmount (V3 Data Type)](StructureDefinition-MO.md), [PQ: PhysicalQuantity (V3 Data Type)](StructureDefinition-PQ.md), [REAL: RealNumber (V3 Data Type)](StructureDefinition-REAL.md)...Show 2 more,[RTO_PQ_PQ: Ratio (V3 Data Type)](StructureDefinition-RTO-PQ-PQ.md)and[TS: PointInTime (V3 Data Type)](StructureDefinition-TS.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/QTY)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-QTY.csv), [Excel](StructureDefinition-QTY.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "QTY",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "QTY",
  "title" : "QTY: Quantity (V3 Data Type)",
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
  "description" : "The quantity data type is an abstract generalization for all data types (1) whose value set has an order relation (less-or-equal) and (2) where difference is defined in all of the data type's totally ordered value subsets. The quantity type abstraction is needed in defining certain other types, such as the interval and the probability distribution.",
  "fhirVersion" : "5.0.0",
  "mapping" : [
    {
      "identity" : "rim",
      "uri" : "http://hl7.org/v3",
      "name" : "RIM Mapping"
    }
  ],
  "kind" : "logical",
  "abstract" : true,
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "QTY",
        "path" : "QTY",
        "definition" : "is an abstract generalization for all data types (1) whose value set has an order relation (less-or-equal) and (2) where difference is defined in all of the data type's totally ordered value subsets. The quantity type abstraction is needed in defining certain other types, such as the interval and the probability distribution.",
        "min" : 1,
        "max" : "*"
      }
    ]
  }
}

```
