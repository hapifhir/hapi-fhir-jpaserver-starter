package ch.ahdis.matchbox.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import ch.ahdis.matchbox.util.http.MatchboxFhirFormat;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;

/**
 * Some utilities to parse, serialize, convert FHIR resources between different versions.
 *
 * @author Quentin Ligier
 **/
public class CrossVersionResourceUtils {

	/**
	 * This class is not instantiable.
	 */
	private CrossVersionResourceUtils() {
	}

	public static Resource parseR4AsR5(final byte[] resource,
												  final MatchboxFhirFormat format) {
		final var parsed = parseResource(resource, format, FhirVersionEnum.R4, org.hl7.fhir.r4.model.Resource.class);
		return VersionConvertorFactory_40_50.convertResource(parsed);
	}

	public static Resource parseR4bAsR5(final byte[] resource,
												   final MatchboxFhirFormat format) {
		final var parsed = parseResource(resource, format, FhirVersionEnum.R4B, org.hl7.fhir.r4b.model.Resource.class);
		return VersionConvertorFactory_43_50.convertResource(parsed);
	}

	public static Resource parseR5(final byte[] resource,
											 final MatchboxFhirFormat format) {
		return parseResource(resource, format, FhirVersionEnum.R5, Resource.class);
	}

	public static void serializeR5AsR4(final Resource resource,
												  final MatchboxFhirFormat format,
												  final Writer writer) throws IOException {
		final var parser = getHapiParser(format, FhirVersionEnum.R4);
		parser.setPrettyPrint(true);
		parser.encodeResourceToWriter(VersionConvertorFactory_40_50.convertResource(resource), writer);
	}

	public static void serializeR5AsR4b(final Resource resource,
											  	   final MatchboxFhirFormat format,
												   final Writer writer) throws IOException {
		final var parser = getHapiParser(format, FhirVersionEnum.R4B);
		parser.setPrettyPrint(true);
		parser.encodeResourceToWriter(VersionConvertorFactory_43_50.convertResource(resource), writer);
	}

	public static void serializeR5(final Resource resource,
											 final MatchboxFhirFormat format,
											 final Writer writer) throws IOException {
		final var parser = getHapiParser(format, FhirVersionEnum.R5);
		parser.setPrettyPrint(true);
		parser.encodeResourceToWriter(resource, writer);
	}

	public static org.hl7.fhir.r4.model.Resource convertResource(final org.hl7.fhir.r4b.model.Resource resource) {
		// Is there a better way of doing this?
		return VersionConvertorFactory_40_50.convertResource(VersionConvertorFactory_43_50.convertResource(resource));
	}

	public static org.hl7.fhir.r4b.model.Resource convertResource(final org.hl7.fhir.r4.model.Resource resource) {
		// Is there a better way of doing this?
		return VersionConvertorFactory_43_50.convertResource(VersionConvertorFactory_40_50.convertResource(resource));
	}

	private static <T extends IBaseResource> T parseResource(final byte[] resource,
																			   final MatchboxFhirFormat format,
																			   final FhirVersionEnum version,
																	  	 	   final Class<T> resourceType) {
		return resourceType.cast(getHapiParser(format, version).parseResource(new ByteArrayInputStream(resource)));
	}

	public static IParser getHapiParser(final MatchboxFhirFormat format, final FhirVersionEnum version) {
		return switch (format) {
			case JSON -> FhirContext.forCached(version).newJsonParser();
			case XML -> FhirContext.forCached(version).newXmlParser();
			case FML -> throw new IllegalStateException("FML is not a parseable format");
		};
	}
}
