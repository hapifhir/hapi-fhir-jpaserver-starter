package ch.ahdis.matchbox.terminology.providers;

import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.*;

import java.util.Date;
import java.util.List;

/**
 * The CapabilityStatement provider for the Matchbox terminology server.
 */
public class CapabilityStatementProvider extends ServerCapabilityStatementProvider {

	public CapabilityStatementProvider(final RestfulServer theServer) {
		super(theServer);
	}

	@Override
	public IBaseConformance getServerConformance(final HttpServletRequest theRequest,
																final RequestDetails theRequestDetails) {
		final CapabilityStatement cs = (CapabilityStatement) super.getServerConformance(theRequest, theRequestDetails);

		cs.setDate(new Date());
		cs.setDescription("Matchbox Terminology Server");
		cs.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		cs.addFormat("application/fhir+json");
		cs.addFormat("application/fhir+xml");
		cs.setInstantiates(List.of(
			new CanonicalType("http://hl7.org/fhir/CapabilityStatement/terminology-server")
		));
		cs.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
		cs.setName("MatchboxTerminologyServer");
		cs.setPublisher("ahdis");
		cs.setSoftware(new CapabilityStatement.CapabilityStatementSoftwareComponent()
								.setName(VersionUtil.getPoweredBy())
								.setVersion(VersionUtil.getVersion()));
		cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
		cs.setTitle("Matchbox Terminology Server Capability Statement");
		cs.setUrl("");
		cs.setVersion(VersionUtil.getVersion());

		var feature = cs.addExtension().setUrl("http://hl7.org/fhir/uv/application-feature/StructureDefinition/feature");
		feature.addExtension("definition", new CanonicalType("http://hl7.org/fhir/uv/tx-tests/FeatureDefinition/test-version"));
		feature.addExtension("value", new CodeType("1.7.8"));

		feature = cs.addExtension().setUrl("http://hl7.org/fhir/uv/application-feature/StructureDefinition/feature");
		feature.addExtension("definition", new CanonicalType("http://hl7.org/fhir/uv/tx-ecosystem/FeatureDefinition/CodeSystemAsParameter"));
		feature.addExtension("value", new BooleanType("true"));

		return cs;
	}
}
