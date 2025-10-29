# CDAActRelationshipType - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAActRelationshipType**

## ValueSet: CDAActRelationshipType 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAActRelationshipType | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAActRelationshipType |

 
A code specifying the meaning and purpose of every ActRelationship instance. Each of its values implies specific constraints to what kinds of Act objects can be related and in which way. 

 **References** 

* [DocumentationOf (CDA Class)](StructureDefinition-DocumentationOf.md)
* [Observation (CDA Class)](StructureDefinition-Observation.md)
* [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)
* [Precondition (CDA Class)](StructureDefinition-Precondition.md)
* [Precondition2 (CDA Class)](StructureDefinition-Precondition2.md)

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
  "id" : "CDAActRelationshipType",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAActRelationshipType",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAActRelationshipType",
  "title" : "CDAActRelationshipType",
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
  "description" : "A code specifying the meaning and purpose of every ActRelationship instance. Each of its values implies specific constraints to what kinds of Act objects can be related and in which way.",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-ActRelationshipType",
        "concept" : [
          {
            "code" : "RSON"
          },
          {
            "code" : "MITGT"
          },
          {
            "code" : "CIND"
          },
          {
            "code" : "PRCN"
          },
          {
            "code" : "TRIG"
          },
          {
            "code" : "COMP"
          },
          {
            "code" : "ARR"
          },
          {
            "code" : "CTRLV"
          },
          {
            "code" : "DEP"
          },
          {
            "code" : "OBJC"
          },
          {
            "code" : "OBJF"
          },
          {
            "code" : "OUTC"
          },
          {
            "code" : "GOAL"
          },
          {
            "code" : "RISK"
          },
          {
            "code" : "CHRG"
          },
          {
            "code" : "COST"
          },
          {
            "code" : "CREDIT"
          },
          {
            "code" : "DEBIT"
          },
          {
            "code" : "SAS"
          },
          {
            "code" : "SPRT"
          },
          {
            "code" : "SPRTBND"
          },
          {
            "code" : "PERT"
          },
          {
            "code" : "AUTH"
          },
          {
            "code" : "CAUS"
          },
          {
            "code" : "COVBY"
          },
          {
            "code" : "DRIV"
          },
          {
            "code" : "EXPL"
          },
          {
            "code" : "ITEMSLOC"
          },
          {
            "code" : "LIMIT"
          },
          {
            "code" : "MFST"
          },
          {
            "code" : "NAME"
          },
          {
            "code" : "PREV"
          },
          {
            "code" : "REFR"
          },
          {
            "code" : "REFV"
          },
          {
            "code" : "SUBJ"
          },
          {
            "code" : "SUMM"
          },
          {
            "code" : "XCRPT"
          },
          {
            "code" : "VRXCRPT"
          },
          {
            "code" : "FLFS"
          },
          {
            "code" : "OCCR"
          },
          {
            "code" : "OREF"
          },
          {
            "code" : "SCH"
          },
          {
            "code" : "RPLC"
          },
          {
            "code" : "SUCC"
          },
          {
            "code" : "SEQL"
          },
          {
            "code" : "APND"
          },
          {
            "code" : "DOC"
          },
          {
            "code" : "ELNK"
          },
          {
            "code" : "GEN"
          },
          {
            "code" : "GEVL"
          },
          {
            "code" : "INST"
          },
          {
            "code" : "MTCH"
          },
          {
            "code" : "OPTN"
          },
          {
            "code" : "REV"
          },
          {
            "code" : "UPDT"
          },
          {
            "code" : "XFRM"
          }
        ]
      }
    ]
  }
}

```
