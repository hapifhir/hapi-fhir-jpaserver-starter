package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.repository.IRepository;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.fhir.cr.hapi.cdshooks.discovery.CrDiscoveryService;

public class UpdatedCrDiscoveryService extends CrDiscoveryService {
	public UpdatedCrDiscoveryService(IIdType thePlanDefinitionId, IRepository theRepository) {
		super(thePlanDefinitionId, theRepository);
		maxUriLength = 6000;
	}
}
