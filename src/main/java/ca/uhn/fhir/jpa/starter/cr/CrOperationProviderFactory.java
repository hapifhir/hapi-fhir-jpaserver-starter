package ca.uhn.fhir.jpa.starter.cr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.cr.r4.measure.CareGapsOperationProvider;
import ca.uhn.fhir.cr.r4.measure.SubmitDataProvider;
import ca.uhn.fhir.cr.dstu3.activitydefinition.ActivityDefinitionOperationsProvider;
import ca.uhn.fhir.cr.dstu3.measure.MeasureOperationsProvider;
import ca.uhn.fhir.cr.dstu3.plandefinition.PlanDefinitionOperationsProvider;
import ca.uhn.fhir.cr.dstu3.questionnaire.QuestionnaireOperationsProvider;
import ca.uhn.fhir.cr.dstu3.questionnaireresponse.QuestionnaireResponseOperationsProvider;
import org.springframework.stereotype.Service;

@Service
public class CrOperationProviderFactory {
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
				throw new ConfigurationException("Measure operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}

	public Object getActivityDefinitionProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(ActivityDefinitionOperationsProvider.class);
			case R4:
				return myApplicationContext
					.getBean(ca.uhn.fhir.cr.r4.activitydefinition.ActivityDefinitionOperationsProvider.class);
			default:
				throw new ConfigurationException("ActivityDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}

	public Object getPlanDefinitionProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(PlanDefinitionOperationsProvider.class);
			case R4:
				return myApplicationContext
					.getBean(ca.uhn.fhir.cr.r4.plandefinition.PlanDefinitionOperationsProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}

	public Object getCareGapsProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case R4:
				return myApplicationContext.getBean(CareGapsOperationProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}
	public Object getSubmitDataProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case R4:
				return myApplicationContext.getBean(SubmitDataProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}
	public Object getQuestionnaireResponseOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(QuestionnaireResponseOperationsProvider.class);
			case R4:
				return myApplicationContext
					.getBean(ca.uhn.fhir.cr.r4.questionnaireresponse.QuestionnaireResponseOperationsProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}
	public Object getQuestionnaireOperationProvider() {
		switch (myFhirContext.getVersion().getVersion()) {
			case DSTU3:
				return myApplicationContext.getBean(QuestionnaireOperationsProvider.class);
			case R4:
				return myApplicationContext
					.getBean(ca.uhn.fhir.cr.r4.questionnaire.QuestionnaireOperationsProvider.class);
			default:
				throw new ConfigurationException("PlanDefinition operations are not supported for FHIR version "
					+ myFhirContext.getVersion().getVersion());
		}
	}
}