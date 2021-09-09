package ch.ahdis.fhir.hapi.jpa.validation;

import javax.annotation.PostConstruct;

import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.RemoteTerminologyServiceValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.UnknownCodeSystemWarningValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
	@Autowired
	private NpmJpaValidationSupport myNpmJpaValidationSupport;
	@Autowired
	private ITermConceptMappingSvc myConceptMappingSvc;
	@Autowired
	private UnknownCodeSystemWarningValidationSupport myUnknownCodeSystemWarningValidationSupport;

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
		addValidationSupport(myDefaultProfileValidationSupport);
		addValidationSupport(myJpaValidationSupport);
		//TODO MAKE SURE THAT THIS IS BEING CAL
		addValidationSupport(myTerminologyService);
		addValidationSupport(new SnapshotGeneratingValidationSupport(myFhirContext));
		addValidationSupport(new ExtInMemoryTerminologyServerValidationSupport(myFhirContext));
		addValidationSupport(myNpmJpaValidationSupport);
		addValidationSupport(new CommonCodeSystemsTerminologyService(myFhirContext));
		addValidationSupport(myConceptMappingSvc);
	}
}
