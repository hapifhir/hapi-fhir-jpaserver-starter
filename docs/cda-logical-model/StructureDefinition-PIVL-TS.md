# PIVL_TS: PeriodicIntervalOfTime (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)**

## Logical Model: PIVL_TS: PeriodicIntervalOfTime (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:PIVL_TS |

 
An interval of time that recurs periodically. Periodic intervals have two properties, phase and period. The phase specifies the "interval prototype" that is repeated every period. 

**Usages:**

* Use this Logical Model: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md)...Show 5 more,[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[SXPR_TS: Component part of GTS (V3 Data Type)](StructureDefinition-SXPR-TS.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md),[Supply (CDA Class)](StructureDefinition-Supply.md)and[TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PIVL-TS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PIVL-TS.csv), [Excel](StructureDefinition-PIVL-TS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PIVL-TS",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "PIVL_TS",
  "title" : "PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)",
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
  "description" : "An interval of time that recurs periodically. Periodic intervals have two properties, phase and period. The phase specifies the \"interval prototype\" that is repeated every period.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL_TS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PIVL_TS",
        "path" : "PIVL_TS",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "PIVL_TS.value",
        "path" : "PIVL_TS.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "PIVL_TS.phase",
        "path" : "PIVL_TS.phase",
        "label" : "Phase",
        "definition" : "A prototype of the repeating interval, specifying the duration of each occurrence and anchors the periodic interval sequence at a certain point in time.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "PIVL_TS.period",
        "path" : "PIVL_TS.period",
        "label" : "Period",
        "definition" : "A time duration specifying as a reciprocal measure of the frequency at which the periodic interval repeats.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "PIVL_TS.alignment",
        "path" : "PIVL_TS.alignment",
        "representation" : ["xmlAttr"],
        "label" : "Alignment to the Calendar",
        "definition" : "Specifies if and how the repetitions are aligned to the cycles of the underlying calendar (e.g., to distinguish every 30 days from \"the 5th of every month\".) A non-aligned periodic interval recurs independently from the calendar. An aligned periodic interval is synchronized with the calendar.",
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
        "id" : "PIVL_TS.institutionSpecified",
        "path" : "PIVL_TS.institutionSpecified",
        "representation" : ["xmlAttr"],
        "label" : "Institution Specified Timing",
        "definition" : "Indicates whether the exact timing is up to the party executing the schedule (e.g., to distinguish \"every 8 hours\" from \"3 times a day\".)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      }
    ]
  }
}

```
