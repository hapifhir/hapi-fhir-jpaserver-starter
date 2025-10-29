# Observation (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Observation (CDA Class)**

## Logical Model: Observation (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Observation | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Observation |

 
A derivative of the RIM Observation class, used for representing coded and other observations. 
Observation.negationInd, when set to "true", is a positive assertion that the Observation as a whole is negated. Some properties such as Observation.id, Observation.moodCode, and the participations are not negated. These properties always have the same meaning: i.e., the author remains the author of the negative Observation. An observation statement with negationInd is still a statement about the specific fact described by the Observation. For instance, a negated "finding of wheezing on July 1" means that the author positively denies that there was wheezing on July 1, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation. 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Observation)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Observation.csv), [Excel](StructureDefinition-Observation.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Observation",
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
      "valueString" : "observation"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Observation",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Observation",
  "title" : "Observation (CDA Class)",
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
  "description" : "A derivative of the RIM Observation class, used for representing coded and other observations.\n\nObservation.negationInd, when set to \"true\", is a positive assertion that the Observation as a whole is negated. Some properties such as Observation.id, Observation.moodCode, and the participations are not negated. These properties always have the same meaning: i.e., the author remains the author of the negative Observation. An observation statement with negationInd is still a statement about the specific fact described by the Observation. For instance, a negated \"finding of wheezing on July 1\" means that the author positively denies that there was wheezing on July 1, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Observation",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Observation",
        "path" : "Observation",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Observation.classCode",
        "path" : "Observation.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation"
        }
      },
      {
        "id" : "Observation.moodCode",
        "path" : "Observation.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xActMoodDocumentObservation|2.0.0"
        }
      },
      {
        "id" : "Observation.negationInd",
        "path" : "Observation.negationInd",
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
        "id" : "Observation.id",
        "path" : "Observation.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Observation.sdtcCategory",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "category"
          }
        ],
        "path" : "Observation.sdtcCategory",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "Observation.code",
        "path" : "Observation.code",
        "representation" : ["typeAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ObservationType"
        }
      },
      {
        "id" : "Observation.derivationExpr",
        "path" : "Observation.derivationExpr",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          }
        ]
      },
      {
        "id" : "Observation.text",
        "path" : "Observation.text",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "Observation.statusCode",
        "path" : "Observation.statusCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActStatus"
        }
      },
      {
        "id" : "Observation.effectiveTime",
        "path" : "Observation.effectiveTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "Observation.priorityCode",
        "path" : "Observation.priorityCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActPriority"
        }
      },
      {
        "id" : "Observation.repeatNumber",
        "path" : "Observation.repeatNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT"
          }
        ]
      },
      {
        "id" : "Observation.languageCode",
        "path" : "Observation.languageCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/fhir/ValueSet/all-languages"
        }
      },
      {
        "id" : "Observation.value",
        "path" : "Observation.value",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CV"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST-dot-r2b"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CO"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SC"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
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
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          },
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
        "id" : "Observation.interpretationCode",
        "path" : "Observation.interpretationCode",
        "min" : 0,
        "max" : "*",
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
        "id" : "Observation.methodCode",
        "path" : "Observation.methodCode",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ObservationMethod"
        }
      },
      {
        "id" : "Observation.targetSiteCode",
        "path" : "Observation.targetSiteCode",
        "definition" : "Drawn from concept domain ActSite",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "Observation.subject",
        "path" : "Observation.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "Observation.specimen",
        "path" : "Observation.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "Observation.performer",
        "path" : "Observation.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "Observation.author",
        "path" : "Observation.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "Observation.informant",
        "path" : "Observation.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "Observation.participant",
        "path" : "Observation.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "Observation.entryRelationship",
        "path" : "Observation.entryRelationship",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship"
          }
        ]
      },
      {
        "id" : "Observation.reference",
        "path" : "Observation.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "Observation.precondition",
        "path" : "Observation.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "Observation.sdtcPrecondition2",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "precondition2"
          }
        ],
        "path" : "Observation.sdtcPrecondition2",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition2"
          }
        ]
      },
      {
        "id" : "Observation.referenceRange",
        "path" : "Observation.referenceRange",
        "definition" : "Relates an Observation to the ObservationRange class, where the expected range of values for a particular observation can be specified.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "Observation.referenceRange.typeCode",
        "path" : "Observation.referenceRange.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "REFV",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActRelationshipType"
        }
      },
      {
        "id" : "Observation.referenceRange.observationRange",
        "path" : "Observation.referenceRange.observationRange",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationRange"
          }
        ]
      },
      {
        "id" : "Observation.sdtcInFulfillmentOf1",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "inFulfillmentOf1"
          }
        ],
        "path" : "Observation.sdtcInFulfillmentOf1",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf1"
          }
        ]
      }
    ]
  }
}

```
