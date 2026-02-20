package ch.ahdis.matchbox.providers;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ch.ahdis.matchbox.config.MatchboxFhirVersion;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class AbstractMatchboxResourceProvider implements IResourceProvider {

	protected final MatchboxFhirVersion fhirVersion;
	protected final Class<? extends org.hl7.fhir.r4.model.Resource> r4Class;
	protected final Class<? extends org.hl7.fhir.r4b.model.Resource> r4bClass;
	protected final Class<? extends org.hl7.fhir.r5.model.Resource> r5Class;

	protected AbstractMatchboxResourceProvider(
		final MatchboxFhirVersion fhirVersion,
		final Class<? extends org.hl7.fhir.r4.model.Resource> r4Class,
		final Class<? extends org.hl7.fhir.r4b.model.Resource> r4bClass,
		final Class<? extends org.hl7.fhir.r5.model.Resource> r5Class
	) {
		this.fhirVersion = fhirVersion;
		this.r4Class = r4Class;
		this.r4bClass = r4bClass;
		this.r5Class = r5Class;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return this.fhirVersion.resourceType(this.r4Class, this.r4bClass, this.r5Class);
	}
}
