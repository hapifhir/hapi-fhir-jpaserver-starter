## Validation of FHIR implementations

Need to test your FHIR implementation for correctness? FHIR [implementation guides](https://hl7.org/fhir/implementationguide.html) have different requirements on your implementation. From country-specific adaptations (e.g. [Swiss FHIR specifications](http://fhir.ch)), to requirements for specific use cases like the [International Patient Summary (IPS)](http://hl7.org/fhir/uv/ips/), or your own organization’s internal FHIR implementation guide. You can configure Matchbox to meet your requirements, and validate your FHIR resources directly with an API during testing or in production. Validation is based on the [HL7 Java reference validator](https://github.com/hapifhir/org.hl7.fhir.core) in accordance with the provided terminologies. An external or internal terminology server can also be configured, and you can validate your implementation through the FHIR API or through a simple GUI. Matchbox can also be integrated with [EVS Client](https://gazelle.ihe.net/EVSClient/home.seam), the validation tool from [IHE](https://www.ihe.net) that is used during Connectathons.

Customized matchbox containers can be created with the following features for integration or production validation:

- configure your container directly with the ig's you validate against, no callout to an external package server is necessary
- ig's can be preloaded and cached in memory to ensure fast validation if the container is ready
- use the internal terminology server of matchbox to be independent of the availability of an external terminology server or configure an own terminology server
- the [HL7 Java reference validator](https://github.com/hapifhir/org.hl7.fhir.core) version is fixed per matchbox version and specified in the results
- warnings and information outcomes can be configured to be ignored

## Mapping health data to and from FHIR using the FHIR mapping language

Need to map your health data into FHIR and want to share your mapping to FHIR in a re-usable way? The [FHIR mapping language](https://www.hl7.org/fhir/mapping-language.html) allows you to define mapping in a text representation and transform them to FHIR [StructureMap](https://www.hl7.org/fhir/structuremap.html) resources. Those resources can then be provided in FHIR implementation guides. Matchbox applies the mapping to your own data to create FHIR-compatible data sets. It also checks that the mapping conforms with the included validation stack. In Switzerland, this approach is tested with mapping between CDA and FHIR exchange formats for medication ([CDA-FHIR-Maps](http://fhir.ch/ig/cda-fhir-maps/index.html)).

## Structured Data Capture (SDC) Questionnaire support for data extraction with the FHIR Mapping Language

SDC (Structured Data Capture) [extraction](https://build.fhir.org/ig/HL7/sdc/extraction.html#map-extract) support based on the FHIR Mapping language and preliminary support for assemble of modular questionnaires.
