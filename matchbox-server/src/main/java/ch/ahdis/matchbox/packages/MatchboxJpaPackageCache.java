package ch.ahdis.matchbox.packages;

import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BaseResource;
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
	 * This is Matchbox's hook to customize the {@link NpmPackageVersionResourceEntity}s being generated when loading
	 * a package.
	 *
	 * This will check the resource type and delegate to the appropriate method to customize the entity.
	 */
	public static void customizeNpmPackageVersionResourceEntity(final NpmPackageVersionResourceEntity entity,
																			final IBaseResource res) {
		switch (res) {
			case org.hl7.fhir.r4.model.StructureDefinition sdR4 -> customizeStructureDefinition(entity, sdR4, null, null);
			case org.hl7.fhir.r4b.model.StructureDefinition sdR4b -> customizeStructureDefinition(entity, null, sdR4b, null);
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

		final var fhirVersion = SupportedFhirVersion.forResource(sdR4, sdR4b, sdR5);

		final var type = switch (fhirVersion) {
			case R4 -> sdR4.getType();
			case R4B -> sdR4b.getType();
			case R5 -> sdR5.getType();
		};

		var title = switch (fhirVersion) {
			case R4 -> sdR4.getTitle() != null ? sdR4.getTitle() : sdR4.getName();
			case R4B -> sdR4b.getTitle() != null ? sdR4b.getTitle() : sdR4b.getName();
			case R5 -> sdR5.getTitle() != null ? sdR5.getTitle() : sdR5.getName();
		};
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

		final var fhirVersion = SupportedFhirVersion.forResource(smR4, smR4b, smR5);
		var title = switch (fhirVersion) {
			case R4 -> smR4.getTitle() != null ? smR4.getTitle() : smR4.getName();
			case R4B -> smR4b.getTitle() != null ? smR4b.getTitle() : smR4b.getName();
			case R5 -> smR5.getTitle() != null ? smR5.getTitle() : smR5.getName();
		};

		// Change the filename for the StructureDefinition title
		npmPackageVersionResourceEntity.setFilename(title);
	}

	private enum SupportedFhirVersion {
		R4, R4B, R5;

		public static SupportedFhirVersion forResource(final BaseResource resR4,
																	  final org.hl7.fhir.r4b.model.BaseResource resR4b,
																	  final org.hl7.fhir.r5.model.BaseResource resR5) {
			if (resR4 != null) {
				return R4;
			} else if (resR4b != null) {
				return R4B;
			} else if (resR5 != null) {
				return R5;
			} else {
				throw new IllegalArgumentException("All arguments are null");
			}
		}
	}
}
