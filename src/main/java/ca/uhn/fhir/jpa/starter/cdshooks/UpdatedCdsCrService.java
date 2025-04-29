package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestJson;
import ca.uhn.hapi.fhir.cdshooks.api.ICdsConfigService;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.hapi.cdshooks.CdsCrService;
import org.opencds.cqf.fhir.utility.adapter.IAdapterFactory;

import static org.opencds.cqf.fhir.utility.Constants.APPLY_PARAMETER_DATA;

public class UpdatedCdsCrService extends CdsCrService {
	private final IAdapterFactory adapterFactory;

	public UpdatedCdsCrService(
			RequestDetails theRequestDetails, Repository theRepository, ICdsConfigService theCdsConfigService) {
		super(theRequestDetails, theRepository, theCdsConfigService);
		adapterFactory = IAdapterFactory.forFhirContext(theRepository.fhirContext());
	}

	@Override
	public IBaseParameters encodeParams(CdsServiceRequestJson theJson) {
		var parameters = adapterFactory.createParameters(super.encodeParams(theJson));
		if (parameters.hasParameter(APPLY_PARAMETER_DATA)) {
			parameters.addParameter(
					"useServerData",
					booleanTypeForVersion(parameters.fhirContext().getVersion().getVersion(), false));
		}
		return (IBaseParameters) parameters.get();
	}

	private IPrimitiveType<Boolean> booleanTypeForVersion(FhirVersionEnum fhirVersion, boolean value) {
		return switch (fhirVersion) {
			case DSTU2 -> new org.hl7.fhir.dstu2.model.BooleanType(value);
			case DSTU3 -> new org.hl7.fhir.dstu3.model.BooleanType(value);
			case R4 -> new org.hl7.fhir.r4.model.BooleanType(value);
			case R5 -> new org.hl7.fhir.r5.model.BooleanType(value);
			default -> throw new IllegalArgumentException("unknown or unsupported FHIR version");
		};
	}
}
