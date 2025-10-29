# AD: PostalAddress (V3 Data Type) - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **AD: PostalAddress (V3 Data Type)**

## Logical Model: AD: PostalAddress (V3 Data Type) 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/StructureDefinition/AD | *Version*:2.0.1-sd-202510-matchbox-patch |
| Active as of 2025-10-29 | *Computable Name*:AD |

 
Mailing and home or office addresses. A sequence of address parts, such as street or post office Box, city, postal code, country, etc. 

**Usages:**

* Use this Logical Model: [AssignedAuthor (CDA Class)](StructureDefinition-AssignedAuthor.md), [AssignedEntity (CDA Class)](StructureDefinition-AssignedEntity.md), [AssociatedEntity (CDA Class)](StructureDefinition-AssociatedEntity.md), [Criterion (CDA Class)](StructureDefinition-Criterion.md)...Show 12 more,[CustodianOrganization (CDA Class)](StructureDefinition-CustodianOrganization.md),[Guardian (CDA Class)](StructureDefinition-Guardian.md),[IntendedRecipient (CDA Class)](StructureDefinition-IntendedRecipient.md),[LabCriterion (CDA Class)](StructureDefinition-LabCriterion.md),[Observation (CDA Class)](StructureDefinition-Observation.md),[ObservationRange (CDA Class)](StructureDefinition-ObservationRange.md),[Organization (CDA Class)](StructureDefinition-Organization.md),[ParticipantRole (CDA Class)](StructureDefinition-ParticipantRole.md),[PatientRole (CDA Class)](StructureDefinition-PatientRole.md),[Place (CDA Class)](StructureDefinition-Place.md),[RelatedEntity (CDA Class)](StructureDefinition-RelatedEntity.md)and[RelatedSubject (CDA Class)](StructureDefinition-RelatedSubject.md)

