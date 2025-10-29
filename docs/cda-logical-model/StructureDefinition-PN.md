# PN: PersonName (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PN: PersonName (V3 Data Type)**

## Logical Model: PN: PersonName (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PN | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:PN |

 
A name for a person. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for person name values are "Jim Bob Walton, Jr.", "Adam Everyman", etc. A person name may be as simple as a character string or may consist of several person name parts, such as, "Jim", "Bob", "Walton", and "Jr.". PN differs from EN because the qualifier type cannot include LS (Legal Status). 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md), [Patient (CDA Class)](StructureDefinition-Patient.md), [Person (CDA Class)](StructureDefinition-Person.md)...Show 2 more,[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md)and[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PN)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PN.csv), [Excel](StructureDefinition-PN.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PN",
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
      "valueString" : "name"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PN",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "PN",
  "title" : "PN: PersonName (V3 Data Type)",
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
  "description" : "A name for a person. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for person name values are \"Jim Bob Walton, Jr.\", \"Adam Everyman\", etc. A person name may be as simple as a character string or may consist of several person name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\". PN differs from EN because the qualifier type cannot include LS (Legal Status).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PN",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/EN",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PN",
        "path" : "PN",
        "definition" : "A name for a person. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for person name values are \"Jim Bob Walton, Jr.\", \"Adam Everyman\", etc. A person name may be as simple as a character string or may consist of several person name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\". PN differs from EN because the qualifier type cannot include LS (Legal Status).",
        "constraint" : [
          {
            "key" : "pn-no-ls",
            "severity" : "error",
            "human" : "No PN name part may have a qualifier of LS.",
            "expression" : "(item.delimiter | item.family | item.given | item.prefix | item.suffix).where(qualifier.where($this = 'LS').exists()).empty()"
          }
        ]
      },
      {
        "id" : "PN.item",
        "path" : "PN.item",
        "min" : 0,
        "max" : "*"
      }
    ]
  }
}

```
