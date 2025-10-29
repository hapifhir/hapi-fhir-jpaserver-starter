# NonXMLBody (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **NonXMLBody (CDA Class)**

## Logical Model: NonXMLBody (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/NonXMLBody | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:NonXMLBody |

 
The NonXMLBody class represents a document body that is in some format other than XML. NonXMLBody.text is used to reference data that is stored externally to the CDA document or to encode the data directly inline. 
Rendering a referenced non-XML body requires a software tool that recognizes the particular MIME media type of the blob. 

**Usages:**

* Use this Logical Model: [Component (CDA Class)](StructureDefinition-Component.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/NonXMLBody)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-NonXMLBody.csv), [Excel](StructureDefinition-NonXMLBody.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "NonXMLBody",
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
      "valueString" : "nonXMLBody"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/NonXMLBody",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "NonXMLBody",
  "title" : "NonXMLBody (CDA Class)",
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
  "description" : "The NonXMLBody class represents a document body that is in some format other than XML. NonXMLBody.text is used to reference data that is stored externally to the CDA document or to encode the data directly inline.\n\nRendering a referenced non-XML body requires a software tool that recognizes the particular MIME media type of the blob.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/NonXMLBody",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "NonXMLBody",
        "path" : "NonXMLBody",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "NonXMLBody.classCode",
        "path" : "NonXMLBody.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DOCBODY",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActClassRecordOrganizer"
        }
      },
      {
        "id" : "NonXMLBody.moodCode",
        "path" : "NonXMLBody.moodCode",
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
        "id" : "NonXMLBody.text",
        "path" : "NonXMLBody.text",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "NonXMLBody.confidentialityCode",
        "path" : "NonXMLBody.confidentialityCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "NonXMLBody.languageCode",
        "path" : "NonXMLBody.languageCode",
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
      }
    ]
  }
}

```
