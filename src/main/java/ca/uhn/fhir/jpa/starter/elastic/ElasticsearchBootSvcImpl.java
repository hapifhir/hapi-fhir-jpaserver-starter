package ca.uhn.fhir.jpa.starter.elastic;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.dao.TolerantJsonParser;
import ca.uhn.fhir.jpa.search.lastn.ElasticsearchSvcImpl;
import ca.uhn.fhir.jpa.search.lastn.IElasticsearchSvc;
import ca.uhn.fhir.jpa.search.lastn.json.ObservationJson;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.storage.IResourcePersistentId;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.google.common.annotations.VisibleForTesting;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Conditional(ElasticConfigCondition.class)
public class ElasticsearchBootSvcImpl implements IElasticsearchSvc {

	// Index Constants
	public static final String OBSERVATION_INDEX_BASE_NAME = "observation_index";
	public static final String OBSERVATION_CODE_INDEX_BASE_NAME = "code_index";
	public static final String OBSERVATION_INDEX_SCHEMA_FILE = "ObservationIndexSchema.json";
	public static final String OBSERVATION_CODE_INDEX_SCHEMA_FILE = "ObservationCodeIndexSchema.json";

	// Aggregation Constants

	// Observation index document element names
	private static final String OBSERVATION_IDENTIFIER_FIELD_NAME = "identifier";

	// Code index document element names
	private static final String CODE_HASH = "codingcode_system_hash";
	private static final String CODE_TEXT = "text";

	private static final String OBSERVATION_RESOURCE_NAME = "Observation";

	private final ElasticsearchClient myRestHighLevelClient;

	private final FhirContext myContext;

	// Prefixed index names
	private String observationIndexName = OBSERVATION_INDEX_BASE_NAME;
	private String observationCodeIndexName = OBSERVATION_CODE_INDEX_BASE_NAME;

	public ElasticsearchBootSvcImpl(ElasticsearchClient client, FhirContext fhirContext, AppProperties appProperties) {

		myContext = fhirContext;
		myRestHighLevelClient = client;

		// Determine index prefix from configuration
		if (appProperties.getElasticsearch() != null) {
			String indexPrefix = appProperties.getElasticsearch().getIndex_prefix();
			if (indexPrefix != null && !sanitizeElasticsearchIndexName(indexPrefix).isEmpty()) {
				// Set prefixed index names
				this.observationIndexName = indexPrefix + "-" + OBSERVATION_INDEX_BASE_NAME;
				this.observationCodeIndexName = indexPrefix + "-" + OBSERVATION_CODE_INDEX_BASE_NAME;
			}
		}

		try {
			createObservationIndexIfMissing();
			createObservationCodeIndexIfMissing();
		} catch (IOException theE) {
			throw new RuntimeException(Msg.code(1175) + "Failed to create document index", theE);
		}
	}

	/**
	 * Sanitizes a string to be a valid Elasticsearch index name.
	 * <p>
	 * Elasticsearch index name requirements:
	 * - Must be lowercase
	 * - Can only contain: lowercase letters, numbers, hyphens (-), and underscores (_)
	 * - Cannot start with: -, _, or +
	 * - Cannot exceed 255 characters
	 * <p>
	 * This method performs the following transformations:
	 * 1. Converts to lowercase
	 * 2. Replaces any invalid characters with underscores
	 * 3. Removes leading -, _, or + characters
	 * 4. Truncates to 255 characters if necessary
	 * 5. Trims any remaining whitespace
	 *
	 * @param name the string to sanitize
	 * @return a valid Elasticsearch index name
	 */
	private String sanitizeElasticsearchIndexName(String name) {
		String cleaned = name.toLowerCase().replaceAll("[^a-z0-9\\-_]", "_");
		cleaned = cleaned.replaceAll("^[\\-_.]+", "");
		if (cleaned.length() > 255) {
			cleaned = cleaned.substring(0, 255);
		}
		return cleaned.trim();
	}

