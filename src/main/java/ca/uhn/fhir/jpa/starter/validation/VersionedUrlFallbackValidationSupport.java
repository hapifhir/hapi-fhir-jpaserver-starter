package ca.uhn.fhir.jpa.starter.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Function;

/**
 * A validation support that provides fallback behavior for versioned canonical URLs.
 *
 * When a versioned URL like "http://hl7.org/fhir/StructureDefinition/Organization|4.0.1"
 * is requested, this support attempts lookups in order:
 * 1. Exact versioned URL as requested
 * 2. Major.minor version (e.g., 4.0.1 -> 4.0)
 * 3. Non-versioned URL
 *
 * For non-versioned URLs or URLs not matching the configured prefixes, this support
 * returns null to let other supports in the chain handle the request.
 *
 * This addresses issues where profiles reference versioned base FHIR resources that
 * aren't available with exact version matching in the validation context.
 */
public class VersionedUrlFallbackValidationSupport implements IValidationSupport {

	private static final Logger ourLog = LoggerFactory.getLogger(VersionedUrlFallbackValidationSupport.class);

	public static final String DEFAULT_URL_PREFIX = "http://hl7.org/fhir/StructureDefinition/";

	private final FhirContext myFhirContext;
	private final IValidationSupport myChain;
	private final Set<String> myUrlPrefixes;

	/**
	 * Creates a fallback validation support that only applies to URLs starting with the default prefix
	 * (http://hl7.org/fhir/StructureDefinition/).
	 */
	public VersionedUrlFallbackValidationSupport(FhirContext theFhirContext, IValidationSupport theChain) {
		this(theFhirContext, theChain, Set.of(DEFAULT_URL_PREFIX));
	}

	/**
	 * Creates a fallback validation support that only applies to URLs starting with the specified prefixes.
	 *
	 * @param theFhirContext the FHIR context
	 * @param theChain the validation support chain to delegate fallback lookups to
	 * @param theUrlPrefixes the URL prefixes to apply fallback logic to (e.g., "http://hl7.org/fhir/StructureDefinition/").
	 *                       Pass an empty set to apply to all URLs.
	 */
	public VersionedUrlFallbackValidationSupport(
			FhirContext theFhirContext, IValidationSupport theChain, Set<String> theUrlPrefixes) {
		myFhirContext = theFhirContext;
		myChain = theChain;
		myUrlPrefixes = theUrlPrefixes;
	}

	@Override
	public FhirContext getFhirContext() {
		return myFhirContext;
	}

	@Override
	public <T extends IBaseResource> T fetchResource(Class<T> theClass, String theUri) {
		return doFetchWithFallback(theUri, uri -> myChain.fetchResource(theClass, uri));
	}

	@Override
	public IBaseResource fetchStructureDefinition(String theUrl) {
		return doFetchWithFallback(theUrl, myChain::fetchStructureDefinition);
	}

	private <T extends IBaseResource> T doFetchWithFallback(String theUrl, Function<String, T> theFetcher) {
		// Check if this is a versioned URL (contains |)
		int pipeIndex = theUrl.indexOf('|');
		if (pipeIndex <= 0) {
			// Not a versioned URL, let other supports handle it
			return null;
		}

		String baseUrl = theUrl.substring(0, pipeIndex);

		// Check if this URL matches our configured prefixes
		if (!matchesPrefix(baseUrl)) {
			return null;
		}

		String version = theUrl.substring(pipeIndex + 1);

		// Try exact versioned URL first
		T result = theFetcher.apply(theUrl);
		if (result != null) {
			return result;
		}

		// Try major.minor version fallback (e.g., 4.0.1 -> 4.0)
		String majorMinorVersion = extractMajorMinorVersion(version);
		if (majorMinorVersion != null && !majorMinorVersion.equals(version)) {
			String majorMinorUrl = baseUrl + "|" + majorMinorVersion;
			result = theFetcher.apply(majorMinorUrl);
			if (result != null) {
				ourLog.warn(
						"Requested versioned canonical '{}' not found, falling back to major.minor version '{}'",
						theUrl,
						majorMinorUrl);
				return result;
			}
		}

		// Try non-versioned URL fallback
		result = theFetcher.apply(baseUrl);
		if (result != null) {
			ourLog.warn(
					"Requested versioned canonical '{}' not found, falling back to non-versioned '{}'",
					theUrl,
					baseUrl);
			return result;
		}

		return null;
	}

	private boolean matchesPrefix(String theUrl) {
		if (myUrlPrefixes.isEmpty()) {
			return true;
		}
		for (String prefix : myUrlPrefixes) {
			if (theUrl.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts major.minor version from a full version string.
	 * For example: "4.0.1" -> "4.0", "4.0" -> "4.0", "4" -> null
	 */
	private String extractMajorMinorVersion(String version) {
		if (version == null || version.isEmpty()) {
			return null;
		}

		String[] parts = version.split("\\.");
		if (parts.length >= 2) {
			return parts[0] + "." + parts[1];
		}
		return null;
	}

	@Override
	public String getName() {
		return "VersionedUrlFallbackValidationSupport";
	}
}
