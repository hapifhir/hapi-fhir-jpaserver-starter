package ch.ahdis.fhir.hapi.jpa.validation;

import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;

/**
 * Additional interface for Matchbox's ImplementationGuideResourceProviders.
 *
 * @author Quentin Ligier
 **/
public interface MatchboxImplementationGuideProvider {

	PackageInstallOutcomeJson loadAll(boolean replace);
}
