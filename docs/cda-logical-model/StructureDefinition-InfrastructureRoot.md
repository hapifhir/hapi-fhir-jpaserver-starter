# InfrastructureRoot (Base Type for all CDA Classes) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **InfrastructureRoot (Base Type for all CDA Classes)**

## Logical Model: InfrastructureRoot (Base Type for all CDA Classes) ( Abstract ) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:InfrastructureRoot |

 
Defines the base elements and attributes on all CDA elements (other than data types) 

**Usages:**

* Derived from this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedCustodian (CDA Class)](StructureDefinition-AssignedCustodian.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md)...Show 77 more,[AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md),[Authenticator (CDA Class)](StructureDefinition-Authenticator.md),[Author (CDA Class)](StructureDefinition-Author.md),[AuthoringDevice (CDA Class)](StructureDefinition-AuthoringDevice.md),[Authorization (CDA Class)](StructureDefinition-Authorization.md),[Birthplace (CDA Class)](StructureDefinition-Birthplace.md),[Component (CDA Class)](StructureDefinition-Component.md),[ComponentOf (CDA Class)](StructureDefinition-ComponentOf.md),[Consent (CDA Class)](StructureDefinition-Consent.md),[Criterion (CDA Class)](StructureDefinition-Criterion.md),[Custodian (CDA Class)](StructureDefinition-Custodian.md),[CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md),[DataEnterer (CDA Class)](StructureDefinition-DataEnterer.md),[Device (CDA Class)](StructureDefinition-Device.md),[DocumentationOf (CDA Class)](StructureDefinition-DocumentationOf.md),[EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md),[Encounter (CDA Class)](StructureDefinition-Encounter.md),[EncounterParticipant (CDA Class)](StructureDefinition-EncounterParticipant.md),[Entity (CDA Class)](StructureDefinition-Entity.md),[Entry (CDA Class)](StructureDefinition-Entry.md),[EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md),[ExternalAct (CDA Class)](StructureDefinition-ExternalAct.md),[ExternalDocument (CDA Class)](StructureDefinition-ExternalDocument.md),[ExternalObservation (CDA Class)](StructureDefinition-ExternalObservation.md),[ExternalProcedure (CDA Class)](StructureDefinition-ExternalProcedure.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md),[InFulfillmentOf (CDA Class)](StructureDefinition-InFulfillmentOf.md),[InFulfillmentOf1 (CDA Class)](StructureDefinition-InFulfillmentOf1.md),[Informant (CDA Class)](StructureDefinition-Informant.md),[InformationRecipient (CDA Class)](StructureDefinition-InformationRecipient.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[LabeledDrug (CDA Class)](StructureDefinition-LabeledDrug.md),[LanguageCommunication (CDA Class)](StructureDefinition-LanguageCommunication.md),[LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md),[MaintainedEntity (CDA Class)](StructureDefinition-MaintainedEntity.md),[ManufacturedProduct (CDA Class)](StructureDefinition-ManufacturedProduct.md),[Material (CDA Class)](StructureDefinition-Material.md),[NonXMLBody (CDA Class)](StructureDefinition-NonXMLBody.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Order (CDA Class)](StructureDefinition-Order.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md),[ParentDocument (CDA Class)](StructureDefinition-ParentDocument.md),[Participant1 (CDA Class)](StructureDefinition-Participant1.md),[Participant2 (CDA Class)](StructureDefinition-Participant2.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md),[Performer1 (CDA Class)](StructureDefinition-Performer1.md),[Performer2 (CDA Class)](StructureDefinition-Performer2.md),[Person (CDA Class)](StructureDefinition-Person.md),[Place (CDA Class)](StructureDefinition-Place.md),[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md),[Precondition (CDA Class)](StructureDefinition-Precondition.md),[Precondition2 (CDA Class)](StructureDefinition-Precondition2.md),[PreconditionBase (CDA Class)](StructureDefinition-PreconditionBase.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RecordTarget (CDA Class)](StructureDefinition-RecordTarget.md),[Reference (CDA Class)](StructureDefinition-Reference.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[RelatedDocument (CDA Class)](StructureDefinition-RelatedDocument.md),[RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md),[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md),[Section (CDA Class)](StructureDefinition-Section.md),[ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md),[Specimen (CDA Class)](StructureDefinition-Specimen.md),[SpecimenRole (CDA Class)](StructureDefinition-SpecimenRole.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md),[Subject (CDA Class)](StructureDefinition-Subject.md),[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)
* Use this Logical Model: [EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md), [InFulfillmentOf1 (CDA Class)](StructureDefinition-InFulfillmentOf1.md), [Observation (CDA Class)](StructureDefinition-Observation.md), [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)...Show 5 more,[Person (CDA Class)](StructureDefinition-Person.md),[Section (CDA Class)](StructureDefinition-Section.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/InfrastructureRoot)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-InfrastructureRoot.csv), [Excel](StructureDefinition-InfrastructureRoot.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "InfrastructureRoot",
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
      "valueString" : "infrastructureRoot"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "InfrastructureRoot",
  "title" : "InfrastructureRoot (Base Type for all CDA Classes)",
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
  "description" : "Defines the base elements and attributes on all CDA elements (other than data types)",
  "fhirVersion" : "5.0.0",
  "mapping" : [
    {
      "identity" : "rim",
      "uri" : "http://hl7.org/v3",
      "name" : "RIM Mapping"
    }
  ],
  "kind" : "logical",
  "abstract" : true,
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "InfrastructureRoot",
        "path" : "InfrastructureRoot",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "InfrastructureRoot.realmCode",
        "path" : "InfrastructureRoot.realmCode",
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
        "id" : "InfrastructureRoot.typeId",
        "path" : "InfrastructureRoot.typeId",
        "definition" : "When valued in an instance, this attribute signals the imposition of constraints defined in an HL7-specified message type. This might be a common type (also known as CMET in the messaging communication environment), or content included within a wrapper. The value of this attribute provides a unique identifier for the type in question.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "InfrastructureRoot.typeId.root",
        "path" : "InfrastructureRoot.typeId.root",
        "representation" : ["xmlAttr"],
        "definition" : "Identifies the type as an HL7 Registered model",
        "min" : 1,
        "max" : "1",
        "fixedString" : "2.16.840.1.113883.1.3"
      },
      {
        "id" : "InfrastructureRoot.typeId.extension",
        "path" : "InfrastructureRoot.typeId.extension",
        "representation" : ["xmlAttr"],
        "definition" : "A character string as a unique identifier within the scope of the identifier root.",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      },
      {
        "id" : "InfrastructureRoot.templateId",
        "path" : "InfrastructureRoot.templateId",
        "definition" : "When valued in an instance, this attribute signals the imposition of a set of template-defined constraints. The value of this attribute provides a unique identifier for the templates in question",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      }
    ]
  }
}

```
