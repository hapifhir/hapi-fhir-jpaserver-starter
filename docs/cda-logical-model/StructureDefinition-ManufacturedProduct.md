# ManufacturedProduct (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ManufacturedProduct (CDA Class)**

## Logical Model: ManufacturedProduct (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ManufacturedProduct | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ManufacturedProduct |

 
ManufacturedProduct (CDA Class) 

**Usages:**

* Use this Logical Model: [SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md) and [Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ManufacturedProduct)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ManufacturedProduct.csv), [Excel](StructureDefinition-ManufacturedProduct.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ManufacturedProduct",
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
      "valueString" : "manufacturedProduct"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ManufacturedProduct",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ManufacturedProduct",
  "title" : "ManufacturedProduct (CDA Class)",
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
  "description" : "ManufacturedProduct (CDA Class)",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ManufacturedProduct",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ManufacturedProduct",
        "path" : "ManufacturedProduct",
        "min" : 1,
        "max" : "1",
        "constraint" : [
          {
            "key" : "product-choice",
            "severity" : "error",
            "human" : "manufacturedLabeledDrug and manufacturedMaterial are mutually exclusive (one must be present)",
            "expression" : "(manufacturedLabeledDrug | manufacturedMaterial).count() = 1"
          }
        ]
      },
      {
        "id" : "ManufacturedProduct.classCode",
        "path" : "ManufacturedProduct.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "MANU",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RoleClassManufacturedProduct"
        }
      },
      {
        "id" : "ManufacturedProduct.id",
        "path" : "ManufacturedProduct.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ManufacturedProduct.sdtcIdentifiedBy",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "identifiedBy"
          }
        ],
        "path" : "ManufacturedProduct.sdtcIdentifiedBy",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy"
          }
        ]
      },
      {
        "id" : "ManufacturedProduct.manufacturedLabeledDrug",
        "path" : "ManufacturedProduct.manufacturedLabeledDrug",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/LabeledDrug"
          }
        ]
      },
      {
        "id" : "ManufacturedProduct.manufacturedMaterial",
        "path" : "ManufacturedProduct.manufacturedMaterial",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Material"
          }
        ]
      },
      {
        "id" : "ManufacturedProduct.manufacturerOrganization",
        "path" : "ManufacturedProduct.manufacturerOrganization",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Organization"
          }
        ]
      }
    ]
  }
}

```
