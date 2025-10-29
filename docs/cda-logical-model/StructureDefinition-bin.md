# bin: Binary Data - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **bin: Binary Data**

## Data Type Profile: bin: Binary Data 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/bin | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:bin |

 
Binary data is a raw block of bits. Binary data is a protected type that MUST not be used outside the data type specification. 

**Usages:**

* Use this Primitive Type Profile: [ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/bin)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-bin.csv), [Excel](StructureDefinition-bin.xlsx), [Schematron](StructureDefinition-bin.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "bin",
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
      "valueCode" : "has-size"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/bin",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "bin",
  "title" : "bin: Binary Data",
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
  "description" : "Binary data is a raw block of bits. Binary data is a protected type that MUST not be used outside the data type specification.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "base64Binary",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/base64Binary",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "base64Binary.id",
        "path" : "base64Binary.id",
        "max" : "0"
      },
      {
        "id" : "base64Binary.extension",
        "path" : "base64Binary.extension",
        "max" : "0"
      }
    ]
  }
}

```