	private String getIndexSchema(String theSchemaFileName) throws IOException {
		InputStreamReader input =
				new InputStreamReader(ElasticsearchSvcImpl.class.getResourceAsStream(theSchemaFileName));
		BufferedReader reader = new BufferedReader(input);
		StringBuilder sb = new StringBuilder();
		String str;
		while ((str = reader.readLine()) != null) {
			sb.append(str);
		}

		return sb.toString();
	}

	private void createObservationIndexIfMissing() throws IOException {
		if (indexExists(observationIndexName)) {
			return;
		}
		String observationMapping = getIndexSchema(OBSERVATION_INDEX_SCHEMA_FILE);
		if (!createIndex(observationIndexName, observationMapping)) {
			throw new RuntimeException(Msg.code(1176) + "Failed to create observation index");
		}
	}

	private void createObservationCodeIndexIfMissing() throws IOException {
		if (indexExists(observationCodeIndexName)) {
			return;
		}
		String observationCodeMapping = getIndexSchema(OBSERVATION_CODE_INDEX_SCHEMA_FILE);
		if (!createIndex(observationCodeIndexName, observationCodeMapping)) {
			throw new RuntimeException(Msg.code(1177) + "Failed to create observation code index");
		}
	}

	private boolean createIndex(String theIndexName, String theMapping) throws IOException {
		return myRestHighLevelClient
				.indices()
				.create(cir -> cir.index(theIndexName).withJson(new StringReader(theMapping)))
				.acknowledged();
	}

	private boolean indexExists(String theIndexName) throws IOException {
		ExistsRequest request = new ExistsRequest.Builder().index(theIndexName).build();
		return myRestHighLevelClient.indices().exists(request).value();
	}

	@Override
	public void close() {
		// nothing
	}

	@Override
	public List<IBaseResource> getObservationResources(Collection<? extends IResourcePersistentId> thePids) {
		SearchRequest searchRequest = buildObservationResourceSearchRequest(thePids);
		try {
			SearchResponse<ObservationJson> observationDocumentResponse =
					myRestHighLevelClient.search(searchRequest, ObservationJson.class);
			List<Hit<ObservationJson>> observationDocumentHits =
					observationDocumentResponse.hits().hits();
			IParser parser = TolerantJsonParser.createWithLenientErrorHandling(myContext, null);
			Class<? extends IBaseResource> resourceType =
					myContext.getResourceDefinition(OBSERVATION_RESOURCE_NAME).getImplementingClass();
			/**
			 * @see ca.uhn.fhir.jpa.dao.BaseHapiFhirDao#toResource(Class, IBaseResourceEntity, Collection, boolean) for
			 * details about parsing raw json to BaseResource
			 */
			return observationDocumentHits.stream()
					.map(Hit::source)
					.map(observationJson -> parser.parseResource(resourceType, observationJson.getResource()))
					.collect(Collectors.toList());
		} catch (IOException theE) {
			throw new InvalidRequestException(
					Msg.code(2003) + "Unable to execute observation document query for provided IDs " + thePids, theE);
		}
	}

	private SearchRequest buildObservationResourceSearchRequest(Collection<? extends IResourcePersistentId> thePids) {
		List<FieldValue> values = thePids.stream()
				.map(Object::toString)
				.map(v -> FieldValue.of(v))
				.collect(Collectors.toList());

		return SearchRequest.of(sr -> sr.index(observationIndexName)
				.query(qb -> qb.bool(bb -> bb.must(bbm -> {
					bbm.terms(terms ->
							terms.field(OBSERVATION_IDENTIFIER_FIELD_NAME).terms(termsb -> termsb.value(values)));
					return bbm;
				})))
				.size(thePids.size()));
	}

	@VisibleForTesting
	public void refreshIndex(String theIndexName) throws IOException {
		myRestHighLevelClient.indices().refresh(fn -> fn.index(theIndexName));
	}

	/**
	 * Get the observation index name (with prefix if configured)
	 * @return the observation index name
	 */
	public String getObservationIndexName() {
		return observationIndexName;
	}

	/**
	 * Get the observation code index name (with prefix if configured)
	 * @return the observation code index name
	 */
	public String getObservationCodeIndexName() {
		return observationCodeIndexName;
	}
}
