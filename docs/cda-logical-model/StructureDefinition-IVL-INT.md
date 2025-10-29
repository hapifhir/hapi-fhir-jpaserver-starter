# IVL_INT: Interval (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **IVL_INT: Interval (V3 Data Type)**

## Logical Model: IVL_INT: Interval (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:IVL_INT |

 
A set of consecutive values of an ordered base data type. 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md), [SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md) and [Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/IVL-INT)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-IVL-INT.csv), [Excel](StructureDefinition-IVL-INT.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "IVL-INT",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "IVL_INT",
  "title" : "IVL_INT: Interval (V3 Data Type)",
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
  "description" : "A set of consecutive values of an ordered base data type.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL_INT",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "IVL_INT",
        "path" : "IVL_INT",
        "definition" : "Interval of integers",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "ivl-int-center",
            "severity" : "error",
            "human" : "Center cannot co-exist with low or high",
            "expression" : "center.empty() or (low.empty() and high.empty())"
          }
        ]
      },
      {
        "id" : "IVL_INT.value",
        "path" : "IVL_INT.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "base" : {
          "path" : "IVL_INT.value",
          "min" : 0,
          "max" : "1"
        },
        "type" : [
          {
            "code" : "integer",
            "profile" : [
              "http://hl7.org/cda/stds/core/StructureDefinition/int-simple"
            ]
          }
        ]
      },
      {
        "id" : "IVL_INT.operator",
        "path" : "IVL_INT.operator",
        "representation" : ["xmlAttr"],
        "definition" : "A code specifying whether the set component is included (union) or excluded (set-difference) from the set, or other set operations with the current set component and the set as constructed from the representation stream up to the current point.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "I",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDASetOperator"
        }
      },
      {
        "id" : "IVL_INT.low",
        "path" : "IVL_INT.low",
        "label" : "Low Boundary",
        "definition" : "This is the low limit of the interval.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB-INT"
          }
        ]
      },
      {
        "id" : "IVL_INT.center",
        "path" : "IVL_INT.center",
        "label" : "Central Value",
        "definition" : "The arithmetic mean of the interval (low plus high divided by 2). The purpose of distinguishing the center as a semantic property is for conversions of intervals from and to point values.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "IVL_INT.width",
        "path" : "IVL_INT.width",
        "label" : "Width",
        "definition" : "The difference between high and low boundary. The purpose of distinguishing a width property is to handle all cases of incomplete information symmetrically. In any interval representation only two of the three properties high, low, and width need to be stated and the third can be derived.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "IVL_INT.high",
        "path" : "IVL_INT.high",
        "label" : "High Boundary",
        "definition" : "This is the high limit of the interval.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB-INT"
          }
        ]
      }
    ]
  }
}

```
