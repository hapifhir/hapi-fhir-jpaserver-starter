# Supply (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Supply (CDA Class)**

## Logical Model: Supply (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/Supply | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:Supply |

 
A derivative of the RIM Supply class, used for representing the provision of a material by one entity to another. 
The dispensed product is associated with the Supply act via a product participant, which connects to the same ManufacturedProduct role used for SubstanceAdministration. 
The Supply class represents dispensing, whereas the SubstanceAdministration class represents administration. Prescriptions are complex activities that involve both an administration request to the patient (e.g. take digoxin 0.125mg by mouth once per day) and a supply request to the pharmacy (e.g. dispense 30 tablets, with 5 refills). This should be represented in CDA by a SubstanceAdministration entry that has a component Supply entry. The nested Supply entry can have Supply.independentInd set to "false" to signal that the Supply cannot stand alone, without it's containing SubstanceAdministration. The following example illustrates a prescription representation in CDA. 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/Supply)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-Supply.csv), [Excel](StructureDefinition-Supply.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "Supply",
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
      "valueString" : "supply"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/type-profile-style",
      "valueCode" : "cda"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/Supply",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "Supply",
  "title" : "Supply (CDA Class)",
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
  "description" : "A derivative of the RIM Supply class, used for representing the provision of a material by one entity to another.\n\nThe dispensed product is associated with the Supply act via a product participant, which connects to the same ManufacturedProduct role used for SubstanceAdministration.\n\nThe Supply class represents dispensing, whereas the SubstanceAdministration class represents administration. Prescriptions are complex activities that involve both an administration request to the patient (e.g. take digoxin 0.125mg by mouth once per day) and a supply request to the pharmacy (e.g. dispense 30 tablets, with 5 refills). This should be represented in CDA by a SubstanceAdministration entry that has a component Supply entry. The nested Supply entry can have Supply.independentInd set to \"false\" to signal that the Supply cannot stand alone, without it's containing SubstanceAdministration. The following example illustrates a prescription representation in CDA.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/Supply",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "Supply",
        "path" : "Supply",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "Supply.classCode",
        "path" : "Supply.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "defaultValueCode" : "SPLY",
        "fixedCode" : "SPLY",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActClassSupply"
        }
      },
      {
        "id" : "Supply.moodCode",
        "path" : "Supply.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-xDocumentSubstanceMood|2.0.0"
        }
      },
      {
        "id" : "Supply.id",
        "path" : "Supply.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "Supply.code",
        "path" : "Supply.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActCode"
        }
      },
      {
        "id" : "Supply.text",
        "path" : "Supply.text",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "Supply.statusCode",
        "path" : "Supply.statusCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CS"
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActStatus"
        }
      },
      {
        "id" : "Supply.effectiveTime",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype",
            "valueCanonical" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS"
          }
        ],
        "path" : "Supply.effectiveTime",
        "representation" : ["typeAttr"],
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS"
          },
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
        "id" : "Supply.priorityCode",
        "path" : "Supply.priorityCode",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActPriority"
        }
      },
      {
        "id" : "Supply.repeatNumber",
        "path" : "Supply.repeatNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT"
          }
        ]
      },
      {
        "id" : "Supply.independentInd",
        "path" : "Supply.independentInd",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/BL"
          }
        ]
      },
      {
        "id" : "Supply.quantity",
        "path" : "Supply.quantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/PQ"
          }
        ]
      },
      {
        "id" : "Supply.expectedUseTime",
        "path" : "Supply.expectedUseTime",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "Supply.product",
        "path" : "Supply.product",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "Supply.product.typeCode",
        "path" : "Supply.product.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "PRD",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ParticipationTargetDirect"
        }
      },
      {
        "id" : "Supply.product.manufacturedProduct",
        "path" : "Supply.product.manufacturedProduct",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ManufacturedProduct"
          }
        ]
      },
      {
        "id" : "Supply.subject",
        "path" : "Supply.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "Supply.specimen",
        "path" : "Supply.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "Supply.performer",
        "path" : "Supply.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "Supply.author",
        "path" : "Supply.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "Supply.informant",
        "path" : "Supply.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "Supply.participant",
        "path" : "Supply.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "Supply.entryRelationship",
        "path" : "Supply.entryRelationship",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship"
          }
        ]
      },
      {
        "id" : "Supply.reference",
        "path" : "Supply.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "Supply.precondition",
        "path" : "Supply.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "Supply.sdtcInFulfillmentOf1",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "inFulfillmentOf1"
          }
        ],
        "path" : "Supply.sdtcInFulfillmentOf1",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InFulfillmentOf1"
          }
        ]
      }
    ]
  }
}

```
