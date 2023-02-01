# CDA Logical Model

To convert between CDA and FHIR an Logical Model is needed which can represent the CDA standard within FHIR. This work has been undertaken by [HL7 International](https://www.hl7.org/) and is in current development ([source](https://build.fhir.org/ig/HL7/CDA-core-2.0/), [ci-build](https://build.fhir.org/ig/HL7/CDA-core-2.0/)). ahdis has forked the CDA Logical Model and has made the following changes in the [main branch](https://github.com/ahdis/cda-core-2.0/tree/main):


* Support for IHE PHARM (PharmContent, PharmIngredient, PharmMedicineClass, PharmPackagedMedicine, PharmSpecializedKind, PharmSubstance, PharmSuperContent) and IHE LAB ([LabCriterion](cda-logical-model/StructureDefinition-LabCriterion.html), [LabCondition](cda-logical-model/StructureDefinition-LabPrecondition.html)) extensions
* Support for Patient.birthTime

The logical model has been deployed at [https://ahdis.github.io/matchbox/cda-logical-model/index.html](https://ahdis.github.io/matchbox/cda-logical-model/index.html), the package is available at [https://ahdis.github.io/matchbox/cda-logical-model/package.tgz](https://ahdis.github.io/matchbox/cda-logical-model/package.tgz)

Note: The CDA-Model is currently built as an FHIR R5 model.