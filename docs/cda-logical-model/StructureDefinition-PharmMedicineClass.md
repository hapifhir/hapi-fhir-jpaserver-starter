# PharmMedicineClass (CDA Pharm Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **PharmMedicineClass (CDA Pharm Class)**

## Logical Model: PharmMedicineClass (CDA Pharm Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/PharmMedicineClass | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CDAR2.PharmMedicineClass |

**Usages:**

* Use this Logical Model: [PharmSpecializedKind (CDA Pharm Class)](StructureDefinition-PharmSpecializedKind.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/PharmMedicineClass)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-PharmMedicineClass.csv), [Excel](StructureDefinition-PharmMedicineClass.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "PharmMedicineClass",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmMedicineClass",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAR2.PharmMedicineClass",
  "title" : "PharmMedicineClass (CDA Pharm Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/PharmMedicineClass",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "PharmMedicineClass",
        "path" : "PharmMedicineClass",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "PharmMedicineClass.classCode",
        "path" : "PharmMedicineClass.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "defaultValueCode" : "MMAT",
        "fixedCode" : "MMAT",
        "mustSupport" : true
      },
      {
        "id" : "PharmMedicineClass.code",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmMedicineClass.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "PharmMedicineClass.name",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:ihe:pharm"
          }
        ],
        "path" : "PharmMedicineClass.name",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EN"
          }
        ]
      }
    ]
  }
}

```
