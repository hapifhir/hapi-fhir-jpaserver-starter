# oid: ISO Object Identifier - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **oid: ISO Object Identifier**

## Data Type Profile: oid: ISO Object Identifier 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/oid | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:oid |

 
A globally unique string representing an ISO Object Identifier (OID) in a form that consists only of numbers and dots (e.g., '2.16.840.1.113883.3.1'). 

**Usages:**

* Use this Primitive Type Profile: [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md), [II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md) and [SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/oid)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-oid.csv), [Excel](StructureDefinition-oid.xlsx), [Schematron](StructureDefinition-oid.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "oid",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/oid",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "oid",
  "title" : "oid: ISO Object Identifier",
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
  "description" : "A globally unique string representing an ISO Object Identifier (OID) in a form that consists only of numbers and dots (e.g., '2.16.840.1.113883.3.1').",
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
            "key" : "oid-pattern",
            "severity" : "error",
            "human" : "Must conform to OID (#.#.#), UUID (32 hexadecimal digits separated by hyphens), or RUID (a letter followed by any combination of letters, numbers or hyphens)",
            "expression" : "matches('^[0-2](\\\\.(0|[1-9][0-9]*))+$')"
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
