# Entity (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Entity (CDA Class)**

## Logical Model: Entity (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Entity | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Entity |

 
A physical thing, group of physical things or an organization capable of participating in Acts while in a role. 
An entity is a physical object that has, had or will have existence. The only exception to this is Organization, which while not having a physical presence, fulfills the other characteristics of an Entity. Entity stipulates the thing itself, not the Roles it may play: the Role of Patient, e.g., is played by the Person Entity. 
Living subjects (including human beings), organizations, materials, places and their specializations. 

**Usages:**

* Use this Logical Model: [ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Entity)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Entity.csv), [Excel](StructureDefinition-Entity.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Entity",
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
      "valueString" : "entity"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Entity",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Entity",
  "title" : "Entity (CDA Class)",
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
  "description" : "A physical thing, group of physical things or an organization capable of participating in Acts while in a role.\n\nAn entity is a physical object that has, had or will have existence. The only exception to this is Organization, which while not having a physical presence, fulfills the other characteristics of an Entity. Entity stipulates the thing itself, not the Roles it may play: the Role of Patient, e.g., is played by the Person Entity.\n\nLiving subjects (including human beings), organizations, materials, places and their specializations.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Entity",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Entity",
        "path" : "Entity",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Entity.classCode",
        "path" : "Entity.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "ENT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-EntityClassRoot"
        }
      },
      {
        "id" : "Entity.determinerCode",
        "path" : "Entity.determinerCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "INSTANCE",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-EntityDeterminer"
        }
      },
      {
        "id" : "Entity.id",
        "path" : "Entity.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Entity.code",
        "path" : "Entity.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAEntityCode"
        }
      },
      {
        "id" : "Entity.desc",
        "path" : "Entity.desc",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      }
    ]
  }
}

```
