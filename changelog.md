2020/10/22 Release 1.1.0
- updated hapi-fhir to (21.10.2020) and spring-boot
- updated fhir.core.version 5.1.15, later is not yet possible due to class name changes
- [fixed EHS-439](https://github.com/ahdis/hapi-fhir-jpaserver-validator/issues/2) added testcase for EHS-439 to verify correct behaviour with fhir.core.version 5.1.15 https://github.com/hapifhir/org.hl7.fhir.core/releases/tag/5.1.15
- [fixed Parameters evaluation](https://github.com/ahdis/hapi-fhir-jpaserver-validator/issues/1) two different versions for calling the $validate operation: with Parameters resource and containing the resource to validate within as additional name "resource" parameter
with Resource to validate directly according to [7.5.5 Asking a FHIR Server](https://www.hl7.org/fhir/validation.html#op)
- [fixed EHS-431](https://gazelle.ihe.net/jira/browse/EHS-431) Validator crashes and does not give a result if the JSON starts with a [ ] (square bracket).
- [fixed EHS-419](https://gazelle.ihe.net/jira/browse/EHS-419) warning instead of crash for Byte order mark in validation request
- changed docker build: ig's will be installed during docker build process, no connection to the internet is needed for validation
- [Validation Test Suite for all examples in the loaded ig's](https://github.com/ahdis/hapi-fhir-jpaserver-validator/blob/ig/src/test/java/ch/ahdis/validation/IgValidateR4Test.java) checking that they can be validated with no errors with the $validate operation
- [Validation Test Suite with hapi-fhir-client for individual examples](https://github.com/ahdis/hapi-fhir-jpaserver-validator/blob/ig/src/test/java/ch/ahdis/validation/IgValidateRawProfileTest.java)
- [Experimental: Validation Test Suite based on on](https://github.com/ahdis/hapi-fhir-jpaserver-validator/blob/ig/src/test/java/ch/ahdis/validation/CoreValidationTests.java) [fhir-testcases](https://github.com/FHIR/fhir-test-cases/tree/master/validator) (only R4, no test-cases from ig's, valuesets or with profiles yet)

2020/09/02 Release 1.0.0
- system level $validate operation for $ig's
- based on hapi-fhir-jpaserverstarter 5.1.0