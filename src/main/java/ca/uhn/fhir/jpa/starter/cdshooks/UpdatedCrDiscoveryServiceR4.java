package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.hapi.fhir.cdshooks.svc.cr.discovery.CrDiscoveryServiceR4;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.fhir.api.Repository;

public class UpdatedCrDiscoveryServiceR4 extends CrDiscoveryServiceR4 {
	public UpdatedCrDiscoveryServiceR4(IIdType thePlanDefinitionId, Repository theRepository) {
		super(thePlanDefinitionId, theRepository);
		myMaxUriLength = 6000;
	}
}
