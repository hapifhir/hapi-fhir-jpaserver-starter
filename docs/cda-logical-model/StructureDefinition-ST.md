# ST: CharacterString (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ST: CharacterString (V3 Data Type)**

## Logical Model: ST: CharacterString (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ST | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ST |

 
The character string data type stands for text data, primarily intended for machine processing (e.g., sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions. 

**Usages:**

* Derived from this Logical Model: [ADXP: CharacterString (V3 Data Type)](StructureDefinition-ADXP.md), [ENXP: Entity Name Part (V3 Data Type)](StructureDefinition-ENXP.md) and [SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md)
* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Material (CDA Class)](StructureDefinition-Material.md)...Show 4 more,[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[PharmPackagedMedicine (CDA Pharm Class)](StructureDefinition-PharmPackagedMedicine.md)and[Section (CDA Class)](StructureDefinition-Section.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ST)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ST.csv), [Excel](StructureDefinition-ST.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ST",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ST",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ST",
  "title" : "ST: CharacterString (V3 Data Type)",
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
  "description" : "The character string data type stands for text data, primarily intended for machine processing (e.g., sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ST",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ST",
        "path" : "ST",
        "definition" : "The character string data type stands for text data, primarily intended for machine processing (e.g., sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions.",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "text-null",
            "severity" : "error",
            "human" : "xmlText and nullFlavor are mutually exclusive (one must be present)",
            "expression" : "(xmlText | nullFlavor).count() = 1"
          }
        ]
      },
      {
        "id" : "ST.representation",
        "path" : "ST.representation",
        "representation" : ["xmlAttr"],
        "definition" : "Specifies the representation of the binary data that is the content of the binary data value",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "TXT"
      },
      {
        "id" : "ST.mediaType",
        "path" : "ST.mediaType",
        "representation" : ["xmlAttr"],
        "label" : "Media Type",
        "definition" : "Identifies the type of the encapsulated data and identifies a method to interpret or render the data.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "text/plain"
      },
      {
        "id" : "ST.language",
        "path" : "ST.language",
        "representation" : ["xmlAttr"],
        "label" : "Language",
        "definition" : "For character based information the language property specifies the human language of the text.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ]
      },
      {
        "id" : "ST.xmlText",
        "path" : "ST.xmlText",
        "representation" : ["xmlText"],
        "short" : "Allows for mixed text content",
        "comment" : "This element is represented in XML as textual content. The actual name \"xmlText\" will not appear in a CDA instance.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      }
    ]
  }
}

```
