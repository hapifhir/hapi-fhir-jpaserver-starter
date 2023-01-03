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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class FlatMapUtil {

	private FlatMapUtil() {
		throw new AssertionError("No instances for you!");
	}

	public static Map<String, Object> flatten(Map<String, Object> map, final Map<String, String> ids) {

		return map.entrySet().stream().flatMap(FlatMapUtil::flatten)
				.filter(e -> e.getKey().endsWith("cotained.id") || !e.getKey().endsWith(".id") && !e.getKey().equals("id"))
				.filter(e -> !e.getKey().contains(".meta") && !e.getKey().startsWith("meta"))
				.filter(e -> !e.getKey().contains(".text") && !e.getKey().startsWith("text")).map(e -> {
					if (e.getKey().contains("fullUrl") || e.getKey().contains("reference") || e.getKey().contains("url")) {
						if (ids.get(e.getValue()) != null) {
							return new AbstractMap.SimpleEntry<>(e.getKey(), ids.get(e.getValue()));
						}
					}
					return e;
				}).collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
	}

	private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {

		if (entry == null) {
			return Stream.empty();
		}

		if (entry.getValue() instanceof Map<?, ?>) {
			return ((Map<?, ?>) entry.getValue()).entrySet().stream()
					.flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey() + "." + e.getKey(), e.getValue())));
		}

		if (entry.getValue() instanceof List<?>) {
			List<?> list = ((List<?>) entry.getValue()).stream().filter(e -> {
				if (entry.getKey().endsWith("fhir_comments")) {
					return false;
				}
				if (entry.getKey().endsWith("extension")) {
					@SuppressWarnings("unchecked")
					final Map<String, String> ext = (Map<String, String>) e;
					return "http://hl7.org/fhir/StructureDefinition/narrativeLink".equals(ext.get("url")) == false;
				}
				return true;
			}).collect(Collectors.toList());

			return IntStream.range(0, list.size())
					.mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "[" + i + "]", list.get(i)))
					.flatMap(FlatMapUtil::flatten);
		}

		return Stream.of(entry);
	}
}