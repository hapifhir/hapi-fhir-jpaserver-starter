# AssignedEntity (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **AssignedEntity (CDA Class)**

## Logical Model: AssignedEntity (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:AssignedEntity |

 
AssignedEntity (CDA Class) 

**Usages:**

* Use this Logical Model: [Authenticator (CDA Class)](StructureDefinition-Authenticator.md), [DataEnterer (CDA Class)](StructureDefinition-DataEnterer.md), [EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md), [EncounterParticipant (CDA Class)](StructureDefinition-EncounterParticipant.md)...Show 4 more,[Informant (CDA Class)](StructureDefinition-Informant.md),[LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md),[Performer1 (CDA Class)](StructureDefinition-Performer1.md)and[Performer2 (CDA Class)](StructureDefinition-Performer2.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/AssignedEntity)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-AssignedEntity.csv), [Excel](StructureDefinition-AssignedEntity.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "AssignedEntity",
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
      "valueString" : "assignedEntity"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "AssignedEntity",
  "title" : "AssignedEntity (CDA Class)",
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
  "description" : "AssignedEntity (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "AssignedEntity",
        "path" : "AssignedEntity",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "AssignedEntity.classCode",
        "path" : "AssignedEntity.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "ASSIGNED",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RoleClassAssignedEntity"
        }
      },
      {
        "id" : "AssignedEntity.id",
        "path" : "AssignedEntity.id",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "AssignedEntity.sdtcIdentifiedBy",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "identifiedBy"
          }
        ],
        "path" : "AssignedEntity.sdtcIdentifiedBy",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy"
          }
        ]
      },
      {
        "id" : "AssignedEntity.code",
        "path" : "AssignedEntity.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDARoleCode"
        }
      },
      {
        "id" : "AssignedEntity.addr",
        "path" : "AssignedEntity.addr",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          }
        ]
      },
      {
        "id" : "AssignedEntity.telecom",
        "path" : "AssignedEntity.telecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "AssignedEntity.assignedPerson",
        "path" : "AssignedEntity.assignedPerson",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Person"
          }
        ]
      },
      {
        "id" : "AssignedEntity.representedOrganization",
        "path" : "AssignedEntity.representedOrganization",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organization"
          }
        ]
      },
      {
        "id" : "AssignedEntity.sdtcPatient",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "patient"
          }
        ],
        "path" : "AssignedEntity.sdtcPatient",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/fhir/StructureDefinition/Base"
          }
        ]
      },
      {
        "id" : "AssignedEntity.sdtcPatient.id",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "AssignedEntity.sdtcPatient.id",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      }
    ]
  }
}

```
