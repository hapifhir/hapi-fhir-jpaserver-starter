# SXCM_TS: GeneralTimingSpecification (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **SXCM_TS: GeneralTimingSpecification (V3 Data Type)**

## Logical Model: SXCM_TS: GeneralTimingSpecification (V3 Data Type) ( Abstract ) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:SXCM_TS |

 
A set of points in time, specifying the timing of events and actions and the cyclical validity-patterns that may exist for certain kinds of information, such as phone numbers (evening, daytime), addresses (so called "snowbirds," residing closer to the equator during winter and farther from the equator during summer) and office hours. 

**Usages:**

* Derived from this Logical Model: [EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)](StructureDefinition-EIVL-TS.md), [IVL_TS: Interval (V3 Data Type)](StructureDefinition-IVL-TS.md), [PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)](StructureDefinition-PIVL-TS.md) and [SXPR_TS: Component part of GTS (V3 Data Type)](StructureDefinition-SXPR-TS.md)
* Use this Logical Model: [SXPR_TS: Component part of GTS (V3 Data Type)](StructureDefinition-SXPR-TS.md), [SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md) and [Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/SXCM-TS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-SXCM-TS.csv), [Excel](StructureDefinition-SXCM-TS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "SXCM-TS",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "SXCM_TS",
  "title" : "SXCM_TS: GeneralTimingSpecification (V3 Data Type)",
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
  "description" : "A set of points in time, specifying the timing of events and actions and the cyclical validity-patterns that may exist for certain kinds of information, such as phone numbers (evening, daytime), addresses (so called \"snowbirds,\" residing closer to the equator during winter and farther from the equator during summer) and office hours.",
  "fhirVersion" : "5.0.0",
  "mapping" : [
    {
      "identity" : "rim",
      "uri" : "http://hl7.org/v3",
      "name" : "RIM Mapping"
    }
  ],
  "kind" : "logical",
  "abstract" : true,
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM_TS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/TS",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "SXCM_TS",
        "path" : "SXCM_TS",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "SXCM_TS.operator",
        "path" : "SXCM_TS.operator",
        "representation" : ["xmlAttr"],
        "definition" : "A code specifying whether the set component is included (union) or excluded (set-difference) from the set, or other set operations with the current set component and the set as constructed from the representation stream up to the current point.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "I",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDASetOperator"
        }
      }
    ]
  }
}

```
