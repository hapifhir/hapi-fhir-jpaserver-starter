# IVL_PQ: Interval (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **IVL_PQ: Interval (V3 Data Type)**

## Logical Model: IVL_PQ: Interval (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:IVL_PQ |

 
A set of consecutive values of an ordered base data type. 

**Usages:**

* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)](StructureDefinition-EIVL-TS.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md)...Show 2 more,[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)and[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/IVL-PQ)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-IVL-PQ.csv), [Excel](StructureDefinition-IVL-PQ.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "IVL-PQ",
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
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-binding-style",
      "valueCode" : "CDA"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "IVL_PQ",
  "title" : "IVL_PQ: Interval (V3 Data Type)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL_PQ",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "IVL_PQ",
        "path" : "IVL_PQ",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "ivl-pq-center",
            "severity" : "error",
            "human" : "Center cannot co-exist with low or high",
            "expression" : "center.empty() or (low.empty() and high.empty())"
          }
        ]
      },
      {
        "id" : "IVL_PQ.operator",
        "path" : "IVL_PQ.operator",
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
        "id" : "IVL_PQ.low",
        "path" : "IVL_PQ.low",
        "label" : "Low Boundary",
        "definition" : "This is the low limit of the interval.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB-PQ"
          }
        ]
      },
      {
        "id" : "IVL_PQ.center",
        "path" : "IVL_PQ.center",
        "label" : "Central Value",
        "definition" : "The arithmetic mean of the interval (low plus high divided by 2). The purpose of distinguishing the center as a semantic property is for conversions of intervals from and to point values.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "IVL_PQ.width",
        "path" : "IVL_PQ.width",
        "label" : "Width",
        "definition" : "The difference between high and low boundary. The purpose of distinguishing a width property is to handle all cases of incomplete information symmetrically. In any interval representation only two of the three properties high, low, and width need to be stated and the third can be derived.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "IVL_PQ.high",
        "path" : "IVL_PQ.high",
        "label" : "High Boundary",
        "definition" : "This is the high limit of the interval.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVXB-PQ"
          }
        ]
      }
    ]
  }
}

```
