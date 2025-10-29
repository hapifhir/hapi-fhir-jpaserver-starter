# xs:ID - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **xs:ID**

## Data Type Profile: xs:ID 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/xs-ID | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:xs:ID |

 
ID represents the ID attribute type from [XML 1.0 (Second Edition)]. The "value space" of ID is the set of all strings that "match" the NCName production in [Namespaces in XML]. The "lexical space" of ID is the set of all strings that "match" the NCName production in [Namespaces in XML]. 

**Usages:**

* Use this Primitive Type Profile: [ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md), [RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md) and [Section (CDA Class)](StructureDefinition-Section.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/xs-ID)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-xs-ID.csv), [Excel](StructureDefinition-xs-ID.xlsx), [Schematron](StructureDefinition-xs-ID.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "xs-ID",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "has-length"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/xs-ID",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "xs:ID",
  "title" : "xs:ID",
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
  "description" : "ID represents the ID attribute type from [XML 1.0 (Second Edition)]. The \"value space\" of ID is the set of all strings that \"match\" the NCName production in [Namespaces in XML]. The \"lexical space\" of ID is the set of all strings that \"match\" the NCName production in [Namespaces in XML].",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "id",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/id",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "id.id",
        "path" : "id.id",
        "max" : "0"
      },
      {
        "id" : "id.extension",
        "path" : "id.extension",
        "max" : "0"
      }
    ]
  }
}

```
