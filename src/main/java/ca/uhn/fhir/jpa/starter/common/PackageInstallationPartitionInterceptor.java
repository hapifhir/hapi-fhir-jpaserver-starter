package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;

/**
 * This interceptor provides a default partition for system-level package installation operations
 * when partitioning is enabled. This prevents the "HAPI-1315: System call is attempting to write
 * a non-partitionable resource to a partition!" error during package installation.
 */
@Interceptor
public class PackageInstallationPartitionInterceptor {

	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_CREATE)
	public RequestPartitionId identifyPartitionForCreate(RequestDetails theRequestDetails) {
		// For system requests without a partition, use the default partition
		if (theRequestDetails instanceof SystemRequestDetails) {
			SystemRequestDetails systemRequestDetails = (SystemRequestDetails) theRequestDetails;
			RequestPartitionId existingPartition = systemRequestDetails.getRequestPartitionId();
			if (existingPartition == null) {
				return RequestPartitionId.defaultPartition();
			}
		}
		return null;
	}

	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_READ)
	public RequestPartitionId identifyPartitionForRead(RequestDetails theRequestDetails) {
		// For system requests without a partition, use the default partition
		if (theRequestDetails instanceof SystemRequestDetails) {
			SystemRequestDetails systemRequestDetails = (SystemRequestDetails) theRequestDetails;
			RequestPartitionId existingPartition = systemRequestDetails.getRequestPartitionId();
			if (existingPartition == null) {
				return RequestPartitionId.defaultPartition();
			}
		}
		return null;
	}
}
