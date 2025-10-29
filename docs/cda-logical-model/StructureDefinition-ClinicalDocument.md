# ClinicalDocument (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ClinicalDocument (CDA Class)**

## Logical Model: ClinicalDocument (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ClinicalDocument |

 
This is a generated StructureDefinition that describes CDA - that is, CDA as it actually is for R2. The intent of this StructureDefinition is to enable CDA to be a FHIR resource. That enables the FHIR infrastructure - API, conformance, query - to be used directly against CDA 

**Usages:**

* This Logical Model is not used by any profiles in this Implementation Guide

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ClinicalDocument)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ClinicalDocument.csv), [Excel](StructureDefinition-ClinicalDocument.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ClinicalDocument",
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
      "valueString" : "ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/type-profile-style",
      "valueCode" : "cda"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ClinicalDocument",
  "title" : "ClinicalDocument (CDA Class)",
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
  "description" : "This is a generated StructureDefinition that describes CDA - that is, CDA as it actually is for R2. The intent of this StructureDefinition is to enable CDA to be a FHIR resource. That enables the FHIR infrastructure - API, conformance, query - to be used directly against CDA",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ClinicalDocument",
        "path" : "ClinicalDocument",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "ClinicalDocument.classCode",
        "path" : "ClinicalDocument.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DOCCLIN",
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClass"
        }
      },
      {
        "id" : "ClinicalDocument.moodCode",
        "path" : "ClinicalDocument.moodCode",
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
        "id" : "ClinicalDocument.realmCode",
        "path" : "ClinicalDocument.realmCode",
        "definition" : "When valued in an instance, this attribute signals the imposition of realm-specific constraints. The value of this attribute identifies the realm in question",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.typeId",
        "path" : "ClinicalDocument.typeId",
        "definition" : "ClinicalDocument.typeId is a technology-neutral explicit reference to this CDA, Release Two specification, and must be valued as follows: ClinicalDocument.typeId.root = \"2.16.840.1.113883.1.3\" (which is the OID for HL7 Registered models); ClinicalDocument.typeId.extension = \"POCD_HD000040\" (which is the unique identifier for the CDA, Release Two Hierarchical Description).",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.typeId.root",
        "path" : "ClinicalDocument.typeId.root",
        "representation" : ["xmlAttr"],
        "definition" : "Identifies the type as an HL7 Registered model",
        "min" : 1,
        "max" : "1",
        "fixedString" : "2.16.840.1.113883.1.3"
      },
      {
        "id" : "ClinicalDocument.typeId.extension",
        "path" : "ClinicalDocument.typeId.extension",
        "representation" : ["xmlAttr"],
        "definition" : "A character string as a unique identifier within the scope of the identifier root.",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ],
        "fixedString" : "POCD_HD000040"
      },
      {
        "id" : "ClinicalDocument.templateId",
        "path" : "ClinicalDocument.templateId",
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
        "id" : "ClinicalDocument.id",
        "path" : "ClinicalDocument.id",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.sdtcCategory",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "category"
          }
        ],
        "path" : "ClinicalDocument.sdtcCategory",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.code",
        "path" : "ClinicalDocument.code",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://hl7.org/fhir/ValueSet/doc-typecodes"
        }
      },
      {
        "id" : "ClinicalDocument.title",
        "path" : "ClinicalDocument.title",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ST"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.sdtcStatusCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "statusCode"
          }
        ],
        "path" : "ClinicalDocument.sdtcStatusCode",
        "definition" : "The statusCode extension attribute allows the implementer to identify a ClinicalDocument that is in other than the completed state. It was created to support the Structured Form Definition IG to identify that the document itself is an unfinished product currently being completed for a patient.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActStatus"
        }
      },
      {
        "id" : "ClinicalDocument.terminologyDate",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-at:v3"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "terminologyDate"
          }
        ],
        "path" : "ClinicalDocument.terminologyDate",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.formatCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-at:v3"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "formatCode"
          }
        ],
        "path" : "ClinicalDocument.formatCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.practiceSettingCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-at:v3"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "practiceSettingCode"
          }
        ],
        "path" : "ClinicalDocument.practiceSettingCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.effectiveTime",
        "path" : "ClinicalDocument.effectiveTime",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.confidentialityCode",
        "path" : "ClinicalDocument.confidentialityCode",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.languageCode",
        "path" : "ClinicalDocument.languageCode",
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
        "id" : "ClinicalDocument.setId",
        "path" : "ClinicalDocument.setId",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.versionNumber",
        "path" : "ClinicalDocument.versionNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/INT"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.copyTime",
        "path" : "ClinicalDocument.copyTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.recordTarget",
        "path" : "ClinicalDocument.recordTarget",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RecordTarget"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.author",
        "path" : "ClinicalDocument.author",
        "min" : 1,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.dataEnterer",
        "path" : "ClinicalDocument.dataEnterer",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/DataEnterer"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.informant",
        "path" : "ClinicalDocument.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.custodian",
        "path" : "ClinicalDocument.custodian",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Custodian"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.informationRecipient",
        "path" : "ClinicalDocument.informationRecipient",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InformationRecipient"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.legalAuthenticator",
        "path" : "ClinicalDocument.legalAuthenticator",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/LegalAuthenticator"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.authenticator",
        "path" : "ClinicalDocument.authenticator",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Authenticator"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.participant",
        "path" : "ClinicalDocument.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant1"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.inFulfillmentOf",
        "path" : "ClinicalDocument.inFulfillmentOf",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.documentationOf",
        "path" : "ClinicalDocument.documentationOf",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/DocumentationOf"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.relatedDocument",
        "path" : "ClinicalDocument.relatedDocument",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RelatedDocument"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.authorization",
        "path" : "ClinicalDocument.authorization",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Authorization"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.componentOf",
        "path" : "ClinicalDocument.componentOf",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ComponentOf"
          }
        ]
      },
      {
        "id" : "ClinicalDocument.component",
        "path" : "ClinicalDocument.component",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Component"
          }
        ]
      }
    ]
  }
}

```
