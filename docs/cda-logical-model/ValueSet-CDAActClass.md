# CDAActClass - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAActClass**

## ValueSet: CDAActClass 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAActClass | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAActClass |

 
A code specifying the major type of Act that this Act-instance represents. 

 **References** 

* [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)
* [Consent (CDA Class)](StructureDefinition-Consent.md)
* [EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md)
* [Encounter (CDA Class)](StructureDefinition-Encounter.md)
* [ExternalAct (CDA Class)](StructureDefinition-ExternalAct.md)
* [Order (CDA Class)](StructureDefinition-Order.md)
* [ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md)
* [SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)

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
  "id" : "CDAActClass",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClass",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAActClass",
  "title" : "CDAActClass",
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
  "description" : "A code specifying the major type of Act that this Act-instance represents.",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-ActClass",
        "concept" : [
          {
            "code" : "FCNTRCT"
          },
          {
            "code" : "COV"
          },
          {
            "code" : "CNTRCT"
          },
          {
            "code" : "CACT"
          },
          {
            "code" : "ACTN"
          },
          {
            "code" : "INFO"
          },
          {
            "code" : "STC"
          },
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
          },
          {
            "code" : "SPLY"
          },
          {
            "code" : "DIET"
          },
          {
            "code" : "DOCCLIN"
          },
          {
            "code" : "CDALVLONE"
          },
          {
            "code" : "DOC"
          },
          {
            "code" : "COMPOSITION"
          },
          {
            "code" : "ENTRY"
          },
          {
            "code" : "BATTERY"
          },
          {
            "code" : "CLUSTER"
          },
          {
            "code" : "EXTRACT"
          },
          {
            "code" : "EHR"
          },
          {
            "code" : "ORGANIZER"
          },
          {
            "code" : "CATEGORY"
          },
          {
            "code" : "DOCBODY"
          },
          {
            "code" : "DOCSECT"
          },
          {
            "code" : "TOPIC"
          },
          {
            "code" : "FOLDER"
          },
          {
            "code" : "ACT"
          },
          {
            "code" : "ACCM"
          },
          {
            "code" : "CONS"
          },
          {
            "code" : "CTTEVENT"
          },
          {
            "code" : "INC"
          },
          {
            "code" : "INFRM"
          },
          {
            "code" : "PCPR"
          },
          {
            "code" : "REG"
          },
          {
            "code" : "SPCTRT"
          },
          {
            "code" : "ACCT"
          },
          {
            "code" : "ACSN"
          },
          {
            "code" : "ADJUD"
          },
          {
            "code" : "CONTREG"
          },
          {
            "code" : "DISPACT"
          },
          {
            "code" : "ENC"
          },
          {
            "code" : "INVE"
          },
          {
            "code" : "LIST"
          },
          {
            "code" : "MPROT"
          },
          {
            "code" : "PROC"
          },
          {
            "code" : "REV"
          },
          {
            "code" : "SBADM"
          },
          {
            "code" : "SUBST"
          },
          {
            "code" : "TRNS"
          },
          {
            "code" : "VERIF"
          },
          {
            "code" : "XACT"
          }
        ]
      }
    ]
  }
}

```
