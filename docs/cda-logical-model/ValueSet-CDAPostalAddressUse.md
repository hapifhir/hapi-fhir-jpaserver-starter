# CDAPostalAddressUse - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAPostalAddressUse**

## ValueSet: CDAPostalAddressUse 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAPostalAddressUse | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAPostalAddressUse |

 
A set of codes advising a system or user which address in a set of like addresses to select for a given purpose - limited to values allowed in original CDA definition 

 **References** 

* [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md)

### Logical Definition (CLD)

 

### Expansion

This value set contains 13 concepts

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
  "id" : "CDAPostalAddressUse",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAPostalAddressUse",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAPostalAddressUse",
  "title" : "CDAPostalAddressUse",
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
  "description" : "A set of codes advising a system or user which address in a set of like addresses to select for a given purpose - limited to values allowed in original CDA definition",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-AddressUse",
        "concept" : [
          {
            "code" : "H"
          },
          {
            "code" : "HP"
          },
          {
            "code" : "HV"
          },
          {
            "code" : "WP"
          },
          {
            "code" : "DIR"
          },
          {
            "code" : "PUB"
          },
          {
            "code" : "BAD"
          },
          {
            "code" : "TMP"
          },
          {
            "code" : "PHYS"
          },
          {
            "code" : "PST"
          }
        ]
      },
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-EntityNameUse",
        "concept" : [
          {
            "code" : "ABC"
          },
          {
            "code" : "IDE"
          },
          {
            "code" : "SYL"
          }
        ]
      }
    ]
  }
}

```
