# InFulfillmentOf (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **InFulfillmentOf (CDA Class)**

## Logical Model: InFulfillmentOf (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:InFulfillmentOf |

 
This class represents those orders that are fulfilled by this document. For instance, a provider orders an X-Ray. The X-Ray is performed. A radiologist reads the X-Ray and generates a report. The X-Ray order identifier is transmitted in the Order class, the performed X-Ray procedure is transmitted in the ServiceEvent class, and the ClinicalDocument.code would be valued with "Diagnostic Imaging Report". 

**Usages:**

* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/InFulfillmentOf)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-InFulfillmentOf.csv), [Excel](StructureDefinition-InFulfillmentOf.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "InFulfillmentOf",
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
      "valueString" : "inFulfillmentOf"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "InFulfillmentOf",
  "title" : "InFulfillmentOf (CDA Class)",
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
  "description" : "This class represents those orders that are fulfilled by this document. For instance, a provider orders an X-Ray. The X-Ray is performed. A radiologist reads the X-Ray and generates a report. The X-Ray order identifier is transmitted in the Order class, the performed X-Ray procedure is transmitted in the ServiceEvent class, and the ClinicalDocument.code would be valued with \"Diagnostic Imaging Report\".",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "InFulfillmentOf",
        "path" : "InFulfillmentOf",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "InFulfillmentOf.typeCode",
        "path" : "InFulfillmentOf.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "FLFS",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActRelationshipFulfills|2.0.0"
        }
      },
      {
        "id" : "InFulfillmentOf.order",
        "path" : "InFulfillmentOf.order",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Order"
          }
        ]
      }
    ]
  }
}

```
