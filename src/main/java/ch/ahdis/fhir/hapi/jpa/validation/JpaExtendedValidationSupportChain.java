package ch.ahdis.fhir.hapi.jpa.validation;

import javax.annotation.PostConstruct;

import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.packages.NpmJpaValidationSupport;
import ca.uhn.fhir.jpa.term.api.ITermConceptMappingSvc;
import ca.uhn.fhir.jpa.term.api.ITermReadSvc;
import ca.uhn.fhir.jpa.validation.JpaValidationSupportChain;

public class JpaExtendedValidationSupportChain extends JpaValidationSupportChain {

//addValidationSupport(myUnknownCodeSystemWarningValidationSupport);
	private final FhirContext myFhirContext;

	@Autowired
	@Qualifier("myJpaValidationSupport")
	public IValidationSupport myJpaValidationSupport;

	@Qualifier("myDefaultProfileValidationSupport")
	@Autowired
	private IValidationSupport myDefaultProfileValidationSupport;
	@Autowired
	private ITermReadSvc myTerminologyService;
	//@Autowired
	//private NpmJpaValidationSupport myNpmJpaValidationSupport;
	@Autowired
	private ITermConceptMappingSvc myConceptMappingSvc;
	@Autowired
	private UnknownCodeSystemWarningValidationSupport myUnknownCodeSystemWarningValidationSupport;
	
	private SnapshotGeneratingValidationSupport snapshotGeneratingValidationSupport;
	private ExtInMemoryTerminologyServerValidationSupport extInMemoryTerminologyServerValidationSupport;
	private CommonCodeSystemsTerminologyService commonCodeSystemsTerminologyService;

	public JpaExtendedValidationSupportChain(FhirContext theFhirContext) {
		super(theFhirContext);
		this.myFhirContext = theFhirContext; 
	}

	@PostConstruct
	public void postConstruct() {
//	Original JpaValidationSupportChain
//		super.postConstruct();	
//		addValidationSupport(myDefaultProfileValidationSupport);
//		addValidationSupport(myJpaValidationSupport);
//		//TODO MAKE SURE THAT THIS IS BEING CAL
//		addValidationSupport(myTerminologyService);
//		addValidationSupport(new SnapshotGeneratingValidationSupport(myFhirContext));
//		addValidationSupport(new InMemoryTerminologyServerValidationSupport(myFhirContext));
//		addValidationSupport(myNpmJpaValidationSupport);
//		addValidationSupport(new CommonCodeSystemsTerminologyService(myFhirContext));
//		addValidationSupport(myConceptMappingSvc);
		
		//TODO MAKE SURE THAT THIS IS BEING CAL
		addValidationSupport(myTerminologyService);
		snapshotGeneratingValidationSupport = new SnapshotGeneratingValidationSupport(myFhirContext);
		addValidationSupport(snapshotGeneratingValidationSupport);
		extInMemoryTerminologyServerValidationSupport = new ExtInMemoryTerminologyServerValidationSupport(myFhirContext);
		addValidationSupport(extInMemoryTerminologyServerValidationSupport);
		//addValidationSupport(myNpmJpaValidationSupport);
		commonCodeSystemsTerminologyService = new CommonCodeSystemsTerminologyService(myFhirContext);
		addValidationSupport(commonCodeSystemsTerminologyService);
		addValidationSupport(myConceptMappingSvc);
		addValidationSupport(myJpaValidationSupport);
		// matchbox 2.1.0 we moved DefaultValidaton Support down below, because it looks like myDefaultProfileValidationSupport contains a complete DICOM CodeSystem and
		// we want the one from hl7.terminology 3.1.0 (check http://localhost:8080/matchbox/fhir/CodeSystem?url=http://dicom.nema.org/resources/ontology/DCM)
		// https://github.com/ahdis/matchbox/issues/50
    addValidationSupport(myDefaultProfileValidationSupport);
	}
	
		
}
