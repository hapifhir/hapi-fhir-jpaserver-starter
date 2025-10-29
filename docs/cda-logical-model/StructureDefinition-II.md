# II: InstanceIdentifier (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **II: InstanceIdentifier (V3 Data Type)**

## Logical Model: II: InstanceIdentifier (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/II | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:II |

 
An identifier that uniquely identifies a thing or object. Examples are object identifier for HL7 RIM objects, medical record number, order id, service catalog item id, Vehicle Identification Number (VIN), etc. Instance identifiers are defined based on ISO object identifiers. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [AlternateIdentification (CDA Class)](StructureDefinition-AlternateIdentification.md), [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md)...Show 41 more,[AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md),[ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md),[Consent (CDA Class)](StructureDefinition-Consent.md),[Criterion (CDA Class)](StructureDefinition-Criterion.md),[CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md),[EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md),[Encounter (CDA Class)](StructureDefinition-Encounter.md),[Entity (CDA Class)](StructureDefinition-Entity.md),[ExternalAct (CDA Class)](StructureDefinition-ExternalAct.md),[ExternalDocument (CDA Class)](StructureDefinition-ExternalDocument.md),[ExternalObservation (CDA Class)](StructureDefinition-ExternalObservation.md),[ExternalProcedure (CDA Class)](StructureDefinition-ExternalProcedure.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md),[InFulfillmentOf1 (CDA Class)](StructureDefinition-InFulfillmentOf1.md),[InfrastructureRoot (Base Type for all CDA Classes)](StructureDefinition-InfrastructureRoot.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[LabPrecondition (CDA Class)](StructureDefinition-LabPrecondition.md),[ManufacturedProduct (CDA Class)](StructureDefinition-ManufacturedProduct.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Order (CDA Class)](StructureDefinition-Order.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[ParentDocument (CDA Class)](StructureDefinition-ParentDocument.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md),[PreconditionBase (CDA Class)](StructureDefinition-PreconditionBase.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md),[Section (CDA Class)](StructureDefinition-Section.md),[ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md),[SpecimenRole (CDA Class)](StructureDefinition-SpecimenRole.md),[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/II)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-II.csv), [Excel](StructureDefinition-II.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "II",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/II",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "II",
  "title" : "II: InstanceIdentifier (V3 Data Type)",
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
  "description" : "An identifier that uniquely identifies a thing or object. Examples are object identifier for HL7 RIM objects, medical record number, order id, service catalog item id, Vehicle Identification Number (VIN), etc. Instance identifiers are defined based on ISO object identifiers.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/II",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "II",
        "path" : "II",
        "definition" : "An identifier that uniquely identifies a thing or object. Examples are object identifier for HL7 RIM objects, medical record number, order id, service catalog item id, Vehicle Identification Number (VIN), etc. Instance identifiers are defined based on ISO object identifiers.",
        "min" : 1,
        "max" : "*",
        "constraint" : [
          {
            "key" : "II-1",
            "severity" : "error",
            "human" : "An II instance must have either a root or an nullFlavor.",
            "expression" : "root.exists() or nullFlavor.exists()"
          }
        ]
      },
      {
        "id" : "II.assigningAuthorityName",
        "path" : "II.assigningAuthorityName",
        "representation" : ["xmlAttr"],
        "label" : "Assigning Authority Name",
        "definition" : "A human readable name or mnemonic for the assigning authority. The Assigning Authority Name has no computational value. The purpose of a Assigning Authority Name is to assist an unaided human interpreter of an II value to interpret the authority. Note: no automated processing must depend on the assigning authority name to be present in any form.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      },
      {
        "id" : "II.displayable",
        "path" : "II.displayable",
        "representation" : ["xmlAttr"],
        "label" : "Displayable",
        "definition" : "Specifies if the identifier is intended for human display and data entry (displayable = true) as opposed to pure machine interoperation (displayable = false).",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "boolean",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/bl-simple"]
          }
        ]
      },
      {
        "id" : "II.root",
        "path" : "II.root",
        "representation" : ["xmlAttr"],
        "label" : "Root",
        "definition" : "A unique identifier that guarantees the global uniqueness of the instance identifier. The root alone may be the entire instance identifier.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : [
              "http://hl7.org/cda/stds/core/StructureDefinition/oid",
              "http://hl7.org/cda/stds/core/StructureDefinition/uuid",
              "http://hl7.org/cda/stds/core/StructureDefinition/ruid"
            ]
          }
        ]
      },
      {
        "id" : "II.extension",
        "path" : "II.extension",
        "representation" : ["xmlAttr"],
        "label" : "Extension",
        "definition" : "A character string as a unique identifier within the scope of the identifier root.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      }
    ]
  }
}

```
