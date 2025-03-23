package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestJson;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsConfigService;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.hapi.cdshooks.CdsCrService;

import static org.opencds.cqf.fhir.utility.Constants.APPLY_PARAMETER_DATA;
import static org.opencds.cqf.fhir.utility.r4.Parameters.part;

public class UpdatedCdsCrService extends CdsCrService {
	public UpdatedCdsCrService(
			RequestDetails theRequestDetails, Repository theRepository, ICdsConfigService theCdsConfigService) {
		super(theRequestDetails, theRepository, theCdsConfigService);
	}

	@Override
	public Parameters encodeParams(CdsServiceRequestJson theJson) {
		var parameters = (Parameters) super.encodeParams(theJson);
		if (parameters.hasParameter(APPLY_PARAMETER_DATA)) {
			parameters.addParameter(part("useServerData", new BooleanType(false)));
		}
		return parameters;
	}
}
