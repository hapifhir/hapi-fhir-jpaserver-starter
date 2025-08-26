package ch.ahdis.matchbox.terminology.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r5.model.*;

import java.util.Date;
import java.util.List;

/**
 * The CapabilityStatement provider for the Matchbox terminology server.
 */
public class CapabilityStatementProvider extends ServerCapabilityStatementProvider {

	private final FhirVersionEnum fhirVersion;

	public CapabilityStatementProvider(final RestfulServer theServer,
												  final FhirContext fhirContext) {
		super(theServer);
		this.fhirVersion = fhirContext.getVersion().getVersion();
	}

	@Override
	public IBaseConformance getServerConformance(final HttpServletRequest theRequest,
																final RequestDetails theRequestDetails) {
		final IBaseConformance cs = super.getServerConformance(theRequest, theRequestDetails);
		return switch (this.fhirVersion) {
			case R4 -> {
				final var csR4 = (org.hl7.fhir.r4.model.CapabilityStatement) cs;
				final var csR5 = (CapabilityStatement) VersionConvertorFactory_40_50.convertResource(csR4);
				this.updateCapabilityStatement(csR5);
				yield (org.hl7.fhir.r4.model.CapabilityStatement) VersionConvertorFactory_40_50.convertResource(csR5);
			}
			case R4B -> {
				final var csR4B = (org.hl7.fhir.r4b.model.CapabilityStatement) cs;
				final var csR5 = (CapabilityStatement) VersionConvertorFactory_43_50.convertResource(csR4B);
				this.updateCapabilityStatement(csR5);
				yield (org.hl7.fhir.r4b.model.CapabilityStatement) VersionConvertorFactory_43_50.convertResource(csR5);
			}
			case R5 -> {
				final var csR5 = (CapabilityStatement) cs;
				this.updateCapabilityStatement(csR5);
				yield csR5;
			}
			default -> throw new IllegalStateException("Unsupported FHIR version: " + this.fhirVersion);
		};
	}

	private void updateCapabilityStatement(final CapabilityStatement cs) {
		cs.setDate(new Date());
		cs.setDescription("Matchbox Terminology Server");
		cs.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
		cs.addFormat("application/fhir+json");
		cs.addFormat("application/fhir+xml");
		cs.setInstantiates(List.of(
			new CanonicalType("http://hl7.org/fhir/CapabilityStatement/terminology-server")
		));
		cs.setKind(Enumerations.CapabilityStatementKind.INSTANCE);
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
	}
}
