# IdentifiedBy (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **IdentifiedBy (CDA Class)**

## Logical Model: IdentifiedBy (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:IdentifiedBy |

 
The alternateIdentification extension provides additional information about an identifier found in the linked role. The extensions augment the id information in the linked role. The id in the alternateIdentification extension SHALL match an id in the linked role. The alternateIdentification provides additional information about a particular identifier, such as its type. As an extension it needs to be safe for implementers to ignore this additional information. 

**Usages:**

* Use this Logical Model: [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md), [AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md), [Guardian (CDA Class)](StructureDefinition-Guardian.md)...Show 7 more,[HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[ManufacturedProduct (CDA Class)](StructureDefinition-ManufacturedProduct.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md)and[SpecimenRole (CDA Class)](StructureDefinition-SpecimenRole.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/IdentifiedBy)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-IdentifiedBy.csv), [Excel](StructureDefinition-IdentifiedBy.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "IdentifiedBy",
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
      "valueUri" : "urn:hl7-org:sdtc"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
      "valueString" : "identifiedBy"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "IdentifiedBy",
  "title" : "IdentifiedBy (CDA Class)",
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
  "description" : "The alternateIdentification extension provides additional information about an identifier found in the linked role. The extensions augment the id information in the linked role.  The id in the alternateIdentification extension SHALL match an id in the linked role. The alternateIdentification provides additional information about a particular identifier, such as its type. As an extension it needs to be safe for implementers to ignore this additional information.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/IdentifiedBy",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "IdentifiedBy",
        "path" : "IdentifiedBy",
        "min" : 0,
        "max" : "*"
      },
      {
        "id" : "IdentifiedBy.typeCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:v3"
          }
        ],
        "path" : "IdentifiedBy.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code"
          }
        ],
        "fixedCode" : "REL",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RoleLinkType"
        }
      },
      {
        "id" : "IdentifiedBy.sdtcAlternateIdentification",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "alternateIdentification"
          }
        ],
        "path" : "IdentifiedBy.sdtcAlternateIdentification",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AlternateIdentification"
          }
        ]
      }
    ]
  }
}

```
