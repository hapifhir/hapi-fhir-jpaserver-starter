# Reference (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Reference (CDA Class)**

## Logical Model: Reference (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Reference | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Reference |

 
CDA entries can reference external objects such as external images and prior reports. These external objects are not part of the authenticated document content. They contain sufficient attributes to enable an explicit reference rather than duplicating the entire referenced object. The CDA entry that wraps the external reference can be used to encode the specific portions of the external reference that are addressed in the narrative block. 
Each object allows for an identifier and a code, and contains the RIM Act.text attribute, which can be used to store the URL and MIME type of the object. External objects always have a fixed moodCode of "EVN". 
The reference class contains the attribute reference.seperatableInd, which indicates whether or not the source is intended to be interpreted independently of the target. The indicator cannot prevent an individual or application from separating the source and target, but indicates the author's desire and willingness to attest to the content of the source if separated from the target. Typically, where seperatableInd is "false", the exchanged package should include the target of the reference so that the recipient can render it. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md)...Show 5 more,[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Reference)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Reference.csv), [Excel](StructureDefinition-Reference.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Reference",
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
      "valueString" : "reference"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Reference",
  "title" : "Reference (CDA Class)",
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
  "description" : "CDA entries can reference external objects such as external images and prior reports. These external objects are not part of the authenticated document content. They contain sufficient attributes to enable an explicit reference rather than duplicating the entire referenced object. The CDA entry that wraps the external reference can be used to encode the specific portions of the external reference that are addressed in the narrative block.\n\nEach object allows for an identifier and a code, and contains the RIM Act.text attribute, which can be used to store the URL and MIME type of the object. External objects always have a fixed moodCode of \"EVN\".\n\nThe reference class contains the attribute reference.seperatableInd, which indicates whether or not the source is intended to be interpreted independently of the target. The indicator cannot prevent an individual or application from separating the source and target, but indicates the author's desire and willingness to attest to the content of the source if separated from the target. Typically, where seperatableInd is \"false\", the exchanged package should include the target of the reference so that the recipient can render it.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Reference",
        "path" : "Reference",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "reference-external",
            "severity" : "error",
            "human" : "Must contain one (and only one) external reference",
            "expression" : "(externalAct | externalObservation | externalProcedure | externalDocument).count() = 1"
          }
        ]
      },
      {
        "id" : "Reference.typeCode",
        "path" : "Reference.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xActRelationshipExternalReference"
        }
      },
      {
        "id" : "Reference.seperatableInd",
        "path" : "Reference.seperatableInd",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          }
        ]
      },
      {
        "id" : "Reference.externalAct",
        "path" : "Reference.externalAct",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ExternalAct"
          }
        ]
      },
      {
        "id" : "Reference.externalObservation",
        "path" : "Reference.externalObservation",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ExternalObservation"
          }
        ]
      },
      {
        "id" : "Reference.externalProcedure",
        "path" : "Reference.externalProcedure",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ExternalProcedure"
          }
        ]
      },
      {
        "id" : "Reference.externalDocument",
        "path" : "Reference.externalDocument",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ExternalDocument"
          }
        ]
      }
    ]
  }
}

```
