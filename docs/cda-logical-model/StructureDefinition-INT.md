# INT: IntegerNumber (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **INT: IntegerNumber (V3 Data Type)**

## Logical Model: INT: IntegerNumber (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/INT | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:INT |

 
Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable. No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity. 

**Usages:**

* Derived from this Logical Model: [INT_POS: Positive integer numbers (V3 Data Type)](StructureDefinition-INT-POS.md) and [IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type)](StructureDefinition-IVXB-INT.md)
* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md), [ExternalDocument (CDA Class)](StructureDefinition-ExternalDocument.md)...Show 7 more,[IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md),[ParentDocument (CDA Class)](StructureDefinition-ParentDocument.md)and[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/INT)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-INT.csv), [Excel](StructureDefinition-INT.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "INT",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/INT",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "INT",
  "title" : "INT: IntegerNumber (V3 Data Type)",
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
  "description" : "Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable. No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/INT",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "INT",
        "path" : "INT",
        "definition" : "Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable. No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity.",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "value-null",
            "severity" : "error",
            "human" : "value and nullFlavor are mutually exclusive (one must be present)",
            "expression" : "(value | nullFlavor).count() = 1"
          }
        ]
      },
      {
        "id" : "INT.value",
        "path" : "INT.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "integer",
            "profile" : [
              "http://hl7.org/cda/stds/core/StructureDefinition/int-simple"
            ]
          }
        ]
      }
    ]
  }
}

```
