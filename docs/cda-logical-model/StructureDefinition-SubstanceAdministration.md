# SubstanceAdministration (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **SubstanceAdministration (CDA Class)**

## Logical Model: SubstanceAdministration (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:SubstanceAdministration |

 
A derivative of the RIM SubstanceAdministration class, used for representing medication-related events such as medication history or planned medication administration orders. 
SubstanceAdministration.negationInd, when set to "true", is a positive assertion that the SubstanceAdministration as a whole is negated. Some properties such as SubstanceAdministration.id, SubstanceAdministration.moodCode, and the participations are not affected. These properties always have the same meaning: i.e., the author remains the author of the negative SubstanceAdministration. A substance administration statement with negationInd is still a statement about the specific fact described by the SubstanceAdministration. For instance, a negated "aspirin administration" means that the author positively denies that aspirin is being administered, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation. 
SubstanceAdministration.priorityCode categorizes the priority of a substance administration. SubstanceAdministration.doseQuantity indicates how much medication is given per dose. SubstanceAdministration.rateQuantity can be used to indicate the rate at which the dose is to be administered (e.g., the flow rate for intravenous infusions). SubstanceAdministration.maxDoseQuantity is used to capture the maximum dose of the medication that can be given over a stated time interval (e.g., maximum daily dose of morphine, maximum lifetime dose of doxorubicin). SubstanceAdministration.effectiveTime is used to describe the timing of administration. It is modeled using the GTS data type to accommodate various dosing scenarios. 
The capture of medication-related information also involves the interrelationship of SubstanceAdministration with several other classes. The consumable participation is used to bring in the LabeledDrug or Material entity that describes the administered substance. The LabeledDrug class, which is an Entity class playing the Role of Manufactured Product, identifies the drug that is consumed in the substance administration. The medication is identified by means of the LabeledDrug.code or the LabeledDrug.name. The Material entity is used to identify non-drug administered substances such as vaccines and blood products. 

**Usages:**

