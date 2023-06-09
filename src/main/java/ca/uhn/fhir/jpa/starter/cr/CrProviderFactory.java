package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.dstu3.measure.MeasureOperationsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This class represents clinical reasoning provider factory used for loading cql and measure operation dependencies of various fhir models
 **/
@Primary
@Service
public class CrProviderFactory {
	@Autowired
	private FhirContext myFhirContext;

	@Autowired
	private ApplicationContext myApplicationContext;

	public Object getMeasureOperationsProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(MeasureOperationsProvider.class);
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.measure.MeasureOperationsProvider.class);
			default:
				throw new ConfigurationException("EvaluateMeasure is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}
	public Object getActivityDefinitionOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.dstu3.activitydefinition.ActivityDefinitionOperationsProvider.class);
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.activitydefinition.ActivityDefinitionOperationsProvider.class);
			default:
				throw new ConfigurationException("ActivityDefinition is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}
	public Object getPlanDefinitionOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.dstu3.plandefinition.PlanDefinitionOperationsProvider.class);
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.plandefinition.PlanDefinitionOperationsProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}

	public Object getCareGapsOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.measure.CareGapsOperationProvider.class);
			default:
				throw new ConfigurationException("Caregaps is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}

	public Object getSubmitDataOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.measure.SubmitDataProvider.class);
			default:
				throw new ConfigurationException("SubmitData is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}

	public Object getQuestionnaireOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.dstu3.questionnaire.QuestionnaireOperationsProvider.class);
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.questionnaire.QuestionnaireOperationsProvider.class);
			default:
				throw new ConfigurationException("Questionnaire is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}
	public Object getQuestionnaireResponseOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.dstu3.questionnaireresponse.QuestionnaireResponseOperationsProvider.class);
			case R4:
				return myApplicationContext.getBean(ca.uhn.fhir.cr.r4.questionnaireresponse.QuestionnaireResponseOperationsProvider.class);
			default:
				throw new ConfigurationException("Questionnaire Response is not supported for FHIR version " + myFhirContext.getVersion().getVersion());
		}
	}
}

