# LegalAuthenticator (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **LegalAuthenticator (CDA Class)**

## Logical Model: LegalAuthenticator (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/LegalAuthenticator | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:LegalAuthenticator |

 
Represents a participant who has legally authenticated the document. The CDA is a standard that specifies the structure of exchanged clinical documents. In the case where a local document is transformed into a CDA document for exchange, authentication occurs on the local document, and that fact is reflected in the exchanged CDA document. A CDA document can reflect the unauthenticated, authenticated, or legally authenticated state. The unauthenticated state exists when no authentication information has been recorded (i.e., it is the absence of being either authenticated or legally authenticated). While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. A legalAuthenticator has a required legalAuthenticator.time indicating the time of authentication, and a required legalAuthenticator.signatureCode, indicating that a signature has been obtained and is on file. 

**Usages:**

* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/LegalAuthenticator)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-LegalAuthenticator.csv), [Excel](StructureDefinition-LegalAuthenticator.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "LegalAuthenticator",
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
      "valueString" : "legalAuthenticator"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/LegalAuthenticator",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "LegalAuthenticator",
  "title" : "LegalAuthenticator (CDA Class)",
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
  "description" : "Represents a participant who has legally authenticated the document. The CDA is a standard that specifies the structure of exchanged clinical documents. In the case where a local document is transformed into a CDA document for exchange, authentication occurs on the local document, and that fact is reflected in the exchanged CDA document. A CDA document can reflect the unauthenticated, authenticated, or legally authenticated state. The unauthenticated state exists when no authentication information has been recorded (i.e., it is the absence of being either authenticated or legally authenticated). While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. A legalAuthenticator has a required legalAuthenticator.time indicating the time of authentication, and a required legalAuthenticator.signatureCode, indicating that a signature has been obtained and is on file.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/LegalAuthenticator",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "LegalAuthenticator",
        "path" : "LegalAuthenticator",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "LegalAuthenticator.typeCode",
        "path" : "LegalAuthenticator.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "LA",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "LegalAuthenticator.contextControlCode",
        "path" : "LegalAuthenticator.contextControlCode",
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
        "id" : "LegalAuthenticator.time",
        "path" : "LegalAuthenticator.time",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "LegalAuthenticator.signatureCode",
        "path" : "LegalAuthenticator.signatureCode",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "constraint" : [
          {
            "key" : "signature",
            "severity" : "warning",
            "human" : "CDA Release One represented either an intended ('X') or actual ('S') authenticator. CDA Release Two only represents an actual authenticator, so has deprecated the value of 'X'.",
            "expression" : "code.exists() implies code != 'X'"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDASignatureCode"
        }
      },
      {
        "id" : "LegalAuthenticator.sdtcSignatureText",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "signatureText"
          }
        ],
        "path" : "LegalAuthenticator.sdtcSignatureText",
        "definition" : "A textual or multimedia depiction of the signature by which the participant endorses his or her participation in the Act as specified in the Participation.typeCode and that he or she agrees to assume the associated accountability.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "LegalAuthenticator.assignedEntity",
        "path" : "LegalAuthenticator.assignedEntity",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity"
          }
        ]
      }
    ]
  }
}

```
