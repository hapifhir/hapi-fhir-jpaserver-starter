# CDARoleClassMutualRelationship - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDARoleClassMutualRelationship**

## ValueSet: CDARoleClassMutualRelationship 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDARoleClassMutualRelationship | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDARoleClassMutualRelationship |

 
A relationship that is based on mutual behavior of the two Entities as being related. The basis of such relationship may be agreements (e.g., spouses, contract parties) or they may be de facto behavior (e.g. friends) or may be an incidental involvement with each other (e.g. parties over a dispute, siblings, children). 

 **References** 

* [RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md)

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
  "id" : "CDARoleClassMutualRelationship",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDARoleClassMutualRelationship",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDARoleClassMutualRelationship",
  "title" : "CDARoleClassMutualRelationship",
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
  "description" : "A relationship that is based on mutual behavior of the two Entities as being related. The basis of such relationship may be agreements (e.g., spouses, contract parties) or they may be de facto behavior (e.g. friends) or may be an incidental involvement with each other (e.g. parties over a dispute, siblings, children).",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-RoleClass",
        "concept" : [
          {
            "code" : "LIC"
          },
          {
            "code" : "NOT"
          },
          {
            "code" : "PROV"
          },
          {
            "code" : "CON"
          },
          {
            "code" : "ECON"
          },
          {
            "code" : "NOK"
          },
          {
            "code" : "ASSIGNED"
          },
          {
            "code" : "COMPAR"
          },
          {
            "code" : "SGNOFF"
          },
          {
            "code" : "AGNT"
          },
          {
            "code" : "GUARD"
          },
          {
            "code" : "EMP"
          },
          {
            "code" : "MIL"
          },
          {
            "code" : "INVSBJ"
          },
          {
            "code" : "CASESBJ"
          },
          {
            "code" : "RESBJ"
          },
          {
            "code" : "CIT"
          },
          {
            "code" : "COVPTY"
          },
          {
            "code" : "CRINV"
          },
          {
            "code" : "CRSPNSR"
          },
          {
            "code" : "GUAR"
          },
          {
            "code" : "PAT"
          },
          {
            "code" : "PAYEE"
          },
          {
            "code" : "PAYOR"
          },
          {
            "code" : "POLHOLD"
          },
          {
            "code" : "QUAL"
          },
          {
            "code" : "SPNSR"
          },
          {
            "code" : "STD"
          },
          {
            "code" : "UNDWRT"
          },
          {
            "code" : "CAREGIVER"
          },
          {
            "code" : "PRS"
          }
        ]
      }
    ]
  }
}

```
