# PharmPackagedMedicine (CDA Pharm Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PharmPackagedMedicine (CDA Pharm Class)**

## Logical Model: PharmPackagedMedicine (CDA Pharm Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PharmPackagedMedicine | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CDAR2.PharmPackagedMedicine |

**Usages:**

* Use this Logical Model: [PharmContent (CDA Pharm Class)](StructureDefinition-PharmContent.md) and [PharmSuperContent (CDA Pharm Class)](StructureDefinition-PharmSuperContent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PharmPackagedMedicine)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PharmPackagedMedicine.csv), [Excel](StructureDefinition-PharmPackagedMedicine.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PharmPackagedMedicine",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:ihe:pharm"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmPackagedMedicine",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAR2.PharmPackagedMedicine",
  "title" : "PharmPackagedMedicine (CDA Pharm Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmPackagedMedicine",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PharmPackagedMedicine",
        "path" : "PharmPackagedMedicine",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "PharmPackagedMedicine.classCode",
        "path" : "PharmPackagedMedicine.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "CONT",
        "fixedCode" : "CONT",
        "mustSupport" : true
      },
      {
        "id" : "PharmPackagedMedicine.determinerCode",
        "path" : "PharmPackagedMedicine.determinerCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "INSTANCE",
        "fixedCode" : "INSTANCE",
        "mustSupport" : true
      },
      {
        "id" : "PharmPackagedMedicine.code",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "PharmPackagedMedicine.name",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          }
        ]
      },
      {
        "id" : "PharmPackagedMedicine.formCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.formCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "PharmPackagedMedicine.lotNumberText",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.lotNumberText",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          }
        ]
      },
      {
        "id" : "PharmPackagedMedicine.capacityQuantity",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.capacityQuantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "PharmPackagedMedicine.asSuperContent",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmPackagedMedicine.asSuperContent",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmSuperContent"
          }
        ]
      }
    ]
  }
}

```
