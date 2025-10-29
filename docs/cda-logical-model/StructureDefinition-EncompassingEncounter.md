# EncompassingEncounter (CDA Class) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **EncompassingEncounter (CDA Class)**

## Logical Model: EncompassingEncounter (CDA Class) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/EncompassingEncounter | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:EncompassingEncounter |

 
This optional class represents the setting of the clinical encounter during which the documented act(s) or ServiceEvent occurred. Documents are not necessarily generated during an encounter, such as when a clinician, in response to an abnormal lab result, attempts to contact the patient but can't, and writes a Progress Note. 
In some cases, the setting of the encounter is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is "Diabetes Clinic Progress Note". The setting of an encounter can also be transmitted in the HealthCareFacility.code attribute. If HealthCareFacility.code is sent, it should be equivalent to or further specialize the value inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply "Clinic Progress Note" and the value of HealthCareFacility.code is "cardiology clinic"), and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation. 
EncompassingEncounter.dischargeDispositionCode can be used to depict the disposition of the patient at the time of hospital discharge (e.g., discharged to home, expired, against medical advice, etc.). 

**Usages:**

* Use this Logical Model: [ComponentOf (CDA Class)](StructureDefinition-ComponentOf.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/EncompassingEncounter)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-EncompassingEncounter.csv), [Excel](StructureDefinition-EncompassingEncounter.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "EncompassingEncounter",
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
      "valueString" : "encompassingEncounter"
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/EncompassingEncounter",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "EncompassingEncounter",
  "title" : "EncompassingEncounter (CDA Class)",
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
  "description" : "This optional class represents the setting of the clinical encounter during which the documented act(s) or ServiceEvent occurred. Documents are not necessarily generated during an encounter, such as when a clinician, in response to an abnormal lab result, attempts to contact the patient but can't, and writes a Progress Note.\n\nIn some cases, the setting of the encounter is inherent in the ClinicalDocument.code, such as where ClinicalDocument.code is \"Diabetes Clinic Progress Note\". The setting of an encounter can also be transmitted in the HealthCareFacility.code attribute. If HealthCareFacility.code is sent, it should be equivalent to or further specialize the value inherent in the ClinicalDocument.code (such as where the ClinicalDocument.code is simply \"Clinic Progress Note\" and the value of HealthCareFacility.code is \"cardiology clinic\"), and shall not conflict with the value inherent in the ClinicalDocument.code, as such a conflict would constitute an ambiguous situation.\n\nEncompassingEncounter.dischargeDispositionCode can be used to depict the disposition of the patient at the time of hospital discharge (e.g., discharged to home, expired, against medical advice, etc.).",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/EncompassingEncounter",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "EncompassingEncounter",
        "path" : "EncompassingEncounter",
        "min" : 1,
        "max" : "1"
      },
      {
        "id" : "EncompassingEncounter.classCode",
        "path" : "EncompassingEncounter.classCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "ENC",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActClass"
        }
      },
      {
        "id" : "EncompassingEncounter.moodCode",
        "path" : "EncompassingEncounter.moodCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "EVN",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAActMood"
        }
      },
      {
        "id" : "EncompassingEncounter.id",
        "path" : "EncompassingEncounter.id",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/II"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.code",
        "path" : "EncompassingEncounter.code",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ActEncounterCode"
        }
      },
      {
        "id" : "EncompassingEncounter.effectiveTime",
        "path" : "EncompassingEncounter.effectiveTime",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/IVL-TS"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.sdtcAdmissionReferralSourceCode",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
            "valueUri" : "urn:hl7-org:sdtc"
          },
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-name",
            "valueString" : "admissionReferralSourceCode"
          }
        ],
        "path" : "EncompassingEncounter.sdtcAdmissionReferralSourceCode",
        "definition" : "This element is a coded concept that represents the type of referral. Its RIM source class is PatientEncounter.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.dischargeDispositionCode",
        "path" : "EncompassingEncounter.dischargeDispositionCode",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/CE"
          }
        ],
        "binding" : {
          "strength" : "example",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-USEncounterDischargeDisposition"
        }
      },
      {
        "id" : "EncompassingEncounter.responsibleParty",
        "path" : "EncompassingEncounter.responsibleParty",
        "definition" : "The responsibleParty participant represents the participant having primary legal responsibility for the encounter. This differs from the legalAuthenticator participant in that the legalAuthenticator may or may not be the responsible party, and is serving a medical records function by signing off on the document, moving it into a completed state.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.responsibleParty.typeCode",
        "path" : "EncompassingEncounter.responsibleParty.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "RESP",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAParticipationType"
        }
      },
      {
        "id" : "EncompassingEncounter.responsibleParty.assignedEntity",
        "path" : "EncompassingEncounter.responsibleParty.assignedEntity",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/AssignedEntity"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.encounterParticipant",
        "path" : "EncompassingEncounter.encounterParticipant",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/EncounterParticipant"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.location",
        "path" : "EncompassingEncounter.location",
        "definition" : "The location participant (location class) relates a healthcare facility (HealthCareFacility class) to the encounter to indicate where the encounter took place. The entity playing the role of HealthCareFacility is a place (Place class). The entity scoping the HealthCareFacility role is an organization (Organization class).\n\nThe setting of an encounter (e.g. cardiology clinic, primary care clinic, rehabilitation hospital, skilled nursing facility) can be expressed in HealthCareFacility.code. Note that setting and physical location are not the same. There is a many-to-many relationship between setting and the physical location where care is delivered. Thus, a particular room can provide the location for cardiology clinic one day, and for primary care clinic another day; and cardiology clinic today might be held in one physical location, but in another physical location tomorrow.\n\nWhen the location is an organization, this is indicated by the presence of a scoping Organization, without a playing Place.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/InfrastructureRoot"
          }
        ]
      },
      {
        "id" : "EncompassingEncounter.location.typeCode",
        "path" : "EncompassingEncounter.location.typeCode",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "LOC",
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://terminology.hl7.org/ValueSet/v3-ParticipationTargetLocation"
        }
      },
      {
        "id" : "EncompassingEncounter.location.healthCareFacility",
        "path" : "EncompassingEncounter.location.healthCareFacility",
        "min" : 1,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/HealthCareFacility"
          }
        ]
      }
    ]
  }
}

```
