# EN: EntityName (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **EN: EntityName (V3 Data Type)**

## Logical Model: EN: EntityName (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/EN | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:EN |

 
A name for a person, organization, place or thing. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for entity name values are "Jim Bob Walton, Jr.", "Health Level Seven, Inc.", "Lake Tahoe", etc. An entity name may be as simple as a character string or may consist of several entity name parts, such as, "Jim", "Bob", "Walton", and "Jr.", "Health Level Seven" and "Inc.", "Lake" and "Tahoe". 

**Usages:**

* Derived from this Logical Model: [ON: OrganizationName (V3 Data Type)](StructureDefinition-ON.md), [PN: PersonName (V3 Data Type)](StructureDefinition-PN.md) and [TN: TrivialName (V3 Data Type)](StructureDefinition-TN.md)
* Use this Logical Model: [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [LabeledDrug (CDA Class)](StructureDefinition-LabeledDrug.md), [Material (CDA Class)](StructureDefinition-Material.md)...Show 6 more,[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[PharmMedicineClass (CDA Pharm Class)](StructureDefinition-PharmMedicineClass.md),[PharmPackagedMedicine (CDA Pharm Class)](StructureDefinition-PharmPackagedMedicine.md),[PharmSubstance (CDA Pharm Class)](StructureDefinition-PharmSubstance.md)and[Place (CDA Class)](StructureDefinition-Place.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/EN)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-EN.csv), [Excel](StructureDefinition-EN.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "EN",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/EN",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "EN",
  "title" : "EN: EntityName (V3 Data Type)",
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
  "description" : "A name for a person, organization, place or thing. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for entity name values are \"Jim Bob Walton, Jr.\", \"Health Level Seven, Inc.\", \"Lake Tahoe\", etc. An entity name may be as simple as a character string or may consist of several entity name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\", \"Health Level Seven\" and \"Inc.\", \"Lake\" and \"Tahoe\".",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/EN",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "EN",
        "path" : "EN",
        "definition" : "A name for a person, organization, place or thing. A sequence of name parts, such as given name or family name, prefix, suffix, etc. Examples for entity name values are \"Jim Bob Walton, Jr.\", \"Health Level Seven, Inc.\", \"Lake Tahoe\", etc. An entity name may be as simple as a character string or may consist of several entity name parts, such as, \"Jim\", \"Bob\", \"Walton\", and \"Jr.\", \"Health Level Seven\" and \"Inc.\", \"Lake\" and \"Tahoe\".",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "EN.use",
        "path" : "EN.use",
        "representation" : ["xmlAttr"],
        "label" : "Use Code",
        "definition" : "A set of codes advising a system or user which name in a set of like names to select for a given purpose. A name without specific use code might be a default name useful for any purpose, but a name with a specific use code would be preferred for that respective purpose",
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
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAEntityNameUse"
        }
      },
      {
        "id" : "EN.item",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-choice-group",
            "valueBoolean" : true
          }
        ],
        "path" : "EN.item",
        "definition" : "A series of items that constitute the name.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/fhir/StructureDefinition/Base"
          }
        ],
        "constraint" : [
          {
            "key" : "EN-1",
            "severity" : "error",
            "human" : "Can only have only one of the possible item elements in each choice",
            "expression" : "(delimiter | family | given | prefix | suffix | xmlText).count() = 1"
          }
        ]
      },
      {
        "id" : "EN.item.delimiter",
        "path" : "EN.item.delimiter",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ENXP"
          }
        ]
      },
      {
        "id" : "EN.item.family",
        "path" : "EN.item.family",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ENXP"
          }
        ]
      },
      {
        "id" : "EN.item.given",
        "path" : "EN.item.given",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ENXP"
          }
        ]
      },
      {
        "id" : "EN.item.prefix",
        "path" : "EN.item.prefix",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ENXP"
          }
        ]
      },
      {
        "id" : "EN.item.suffix",
        "path" : "EN.item.suffix",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ENXP"
          }
        ]
      },
      {
        "id" : "EN.item.xmlText",
        "path" : "EN.item.xmlText",
        "representation" : ["xmlText"],
        "short" : "Allows for mixed text content",
        "comment" : "This element is represented in XML as textual content. The actual name \"xmlText\" will not appear in a CDA instance.",
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
        "id" : "EN.validTime",
        "path" : "EN.validTime",
        "label" : "Valid Time",
        "definition" : "An interval of time specifying the time during which the name is or was used for the entity. This accomodates the fact that people change names for people, places and things.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      }
    ]
  }
}

```
