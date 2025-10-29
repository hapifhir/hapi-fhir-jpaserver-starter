# RelatedEntity (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **RelatedEntity (CDA Class)**

## Logical Model: RelatedEntity (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/RelatedEntity | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:RelatedEntity |

 
The RelatedEntity role is used to represent an informant without a role.id (e.g. a parent or guy on the street). 

**Usages:**

* Use this Logical Model: [Informant (CDA Class)](StructureDefinition-Informant.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/RelatedEntity)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-RelatedEntity.csv), [Excel](StructureDefinition-RelatedEntity.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "RelatedEntity",
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
      "valueString" : "relatedEntity"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/RelatedEntity",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "RelatedEntity",
  "title" : "RelatedEntity (CDA Class)",
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
  "description" : "The RelatedEntity role is used to represent an informant without a role.id (e.g. a parent or guy on the street).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/RelatedEntity",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "RelatedEntity",
        "path" : "RelatedEntity",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "RelatedEntity.classCode",
        "path" : "RelatedEntity.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDARoleClassMutualRelationship"
        }
      },
      {
        "id" : "RelatedEntity.code",
        "path" : "RelatedEntity.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-PersonalRelationshipRoleType"
        }
      },
      {
        "id" : "RelatedEntity.addr",
        "path" : "RelatedEntity.addr",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          }
        ]
      },
      {
        "id" : "RelatedEntity.telecom",
        "path" : "RelatedEntity.telecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "RelatedEntity.effectiveTime",
        "path" : "RelatedEntity.effectiveTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "RelatedEntity.relatedPerson",
        "path" : "RelatedEntity.relatedPerson",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Person"
          }
        ]
      }
    ]
  }
}

```
