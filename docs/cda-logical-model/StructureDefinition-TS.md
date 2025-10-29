# TS: PointInTime (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **TS: PointInTime (V3 Data Type)**

## Logical Model: TS: PointInTime (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/TS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:TS |

 
A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression. 

**Usages:**

* Derived from this Logical Model: [IVXB_TS: Interval Boundary PointInTime (V3 Data Type)](StructureDefinition-IVXB-TS.md) and [SXCM_TS: GeneralTimingSpecification (V3 Data Type)](StructureDefinition-SXCM-TS.md)
* Use this Logical Model: [Authenticator (CDA Class)](StructureDefinition-Authenticator.md), [Author (CDA Class)](StructureDefinition-Author.md), [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md)...Show 10 more,[DataEnterer (CDA Class)](StructureDefinition-DataEnterer.md),[IVL_TS: Interval (V3 Data Type)](StructureDefinition-IVL-TS.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[Person (CDA Class)](StructureDefinition-Person.md),[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md)and[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/TS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-TS.csv), [Excel](StructureDefinition-TS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "TS",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/TS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "TS",
  "title" : "TS: PointInTime (V3 Data Type)",
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
  "description" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/TS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/QTY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "TS",
        "path" : "TS",
        "definition" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "TS.value",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-date-format",
            "valueString" : "YYYYMMDDHHMMSS.UUUU[+|-ZZzz]"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-date-rules",
            "valueString" : "year-valid"
          }
        ],
        "path" : "TS.value",
        "representation" : ["xmlAttr"],
        "definition" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "dateTime",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/ts-simple"]
          }
        ]
      }
    ]
  }
}

```
