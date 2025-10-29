# bl: Boolean - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **bl: Boolean**

## Data Type Profile: bl: Boolean 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/bl-simple | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:bl |

 
The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false. 

**Usages:**

* Use this Primitive Type Profile: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [Act (CDA Class)](StructureDefinition-Act.md), [BL: Boolean (V3 Data Type)](StructureDefinition-BL.md), [Component (CDA Class)](StructureDefinition-Component.md)...Show 15 more,[Entry (CDA Class)](StructureDefinition-Entry.md),[EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md),[II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md),[IVXB_INT: Interval Boundary IntegerNumber (V3 Data Type)](StructureDefinition-IVXB-INT.md),[IVXB_PQ: Interval Boundary PhysicalQuantity (V3 Data Type)](StructureDefinition-IVXB-PQ.md),[IVXB_TS: Interval Boundary PointInTime (V3 Data Type)](StructureDefinition-IVXB-TS.md),[InFulfillmentOf1 (CDA Class)](StructureDefinition-InFulfillmentOf1.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md),[PIVL_TS: PeriodicIntervalOfTime (V3 Data Type)](StructureDefinition-PIVL-TS.md),[Precondition2 (CDA Class)](StructureDefinition-Precondition2.md),[Procedure (CDA Class)](StructureDefinition-Procedure.md),[Section (CDA Class)](StructureDefinition-Section.md),[StructuredBody (CDA Class)](StructureDefinition-StructuredBody.md)and[SubstanceAdministration (CDA Class)](StructureDefinition-SubstanceAdministration.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/bl-simple)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-bl-simple.csv), [Excel](StructureDefinition-bl-simple.xlsx), [Schematron](StructureDefinition-bl-simple.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "bl-simple",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/bl-simple",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "bl",
  "title" : "bl: Boolean",
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
  "description" : "The Boolean type stands for the values of two-valued logic. A Boolean value can be either true or false.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "boolean",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/boolean",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "boolean.id",
        "path" : "boolean.id",
        "max" : "0"
      },
      {
        "id" : "boolean.extension",
        "path" : "boolean.extension",
        "max" : "0"
      }
    ]
  }
}

```
