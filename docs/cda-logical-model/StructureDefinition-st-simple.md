# st: Character String - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **st: Character String**

## Data Type Profile: st: Character String 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/st-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:st |

 
The character string data type stands for text data, primarily intended for machine processing (e.g. sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions. 

**Usages:**

* Use this Primitive Type Profile: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md), [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md)...Show 6 more,[EN: EntityName (V3 Data Type)](StructureDefinition-EN.md),[II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md),[InfrastructureRoot (Base Type for all CDA Classes)](StructureDefinition-InfrastructureRoot.md),[SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md),[ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type)](StructureDefinition-ST-dot-r2b.md)and[ST: CharacterString (V3 Data Type)](StructureDefinition-ST.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/st-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-st-simple.csv), [Excel](StructureDefinition-st-simple.xlsx), [Schematron](StructureDefinition-st-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "st-simple",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/st-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "st",
  "title" : "st: Character String",
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
  "description" : "The character string data type stands for text data, primarily intended for machine processing (e.g. sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "string",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/string",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
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
