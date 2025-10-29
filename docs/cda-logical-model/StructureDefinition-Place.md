# Place (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Place (CDA Class)**

## Logical Model: Place (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Place | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Place |

 
A bounded physical place or site, including any contained structures. 
Place may be natural or man-made. The geographic position of a place may or may not be constant. Places may be work facilities (where relevant acts occur), homes (where people live) or offices (where people work). Places may contain sub-places (floor, room, booth, bed). Places may also be sites that are investigated in the context of health care, social work, public health administration (e.g., buildings, picnic grounds, day care centers, prisons, counties, states, and other focuses of epidemiological events). 
Examples: A field, lake, city, county, state, country, lot (land), building, pipeline, power line, playground, ship, truck 

**Usages:**

* Use this Logical Model: [Birthplace (CDA Class)](StructureDefinition-Birthplace.md) and [HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Place)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Place.csv), [Excel](StructureDefinition-Place.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Place",
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
      "valueString" : "place"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Place",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Place",
  "title" : "Place (CDA Class)",
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
  "description" : "A bounded physical place or site, including any contained structures.\n\nPlace may be natural or man-made. The geographic position of a place may or may not be constant. Places may be work facilities (where relevant acts occur), homes (where people live) or offices (where people work). Places may contain sub-places (floor, room, booth, bed). Places may also be sites that are investigated in the context of health care, social work, public health administration (e.g., buildings, picnic grounds, day care centers, prisons, counties, states, and other focuses of epidemiological events).\n\nExamples: A field, lake, city, county, state, country, lot (land), building, pipeline, power line, playground, ship, truck",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Place",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Place",
        "path" : "Place",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Place.classCode",
        "path" : "Place.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "PLC",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-EntityClassPlace"
        }
      },
      {
        "id" : "Place.determinerCode",
        "path" : "Place.determinerCode",
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
        "id" : "Place.name",
        "path" : "Place.name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          }
        ]
      },
      {
        "id" : "Place.addr",
        "path" : "Place.addr",
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
