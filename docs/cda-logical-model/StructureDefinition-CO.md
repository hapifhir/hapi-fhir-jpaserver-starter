# CO: CodedOrdinal (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CO: CodedOrdinal (V3 Data Type)**

## Logical Model: CO: CodedOrdinal (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/CO | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:CO |

 
Coded data, where the coding system from which the code comes is ordered 

**Usages:**

* Use this Logical Model: [Observation (CDA Class)](StructureDefinition-Observation.md) and [ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/CO)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-CO.csv), [Excel](StructureDefinition-CO.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "CO",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/CO",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CO",
  "title" : "CO: CodedOrdinal (V3 Data Type)",
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
  "description" : "Coded data, where the coding system from which the code comes is ordered",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/CO",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/CV",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "CO",
        "path" : "CO",
        "definition" : "Coded data, where the domain from which the codeset comes is ordered. The Coded Ordinal data type adds semantics related to ordering so that models that make use of such domains may introduce model elements that involve statements about the order of the terms in a domain.",
        "min" : 1,
        "max" : "*"
      }
    ]
  }
}

```
