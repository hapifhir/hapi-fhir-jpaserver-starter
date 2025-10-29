# ts: Point in Time - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ts: Point in Time**

## Data Type Profile: ts: Point in Time 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ts-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ts |

 
A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression. 

**Usages:**

* Use this Primitive Type Profile: [TS: PointInTime (V3 Data Type)](StructureDefinition-TS.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ts-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ts-simple.csv), [Excel](StructureDefinition-ts-simple.xlsx), [Schematron](StructureDefinition-ts-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ts-simple",
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
      "valueCode" : "has-range"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ts-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ts",
  "title" : "ts: Point in Time",
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
  "description" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "dateTime",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/dateTime",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "dateTime.id",
        "path" : "dateTime.id",
        "max" : "0"
      },
      {
        "id" : "dateTime.extension",
        "path" : "dateTime.extension",
        "max" : "0"
      },
      {
        "id" : "dateTime.value",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-date-format",
            "valueString" : "YYYYMMDDHHMMSS.UUUU[+|-ZZzz]"
          }
        ],
        "path" : "dateTime.value"
      }
    ]
  }
}

```
