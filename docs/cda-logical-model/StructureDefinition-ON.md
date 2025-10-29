# ON: OrganizationName (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ON: OrganizationName (V3 Data Type)**

## Logical Model: ON: OrganizationName (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ON | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ON |

 
A name for an organization. A sequence of name parts. Examples for organization name values are "Health Level Seven, Inc.", "Hospital", etc. An organization name may be as simple as a character string or may consist of several person name parts, such as, "Health Level 7", "Inc.". ON differs from EN because certain person related name parts are not possible. 

**Usages:**

* Use this Logical Model: [CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md) and [Organization (CDA Class)](StructureDefinition-Organization.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ON)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ON.csv), [Excel](StructureDefinition-ON.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ON",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ON",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ON",
  "title" : "ON: OrganizationName (V3 Data Type)",
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
  "description" : "A name for an organization. A sequence of name parts. Examples for organization name values are \"Health Level Seven, Inc.\", \"Hospital\", etc. An organization name may be as simple as a character string or may consist of several person name parts, such as, \"Health Level 7\", \"Inc.\". ON differs from EN because certain person related name parts are not possible.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ON",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/EN",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ON",
        "path" : "ON",
        "definition" : "A name for an organization. A sequence of name parts. Examples for organization name values are \"Health Level Seven, Inc.\", \"Hospital\", etc. An organization name may be as simple as a character string or may consist of several person name parts, such as, \"Health Level 7\", \"Inc.\". ON differs from EN because certain person related name parts are not possible.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "ON.item.family",
        "path" : "ON.item.family",
        "max" : "0"
      },
      {
        "id" : "ON.item.given",
        "path" : "ON.item.given",
        "max" : "0"
      }
    ]
  }
}

```
