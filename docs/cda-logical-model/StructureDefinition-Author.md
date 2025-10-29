# Author (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Author (CDA Class)**

## Logical Model: Author (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Author | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Author |

 
Represents the humans and/or machines that authored the document. In some cases, the role or function of the author is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is "Medical Student Progress Note". The role of the author can also be recorded in the Author.functionCode or AssignedAuthor.code attribute. If either of these attributes is included, they should be equivalent to or further specialize the role inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply "Physician Progress Note" and the value of Author.functionCode is "rounding physician"), and shall not conflict with the role inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [Encounter (CDA Class)](StructureDefinition-Encounter.md), [ExternalAct (CDA Class)](StructureDefinition-ExternalAct.md)...Show 11 more,[ExternalDocument (CDA Class)](StructureDefinition-ExternalDocument.md),[ExternalObservation (CDA Class)](StructureDefinition-ExternalObservation.md),[ExternalProcedure (CDA Class)](StructureDefinition-ExternalProcedure.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[Section (CDA Class)](StructureDefinition-Section.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Author)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Author.csv), [Excel](StructureDefinition-Author.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Author",
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
      "valueString" : "author"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Author",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Author",
  "title" : "Author (CDA Class)",
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
  "description" : "Represents the humans and/or machines that authored the document. In some cases, the role or function of the author is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"Medical Student Progress Note\". The role of the author can also be recorded in the Author.functionCode or AssignedAuthor.code attribute. If either of these attributes is included, they should be equivalent to or further specialize the role inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply \"Physician Progress Note\" and the value of Author.functionCode is \"rounding physician\"), and shall not conflict with the role inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Author",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Author",
        "path" : "Author",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Author.typeCode",
        "path" : "Author.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "AUT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "Author.contextControlCode",
        "path" : "Author.contextControlCode",
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
        "id" : "Author.functionCode",
        "path" : "Author.functionCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "Author.time",
        "path" : "Author.time",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "Author.assignedAuthor",
        "path" : "Author.assignedAuthor",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedAuthor"
          }
        ]
      }
    ]
  }
}

```
