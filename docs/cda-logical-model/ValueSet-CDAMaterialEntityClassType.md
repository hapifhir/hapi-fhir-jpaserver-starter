# CDAMaterialEntityClassType - Clinical Document Architecture v2.0.1-sd-202510-matchbox-patch

* [**Table of Contents**](toc.md)
* [**Artifacts Summary**](artifacts.md)
* **CDAMaterialEntityClassType**

## ValueSet: CDAMaterialEntityClassType 

| | |
| :--- | :--- |
| *Official URL*:http://hl7.org/cda/stds/core/ValueSet/CDAMaterialEntityClassType | *Version*:2.0.1-sd-202510-matchbox-patch |
| Draft as of 2025-10-29 | *Computable Name*:CDAMaterialEntityClassType |

 
Types of Material for EntityClass “MAT” 

 **References** 

* Included into [CDAEntityCode](ValueSet-CDAEntityCode.md)
* [Material (CDA Class)](StructureDefinition-Material.md)

### Logical Definition (CLD)

 

### Expansion

This value set contains 227 concepts

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
  "id" : "CDAMaterialEntityClassType",
  "url" : "http://hl7.org/cda/stds/core/ValueSet/CDAMaterialEntityClassType",
  "version" : "2.0.1-sd-202510-matchbox-patch",
  "name" : "CDAMaterialEntityClassType",
  "title" : "CDAMaterialEntityClassType",
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
  "description" : "Types of Material for EntityClass “MAT”",
  "compose" : {
    "include" : [
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-EntityCode",
        "concept" : [
          {
            "code" : "PKG"
          },
          {
            "code" : "BAG"
          },
          {
            "code" : "PACKT"
          },
          {
            "code" : "PCH"
          },
          {
            "code" : "SACH"
          },
          {
            "code" : "AMP"
          },
          {
            "code" : "MINIM"
          },
          {
            "code" : "NEBAMP"
          },
          {
            "code" : "OVUL"
          },
          {
            "code" : "BOT"
          },
          {
            "code" : "BOTA"
          },
          {
            "code" : "BOTD"
          },
          {
            "code" : "BOTG"
          },
          {
            "code" : "BOTP"
          },
          {
            "code" : "BOTPLY"
          },
          {
            "code" : "BOX"
          },
          {
            "code" : "CAN"
          },
          {
            "code" : "CART"
          },
          {
            "code" : "CNSTR"
          },
          {
            "code" : "JAR"
          },
          {
            "code" : "JUG"
          },
          {
            "code" : "TIN"
          },
          {
            "code" : "TUB"
          },
          {
            "code" : "TUBE"
          },
          {
            "code" : "VIAL"
          },
          {
            "code" : "BLSTRPK"
          },
          {
            "code" : "CARD"
          },
          {
            "code" : "COMPPKG"
          },
          {
            "code" : "DIALPK"
          },
          {
            "code" : "DISK"
          },
          {
            "code" : "DOSET"
          },
          {
            "code" : "STRIP"
          },
          {
            "code" : "KIT"
          },
          {
            "code" : "SYSTM"
          },
          {
            "code" : "LINE"
          },
          {
            "code" : "IALINE"
          },
          {
            "code" : "IVLINE"
          },
          {
            "code" : "AINJ"
          },
          {
            "code" : "PEN"
          },
          {
            "code" : "SYR"
          },
          {
            "code" : "APLCTR"
          },
          {
            "code" : "INH"
          },
          {
            "code" : "DSKS"
          },
          {
            "code" : "DSKUNH"
          },
          {
            "code" : "TRBINH"
          },
          {
            "code" : "PMP"
          },
          {
            "code" : "ACDA"
          },
          {
            "code" : "ACDB"
          },
          {
            "code" : "ACET"
          },
          {
            "code" : "AMIES"
          },
          {
            "code" : "BACTM"
          },
          {
            "code" : "BF10"
          },
          {
            "code" : "BOR"
          },
          {
            "code" : "BOUIN"
          },
          {
            "code" : "BSKM"
          },
          {
            "code" : "C32"
          },
          {
            "code" : "C38"
          },
          {
            "code" : "CARS"
          },
          {
            "code" : "CARY"
          },
          {
            "code" : "CHLTM"
          },
          {
            "code" : "CTAD"
          },
          {
            "code" : "EDTK15"
          },
          {
            "code" : "EDTK75"
          },
          {
            "code" : "EDTN"
          },
          {
            "code" : "ENT"
          },
          {
            "code" : "F10"
          },
          {
            "code" : "FDP"
          },
          {
            "code" : "FL10"
          },
          {
            "code" : "FL100"
          },
          {
            "code" : "HCL6"
          },
          {
            "code" : "HEPA"
          },
          {
            "code" : "HEPL"
          },
          {
            "code" : "HEPN"
          },
          {
            "code" : "HNO3"
          },
          {
            "code" : "JKM"
          },
          {
            "code" : "KARN"
          },
          {
            "code" : "KOX"
          },
          {
            "code" : "LIA"
          },
          {
            "code" : "M4"
          },
          {
            "code" : "M4RT"
          },
          {
            "code" : "M5"
          },
          {
            "code" : "MICHTM"
          },
          {
            "code" : "MMDTM"
          },
          {
            "code" : "NAF"
          },
          {
            "code" : "NONE"
          },
          {
            "code" : "PAGE"
          },
          {
            "code" : "PHENOL"
          },
          {
            "code" : "PVA"
          },
          {
            "code" : "RLM"
          },
          {
            "code" : "SILICA"
          },
          {
            "code" : "SPS"
          },
          {
            "code" : "SST"
          },
          {
            "code" : "STUTM"
          },
          {
            "code" : "THROM"
          },
          {
            "code" : "THYMOL"
          },
          {
            "code" : "THYO"
          },
          {
            "code" : "TOLU"
          },
          {
            "code" : "URETM"
          },
          {
            "code" : "VIRTM"
          },
          {
            "code" : "WEST"
          },
          {
            "code" : "BLDPRD"
          },
          {
            "code" : "VCCNE"
          }
        ]
      },
      {
        "system" : "http://terminology.hl7.org/CodeSystem/v3-SpecimenType",
        "concept" : [
          {
            "code" : "ABS"
          },
          {
            "code" : "AMN"
          },
          {
            "code" : "ASP"
          },
          {
            "code" : "BBL"
          },
          {
            "code" : "BDY"
          },
          {
            "code" : "BIFL"
          },
          {
            "code" : "BLD"
          },
          {
            "code" : "BLDA"
          },
          {
            "code" : "BLDC"
          },
          {
            "code" : "BLDCO"
          },
          {
            "code" : "BLDV"
          },
          {
            "code" : "BON"
          },
          {
            "code" : "BPH"
          },
          {
            "code" : "BPU"
          },
          {
            "code" : "BRN"
          },
          {
            "code" : "BRO"
          },
          {
            "code" : "BRTH"
          },
          {
            "code" : "EXG"
          },
          {
            "code" : "CALC"
          },
          {
            "code" : "STON"
          },
          {
            "code" : "CDM"
          },
          {
            "code" : "CNJT"
          },
          {
            "code" : "CNL"
          },
          {
            "code" : "COL"
          },
          {
            "code" : "CRN"
          },
          {
            "code" : "CSF"
          },
          {
            "code" : "CTP"
          },
          {
            "code" : "CUR"
          },
          {
            "code" : "CVM"
          },
          {
            "code" : "CVX"
          },
          {
            "code" : "CYST"
          },
          {
            "code" : "DIAF"
          },
          {
            "code" : "DOSE"
          },
          {
            "code" : "DRN"
          },
          {
            "code" : "DUFL"
          },
          {
            "code" : "EAR"
          },
          {
            "code" : "EARW"
          },
          {
            "code" : "ELT"
          },
          {
            "code" : "ENDC"
          },
          {
            "code" : "ENDM"
          },
          {
            "code" : "EOS"
          },
          {
            "code" : "EYE"
          },
          {
            "code" : "FIB"
          },
          {
            "code" : "FIST"
          },
          {
            "code" : "FLT"
          },
          {
            "code" : "FLU"
          },
          {
            "code" : "FOOD"
          },
          {
            "code" : "GAS"
          },
          {
            "code" : "GAST"
          },
          {
            "code" : "GEN"
          },
          {
            "code" : "GENC"
          },
          {
            "code" : "GENF"
          },
          {
            "code" : "GENL"
          },
          {
            "code" : "GENV"
          },
          {
            "code" : "HAR"
          },
          {
            "code" : "IHG"
          },
          {
            "code" : "ISLT"
          },
          {
            "code" : "IT"
          },
          {
            "code" : "LAM"
          },
          {
            "code" : "LIQ"
          },
          {
            "code" : "LN"
          },
          {
            "code" : "LNA"
          },
          {
            "code" : "LNV"
          },
          {
            "code" : "LYM"
          },
          {
            "code" : "MAC"
          },
          {
            "code" : "MAR"
          },
          {
            "code" : "MBLD"
          },
          {
            "code" : "MEC"
          },
          {
            "code" : "MILK"
          },
          {
            "code" : "MLK"
          },
          {
            "code" : "NAIL"
          },
          {
            "code" : "NOS"
          },
          {
            "code" : "PAFL"
          },
          {
            "code" : "PAT"
          },
          {
            "code" : "PLAS"
          },
          {
            "code" : "PLB"
          },
          {
            "code" : "PLC"
          },
          {
            "code" : "PLR"
          },
          {
            "code" : "PMN"
          },
          {
            "code" : "PPP"
          },
          {
            "code" : "PRP"
          },
          {
            "code" : "PRT"
          },
          {
            "code" : "PUS"
          },
          {
            "code" : "RBC"
          },
          {
            "code" : "SAL"
          },
          {
            "code" : "SER"
          },
          {
            "code" : "SKM"
          },
          {
            "code" : "SKN"
          },
          {
            "code" : "SMN"
          },
          {
            "code" : "SMPLS"
          },
          {
            "code" : "SNV"
          },
          {
            "code" : "SPRM"
          },
          {
            "code" : "SPT"
          },
          {
            "code" : "SPTC"
          },
          {
            "code" : "SPTT"
          },
          {
            "code" : "STL"
          },
          {
            "code" : "SWT"
          },
          {
            "code" : "TEAR"
          },
          {
            "code" : "THRB"
          },
          {
            "code" : "THRT"
          },
          {
            "code" : "TISG"
          },
          {
            "code" : "TISPL"
          },
          {
            "code" : "TISS"
          },
          {
            "code" : "TISU"
          },
          {
            "code" : "TLGI"
          },
          {
            "code" : "TLNG"
          },
          {
            "code" : "TSMI"
          },
          {
            "code" : "TUB"
          },
          {
            "code" : "ULC"
          },
          {
            "code" : "UMB"
          },
          {
            "code" : "UMED"
          },
          {
            "code" : "UR"
          },
          {
            "code" : "URC"
          },
          {
            "code" : "URNS"
          },
          {
            "code" : "URT"
          },
          {
            "code" : "URTH"
          },
          {
            "code" : "USUB"
          },
          {
            "code" : "VOM"
          },
          {
            "code" : "WAT"
          },
          {
            "code" : "WBC"
          },
          {
            "code" : "WICK"
          },
          {
            "code" : "WND"
          },
          {
            "code" : "WNDA"
          },
          {
            "code" : "WNDD"
          },
          {
            "code" : "WNDE"
          }
        ]
      }
    ]
  }
}

```
