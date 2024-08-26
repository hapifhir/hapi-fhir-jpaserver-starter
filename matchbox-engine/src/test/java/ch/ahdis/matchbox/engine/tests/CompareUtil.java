package ch.ahdis.matchbox.engine.tests;

/*
 * #%L
 * Matchbox Engine
 * %%
 * Copyright (C) 2022 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompareUtil {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CompareUtil.class);

	static public Map<String, String> harmonizeBunldeIds(Bundle bundle) {
		Map<String, String> ids = new HashMap<String, String>();
		int index = 0;
		for (BundleEntryComponent entry : bundle.getEntry()) {
			if (entry.getFullUrl() != null) {
				String resourceId = "res:" + index++;
				ids.put(entry.getFullUrl(), resourceId);
				if (entry.getFullUrl().startsWith("http")) {
					int slashResource = entry.getFullUrl().lastIndexOf("/");
					if (slashResource > 0) {
						int relResource = entry.getFullUrl().lastIndexOf("/", slashResource - 1);
						String relUrl = entry.getFullUrl().substring(relResource + 1);
						ids.put(relUrl, resourceId);
					}
				}
				if (entry.getFullUrl().startsWith("urn:uuid:")) {
					if (entry.getResource().getId() != null) {
						// not completely corret, but sometime fullUrl = urn:oid:xxxx, id xxxx and then
						// reference
						String id = entry.getResource().getId();
						if (id.startsWith("urn:uuid:")) {
							id = id.substring(9);
						}
						String ref = entry.getResource().fhirType() + "/" + id;
						ids.put(ref, resourceId);
					}
				}
			}
		}
		return ids;
	}

	static public String fshy(String text, Map<String, Object> map) {
		String result = text + "\n";
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			result += entry.getKey() + " = " + entry.getValue() + "\n";
		}
		result += "\n";
		return result;
	}

	static public String fshyDifferrence(String text, Map<String, ValueDifference<Object>> map) {
		String result = text + "\n";
		for (Map.Entry<String, ValueDifference<Object>> entry : map.entrySet()) {
			result += entry.getKey() + " = " + entry.getValue() + "\n";
		}
		result += "\n";
		return result;
	}

	static public void compare(String jsonLeft, String jsonRight, boolean onlyDiffering) throws IOException {
		compare(jsonLeft, jsonRight, onlyDiffering, new HashMap<String, String>(), new HashMap<String, String>());
	}

	static public void compare(String jsonLeft, String jsonRight, boolean onlyDiffering,
			Map<String, String> bundleLeftIds, Map<String, String> bundleRightIds) throws IOException {
		Gson g = new Gson();

		@SuppressWarnings("serial")
		Type mapType = new TypeToken<Map<String, Object>>() {
		}.getType();

		Map<String, Object> leftMap = g.fromJson(jsonLeft, mapType);
		Map<String, Object> rightMap = g.fromJson(jsonRight, mapType);

		Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap, bundleLeftIds);
		Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap, bundleRightIds);

		log.debug(fshy("resulting transform", rightFlatMap));

		MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);
		if (!difference.areEqual()) {
			log.error(difference.toString());
			log.error(fshy("entries only on left", difference.entriesOnlyOnLeft()));
			log.error(fshy("entries only on right", difference.entriesOnlyOnRight()));
			log.error(fshyDifferrence("entries differing", difference.entriesDiffering()));
		}

		assertTrue(onlyDiffering ? difference.entriesDiffering().isEmpty() : difference.areEqual());
	}

	static public void compare(Bundle left, Bundle right, boolean onlyDiffering) throws IOException {

		Map<String, String> bundleLeftIds = null;
		Map<String, String> bundleRightIds = null;

		if (left.fhirType().equals("Bundle") && right.fhirType().equals("Bundle")) {
			bundleLeftIds = harmonizeBunldeIds((Bundle) left);
			bundleRightIds = harmonizeBunldeIds((Bundle) right);
		}

		String jsonLeft = new org.hl7.fhir.r4.formats.JsonParser().composeString(left);
		String jsonRight = new org.hl7.fhir.r4.formats.JsonParser().composeString(right);

		compare(jsonLeft, jsonRight, onlyDiffering, bundleLeftIds, bundleRightIds);
	}
	
	
	static public void logMemory() {
  	Runtime runtime = Runtime.getRuntime();
  	long beforeGcTotalMemory = runtime.totalMemory() / 1024 / 1024;
    long beforeGcFreeMemory = runtime.freeMemory() / 1024 / 1024;
    long beforeGcUsedMemory = beforeGcTotalMemory - beforeGcFreeMemory;
    // Run the garbage collector
    runtime.gc();
    // Calculate the used memory
    long totalMemory = runtime.totalMemory() / 1024 / 1024;
    long freeMemory = runtime.freeMemory() / 1024 / 1024;
    long usedMemory = totalMemory - freeMemory;
    // Print the memory usage
    log.info("Total Memory: " + totalMemory + " MB"  + " beforeGc: " + beforeGcTotalMemory + " MB");
    log.info("Free Memory: " + freeMemory + " MB" + " beforeGc: " + beforeGcFreeMemory + " MB");
    log.info("Used Memory: " + usedMemory + " MB" + " beforeGc: " + beforeGcUsedMemory + " MB");
	}

}
