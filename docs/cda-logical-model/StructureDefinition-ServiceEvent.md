# ServiceEvent (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ServiceEvent (CDA Class)**

## Logical Model: ServiceEvent (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ServiceEvent | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ServiceEvent |

 
This class represents the main Act, such as a colonoscopy or an appendectomy, being documented. 
In some cases, the ServiceEvent is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is "History and Physical Report" and the procedure being documented is a "History and Physical" act. A ServiceEvent can further specialize the act inherent in the ClinicalDocument.code, such as where the ClinicalDocument.code is simply "Procedure Report" and the procedure was a "colonoscopy". If ServiceEvent is included, it must be equivalent to or further specialize the value inherent in the ClinicalDocument.code, and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation. 
ServiceEvent.effectiveTime can be used to indicate the time the actual event (as opposed to the encounter surrounding the event) took place. 

**Usages:**

* Use this Logical Model: [DocumentationOf (CDA Class)](StructureDefinition-DocumentationOf.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ServiceEvent)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ServiceEvent.csv), [Excel](StructureDefinition-ServiceEvent.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ServiceEvent",
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
      "valueString" : "serviceEvent"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ServiceEvent",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ServiceEvent",
  "title" : "ServiceEvent (CDA Class)",
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
  "description" : "This class represents the main Act, such as a colonoscopy or an appendectomy, being documented.\n\nIn some cases, the ServiceEvent is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"History and Physical Report\" and the procedure being documented is a \"History and Physical\" act. A ServiceEvent can further specialize the act inherent in the ClinicalDocument.code, such as where the ClinicalDocument.code is simply \"Procedure Report\" and the procedure was a \"colonoscopy\". If ServiceEvent is included, it must be equivalent to or further specialize the value inherent in the ClinicalDocument.code, and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.\n\nServiceEvent.effectiveTime can be used to indicate the time the actual event (as opposed to the encounter surrounding the event) took place.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ServiceEvent",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ServiceEvent",
        "path" : "ServiceEvent",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "ServiceEvent.classCode",
        "path" : "ServiceEvent.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "ACT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClass"
        }
      },
      {
        "id" : "ServiceEvent.moodCode",
        "path" : "ServiceEvent.moodCode",
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
        "id" : "ServiceEvent.id",
        "path" : "ServiceEvent.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ServiceEvent.code",
        "path" : "ServiceEvent.code",
        "definition" : "Drawn from concept domain ActCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "ServiceEvent.statusCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:oid:1.3.6.1.4.1.19376.1.3.2"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "statusCode"
          }
        ],
        "path" : "ServiceEvent.statusCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ]
      },
      {
        "id" : "ServiceEvent.effectiveTime",
        "path" : "ServiceEvent.effectiveTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "ServiceEvent.performer",
        "path" : "ServiceEvent.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer1"
          }
        ]
      }
    ]
  }
}

```
