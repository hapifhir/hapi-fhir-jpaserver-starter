# CDASetOperator - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDASetOperator**

## ValueSet: CDASetOperator 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDASetOperator | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDASetOperator |

 
Determins the intersectionality of multiple sets 

 **References** 

* [IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md)
* [IVL_PQ: Interval (V3 Data Type)](StructureDefinition-IVL-PQ.md)
* [SXCM_TS: GeneralTimingSpecification (V3 Data Type)](StructureDefinition-SXCM-TS.md)

### Logical Definition (CLD)

 

### Expansion

-------

 Explanation of the columns that may appear on this page: 

| | |
| :--- | :--- |
| Level | A few code lists that FHIR defines are hierarchical - each code is assigned a level. In this scheme, some codes are under other codes, and imply that the code they are under also applies |
| System | The source of the definition of the code (when the value set draws in codes defined elsewhere) |
| Code | The code (used as the code in the resource instance) |
| Display | The display (used in the*display*element of a[Coding](http://hl7.org/fhir/R5/datatypes.html#Coding)). If there is no display, implementers should not simply display the code, but map the concept into their application |
| Definition | An explanation of the meaning of the concept |
| Comments | Additional notes about how to use the code |



## Resource Content

```json
{
  "resourceType" : "ValueSet",
  "id" : "CDASetOperator",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDASetOperator",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDASetOperator",
  "title" : "CDASetOperator",
  "status" : "draft",
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
  "description" : "Determins the intersectionality of multiple sets",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-SetOperator",
        "concept" : [
          {
            "code" : "A"
          },
          {
            "code" : "E"
          },
          {
            "code" : "H"
          },
          {
            "code" : "I"
          },
          {
            "code" : "P"
          }
        ]
      }
    ]
  }
}

```
