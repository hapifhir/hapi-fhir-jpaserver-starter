package ch.ahdis.matchbox.packages;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.util.FhirTerser;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * matchbox
 *
 * @author Quentin Ligier
 * @see <a href="https://github.com/ahdis/matchbox/issues/341">The NpmPackageVersionResourceEntity update is costly</a>
 **/
public class MatchboxJpaPackageCache {
	private static final Logger ourLog = LoggerFactory.getLogger(MatchboxJpaPackageCache.class);

	public static final String SD_EXTENSION_TITLE_PREFIX = "[Extension] ";

	/**
	 * This class is not instantiable.
	 */
	private MatchboxJpaPackageCache() {
	}

	/**
	 * This is Matchbox's hook to customize the {@link NpmPackageVersionResourceEntity}s being generated when loading a
	 * package.
	 * <p>
	 * This will check the resource type and delegate to the appropriate method to customize the entity.
	 */
	public static void customizeNpmPackageVersionResourceEntity(final NpmPackageVersionResourceEntity entity,
																					final IBaseResource res) {
		switch (res) {
			case org.hl7.fhir.r4.model.StructureDefinition sdR4 -> customizeStructureDefinition(entity, sdR4, null, null);
			case org.hl7.fhir.r4b.model.StructureDefinition sdR4b ->
				customizeStructureDefinition(entity, null, sdR4b, null);
			case org.hl7.fhir.r5.model.StructureDefinition sdR5 -> customizeStructureDefinition(entity, null, null, sdR5);
			case org.hl7.fhir.r4.model.StructureMap smR4 -> customizeStructureMap(entity, smR4, null, null);
			case org.hl7.fhir.r4b.model.StructureMap smR4b -> customizeStructureMap(entity, null, smR4b, null);
			case org.hl7.fhir.r5.model.StructureMap smR5 -> customizeStructureMap(entity, null, null, smR5);
			default -> { /* do nothing */ }
		}
	}

	/**
	 * Updates the NpmPackageVersionResourceEntity of a StructureDefinition:
	 * <ol>
	 *    <li>entity.myFilename now contains the StructureDefinition.title or StructureDefinition.name</li>
	 *    <li>entity.myCanonicalVersion now contains the StructureDefinition package version</li>
	 * </ol>
	 */
	private static void customizeStructureDefinition(final NpmPackageVersionResourceEntity npmPackageVersionResourceEntity,
																	 final org.hl7.fhir.r4.model.@Nullable StructureDefinition sdR4,
																	 final org.hl7.fhir.r4b.model.@Nullable StructureDefinition sdR4b,
																	 final org.hl7.fhir.r5.model.@Nullable StructureDefinition sdR5) {
		// we update the canonical version to the package version for StructureDefinitions
		// https://github.com/ahdis/matchbox/issues/225
		npmPackageVersionResourceEntity.setCanonicalVersion(npmPackageVersionResourceEntity.getPackageVersion().getVersionId());

		final var terser = new FhirTerserWrapper(sdR4, sdR4b, sdR5);

		final var type = terser.getSinglePrimitiveValueOrNull("type");

		var title = terser.getSinglePrimitiveValueOrNull("title");
		if (title == null) {
			title = terser.getSinglePrimitiveValueOrNull("name");
		}
		if ("Extension".equals(type)) {
			title = SD_EXTENSION_TITLE_PREFIX + title;
		}

		// Change the filename for the StructureDefinition title
		npmPackageVersionResourceEntity.setFilename(title);
	}

	/**
	 * Updates the NpmPackageVersionResourceEntity of a StructureMap:
	 * <ol>
	 *    <li>entity.myFilename now contains the StructureMap.title or StructureMap.name</li>
	 * </ol>
	 */
	private static void customizeStructureMap(final NpmPackageVersionResourceEntity npmPackageVersionResourceEntity,
															final org.hl7.fhir.r4.model.@Nullable StructureMap smR4,
															final org.hl7.fhir.r4b.model.@Nullable StructureMap smR4b,
															final org.hl7.fhir.r5.model.@Nullable StructureMap smR5) {
		final var terser = new FhirTerserWrapper(smR4, smR4b, smR5);

		// Change the filename for the StructureDefinition title
		npmPackageVersionResourceEntity.setFilename(terser.getSinglePrimitiveValueOrNull("title"));
	}

	// A small wrapper around FhirTerser to handle the different FHIR versions of a resource
	private static class FhirTerserWrapper {
		private final IBase resource;
		private final FhirTerser terser;

		public FhirTerserWrapper(final org.hl7.fhir.r4.model.@Nullable BaseResource resourceR4,
										 final org.hl7.fhir.r4b.model.@Nullable BaseResource resourceR4b,
										 final org.hl7.fhir.r5.model.@Nullable BaseResource resourceR5) {
			if (resourceR4 != null) {
				this.resource = resourceR4;
				this.terser = new FhirTerser(FhirContext.forR4Cached());
			} else if (resourceR4b != null) {
				this.resource = resourceR4b;
				this.terser = new FhirTerser(FhirContext.forR4BCached());
			} else if (resourceR5 != null) {
				this.resource = resourceR5;
				this.terser = new FhirTerser(FhirContext.forR5Cached());
			} else {
				throw new IllegalArgumentException("All arguments are null");
			}
		}

		public String getSinglePrimitiveValueOrNull(final String thePath) {
			return this.terser.getSinglePrimitiveValueOrNull(this.resource, thePath);
		}
	}
}
