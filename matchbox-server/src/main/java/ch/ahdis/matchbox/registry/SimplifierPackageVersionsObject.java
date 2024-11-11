package ch.ahdis.matchbox.registry;

import lombok.Data;

import java.util.Map;

/**
 * The model 'PackageVersionsObject' of the Simplifier API.
 *
 * @author Quentin Ligier
 * @see <a href="https://app.swaggerhub.com/apis-docs/firely/Simplifier.net_FHIR_Package_API/1.0.1">Simplifier.net FHIR Package API</a>
 **/
@Data
public class SimplifierPackageVersionsObject {

	//private String id;

	//private String name;

	//private String distTags;

	private Map<String, SimplifierPackageVersion> versions;
}
