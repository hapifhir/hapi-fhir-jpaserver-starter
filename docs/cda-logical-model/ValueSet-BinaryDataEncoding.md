# CDABinaryDataEncoding - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDABinaryDataEncoding**

## ValueSet: CDABinaryDataEncoding 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/BinaryDataEncoding | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:BinaryDataEncoding |

 
Identifies the representation of binary data in a text field 

 **References** 

* [ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md)

### Logical Definition (CLD)

* Include all codes defined in [`http://hl7.org/cda/stds/core/CodeSystem/BinaryDataEncoding`](CodeSystem-BinaryDataEncoding.md)version 📦2.0.1-sd-202510-matchbox-patch

 

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
  "id" : "BinaryDataEncoding",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/BinaryDataEncoding",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "BinaryDataEncoding",
  "title" : "CDABinaryDataEncoding",
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
  "description" : "Identifies the representation of binary data in a text field",
  "compose" : {
    "include" : [
      {
        "system" : "http://hl7.org/cda/stds/core/CodeSystem/BinaryDataEncoding"
      }
    ]
  }
}

```
