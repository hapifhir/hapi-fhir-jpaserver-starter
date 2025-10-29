# CS: CodedSimpleValue (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CS: CodedSimpleValue (V3 Data Type)**

## Logical Model: CS: CodedSimpleValue (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CS | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CS |

 
Coded data in its simplest form, where only the code is not predetermined. The code system and code system version are fixed by the context in which the CS value occurs. CS is used for coded attributes that have a single HL7-defined value set. 

**Usages:**

* Use this Logical Model: [Act (CDA Class)](StructureDefinition-Act.md), [AlternateIdentification (CDA Class)](StructureDefinition-AlternateIdentification.md), [Authenticator (CDA Class)](StructureDefinition-Authenticator.md), [ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md)...Show 20 more,[Consent (CDA Class)](StructureDefinition-Consent.md),[Encounter (CDA Class)](StructureDefinition-Encounter.md),[InfrastructureRoot (Base Type for all CDA Classes)](StructureDefinition-InfrastructureRoot.md),[LabPrecondition (CDA Class)](StructureDefinition-LabPrecondition.md),[LanguageCommunication (CDA Class)](StructureDefinition-LanguageCommunication.md),[LegalAuthenticator (CDA Class)](StructureDefinition-LegalAuthenticator.md),[NonXMLBody (CDA Class)](StructureDefinition-NonXMLBody.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationMedia (CDA Class)](StructureDefinition-ObservationMedia.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[OrganizationPartOf (CDA Class)](StructureDefinition-OrganizationPartOf.md),[Organizer (CDA Class)](StructureDefinition-Organizer.md),[Precondition2 (CDA Class)](StructureDefinition-Precondition2.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[RegionOfInterest (CDA Class)](StructureDefinition-RegionOfInterest.md),[Section (CDA Class)](StructureDefinition-Section.md),[ServiceEvent (CDA Class)](StructureDefinition-ServiceEvent.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md),[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)and[Supply (CDA Class)](StructureDefinition-Supply.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CS)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CS.csv), [Excel](StructureDefinition-CS.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CS",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CS",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CS",
  "title" : "CS: CodedSimpleValue (V3 Data Type)",
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
  "description" : "Coded data in its simplest form, where only the code is not predetermined. The code system and code system version are fixed by the context in which the CS value occurs. CS is used for coded attributes that have a single HL7-defined value set.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CS",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/CV",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CS",
        "path" : "CS",
        "definition" : "Coded data in its simplest form, where only the code is not predetermined. Used when a single code value must be sent.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "CS.codeSystem",
        "path" : "CS.codeSystem",
        "representation" : ["xmlAttr"],
        "label" : "Code System",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "CS.codeSystemName",
        "path" : "CS.codeSystemName",
        "representation" : ["xmlAttr"],
        "label" : "Code System Name",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "CS.codeSystemVersion",
        "path" : "CS.codeSystemVersion",
        "representation" : ["xmlAttr"],
        "label" : "Code System Version",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "CS.displayName",
        "path" : "CS.displayName",
        "representation" : ["xmlAttr"],
        "label" : "Display Name",
        "min" : 0,
        "max" : "0"
      },
      {
        "id" : "CS.originalText",
        "path" : "CS.originalText",
        "label" : "Original Text",
        "min" : 0,
        "max" : "0"
      }
    ]
  }
}

```
