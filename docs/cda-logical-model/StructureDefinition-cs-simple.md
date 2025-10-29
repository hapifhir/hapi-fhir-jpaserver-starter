# cs: Coded Simple Value - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **cs: Coded Simple Value**

## Data Type Profile: cs: Coded Simple Value 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/cs-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:cs |

 
Coded data in its simplest form, consists of a code. The code system and code system version is fixed by the context in which the value occurs. 'cs' is used for coded attributes that have a single HL7-defined value set. 

**Usages:**

* Use this Primitive Type Profile: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [ADXP: CharacterString (V3 Data Type)](StructureDefinition-ADXP.md), [ANY: DataValue (V3 Data Type)](StructureDefinition-ANY.md), [Act (CDA Class)](StructureDefinition-Act.md)...Show 94 more,[AlternateIdentification (CDA Class)](StructureDefinition-AlternateIdentification.md),[AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md),[AssignedCustodian (CDA Class)](StructureDefinition-AssignedCustodian.md),[AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md),[AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md),[Authenticator (CDA Class)](StructureDefinition-Authenticator.md),[Author (CDA Class)](StructureDefinition-Author.md),[AuthoringDevice (CDA Class)](StructureDefinition-AuthoringDevice.md),[Authorization (CDA Class)](StructureDefinition-Authorization.md),[Birthplace (CDA Class)](StructureDefinition-Birthplace.md),[CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md),[ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md),[Component (CDA Class)](StructureDefinition-Component.md),[ComponentOf (CDA Class)](StructureDefinition-ComponentOf.md),[Consent (CDA Class)](StructureDefinition-Consent.md),[Criterion (CDA Class)](StructureDefinition-Criterion.md),[Custodian (CDA Class)](StructureDefinition-Custodian.md),[CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md),[DataEnterer (CDA Class)](StructureDefinition-DataEnterer.md),[Device (CDA Class)](StructureDefinition-Device.md),[DocumentationOf (CDA Class)](StructureDefinition-DocumentationOf.md),[ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md),[EN: EntityName (V3 Data Type)](StructureDefinition-EN.md),[ENXP: Entity Name Part (V3 Data Type)](StructureDefinition-ENXP.md),[EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md),[Encounter (CDA Class)](StructureDefinition-Encounter.md),[EncounterParticipant (CDA Class)](StructureDefinition-EncounterParticipant.md),[Entity (CDA Class)](StructureDefinition-Entity.md),[Entry (CDA Class)](StructureDefinition-Entry.md),[EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md),[ExternalAct (CDA Class)](StructureDefinition-ExternalAct.md),[ExternalDocument (CDA Class)](StructureDefinition-ExternalDocument.md),[ExternalObservation (CDA Class)](StructureDefinition-ExternalObservation.md),[ExternalProcedure (CDA Class)](StructureDefinition-ExternalProcedure.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md),[IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md),[IVL_PQ: Interval (V3 Data Type)](StructureDefinition-IVL-PQ.md),[InFulfillmentOf (CDA Class)](StructureDefinition-InFulfillmentOf.md),[InFulfillmentOf1 (CDA Class)](StructureDefinition-InFulfillmentOf1.md),[Informant (CDA Class)](StructureDefinition-Informant.md),[InformationRecipient (CDA Class)](StructureDefinition-InformationRecipient.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[LabeledDrug (CDA Class)](StructureDefinition-LabeledDrug.md),[LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md),[MO: MonetaryAmount (V3 Data Type)](StructureDefinition-MO.md),[MaintainedEntity (CDA Class)](StructureDefinition-MaintainedEntity.md),[ManufacturedProduct (CDA Class)](StructureDefinition-ManufacturedProduct.md),[Material (CDA Class)](StructureDefinition-Material.md),[NonXMLBody (CDA Class)](StructureDefinition-NonXMLBody.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Order (CDA Class)](StructureDefinition-Order.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md),[PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)](StructureDefinition-PIVL-TS.md),[PQ: PhysicalQuantity (V3 Data Type)](StructureDefinition-PQ.md),[ParentDocument (CDA Class)](StructureDefinition-ParentDocument.md),[Participant1 (CDA Class)](StructureDefinition-Participant1.md),[Participant2 (CDA Class)](StructureDefinition-Participant2.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md),[Performer1 (CDA Class)](StructureDefinition-Performer1.md),[Performer2 (CDA Class)](StructureDefinition-Performer2.md),[Person (CDA Class)](StructureDefinition-Person.md),[Place (CDA Class)](StructureDefinition-Place.md),[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md),[Precondition (CDA Class)](StructureDefinition-Precondition.md),[Precondition2 (CDA Class)](StructureDefinition-Precondition2.md),[PreconditionBase (CDA Class)](StructureDefinition-PreconditionBase.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RecordTarget (CDA Class)](StructureDefinition-RecordTarget.md),[Reference (CDA Class)](StructureDefinition-Reference.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[RelatedDocument (CDA Class)](StructureDefinition-RelatedDocument.md),[RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md),[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md),[SC: CharacterStringWithCode (V3 Data Type)](StructureDefinition-SC.md),[ST: CharacterString (V3 Data Type)](StructureDefinition-ST.md),[SXCM_TS: GeneralTimingSpecification (V3 Data Type)](StructureDefinition-SXCM-TS.md),[Section (CDA Class)](StructureDefinition-Section.md),[ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md),[Specimen (CDA Class)](StructureDefinition-Specimen.md),[SpecimenRole (CDA Class)](StructureDefinition-SpecimenRole.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md),[Subject (CDA Class)](StructureDefinition-Subject.md),[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md),[Supply (CDA Class)](StructureDefinition-Supply.md)and[TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/cs-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-cs-simple.csv), [Excel](StructureDefinition-cs-simple.xlsx), [Schematron](StructureDefinition-cs-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "cs-simple",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/cs-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "cs",
  "title" : "cs: Coded Simple Value",
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
  "description" : "Coded data in its simplest form, consists of a code. The code system and code system version is fixed by the context in which the value occurs. 'cs' is used for coded attributes that have a single HL7-defined value set.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "code",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/code",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "code",
        "path" : "code",
        "constraint" : [
          {
            "key" : "cs-pattern",
            "severity" : "error",
            "human" : "cs attributes must not contain any whitespace",
            "expression" : "matches('^[^\\\\s]+$')"
          }
        ]
      },
      {
        "id" : "code.id",
        "path" : "code.id",
        "max" : "0"
      },
      {
        "id" : "code.extension",
        "path" : "code.extension",
        "max" : "0"
      }
    ]
  }
}

```
