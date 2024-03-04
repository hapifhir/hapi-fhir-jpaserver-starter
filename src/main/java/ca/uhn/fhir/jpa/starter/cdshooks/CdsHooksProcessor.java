package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.hapi.fhir.cdshooks.api.json.CdsServiceRequestJson;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.engine.retrieve.RepositoryRetrieveProvider;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.RepositoryTerminologyProvider;
import org.opencds.cqf.fhir.utility.repository.InMemoryFhirRepository;

import java.util.HashMap;
import java.util.Map;

public class CdsHooksProcessor {
	private final Repository contentRepository;
	private final RepositoryTerminologyProvider terminologyProvider;
	private RepositoryRetrieveProvider dataProvider;

	public CdsHooksProcessor(Repository contentRepository, Repository terminologyRepository) {
		this.contentRepository = contentRepository;
		this.terminologyProvider = new RepositoryTerminologyProvider(terminologyRepository);
	}

//	public CdsServiceRequestJson resolve(CdsServiceRequestJson request) {
//		PlanDefinition planDefinition = contentRepository.read(PlanDefinition.class, new IdType(request.getHook()));
//
//		if (!request.getPrefetchKeys().isEmpty()) {
//			dataProvider = new RepositoryRetrieveProvider(new InMemoryFhirRepository(
//				contentRepository.fhirContext(), getPrefetchResources(request)), new RetrieveSettings());
//		} else {
//			dataProvider = new RepositoryRetrieveProvider(contentRepository, new RetrieveSettings());
//		}
//		dataProvider.setTerminologyProvider(terminologyProvider);
//
//
//	}

	protected Map<String, Resource> getResourcesFromBundle(Bundle theBundle) {
		// using HashMap to avoid duplicates
		Map<String, Resource> resourceMap = new HashMap<>();
		theBundle
			.getEntry()
			.forEach(x -> resourceMap.put(x.fhirType() + x.getResource().getId(), x.getResource()));
		return resourceMap;
	}

	protected Bundle getPrefetchResources(CdsServiceRequestJson theJson) {
		// using HashMap to avoid duplicates
		Map<String, Resource> resourceMap = new HashMap<>();
		Bundle prefetchResources = new Bundle();
		Resource resource;
		for (String key : theJson.getPrefetchKeys()) {
			resource = (Resource) theJson.getPrefetch(key);
			if (resource == null) {
				continue;
			}
			if (resource instanceof Bundle) {
				resourceMap.putAll(getResourcesFromBundle((Bundle) resource));
			} else {
				resourceMap.put(resource.fhirType() + resource.getId(), resource);
			}
		}
		resourceMap.forEach((key, value) -> prefetchResources.addEntry().setResource(value));
		return prefetchResources;
	}
}
