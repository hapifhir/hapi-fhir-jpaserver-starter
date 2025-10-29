# SXPR_TS: Component part of GTS (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **SXPR_TS: Component part of GTS (V3 Data Type)**

## Logical Model: SXPR_TS: Component part of GTS (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:SXPR_TS |

 
A set-component that is itself made up of set-components that are evaluated as one value 

**Usages:**

* Use this Logical Model: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md)...Show 5 more,[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[SXPR_TS: Component part of GTS (V3 Data Type)](StructureDefinition-SXPR-TS.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md),[Supply (CDA Class)](StructureDefinition-Supply.md)and[TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/SXPR-TS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-SXPR-TS.csv), [Excel](StructureDefinition-SXPR-TS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "SXPR-TS",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "SXPR_TS",
  "title" : "SXPR_TS: Component part of GTS (V3 Data Type)",
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
  "description" : "A set-component that is itself made up of set-components that are evaluated as one value",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR_TS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "SXPR_TS",
        "path" : "SXPR_TS",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "SXPR_TS.comp",
        "path" : "SXPR_TS.comp",
        "representation" : ["typeAttr"],
        "min" : 2,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ]
      }
    ]
  }
}

```
