package ca.uhn.fhir.jpa.starter.cdshooks;

import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.hapi.cdshooks.discovery.CrDiscoveryService;

public class UpdatedCrDiscoveryService extends CrDiscoveryService {
	public UpdatedCrDiscoveryService(IIdType thePlanDefinitionId, Repository theRepository) {
		super(thePlanDefinitionId, theRepository);
		maxUriLength = 6000;
	}
}
