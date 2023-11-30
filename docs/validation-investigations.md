# Investigation of the FHIR validator

## Setup

To build matchbox and matchbox-ch-elm:
```bash
cd matchbox
mvn --batch-mode --update-snapshots package -DskipTests -P release
docker build --tag "matchbox:3.4.5.1" --platform=linux/amd64 -f matchbox-server/Dockerfile matchbox-server
cd ../matchbox-ch-elm
docker build --tag "matchbox-ch-elm:3.4.5.1" --platform=linux/amd64 -f ./Dockerfile .
```

matchbox-ch-elm is configured with `txServer: http://host.docker.internal:18002/eprik-cara/camel/tx/r4/`.

## Start up

At startup, matchbox makes two HTTP requests to the terminology server:
```http
GET http://host.docker.internal:18002/eprik-cara/camel/tx/r4/metadata?_summary=true HTTP/1.1
```
which returns a CapabilityStatement that contains supported operations; and:

```http
GET http://host.docker.internal:18002/eprik-cara/camel/tx/r4/metadata?mode=terminology HTTP/1.1
```
which returns a TerminologyCapabilities that seems to contain the list of supported code systems, and expansion parameters.

The responsible is `SimpleWorkerContext.connectToTSServer()`, both resources are put in the `txCache`.

## During validation

During validation, the library makes a number of requests to the terminology server.
The first type is to check the membership of a code in a value set:

```http
POST http://host.docker.internal:18002/eprik-cara/camel/tx/r4/ValueSet/$validate-code? HTTP/1.1
```
```json
{
  "resourceType": "Parameters",
  "parameter": [
    {
      "name": "coding",
      "valueCoding": {
        "system": "http://loinc.org",
        "code": "697-3"
      }
    },
    {
      "name": "valueSetMode",
      "valueString": "CHECK_MEMERSHIP_ONLY"
    },
    {
      "name": "default-to-latest-version",
      "valueBoolean": true
    },
    {
      "name": "valueSet",
      "resource": {
        "resourceType": "ValueSet",
        "url": "http://hl7.org/fhir/uv/ips/ValueSet/results-laboratory-observations-uv-ips--0",
        "version": "current",
        "status": "active",
        "compose": {
          "include": [
            {
              "system": "http://loinc.org",
              "filter": [
                {
                  "property": "STATUS",
                  "op": "=",
                  "value": "ACTIVE"
                },
                {
                  "property": "CLASSTYPE",
                  "op": "=",
                  "value": "1"
                }
              ]
            }
          ]
        }
      }
    },
    {
      "name": "cache-id",
      "valueId": "5a375c37-d208-4381-b8f7-409926dcf760"
    },
    {
      "name": "profile",
      "resource": {
        "resourceType": "Parameters",
        "parameter": [
          {
            "name": "profile-url",
            "valueString": "http://hl7.org/fhir/ExpansionProfile/dc8fd4bc-091a-424a-8a3b-6198ef146891"
          }
        ]
      }
    },
    {
      "name": "mode",
      "valueString": "lenient-display-validation"
    }
  ]
}
```
`valueSetMode` is sometimes set to `NO_MEMBERSHIP_CHECK`, other times to `CHECK_MEMERSHIP_ONLY`. It is linked to 
enum `ValidationOptions.ValueSetMode`: `ALL_CHECKS, CHECK_MEMERSHIP_ONLY, NO_MEMBERSHIP_CHECK`.

---

The request contains both the code to be checked, and the value set to check it against, as well as some parameters.

The second type is to check the validity of a code in a code system:

```http
POST http://host.docker.internal:18002/eprik-cara/camel/tx/r4/CodeSystem/$validate-code? HTTP/1.1
```
```json
{
  "resourceType": "Parameters",
  "parameter": [
    {
      "name": "coding",
      "valueCoding": {
        "system": "http://loinc.org",
        "code": "697-3"
      }
    },
    {
      "name": "default-to-latest-version",
      "valueBoolean": true
    },
    {
      "name": "cache-id",
      "valueId": "5a375c37-d208-4381-b8f7-409926dcf760"
    },
    {
      "name": "profile",
      "resource": {
        "resourceType": "Parameters",
        "parameter": [
          {
            "name": "profile-url",
            "valueString": "http://hl7.org/fhir/ExpansionProfile/dc8fd4bc-091a-424a-8a3b-6198ef146891"
          }
        ]
      }
    },
    {
      "name": "mode",
      "valueString": "lenient-display-validation"
    }
  ]
}
```

The responsible code for these parts is `BaseWorkerContext.validateOnServer()`.

---

A cache is also implemented with the parameter `cache-id`: within the same cache, the TX client does not need to 
send a resource (ValueSet) again if it has already been sent.

E.g.
```http
POST http://localhost:18002/eprik-cara/camel/tx/r4/ValueSet/$validate-code? HTTP/1.1
```
```json
{
  "resourceType": "Parameters",
  "parameter": [
    {
      "name": "coding",
      "valueCoding": {
        "system": "http://loinc.org",
        "code": "697-3"
      }
    },
    {
      "name": "default-to-latest-version",
      "valueBoolean": true
    },
    {
      "name": "url",
      "valueUri": "http://fhir.ch/ig/ch-elm/ValueSet/ch-elm-expecting-specimen-specification--0|1.0.0"
    },
    {
      "name": "cache-id",
      "valueId": "a5ca9e93-2834-485b-9640-378503755211"
    },
    {
      "name": "profile",
      "resource": {
        "resourceType": "Parameters",
        "parameter": [
          {
            "name": "profile-url",
            "valueString": "http://hl7.org/fhir/ExpansionProfile/dc8fd4bc-091a-424a-8a3b-6198ef146891"
          }
        ]
      }
    }
  ]
}
```
The parameter `url` points to a ValueSet that has been sent in a previous request, with the same `cache-id`.


## Caching

Code validation results are cached in a `TerminologyCache`. It can be disabled by putting
`this.getContext().setCachingAllowed(false);` in the `MatchboxEngine` constructor.
