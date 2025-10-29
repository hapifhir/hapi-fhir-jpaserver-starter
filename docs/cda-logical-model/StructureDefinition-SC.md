# SC: CharacterStringWithCode (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **SC: CharacterStringWithCode (V3 Data Type)**

## Logical Model: SC: CharacterStringWithCode (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/SC | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:SC |

 
A character string that optionally may have a code attached. The text must always be present if a code is present. The code is often a local code. 

**Usages:**

* Use this Logical Model: [AuthoringDevice (CDA Class)](StructureDefinition-AuthoringDevice.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [Device (CDA Class)](StructureDefinition-Device.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md)...Show 2 more,[Observation (CDA Class)](StructureDefinition-Observation.md)and[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/SC)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-SC.csv), [Excel](StructureDefinition-SC.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "SC",
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
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-binding-style",
      "valueCode" : "CDA"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/SC",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "SC",
  "title" : "SC: CharacterStringWithCode (V3 Data Type)",
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
  "description" : "A character string that optionally may have a code attached. The text must always be present if a code is present. The code is often a local code.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/SC",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ST",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "SC",
        "path" : "SC",
        "definition" : "An ST that optionally may have a code attached. The text must always be present if a code is present. The code is often a local code.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "SC.code",
        "path" : "SC.code",
        "representation" : ["xmlAttr"],
        "label" : "Code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ]
      },
      {
        "id" : "SC.codeSystem",
        "path" : "SC.codeSystem",
        "representation" : ["xmlAttr"],
        "label" : "Code System",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : [
              "http://hl7.org/cda/stds/core/StructureDefinition/oid",
              "http://hl7.org/cda/stds/core/StructureDefinition/uuid",
              "http://hl7.org/cda/stds/core/StructureDefinition/ruid"
            ]
          }
        ]
      },
      {
        "id" : "SC.codeSystemName",
        "path" : "SC.codeSystemName",
        "representation" : ["xmlAttr"],
        "label" : "Code System Name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      },
      {
        "id" : "SC.codeSystemVersion",
        "path" : "SC.codeSystemVersion",
        "representation" : ["xmlAttr"],
        "label" : "Code System Version",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      },
      {
        "id" : "SC.displayName",
        "path" : "SC.displayName",
        "representation" : ["xmlAttr"],
        "label" : "Display Name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      }
    ]
  }
}

```
