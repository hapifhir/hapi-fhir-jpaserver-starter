# ObservationMedia (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ObservationMedia (CDA Class)**

## Logical Model: ObservationMedia (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ObservationMedia |

 
A derivative of the RIM Observation class that represents multimedia that is logically part of the current document. This class is only for multimedia that is logically part of the attested content of the document. Rendering a referenced ObservationMedia requires a software tool that recognizes the particular MIME media type. 
An XML attribute "ID" of type XML ID, is added to ObservationMedia within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification). 
The distinction between ObservationMedia and ExternalObservation is that ObservationMedia entries are part of the attested content of the document whereas ExternalObservations are not. For instance, when a clinician draws a picture as part of a progress note, that picture is represented as a CDA ObservationMedia. If that clinician is also describing a finding seen on a chest-x-ray, the referenced chest-x-ray is represented as a CDA ExternalObservation. 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ObservationMedia)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ObservationMedia.csv), [Excel](StructureDefinition-ObservationMedia.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ObservationMedia",
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
      "valueString" : "observationMedia"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ObservationMedia",
  "title" : "ObservationMedia (CDA Class)",
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
  "description" : "A derivative of the RIM Observation class that represents multimedia that is logically part of the current document. This class is only for multimedia that is logically part of the attested content of the document. Rendering a referenced ObservationMedia requires a software tool that recognizes the particular MIME media type.\n\nAn XML attribute \"ID\" of type XML ID, is added to ObservationMedia within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).\n\nThe distinction between ObservationMedia and ExternalObservation is that ObservationMedia entries are part of the attested content of the document whereas ExternalObservations are not. For instance, when a clinician draws a picture as part of a progress note, that picture is represented as a CDA ObservationMedia. If that clinician is also describing a finding seen on a chest-x-ray, the referenced chest-x-ray is represented as a CDA ExternalObservation.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ObservationMedia",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ObservationMedia",
        "path" : "ObservationMedia",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "ObservationMedia.ID",
        "path" : "ObservationMedia.ID",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "id",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/xs-ID"]
          }
        ]
      },
      {
        "id" : "ObservationMedia.classCode",
        "path" : "ObservationMedia.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "OBS",
        "fixedCode" : "OBS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation"
        }
      },
      {
        "id" : "ObservationMedia.moodCode",
        "path" : "ObservationMedia.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "EVN",
        "fixedCode" : "EVN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActMood"
        }
      },
      {
        "id" : "ObservationMedia.id",
        "path" : "ObservationMedia.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ObservationMedia.languageCode",
        "path" : "ObservationMedia.languageCode",
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
        "id" : "ObservationMedia.value",
        "path" : "ObservationMedia.value",
        "representation" : ["typeAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "ObservationMedia.subject",
        "path" : "ObservationMedia.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "ObservationMedia.specimen",
        "path" : "ObservationMedia.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "ObservationMedia.performer",
        "path" : "ObservationMedia.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "ObservationMedia.author",
        "path" : "ObservationMedia.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "ObservationMedia.informant",
        "path" : "ObservationMedia.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "ObservationMedia.participant",
        "path" : "ObservationMedia.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "ObservationMedia.entryRelationship",
        "path" : "ObservationMedia.entryRelationship",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship"
          }
        ]
      },
      {
        "id" : "ObservationMedia.reference",
        "path" : "ObservationMedia.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "ObservationMedia.precondition",
        "path" : "ObservationMedia.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "ObservationMedia.sdtcPrecondition2",
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
        "path" : "ObservationMedia.sdtcPrecondition2",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition2"
          }
        ]
      }
    ]
  }
}

```
