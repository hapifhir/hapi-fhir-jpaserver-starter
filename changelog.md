2022/05/xx Release 2.1.0
- Validation: CapabilityStatement caching fixed [#43](https://github.com/ahdis/matchbox/issues/43)
- prototype [SDC $assembly operation](http://hl7.org/fhir/uv/sdc/OperationDefinition-Questionnaire-assemble.html) [#46](https://github.com/ahdis/matchbox/issues/46)
- Enable SDC extraction with unknown ValueSets [#48](https://github.com/ahdis/matchbox/issues/48)

2022/04/28 Release 2.0.0
- version of ig, validator and matchbox should be provided in the validation report [#40](https://github.com/ahdis/matchbox/issues/40)
- hapi-fhir 6.0.0-PRE10-SNAPSHOT and org.hl7.fhir.core 5.6.43
- allow xml in gui for validation [#38](https://github.com/ahdis/matchbox/issues/38)
- mobile access gateway gui: prefix DocumentEntry.identifier with urn:uuid in GUI [#41](https://github.com/ahdis/matchbox/issues/41)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v200
  
2022/03/21 Release 1.9.1
- custom log banner, thanks [ralych](https://github.com/ralych)
- Fixed StructureMap transformation [issue core](https://github.com/hapifhir/org.hl7.fhir.core/issues/771) and [issue#37](https://github.com/ahdis/matchbox/issues/37)

2022/03/10 Release 1.9.0
- Updated to hap-fhir 5.7.0, fhir.core.version (validator) 5.6.27
- Extended Mobile Access Gateway support for PMP (replacing FHIR documents with selected Patient in Mobile Access Gateway, transforming to CDA and MDH publish)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v190
- docker-compose setup for postgres and for postgres and swiss igs

2022/02/21 Release 1.8.2
- OAuth integration for [Mobile Access Gateway](https://github.com/i4mi/MobileAccessGateway) in webapp
  
2022/02/21 Release 1.8.1
- Parsing of bundles adds additional contained resources [#11|(https://github.com/ahdis/matchbox/issues/11)
  
2022/02/08 Release 1.8.0
- Integrate webapp running on matchbox port and root itself [#35](https://github.com/ahdis/matchbox/issues/35)
- NPM can be downloaded with Accept:application/gzip on Implementation Guide Resource

2022/01/13 Release 1.7.1
- JSON POST Requests have a size limit (filler issue) [#33](https://github.com/ahdis/matchbox/issues/33)
- FHIRPathEnginge construction is expensive [#31](https://github.com/ahdis/matchbox/issues/31)
- SNOMED CT Code validation problem for Quantity in Medication.amount [#30](https://github.com/ahdis/matchbox/issues/30)
- Validation: Uploaded StructureDefinitions via NPM are not available in same session for $validate [#29](https://github.com/ahdis/matchbox/issues/29)
- StructureMap transformation: Bundle request element not correctly ordered [#27](https://github.com/ahdis/matchbox/issues/27)
- Error on release V1.6.0 [#24](https://github.com/ahdis/matchbox/issues/24), thanks [@delcroip](https://github.com/delcroip)
- Integrated [PR](https://github.com/ahdis/matchbox/pull/25) and [PR](https://github.com/ahdis/matchbox/pull/32)  for translate in Structure Map, thanks [@aralych](https://github.com/ralych) 
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v171

2022/01/04 Release 1.6.0
- extend FHIR API based on Implementation Guide NPM packages [#23](https://github.com/ahdis/matchbox/issues/23)
- add spring actuator for health checks [#22](https://github.com/ahdis/matchbox/issues/22)
- disable special questionnaire validation [#21](https://github.com/ahdis/matchbox/issues/21)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v160

2021/12/17 Release 1.5.0
- updated hapi-fhir to 5.6.0
- patched slicing validation problems in [bundle](https://github.com/ahdis/matchbox/issues/15)
- activated $expand operation on ValueSet
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v150

2021/09/14 Release 1.4.0
- updated hapi-fhir to 5.5.1, no more dependencies on forked packages
- $extract on QuestionnaireResponse for StructureMap based extraction
- support for the $transform operation for StructureMap
- FHIR Mapping Language Support (POST FHIR Mapping language, transform)
- fixed issues #7 and #8 (custom SearchParmeters and validation)
- public test instance https://test.ahdis.ch/matchbox/fhir
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v140
- swiss epr release: docker pull eu.gcr.io/fhir-ch/matchbox-swissepr:v140

2021/07/05 Release 1.3.0
- updated hapi-fhir to 5.5.0-PRE5-SNAPSHOT with patches for hapi-fhir and org.hl7.fhir.core (dev branch on ahdis foreach project)
- updated swiss epr implementation guides to STU2 Ballot
- renamed project to matchbox-validator
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox-validator:v130
- swiss epr release: docker pull eu.gcr.io/fhir-ch/matchbox-validator-swissepr:v130
- testsystem endpoint for siwssepr validator: https://test.ahdis.ch/matchbox-validator/fhir

2020/12/23 Release 1.2.0
- updated hapi-fhir to 5.2.0
- updated ch-epr-mhealth to 0.1.2
- Release is available here:
docker pull eu.gcr.io/fhir-ch/hapi-fhir-jpavalidator:v120

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