# Binary Data Encoding Code System - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **Binary Data Encoding Code System**

## CodeSystem: Binary Data Encoding Code System 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/CodeSystem/BinaryDataEncoding | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:BinaryDataEncoding |

 
Identifies the representation of binary data in a text field 

 This Code system is referenced in the content logical definition of the following value sets: 

* [BinaryDataEncoding](ValueSet-BinaryDataEncoding.md)



## Resource Content

```json
{
  "resourceType" : "CodeSystem",
  "id" : "BinaryDataEncoding",
  "url" : "http://hl7.org/cda/stds/core/CodeSystem/BinaryDataEncoding",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "BinaryDataEncoding",
  "title" : "Binary Data Encoding Code System",
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
  "caseSensitive" : true,
  "content" : "complete",
  "count" : 2,
  "concept" : [
    {
      "code" : "B64",
      "display" : "Base64-encoded text"
    },
    {
      "code" : "TXT",
      "display" : "Plain text"
    }
  ]
}

```
