package ch.ahdis.matchbox.mappinglanguage;

import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionResourceEntity;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ch.ahdis.matchbox.config.MatchboxFhirVersion;
import ch.ahdis.matchbox.providers.AbstractMatchboxResourceProvider;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
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
public class StructureMapListProvider extends AbstractMatchboxResourceProvider {

	private final INpmPackageVersionResourceDao npmPackageVersionResourceDao;
	private final PlatformTransactionManager myTxManager;

	public StructureMapListProvider(final MatchboxEngineSupport matchboxEngineSupport,
											  final MatchboxFhirVersion matchboxFhirVersion) {
		super(matchboxFhirVersion, org.hl7.fhir.r4.model.StructureMap.class, org.hl7.fhir.r4b.model.StructureMap.class, StructureMap.class);
		this.npmPackageVersionResourceDao = matchboxEngineSupport.getMyPackageVersionResourceDao();
		this.myTxManager = matchboxEngineSupport.getMyTxManager();
	}

	@Operation(name = "$list", idempotent = true, manualRequest = true)
	public IBaseResource listStructureMaps(final RequestDetails requestDetails) {
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

		return this.fhirVersion.convertForResponse(bundle);
	}

	private StructureMap summarizeStructureMap(final NpmPackageVersionResourceEntity entity) {
		final StructureMap structureMap = new StructureMap();
		structureMap.setTitle(entity.getFilename());
		structureMap.setUrl(entity.getCanonicalUrl());
		structureMap.setVersion(entity.getCanonicalVersion());
		return structureMap;
	}
}
