# MO: MonetaryAmount (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **MO: MonetaryAmount (V3 Data Type)**

## Logical Model: MO: MonetaryAmount (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/MO | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:MO |

 
A monetary amount is a quantity expressing the amount of money in some currency. Currencies are the units in which monetary amounts are denominated in different economic regions. While the monetary amount is a single kind of quantity (money) the exchange rates between the different units are variable. This is the principle difference between physical quantity and monetary amounts, and the reason why currency units are not physical units. 

**Usages:**

* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md) and [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/MO)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-MO.csv), [Excel](StructureDefinition-MO.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "MO",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/MO",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "MO",
  "title" : "MO: MonetaryAmount (V3 Data Type)",
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
  "description" : "A monetary amount is a quantity expressing the amount of money in some currency. Currencies are the units in which monetary amounts are denominated in different economic regions. While the monetary amount is a single kind of quantity (money) the exchange rates between the different units are variable. This is the principle difference between physical quantity and monetary amounts, and the reason why currency units are not physical units.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/MO",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "MO",
        "path" : "MO",
        "definition" : "A monetary amount is a quantity expressing the amount of money in some currency. Currencies are the units in which monetary amounts are denominated in different economic regions. While the monetary amount is a single kind of quantity (money) the exchange rates between the different units are variable. This is the principle difference between physical quantity and monetary amounts, and the reason why currency units are not physical units.",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "value-null",
            "severity" : "error",
            "human" : "value and nullFlavor are mutually exclusive (one must be present)",
            "expression" : "nullFlavor.exists() implies (value | currency).empty()"
          }
        ]
      },
      {
        "id" : "MO.currency",
        "path" : "MO.currency",
        "representation" : ["xmlAttr"],
        "label" : "Currency",
        "definition" : "The currency unit as defined in ISO 4217.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ]
      },
      {
        "id" : "MO.value",
        "path" : "MO.value",
        "representation" : ["xmlAttr"],
        "label" : "Value",
        "definition" : "The magnitude of the monetary amount in terms of the currency unit.",
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
