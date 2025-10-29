# CDARoleClass - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDARoleClass**

## ValueSet: CDARoleClass 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDARoleClass | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDARoleClass |

 
Represent a Role which is an association or relationship between two entities - the entity that plays the role and the entity that scopes the role. Roles names are derived from the name of the playing entity in that role. 

 **References** 

* [AlternateIdentification (CDA Class)](StructureDefinition-AlternateIdentification.md)

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
  "id" : "CDARoleClass",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDARoleClass",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDARoleClass",
  "title" : "CDARoleClass",
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
  "description" : "Represent a Role which is an association or relationship between two entities - the entity that plays the role and the entity that scopes the role. Roles names are derived from the name of the playing entity in that role.",
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
          },
          {
            "code" : "DST"
          },
          {
            "code" : "RET"
          },
          {
            "code" : "MANU"
          },
          {
            "code" : "THER"
          },
          {
            "code" : "SDLOC"
          },
          {
            "code" : "DSDLOC"
          },
          {
            "code" : "ISDLOC"
          },
          {
            "code" : "ACCESS"
          },
          {
            "code" : "BIRTHPL"
          },
          {
            "code" : "EXPR"
          },
          {
            "code" : "HLD"
          },
          {
            "code" : "HLTHCHRT"
          },
          {
            "code" : "IDENT"
          },
          {
            "code" : "MNT"
          },
          {
            "code" : "OWN"
          },
          {
            "code" : "RGPR"
          },
          {
            "code" : "TERR"
          },
          {
            "code" : "WRTE"
          },
          {
            "code" : "GEN"
          },
          {
            "code" : "GRIC"
          },
          {
            "code" : "INST"
          },
          {
            "code" : "SUBS"
          },
          {
            "code" : "SUBY"
          },
          {
            "code" : "IACT"
          },
          {
            "code" : "COLR"
          },
          {
            "code" : "FLVR"
          },
          {
            "code" : "PRSV"
          },
          {
            "code" : "STBL"
          },
          {
            "code" : "INGR"
          },
          {
            "code" : "ACTI"
          },
          {
            "code" : "ACTM"
          },
          {
            "code" : "ADTV"
          },
          {
            "code" : "BASE"
          },
          {
            "code" : "LOCE"
          },
          {
            "code" : "STOR"
          },
          {
            "code" : "SPEC"
          },
          {
            "code" : "ALQT"
          },
          {
            "code" : "ISLT"
          },
          {
            "code" : "CONT"
          },
          {
            "code" : "MBR"
          },
          {
            "code" : "PART"
          },
          {
            "code" : "ROL"
          }
        ]
      }
    ]
  }
}

```
