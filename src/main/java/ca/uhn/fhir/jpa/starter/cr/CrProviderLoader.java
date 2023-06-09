package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * This class loads and registers CQL provider factory for clinical reasoning into hapi-fhir central provider factory
 **/
@Primary
@Service
public class CrProviderLoader {
	private static final Logger myLogger = LoggerFactory.getLogger(CrProviderLoader.class);
	private final FhirContext myFhirContext;
	private final ResourceProviderFactory myResourceProviderFactory;
	private final CrProviderFactory myCrProviderFactory;

	public CrProviderLoader(FhirContext theFhirContext, ResourceProviderFactory theResourceProviderFactory, CrProviderFactory theCrProviderFactory) {
		myFhirContext = theFhirContext;
		myResourceProviderFactory = theResourceProviderFactory;
		myCrProviderFactory = theCrProviderFactory;
	}

	@EventListener(ContextRefreshedEvent.class)
	public void loadProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				myLogger.info("Registering Dstu3 Cr Providers");
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getMeasureOperationsProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getActivityDefinitionOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getPlanDefinitionOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getQuestionnaireOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getQuestionnaireResponseOperationProvider());
			case R4:
				myLogger.info("Registering R4 Cr Providers");
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getMeasureOperationsProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getActivityDefinitionOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getPlanDefinitionOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getCareGapsOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getSubmitDataOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getQuestionnaireOperationProvider());
				myResourceProviderFactory.addSupplier(() -> myCrProviderFactory.getQuestionnaireResponseOperationProvider());
				break;
			default:
				throw new ConfigurationException(Msg.code(1653) + "Cr providers not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}
}