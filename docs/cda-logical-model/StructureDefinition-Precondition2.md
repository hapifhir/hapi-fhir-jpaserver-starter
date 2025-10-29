# Precondition2 (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Precondition2 (CDA Class)**

## Logical Model: Precondition2 (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Precondition2 | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Precondition2 |

 
The sdtc:precondition2 extension allows a more flexible set of skip conditions on a set of criteria. Without this extension the skip condition is restricted to all criteria true. The extension allows a choice of the following logical operation extensions sdtc:allTrue, sdtc:allFalse, sdtc:atLeastOneTrue, sdtc:atLeastOneFalse, sdtc:onlyOneFalse, and sdtc:onlyOneTrue to be placed upon the encapsulated criteria or a criterion. The logical operation extensions nest a [0 .. *] sdtc:precondition2 extension allowing for a complex specification of nested skip conditions if needed. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)...Show 4 more,[Organizer (CDA Class)](StructureDefinition-Organizer.md),[PreconditionBase (CDA Class)](StructureDefinition-PreconditionBase.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md)and[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Precondition2)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Precondition2.csv), [Excel](StructureDefinition-Precondition2.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Precondition2",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "precondition"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition2",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Precondition2",
  "title" : "Precondition2 (CDA Class)",
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
  "description" : "The sdtc:precondition2 extension allows a more flexible set of skip conditions on a set of criteria. Without this extension the skip condition is restricted to all criteria true. The extension allows a choice of the following logical operation extensions sdtc:allTrue, sdtc:allFalse, sdtc:atLeastOneTrue, sdtc:atLeastOneFalse, sdtc:onlyOneFalse, and sdtc:onlyOneTrue to be placed upon the encapsulated criteria or a criterion. The logical operation extensions nest a [0 .. *] sdtc:precondition2 extension allowing for a complex specification of nested skip conditions if needed.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition2",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Precondition2",
        "path" : "Precondition2",
        "min" : 1,
        "max" : "1",
        "constraint" : [
          {
            "key" : "precondition2-only-one",
            "severity" : "error",
            "human" : "SHALL have only one of allTrue, allFalse, atLeastOneTrue, atLeastOneFalse, onlyOneTrue, onlyOneFalse, or criterion",
            "expression" : "(allTrue | allFalse | atLeastOneTrue | atLeastOneFalse | onlyOneTrue | onlyOneFalse | criterion).count() = 1"
          }
        ]
      },
      {
        "id" : "Precondition2.typeCode",
        "path" : "Precondition2.typeCode",
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
        "id" : "Precondition2.negationInd",
        "path" : "Precondition2.negationInd",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      },
      {
        "id" : "Precondition2.conjunctionCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.conjunctionCode",
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
        "id" : "Precondition2.allTrue",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.allTrue",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.allFalse",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.allFalse",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.atLeastOneTrue",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.atLeastOneTrue",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.atLeastOneFalse",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.atLeastOneFalse",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.onlyOneTrue",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.onlyOneTrue",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.onlyOneFalse",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.onlyOneFalse",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PreconditionBase"
          }
        ]
      },
      {
        "id" : "Precondition2.criterion",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "Precondition2.criterion",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Criterion"
          }
        ]
      }
    ]
  }
}

```
