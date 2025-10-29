# ruid: HL7 Reserved Identifier Scheme - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ruid: HL7 Reserved Identifier Scheme**

## Data Type Profile: ruid: HL7 Reserved Identifier Scheme 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ruid | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ruid |

 
HL7 Reserved Identifier Scheme (RUID)\nA globally unique string defined exclusively by HL7. Identifiers in this scheme are only defined by balloted HL7 specifications. Local communities or systems must never use such reserved identifiers based on bilateral negotiations. 
HL7 reserved identifiers are strings that consist only of (US-ASCII) letters, digits and hyphens, where the first character must be a letter. HL7 may assign these reserved identifiers as mnemonic identifiers for major concepts of interest to HL7. 

**Usages:**

* Use this Primitive Type Profile: [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md), [II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md) and [SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ruid)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ruid.csv), [Excel](StructureDefinition-ruid.xlsx), [Schematron](StructureDefinition-ruid.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ruid",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ruid",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ruid",
  "title" : "ruid: HL7 Reserved Identifier Scheme",
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
  "description" : "HL7 Reserved Identifier Scheme (RUID)\\nA globally unique string defined exclusively by HL7. Identifiers in this scheme are only defined by balloted HL7 specifications. Local communities or systems must never use such reserved identifiers based on bilateral negotiations.\n\nHL7 reserved identifiers are strings that consist only of (US-ASCII) letters, digits and hyphens, where the first character must be a letter. HL7 may assign these reserved identifiers as mnemonic identifiers for major concepts of interest to HL7.",
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
            "key" : "ruid-pattern",
            "severity" : "error",
            "human" : "An identifier that starts with a letter and contains any combination of letters, numbers, and hyphen.",
            "expression" : "matches('^[A-Za-z][A-Za-z0-9\\\\-]*$') and matches('^[0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12}$').not()"
          }
        ]
      },
      {
        "id" : "string.id",
        "path" : "string.id",
        "min" : 0
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
