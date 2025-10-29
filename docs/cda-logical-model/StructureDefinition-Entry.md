# Entry (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Entry (CDA Class)**

## Logical Model: Entry (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Entry | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Entry |

 
CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries. 

**Usages:**

* Use this Logical Model: [Section (CDA Class)](StructureDefinition-Section.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Entry)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Entry.csv), [Excel](StructureDefinition-Entry.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Entry",
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
      "valueString" : "entry"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Entry",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Entry",
  "title" : "Entry (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Entry",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Entry",
        "path" : "Entry",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "entry-only-one",
            "severity" : "error",
            "human" : "SHALL have no more than one of act, encounter, observation, observationMedia, organizer, procedure, regionOfInterest, substanceAdministration, or supply.",
            "expression" : "(act | encounter | observation | observationMedia | organizer | procedure | regionOfInterest | substanceAdministration | supply).count() = 1"
          }
        ]
      },
      {
        "id" : "Entry.typeCode",
        "path" : "Entry.typeCode",
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
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xActRelationshipEntry"
        }
      },
      {
        "id" : "Entry.contextConductionInd",
        "path" : "Entry.contextConductionInd",
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
        "id" : "Entry.act",
        "path" : "Entry.act",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Act"
          }
        ]
      },
      {
        "id" : "Entry.encounter",
        "path" : "Entry.encounter",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Encounter"
          }
        ]
      },
      {
        "id" : "Entry.observation",
        "path" : "Entry.observation",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Observation"
          }
        ]
      },
      {
        "id" : "Entry.observationMedia",
        "path" : "Entry.observationMedia",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia"
          }
        ]
      },
      {
        "id" : "Entry.organizer",
        "path" : "Entry.organizer",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organizer"
          }
        ]
      },
      {
        "id" : "Entry.procedure",
        "path" : "Entry.procedure",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Procedure"
          }
        ]
      },
      {
        "id" : "Entry.regionOfInterest",
        "path" : "Entry.regionOfInterest",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest"
          }
        ]
      },
      {
        "id" : "Entry.substanceAdministration",
        "path" : "Entry.substanceAdministration",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration"
          }
        ]
      },
      {
        "id" : "Entry.supply",
        "path" : "Entry.supply",
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
