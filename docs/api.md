# API for Matchbox

[Matchbox](https://matchbox.health) is a FHIR server based on the [hapifhir/hapi-fhir-jpaserver-starter](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) 

- (pre-)load FHIR implementation guides from the package server for conformance resources (StructureMap, Questionnaire, CodeSystem, ValueSet, ConceptMap, NamingSystem, StructureDefinition). The "with-preload" subfolder contains an example with the implementation guides provided for the [public test server](https://test.ahdis.ch/matchbox/fhir).
- validation support: [server]/$validate for checking FHIR resources conforming to the loaded implementation guides
- FHIR Mapping Language endpoints for creation of StructureMaps and support for the [StructureMap/$transform](https://www.hl7.org/fhir/operation-structuremap-transform.html) operation
- SDC (Structured Data Capture) [extraction](https://build.fhir.org/ig/HL7/sdc/extraction.html#map-extract) support based on the FHIR Mapping language and [Questionnaire/$extract](http://build.fhir.org/ig/HL7/sdc/OperationDefinition-QuestionnaireResponse-extract.html)

a public test server is hosted at [https://test.ahdis.ch/matchbox/fhir](https://test.ahdis.ch/matchbox/fhir) with a corresponding gui [https://test.ahdis.ch/matchbox/](https://test.ahdis.ch/matchbox/#)