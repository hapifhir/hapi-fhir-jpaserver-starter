# CE: CodedWithEquivalents (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CE: CodedWithEquivalents (V3 Data Type)**

## Logical Model: CE: CodedWithEquivalents (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CE | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CE |

 
Coded data that consists of a coded value (CV) and, optionally, coded value(s) from other coding systems that identify the same concept. Used when alternative codes may exist. 

**Usages:**

* Derived from this Logical Model: [CV: CodedValue (V3 Data Type)](StructureDefinition-CV.md)
* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md), [AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md)...Show 43 more,[Author (CDA Class)](StructureDefinition-Author.md),[AuthoringDevice (CDA Class)](StructureDefinition-AuthoringDevice.md),[ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md),[Consent (CDA Class)](StructureDefinition-Consent.md),[Criterion (CDA Class)](StructureDefinition-Criterion.md),[Device (CDA Class)](StructureDefinition-Device.md),[EIVL_TS: EventRelatedPeriodicInterval (V3 Data Type)](StructureDefinition-EIVL-TS.md),[EncompassingEncounter (CDA Class)](StructureDefinition-EncompassingEncounter.md),[Encounter (CDA Class)](StructureDefinition-Encounter.md),[Entity (CDA Class)](StructureDefinition-Entity.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[HealthCareFacility (CDA Class)](StructureDefinition-HealthCareFacility.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[LabeledDrug (CDA Class)](StructureDefinition-LabeledDrug.md),[LanguageCommunication (CDA Class)](StructureDefinition-LanguageCommunication.md),[Material (CDA Class)](StructureDefinition-Material.md),[NonXMLBody (CDA Class)](StructureDefinition-NonXMLBody.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Order (CDA Class)](StructureDefinition-Order.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[Participant1 (CDA Class)](StructureDefinition-Participant1.md),[Participant2 (CDA Class)](StructureDefinition-Participant2.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[Patient (CDA Class)](StructureDefinition-Patient.md),[Performer1 (CDA Class)](StructureDefinition-Performer1.md),[Performer2 (CDA Class)](StructureDefinition-Performer2.md),[Person (CDA Class)](StructureDefinition-Person.md),[PharmMedicineClass (CDA Pharm Class)](StructureDefinition-PharmMedicineClass.md),[PharmPackagedMedicine (CDA Pharm Class)](StructureDefinition-PharmPackagedMedicine.md),[PharmSubstance (CDA Pharm Class)](StructureDefinition-PharmSubstance.md),[PlayingEntity (CDA Class)](StructureDefinition-PlayingEntity.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md),[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md),[Section (CDA Class)](StructureDefinition-Section.md),[ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md),[Subject (CDA Class)](StructureDefinition-Subject.md),[SubjectPerson (CDA Class)](StructureDefinition-SubjectPerson.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CE)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CE.csv), [Excel](StructureDefinition-CE.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CE",
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
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/elementdefinition-binding-style",
      "valueCode" : "CDA"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CE",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CE",
  "title" : "CE: CodedWithEquivalents (V3 Data Type)",
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
  "description" : "Coded data that consists of a coded value (CV) and, optionally, coded value(s) from other coding systems that identify the same concept. Used when alternative codes may exist.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CE",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/CD",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CE",
        "path" : "CE",
        "definition" : "Coded data, consists of a coded value (CV) and, optionally, coded value(s) from other coding systems that identify the same concept. Used when alternative codes may exist.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "CE.qualifier",
        "path" : "CE.qualifier",
        "label" : "Qualifier",
        "definition" : "Specifies additional codes that increase the specificity of the the primary code.",
        "min" : 0,
        "max" : "0",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CR"
          }
        ]
      }
    ]
  }
}

```
