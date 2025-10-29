# Guardian (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Guardian (CDA Class)**

## Logical Model: Guardian (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Guardian | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Guardian |

 
A patient's guardian is a person or organization in the role of guardian (Guardian class). The entity playing the role of guardian is a person (Person class) or organization (Organization class). The entity scoping the role is the patient (Patient class). 
Where a guardian is not explicitly stated, the value should default to local business practice (e.g. the patient makes their own health care decisions unless incapacitated in which case healthcare decisions are made by the patient's spouse). 

**Usages:**

* Use this Logical Model: [Patient (CDA Class)](StructureDefinition-Patient.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Guardian)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Guardian.csv), [Excel](StructureDefinition-Guardian.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Guardian",
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
      "valueString" : "guardian"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Guardian",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Guardian",
  "title" : "Guardian (CDA Class)",
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
  "description" : "A patient's guardian is a person or organization in the role of guardian (Guardian class). The entity playing the role of guardian is a person (Person class) or organization (Organization class). The entity scoping the role is the patient (Patient class).\n\nWhere a guardian is not explicitly stated, the value should default to local business practice (e.g. the patient makes their own health care decisions unless incapacitated in which case healthcare decisions are made by the patient's spouse).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Guardian",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Guardian",
        "path" : "Guardian",
        "min" : 1,
        "max" : "1",
        "constraint" : [
          {
            "key" : "guardian-choice",
            "severity" : "error",
            "human" : "guardianPerson and guardianOrganization are mutually exclusive (one must be present)",
            "expression" : "(guardianPerson | guardianOrganization).count() = 1"
          }
        ]
      },
      {
        "id" : "Guardian.classCode",
        "path" : "Guardian.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "GUARD",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RoleClassAgent"
        }
      },
      {
        "id" : "Guardian.id",
        "path" : "Guardian.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Guardian.sdtcIdentifiedBy",
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
        "path" : "Guardian.sdtcIdentifiedBy",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy"
          }
        ]
      },
      {
        "id" : "Guardian.code",
        "path" : "Guardian.code",
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
        "id" : "Guardian.addr",
        "path" : "Guardian.addr",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          }
        ]
      },
      {
        "id" : "Guardian.telecom",
        "path" : "Guardian.telecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "Guardian.guardianPerson",
        "path" : "Guardian.guardianPerson",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Person"
          }
        ]
      },
      {
        "id" : "Guardian.guardianOrganization",
        "path" : "Guardian.guardianOrganization",
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
