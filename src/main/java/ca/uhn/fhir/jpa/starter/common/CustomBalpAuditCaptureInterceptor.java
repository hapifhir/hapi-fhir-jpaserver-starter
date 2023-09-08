package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.storage.interceptor.balp.BalpAuditCaptureInterceptor;
import ca.uhn.fhir.storage.interceptor.balp.IBalpAuditContextServices;
import ca.uhn.fhir.storage.interceptor.balp.IBalpAuditEventSink;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.jetbrains.annotations.NotNull;

public class CustomBalpAuditCaptureInterceptor extends BalpAuditCaptureInterceptor {
	public CustomBalpAuditCaptureInterceptor(@NotNull IBalpAuditEventSink theAuditEventSink, @NotNull IBalpAuditContextServices theContextServices) {
		super(theAuditEventSink, theContextServices);
	}

	@Override
	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_CREATED)
	public void hookStoragePrecommitResourceCreated(IBaseResource theResource, ServletRequestDetails theRequestDetails) {
		if (theResource.getClass().getName().contains("AuditEvent")) {
			return;
		}

		super.hookStoragePrecommitResourceCreated(theResource, theRequestDetails);
	}

	@Override
	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_DELETED)
	public void hookStoragePrecommitResourceDeleted(IBaseResource theResource, ServletRequestDetails theRequestDetails) {
		if (theResource.getClass().getName().contains("AuditEvent")) {
			return;
		}

		super.hookStoragePrecommitResourceDeleted(theResource, theRequestDetails);
	}

}
