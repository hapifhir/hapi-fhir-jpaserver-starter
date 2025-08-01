/*
 * #%L
 * HAPI FHIR JPA - Search Parameters
 * %%
 * Copyright (C) 2014 - 2025 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ca.uhn.fhir.jpa.searchparam.extractor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.StorageSettings;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.sl.cache.Cache;
import ca.uhn.fhir.sl.cache.CacheFactory;
import ca.uhn.fhir.util.BundleUtil;
import jakarta.annotation.PostConstruct;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.fhirpath.ExpressionNode;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.fhirpath.FHIRPathUtilityClasses.FunctionDetails;
import org.hl7.fhir.r5.fhirpath.TypeDetails;
import org.hl7.fhir.r5.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ResourceType;
import org.hl7.fhir.r5.model.ValueSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/*
 * MATCHBOX FIX: backported from upstream to fix bean construction
 */
public class SearchParamExtractorR5 extends BaseSearchParamExtractor implements ISearchParamExtractor {

	private FHIRPathEngine myFhirPathEngine;
	private Cache<String, ExpressionNode> myParsedFhirPathCache;

	public SearchParamExtractorR5() {
		super();
	}

	/**
	 * Constructor for unit tests
	 */
	public SearchParamExtractorR5(
			StorageSettings theStorageSettings,
			PartitionSettings thePartitionSettings,
			FhirContext theCtx,
			ISearchParamRegistry theSearchParamRegistry) {
		super(theStorageSettings, thePartitionSettings, theCtx, theSearchParamRegistry);
		initFhirPath();
		start();
	}

	@Override
	@PostConstruct
	public void start() {
		super.start();
		if (myFhirPathEngine == null) {
			initFhirPath();
		}
	}

	public void initFhirPath() {
		IWorkerContext worker = new HapiWorkerContext(getContext(), getContext().getValidationSupport());
		myFhirPathEngine = new FHIRPathEngine(worker);

		myParsedFhirPathCache = CacheFactory.build(TimeUnit.MINUTES.toMillis(10));
	}

	@Override
	public IValueExtractor getPathValueExtractor(IBase theResource, String theSinglePath) {
		return () -> {
			ExpressionNode parsed = myParsedFhirPathCache.get(theSinglePath, path -> myFhirPathEngine.parse(path));
			return myFhirPathEngine.evaluate(
					theResource, (Base) theResource, (Base) theResource, (Base) theResource, parsed);
		};
	}
}