You can also check for [usages in the FHIR IG Statistics](https://packages2.fhir.org/xig/hl7.cda.uv.core|current/StructureDefinition/AD)

### Formal Views of Template Content

 [Description of Template, Differentials, Snapshots and how the different presentations work](http://hl7.org/fhir/R5/profiling.html#presentation). 

 

Other representations of profile: [CSV](StructureDefinition-AD.csv), [Excel](StructureDefinition-AD.xlsx) 



## Resource Content

```json
{
  "resourceType" : "StructureDefinition",
  "id" : "AD",
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
  "url" : "http://hl7.org/cda/stds/core/StructureDefinition/AD",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "AD",
  "title" : "AD: PostalAddress (V3 Data Type)",
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
  "description" : "Mailing and home or office addresses. A sequence of address parts, such as street or post office Box, city, postal code, country, etc.",
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
  "type" : "http://hl7.org/cda/stds/core/StructureDefinition/AD",
  "baseDefinition" : "http://hl7.org/cda/stds/core/StructureDefinition/ANY",
  "derivation" : "specialization",
  "differential" : {
    "element" : [
      {
        "id" : "AD",
        "path" : "AD",
        "definition" : "Mailing and home or office addresses. A sequence of address parts, such as street or post office Box, city, postal code, country, etc.",
        "min" : 1,
        "max" : "*",
        "base" : {
          "path" : "ANY",
          "min" : 1,
          "max" : "*"
        }
      },
      {
        "id" : "AD.isNotOrdered",
        "path" : "AD.isNotOrdered",
        "representation" : ["xmlAttr"],
        "label" : "Is Not Ordered",
        "definition" : "A boolean value specifying whether the order of the address parts is known or not. While the address parts are always a Sequence, the order in which they are presented may or may not be known. Where this matters, the isNotOrdered property can be used to convey this information.",
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
        "id" : "AD.use",
        "path" : "AD.use",
        "representation" : ["xmlAttr"],
        "label" : "Use Code",
        "definition" : "A set of codes advising a system or user which address in a set of like addresses to select for a given purpose.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "binding" : {
          "strength" : "required",
          "valueSet" : "http://hl7.org/cda/stds/core/ValueSet/CDAPostalAddressUse"
        }
      },
      {
        "id" : "AD.item",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/tools/StructureDefinition/xml-choice-group",
            "valueBoolean" : true
          }
        ],
        "path" : "AD.item",
        "definition" : "A series of items that constitute the address.",
        "min" : 0,
        "max" : "*",
        "type" : [
          {
            "code" : "http://hl7.org/fhir/StructureDefinition/Base"
          }
        ],
        "constraint" : [
          {
            "key" : "AD-1",
            "severity" : "error",
            "human" : "Can only have only one of the possible item elements in each choice",
            "expression" : "(delimiter | country | state | county | city | postalCode | streetAddressLine | houseNumber | houseNumberNumeric | direction | streetName | streetNameBase | streetNameType | additionalLocator | unitID | unitType | careOf | censusTract | deliveryAddressLine | deliveryInstallationType | deliveryInstallationArea | deliveryInstallationQualifier | deliveryMode | deliveryModeIdentifier | buildingNumberSuffix | postBox | precinct | xmlText).count() = 1"
          }
        ]
      },
      {
        "id" : "AD.item.delimiter",
        "path" : "AD.item.delimiter",
        "definition" : "Delimiters are printed without framing white space. If no value component is provided, the delimiter appears as a line break.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.delimiter.partType",
        "path" : "AD.item.delimiter.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DEL"
      },
      {
        "id" : "AD.item.country",
        "path" : "AD.item.country",
        "definition" : "Country",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.country.partType",
        "path" : "AD.item.country.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CNT"
      },
      {
        "id" : "AD.item.state",
        "path" : "AD.item.state",
        "definition" : "A sub-unit of a country with limited sovereignty in a federally organized country.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.state.partType",
        "path" : "AD.item.state.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "STA"
      },
      {
        "id" : "AD.item.county",
        "path" : "AD.item.county",
        "definition" : "A sub-unit of a state or province. (49 of the United States of America use the term \"county;\" Louisiana uses the term \"parish\".)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.county.partType",
        "path" : "AD.item.county.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CPA"
      },
      {
        "id" : "AD.item.city",
        "path" : "AD.item.city",
        "definition" : "The name of the city, town, village, or other community or delivery center",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.city.partType",
        "path" : "AD.item.city.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CTY"
      },
      {
        "id" : "AD.item.postalCode",
        "path" : "AD.item.postalCode",
        "definition" : "A postal code designating a region defined by the postal service.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.postalCode.partType",
        "path" : "AD.item.postalCode.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "ZIP"
      },
      {
        "id" : "AD.item.streetAddressLine",
        "path" : "AD.item.streetAddressLine",
        "definition" : "Street address line",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.streetAddressLine.partType",
        "path" : "AD.item.streetAddressLine.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "SAL"
      },
      {
        "id" : "AD.item.houseNumber",
        "path" : "AD.item.houseNumber",
        "definition" : "The number of a building, house or lot alongside the street. Also known as \"primary street number\". This does not number the street but rather the building.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.houseNumber.partType",
        "path" : "AD.item.houseNumber.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "BNR"
      },
      {
        "id" : "AD.item.houseNumberNumeric",
        "path" : "AD.item.houseNumberNumeric",
        "definition" : "The numeric portion of a building number",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.houseNumberNumeric.partType",
        "path" : "AD.item.houseNumberNumeric.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "BNN"
      },
      {
        "id" : "AD.item.direction",
        "path" : "AD.item.direction",
        "definition" : "Direction (e.g., N, S, W, E)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.direction.partType",
        "path" : "AD.item.direction.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DIR"
      },
      {
        "id" : "AD.item.streetName",
        "path" : "AD.item.streetName",
        "definition" : "Name of a roadway or artery recognized by a municipality (including street type and direction)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.streetName.partType",
        "path" : "AD.item.streetName.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "STR"
      },
      {
        "id" : "AD.item.streetNameBase",
        "path" : "AD.item.streetNameBase",
        "definition" : "The base name of a roadway or artery recognized by a municipality (excluding street type and direction)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.streetNameBase.partType",
        "path" : "AD.item.streetNameBase.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "STB"
      },
      {
        "id" : "AD.item.streetNameType",
        "path" : "AD.item.streetNameType",
        "definition" : "The designation given to the street. (e.g. Street, Avenue, Crescent, etc.)",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.streetNameType.partType",
        "path" : "AD.item.streetNameType.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "STTYP"
      },
      {
        "id" : "AD.item.additionalLocator",
        "path" : "AD.item.additionalLocator",
        "definition" : "This can be a unit designator, such as apartment number, suite number, or floor. There may be several unit designators in an address (e.g., \"3rd floor, Appt. 342\"). This can also be a designator pointing away from the location, rather than specifying a smaller location within some larger one (e.g., Dutch \"t.o.\" means \"opposite to\" for house boats located across the street facing houses).",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.additionalLocator.partType",
        "path" : "AD.item.additionalLocator.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "ADL"
      },
      {
        "id" : "AD.item.unitID",
        "path" : "AD.item.unitID",
        "definition" : "The number or name of a specific unit contained within a building or complex, as assigned by that building or complex.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.unitID.partType",
        "path" : "AD.item.unitID.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "UNID"
      },
      {
        "id" : "AD.item.unitType",
        "path" : "AD.item.unitType",
        "definition" : "Indicates the type of specific unit contained within a building or complex. E.g. Appartment, Floor",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.unitType.partType",
        "path" : "AD.item.unitType.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "UNIT"
      },
      {
        "id" : "AD.item.careOf",
        "path" : "AD.item.careOf",
        "definition" : "The name of the party who will take receipt at the specified address, and will take on responsibility for ensuring delivery to the target recipient",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.careOf.partType",
        "path" : "AD.item.careOf.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CAR"
      },
      {
        "id" : "AD.item.censusTract",
        "path" : "AD.item.censusTract",
        "definition" : "A geographic sub-unit delineated for demographic purposes.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.censusTract.partType",
        "path" : "AD.item.censusTract.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "CEN"
      },
      {
        "id" : "AD.item.deliveryAddressLine",
        "path" : "AD.item.deliveryAddressLine",
        "definition" : "A delivery address line is frequently used instead of breaking out delivery mode, delivery installation, etc. An address generally has only a delivery address line or a street address line, but not both.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryAddressLine.partType",
        "path" : "AD.item.deliveryAddressLine.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DAL"
      },
      {
        "id" : "AD.item.deliveryInstallationType",
        "path" : "AD.item.deliveryInstallationType",
        "definition" : "Indicates the type of delivery installation (the facility to which the mail will be delivered prior to final shipping via the delivery mode.) Example: post office, letter carrier depot, community mail center, station, etc.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryInstallationType.partType",
        "path" : "AD.item.deliveryInstallationType.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DINST"
      },
      {
        "id" : "AD.item.deliveryInstallationArea",
        "path" : "AD.item.deliveryInstallationArea",
        "definition" : "The location of the delivery installation, usually a town or city, and is only required if the area is different from the municipality. Area to which mail delivery service is provided from any postal facility or service such as an individual letter carrier, rural route, or postal route.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryInstallationArea.partType",
        "path" : "AD.item.deliveryInstallationArea.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DINSTA"
      },
      {
        "id" : "AD.item.deliveryInstallationQualifier",
        "path" : "AD.item.deliveryInstallationQualifier",
        "definition" : "A number, letter or name identifying a delivery installation. E.g., for Station A, the delivery installation qualifier would be 'A'.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryInstallationQualifier.partType",
        "path" : "AD.item.deliveryInstallationQualifier.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DINSTQ"
      },
      {
        "id" : "AD.item.deliveryMode",
        "path" : "AD.item.deliveryMode",
        "definition" : "Indicates the type of service offered, method of delivery. For example: post office box, rural route, general delivery, etc.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryMode.partType",
        "path" : "AD.item.deliveryMode.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DMOD"
      },
      {
        "id" : "AD.item.deliveryModeIdentifier",
        "path" : "AD.item.deliveryModeIdentifier",
        "definition" : "Represents the routing information such as a letter carrier route number. It is the identifying number of the designator (the box number or rural route number).",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.deliveryModeIdentifier.partType",
        "path" : "AD.item.deliveryModeIdentifier.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "DMODID"
      },
      {
        "id" : "AD.item.buildingNumberSuffix",
        "path" : "AD.item.buildingNumberSuffix",
        "definition" : "Any alphabetic character, fraction or other text that may appear after the numeric portion of a building number",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.buildingNumberSuffix.partType",
        "path" : "AD.item.buildingNumberSuffix.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "BNS"
      },
      {
        "id" : "AD.item.postBox",
        "path" : "AD.item.postBox",
        "definition" : "A numbered box located in a post station.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.postBox.partType",
        "path" : "AD.item.postBox.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "POB"
      },
      {
        "id" : "AD.item.precinct",
        "path" : "AD.item.precinct",
        "definition" : "A subsection of a municipality",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "http://hl7.org/cda/stds/core/StructureDefinition/ADXP"
          }
        ]
      },
      {
        "id" : "AD.item.precinct.partType",
        "path" : "AD.item.precinct.partType",
        "representation" : ["xmlAttr"],
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "code",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/cs-simple"]
          }
        ],
        "fixedCode" : "PRE"
      },
      {
        "id" : "AD.item.xmlText",
        "path" : "AD.item.xmlText",
        "representation" : ["xmlText"],
        "short" : "Allows for mixed text content",
        "comment" : "This element is represented in XML as textual content. The actual name \"xmlText\" will not appear in a CDA instance.",
        "min" : 0,
        "max" : "1",
        "type" : [
          {
            "code" : "string",
            "profile" : ["http://hl7.org/cda/stds/core/StructureDefinition/st-simple"]
          }
        ]
      },
      {
        "id" : "AD.useablePeriod",
        "extension" : [
          {
            "url" : "http://hl7.org/fhir/StructureDefinition/elementdefinition-defaulttype",
            "valueCanonical" : "http://hl7.org/cda/stds/core/StructureDefinition/SXPR-TS"
          }
        ],
        "path" : "AD.useablePeriod",
        "representation" : ["typeAttr"],
        "label" : "Useable Period",
        "definition" : "A General Timing Specification (GTS) specifying the periods of time during which the address can be used. This is used to specify different addresses for different times of the week or year.",
        "min" : 0,
        "max" : "*",
        "type" : [
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
      }
    ]
  }
}

```
