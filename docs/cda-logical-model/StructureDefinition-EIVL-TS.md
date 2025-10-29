# EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)**

## Logical Model: EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:EIVL_TS |

 
Specifies a periodic interval of time where the recurrence is based on activities of daily living or other important events that are time-related but not fully determined by time. 

**Usages:**

* Use this Logical Model: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md)...Show 5 more,[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[SXPR_TS: Component part of GTS (V3 Data Type)](StructureDefinition-SXPR-TS.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md),[Supply (CDA Class)](StructureDefinition-Supply.md)and[TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/EIVL-TS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-EIVL-TS.csv), [Excel](StructureDefinition-EIVL-TS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "EIVL-TS",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "EIVL_TS",
  "title" : "EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)",
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
  "description" : "Specifies a periodic interval of time where the recurrence is based on activities of daily living or other important events that are time-related but not fully determined by time.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL_TS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "EIVL_TS",
        "path" : "EIVL_TS",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "EIVL_TS.value",
        "path" : "EIVL_TS.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "EIVL_TS.event",
        "path" : "EIVL_TS.event",
        "label" : "Event",
        "definition" : "A code for a common (periodical) activity of daily living based on which the event related periodic interval is specified.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "EIVL_TS.offset",
        "path" : "EIVL_TS.offset",
        "label" : "Offset",
        "definition" : "An interval of elapsed time (duration, not absolute point in time) that marks the offsets for the beginning, width and end of the event-related periodic interval measured from the time each such event actually occurred.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          }
        ]
      }
    ]
  }
}

```