* Use this Logical Model: [Entry (CDA Class)](StructureDefinition-Entry.md), [EntryRelationship (CDA Class)](StructureDefinition-EntryRelationship.md) and [OrganizerComponent (CDA Class)](StructureDefinition-OrganizerComponent.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/SubstanceAdministration)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-SubstanceAdministration.csv), [Excel](StructureDefinition-SubstanceAdministration.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "SubstanceAdministration",
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
      "valueString" : "substanceAdministration"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "SubstanceAdministration",
  "title" : "SubstanceAdministration (CDA Class)",
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
  "description" : "A derivative of the RIM SubstanceAdministration class, used for representing medication-related events such as medication history or planned medication administration orders.\n\nSubstanceAdministration.negationInd, when set to \"true\", is a positive assertion that the SubstanceAdministration as a whole is negated. Some properties such as SubstanceAdministration.id, SubstanceAdministration.moodCode, and the participations are not affected. These properties always have the same meaning: i.e., the author remains the author of the negative SubstanceAdministration. A substance administration statement with negationInd is still a statement about the specific fact described by the SubstanceAdministration. For instance, a negated \"aspirin administration\" means that the author positively denies that aspirin is being administered, and that he takes the same responsibility for such statement and the same requirement to have evidence for such statement than if he had not used negation.\n\nSubstanceAdministration.priorityCode categorizes the priority of a substance administration. SubstanceAdministration.doseQuantity indicates how much medication is given per dose. SubstanceAdministration.rateQuantity can be used to indicate the rate at which the dose is to be administered (e.g., the flow rate for intravenous infusions). SubstanceAdministration.maxDoseQuantity is used to capture the maximum dose of the medication that can be given over a stated time interval (e.g., maximum daily dose of morphine, maximum lifetime dose of doxorubicin). SubstanceAdministration.effectiveTime is used to describe the timing of administration. It is modeled using the GTS data type to accommodate various dosing scenarios.\n\nThe capture of medication-related information also involves the interrelationship of SubstanceAdministration with several other classes. The consumable participation is used to bring in the LabeledDrug or Material entity that describes the administered substance. The LabeledDrug class, which is an Entity class playing the Role of Manufactured Product, identifies the drug that is consumed in the substance administration. The medication is identified by means of the LabeledDrug.code or the LabeledDrug.name. The Material entity is used to identify non-drug administered substances such as vaccines and blood products.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/SubstanceAdministration",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "SubstanceAdministration",
        "path" : "SubstanceAdministration",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "SubstanceAdministration.classCode",
        "path" : "SubstanceAdministration.classCode",
        "representation" : ["xmlAttr"],
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "SBADM",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClass"
        }
      },
      {
        "id" : "SubstanceAdministration.moodCode",
        "path" : "SubstanceAdministration.moodCode",
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
        "id" : "SubstanceAdministration.id",
        "path" : "SubstanceAdministration.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.code",
        "path" : "SubstanceAdministration.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActSubstanceAdministrationCode"
        }
      },
      {
        "id" : "SubstanceAdministration.negationInd",
        "path" : "SubstanceAdministration.negationInd",
        "representation" : ["xmlAttr"],
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
        "id" : "SubstanceAdministration.text",
        "path" : "SubstanceAdministration.text",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ED"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.statusCode",
        "path" : "SubstanceAdministration.statusCode",
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
        "id" : "SubstanceAdministration.effectiveTime",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype",
            "valueCanonical" : "http://hl7.org/cda/stds/core/StructureDefinition/SXCM-TS"
          }
        ],
        "path" : "SubstanceAdministration.effectiveTime",
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
        "id" : "SubstanceAdministration.priorityCode",
        "path" : "SubstanceAdministration.priorityCode",
        "min" : 0,
        "max" : "1",
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
        "id" : "SubstanceAdministration.repeatNumber",
        "path" : "SubstanceAdministration.repeatNumber",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-INT"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.routeCode",
        "path" : "SubstanceAdministration.routeCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-RouteOfAdministration"
        }
      },
      {
        "id" : "SubstanceAdministration.approachSiteCode",
        "path" : "SubstanceAdministration.approachSiteCode",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CD"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActSite"
        }
      },
      {
        "id" : "SubstanceAdministration.doseQuantity",
        "path" : "SubstanceAdministration.doseQuantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.rateQuantity",
        "path" : "SubstanceAdministration.rateQuantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-PQ"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.maxDoseQuantity",
        "path" : "SubstanceAdministration.maxDoseQuantity",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/RTO-PQ-PQ"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.administrationUnitCode",
        "path" : "SubstanceAdministration.administrationUnitCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-AdministrableDrugForm"
        }
      },
      {
        "id" : "SubstanceAdministration.consumable",
        "path" : "SubstanceAdministration.consumable",
        "definition" : "The consumable participation is used to bring in the LabeledDrug or Material entity that describes the administered substance. The LabeledDrug class, which is an Entity class playing the Role of Manufactured Product, identifies the drug that is consumed in the substance administration. The medication is identified by means of the LabeledDrug.code or the LabeledDrug.name. The Material entity is used to identify non-drug administered substances such as vaccines and blood products.",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.consumable.typeCode",
        "path" : "SubstanceAdministration.consumable.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CSM",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "SubstanceAdministration.consumable.manufacturedProduct",
        "path" : "SubstanceAdministration.consumable.manufacturedProduct",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ManufacturedProduct"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.subject",
        "path" : "SubstanceAdministration.subject",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Subject"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.specimen",
        "path" : "SubstanceAdministration.specimen",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Specimen"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.performer",
        "path" : "SubstanceAdministration.performer",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Performer2"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.author",
        "path" : "SubstanceAdministration.author",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Author"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.informant",
        "path" : "SubstanceAdministration.informant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Informant"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.participant",
        "path" : "SubstanceAdministration.participant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Participant2"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.entryRelationship",
        "path" : "SubstanceAdministration.entryRelationship",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EntryRelationship"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.reference",
        "path" : "SubstanceAdministration.reference",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Reference"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.precondition",
        "path" : "SubstanceAdministration.precondition",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/Precondition"
          }
        ]
      },
      {
        "id" : "SubstanceAdministration.sdtcInFulfillmentOf1",
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
        "path" : "SubstanceAdministration.sdtcInFulfillmentOf1",
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
