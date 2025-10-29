# PQ: PhysicalQuantity (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PQ: PhysicalQuantity (V3 Data Type)**

## Logical Model: PQ: PhysicalQuantity (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PQ | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:PQ |

 
A dimensioned quantity expressing the result of measuring. 

**Usages:**

* Derived from this Logical Model: [IVL_PQ: Interval (V3 Data Type)](StructureDefinition-IVL-PQ.md) and [IVXB_PQ: Interval Boundary PhysicalQuantity (V3 Data Type)](StructureDefinition-IVXB-PQ.md)
* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [IVL_PQ: Interval (V3 Data Type)](StructureDefinition-IVL-PQ.md), [IVL_TS: Interval (V3 Data Type)](StructureDefinition-IVL-TS.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md)...Show 7 more,[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)](StructureDefinition-PIVL-TS.md),[PharmPackagedMedicine (CDA Pharm Class)](StructureDefinition-PharmPackagedMedicine.md),[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md),[RTO_PQ_PQ: Ratio (V3 Data Type)](StructureDefinition-RTO-PQ-PQ.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PQ)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PQ.csv), [Excel](StructureDefinition-PQ.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PQ",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "PQ",
  "title" : "PQ: PhysicalQuantity (V3 Data Type)",
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
  "description" : "A dimensioned quantity expressing the result of measuring.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PQ",
        "path" : "PQ",
        "definition" : "A dimensioned quantity expressing the result of a measurement act.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "PQ.unit",
        "path" : "PQ.unit",
        "representation" : ["xmlAttr"],
        "label" : "Unit of Measure",
        "definition" : "The unit of measure specified in the Unified Code for Units of Measure (UCUM) [].",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "1"
      },
      {
        "id" : "PQ.value",
        "path" : "PQ.value",
        "representation" : ["xmlAttr"],
        "label" : "Maginitude Value",
        "definition" : "The magnitude of the quantity measured in terms of the unit.",
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
      },
      {
        "id" : "PQ.translation",
        "path" : "PQ.translation",
        "label" : "Translation",
        "definition" : "An alternative representation of the same physical quantity expressed in a different unit, of a different unit code system and possibly with a different value.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQR"
          }
        ]
      }
    ]
  }
}

```
