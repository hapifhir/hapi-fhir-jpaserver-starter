# MaintainedEntity (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **MaintainedEntity (CDA Class)**

## Logical Model: MaintainedEntity (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/MaintainedEntity | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:MaintainedEntity |

 
The MaintainedEntity class is present for backwards compatibility, and its use is discouraged, except where needed to support the transformation of CDA, Release One documents. 
NOTE: In CDA, Release One, it was possible to specify those individuals responsible for the device. This functionality has been deprecated in CDA, Release Two. 

**Usages:**

* Use this Logical Model: [AuthoringDevice (CDA Class)](StructureDefinition-AuthoringDevice.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/MaintainedEntity)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-MaintainedEntity.csv), [Excel](StructureDefinition-MaintainedEntity.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "MaintainedEntity",
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
      "valueString" : "maintainedEntity"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/MaintainedEntity",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "MaintainedEntity",
  "title" : "MaintainedEntity (CDA Class)",
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
  "description" : "The MaintainedEntity class is present for backwards compatibility, and its use is discouraged, except where needed to support the transformation of CDA, Release One documents.\n\nNOTE: In CDA, Release One, it was possible to specify those individuals responsible for the device. This functionality has been deprecated in CDA, Release Two.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/MaintainedEntity",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "MaintainedEntity",
        "path" : "MaintainedEntity",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "MaintainedEntity.classCode",
        "path" : "MaintainedEntity.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "MNT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RoleClassPassive"
        }
      },
      {
        "id" : "MaintainedEntity.effectiveTime",
        "path" : "MaintainedEntity.effectiveTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "MaintainedEntity.maintainingPerson",
        "path" : "MaintainedEntity.maintainingPerson",
        "min" : 1,
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
