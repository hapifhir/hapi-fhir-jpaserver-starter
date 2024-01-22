package ch.ahdis.matchbox.gazelle.utils;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class BidMap<K, V> extends HashMap<K, V> {
	@Serial
	private static final long serialVersionUID = 5425067725254365745L;
	private final Map<V, K> reversedMap = new HashMap<>();

	public BidMap() {
	}

	public K getKey(V value) {
		return this.reversedMap.get(value);
	}

	public boolean isEmpty() {
		return this.size() == 0;
	}

	public V remove(Object key) {
		V val = super.remove(key);
		this.reversedMap.remove(val);
		return val;
	}

	public K removeKey(V value) {
		K val = this.reversedMap.remove(value);
		super.remove(val);
		return val;
	}

	public V put(K key, V value) {
		if (this.reversedMap.containsKey(value)) {
			super.remove(this.reversedMap.get(value));
		}

		if (super.containsKey(key)) {
			this.reversedMap.remove(super.get(key));
		}

		this.reversedMap.put(value, key);
		return super.put(key, value);
	}

	@SafeVarargs
	public static <K, V> BidMap<K, V> of(BidMap.Entry<K, V>... entries) {
		BidMap<K, V> result = new BidMap<>();

		for(BidMap.Entry<K, V> entry : entries) {
			result.put(entry.key, entry.value);
		}

		return result;
	}

	public static class Entry<K, V> {
		private K key;
		private V value;

		public Entry() {
		}

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return this.key;
		}

		public BidMap.Entry<K, V> setKey(K key) {
			this.key = key;
			return this;
		}

		public V getValue() {
			return this.value;
		}

		public BidMap.Entry<K, V> setValue(V value) {
			this.value = value;
			return this;
		}
	}
}
