Bundle-DocumentRadiologyOrder:

Errors:

```json
      "extension": [
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line",
          "valueInteger": 2482
        }
      ],
      "severity": "error",
      "code": "processing",
      "diagnostics": "The value provided (http://snomed.info/sct::441509002) is not in the options value set in the questionnaire",
      "location": [
        "Bundle.entry[2].resource.ofType(QuestionnaireResponse).item[0].item[0].answer[0]",
        "Line 2482, Col 58"
      ]
```


```

Definition Questionnaire:

```json
       {
          "linkId" : "caveat.device",
          "definition" : "http://fhir.ch/ig/ch-rad-order/StructureDefinition/ch-rad-order-caveat-condition#Condition.code",
          "text" : "Device (Herzschrittmacher, Herzklappenersatz, Insulinpumpe etc.)",
          "type" : "choice",
          "repeats" : true,
          "answerValueSet" : "http://fhir.ch/ig/ch-rad-order/ValueSet/ch-rad-order-caveat-device"
        },
```
remove in the questionnaire response: caveat.device
___



    {
      "extension": [
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line",
          "valueInteger": 3682
        }
      ],
      "severity": "error",
      "code": "processing",
      "diagnostics": "None of the codings provided are in the value set http://fhir.ch/ig/ch-rad-order/ValueSet/ch-rad-order-caveat-condition (http://fhir.ch/ig/ch-rad-order/ValueSet/ch-rad-order-caveat-condition), and a coding from this value set is required) (codes = http://snomed.info/sct#372567009)",
      "location": [
        "Bundle.entry[28].resource.code",
        "Line 3682, Col 22"
      ]

    {
      "extension": [
        {
          "url": "http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line",
          "valueInteger": 87
        }
      ],
      "severity": "error",
      "code": "processing",
      "diagnostics": "Composition.section:orderReferral.entry:QuestionnaireResponse: minimum required = 1, but only found 0 (from http://fhir.ch/ig/ch-rad-order/StructureDefinition/ch-rad-order-composition)",
      "location": [
        "Bundle.entry[0].resource.section[0]",
        "Line 87, Col 12"
      ]
    },