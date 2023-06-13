package ca.uhn.fhir.jpa.starter.cr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;

public class CrOperationProviderLoader {
	private static final Logger myLogger = LoggerFactory.getLogger(CrOperationProviderLoader.class);
	private final FhirContext myFhirContext;
	private final ResourceProviderFactory myResourceProviderFactory;
	private final CrOperationProviderFactory myCrProviderFactory;

	public CrOperationProviderLoader(FhirContext theFhirContext, ResourceProviderFactory theResourceProviderFactory,
			CrOperationProviderFactory theCrProviderFactory) {
		myFhirContext = theFhirContext;
		myResourceProviderFactory = theResourceProviderFactory;
		myCrProviderFactory = theCrProviderFactory;
		loadProvider();
	}

	public void loadProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				myLogger.info("Registering DSTU3 Clinical Reasoning Providers");
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getMeasureOperationsProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getActivityDefinitionProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getPlanDefinitionProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getQuestionnaireResponseOperationProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getQuestionnaireOperationProvider);
				break;
			case R4:
				myLogger.info("Registering R4 Clinical Reasoning Providers");
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getMeasureOperationsProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getActivityDefinitionProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getPlanDefinitionProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getCareGapsProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getSubmitDataProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getQuestionnaireResponseOperationProvider);
				myResourceProviderFactory.addSupplier(myCrProviderFactory::getQuestionnaireOperationProvider);
				break;
			default:
				throw new ConfigurationException("Clinical Reasoning not supported for FHIR version "
						+ myFhirContext.getVersion().getVersion());
		}
	}
}
