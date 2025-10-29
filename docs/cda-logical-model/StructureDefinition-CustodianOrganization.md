# CustodianOrganization (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CustodianOrganization (CDA Class)**

## Logical Model: CustodianOrganization (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CustodianOrganization | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CustodianOrganization |

 
The steward organization (CustodianOrganization class) is an entity scoping the role of AssignedCustodian, and has a required CustodianOrganization.id. 

**Usages:**

* Use this Logical Model: [AssignedCustodian (CDA Class)](StructureDefinition-AssignedCustodian.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CustodianOrganization)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CustodianOrganization.csv), [Excel](StructureDefinition-CustodianOrganization.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CustodianOrganization",
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
      "valueString" : "custodianOrganization"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CustodianOrganization",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CustodianOrganization",
  "title" : "CustodianOrganization (CDA Class)",
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
  "description" : "The steward organization (CustodianOrganization class) is an entity scoping the role of AssignedCustodian, and has a required CustodianOrganization.id.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CustodianOrganization",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CustodianOrganization",
        "path" : "CustodianOrganization",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "CustodianOrganization.classCode",
        "path" : "CustodianOrganization.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "ORG",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-EntityClassOrganization"
        }
      },
      {
        "id" : "CustodianOrganization.determinerCode",
        "path" : "CustodianOrganization.determinerCode",
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
        "id" : "CustodianOrganization.id",
        "path" : "CustodianOrganization.id",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "CustodianOrganization.name",
        "path" : "CustodianOrganization.name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ON"
          }
        ]
      },
      {
        "id" : "CustodianOrganization.telecom",
        "path" : "CustodianOrganization.telecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "CustodianOrganization.sdtcTelecom",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "telecom"
          }
        ],
        "path" : "CustodianOrganization.sdtcTelecom",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          }
        ]
      },
      {
        "id" : "CustodianOrganization.addr",
        "path" : "CustodianOrganization.addr",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          }
        ]
      }
    ]
  }
}

```
