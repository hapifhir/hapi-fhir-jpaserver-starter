# LabCriterion (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **LabCriterion (CDA Class)**

## Logical Model: LabCriterion (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/LabCriterion | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CDAR2.LabCriterion |

**Usages:**

* Use this Logical Model: [LabPrecondition (CDA Class)](StructureDefinition-LabPrecondition.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/LabCriterion)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-LabCriterion.csv), [Excel](StructureDefinition-LabCriterion.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "LabCriterion",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:oid:1.3.6.1.4.1.19376.1.3.2"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "LabCriterion"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/LabCriterion",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAR2.LabCriterion",
  "title" : "LabCriterion (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/LabCriterion",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "LabCriterion",
        "path" : "LabCriterion",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "LabCriterion.nullFlavor",
        "path" : "LabCriterion.nullFlavor",
        "representation" : ["xmlAttr"],
        "label" : "Exceptional Value Detail",
        "definition" : "If a value is an exceptional value (NULL-value), this specifies in what way and why proper information is missing.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-NullFlavor"
        }
      },
      {
        "id" : "LabCriterion.classCode",
        "path" : "LabCriterion.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "OBS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActClassObservation"
        }
      },
      {
        "id" : "LabCriterion.moodCode",
        "path" : "LabCriterion.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "EVN.CRT",
        "fixedCode" : "EVN.CRT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActMoodPredicate"
        }
      },
      {
        "id" : "LabCriterion.templateId",
        "path" : "LabCriterion.templateId",
        "definition" : "When valued in an instance, this attribute signals the imposition of a set of template-defined constraints. The value of this attribute provides a unique identifier for the templates in question",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "LabCriterion.code",
        "path" : "LabCriterion.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "extensible",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActCode"
        }
      },
      {
        "id" : "LabCriterion.text",
        "path" : "LabCriterion.text",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "LabCriterion.value",
        "path" : "LabCriterion.value",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CV"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SC"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AD"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/REAL"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/MO"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ]
      }
    ]
  }
}

```
