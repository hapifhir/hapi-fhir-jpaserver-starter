# CDAParticipationType - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAParticipationType**

## ValueSet: CDAParticipationType 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAParticipationType |

 
A code specifying the meaning and purpose of every Participation instance. Each of its values implies specific constraints on the Roles undertaking the participation. Limited to values allowed in original CDA definition 

 **References** 

* [Authenticator (CDA Class)](StructureDefinition-Authenticator.md)
* [Author (CDA Class)](StructureDefinition-Author.md)
* [Custodian (CDA Class)](StructureDefinition-Custodian.md)
* [DataEnterer (CDA Class)](StructureDefinition-DataEnterer.md)
* [EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md)
* [Informant (CDA Class)](StructureDefinition-Informant.md)
* [LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md)
* [Participant1 (CDA Class)](StructureDefinition-Participant1.md)
* [Participant2 (CDA Class)](StructureDefinition-Participant2.md)
* [RecordTarget (CDA Class)](StructureDefinition-RecordTarget.md)
* [Specimen (CDA Class)](StructureDefinition-Specimen.md)
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
  "id" : "CDAParticipationType",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAParticipationType",
  "title" : "CDAParticipationType",
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
  "description" : "A code specifying the meaning and purpose of every Participation instance. Each of its values implies specific constraints on the Roles undertaking the participation. Limited to values allowed in original CDA definition",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-ParticipationType",
        "concept" : [
          {
            "code" : "ADM"
          },
          {
            "code" : "ATND"
          },
          {
            "code" : "CALLBCK"
          },
          {
            "code" : "CON"
          },
          {
            "code" : "DIS"
          },
          {
            "code" : "ESC"
          },
          {
            "code" : "REF"
          },
          {
            "code" : "IND"
          },
          {
            "code" : "BEN"
          },
          {
            "code" : "COV"
          },
          {
            "code" : "HLD"
          },
          {
            "code" : "RCT"
          },
          {
            "code" : "RCV"
          },
          {
            "code" : "AUT"
          },
          {
            "code" : "ENT"
          },
          {
            "code" : "INF"
          },
          {
            "code" : "WIT"
          },
          {
            "code" : "IRCP"
          },
          {
            "code" : "NOT"
          },
          {
            "code" : "PRCP"
          },
          {
            "code" : "REFB"
          },
          {
            "code" : "REFT"
          },
          {
            "code" : "TRC"
          },
          {
            "code" : "PRF"
          },
          {
            "code" : "DIST"
          },
          {
            "code" : "PPRF"
          },
          {
            "code" : "SPRF"
          },
          {
            "code" : "DEV"
          },
          {
            "code" : "NRD"
          },
          {
            "code" : "RDV"
          },
          {
            "code" : "SBJ"
          },
          {
            "code" : "SPC"
          },
          {
            "code" : "DIR"
          },
          {
            "code" : "BBY"
          },
          {
            "code" : "CSM"
          },
          {
            "code" : "DON"
          },
          {
            "code" : "PRD"
          },
          {
            "code" : "LOC"
          },
          {
            "code" : "DST"
          },
          {
            "code" : "ELOC"
          },
          {
            "code" : "ORG"
          },
          {
            "code" : "RML"
          },
          {
            "code" : "VIA"
          },
          {
            "code" : "VRF"
          },
          {
            "code" : "AUTHEN"
          },
          {
            "code" : "LA"
          },
          {
            "code" : "RESP"
          },
          {
            "code" : "CST"
          }
        ]
      }
    ]
  }
}

```
