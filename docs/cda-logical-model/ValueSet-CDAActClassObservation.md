# CDAActClassObservation - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAActClassObservation**

## ValueSet: CDAActClassObservation 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAActClassObservation |

 
An act that is intended to result in new information about a subject. The main difference between Observations and other Acts is that Observations have a value attribute. 

 **References** 

* [Criterion (CDA Class)](StructureDefinition-Criterion.md)
* [ExternalObservation (CDA Class)](StructureDefinition-ExternalObservation.md)
* [Observation (CDA Class)](StructureDefinition-Observation.md)
* [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)
* [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

### Logical Definition (CLD)

 

### Expansion

-------

 Explanation of the columns that may appear on this page: 

| | |
| :--- | :--- |
| Level | A few code lists that FHIR defines are hierarchical - each code is assigned a level. In this scheme, some codes are under other codes, and imply that the code they are under also applies |
| System | The source of the definition of the code (when the value set draws in codes defined elsewhere) |
| Code | The code (used as the code in the resource instance) |
| Display | The display (used in the*display*element of a[Coding](http://hl7.org/fhir/R5/datatypes.html#Coding)). If there is no display, implementers should not simply display the code, but map the concept into their application |
| Definition | An explanation of the meaning of the concept |
| Comments | Additional notes about how to use the code |



## Resource Content

```json
{
  "resourceType" : "ValueSet",
  "id" : "CDAActClassObservation",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClassObservation",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAActClassObservation",
  "title" : "CDAActClassObservation",
  "status" : "draft",
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
  "description" : "An act that is intended to result in new information about a subject. The main difference between Observations and other Acts is that Observations have a value attribute.",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-ActClass",
        "concept" : [
          {
            "code" : "CASE"
          },
          {
            "code" : "OUTB"
          },
          {
            "code" : "COND"
          },
          {
            "code" : "OBSSER"
          },
          {
            "code" : "OBSCOR"
          },
          {
            "code" : "ROIBND"
          },
          {
            "code" : "ROIOVL"
          },
          {
            "code" : "OBS"
          },
          {
            "code" : "ALRT"
          },
          {
            "code" : "CLNTRL"
          },
          {
            "code" : "CNOD"
          },
          {
            "code" : "DGIMG"
          },
          {
            "code" : "INVSTG"
          },
          {
            "code" : "SPCOBS"
          }
        ]
      }
    ]
  }
}

```
