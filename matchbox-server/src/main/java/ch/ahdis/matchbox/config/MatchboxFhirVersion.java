package ch.ahdis.matchbox.config;

import ca.uhn.fhir.context.FhirVersionEnum;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.IOException;
import java.util.function.Function;

/**
 * Utility class that stores the Matchbox server FHIR version, and provides methods for common operations.
 * Injected as a Spring Bean.
 **/
public class MatchboxFhirVersion {

	private final MatchboxFhirVersionEnum fhirVersion;

	public MatchboxFhirVersion(FhirVersionEnum fhirVersion) {
		this.fhirVersion = switch (fhirVersion) {
			case R4 -> MatchboxFhirVersionEnum.R4;
			case R4B -> MatchboxFhirVersionEnum.R4B;
			case R5 -> MatchboxFhirVersionEnum.R5;
			default -> throw new MatchboxUnsupportedFhirVersionException("MatchboxFhirVersion", fhirVersion);
		};
	}

	public Class<? extends IBaseResource> resourceType(
		final Class<? extends org.hl7.fhir.r4.model.Resource> resourceClassR4,
		final Class<? extends org.hl7.fhir.r4b.model.Resource> resourceClassR4B,
		final Class<? extends org.hl7.fhir.r5.model.Resource> resourceClassR5
	) {
		return switch (fhirVersion) {
			case R4 -> resourceClassR4;
			case R4B -> resourceClassR4B;
			case R5 -> resourceClassR5;
		};
	}

	public void execute(
		final Runnable r4Runnable,
		final Runnable r4bRunnable,
		final Runnable r5Runnable
	) {
		switch (fhirVersion) {
			case R4 -> r4Runnable.run();
			case R4B -> r4bRunnable.run();
			case R5 -> r5Runnable.run();
		}
	}

	/**
	 * Convert a resource to the server's FHIR version for response.
	 * If the resource is already in the correct version, it is returned as-is.
	 *
	 * @param resource The resource to convert.
	 * @return The converted resource, in the server's FHIR version.
	 */
	public IBaseResource convertForResponse(
		final org.hl7.fhir.r5.model.Resource resource
	) {
		return switch (this.fhirVersion) {
			case R4 -> convertToR4(resource);
			case R4B -> convertToR4B(resource);
			case R5 -> resource;
		};
	}

	/**
	 * Convert a resource to R5, apply a function to it, and convert the result back to the original FHIR version.
	 * @param resource The resource to convert and apply the function to.
	 * @param r5Function The function to apply to the resource in R5.
	 * @return The result of applying the function, converted back to the original FHIR version.
	 * @param <T> The R5 resource type to apply the function on.
	 * @param <U> The R5 resource type returned by the function.
	 */
	public <T extends org.hl7.fhir.r5.model.Resource, U extends org.hl7.fhir.r5.model.Resource> IBaseResource applyOnR5(
		final IBaseResource resource,
		final Function<T, U> r5Function,
		final Class<T> resourceClassR5
	) {
		final var resourceR5 = convertToR5(resource, org.hl7.fhir.r5.model.Resource.class);
		final var resultR5 = r5Function.apply(resourceClassR5.cast(resourceR5));
		return convertForResponse(resultR5);
	}

	public String serializeForResponse(final org.hl7.fhir.r5.model.Resource resource) throws IOException {
		return switch (this.fhirVersion) {
			case R4 -> new org.hl7.fhir.r4.formats.JsonParser().composeString(convertToR4(resource));
			case R4B -> new org.hl7.fhir.r4b.formats.JsonParser().composeString(convertToR4B(resource));
			case R5 -> new org.hl7.fhir.r5.formats.JsonParser().composeString(resource);
		};
	}

	@SuppressWarnings("unchecked")
	public static <T extends org.hl7.fhir.r5.model.Resource> T convertToR5(
		final IBaseResource resource,
		final Class<T> convertedResourceClass
	) {
		return switch (resource.getStructureFhirVersionEnum()) {
			case R4 -> (T) VersionConvertorFactory_40_50.convertResource((org.hl7.fhir.r4.model.Resource) resource);
			case R4B -> (T) VersionConvertorFactory_43_50.convertResource((org.hl7.fhir.r4b.model.Resource) resource);
			case R5 -> (T) resource;
			default -> throw new MatchboxUnsupportedFhirVersionException("MatchboxFhirVersion.convertToR5", resource.getStructureFhirVersionEnum());
		};
	}

	public static org.hl7.fhir.r4.model.Resource convertToR4(final org.hl7.fhir.r5.model.Resource resource) {
		return VersionConvertorFactory_40_50.convertResource(resource);
	}

	public static org.hl7.fhir.r4b.model.Resource convertToR4B(final org.hl7.fhir.r5.model.Resource resource) {
		return VersionConvertorFactory_43_50.convertResource(resource);
	}

	public enum MatchboxFhirVersionEnum {
		R4, R4B, R5
	}
}
