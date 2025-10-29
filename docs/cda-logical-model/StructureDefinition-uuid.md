# uuid: DCE Universal Unique Identifier - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **uuid: DCE Universal Unique Identifier**

## Data Type Profile: uuid: DCE Universal Unique Identifier 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/uuid | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:uuid |

 
A globally unique string representing a DCE Universal Unique Identifier (UUID) in the common UUID format that consists of 5 hyphen-separated groups of hexadecimal digits having 8, 4, 4, 4, and 12 places respectively. 
****NOTE:****The output of UUID related programs and functions may use all sorts of forms, upper case, lower case, and with or without the hyphens that group the digits. This variate output must be postprocessed to conform to the HL7 specification, i.e., the hyphens must be inserted for the 8-4-4-4-12 grouping. Historically, CDA also required that all hexadecimal digits must be converted to upper case, but due to real-world issues encountered when enforcing this rule, it has been relaxed to allow for upper or lower case letters. Additionally, FHIR requires that UUID's be communicated using only lower case letters, so for broader compatibility, implementers are encouraged to use lower case letters. 

**Usages:**

* Use this Primitive Type Profile: [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md), [II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md) and [SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/uuid)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-uuid.csv), [Excel](StructureDefinition-uuid.xlsx), [Schematron](StructureDefinition-uuid.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "uuid",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/uuid",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "uuid",
  "title" : "uuid: DCE Universal Unique Identifier",
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
  "description" : "A globally unique string representing a DCE Universal Unique Identifier (UUID) in the common UUID format that consists of 5 hyphen-separated groups of hexadecimal digits having 8, 4, 4, 4, and 12 places respectively.\n\n***NOTE:*** The output of UUID related programs and functions may use all sorts of forms, upper case, lower case, and with or without the hyphens that group the digits. This variate output must be postprocessed to conform to the HL7 specification, i.e., the hyphens must be inserted for the 8-4-4-4-12 grouping. Historically, CDA also required that all hexadecimal digits must be converted to upper case, but due to real-world issues encountered when enforcing this rule, it has been relaxed to allow for upper or lower case letters. Additionally, FHIR requires that UUID's be communicated using only lower case letters, so for broader compatibility, implementers are encouraged to use lower case letters.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "string",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/string",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "string",
        "path" : "string",
        "constraint" : [
          {
            "key" : "uuid-pattern",
            "severity" : "error",
            "human" : "Must contain 5 hyphen-separated groups of hexadecimal digits having 8, 4, 4, 4, and 12 places respectively.",
            "expression" : "matches('^[0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12}$')"
          }
        ]
      },
      {
        "id" : "string.id",
        "path" : "string.id",
        "max" : "0"
      },
      {
        "id" : "string.extension",
        "path" : "string.extension",
        "max" : "0"
      }
    ]
  }
}

```
