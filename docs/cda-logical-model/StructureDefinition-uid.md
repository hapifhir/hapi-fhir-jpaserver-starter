# uid: Unique Identifier String - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **uid: Unique Identifier String**

## Data Type Profile: uid: Unique Identifier String 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/uid | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:uid |

 
A unique identifier string is a character string which identifies an object in a globally unique and timeless manner. The allowable formats and values and procedures of this data type are strictly controlled by HL7. At this time, user-assigned identifiers may be certain character representations of ISO Object Identifiers (OID) and DCE Universally Unique Identifiers (UUID). HL7 also reserves the right to assign other forms of UIDs (RUID), such as mnemonic identifiers for code systems. 

**Usages:**

* This Primitive Type Profile is not used by any profiles in this Implementation Guide

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/uid)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-uid.csv), [Excel](StructureDefinition-uid.xlsx), [Schematron](StructureDefinition-uid.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "uid",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/uid",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "uid",
  "title" : "uid: Unique Identifier String",
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
  "description" : "A unique identifier string is a character string which identifies an object in a globally unique and timeless manner. The allowable formats and values and procedures of this data type are strictly controlled by HL7. At this time, user-assigned identifiers may be certain character representations of ISO Object Identifiers (OID) and DCE Universally Unique Identifiers (UUID). HL7 also reserves the right to assign other forms of UIDs (RUID), such as mnemonic identifiers for code systems.",
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
            "key" : "uid-pattern",
            "severity" : "error",
            "human" : "Must conform to OID (#.#.#), UUID (32 hexadecimal digits separated by hyphens), or RUID (a letter followed by any combination of letters, numbers or hyphens)",
            "expression" : "matches('^([0-2](\\\\.(0|[1-9][0-9]*))+)|([0-9A-Za-z]{8}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{4}-[0-9A-Za-z]{12})|([A-Za-z][A-Za-z0-9\\\\-]*)$')"
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
