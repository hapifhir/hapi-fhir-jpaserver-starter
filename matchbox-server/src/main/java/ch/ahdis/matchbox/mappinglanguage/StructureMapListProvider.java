package ch.ahdis.matchbox.mappinglanguage;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.providers.StructureMapResourceProvider;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.StructureMap;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The provider of the StructureMap/$list operation.
 * <p>
 * This operation allows listing all StructureMaps and retrieving a minimal subset of fields. The summary mode includes
 * almost all fields of StructureMap, leading to huge responses. This operation is useful for clients that need to
 * display a list of StructureMaps (e.g. name or url), but do not need all the details of each StructureMap.
 *
 * @author Quentin Ligier
 **/
public class StructureMapListProvider extends StructureMapResourceProvider {

	private final INpmPackageVersionResourceDao npmPackageVersionResourceDao;

	private final PlatformTransactionManager myTxManager;

	private final FhirContext fhirContext;

	public StructureMapListProvider(final INpmPackageVersionResourceDao npmPackageVersionResourceDao,
											  final PlatformTransactionManager myTxManager,
											  final FhirContext fhirContext) {
		super();
		this.npmPackageVersionResourceDao = npmPackageVersionResourceDao;
		this.myTxManager = myTxManager;
		this.fhirContext = fhirContext;
	}

	@Operation(name = "$list", idempotent = true)
	public IBaseBundle listStructureMaps(final RequestDetails requestDetails) {
		final var resources = new TransactionTemplate(this.myTxManager)
			.execute(tx -> this.npmPackageVersionResourceDao.getStructureMapResources())
			.stream()
			.map(this::summarizeStructureMap)
			.toList();

		final var bundle = new Bundle();
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setTotal(resources.size());
		bundle.addLink().setRelation(Bundle.LinkRelationTypes.SELF).setUrl(requestDetails.getCompleteUrl());
		resources.forEach(resource -> bundle.addEntry().setResource(resource));

		return switch (this.fhirContext.getVersion().getVersion()) {
			case R4 -> (org.hl7.fhir.r4.model.Bundle) VersionConvertorFactory_40_50.convertResource(bundle);
			case R4B -> (org.hl7.fhir.r4b.model.Bundle) VersionConvertorFactory_43_50.convertResource(bundle);
			case R5 -> bundle;
			default -> throw new MatchboxUnsupportedFhirVersionException("StructureMapListProvider",
																							 this.fhirContext.getVersion().getVersion());
		};
	}

	private StructureMap summarizeStructureMap(final NpmPackageVersionResourceEntity entity) {
		final StructureMap structureMap = new StructureMap();
		structureMap.setTitle(entity.getFilename());
		structureMap.setUrl(entity.getCanonicalUrl());
		structureMap.setVersion(entity.getCanonicalVersion());
		return structureMap;
	}
}
