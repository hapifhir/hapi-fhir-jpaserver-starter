# ObservationRange (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ObservationRange (CDA Class)**

## Logical Model: ObservationRange (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ObservationRange | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ObservationRange |

 
ObservationRange (CDA Class) 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ObservationRange)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ObservationRange.csv), [Excel](StructureDefinition-ObservationRange.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ObservationRange",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "observationRange"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/type-profile-style",
      "valueCode" : "cda"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationRange",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ObservationRange",
  "title" : "ObservationRange (CDA Class)",
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
  "description" : "ObservationRange (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationRange",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ObservationRange",
        "path" : "ObservationRange",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "ObservationRange.classCode",
        "path" : "ObservationRange.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "OBS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation"
        }
      },
      {
        "id" : "ObservationRange.moodCode",
        "path" : "ObservationRange.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "EVN.CRT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActMoodPredicate"
        }
      },
      {
        "id" : "ObservationRange.code",
        "path" : "ObservationRange.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActCode"
        }
      },
      {
        "id" : "ObservationRange.text",
        "path" : "ObservationRange.text",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "ObservationRange.value",
        "path" : "ObservationRange.value",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CO"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CV"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT-POS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/MO"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ON"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SC"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RTO-PQ-PQ"
          }
        ]
      },
      {
        "id" : "ObservationRange.interpretationCode",
        "path" : "ObservationRange.interpretationCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAObservationInterpretation"
        }
      },
      {
        "id" : "ObservationRange.sdtcPrecondition1",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "precondition1"
          }
        ],
        "path" : "ObservationRange.sdtcPrecondition1",
        "definition" : "The sdtc:precondition1 extension allows for the association of a criterion with a reference range (ObservationRange), which allows the expression in a lab report that a reference range is conditional on some criterion such as patient sex or age (or a combination of criterion).",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "ObservationRange.sdtcPrecondition1.typeCode",
        "path" : "ObservationRange.sdtcPrecondition1.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "PRCN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActRelationshipType"
        }
      },
      {
        "id" : "ObservationRange.sdtcPrecondition1.conjunctionCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "ObservationRange.sdtcPrecondition1.conjunctionCode",
        "definition" : "A code specifying the logical conjunction of the criteria among all the condition-links of Acts (e.g., and, or, exclusive-or).\n\nAll AND criteria must be true.\n\nIf OR and AND criteria occur together, one criterion out of the OR-group must be true and all AND criteria must be true also.\n\nIf XOR criteria occur together with OR and AND criteria, exactly one of the XOR criteria must be true, and at least one of the OR criteria and all AND criteria must be true.\n\nIn other words, the sets of AND, OR, and XOR criteria are in turn combined by a logical AND operator (all AND criteria and at least one OR criterion and exactly one XOR criterion).",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RelationshipConjunction|2.0.0"
        }
      },
      {
        "id" : "ObservationRange.sdtcPrecondition1.criterion1",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "ObservationRange.sdtcPrecondition1.criterion1",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Criterion"
          }
        ]
      },
      {
        "id" : "ObservationRange.precondition",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:oid:1.3.6.1.4.1.19376.1.3.2"
          }
        ],
        "path" : "ObservationRange.precondition",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/LabPrecondition"
          }
        ]
      }
    ]
  }
}

```
