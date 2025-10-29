# CDARoleCode - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDARoleCode**

## ValueSet: CDARoleCode 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDARoleCode | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDARoleCode |

 
A set of codes further specifying the kind of Role; specific classification codes for further qualifying RoleClass codes. 

 **References** 

* [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md)
* [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md)
* [AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md)
* [Guardian (CDA Class)](StructureDefinition-Guardian.md)
* [OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md)
* [ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md)

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
  "id" : "CDARoleCode",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDARoleCode",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDARoleCode",
  "title" : "CDARoleCode",
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
  "description" : "A set of codes further specifying the kind of Role; specific classification codes for further qualifying RoleClass codes.",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-RoleCode",
        "concept" : [
          {
            "code" : "DX"
          },
          {
            "code" : "CVDX"
          },
          {
            "code" : "CATH"
          },
          {
            "code" : "ECHO"
          },
          {
            "code" : "GIDX"
          },
          {
            "code" : "ENDOS"
          },
          {
            "code" : "RADDX"
          },
          {
            "code" : "RADO"
          },
          {
            "code" : "RNEU"
          },
          {
            "code" : "HOSP"
          },
          {
            "code" : "CHR"
          },
          {
            "code" : "GACH"
          },
          {
            "code" : "MHSP"
          },
          {
            "code" : "PSYCHF"
          },
          {
            "code" : "RH"
          },
          {
            "code" : "RHAT"
          },
          {
            "code" : "RHII"
          },
          {
            "code" : "RHMAD"
          },
          {
            "code" : "RHPI"
          },
          {
            "code" : "RHPIH"
          },
          {
            "code" : "RHPIMS"
          },
          {
            "code" : "RHPIVS"
          },
          {
            "code" : "RHYAD"
          },
          {
            "code" : "HU"
          },
          {
            "code" : "BMTU"
          },
          {
            "code" : "CCU"
          },
          {
            "code" : "CHEST"
          },
          {
            "code" : "EPIL"
          },
          {
            "code" : "ER"
          },
          {
            "code" : "ETU"
          },
          {
            "code" : "HD"
          },
          {
            "code" : "HLAB"
          },
          {
            "code" : "INLAB"
          },
          {
            "code" : "OUTLAB"
          },
          {
            "code" : "HRAD"
          },
          {
            "code" : "HUSCS"
          },
          {
            "code" : "ICU"
          },
          {
            "code" : "PEDICU"
          },
          {
            "code" : "PEDNICU"
          },
          {
            "code" : "INPHARM"
          },
          {
            "code" : "MBL"
          },
          {
            "code" : "NCCS"
          },
          {
            "code" : "NS"
          },
          {
            "code" : "OUTPHARM"
          },
          {
            "code" : "PEDU"
          },
          {
            "code" : "PHU"
          },
          {
            "code" : "RHU"
          },
          {
            "code" : "SLEEP"
          },
          {
            "code" : "NCCF"
          },
          {
            "code" : "SNF"
          },
          {
            "code" : "OF"
          },
          {
            "code" : "ALL"
          },
          {
            "code" : "AMPUT"
          },
          {
            "code" : "BMTC"
          },
          {
            "code" : "BREAST"
          },
          {
            "code" : "CANC"
          },
          {
            "code" : "CAPC"
          },
          {
            "code" : "CARD"
          },
          {
            "code" : "PEDCARD"
          },
          {
            "code" : "COAG"
          },
          {
            "code" : "CRS"
          },
          {
            "code" : "DERM"
          },
          {
            "code" : "ENDO"
          },
          {
            "code" : "PEDE"
          },
          {
            "code" : "ENT"
          },
          {
            "code" : "FMC"
          },
          {
            "code" : "GI"
          },
          {
            "code" : "PEDGI"
          },
          {
            "code" : "GIM"
          },
          {
            "code" : "GYN"
          },
          {
            "code" : "HEM"
          },
          {
            "code" : "PEDHEM"
          },
          {
            "code" : "HTN"
          },
          {
            "code" : "IEC"
          },
          {
            "code" : "INFD"
          },
          {
            "code" : "PEDID"
          },
          {
            "code" : "INV"
          },
          {
            "code" : "LYMPH"
          },
          {
            "code" : "MGEN"
          },
          {
            "code" : "NEPH"
          },
          {
            "code" : "PEDNEPH"
          },
          {
            "code" : "NEUR"
          },
          {
            "code" : "OB"
          },
          {
            "code" : "OMS"
          },
          {
            "code" : "ONCL"
          },
          {
            "code" : "PEDHO"
          },
          {
            "code" : "OPH"
          },
          {
            "code" : "OPTC"
          },
          {
            "code" : "ORTHO"
          },
          {
            "code" : "HAND"
          },
          {
            "code" : "PAINCL"
          },
          {
            "code" : "PC"
          },
          {
            "code" : "PEDC"
          },
          {
            "code" : "PEDRHEUM"
          },
          {
            "code" : "POD"
          },
          {
            "code" : "PREV"
          },
          {
            "code" : "PROCTO"
          },
          {
            "code" : "PROFF"
          },
          {
            "code" : "PROS"
          },
          {
            "code" : "PSI"
          },
          {
            "code" : "PSY"
          },
          {
            "code" : "RHEUM"
          },
          {
            "code" : "SPMED"
          },
          {
            "code" : "SU"
          },
          {
            "code" : "PLS"
          },
          {
            "code" : "URO"
          },
          {
            "code" : "TR"
          },
          {
            "code" : "TRAVEL"
          },
          {
            "code" : "WND"
          },
          {
            "code" : "RTF"
          },
          {
            "code" : "PRC"
          },
          {
            "code" : "SURF"
          },
          {
            "code" : "DADDR"
          },
          {
            "code" : "MOBL"
          },
          {
            "code" : "AMB"
          },
          {
            "code" : "PHARM"
          },
          {
            "code" : "ACC"
          },
          {
            "code" : "COMM"
          },
          {
            "code" : "CSC"
          },
          {
            "code" : "PTRES"
          },
          {
            "code" : "SCHOOL"
          },
          {
            "code" : "UPC"
          },
          {
            "code" : "WORK"
          }
        ]
      }
    ]
  }
}

```
