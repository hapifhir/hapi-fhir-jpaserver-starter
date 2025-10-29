# CR: ConceptRole (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CR: ConceptRole (V3 Data Type)**

## Logical Model: CR: ConceptRole (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CR | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CR |

 
A concept qualifier code with optionally named role. Both qualifier role and value codes must be defined by the coding system of the CD containing the concept qualifier. For example, if SNOMED RT defines a concept "leg", a role relation "has-laterality", and another concept "left", the concept role relation allows to add the qualifier "has-laterality: left" to a primary code "leg" to construct the meaning "left leg". 

**Usages:**

* Use this Logical Model: [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md) and [CE: CodedWithEquivalents (V3 Data Type)](StructureDefinition-CE.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CR)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CR.csv), [Excel](StructureDefinition-CR.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CR",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CR",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CR",
  "title" : "CR: ConceptRole (V3 Data Type)",
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
  "description" : "A concept qualifier code with optionally named role. Both qualifier role and value codes must be defined by the coding system of the CD containing the concept qualifier. For example, if SNOMED RT defines a concept \"leg\", a role relation \"has-laterality\", and another concept \"left\", the concept role relation allows to add the qualifier \"has-laterality: left\" to a primary code \"leg\" to construct the meaning \"left leg\".",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CR",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CR",
        "path" : "CR",
        "definition" : "A concept qualifier code with optionally named role. Both qualifier role and value codes must be defined by the coding system. For example, if SNOMED RT defines a concept \"leg\", a role relation \"has-laterality\", and another concept \"left\", the concept role relation allows to add the qualifier \"has-laterality: left\" to a primary code \"leg\" to construct the meaning \"left leg\".",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "value-null-cr",
            "severity" : "error",
            "human" : "Must contain value or nullFlavor. If nullFlavor is present, name and value must not be present.",
            "expression" : "(value.exists() or nullFlavor.exists()) and (nullFlavor.exists() implies (name | value).empty())"
          }
        ]
      },
      {
        "id" : "CR.inverted",
        "path" : "CR.inverted",
        "representation" : ["xmlAttr"],
        "label" : "Inversion Indicator",
        "definition" : "Indicates if the sense of the role name is inverted. This can be used in cases where the underlying code system defines inversion but does not provide reciprocal pairs of role names. By default, inverted is false.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bn"]
          }
        ]
      },
      {
        "id" : "CR.name",
        "path" : "CR.name",
        "label" : "Name",
        "definition" : "Specifies the manner in which the concept role value contributes to the meaning of a code phrase. For example, if SNOMED RT defines a concept \"leg\", a role relation \"has-laterality\", and another concept \"left\", the concept role relation allows to add the qualifier \"has-laterality: left\" to a primary code \"leg\" to construct the meaning \"left leg\". In this example \"has-laterality\" is the CR.name.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CV"
          }
        ]
      },
      {
        "id" : "CR.value",
        "path" : "CR.value",
        "label" : "Value",
        "definition" : "The concept that modifies the primary code of a code phrase through the role relation. For example, if SNOMED RT defines a concept \"leg\", a role relation \"has-laterality\", and another concept \"left\", the concept role relation allows adding the qualifier \"has-laterality: left\" to a primary code \"leg\" to construct the meaning \"left leg\". In this example \"left\" is the CR.value.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      }
    ]
  }
}

```
