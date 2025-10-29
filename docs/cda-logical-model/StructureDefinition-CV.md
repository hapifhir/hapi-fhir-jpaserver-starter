# CV: CodedValue (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CV: CodedValue (V3 Data Type)**

## Logical Model: CV: CodedValue (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CV | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CV |

 
Coded data, specifying only a code, code system, and optionally display name and original text. Used only as the data type for other data types' properties. 

**Usages:**

* Derived from this Logical Model: [CO: CodedOrdinal (V3 Data Type)](StructureDefinition-CO.md), [CS: CodedSimpleValue (V3 Data Type)](StructureDefinition-CS.md) and [PQR: PhysicalQuantityRepresentation (V3 Data Type)](StructureDefinition-PQR.md)
* Use this Logical Model: [CR: ConceptRole (V3 Data Type)](StructureDefinition-CR.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md), [LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md), [Observation (CDA Class)](StructureDefinition-Observation.md) and [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CV)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CV.csv), [Excel](StructureDefinition-CV.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CV",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CV",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CV",
  "title" : "CV: CodedValue (V3 Data Type)",
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
  "description" : "Coded data, specifying only a code, code system, and optionally display name and original text. Used only as the data type for other data types' properties.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CV",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/CE",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CV",
        "path" : "CV",
        "definition" : "Coded data, consists of a code, display name, code system, and original text. Used when a single code value must be sent.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "CV.translation",
        "path" : "CV.translation",
        "label" : "Translation",
        "definition" : "A set of other concept descriptors that translate this concept descriptor into other code systems.",
        "min" : 0,
        "max" : "0"
      }
    ]
  }
}

```
