# EntryRelationship (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **EntryRelationship (CDA Class)**

## Logical Model: EntryRelationship (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:EntryRelationship |

 
CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)...Show 4 more,[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/EntryRelationship)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-EntryRelationship.csv), [Excel](StructureDefinition-EntryRelationship.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "EntryRelationship",
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
      "valueString" : "entryRelationship"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "EntryRelationship",
  "title" : "EntryRelationship (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "EntryRelationship",
        "path" : "EntryRelationship",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "entry-rel-only-one",
            "severity" : "error",
            "human" : "SHALL have no more than one of act, encounter, observation, observationMedia, organizer, procedure, regionOfInterest, substanceAdministration, or supply.",
            "expression" : "(act | encounter | observation | observationMedia | organizer | procedure | regionOfInterest | substanceAdministration | supply).count() = 1"
          }
        ]
      },
      {
        "id" : "EntryRelationship.typeCode",
        "path" : "EntryRelationship.typeCode",
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
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xActRelationshipEntryRelationship"
        }
      },
      {
        "id" : "EntryRelationship.inversionInd",
        "path" : "EntryRelationship.inversionInd",
        "representation" : ["xmlAttr"],
        "definition" : "The entryRelationship.inversionInd can be set to \"true\" to indicate that the relationship should be interpreted as if the roles of the source and target entries were reversed.",
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
        "id" : "EntryRelationship.contextConductionInd",
        "path" : "EntryRelationship.contextConductionInd",
        "representation" : ["xmlAttr"],
        "definition" : "The entryRelationship.contextConductionInd differs from the otherwise common use of this attribute in that in all other cases where this attribute is used, the value is fixed at \"true\", whereas here the value is defaulted to \"true\", and can be changed to \"false\" when referencing an entry in the same document. Setting the context conduction to false when referencing an entry in the same document keeps clear the fact that the referenced object retains its original context.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ],
        "defaultValueBoolean" : true
      },
      {
        "id" : "EntryRelationship.negationInd",
        "path" : "EntryRelationship.negationInd",
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
        "id" : "EntryRelationship.sequenceNumber",
        "path" : "EntryRelationship.sequenceNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "EntryRelationship.seperatableInd",
        "path" : "EntryRelationship.seperatableInd",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          }
        ]
      },
      {
        "id" : "EntryRelationship.act",
        "path" : "EntryRelationship.act",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Act"
          }
        ]
      },
      {
        "id" : "EntryRelationship.encounter",
        "path" : "EntryRelationship.encounter",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Encounter"
          }
        ]
      },
      {
        "id" : "EntryRelationship.observation",
        "path" : "EntryRelationship.observation",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Observation"
          }
        ]
      },
      {
        "id" : "EntryRelationship.observationMedia",
        "path" : "EntryRelationship.observationMedia",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia"
          }
        ]
      },
      {
        "id" : "EntryRelationship.organizer",
        "path" : "EntryRelationship.organizer",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organizer"
          }
        ]
      },
      {
        "id" : "EntryRelationship.procedure",
        "path" : "EntryRelationship.procedure",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Procedure"
          }
        ]
      },
      {
        "id" : "EntryRelationship.regionOfInterest",
        "path" : "EntryRelationship.regionOfInterest",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest"
          }
        ]
      },
      {
        "id" : "EntryRelationship.substanceAdministration",
        "path" : "EntryRelationship.substanceAdministration",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration"
          }
        ]
      },
      {
        "id" : "EntryRelationship.supply",
        "path" : "EntryRelationship.supply",
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
