# TEL: TelecommunicationAddress (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **TEL: TelecommunicationAddress (V3 Data Type)**

## Logical Model: TEL: TelecommunicationAddress (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/TEL | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:TEL |

 
A telephone number (voice or fax), e-mail address, or other locator for a resource mediated by telecommunication equipment. The address is specified as a Universal Resource Locator (URL) qualified by time specification and use codes that help in deciding which address to use for a given time and purpose. 

**Usages:**

* Use this Logical Model: [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md), [AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md)...Show 12 more,[CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md),[ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md),[RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md)and[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/TEL)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-TEL.csv), [Excel](StructureDefinition-TEL.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "TEL",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "TEL",
  "title" : "TEL: TelecommunicationAddress (V3 Data Type)",
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
  "description" : "A telephone number (voice or fax), e-mail address, or other locator for a resource mediated by telecommunication equipment. The address is specified as a Universal Resource Locator (URL) qualified by time specification and use codes that help in deciding which address to use for a given time and purpose.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/TEL",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "TEL",
        "path" : "TEL",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "value-null",
            "severity" : "error",
            "human" : "value and nullFlavor are mutually exclusive (one must be present)",
            "expression" : "(value | nullFlavor).count() = 1"
          }
        ]
      },
      {
        "id" : "TEL.value",
        "path" : "TEL.value",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "url",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/url"]
          }
        ]
      },
      {
        "id" : "TEL.useablePeriod",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype",
            "valueCanonical" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ],
        "path" : "TEL.useablePeriod",
        "representation" : ["typeAttr"],
        "label" : "Useable Period",
        "definition" : "Specifies the periods of time during which the telecommunication address can be used. For a telephone number, this can indicate the time of day in which the party can be reached on that telephone. For a web address, it may specify a time range in which the web content is promised to be available under the given address.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PIVL-TS"
          },
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ]
      },
      {
        "id" : "TEL.use",
        "path" : "TEL.use",
        "representation" : ["xmlAttr"],
        "label" : "Use Code",
        "definition" : "One or more codes advising a system or user which telecommunication address in a set of like addresses to select for a given telecommunication need.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-TelecommunicationAddressUse|2.0.0"
        }
      }
    ]
  }
}

```
