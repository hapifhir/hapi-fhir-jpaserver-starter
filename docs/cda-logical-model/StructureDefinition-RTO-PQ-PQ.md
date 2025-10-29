# RTO_PQ_PQ: Ratio (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **RTO_PQ_PQ: Ratio (V3 Data Type)**

## Logical Model: RTO_PQ_PQ: Ratio (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/RTO-PQ-PQ | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:RTO_PQ_PQ |

 
A quantity constructed as the quotient of a numerator quantity divided by a denominator quantity. Common factors in the numerator and denominator are not automatically cancelled out. The data type supports titers (e.g., "1:128") and other quantities produced by laboratories that truly represent ratios. Ratios are not simply "structured numerics", particularly blood pressure measurements (e.g. "120/60") are not ratios. In many cases the should be used instead of the . 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md), [PharmIngredient (CDA Pharm Class)](StructureDefinition-PharmIngredient.md) and [SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/RTO-PQ-PQ)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-RTO-PQ-PQ.csv), [Excel](StructureDefinition-RTO-PQ-PQ.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "RTO-PQ-PQ",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/RTO-PQ-PQ",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "RTO_PQ_PQ",
  "title" : "RTO_PQ_PQ: Ratio (V3 Data Type)",
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
  "description" : "A quantity constructed as the quotient of a numerator quantity divided by a denominator quantity. Common factors in the numerator and denominator are not automatically cancelled out. The data type supports titers (e.g., \"1:128\") and other quantities produced by laboratories that truly represent ratios. Ratios are not simply \"structured numerics\", particularly blood pressure measurements (e.g. \"120/60\") are not ratios. In many cases the should be used instead of the .",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/RTO_PQ_PQ",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "RTO_PQ_PQ",
        "path" : "RTO_PQ_PQ",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "RTO_PQ_PQ.numerator",
        "path" : "RTO_PQ_PQ.numerator",
        "label" : "Numerator",
        "definition" : "The quantity that is being divided in the ratio. The default is the integer number 1 (one.)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "RTO_PQ_PQ.denominator",
        "path" : "RTO_PQ_PQ.denominator",
        "label" : "Denominator",
        "definition" : "The quantity that devides the numerator in the ratio. The default is the integer number 1 (one.) The denominator must not be zero.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      }
    ]
  }
}

```
