# InFulfillmentOf1 (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **InFulfillmentOf1 (CDA Class)**

## Logical Model: InFulfillmentOf1 (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf1 | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:InFulfillmentOf1 |

 
This is an actRelationship called inFulfillmentOf1 that represents the Fulfills General Relationship Operator in QDM 4.1.x in QDM-Base QRDA Category 1, R3. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [Procedure (CDA Class)](StructureDefinition-Procedure.md)...Show 2 more,[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/InFulfillmentOf1)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-InFulfillmentOf1.csv), [Excel](StructureDefinition-InFulfillmentOf1.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "InFulfillmentOf1",
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
      "valueUri" : "urn:hl7-org:sdtc"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "inFulfillmentOf"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf1",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "InFulfillmentOf1",
  "title" : "InFulfillmentOf1 (CDA Class)",
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
  "description" : "This is an actRelationship called inFulfillmentOf1 that represents the Fulfills General Relationship Operator in QDM 4.1.x in QDM-Base QRDA Category 1, R3.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf1",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "InFulfillmentOf1",
        "path" : "InFulfillmentOf1",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "InFulfillmentOf1.typeCode",
        "path" : "InFulfillmentOf1.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "FLFS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActRelationshipFulfills|2.0.0"
        }
      },
      {
        "id" : "InFulfillmentOf1.inversionInd",
        "path" : "InFulfillmentOf1.inversionInd",
        "representation" : ["xmlAttr"],
        "definition" : "Can be set to \"true\" to indicate that the relationship should be interpreted as if the roles of the source and target entries were reversed.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      },
      {
        "id" : "InFulfillmentOf1.negationInd",
        "path" : "InFulfillmentOf1.negationInd",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      },
      {
        "id" : "InFulfillmentOf1.actReference",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "InFulfillmentOf1.actReference",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "InFulfillmentOf1.actReference.classCode",
        "path" : "InFulfillmentOf1.actReference.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ]
      },
      {
        "id" : "InFulfillmentOf1.actReference.moodCode",
        "path" : "InFulfillmentOf1.actReference.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ]
      },
      {
        "id" : "InFulfillmentOf1.actReference.determinerCode",
        "path" : "InFulfillmentOf1.actReference.determinerCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "INSTANCE"
      },
      {
        "id" : "InFulfillmentOf1.actReference.id",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          }
        ],
        "path" : "InFulfillmentOf1.actReference.id",
        "min" : 0,
        "max" : "*",
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
