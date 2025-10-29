# url: Universal Resource Locator - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **url: Universal Resource Locator**

## Data Type Profile: url: Universal Resource Locator 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/url | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:url |

 
A telecommunications address specified according to Internet standard RFC 1738 [http://www.ietf.org/rfc/rfc1738.txt]. The URL specifies the protocol and the contact point defined by that protocol for the resource. Notable uses of the telecommunication address data type are for telephone and telefax numbers, e-mail addresses, Hypertext references, FTP references, etc. 

**Usages:**

* Use this Primitive Type Profile: [TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/url)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-url.csv), [Excel](StructureDefinition-url.xlsx), [Schematron](StructureDefinition-url.sch) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "url",
  "extension" : [
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-namespace",
      "valueUri" : "urn:hl7-org:v3"
    },
    {
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    },
    {
      "url" : "http://hl7.org/fhir/StructureDefinition/structuredefinition-type-characteristics",
      "valueCode" : "can-bind"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/url",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "url",
  "title" : "url: Universal Resource Locator",
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
  "description" : "A telecommunications address specified according to Internet standard RFC 1738 [http://www.ietf.org/rfc/rfc1738.txt]. The URL specifies the protocol and the contact point defined by that protocol for the resource.  Notable uses of the telecommunication address data type are for telephone and telefax numbers, e-mail addresses, Hypertext references, FTP references, etc.",
  "fhirVersion" : "5.0.0",
  "kind" : "primitive-type",
  "abstract" : false,
  "type" : "url",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/url",
  "derivation" : "constraint",
  "differential" : {
    "element" : [
      {
        "id" : "url.id",
        "path" : "url.id",
        "max" : "0"
      },
      {
        "id" : "url.extension",
        "path" : "url.extension",
        "max" : "0"
      }
    ]
  }
}

```
