# OrganizerComponent (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **OrganizerComponent (CDA Class)**

## Logical Model: OrganizerComponent (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/OrganizerComponent | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:OrganizerComponent |

 
CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries. 

**Usages:**

* Use this Logical Model: [Organizer (CDA Class)](StructureDefinition-Organizer.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/OrganizerComponent)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-OrganizerComponent.csv), [Excel](StructureDefinition-OrganizerComponent.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "OrganizerComponent",
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
      "valueString" : "component"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/OrganizerComponent",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "OrganizerComponent",
  "title" : "OrganizerComponent (CDA Class)",
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
  "description" : "CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/OrganizerComponent",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "OrganizerComponent",
        "path" : "OrganizerComponent",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "organizer-only-one",
            "severity" : "error",
            "human" : "SHALL have no more than one of act, encounter, observation, observationMedia, organizer, procedure, regionOfInterest, substanceAdministration, or supply.",
            "expression" : "(act | encounter | observation | observationMedia | organizer | procedure | regionOfInterest | substanceAdministration | supply).count() = 1"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.typeCode",
        "path" : "OrganizerComponent.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "COMP",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActRelationshipHasComponent"
        }
      },
      {
        "id" : "OrganizerComponent.contextConductionInd",
        "path" : "OrganizerComponent.contextConductionInd",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ],
        "fixedBoolean" : true
      },
      {
        "id" : "OrganizerComponent.sequenceNumber",
        "path" : "OrganizerComponent.sequenceNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.sdtcPriorityNumber",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "priorityNumber"
          }
        ],
        "path" : "OrganizerComponent.sdtcPriorityNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.seperatableInd",
        "path" : "OrganizerComponent.seperatableInd",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.act",
        "path" : "OrganizerComponent.act",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Act"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.encounter",
        "path" : "OrganizerComponent.encounter",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Encounter"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.observation",
        "path" : "OrganizerComponent.observation",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Observation"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.observationMedia",
        "path" : "OrganizerComponent.observationMedia",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.organizer",
        "path" : "OrganizerComponent.organizer",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organizer"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.procedure",
        "path" : "OrganizerComponent.procedure",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Procedure"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.regionOfInterest",
        "path" : "OrganizerComponent.regionOfInterest",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.substanceAdministration",
        "path" : "OrganizerComponent.substanceAdministration",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration"
          }
        ]
      },
      {
        "id" : "OrganizerComponent.supply",
        "path" : "OrganizerComponent.supply",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Supply"
          }
        ]
      }
    ]
  }
}

```
