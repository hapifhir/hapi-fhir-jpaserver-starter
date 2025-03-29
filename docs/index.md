# Welcome

[Matchbox](https://www.matchbox.health/) makes it easier to validate, transform, and exchange healthcare information. At its core, Matchbox helps ensure that health data follows the [FHIR®](https://www.hl7.org/fhir/) standard. It is an open-source solution to support testing and implementation of FHIR-based solutions and to map or capture health data into [HL7® FHIR®](https://www.hl7.org/fhir/) with the FHIR mapping language. Matchbox is used at [IHE Connectathon](https://www.ihe.net/testing/connectathon/) integrated in the [Gazelle tooling](https://ihe-catalyst.net/test-system-gazelle/) for validating client messages against FHIR based implementation guides. Matchbox is also used in production for validating messages ensuring that not call outs to the internet are made.

Healthcare organizations can use Matchbox in two ways: either as a standalone service through a docker container that runs independently in their IT infrastructure, or as a java software library that can be integrated into their existing applications. 

Matchbox is developed by [ahdis](https://www.ahdis.ch/en/home) and is freely available under the business-friendly [Apache Software License 2.0](https://github.com/ahdis/matchbox/blob/main/LICENSE). It is itself based on [HAPI FHIR](https://hapifhir.io/) and the official [HL7 FHIR validator](https://confluence.hl7.org/spaces/FHIR/pages/35718580/Using+the+FHIR+Validator). 

Ahdis offers a [public test server](https://test.ahdis.ch/matchbox/) for organizations to try out its features.

## Changelog

[Changelog](changelog.md)

## Main Features

- [Validation of FHIR implementations](features.md/#validation-of-fhir-implementations)
- [Mapping health data to and from FHIR using the FHIR mapping language](features.md/#mapping-health-data-to-and-from-fhir-using-the-fhir-mapping-language)
