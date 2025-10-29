# Specimen (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Specimen (CDA Class)**

## Logical Model: Specimen (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Specimen | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Specimen |

 
A specimen is a part of some entity, typically the subject, that is the target of focused laboratory, radiology or other observations. In many clinical observations, such as physical examination of a patient, the patient is the subject of the observation, and there is no specimen. The specimen participant is only used when observations are made against some substance or object that is taken or derived from the subject. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)...Show 5 more,[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Specimen)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Specimen.csv), [Excel](StructureDefinition-Specimen.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Specimen",
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
      "valueString" : "specimen"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Specimen",
  "title" : "Specimen (CDA Class)",
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
  "description" : "A specimen is a part of some entity, typically the subject, that is the target of focused laboratory, radiology or other observations. In many clinical observations, such as physical examination of a patient, the patient is the subject of the observation, and there is no specimen. The specimen participant is only used when observations are made against some substance or object that is taken or derived from the subject.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Specimen",
        "path" : "Specimen",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Specimen.typeCode",
        "path" : "Specimen.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "SPC",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "Specimen.specimenRole",
        "path" : "Specimen.specimenRole",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SpecimenRole"
          }
        ]
      }
    ]
  }
}

```
