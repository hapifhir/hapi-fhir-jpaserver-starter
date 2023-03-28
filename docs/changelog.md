2023/03/28 Release 3.2.1

- docker pull \
   europe-west6-docker.pkg.dev/ahdis-ch/ahdis/matchbox:v3.2.1
- reenable proxy support for downloading packages
- update to core 5.6.116

2023/03/06 Release 3.2.0

- updated CDA core logical model 2.0 and added tests
- docker multiarchitecture support and ci-build setup [#76](https://github.com/ahdis/matchbox/issues/76)
- proxy support for downloading packages, thanks @ValentinLorand for your [PR](https://github.com/ahdis/matchbox/pull/74), [#76](https://github.com/ahdis/matchbox/issues/76)
- matchbox-server: disable caching for specific engines / implementation guides [#77](https://github.com/ahdis/matchbox/issues/77)
- update to core 5.6.100 and hapi-fhir 5.4.1 for r4 and r5 maps support [#81](https://github.com/ahdis/matchbox/issues/81)

2023/02/01 Release 3.1.0

- Reenable FHIR Mapping Language tutorial, xml and json issues with matchbox [#51](https://github.com/ahdis/matchbox/issues/51)
- Enable create and update on conformance resources [#70](https://github.com/ahdis/matchbox/issues/70), valid for 60 minutes (not persisting)
- GUI: more intuitive order for validation [#69](https://github.com/ahdis/matchbox/issues/69)
- GUI: paged ig's page does not work [#67](https://github.com/ahdis/matchbox/issues/67)
- Update to https://github.com/hapifhir/org.hl7.fhir.core/releases/tag/5.6.92 and hapi-fhir 6.2.5
- validation difference to HL7 FHIR validator [#71](https://github.com/ahdis/matchbox/issues/71): only selected ig (and dependencies) for selected canonical will be used for validation if configured on matchbox (including no dynamic loading of packages depending on meta.profile)
- spurios validation erros with package validation [#72](https://github.com/ahdis/matchbox/issues/72)
- Fixed package configuration, not loading additional ig / conformance resources [#71](https://github.com/ahdis/matchbox/issues/71)
- loading IG from package by filepath does not work [#26](https://github.com/ahdis/matchbox/issues/26)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v313

2023/01/16 Release 3.0.0

- Update to https://github.com/hapifhir/org.hl7.fhir.core/releases/tag/5.6.88
- Extracting matchbox-engine out of matchbox for validation and transformation with standalone validation engine
- CDA transformation: Updating to latest [CDA Core 2.0 logical model](cda-logical-model/index.html) with lab/pharm additions, [package](https://github.com/ahdis/cda-core-2.0/releases/download/v0.0.4-dev/cda-core-2.0.2.1.0-cibuild.tgz)
- matchbox-server for validation and transformation but not storage of FHIR resources
- cda to fhir: decimal in cda allows spaces [#62](https://github.com/ahdis/matchbox/issues/62)
- Mapping of xmlText fails [#61](https://github.com/ahdis/matchbox/issues/61)
- removing questionnaire viewer and mobile access gateway gui

2022/09/11 Release 2.4.0

- hapi-fhir 6.2.0 and org.hl7.fhir.core 5.6.43
- update mobile access
- ihe.iti.pmir#1.5.0 cannot be uploaded to matchbox [#59](https://github.com/ahdis/matchbox/issues/59): removed Subscription from resources to import
- show all cda2fhir and fhir2cda maps [#58](https://github.com/ahdis/matchbox/issues/58)
- hapi.fhir.version: 6.2.0-PRE5-SNAPSHOT and fhir.core.version 5.6.65

2022/07/11 Release 2.3.0

- favicon fixed [#53](https://github.com/ahdis/matchbox/issues/53)
- add possiblity to add a static file location [#57](https://github.com/ahdis/matchbox/issues/57)

2022/06/08 Release 2.2.0

- FHIR Mapping Language tutorial, xml and json issues [#51](https://github.com/ahdis/matchbox/issues/51)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v220

2022/05/25 Release 2.1.0

- hapi-fhir 6.0.0 and org.hl7.fhir.core 5.6.43
- Validation: CapabilityStatement caching fixed [#43](https://github.com/ahdis/matchbox/issues/43)
- prototype [SDC $assembly operation](http://hl7.org/fhir/uv/sdc/OperationDefinition-Questionnaire-assemble.html) [#46](https://github.com/ahdis/matchbox/issues/46)
- Enable SDC extraction with unknown ValueSets [#48](https://github.com/ahdis/matchbox/issues/48)
- Patch for FHIR Mapping Language: funcMemberOf/resolveValueSet: Not Implemented Yet [#49](https://github.com/ahdis/matchbox/issues/49)
- validation without terminology server and with hl7.terminology [#50](https://github.com/ahdis/matchbox/issues/50)
- base release with no ig's configured: docker pull eu.gcr.io/fhir-ch/matchbox:v210

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
- Integrated [PR](https://github.com/ahdis/matchbox/pull/25) and [PR](https://github.com/ahdis/matchbox/pull/32) for translate in Structure Map, thanks [@aralych](https://github.com/ralych)
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
