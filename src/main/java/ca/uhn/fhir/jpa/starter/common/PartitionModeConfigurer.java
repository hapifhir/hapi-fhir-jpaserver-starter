package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.interceptor.PatientIdPartitionInterceptor;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.partition.PartitionManagementProvider;
import ca.uhn.fhir.jpa.searchparam.extractor.ISearchParamExtractor;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.partition.RequestTenantPartitionInterceptor;
import ca.uhn.fhir.rest.server.tenant.UrlBaseTenantIdentificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional({OnPartitionModeEnabled.class})
public class PartitionModeConfigurer {
	private static final Logger ourLog = LoggerFactory.getLogger(PartitionModeConfigurer.class);

	public PartitionModeConfigurer(
			AppProperties myAppProperties,
			ISearchParamExtractor mySearchParamExtractor,
			PartitionSettings myPartitionSettings,
			RestfulServer myRestfulServer,
			PartitionManagementProvider myPartitionManagementProvider) {

		var partitioning = myAppProperties.getPartitioning();
		if (partitioning.getPatient_id_partitioning_mode()) {
			ourLog.info("Partitioning mode enabled in: Patient ID partitioning mode");
			var patientIdInterceptor = new PatientIdPartitionInterceptor(
					myRestfulServer.getFhirContext(), mySearchParamExtractor, myPartitionSettings);
			myRestfulServer.registerInterceptor(patientIdInterceptor);
			myPartitionSettings.setUnnamedPartitionMode(true);
		} else if (partitioning.getRequest_tenant_partitioning_mode()) {
			ourLog.info("Partitioning mode enabled in: Request tenant partitioning mode");
			RequestTenantPartitionInterceptor tenantPartitionInterceptor = new RequestTenantPartitionInterceptor();
			tenantPartitionInterceptor.setPartitionSettings(myPartitionSettings);
			myRestfulServer.registerInterceptor(tenantPartitionInterceptor);
			myRestfulServer.setTenantIdentificationStrategy(new UrlBaseTenantIdentificationStrategy());
		}

		myRestfulServer.registerProviders(myPartitionManagementProvider);
	}
}
