# bn: BooleanNonNull - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **bn: BooleanNonNull**

## Data Type Profile: bn: BooleanNonNull 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/bn | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:bn |

 
The BooleanNonNull type is used where a Boolean cannot have a null value. A Boolean value can be either true or false. 

**Usages:**

* Use this Primitive Type Profile: [CR: ConceptRole (V3 Data Type)](StructureDefinition-CR.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/bn)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-bn.csv), [Excel](StructureDefinition-bn.xlsx), [Schematron](StructureDefinition-bn.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "bn",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/bn",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "bn",
  "title" : "bn: BooleanNonNull",
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
  "description" : "The BooleanNonNull type is used where a Boolean cannot have a null value. A Boolean value can be either true or false.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "boolean",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/boolean",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "boolean.id",
        "path" : "boolean.id",
        "max" : "0"
      },
      {
        "id" : "boolean.extension",
        "path" : "boolean.extension",
        "max" : "0"
      }
    ]
  }
}

```
