package ch.ahdis.matchbox.registry;

import lombok.Data;

/**
 * The model 'PackageVersion' of the Simplifier API.
 *
 * @author Quentin Ligier
 * @see <a href="https://app.swaggerhub.com/apis-docs/firely/Simplifier.net_FHIR_Package_API/1.0.1">Simplifier.net FHIR Package API</a>
 **/
@Data
public class SimplifierPackageVersion {

	private String version;
}
