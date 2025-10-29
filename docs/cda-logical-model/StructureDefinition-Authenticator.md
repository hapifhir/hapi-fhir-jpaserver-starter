# Authenticator (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Authenticator (CDA Class)**

## Logical Model: Authenticator (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Authenticator | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Authenticator |

 
Represents a participant who has attested to the accuracy of the document, but who does not have privileges to legally authenticate the document. An example would be a resident physician who sees a patient and dictates a note, then later signs it. A clinical document can have zero to many authenticators. While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. An authenticator has a required authenticator.time indicating the time of authentication, and a required authenticator.signatureCode, indicating that a signature has been obtained and is on file. 

**Usages:**

* Use this Logical Model: [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Authenticator)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Authenticator.csv), [Excel](StructureDefinition-Authenticator.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Authenticator",
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
      "valueString" : "authenticator"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Authenticator",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Authenticator",
  "title" : "Authenticator (CDA Class)",
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
  "description" : "Represents a participant who has attested to the accuracy of the document, but who does not have privileges to legally authenticate the document. An example would be a resident physician who sees a patient and dictates a note, then later signs it. A clinical document can have zero to many authenticators. While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. An authenticator has a required authenticator.time indicating the time of authentication, and a required authenticator.signatureCode, indicating that a signature has been obtained and is on file.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Authenticator",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Authenticator",
        "path" : "Authenticator",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Authenticator.typeCode",
        "path" : "Authenticator.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "AUTHEN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "Authenticator.time",
        "path" : "Authenticator.time",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/TS"
          }
        ]
      },
      {
        "id" : "Authenticator.signatureCode",
        "path" : "Authenticator.signatureCode",
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
        "id" : "Authenticator.sdtcSignatureText",
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
        "path" : "Authenticator.sdtcSignatureText",
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
        "id" : "Authenticator.assignedEntity",
        "path" : "Authenticator.assignedEntity",
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
