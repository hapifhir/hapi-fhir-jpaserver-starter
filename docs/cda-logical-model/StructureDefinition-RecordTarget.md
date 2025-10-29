# RecordTarget (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **RecordTarget (CDA Class)**

## Logical Model: RecordTarget (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/RecordTarget | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:RecordTarget |

 
The recordTarget represents the medical record that this document belongs to. A clinical document typically has exactly one recordTarget participant. In the uncommon case where a clinical document (such as a group encounter note) is placed into more than one patient chart, more than one recordTarget participants can be stated. The recordTarget(s) of a document are stated in the header and propagate to nested content, where they cannot be overridden (see See CDA Context (§ 4.4 )). 

**Usages:**

* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/RecordTarget)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-RecordTarget.csv), [Excel](StructureDefinition-RecordTarget.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "RecordTarget",
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
      "valueString" : "recordTarget"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/RecordTarget",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "RecordTarget",
  "title" : "RecordTarget (CDA Class)",
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
  "description" : "The recordTarget represents the medical record that this document belongs to. A clinical document typically has exactly one recordTarget participant. In the uncommon case where a clinical document (such as a group encounter note) is placed into more than one patient chart, more than one recordTarget participants can be stated. The recordTarget(s) of a document are stated in the header and propagate to nested content, where they cannot be overridden (see See CDA Context (§ 4.4 )).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/RecordTarget",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "RecordTarget",
        "path" : "RecordTarget",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "RecordTarget.typeCode",
        "path" : "RecordTarget.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "RCT",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "RecordTarget.contextControlCode",
        "path" : "RecordTarget.contextControlCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "OP",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAContextControl"
        }
      },
      {
        "id" : "RecordTarget.patientRole",
        "path" : "RecordTarget.patientRole",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PatientRole"
          }
        ]
      }
    ]
  }
}

```
