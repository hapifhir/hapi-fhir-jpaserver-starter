# IG Home Page - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* **IG Home Page**

## IG Home Page

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ImplementationGuide/hl7.cda.uv.core | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ClinicalDocumentArchitecture |

### MATCHBOX CDA definition

This Implementation Guide is a representation of the [Clinical Document Architecture (CDA) R2.0 specification](https://hl7.org/cda/stds/online-navigation/index.html) using FHIR Logical Models expressed as FHIR StructureDefinition instances.

20240619: This CDA Model is derived from [HL7](https://hl7.org/cda/stds/core/) [2.0.0-sd](https://hl7.org/cda/stds/core/2.0.0-sd/)

For use with matchbox the following adaptions have been made:

#### IHE LAB and Pharm additions

[LabCriterion](StructureDefinition-LabCriterion.md)
 [LabPrecondition](StructureDefinition-LabPrecondition.md)
 [Material](StructureDefinition-Material.md)
 [ObservationRange](StructureDefinition-ObservationRange.md)
 [Person](StructureDefinition-Person.md)
 [PharmContent](StructureDefinition-PharmContent.md)
 [PharmIngredient](StructureDefinition-PharmIngredient.md)
 [PharmMedicineClass](StructureDefinition-PharmMedicineClass.md)
 [PharmPackagedMedicine](StructureDefinition-PharmPackagedMedicine.md)
 [PharmSpecializedKind](StructureDefinition-PharmSpecializedKind.md)
 [PharmSubstance](StructureDefinition-PharmSubstance.md)
 [PharmSuperContent](StructureDefinition-PharmSuperContent.md)
 [ServiceEvent](StructureDefinition-ServiceEvent.md)

#### Versionless dependency to terminology

* see [Pull Request](https://github.com/HL7/CDA-core-sd/pull/15)

#### Australian addition

[CS Support in Observation.value](StructureDefinition-Observation.md)

#### Austria additions

hl7at extensions: [ClinicalDocument.terminologyDate, ClinicalDocument.formatCode, ClinicalDocument.practiceSettingCode](StructureDefinition-ClinicalDocument.md)

max card. changed from 1 to * for [ClinicalDocument.legalAuthenticator](StructureDefinition-ClinicalDocument.md) and max card. changed from 1 to * [CustodianOrganization.telecom](StructureDefinition-CustodianOrganization.md)

#### Italian additions

[ST.r2b for Observation.value xsi:type](StructureDefinition-ST-dot-r2b.md)

### CDA definition

This guide does not replace the CDA specification. It includes the Overview, Implementation Notes, and Narrative Block information from the specification to provide context and guidance. To understand CDA, readers should consult the actual CDA specification. If there are any differences found between the specification and this guide, the specification takes precedence and is assumed to be correct.

| | | |
| :--- | :--- | :--- |
| ### CDA Classes* [**ClinicalDocument**](StructureDefinition-ClinicalDocument.md)
* [Act](StructureDefinition-Act.md)
* [AlternateIdentification](StructureDefinition-AlternateIdentification.md)
* [AssignedAuthor](StructureDefinition-AssignedAuthor.md)
* [AssignedCustodian](StructureDefinition-AssignedCustodian.md)
* [AssignedEntity](StructureDefinition-AssignedEntity.md)
* [AssociatedEntity](StructureDefinition-AssociatedEntity.md)
* [Authenticator](StructureDefinition-Authenticator.md)
* [Author](StructureDefinition-Author.md)
* [AuthoringDevice](StructureDefinition-AuthoringDevice.md)
* [Authorization](StructureDefinition-Authorization.md)
* [Birthplace](StructureDefinition-Birthplace.md)
* [Component](StructureDefinition-Component.md)
* [ComponentOf](StructureDefinition-ComponentOf.md)
* [Consent](StructureDefinition-Consent.md)
* [Criterion](StructureDefinition-Criterion.md)
* [Custodian](StructureDefinition-Custodian.md)
* [CustodianOrganization](StructureDefinition-CustodianOrganization.md)
* [DataEnterer](StructureDefinition-DataEnterer.md)
* [Device](StructureDefinition-Device.md)
* [DocumentationOf](StructureDefinition-DocumentationOf.md)
* [EncompassingEncounter](StructureDefinition-EncompassingEncounter.md)
* [Encounter](StructureDefinition-Encounter.md)
* [EncounterParticipant](StructureDefinition-EncounterParticipant.md)
* [Entity](StructureDefinition-Entity.md)
* [Entry](StructureDefinition-Entry.md)
* [EntryRelationship](StructureDefinition-EntryRelationship.md)
* [ExternalAct](StructureDefinition-ExternalAct.md)
* [ExternalDocument](StructureDefinition-ExternalDocument.md)
* [ExternalObservation](StructureDefinition-ExternalObservation.md)
* [ExternalProcedure](StructureDefinition-ExternalProcedure.md)
* [Guardian](StructureDefinition-Guardian.md)
* [HealthCareFacility](StructureDefinition-HealthCareFacility.md)
* [IdentifiedBy](StructureDefinition-IdentifiedBy.md)
* [InFulfillmentOf](StructureDefinition-InFulfillmentOf.md)
* [InFulfillmentOf1](StructureDefinition-InFulfillmentOf1.md)
* [Informant](StructureDefinition-Informant.md)
* [InformationRecipient](StructureDefinition-InformationRecipient.md)
* [InfrastructureRoot](StructureDefinition-InfrastructureRoot.md)
* [IntendedRecipient](StructureDefinition-IntendedRecipient.md)
* [LabeledDrug](StructureDefinition-LabeledDrug.md)
* [LanguageCommunication](StructureDefinition-LanguageCommunication.md)
* [LegalAuthenticator](StructureDefinition-LegalAuthenticator.md)
* [MaintainedEntity](StructureDefinition-MaintainedEntity.md)
* [ManufacturedProduct](StructureDefinition-ManufacturedProduct.md)
* [Material](StructureDefinition-Material.md)
* [NonXMLBody](StructureDefinition-NonXMLBody.md)
* [Observation](StructureDefinition-Observation.md)
* [ObservationMedia](StructureDefinition-ObservationMedia.md)
* [ObservationRange](StructureDefinition-ObservationRange.md)
* [Order](StructureDefinition-Order.md)
* [Organization](StructureDefinition-Organization.md)
* [OrganizationPartOf](StructureDefinition-OrganizationPartOf.md)
* [Organizer](StructureDefinition-Organizer.md)
* [OrganizerComponent](StructureDefinition-OrganizerComponent.md)
* [ParentDocument](StructureDefinition-ParentDocument.md)
* [Participant1](StructureDefinition-Participant1.md)
* [Participant2](StructureDefinition-Participant2.md)
* [ParticipantRole](StructureDefinition-ParticipantRole.md)
* [Patient](StructureDefinition-Patient.md)
* [PatientRole](StructureDefinition-PatientRole.md)
* [Performer1](StructureDefinition-Performer1.md)
* [Performer2](StructureDefinition-Performer2.md)
* [Person](StructureDefinition-Person.md)
* [Place](StructureDefinition-Place.md)
* [PlayingEntity](StructureDefinition-PlayingEntity.md)
* [Precondition](StructureDefinition-Precondition.md)
* [Precondition2](StructureDefinition-Precondition2.md)
* [Procedure](StructureDefinition-Procedure.md)
* [RecordTarget](StructureDefinition-RecordTarget.md)
* [Reference](StructureDefinition-Reference.md)
* [RegionOfInterest](StructureDefinition-RegionOfInterest.md)
* [RelatedDocument](StructureDefinition-RelatedDocument.md)
* [RelatedEntity](StructureDefinition-RelatedEntity.md)
* [RelatedSubject](StructureDefinition-RelatedSubject.md)
* [Section](StructureDefinition-Section.md)
* [ServiceEvent](StructureDefinition-ServiceEvent.md)
* [Specimen](StructureDefinition-Specimen.md)
* [SpecimenRole](StructureDefinition-SpecimenRole.md)
* [StructuredBody](StructureDefinition-StructuredBody.md)
* [Subject](StructureDefinition-Subject.md)
* [SubjectPerson](StructureDefinition-SubjectPerson.md)
* [SubstanceAdministration](StructureDefinition-SubstanceAdministration.md)
* [Supply](StructureDefinition-Supply.md)
 | ### V3 Complex Data Types* [AD: PostalAddress](StructureDefinition-AD.md)
* [ADXP: CharacterString](StructureDefinition-ADXP.md)
* [ANY: DataValue](StructureDefinition-ANY.md)
* [BL: Boolean](StructureDefinition-BL.md)
* [CD: ConceptDescriptor](StructureDefinition-CD.md)
* [CE: CodedWithEquivalents](StructureDefinition-CE.md)
* [CO: CodedOrdinal](StructureDefinition-CO.md)
* [CR: ConceptRole](StructureDefinition-CR.md)
* [CS: CodedSimpleValue](StructureDefinition-CS.md)
* [CV: CodedValue](StructureDefinition-CV.md)
* [ED: EncapsulatedData](StructureDefinition-ED.md)
* [EIVL_TS: EventRelatedPeriodicInterval](StructureDefinition-EIVL-TS.md)
* [EN: EntityName](StructureDefinition-EN.md)
* [ENXP: Entity Name Part](StructureDefinition-ENXP.md)
* [II: InstanceIdentifier](StructureDefinition-II.md)
* [INT_POS: Positive integer numbers](StructureDefinition-INT-POS.md)
* [INT: IntegerNumber](StructureDefinition-INT.md)
* [IVL_INT: Interval](StructureDefinition-IVL-INT.md)
* [IVL_PQ: Interval](StructureDefinition-IVL-PQ.md)
* [IVL_TS: Interval](StructureDefinition-IVL-TS.md)
* [IVXB_INT: Interval Boundary IntegerNumber](StructureDefinition-IVXB-INT.md)
* [IVXB_PQ: Interval Boundary PhysicalQuantity](StructureDefinition-IVXB-PQ.md)
* [IVXB_TS: Interval Boundary PointInTime](StructureDefinition-IVXB-TS.md)
* [MO: MonetaryAmount](StructureDefinition-MO.md)
* [ON: OrganizationName](StructureDefinition-ON.md)
* [PIVL_TS: PeriodicIntervalOfTime](StructureDefinition-PIVL-TS.md)
* [PN: PersonName](StructureDefinition-PN.md)
* [PQ: PhysicalQuantity](StructureDefinition-PQ.md)
* [PQR: PhysicalQuantityRepresentation](StructureDefinition-PQR.md)
* [QTY: Quantity](StructureDefinition-QTY.md)
* [REAL: RealNumber](StructureDefinition-REAL.md)
* [RTO_PQ_PQ: Ratio](StructureDefinition-RTO-PQ-PQ.md)
* [SC: CharacterStringWithCode](StructureDefinition-SC.md)
* [ST: CharacterString](StructureDefinition-ST.md)
* [SXCM_TS: GeneralTimingSpecification](StructureDefinition-SXCM-TS.md)
* [SXPR_TS: Component part of GTS](StructureDefinition-SXPR-TS.md)
* [TEL: TelecommunicationAddress](StructureDefinition-TEL.md)
* [TN: TrivialName](StructureDefinition-TN.md)
* [TS: PointInTime](StructureDefinition-TS.md)
 | ### V3 Simple Data Types* [bin: Binary Data](StructureDefinition-bin.md)
* [bl: Boolean](StructureDefinition-bl-simple.md)
* [bn: BooleanNonNull](StructureDefinition-bn.md)
* [cs: Coded Simple Value](StructureDefinition-cs-simple.md)
* [int: Integer Number](StructureDefinition-int-simple.md)
* [oid: ISO Object Identifier](StructureDefinition-oid.md)
* [probability: Probability](StructureDefinition-probability.md)
* [real: Real Number](StructureDefinition-real-simple.md)
* [ruid: HL7 Reserved Identifier Scheme](StructureDefinition-ruid.md)
* [st: Character String](StructureDefinition-st-simple.md)
* [ts: Point in Time](StructureDefinition-ts-simple.md)
* [uid: Unique Identifier String](StructureDefinition-uid.md)
* [url: Universal Resource Locator](StructureDefinition-url.md)
* [uuid: DCE Universal Unique Identifier](StructureDefinition-uuid.md)
 |

### CDA Extensions

This guide also incorporates the [approved SDTC extensions](https://confluence.hl7.org/display/SD/CDA+Extensions). Elements from the extensions will be found with 'sdtc' before their name. They also are defined to be in the `urn:hl7-org:sdtc` namespace and that is visible in the structure pages. [Custodian Organization](StructureDefinition-CustodianOrganization.md) has an example of an extension element (sdtcTelecom). Note that while extensions are prefixed with 'sdtc', their actual XML name does not include this. Their XML name is displayed in the structure pages as `XML`. For example, the CustodianOrganization's sdtcTelecom would appear in an instance as either `<telecom xmlns="urn:hl7-org:sdtc" value="...." />` or in a document with a defined prefix for sdtc:

```
<ClinicalDocument xmlns="urn:hl7-org:v3" xmlns:sdtc="urn:hl7-org:v3/voc">
	<custodian>
		<assignedCustodian>
			<representedCustodianOrganization>
				<sdtc:telecom value="..." />
	...

```

### CDA Example

An [example of a CDA document](Binary-clinicaldocument-example.md) has been provided along with a [transformed version of the example](transformed-example.md) using the [informative CDA stylesheet](https://github.com/HL7/cda-core-xsl).

### CDA Validation

With the representation of the CDA structures using FHIR StructureDefinitions, there is now an option on how to validate CDA documents. The CDA schemas are still valid and can be [found here](https://github.com/HL7/CDA-core-2.0). Additionally, by pointing the FHIR validator at this guide, CDA instances can be validated using FHIR validators.

#### FHIRPath Supplements

The FHIRPath language defines a set of contexts that get passed into expressions and also allows the definition of additional contexts and functions. CDA provides the following supplemental guidance for evaluating FHIRPath:

* The `%resource` variable when it appears in expressions on CDA profiles will be evaluated as the root `ClinicalDocument`.
* A new function: `hasTemplateIdOf([ProfileUrl])` evaluates to true or false based on whether the XML contains a `<templateId />` element corresponding to the identifier of a particular profile.

For example, if a profile like `http://hl7.org/cda/us/custom/StructureDefinition/ExampleSection` contains an identifier property like `urn:hl7ii:2.16.840.1.113883.10.20.22.99.999:2024-05-01`, then the following XPath:

`%resource.component.structuredBody.component.where(section.hasTemplateIdOf('http://hl7.org/cda/us/custom/StructureDefinition/ExampleSection'))`

will return true if the document contains a section with the templateId of Example Section.

It is equivalent to the following, but allows an IG author to easily update the templateId extensions without finding-and-replacing constraint expressions:

`%resource.component.structuredBody.component.where(section.templateId.where(root = '2.16.840.1.113883.10.20.22.99.999' and extension = '2024-05-01'))`

#### Implementation Guide Parameters

Parameters from the [IG Parameters CDA Validation Code System](CodeSystem-IGParametersCDAValidation.md) may be included to control the behavior of Schematron generation in CDA implementation guides written in FHIR StructureDefinition format.

### Authors

The current specification lists the following people as editors/authors:

* Robert H. Dolin, MD
* Liora Alschuler
* Sandy Boyer, BSP
* Calvin Beebe
* Fred M. Behlen, PhD
* Paul V. Biron
* Amnon Shabo (Shvo), PhD

This guide has the following authors:

* Jean Duteau
* Rosemary Hofstede
* Benjamin Flessner
* Susan Rand

The CDA community also benefits from the following people who have contributed to the guide:

* Austin Kreisler
* John D'Amore
* Lisa Nelson
* Brett Marquard
* Gay Dolin
* Matt Szczepankiewicz

### Other Information

This publication includes IP covered under the following statements.

* This material contains content from [LOINC](http://loinc.org). LOINC is copyright © 1995-2020, Regenstrief Institute, Inc. and the Logical Observation Identifiers Names and Codes (LOINC) Committee and is available at no cost under the [license](http://loinc.org/license). LOINC® is a registered United States trademark of Regenstrief Institute, Inc.

* [LOINC](http://terminology.hl7.org/5.3.0/CodeSystem-v3-loinc.html): [ClinicalDocument](StructureDefinition-ClinicalDocument.md), [ExternalDocument](StructureDefinition-ExternalDocument.md) and [ParentDocument](StructureDefinition-ParentDocument.md)


* This material derives from the HL7 Terminology (THO). THO is copyright ©1989+ Health Level Seven International and is made available under the CC0 designation. For more licensing information see: [https://terminology.hl7.org/license.html](https://terminology.hl7.org/license.html)

* [ActCode](http://tx.fhir.org/r5/ValueSet/v3-ActCode): [Act](StructureDefinition-Act.md), [CDAActSubstanceAdministrationCode](ValueSet-CDAActSubstanceAdministrationCode.md)...Show 15 more,[CDAR2.LabCriterion](StructureDefinition-LabCriterion.md),[Consent](StructureDefinition-Consent.md),[Criterion](StructureDefinition-Criterion.md),[EncompassingEncounter](StructureDefinition-EncompassingEncounter.md),[Encounter](StructureDefinition-Encounter.md),[ExternalAct](StructureDefinition-ExternalAct.md),[ExternalObservation](StructureDefinition-ExternalObservation.md),[ExternalProcedure](StructureDefinition-ExternalProcedure.md),[Observation](StructureDefinition-Observation.md),[ObservationRange](StructureDefinition-ObservationRange.md),[Order](StructureDefinition-Order.md),[Organizer](StructureDefinition-Organizer.md),[RegionOfInterest](StructureDefinition-RegionOfInterest.md),[SubstanceAdministration](StructureDefinition-SubstanceAdministration.md)and[Supply](StructureDefinition-Supply.md)


-------




-------

*There are no Global profiles defined*



## Resource Content

```json
{
  "resourceType" : "ImplementationGuide",
  "id" : "hl7.cda.uv.core",
  "url" : "http://hl7.org/cda/stds/core/ImplementationGuide/hl7.cda.uv.core",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ClinicalDocumentArchitecture",
  "title" : "Clinical Document Architecture",
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
  "packageId" : "hl7.cda.uv.core",
  "license" : "CC0-1.0",
  "fhirVersion" : ["5.0.0"],
  "dependsOn" : [
    {
      "id" : "hl7ext",
      "extension" : [
        {
          "url" : "http://hl7.org/fhir/tools/StructureDefinition/implementationguide-dependency-comment",
          "valueMarkdown" : "Automatically added as a dependency - all IGs depend on the HL7 Extension Pack"
        }
      ],
      "uri" : "http://hl7.org/fhir/extensions/ImplementationGuide/hl7.fhir.uv.extensions",
      "packageId" : "hl7.fhir.uv.extensions.r5",
      "version" : "5.2.0"
    },
    {
      "id" : "terminology",
      "uri" : "http://terminology.hl7.org/ImplementationGuide/hl7.terminology",
      "packageId" : "hl7.terminology",
      "version" : "5.3.0"
    }
  ],
  "definition" : {
    "extension" : [
      {
        "url" : "http://hl7.org/fhir/tools/StructureDefinition/ig-internal-dependency",
        "valueCode" : "hl7.fhir.uv.tools.r5#0.8.0"
      }
    ],
    "grouping" : [
      {
        "id" : "classes",
        "name" : "CDA Classes",
        "description" : "Primary CDA Objects"
      },
      {
        "id" : "datatypes-complex",
        "name" : "V3 Complex Data Types",
        "description" : "General-purpose complex types, which are re-usable clusters of elements"
      },
      {
        "id" : "datatypes-simple",
        "name" : "V3 Simple Data Types",
        "description" : "Simple / primitive types, which are single XML attributes"
      },
      {
        "id" : "datatypes-xml",
        "name" : "XML Data Types",
        "description" : "Other, non-CDA, data types used to support exchange of CDA documents"
      }
    ],
    "resource" : [
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Act"
        },
        "name" : "Act (CDA Class)",
        "description" : "A derivative of the RIM Act class, to be used when the other more specific classes aren't appropriate.\n\nAct.negationInd, when set to \"true\", is a positive assertion that the Act as a whole is negated. Some properties such as Act.id, Act.moodCode, and the participations are not affected. These properties always have the same meaning: i.e., the author remains the author of the negative Act. An act statement with negationInd is still a statement about the specific fact described by the Act. For instance, a negated \"finding of wheezing on July 1\" means that the author positively denies that there was wheezing on July 1, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AD"
        },
        "name" : "AD: PostalAddress (V3 Data Type)",
        "description" : "Mailing and home or office addresses. A sequence of address parts, such as street or post office Box, city, postal code, country, etc.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ADXP"
        },
        "name" : "ADXP: CharacterString (V3 Data Type)",
        "description" : "A character string that may have a type-tag signifying its role in the address. Typical parts that exist in about every address are street, house number, or post box, postal code, city, country but other roles may be defined regionally, nationally, or on an enterprise level (e.g. in military addresses). Addresses are usually broken up into lines, which are indicated by special line-breaking delimiter elements (e.g., DEL).",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AlternateIdentification"
        },
        "name" : "AlternateIdentification (CDA Class)",
        "description" : "The alternateIdentification extension provides additional information about an identifier found in the linked role. The extensions augment the id information in the linked role.  The id in the alternateIdentification extension SHALL match an id in the linked role. The alternateIdentification provides additional information about a particular identifier, such as its type. As an extension it needs to be safe for implementers to ignore this additional information.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical:abstract"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ANY"
        },
        "name" : "ANY: DataValue (V3 Data Type)",
        "description" : "Defines the basic properties of every data value. This is an abstract type, meaning that no value can be just a data value without belonging to any concrete type. Every concrete type is a specialization of this general abstract DataValue type.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AssignedAuthor"
        },
        "name" : "AssignedAuthor (CDA Class)",
        "description" : "An author is a person in the role of an assigned author (AssignedAuthor class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AssignedCustodian"
        },
        "name" : "AssignedCustodian (CDA Class)",
        "description" : "A custodian is a scoping organization in the role of an assigned custodian (AssignedCustodian class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AssignedEntity"
        },
        "name" : "AssignedEntity (CDA Class)",
        "description" : "AssignedEntity (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AssociatedEntity"
        },
        "name" : "AssociatedEntity (CDA Class)",
        "description" : "A participant is a person or organization in the role of a participating entity (AssociatedEntity class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Authenticator"
        },
        "name" : "Authenticator (CDA Class)",
        "description" : "Represents a participant who has attested to the accuracy of the document, but who does not have privileges to legally authenticate the document. An example would be a resident physician who sees a patient and dictates a note, then later signs it. A clinical document can have zero to many authenticators. While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. An authenticator has a required authenticator.time indicating the time of authentication, and a required authenticator.signatureCode, indicating that a signature has been obtained and is on file.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Author"
        },
        "name" : "Author (CDA Class)",
        "description" : "Represents the humans and/or machines that authored the document. In some cases, the role or function of the author is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"Medical Student Progress Note\". The role of the author can also be recorded in the Author.functionCode or AssignedAuthor.code attribute. If either of these attributes is included, they should be equivalent to or further specialize the role inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply \"Physician Progress Note\" and the value of Author.functionCode is \"rounding physician\"), and shall not conflict with the role inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/AuthoringDevice"
        },
        "name" : "AuthoringDevice (CDA Class)",
        "description" : "AuthoringDevice (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Authorization"
        },
        "name" : "Authorization (CDA Class)",
        "description" : "Authorization (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Birthplace"
        },
        "name" : "Birthplace (CDA Class)",
        "description" : "A Patient's birthplace is represented as a relationship between a patient and a place. The Birthplace class is played by a place (Place class), and scoped by the patient (Patient class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/BL"
        },
        "name" : "BL: Boolean (V3 Data Type)",
        "description" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false, or, as any other value may be NULL.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CD"
        },
        "name" : "CD: ConceptDescriptor (V3 Data Type)",
        "description" : "A concept descriptor represents any kind of concept usually by giving a code defined in a code system. A concept descriptor can contain the original text or phrase that served as the basis of the coding and one or more translations into different coding systems. A concept descriptor can also contain qualifiers to describe, e.g., the concept of a \"left foot\" as a postcoordinated term built from the primary code \"FOOT\" and the qualifier \"LEFT\". In cases of an exceptional value, the concept descriptor need not contain a code but only the original text describing that concept.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CE"
        },
        "name" : "CE: CodedWithEquivalents (V3 Data Type)",
        "description" : "Coded data that consists of a coded value (CV) and, optionally, coded value(s) from other coding systems that identify the same concept. Used when alternative codes may exist.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ClinicalDocument"
        },
        "name" : "ClinicalDocument (CDA Class)",
        "description" : "This is a generated StructureDefinition that describes CDA - that is, CDA as it actually is for R2. The intent of this StructureDefinition is to enable CDA to be a FHIR resource. That enables the FHIR infrastructure - API, conformance, query - to be used directly against CDA",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/implementationguide-resource-format",
            "valueCode" : "application/xml"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "Binary"
          }
        ],
        "reference" : {
          "reference" : "Binary/clinicaldocument-example"
        },
        "name" : "Example CDA document",
        "description" : "Example CDA document from original CDA release",
        "isExample" : true,
        "profile" : [
          "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
        ]
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CO"
        },
        "name" : "CO: CodedOrdinal (V3 Data Type)",
        "description" : "Coded data, where the coding system from which the code comes is ordered",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Component"
        },
        "name" : "Component (CDA Class)",
        "description" : "Component (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ComponentOf"
        },
        "name" : "ComponentOf (CDA Class)",
        "description" : "ComponentOf (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Consent"
        },
        "name" : "Consent (CDA Class)",
        "description" : "This class references the consents associated with this document. The type of consent (e.g. a consent to perform the related ServiceEvent, a consent for the information contained in the document to be released to a third party) is conveyed in Consent.code. Consents referenced in the CDA Header have been finalized (Consent.statusCode must equal \"completed\") and should be on file.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CR"
        },
        "name" : "CR: ConceptRole (V3 Data Type)",
        "description" : "A concept qualifier code with optionally named role. Both qualifier role and value codes must be defined by the coding system of the CD containing the concept qualifier. For example, if SNOMED RT defines a concept \"leg\", a role relation \"has-laterality\", and another concept \"left\", the concept role relation allows to add the qualifier \"has-laterality: left\" to a primary code \"leg\" to construct the meaning \"left leg\".",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Criterion"
        },
        "name" : "Criterion (CDA Class)",
        "description" : "Criterion (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CS"
        },
        "name" : "CS: CodedSimpleValue (V3 Data Type)",
        "description" : "Coded data in its simplest form, where only the code is not predetermined. The code system and code system version are fixed by the context in which the CS value occurs. CS is used for coded attributes that have a single HL7-defined value set.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Custodian"
        },
        "name" : "Custodian (CDA Class)",
        "description" : "Represents the organization that is in charge of maintaining the document. The custodian is the steward that is entrusted with the care of the document. Every CDA document has exactly one custodian.The custodian participation satisfies the CDA definition of Stewardship (see What is the CDA (§ 1.1 )). Because CDA is an exchange standard and may not represent the original form of the authenticated document, the custodian represents the steward of the original source document.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CustodianOrganization"
        },
        "name" : "CustodianOrganization (CDA Class)",
        "description" : "The steward organization (CustodianOrganization class) is an entity scoping the role of AssignedCustodian, and has a required CustodianOrganization.id.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/CV"
        },
        "name" : "CV: CodedValue (V3 Data Type)",
        "description" : "Coded data, specifying only a code, code system, and optionally display name and original text. Used only as the data type for other data types' properties.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/DataEnterer"
        },
        "name" : "DataEnterer (CDA Class)",
        "description" : "Represents the participant who has transformed a dictated note into text.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Device"
        },
        "name" : "Device (CDA Class)",
        "description" : "A ManufacturedMaterial used in an activity without being substantially changed through that activity.\n\nThis includes durable (reusable) medical equipment as well as disposable equipment. The kind of device is identified by the code attribute inherited from Entity.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/DocumentationOf"
        },
        "name" : "DocumentationOf (CDA Class)",
        "description" : "TODO",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Performer1"
        },
        "name" : "Performer1 (CDA Class)",
        "description" : "TODO",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Performer2"
        },
        "name" : "Performer2 (CDA Class)",
        "description" : "TODO",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ED"
        },
        "name" : "ED: EncapsulatedData (V3 Data Type)",
        "description" : "Data that is primarily intended for human interpretation or for further machine processing outside the scope of HL7. This includes unformatted or formatted written language, multimedia data, or structured information in as defined by a different standard (e.g., XML-signatures.) Instead of the data itself, an may contain only a reference (see .) Note that the data type is a specialization of the data type when the media type is text/plain.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/EIVL-TS"
        },
        "name" : "EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)",
        "description" : "Specifies a periodic interval of time where the recurrence is based on activities of daily living or other important events that are time-related but not fully determined by time.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/EN"
        },
        "name" : "EN: EntityName (V3 Data Type)",
        "description" : "A name for a person, organization, place or thing. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for entity name values are \"Jim Bob Walton, Jr.\", \"Health Level Seven, Inc.\", \"Lake Tahoe\", etc. An entity name may be as simple as a character string or may consist of several entity name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\", \"Health Level Seven\" and \"Inc.\", \"Lake\" and \"Tahoe\".",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/EncompassingEncounter"
        },
        "name" : "EncompassingEncounter (CDA Class)",
        "description" : "This optional class represents the setting of the clinical encounter during which the documented act(s) or ServiceEvent occurred. Documents are not necessarily generated during an encounter, such as when a clinician, in response to an abnormal lab result, attempts to contact the patient but can't, and writes a Progress Note.\n\nIn some cases, the setting of the encounter is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"Diabetes Clinic Progress Note\". The setting of an encounter can also be transmitted in the HealthCareFacility.code attribute. If HealthCareFacility.code is sent, it should be equivalent to or further specialize the value inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply \"Clinic Progress Note\" and the value of HealthCareFacility.code is \"cardiology clinic\"), and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.\n\nEncompassingEncounter.dischargeDispositionCode can be used to depict the disposition of the patient at the time of hospital discharge (e.g., discharged to home, expired, against medical advice, etc.).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Encounter"
        },
        "name" : "Encounter (CDA Class)",
        "description" : "A derivative of the RIM PatientEncounter class, used to represent related encounters, such as follow-up visits or referenced past encounters.\n\nNOTE: The EncompassingEncounter class in the CDA Header (see Header Relationships (§ 4.2.3 )) represents the setting of the clinical encounter during which the documented act occurred. The Encounter class in the CDA Body is used to represent other related encounters.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/EncounterParticipant"
        },
        "name" : "EncounterParticipant (CDA Class)",
        "description" : "TODO",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Entity"
        },
        "name" : "Entity (CDA Class)",
        "description" : "A physical thing, group of physical things or an organization capable of participating in Acts while in a role.\n\nAn entity is a physical object that has, had or will have existence. The only exception to this is Organization, which while not having a physical presence, fulfills the other characteristics of an Entity. Entity stipulates the thing itself, not the Roles it may play: the Role of Patient, e.g., is played by the Person Entity.\n\nLiving subjects (including human beings), organizations, materials, places and their specializations.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Entry"
        },
        "name" : "Entry (CDA Class)",
        "description" : "CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/EntryRelationship"
        },
        "name" : "EntryRelationship (CDA Class)",
        "description" : "CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ENXP"
        },
        "name" : "ENXP: Entity Name Part (V3 Data Type)",
        "description" : "A character string token representing a part of a name. May have a type code signifying the role of the part in the whole entity name, and a qualifier code for more detail about the name part type. Typical name parts for person names are given names, and family names, titles, etc.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ExternalAct"
        },
        "name" : "ExternalAct (CDA Class)",
        "description" : "ExternalAct is a derivative of the RIM Act class, to be used when the other more specific classes are not appropriate.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ExternalDocument"
        },
        "name" : "ExternalDocument (CDA Class)",
        "description" : "ExternalDocument is a derivative of the RIM Document class, used for representing external documents. ExternalDocument.text is modeled as an ED data type - allowing for the expression of the MIME type of the external document.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ExternalObservation"
        },
        "name" : "ExternalObservation (CDA Class)",
        "description" : "ExternalObservation is a derivative of the RIM Observation class, used for representing external coded and other observations.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ExternalProcedure"
        },
        "name" : "ExternalProcedure (CDA Class)",
        "description" : "ExternalProcedure is a derivative of the RIM Procedure class, used for representing external procedures.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Guardian"
        },
        "name" : "Guardian (CDA Class)",
        "description" : "A patient's guardian is a person or organization in the role of guardian (Guardian class). The entity playing the role of guardian is a person (Person class) or organization (Organization class). The entity scoping the role is the patient (Patient class).\n\nWhere a guardian is not explicitly stated, the value should default to local business practice (e.g. the patient makes their own health care decisions unless incapacitated in which case healthcare decisions are made by the patient's spouse).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/HealthCareFacility"
        },
        "name" : "HealthCareFacility (CDA Class)",
        "description" : "HealthCareFacility (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IdentifiedBy"
        },
        "name" : "IdentifiedBy (CDA Class)",
        "description" : "The alternateIdentification extension provides additional information about an identifier found in the linked role. The extensions augment the id information in the linked role.  The id in the alternateIdentification extension SHALL match an id in the linked role. The alternateIdentification provides additional information about a particular identifier, such as its type. As an extension it needs to be safe for implementers to ignore this additional information.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/II"
        },
        "name" : "II: InstanceIdentifier (V3 Data Type)",
        "description" : "An identifier that uniquely identifies a thing or object. Examples are object identifier for HL7 RIM objects, medical record number, order id, service catalog item id, Vehicle Identification Number (VIN), etc. Instance identifiers are defined based on ISO object identifiers.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Informant"
        },
        "name" : "Informant (CDA Class)",
        "description" : "An informant (or source of information) is a person that provides relevant information, such as the parent of a comatose patient who describes the patient's behavior prior to the onset of coma.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/InformationRecipient"
        },
        "name" : "InformationRecipient (CDA Class)",
        "description" : "Represents the participant who has transformed a dictated note into text.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical:abstract"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/InfrastructureRoot"
        },
        "name" : "InfrastructureRoot (Base Type for all CDA Classes)",
        "description" : "Defines the base elements and attributes on all CDA elements (other than data types)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/InFulfillmentOf"
        },
        "name" : "InFulfillmentOf (CDA Class)",
        "description" : "This class represents those orders that are fulfilled by this document. For instance, a provider orders an X-Ray. The X-Ray is performed. A radiologist reads the X-Ray and generates a report. The X-Ray order identifier is transmitted in the Order class, the performed X-Ray procedure is transmitted in the ServiceEvent class, and the ClinicalDocument.code would be valued with \"Diagnostic Imaging Report\".",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/InFulfillmentOf1"
        },
        "name" : "InFulfillmentOf1 (CDA Class)",
        "description" : "This is an actRelationship called inFulfillmentOf1 that represents the Fulfills General Relationship Operator in QDM 4.1.x in QDM-Base QRDA Category 1, R3.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/INT"
        },
        "name" : "INT: IntegerNumber (V3 Data Type)",
        "description" : "Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable. No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/INT-POS"
        },
        "name" : "INT_POS: Positive integer numbers (V3 Data Type)",
        "description" : "Positive integer numbers.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IntendedRecipient"
        },
        "name" : "IntendedRecipient (CDA Class)",
        "description" : "IntendedRecipient (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVL-INT"
        },
        "name" : "IVL_INT: Interval (V3 Data Type)",
        "description" : "A set of consecutive values of an ordered base data type.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVL-PQ"
        },
        "name" : "IVL_PQ: Interval (V3 Data Type)",
        "description" : "A set of consecutive values of an ordered base data type.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVL-TS"
        },
        "name" : "IVL_TS: Interval (V3 Data Type)",
        "description" : "A set of consecutive values of an ordered base data type.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVXB-INT"
        },
        "name" : "IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type)",
        "description" : "An integer interval boundary containing an inclusive/exclusive flag.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVXB-PQ"
        },
        "name" : "IVXB_PQ: Interval Boundary PhysicalQuantity (V3 Data Type)",
        "description" : "A physical quantity interval boundary containing an inclusive/exclusive flag.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/IVXB-TS"
        },
        "name" : "IVXB_TS: Interval Boundary PointInTime (V3 Data Type)",
        "description" : "A timestamp interval boundary containing an inclusive/exclusive flag.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/LabCriterion"
        },
        "name" : "LabCriterion (CDA Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/LabPrecondition"
        },
        "name" : "LabPrecondition (CDA Class)",
        "description" : "The precondition class, derived from the ActRelationship class, is used along with the LabPrecondition class to express a condition that must hold true before some over activity occurs."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/LabeledDrug"
        },
        "name" : "LabeledDrug (CDA Class)",
        "description" : "The LabeledDrug class, which is an Entity class playing the Role of Manufactured Product, identifies the drug that is consumed in the substance administration. The medication is identified by means of the LabeledDrug.code or the LabeledDrug.name.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/LanguageCommunication"
        },
        "name" : "LanguageCommunication (CDA Class)",
        "description" : "The language communication capabilities of an Entity.\n\nWhile it may seem on the surface that this class would be restricted in usage to only the LivingSubject subtypes, Devices also have the ability to communicate, such as automated telephony devices that transmit patient information to live operators on a triage line or provide automated laboratory results to clinicians.\n\nA patient who originally came from Mexico may have fluent language capabilities to speak, read and write in Spanish, and rudimentary capabilities in English. A person from Russia may have the capability to communicate equally well in spoken language in Russian, Armenian or Ukrainian, and a preference to speak in Armenian.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/LegalAuthenticator"
        },
        "name" : "LegalAuthenticator (CDA Class)",
        "description" : "Represents a participant who has legally authenticated the document. The CDA is a standard that specifies the structure of exchanged clinical documents. In the case where a local document is transformed into a CDA document for exchange, authentication occurs on the local document, and that fact is reflected in the exchanged CDA document. A CDA document can reflect the unauthenticated, authenticated, or legally authenticated state. The unauthenticated state exists when no authentication information has been recorded (i.e., it is the absence of being either authenticated or legally authenticated). While electronic signatures are not captured in a CDA document, both authentication and legal authentication require that a document has been signed manually or electronically by the responsible individual. A legalAuthenticator has a required legalAuthenticator.time indicating the time of authentication, and a required legalAuthenticator.signatureCode, indicating that a signature has been obtained and is on file.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/MaintainedEntity"
        },
        "name" : "MaintainedEntity (CDA Class)",
        "description" : "The MaintainedEntity class is present for backwards compatibility, and its use is discouraged, except where needed to support the transformation of CDA, Release One documents.\n\nNOTE: In CDA, Release One, it was possible to specify those individuals responsible for the device. This functionality has been deprecated in CDA, Release Two.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ManufacturedProduct"
        },
        "name" : "ManufacturedProduct (CDA Class)",
        "description" : "ManufacturedProduct (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Material"
        },
        "name" : "Material (CDA Class)",
        "description" : "A subtype of Entity that is inanimate and locationally independent.\n\nMaterials are entities that are neither Living Subjects nor places. Manufactured or processed products are considered material, even if they originate as living matter. Materials come in a wide variety of physical forms and can pass through different states (ie. Gas, liquid, solid) while still retaining their physical composition and material characteristics.\n\nClarify the meaning of \"locationally independent\"; suggest removing it and supplanting with first Usage Note sentence.\n\nPharmaceutical substances (including active vaccines containing retarded virus), disposable supplies, durable equipment, implantable devices, food items (including meat or plant products), waste, traded goods.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/MO"
        },
        "name" : "MO: MonetaryAmount (V3 Data Type)",
        "description" : "A monetary amount is a quantity expressing the amount of money in some currency. Currencies are the units in which monetary amounts are denominated in different economic regions. While the monetary amount is a single kind of quantity (money) the exchange rates between the different units are variable. This is the principle difference between physical quantity and monetary amounts, and the reason why currency units are not physical units.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/NonXMLBody"
        },
        "name" : "NonXMLBody (CDA Class)",
        "description" : "The NonXMLBody class represents a document body that is in some format other than XML. NonXMLBody.text is used to reference data that is stored externally to the CDA document or to encode the data directly inline.\n\nRendering a referenced non-XML body requires a software tool that recognizes the particular MIME media type of the blob.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Observation"
        },
        "name" : "Observation (CDA Class)",
        "description" : "A derivative of the RIM Observation class, used for representing coded and other observations.\n\nObservation.negationInd, when set to \"true\", is a positive assertion that the Observation as a whole is negated. Some properties such as Observation.id, Observation.moodCode, and the participations are not negated. These properties always have the same meaning: i.e., the author remains the author of the negative Observation. An observation statement with negationInd is still a statement about the specific fact described by the Observation. For instance, a negated \"finding of wheezing on July 1\" means that the author positively denies that there was wheezing on July 1, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ObservationMedia"
        },
        "name" : "ObservationMedia (CDA Class)",
        "description" : "A derivative of the RIM Observation class that represents multimedia that is logically part of the current document. This class is only for multimedia that is logically part of the attested content of the document. Rendering a referenced ObservationMedia requires a software tool that recognizes the particular MIME media type.\n\nAn XML attribute \"ID\" of type XML ID, is added to ObservationMedia within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).\n\nThe distinction between ObservationMedia and ExternalObservation is that ObservationMedia entries are part of the attested content of the document whereas ExternalObservations are not. For instance, when a clinician draws a picture as part of a progress note, that picture is represented as a CDA ObservationMedia. If that clinician is also describing a finding seen on a chest-x-ray, the referenced chest-x-ray is represented as a CDA ExternalObservation.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ObservationRange"
        },
        "name" : "ObservationRange (CDA Class)",
        "description" : "ObservationRange (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ON"
        },
        "name" : "ON: OrganizationName (V3 Data Type)",
        "description" : "A name for an organization. A sequence of name parts. Examples for organization name values are \"Health Level Seven, Inc.\", \"Hospital\", etc. An organization name may be as simple as a character string or may consist of several person name parts, such as, \"Health Level 7\", \"Inc.\". ON differs from EN because certain person related name parts are not possible.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Order"
        },
        "name" : "Order (CDA Class)",
        "description" : "This class represents those orders that are fulfilled by this document. For instance, a provider orders an X-Ray. The X-Ray is performed. A radiologist reads the X-Ray and generates a report. The X-Ray order identifier is transmitted in the Order class, the performed X-Ray procedure is transmitted in the ServiceEvent class, and the ClinicalDocument.code would be valued with \"Diagnostic Imaging Report\".",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Organization"
        },
        "name" : "Organization (CDA Class)",
        "description" : "An Entity representing a formalized group of persons or other organizations with a common purpose and the infrastructure to carry out that purpose.\n\nCompanies and institutions, a government department, an incorporated body that is responsible for administering a facility, an insurance company.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/OrganizationPartOf"
        },
        "name" : "OrganizationPartOf (CDA Class)",
        "description" : "OrganizationPartOf (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Organizer"
        },
        "name" : "Organizer (CDA Class)",
        "description" : "A derivative of the RIM Act class, which can be used to create arbitrary groupings of other CDA entries that share a common context. An Organizer can contain other Organizers and/or other CDA entries, by traversing the component relationship. An Organizer can refer to external acts by traversing the reference relationship. An Organizer cannot be the source of an entryRelationship relationship.\nNOTE: CDA entries such as Observation can also contain other CDA entries by traversing the entryRelationship class. There is no requirement that the Organizer entry be used in order to group CDA entries.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/OrganizerComponent"
        },
        "name" : "OrganizerComponent (CDA Class)",
        "description" : "CDA entries represent the structured computer-processable components within a document section. Each section can contain zero to many entries.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ParentDocument"
        },
        "name" : "ParentDocument (CDA Class)",
        "description" : "The ParentDocument represents the source of a document revision, addenda, or transformation. ParentDocument.text is modeled as an ED data type - allowing for the expression of the MIME type of the parent document. It is not to be used to embed the related document, and thus ParentDocument.text.BIN is precluded from use.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Participant1"
        },
        "name" : "Participant1 (CDA Class)",
        "description" : "Used to represent other participants not explicitly mentioned by other classes, that were somehow involved in the documented acts.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Participant2"
        },
        "name" : "Participant2 (CDA Class)",
        "description" : "Can be used to represent any other participant that cannot be represented with one of the more specific participants. The participant can be ascribed to a CDA entry, and propagates to nested CDA entries, unless overridden.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ParticipantRole"
        },
        "name" : "ParticipantRole (CDA Class)",
        "description" : "ParticipantRole (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Patient"
        },
        "name" : "Patient (CDA Class)",
        "description" : "A LivingSubject as a recipient of health care services from a healthcare provider.\nThe patient is the player; the provider is the scoper.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PatientRole"
        },
        "name" : "PatientRole (CDA Class)",
        "description" : "A recordTarget is represented as a relationship between a person and an organization, where the person is in a patient role (PatientRole class). The entity playing the role is a patient (Patient class). The entity scoping the role is an organization (Organization class). A patient is uniquely identified via the PatientRole.id attribute.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Person"
        },
        "name" : "Person (CDA Class)",
        "description" : "A human being.\n\nThis class can be used to represent either a single individual, a group of individuals or a kind of individual based on the values of Entity.determinerCode and Entity.quantity.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmContent"
        },
        "name" : "PharmContent (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmIngredient"
        },
        "name" : "PharmIngredient (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmMedicineClass"
        },
        "name" : "PharmMedicineClass (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmPackagedMedicine"
        },
        "name" : "PharmPackagedMedicine (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmSpecializedKind"
        },
        "name" : "PharmSpecializedKind (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmSubstance"
        },
        "name" : "PharmSubstance (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PharmSuperContent"
        },
        "name" : "PharmSuperContent (CDA Pharm Class)"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PIVL-TS"
        },
        "name" : "PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)",
        "description" : "An interval of time that recurs periodically. Periodic intervals have two properties, phase and period. The phase specifies the \"interval prototype\" that is repeated every period.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Place"
        },
        "name" : "Place (CDA Class)",
        "description" : "A bounded physical place or site, including any contained structures.\n\nPlace may be natural or man-made. The geographic position of a place may or may not be constant. Places may be work facilities (where relevant acts occur), homes (where people live) or offices (where people work). Places may contain sub-places (floor, room, booth, bed). Places may also be sites that are investigated in the context of health care, social work, public health administration (e.g., buildings, picnic grounds, day care centers, prisons, counties, states, and other focuses of epidemiological events).\n\nExamples: A field, lake, city, county, state, country, lot (land), building, pipeline, power line, playground, ship, truck",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PlayingEntity"
        },
        "name" : "PlayingEntity (CDA Class)",
        "description" : "PlayingEntity (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PN"
        },
        "name" : "PN: PersonName (V3 Data Type)",
        "description" : "A name for a person. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for person name values are \"Jim Bob Walton, Jr.\", \"Adam Everyman\", etc. A person name may be as simple as a character string or may consist of several person name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\". PN differs from EN because the qualifier type cannot include LS (Legal Status).",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PQ"
        },
        "name" : "PQ: PhysicalQuantity (V3 Data Type)",
        "description" : "A dimensioned quantity expressing the result of measuring.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PQR"
        },
        "name" : "PQR: PhysicalQuantityRepresentation (V3 Data Type)",
        "description" : "An extension of the coded value data type representating a physical quantity using a unit from any code system. Used to show alternative representation for a physical quantity.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Precondition"
        },
        "name" : "Precondition (CDA Class)",
        "description" : "The precondition class, derived from the ActRelationship class, is used along with the Precondition class to express a condition that must hold true before some over activity occurs.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Precondition2"
        },
        "name" : "Precondition2 (CDA Class)",
        "description" : "The sdtc:precondition2 extension allows a more flexible set of skip conditions on a set of criteria. Without this extension the skip condition is restricted to all criteria true. The extension allows a choice of the following logical operation extensions sdtc:allTrue, sdtc:allFalse, sdtc:atLeastOneTrue, sdtc:atLeastOneFalse, sdtc:onlyOneFalse, and sdtc:onlyOneTrue to be placed upon the encapsulated criteria or a criterion. The logical operation extensions nest a [0 .. *] sdtc:precondition2 extension allowing for a complex specification of nested skip conditions if needed.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/PreconditionBase"
        },
        "name" : "PreconditionBase (CDA Class)",
        "description" : "An abstract class containing the common fields used by sdtc:allTrue, sdtc:allFalse, sdtc:atLeastOneTrue, sdtc:atLeastOneFalse, sdtc:onlyOneTrue, and sdtc:onlyOneFalse.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Procedure"
        },
        "name" : "Procedure (CDA Class)",
        "description" : "A derivative of the RIM Procedure class, used for representing procedures.\n\nProcedure.negationInd, when set to \"true\", is a positive assertion that the Procedure as a whole is negated. Some properties such as Procedure.id, Procedure.moodCode, and the participations are not affected. These properties always have the same meaning: i.e., the author remains the author of the negative Procedure. A procedure statement with negationInd is still a statement about the specific fact described by the Procedure. For instance, a negated \"appendectomy performed\" means that the author positively denies that there was ever an appendectomy performed, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical:abstract"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/QTY"
        },
        "name" : "QTY: Quantity (V3 Data Type)",
        "description" : "The quantity data type is an abstract generalization for all data types (1) whose value set has an order relation (less-or-equal) and (2) where difference is defined in all of the data type's totally ordered value subsets. The quantity type abstraction is needed in defining certain other types, such as the interval and the probability distribution.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/REAL"
        },
        "name" : "REAL: RealNumber (V3 Data Type)",
        "description" : "Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers. The typical representation is decimal, where the number of significant decimal digits is known as the precision.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RecordTarget"
        },
        "name" : "RecordTarget (CDA Class)",
        "description" : "The recordTarget represents the medical record that this document belongs to. A clinical document typically has exactly one recordTarget participant. In the uncommon case where a clinical document (such as a group encounter note) is placed into more than one patient chart, more than one recordTarget participants can be stated. The recordTarget(s) of a document are stated in the header and propagate to nested content, where they cannot be overridden (see See CDA Context (§ 4.4 )).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Reference"
        },
        "name" : "Reference (CDA Class)",
        "description" : "CDA entries can reference external objects such as external images and prior reports. These external objects are not part of the authenticated document content. They contain sufficient attributes to enable an explicit reference rather than duplicating the entire referenced object. The CDA entry that wraps the external reference can be used to encode the specific portions of the external reference that are addressed in the narrative block.\n\nEach object allows for an identifier and a code, and contains the RIM Act.text attribute, which can be used to store the URL and MIME type of the object. External objects always have a fixed moodCode of \"EVN\".\n\nThe reference class contains the attribute reference.seperatableInd, which indicates whether or not the source is intended to be interpreted independently of the target. The indicator cannot prevent an individual or application from separating the source and target, but indicates the author's desire and willingness to attest to the content of the source if separated from the target. Typically, where seperatableInd is \"false\", the exchanged package should include the target of the reference so that the recipient can render it.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RegionOfInterest"
        },
        "name" : "RegionOfInterest (CDA Class)",
        "description" : "A derivative of the RIM Observation class that represents a region of interest on an image, using an overlay shape. RegionOfInterest is used to make reference to specific regions in images, e.g., to specify the site of a physical finding by \"circling\" a region in a schematic picture of a human body. The units of the coordinate values in RegionOfInterest.value are in pixels, expressed as a list of integers. The origin is in the upper left hand corner, with positive X values going to the right and positive Y values going down. The relationship between a RegionOfInterest and its referenced ObservationMedia or ExternalObservation is specified by traversing the entryRelationship or reference class, respectively, where typeCode equals \"SUBJ\". A RegionOfInterest must reference exactly one ObservationMedia or one ExternalObservation. If the RegionOfInterest is the target of a renderMultimedia reference, then it shall only reference an ObservationMedia and not an ExternalObservation.\n\nAn XML attribute \"ID\" of type XML ID, is added to RegionOfInterest within the CDA Schema. This attribute serves as the target of a renderMultiMedia reference (see renderMultiMedia). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RelatedDocument"
        },
        "name" : "RelatedDocument (CDA Class)",
        "description" : "Represents the participant who has transformed a dictated note into text.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RelatedEntity"
        },
        "name" : "RelatedEntity (CDA Class)",
        "description" : "The RelatedEntity role is used to represent an informant without a role.id (e.g. a parent or guy on the street).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RelatedSubject"
        },
        "name" : "RelatedSubject (CDA Class)",
        "description" : "A subject is a person playing one of several possible roles (RelatedSubject class). The entity playing the role is a person (SubjectPerson class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/RTO-PQ-PQ"
        },
        "name" : "RTO_PQ_PQ: Ratio (V3 Data Type)",
        "description" : "A quantity constructed as the quotient of a numerator quantity divided by a denominator quantity. Common factors in the numerator and denominator are not automatically cancelled out. The data type supports titers (e.g., \"1:128\") and other quantities produced by laboratories that truly represent ratios. Ratios are not simply \"structured numerics\", particularly blood pressure measurements (e.g. \"120/60\") are not ratios. In many cases the should be used instead of the .",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SC"
        },
        "name" : "SC: CharacterStringWithCode (V3 Data Type)",
        "description" : "A character string that optionally may have a code attached. The text must always be present if a code is present. The code is often a local code.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Section"
        },
        "name" : "Section (CDA Class)",
        "description" : "Document sections can nest, can override context propagated from the header (See CDA Context), and can contain narrative and CDA entries.\n\nAn XML attribute \"ID\" of type XML ID, is added to Section within the CDA Schema. This attribute serves as the target of a linkHtml reference (see linkHtml). All values of attributes of type XML ID must be unique within the document (per the W3C XML specification).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ServiceEvent"
        },
        "name" : "ServiceEvent (CDA Class)",
        "description" : "This class represents the main Act, such as a colonoscopy or an appendectomy, being documented.\n\nIn some cases, the ServiceEvent is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"History and Physical Report\" and the procedure being documented is a \"History and Physical\" act. A ServiceEvent can further specialize the act inherent in the ClinicalDocument.code, such as where the ClinicalDocument.code is simply \"Procedure Report\" and the procedure was a \"colonoscopy\". If ServiceEvent is included, it must be equivalent to or further specialize the value inherent in the ClinicalDocument.code, and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.\n\nServiceEvent.effectiveTime can be used to indicate the time the actual event (as opposed to the encounter surrounding the event) took place.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Specimen"
        },
        "name" : "Specimen (CDA Class)",
        "description" : "A specimen is a part of some entity, typically the subject, that is the target of focused laboratory, radiology or other observations. In many clinical observations, such as physical examination of a patient, the patient is the subject of the observation, and there is no specimen. The specimen participant is only used when observations are made against some substance or object that is taken or derived from the subject.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SpecimenRole"
        },
        "name" : "SpecimenRole (CDA Class)",
        "description" : "SpecimenRole (CDA Class)",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ST"
        },
        "name" : "ST: CharacterString (V3 Data Type)",
        "description" : "The character string data type stands for text data, primarily intended for machine processing (e.g., sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ST-dot-r2b"
        },
        "name" : "ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type)",
        "description" : "A character string data type variant that uses a value attribute rather than element content. This datatype is primarily used to handle version numbers and other string values in CDA documents where the data is stored as an XML attribute.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/StructuredBody"
        },
        "name" : "StructuredBody (CDA Class)",
        "description" : "The StructuredBody class represents a CDA document body that is comprised of one or more document sections.\n\nThe StructuredBody class is associated with one or more Section classes through a component relationship.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Subject"
        },
        "name" : "Subject (CDA Class)",
        "description" : "The subject participant represents the primary target of the entries recorded in the document. Most of the time the subject is the same as the recordTarget, but need not be, for instance when the subject is a fetus observed in an obstetrical ultrasound.\n\nThe subject participant can be ascribed to a CDA section or a CDA entry. It propagates to nested components, unless overridden. The subject of a document is presumed to be the patient.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SubjectPerson"
        },
        "name" : "SubjectPerson (CDA Class)",
        "description" : "The entity playing the role is a person (SubjectPerson class).",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SubstanceAdministration"
        },
        "name" : "SubstanceAdministration (CDA Class)",
        "description" : "A derivative of the RIM SubstanceAdministration class, used for representing medication-related events such as medication history or planned medication administration orders.\n\nSubstanceAdministration.negationInd, when set to \"true\", is a positive assertion that the SubstanceAdministration as a whole is negated. Some properties such as SubstanceAdministration.id, SubstanceAdministration.moodCode, and the participations are not affected. These properties always have the same meaning: i.e., the author remains the author of the negative SubstanceAdministration. A substance administration statement with negationInd is still a statement about the specific fact described by the SubstanceAdministration. For instance, a negated \"aspirin administration\" means that the author positively denies that aspirin is being administered, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.\n\nSubstanceAdministration.priorityCode categorizes the priority of a substance administration. SubstanceAdministration.doseQuantity indicates how much medication is given per dose. SubstanceAdministration.rateQuantity can be used to indicate the rate at which the dose is to be administered (e.g., the flow rate for intravenous infusions). SubstanceAdministration.maxDoseQuantity is used to capture the maximum dose of the medication that can be given over a stated time interval (e.g., maximum daily dose of morphine, maximum lifetime dose of doxorubicin). SubstanceAdministration.effectiveTime is used to describe the timing of administration. It is modeled using the GTS data type to accommodate various dosing scenarios.\n\nThe capture of medication-related information also involves the interrelationship of SubstanceAdministration with several other classes. The consumable participation is used to bring in the LabeledDrug or Material entity that describes the administered substance. The LabeledDrug class, which is an Entity class playing the Role of Manufactured Product, identifies the drug that is consumed in the substance administration. The medication is identified by means of the LabeledDrug.code or the LabeledDrug.name. The Material entity is used to identify non-drug administered substances such as vaccines and blood products.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/Supply"
        },
        "name" : "Supply (CDA Class)",
        "description" : "A derivative of the RIM Supply class, used for representing the provision of a material by one entity to another.\n\nThe dispensed product is associated with the Supply act via a product participant, which connects to the same ManufacturedProduct role used for SubstanceAdministration.\n\nThe Supply class represents dispensing, whereas the SubstanceAdministration class represents administration. Prescriptions are complex activities that involve both an administration request to the patient (e.g. take digoxin 0.125mg by mouth once per day) and a supply request to the pharmacy (e.g. dispense 30 tablets, with 5 refills). This should be represented in CDA by a SubstanceAdministration entry that has a component Supply entry. The nested Supply entry can have Supply.independentInd set to \"false\" to signal that the Supply cannot stand alone, without it's containing SubstanceAdministration. The following example illustrates a prescription representation in CDA.",
        "groupingId" : "classes"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical:abstract"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SXCM-TS"
        },
        "name" : "SXCM_TS: GeneralTimingSpecification (V3 Data Type)",
        "description" : "A set of points in time, specifying the timing of events and actions and the cyclical validity-patterns that may exist for certain kinds of information, such as phone numbers (evening, daytime), addresses (so called \"snowbirds,\" residing closer to the equator during winter and farther from the equator during summer) and office hours.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/SXPR-TS"
        },
        "name" : "SXPR_TS: Component part of GTS (V3 Data Type)",
        "description" : "A set-component that is itself made up of set-components that are evaluated as one value",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/TEL"
        },
        "name" : "TEL: TelecommunicationAddress (V3 Data Type)",
        "description" : "A telephone number (voice or fax), e-mail address, or other locator for a resource mediated by telecommunication equipment. The address is specified as a Universal Resource Locator (URL) qualified by time specification and use codes that help in deciding which address to use for a given time and purpose.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/TN"
        },
        "name" : "TN: TrivialName (V3 Data Type)",
        "description" : "A restriction of entity name that is effectively a simple string used for a simple name for things and places.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:logical"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/TS"
        },
        "name" : "TS: PointInTime (V3 Data Type)",
        "description" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
        "groupingId" : "datatypes-complex"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/bin"
        },
        "name" : "bin: Binary Data",
        "description" : "Binary data is a raw block of bits. Binary data is a protected type that MUST not be used outside the data type specification.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/bl-simple"
        },
        "name" : "bl: Boolean",
        "description" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/bn"
        },
        "name" : "bn: BooleanNonNull",
        "description" : "The BooleanNonNull type is used where a Boolean cannot have a null value. A Boolean value can be either true or false.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/cs-simple"
        },
        "name" : "cs: Coded Simple Value",
        "description" : "Coded data in its simplest form, consists of a code. The code system and code system version is fixed by the context in which the value occurs. 'cs' is used for coded attributes that have a single HL7-defined value set.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/int-simple"
        },
        "name" : "int: Integer Number",
        "description" : "Integer numbers (-1,0,1,2, 100, 3398129, etc.) are precise numbers that are results of counting and enumerating. Integer numbers are discrete, the set of integers is infinite but countable.  No arbitrary limit is imposed on the range of integer numbers. Two NULL flavors are defined for the positive and negative infinity.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/oid"
        },
        "name" : "oid: ISO Object Identifier",
        "description" : "A globally unique string representing an ISO Object Identifier (OID) in a form that consists only of numbers and dots (e.g., '2.16.840.1.113883.3.1').",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/probability"
        },
        "name" : "probability: Probability",
        "description" : "The probability assigned to the value, a decimal number between 0 (very uncertain) and 1 (certain).",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/real-simple"
        },
        "name" : "real: Real Number",
        "description" : "Fractional numbers. Typically used whenever quantities are measured, estimated, or computed from other real numbers.  The typical representation is decimal, where the number of significant decimal digits is known as the precision. Real numbers are needed beyond integers whenever quantities of the real world are measured, estimated, or computed from other real numbers. The term \"Real number\" in this specification is used to mean that fractional values are covered without necessarily implying the full set of the mathematical real numbers.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ruid"
        },
        "name" : "ruid: HL7 Reserved Identifier Scheme",
        "description" : "HL7 Reserved Identifier Scheme (RUID)\\nA globally unique string defined exclusively by HL7. Identifiers in this scheme are only defined by balloted HL7 specifications. Local communities or systems must never use such reserved identifiers based on bilateral negotiations.\n\nHL7 reserved identifiers are strings that consist only of (US-ASCII) letters, digits and hyphens, where the first character must be a letter. HL7 may assign these reserved identifiers as mnemonic identifiers for major concepts of interest to HL7.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/st-simple"
        },
        "name" : "st: Character String",
        "description" : "The character string data type stands for text data, primarily intended for machine processing (e.g. sorting, querying, indexing, etc.) Used for names, symbols, and formal expressions.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/ts-simple"
        },
        "name" : "ts: Point in Time",
        "description" : "A quantity specifying a point on the axis of natural time. A point in time is most often represented as a calendar expression.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/uid"
        },
        "name" : "uid: Unique Identifier String",
        "description" : "A unique identifier string is a character string which identifies an object in a globally unique and timeless manner. The allowable formats and values and procedures of this data type are strictly controlled by HL7. At this time, user-assigned identifiers may be certain character representations of ISO Object Identifiers (OID) and DCE Universally Unique Identifiers (UUID). HL7 also reserves the right to assign other forms of UIDs (RUID), such as mnemonic identifiers for code systems.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/url"
        },
        "name" : "url: Universal Resource Locator",
        "description" : "A telecommunications address specified according to Internet standard RFC 1738 [http://www.ietf.org/rfc/rfc1738.txt]. The URL specifies the protocol and the contact point defined by that protocol for the resource.  Notable uses of the telecommunication address data type are for telephone and telefax numbers, e-mail addresses, Hypertext references, FTP references, etc.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/uuid"
        },
        "name" : "uuid: DCE Universal Unique Identifier",
        "description" : "A globally unique string representing a DCE Universal Unique Identifier (UUID) in the common UUID format that consists of 5 hyphen-separated groups of hexadecimal digits having 8, 4, 4, 4, and 12 places respectively.\n\n***NOTE:*** The output of UUID related programs and functions may use all sorts of forms, upper case, lower case, and with or without the hyphens that group the digits. This variate output must be postprocessed to conform to the HL7 specification, i.e., the hyphens must be inserted for the 8-4-4-4-12 grouping. Historically, CDA also required that all hexadecimal digits must be converted to upper case, but due to real-world issues encountered when enforcing this rule, it has been relaxed to allow for upper or lower case letters. Additionally, FHIR requires that UUID's be communicated using only lower case letters, so for broader compatibility, implementers are encouraged to use lower case letters.",
        "groupingId" : "datatypes-simple"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "StructureDefinition:primitive-type"
          }
        ],
        "reference" : {
          "reference" : "StructureDefinition/xs-ID"
        },
        "name" : "xs:ID",
        "description" : "ID represents the ID attribute type from [XML 1.0 (Second Edition)]. The \"value space\" of ID is the set of all strings that \"match\" the NCName production in [Namespaces in XML]. The \"lexical space\" of ID is the set of all strings that \"match\" the NCName production in [Namespaces in XML].",
        "groupingId" : "datatypes-xml"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "CodeSystem"
          }
        ],
        "reference" : {
          "reference" : "CodeSystem/IGParametersCDAValidation"
        },
        "name" : "IG Parameters CDA Validation",
        "description" : "Code system for CDA validation parameters in the IG."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "CodeSystem"
          }
        ],
        "reference" : {
          "reference" : "CodeSystem/BinaryDataEncoding"
        },
        "name" : "Binary Data Encoding Code System",
        "description" : "Identifies the representation of binary data in a text field"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/BinaryDataEncoding"
        },
        "name" : "CDABinaryDataEncoding",
        "description" : "Identifies the representation of binary data in a text field"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDACompressionAlgorithm"
        },
        "name" : "CDACompressionAlgorithm",
        "description" : "Type of compression algorithm used - limited to 4 concepts from original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAEntityNameUse"
        },
        "name" : "CDAEntityNameUse",
        "description" : "A set of codes advising a system or user which name in a set of names to select for a given purpose - limited to values allowed in original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAInformationRecipientRole"
        },
        "name" : "CDAInformationRecipientRole",
        "description" : "Used to represent the role(s) of those who should receive a copy of a document - limited to values allowed in original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAPostalAddressUse"
        },
        "name" : "CDAPostalAddressUse",
        "description" : "A set of codes advising a system or user which address in a set of like addresses to select for a given purpose - limited to values allowed in original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDASignatureCode"
        },
        "name" : "CDASignatureCode",
        "description" : "A set of codes specifying whether and how the participant has attested his participation through a signature - limited to values allowed in original CDA definition.\n\n**Note:** CDA Release One represented either an intended (`X`) or actual (`S`) authenticator. CDA Release Two only represents an actual authenticator, so has deprecated the value of `X`."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDANullFlavor"
        },
        "name" : "CDANullFlavor",
        "description" : "CDA NullFlavors - limited to values allowed in original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActClass"
        },
        "name" : "CDAActClass",
        "description" : "A code specifying the major type of Act that this Act-instance represents."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActClassObservation"
        },
        "name" : "CDAActClassObservation",
        "description" : "An act that is intended to result in new information about a subject. The main difference between Observations and other Acts is that Observations have a value attribute."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActMood"
        },
        "name" : "CDAActMood",
        "description" : "A code distinguishing whether an Act is conceived of as a factual statement or in some other manner as a command, possibility, goal, etc."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActMoodIntent"
        },
        "name" : "CDAActMoodIntent",
        "description" : "An intention or plan to perform a service."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActRelationshipType"
        },
        "name" : "CDAActRelationshipType",
        "description" : "A code specifying the meaning and purpose of every ActRelationship instance. Each of its values implies specific constraints to what kinds of Act objects can be related and in which way."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAContextControl"
        },
        "name" : "CDAContextControl",
        "description" : "A code that specifies how an ActRelationship or Participation contributes to the context of an Act, and whether it may be propagated to descendent Acts whose association allows such propagation."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAEntityNamePartQualifier"
        },
        "name" : "CDAEntityNamePartQualifier",
        "description" : "Qualifies parts of names"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAParticipationType"
        },
        "name" : "CDAParticipationType",
        "description" : "A code specifying the meaning and purpose of every Participation instance. Each of its values implies specific constraints on the Roles undertaking the participation. Limited to values allowed in original CDA definition"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDARoleClass"
        },
        "name" : "CDARoleClass",
        "description" : "Represent a Role which is an association or relationship between two entities - the entity that plays the role and the entity that scopes the role. Roles names are derived from the name of the playing entity in that role."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDARoleClassAssociative"
        },
        "name" : "CDARoleClassAssociative",
        "description" : "A general association between two entities that is neither partitive nor ontological."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDARoleClassMutualRelationship"
        },
        "name" : "CDARoleClassMutualRelationship",
        "description" : "A relationship that is based on mutual behavior of the two Entities as being related. The basis of such relationship may be agreements (e.g., spouses, contract parties) or they may be de facto behavior (e.g. friends) or may be an incidental involvement with each other (e.g. parties over a dispute, siblings, children)."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDARoleClassRoot"
        },
        "name" : "CDARoleClassRoot",
        "description" : "Corresponds to the Role class"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDASetOperator"
        },
        "name" : "CDASetOperator",
        "description" : "Determins the intersectionality of multiple sets"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAActSubstanceAdministrationCode"
        },
        "name" : "CDAActSubstanceAdministrationCode",
        "description" : "Describes the type of substance administration being performed."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAEntityCode"
        },
        "name" : "CDAEntityCode",
        "description" : "A value representing the specific kind of Entity the instance represents."
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAMaterialEntityClassType"
        },
        "name" : "CDAMaterialEntityClassType",
        "description" : "Types of Material for EntityClass “MAT”"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDAObservationInterpretation"
        },
        "name" : "CDAObservationInterpretation",
        "description" : "One or more codes providing a rough qualitative interpretation of the observation - limited to values available in original CDA"
      },
      {
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/resource-information",
            "valueString" : "ValueSet"
          }
        ],
        "reference" : {
          "reference" : "ValueSet/CDARoleCode"
        },
        "name" : "CDARoleCode",
        "description" : "A set of codes further specifying the kind of Role; specific classification codes for further qualifying RoleClass codes."
      }
    ],
    "page" : {
      "sourceUrl" : "index.html",
      "name" : "index.html",
      "title" : "IG Home Page",
      "generation" : "markdown",
      "page" : [
        {
          "sourceUrl" : "overview.html",
          "name" : "overview.html",
          "title" : "Overview",
          "generation" : "markdown",
          "page" : [
            {
              "sourceUrl" : "implementation.html",
              "name" : "implementation.html",
              "title" : "Implementation Notes",
              "generation" : "markdown"
            },
            {
              "sourceUrl" : "cda-rmim.html",
              "name" : "cda-rmim.html",
              "title" : "Graphical Map of CDA",
              "generation" : "html"
            },
            {
              "sourceUrl" : "dt-uml.html",
              "name" : "dt-uml.html",
              "title" : "Graphical Map of Datatypes",
              "generation" : "html"
            },
            {
              "sourceUrl" : "narrative.html",
              "name" : "narrative.html",
              "title" : "Narrative Block",
              "generation" : "markdown"
            }
          ]
        },
        {
          "sourceUrl" : "terminology.html",
          "name" : "terminology.html",
          "title" : "Terminology",
          "generation" : "markdown"
        },
        {
          "sourceUrl" : "downloads.html",
          "name" : "downloads.html",
          "title" : "Downloads",
          "generation" : "markdown"
        }
      ]
    },
    "parameter" : [
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "copyrightyear"
        },
        "value" : "2019+"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-version"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "excludejson"
        },
        "value" : "false"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "jira-code"
        },
        "value" : "cda-sd"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "releaselabel"
        },
        "value" : "release"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "html-exempt"
        },
        "value" : "transformed-example.html"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "autoload-resources"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/capabilities"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/examples"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/extensions"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/models"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/operations"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/profiles"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/resources"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/vocabulary"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/maps"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/testing"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "input/history"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-resource"
        },
        "value" : "fsh-generated/resources"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-pages"
        },
        "value" : "template/config"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-pages"
        },
        "value" : "input/images"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-liquid"
        },
        "value" : "template/liquid"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-liquid"
        },
        "value" : "input/liquid"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-qa"
        },
        "value" : "temp/qa"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-temp"
        },
        "value" : "temp/pages"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-output"
        },
        "value" : "output"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/guide-parameter-code",
          "code" : "path-tx-cache"
        },
        "value" : "input-cache/txcache"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-suppressed-warnings"
        },
        "value" : "input/ignoreWarnings.txt"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "path-history"
        },
        "value" : "http://hl7.org/cda/stds/core/history.html"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "template-html"
        },
        "value" : "template-page.html"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "template-md"
        },
        "value" : "template-page-md.html"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-contact"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-context"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-copyright"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-jurisdiction"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-license"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-publisher"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "apply-wg"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "active-tables"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "fmm-definition"
        },
        "value" : "http://hl7.org/fhir/versions.html#maturity"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "propagate-status"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "excludelogbinaryformat"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "tabbed-snapshots"
        },
        "value" : "true"
      },
      {
        "code" : {
          "system" : "http://hl7.org/fhir/tools/CodeSystem/ig-parameters",
          "code" : "excludettl"
        },
        "value" : "true"
      }
    ]
  }
}

```
