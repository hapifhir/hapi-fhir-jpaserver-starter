# matchbox-engine

matchbox-engine has been created to allow a standalone FHIR validation and transformation without the need for a connection to the internet:

- java library
- supports CDA to FHIR mapping
- FHIR validation with no external terminology server
- local configuration (no internet access necessary, ig packages can be loaded directly from classpath)
- [javadoc](/apidocs/)

default fhir package configuration:

- cda-core-2.0#2.1.0-cibuild.tgz(*patched) 
- hl7.fhir.r4.core.tgz
- hl7.fhir.xver-extensions#0.0.11.tgz
- hl7.terminology#5.0.0.tgz

