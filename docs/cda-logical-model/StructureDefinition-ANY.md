# ANY: DataValue (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **ANY: DataValue (V3 Data Type)**

## Logical Model: ANY: DataValue (V3 Data Type) ( Abstract ) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/ANY | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:ANY |

 
Defines the basic properties of every data value. This is an abstract type, meaning that no value can be just a data value without belonging to any concrete type. Every concrete type is a specialization of this general abstract DataValue type. 

**Usages:**

* Derived from this Logical Model: [AD: PostalAddress (V3 Data Type)](StructureDefinition-AD.md), [BL: Boolean (V3 Data Type)](StructureDefinition-BL.md), [CD: ConceptDescriptor (V3 Data Type)](StructureDefinition-CD.md), [CR: ConceptRole (V3 Data Type)](StructureDefinition-CR.md)...Show 10 more,[ClinicalDocument (CDA Class)](StructureDefinition-ClinicalDocument.md),[ED: EncapsulatedData (V3 Data Type)](StructureDefinition-ED.md),[EN: EntityName (V3 Data Type)](StructureDefinition-EN.md),[II: InstanceIdentifier (V3 Data Type)](StructureDefinition-II.md),[IVL_INT: Interval (V3 Data Type)](StructureDefinition-IVL-INT.md),[InfrastructureRoot (Base Type for all CDA Classes)](StructureDefinition-InfrastructureRoot.md),[QTY: Quantity (V3 Data Type)](StructureDefinition-QTY.md),[ST-dot-r2b: CharacterString with Value Attribute (V3 Data Type)](StructureDefinition-ST-dot-r2b.md),[ST: CharacterString (V3 Data Type)](StructureDefinition-ST.md)and[TEL: TelecommunicationAddress (V3 Data Type)](StructureDefinition-TEL.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/ANY)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-ANY.csv), [Excel](StructureDefinition-ANY.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "ANY",
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
      "url" : "http://hl7.org/fhir/tools/StructureDefinition/logical-container",
      "valueUri" : "http://hl7.org/cda/stds/core/StructureDefinition/ClinicalDocument"
    }
  ],
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "ANY",
  "title" : "ANY: DataValue (V3 Data Type)",
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
  "description" : "Defines the basic properties of every data value. This is an abstract type, meaning that no value can be just a data value without belonging to any concrete type. Every concrete type is a specialization of this general abstract DataValue type.",
  "fhirVersion" : "5.0.0",
  "mapping" : [
    {
      "identity" : "rim",
      "uri" : "http://hl7.org/v3",
      "name" : "RIM Mapping"
    }
  ],
  "kind" : "logical",
  "abstract" : true,
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "baseDefinition" : "http://hl7.org/fhir/StructureDefinition/Base",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "ANY",
        "path" : "ANY",
        "definition" : "Defines the basic properties of every data value. This is an abstract type, meaning that no value can be just a data value without belonging to any concrete type. Every concrete type is a specialization of this general abstract DataValue type.",
        "min" : 1,
        "max" : "*"
      },
      {
        "id" : "ANY.nullFlavor",
        "path" : "ANY.nullFlavor",
        "representation" : ["xmlAttr"],
        "label" : "Exceptional Value Detail",
        "definition" : "If a value is an exceptional value (NULL-value), this specifies in what way and why proper information is missing.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDANullFlavor"
        }
      }
    ]
  }
}

```
