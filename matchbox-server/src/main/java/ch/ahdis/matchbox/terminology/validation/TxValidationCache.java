package ch.ahdis.matchbox.terminology.validation;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.hl7.fhir.r5.model.ValueSet;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TxValidationCache {

	/**
	 * A cache that stores a mapping from value set URLs to expanded value sets, per cache ID.
	 */
	private final Map<String, Map<String, ValueSet>> valueSetCache =
		new PassiveExpiringMap<>(5, TimeUnit.MINUTES);

	@Nullable
	public ValueSet getValueSet(final String cacheId,
										 final String url) {
		return Optional.ofNullable(valueSetCache.get(cacheId))
				.map(map -> map.get(url))
				.orElse(null);
	}

	public void cacheValueSet(final String cacheId,
									  final String url,
									  final ValueSet valueSet) {
		this.valueSetCache
			.computeIfAbsent(cacheId, k -> HashMap.newHashMap(20))
			.put(url, valueSet);
	}
}
