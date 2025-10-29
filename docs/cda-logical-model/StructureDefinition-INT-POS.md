# INT_POS: Positive integer numbers (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **INT_POS: Positive integer numbers (V3 Data Type)**

## Logical Model: INT_POS: Positive integer numbers (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/INT-POS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:INT_POS |

 
Positive integer numbers. 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md), [Patient (CDA Class)](StructureDefinition-Patient.md) and [SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/INT-POS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-INT-POS.csv), [Excel](StructureDefinition-INT-POS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "INT-POS",
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
      "valueUri" : "urn:hl7-org:sdtc"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/INT-POS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "INT_POS",
  "title" : "INT_POS: Positive integer numbers (V3 Data Type)",
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
  "description" : "Positive integer numbers.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/INT_POS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/INT",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "INT_POS",
        "path" : "INT_POS",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "INT_POS.value",
        "path" : "INT_POS.value",
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
        ],
        "minValueInteger" : 1
      }
    ]
  }
}

```
