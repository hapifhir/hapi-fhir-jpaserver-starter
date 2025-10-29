# RegionOfInterest (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **RegionOfInterest (CDA Class)**

## Logical Model: RegionOfInterest (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:RegionOfInterest |

 
A derivative of the RIM Observation class that represents a region of interest on an image, using an overlay shape. RegionOfInterest is used to make reference to specific regions in images, e.g., to specify the site of a physical finding by "circling" a region in a schematic picture of a human body. The units of the coordinate values in RegionOfInterest.value are in pixels, expressed as a list of integers. The origin is in the upper left hand corner, with positive X values going to the right and positive Y values going down. The relationship between a RegionOfInterest and its referenced ObservationMedia or ExternalObservation is specified by traversing the entryRelationship or reference class, respectively, where typeCode equals "SUBJ". A RegionOfInterest must reference exactly one ObservationMedia or one ExternalObservation. If the RegionOfInterest is the target of a renderMultimedia reference, then it shall only reference an ObservationMedia and not an ExternalObservation. 
An XML attribute "ID" of type XML ID, is added to RegionOfInterest within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification). 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/RegionOfInterest)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-RegionOfInterest.csv), [Excel](StructureDefinition-RegionOfInterest.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "RegionOfInterest",
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
      "valueString" : "regionOfInterest"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "RegionOfInterest",
  "title" : "RegionOfInterest (CDA Class)",
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
  "description" : "A derivative of the RIM Observation class that represents a region of interest on an image, using an overlay shape. RegionOfInterest is used to make reference to specific regions in images, e.g., to specify the site of a physical finding by \"circling\" a region in a schematic picture of a human body. The units of the coordinate values in RegionOfInterest.value are in pixels, expressed as a list of integers. The origin is in the upper left hand corner, with positive X values going to the right and positive Y values going down. The relationship between a RegionOfInterest and its referenced ObservationMedia or ExternalObservation is specified by traversing the entryRelationship or reference class, respectively, where typeCode equals \"SUBJ\". A RegionOfInterest must reference exactly one ObservationMedia or one ExternalObservation. If the RegionOfInterest is the target of a renderMultimedia reference, then it shall only reference an ObservationMedia and not an ExternalObservation.\n\nAn XML attribute \"ID\" of type XML ID, is added to RegionOfInterest within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/RegionOfInterest",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "RegionOfInterest",
        "path" : "RegionOfInterest",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "RegionOfInterest.ID",
        "path" : "RegionOfInterest.ID",
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
        "id" : "RegionOfInterest.classCode",
        "path" : "RegionOfInterest.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "ROIOVL",
        "fixedCode" : "ROIOVL",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActClassROI"
        }
      },
      {
        "id" : "RegionOfInterest.moodCode",
        "path" : "RegionOfInterest.moodCode",
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
        "id" : "RegionOfInterest.id",
        "path" : "RegionOfInterest.id",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.code",
        "path" : "RegionOfInterest.code",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ROIOverlayShape|2.0.0"
        }
      },
      {
        "id" : "RegionOfInterest.value",
        "path" : "RegionOfInterest.value",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.subject",
        "path" : "RegionOfInterest.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.specimen",
        "path" : "RegionOfInterest.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.performer",
        "path" : "RegionOfInterest.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.author",
        "path" : "RegionOfInterest.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.informant",
        "path" : "RegionOfInterest.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.participant",
        "path" : "RegionOfInterest.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.entryRelationship",
        "path" : "RegionOfInterest.entryRelationship",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.reference",
        "path" : "RegionOfInterest.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.precondition",
        "path" : "RegionOfInterest.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "RegionOfInterest.sdtcPrecondition2",
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
        "path" : "RegionOfInterest.sdtcPrecondition2",
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
