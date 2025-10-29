# IG Parameters CDA Validation - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **IG Parameters CDA Validation**

## CodeSystem: IG Parameters CDA Validation 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/CodeSystem/IGParametersCDAValidation | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:IGParametersCDAValidation |

 
Code system for CDA validation parameters in the IG. 

 This Code system is referenced in the content logical definition of the following value sets: 

* This CodeSystem is not used here; it may be used elsewhere (e.g. specifications and/or implementations that use this content)



## Resource Content

```json
{
  "resourceType" : "CodeSystem",
  "id" : "IGParametersCDAValidation",
  "url" : "http://hl7.org/cda/stds/core/CodeSystem/IGParametersCDAValidation",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "IGParametersCDAValidation",
  "title" : "IG Parameters CDA Validation",
  "status" : "draft",
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
  "description" : "Code system for CDA validation parameters in the IG.",
  "caseSensitive" : true,
  "content" : "complete",
  "count" : 2,
  "concept" : [
    {
      "code" : "parent-template-id",
      "display" : "Parent TemplateId",
      "definition" : "TemplateId from which all templates in this IG descend. A schematron warning will detect any teplateIds starting with this value that are not defined in the IG."
    },
    {
      "code" : "value-set-limit",
      "display" : "ValueSet Limit",
      "definition" : "Maximum number of values to include in value set lookups."
    }
  ]
}

```
