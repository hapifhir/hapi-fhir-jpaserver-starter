# Informant (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Informant (CDA Class)**

## Logical Model: Informant (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Informant | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Informant |

 
An informant (or source of information) is a person that provides relevant information, such as the parent of a comatose patient who describes the patient's behavior prior to the onset of coma. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md)...Show 7 more,[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[Section (CDA Class)](StructureDefinition-Section.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Informant)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Informant.csv), [Excel](StructureDefinition-Informant.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Informant",
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
      "valueString" : "informant"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Informant",
  "title" : "Informant (CDA Class)",
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
  "description" : "An informant (or source of information) is a person that provides relevant information, such as the parent of a comatose patient who describes the patient's behavior prior to the onset of coma.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Informant",
        "path" : "Informant",
        "min" : 1,
        "max" : "1",
        "constraint" : [
          {
            "key" : "informant-entity",
            "severity" : "error",
            "human" : "AssignedEntity and RelatedEntity are mutually exclusive (one must be present)",
            "expression" : "(assignedEntity | relatedEntity).count() = 1"
          }
        ]
      },
      {
        "id" : "Informant.typeCode",
        "path" : "Informant.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "INF",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "Informant.contextControlCode",
        "path" : "Informant.contextControlCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "OP",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAContextControl"
        }
      },
      {
        "id" : "Informant.assignedEntity",
        "path" : "Informant.assignedEntity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity"
          }
        ]
      },
      {
        "id" : "Informant.relatedEntity",
        "path" : "Informant.relatedEntity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RelatedEntity"
          }
        ]
      }
    ]
  }
}

```
