# Section (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Section (CDA Class)**

## Logical Model: Section (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Section | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Section |

 
Document sections can nest, can override context propagated from the header (See CDA Context), and can contain narrative and CDA entries. 
An XML attribute "ID" of type XML ID, is added to Section within the CDA Schema. This attribute serves as the target of a linkHtml reference (see linkHtml). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification). 

**Usages:**

* Use this Logical Model: [Section (CDA Class)](StructureDefinition-Section.md) and [StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Section)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Section.csv), [Excel](StructureDefinition-Section.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Section",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "section"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/type-profile-style",
      "valueCode" : "cda"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Section",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Section",
  "title" : "Section (CDA Class)",
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
  "description" : "Document sections can nest, can override context propagated from the header (See CDA Context), and can contain narrative and CDA entries.\n\nAn XML attribute \"ID\" of type XML ID, is added to Section within the CDA Schema. This attribute serves as the target of a linkHtml reference (see linkHtml). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Section",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Section",
        "path" : "Section",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Section.ID",
        "path" : "Section.ID",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "id",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/xs-ID"]
          }
        ]
      },
      {
        "id" : "Section.classCode",
        "path" : "Section.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DOCSECT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActClassRecordOrganizer"
        }
      },
      {
        "id" : "Section.moodCode",
        "path" : "Section.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "EVN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActMood"
        }
      },
      {
        "id" : "Section.id",
        "path" : "Section.id",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Section.code",
        "path" : "Section.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "Section.title",
        "path" : "Section.title",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          }
        ]
      },
      {
        "id" : "Section.text",
        "path" : "Section.text",
        "representation" : ["cdaText"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "xhtml"
          }
        ]
      },
      {
        "id" : "Section.confidentialityCode",
        "path" : "Section.confidentialityCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "Section.languageCode",
        "path" : "Section.languageCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/fhir/ValueSet/all-languages"
        }
      },
      {
        "id" : "Section.subject",
        "path" : "Section.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "Section.author",
        "path" : "Section.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "Section.informant",
        "path" : "Section.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "Section.entry",
        "path" : "Section.entry",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Entry"
          }
        ]
      },
      {
        "id" : "Section.component",
        "path" : "Section.component",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "Section.component.typeCode",
        "path" : "Section.component.typeCode",
        "representation" : ["xmlAttr"],
        "definition" : "Drawn from concept domain DocumentSectionType",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "COMP"
      },
      {
        "id" : "Section.component.contextConductionInd",
        "path" : "Section.component.contextConductionInd",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ],
        "fixedBoolean" : true
      },
      {
        "id" : "Section.component.section",
        "path" : "Section.component.section",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Section"
          }
        ]
      }
    ]
  }
}

```
