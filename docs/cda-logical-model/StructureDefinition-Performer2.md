# Performer2 (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Performer2 (CDA Class)**

## Logical Model: Performer2 (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Performer2 | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Performer2 |

 
TODO 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)...Show 5 more,[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Performer2)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Performer2.csv), [Excel](StructureDefinition-Performer2.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Performer2",
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
      "valueString" : "performer"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Performer2",
  "title" : "Performer2 (CDA Class)",
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
  "description" : "TODO",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Performer2",
        "path" : "Performer2",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Performer2.typeCode",
        "path" : "Performer2.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "PRF",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ParticipationPhysicalPerformer"
        }
      },
      {
        "id" : "Performer2.sdtcFunctionCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "functionCode"
          }
        ],
        "path" : "Performer2.sdtcFunctionCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "Performer2.time",
        "path" : "Performer2.time",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "Performer2.modeCode",
        "path" : "Performer2.modeCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "Performer2.assignedEntity",
        "path" : "Performer2.assignedEntity",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity"
          }
        ]
      }
    ]
  }
}

```
