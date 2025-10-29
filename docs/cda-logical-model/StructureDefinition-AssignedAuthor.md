# AssignedAuthor (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **AssignedAuthor (CDA Class)**

## Logical Model: AssignedAuthor (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/AssignedAuthor | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:AssignedAuthor |

 
An author is a person in the role of an assigned author (AssignedAuthor class). 

**Usages:**

* Use this Logical Model: [Author (CDA Class)](StructureDefinition-Author.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/AssignedAuthor)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-AssignedAuthor.csv), [Excel](StructureDefinition-AssignedAuthor.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "AssignedAuthor",
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
      "valueString" : "assignedAuthor"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedAuthor",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "AssignedAuthor",
  "title" : "AssignedAuthor (CDA Class)",
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
  "description" : "An author is a person in the role of an assigned author (AssignedAuthor class).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedAuthor",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "AssignedAuthor",
        "path" : "AssignedAuthor",
        "min" : 1,
        "max" : "1",
        "constraint" : [
          {
            "key" : "assigned-author-choice",
            "severity" : "error",
            "human" : "assignedPerson and assignedAuthoringDevice are mutually exclusive",
            "expression" : "(assignedPerson | assignedAuthoringDevice).count() <= 1"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.classCode",
        "path" : "AssignedAuthor.classCode",
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
        "id" : "AssignedAuthor.id",
        "path" : "AssignedAuthor.id",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.sdtcIdentifiedBy",
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
        "path" : "AssignedAuthor.sdtcIdentifiedBy",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.code",
        "path" : "AssignedAuthor.code",
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
        "id" : "AssignedAuthor.addr",
        "path" : "AssignedAuthor.addr",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.telecom",
        "path" : "AssignedAuthor.telecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.assignedPerson",
        "path" : "AssignedAuthor.assignedPerson",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Person"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.assignedAuthoringDevice",
        "path" : "AssignedAuthor.assignedAuthoringDevice",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AuthoringDevice"
          }
        ]
      },
      {
        "id" : "AssignedAuthor.representedOrganization",
        "path" : "AssignedAuthor.representedOrganization",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organization"
          }
        ]
      }
    ]
  }
}

```
