# Organizer (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Organizer (CDA Class)**

## Logical Model: Organizer (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Organizer | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Organizer |

 
A derivative of the RIM Act class, which can be used to create arbitrary groupings of other CDA entries that share a common context. An Organizer can contain other Organizers and/or other CDA entries, by traversing the component relationship. An Organizer can refer to external acts by traversing the reference relationship. An Organizer cannot be the source of an entryRelationship relationship. NOTE: CDA entries such as Observation can also contain other CDA entries by traversing the entryRelationship class. There is no requirement that the Organizer entry be used in order to group CDA entries. 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Organizer)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Organizer.csv), [Excel](StructureDefinition-Organizer.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Organizer",
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
      "valueString" : "organizer"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Organizer",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Organizer",
  "title" : "Organizer (CDA Class)",
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
  "description" : "A derivative of the RIM Act class, which can be used to create arbitrary groupings of other CDA entries that share a common context. An Organizer can contain other Organizers and/or other CDA entries, by traversing the component relationship. An Organizer can refer to external acts by traversing the reference relationship. An Organizer cannot be the source of an entryRelationship relationship.\nNOTE: CDA entries such as Observation can also contain other CDA entries by traversing the entryRelationship class. There is no requirement that the Organizer entry be used in order to group CDA entries.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Organizer",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Organizer",
        "path" : "Organizer",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Organizer.classCode",
        "path" : "Organizer.classCode",
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
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xActClassDocumentEntryOrganizer|2.0.0"
        }
      },
      {
        "id" : "Organizer.moodCode",
        "path" : "Organizer.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "EVN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActMood"
        }
      },
      {
        "id" : "Organizer.id",
        "path" : "Organizer.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Organizer.sdtcCategory",
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
        "path" : "Organizer.sdtcCategory",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "Organizer.code",
        "path" : "Organizer.code",
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
        "id" : "Organizer.sdtcText",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "text"
          }
        ],
        "path" : "Organizer.sdtcText",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "Organizer.statusCode",
        "path" : "Organizer.statusCode",
        "min" : 1,
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
        "id" : "Organizer.effectiveTime",
        "path" : "Organizer.effectiveTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "Organizer.subject",
        "path" : "Organizer.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "Organizer.specimen",
        "path" : "Organizer.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "Organizer.performer",
        "path" : "Organizer.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "Organizer.author",
        "path" : "Organizer.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "Organizer.informant",
        "path" : "Organizer.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "Organizer.participant",
        "path" : "Organizer.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "Organizer.reference",
        "path" : "Organizer.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "Organizer.precondition",
        "path" : "Organizer.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "Organizer.sdtcPrecondition2",
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
        "path" : "Organizer.sdtcPrecondition2",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition2"
          }
        ]
      },
      {
        "id" : "Organizer.component",
        "path" : "Organizer.component",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/OrganizerComponent"
          }
        ]
      }
    ]
  }
}

```
