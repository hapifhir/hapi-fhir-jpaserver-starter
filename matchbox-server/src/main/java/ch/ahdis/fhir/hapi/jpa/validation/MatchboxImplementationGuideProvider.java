package ch.ahdis.fhir.hapi.jpa.validation;

import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;

/**
 * Additional interface for Matchbox's ImplementationGuideResourceProviders.
 *
 * @author Quentin Ligier
 **/
public interface MatchboxImplementationGuideProvider {

	PackageInstallOutcomeJson loadAll(boolean replace);

	/**
	 * Returns whether the given ImplementationGuide is installed or not.
	 */
	boolean has(final String packageId, final String packageVersion);

	/**
	 * Installs the given ImplementationGuide from the internet registry.
	 */
	void installFromInternetRegistry(final String packageId, final String packageVersion);
}
