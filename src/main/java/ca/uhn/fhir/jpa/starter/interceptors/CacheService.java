package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ca.uhn.fhir.sl.cache.Cache;
import ca.uhn.fhir.sl.cache.CacheFactory;

public class CacheService {
    private final EnumMap<CacheEnum, Cache<?, ?>> myCaches = new EnumMap<>(CacheEnum.class);

    public CacheService() {
        populateCaches();
    }

    private void populateCaches() {
    for (CacheEnum next : CacheEnum.values()) {
    Cache<Object, Object> nextCache;
    switch (next) {
    case CONSENT_LIST:
    // TTL diperpendek dari 60 -> 10 menit. Ini sekarang cuma jadi
    // fallback safety-net, karena invalidasi normalnya sudah lewat
     // endpoint /internal/consent-cache/invalidate (webhook dari
     // consent registry setiap ada perubahan Consent/Provision).
     // TTL ini menjaga batas atas staleness kalau webhook gagal
     // terkirim (network blip, pod baru saja restart, dst) - terutama
    // penting di deployment multi-replica di mana webhook cuma
    // menjangkau satu pod per panggilan (lihat service-headless.yaml).
    nextCache = CacheFactory.buildEternal(10, 250);
					break;
     case ENCOUNTER:
    case SERVICE_REQUEST:
    case EPISODE_OF_CARE:
				default:
					nextCache = CacheFactory.buildEternal(5, 5000);
					break;
            }

			myCaches.put(next, nextCache);
        }
    }

    public <K, T> T get(CacheEnum theCache, K theKey, Function<K, T> theSupplier) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass());
		return doGet(theCache, theKey, theSupplier);
	}

	protected <K, T> T doGet(CacheEnum theCache, K theKey, Function<K, T> theSupplier) {
		Cache<K, T> cache = getCache(theCache);
		return cache.get(theKey, theSupplier);
	}

	/**
	 * Fetch an item from the cache if it exists, and use the loading function to
	 * obtain it otherwise.
	 * <p>
	 * This method will put the value into the cache using {@link #putAfterCommit(CacheEnum, Object, Object)}.
	 */
	public <K, T> T getThenPutAfterCommit(CacheEnum theCache, K theKey, Function<K, T> theSupplier) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass());
		T retVal = getIfPresent(theCache, theKey);
		if (retVal == null) {
			retVal = theSupplier.apply(theKey);
			putAfterCommit(theCache, theKey, retVal);
		}
		return retVal;
	}

	/**
	 * Fetch an item from the cache if it exists and use the loading function to
	 * obtain it otherwise. If the loading function returns null, the item will not
	 * be placed in the cache and <code>null</code> will be returned.
	 * <p>
	 * This method will put the value into the cache using {@link #putAfterCommit(CacheEnum, Object, Object)}.
	 *
	 * @since 8.6.0
	 */
	public <K, T> T getThenPutAfterCommitIfNotNull(CacheEnum theCache, K theKey, Function<K, T> theSupplier) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass());
		T retVal = getIfPresent(theCache, theKey);
		if (retVal == null) {
			retVal = theSupplier.apply(theKey);
			if (retVal != null) {
				putAfterCommit(theCache, theKey, retVal);
			}
		}
		return retVal;
	}

	public <K, V> V getIfPresent(CacheEnum theCache, K theKey) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass());
		return doGetIfPresent(theCache, theKey);
	}

	protected <K, V> V doGetIfPresent(CacheEnum theCache, K theKey) {
		return (V) getCache(theCache).getIfPresent(theKey);
	}

	public <K, V> void put(CacheEnum theCache, K theKey, V theValue) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass())
				: "Key type " + theKey.getClass() + " doesn't match expected " + theCache.getKeyType() + " for cache "
						+ theCache;
		doPut(theCache, theKey, theValue);
	}

	protected <K, V> void doPut(CacheEnum theCache, K theKey, V theValue) {
		getCache(theCache).put(theKey, theValue);
	}

	/**
	 * This method registers a transaction synchronization that puts an entry in the cache
	 * if and when the current database transaction successfully commits. If the
	 * transaction is rolled back, the key+value passed into this method will
	 * not be added to the cache.
	 * <p>
	 * This is useful for situations where you want to store something that has been
	 * resolved in the DB during the current transaction, but it's not yet guaranteed
	 * that this item will successfully save to the DB. Use this method in that case
	 * in order to avoid cache poisoning.
	 */
	public <K, V> void putAfterCommit(CacheEnum theCache, K theKey, V theValue) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass())
				: "Key type " + theKey.getClass() + " doesn't match expected " + theCache.getKeyType() + " for cache "
						+ theCache;
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					put(theCache, theKey, theValue);
				}
			});
		} else {
			put(theCache, theKey, theValue);
		}
	}

	public <K, V> Map<K, V> getAllPresent(CacheEnum theCache, Collection<K> theKeys) {
		return doGetAllPresent(theCache, theKeys);
	}

	protected <K, V> Map<K, V> doGetAllPresent(CacheEnum theCache, Collection<K> theKeys) {
		return (Map<K, V>) getCache(theCache).getAllPresent(theKeys);
	}

	public void invalidateAllCaches() {
		myCaches.values().forEach(Cache::invalidateAll);
	}

	private <K, T> Cache<K, T> getCache(CacheEnum theCache) {
		return (Cache<K, T>) myCaches.get(theCache);
	}

	public long getEstimatedSize(CacheEnum theCache) {
		return getCache(theCache).estimatedSize();
	}

	public void invalidateCaches(CacheEnum... theCaches) {
		for (CacheEnum next : theCaches) {
			getCache(next).invalidateAll();
		}
	}

	/**
	 * Invalidate satu entry spesifik di dalam sebuah cache, berdasarkan key-nya.
	 * Dipakai untuk invalidasi selektif (mis. consent list utk satu organisasi
	 * saja), tanpa perlu membuang seluruh isi cache lewat {@link #invalidateCaches}.
	 */
	public <K> void invalidate(CacheEnum theCache, K theKey) {
		assert theCache.getKeyType().isAssignableFrom(theKey.getClass());
		Cache<K, Object> cache = getCache(theCache);
		cache.invalidate(theKey);
	}

    public enum CacheEnum {
        CONSENT_LIST(String.class),
        ENCOUNTER(String.class),
        SERVICE_REQUEST(String.class),
        EPISODE_OF_CARE(String.class);

        private final Class<?> myKeyType;

		CacheEnum(Class<?> theKeyType) {
			myKeyType = theKeyType;
		}

		public Class<?> getKeyType() {
			return myKeyType;
		}

		public static CacheEnum fromResourceType(String resourceType) {
			switch (resourceType) {
				case "Encounter":
					return ENCOUNTER;
				case "ServiceRequest":
					return SERVICE_REQUEST;
				case "EpisodeOfCare":
					return EPISODE_OF_CARE;
			}

			return null;
		}
    }
}
