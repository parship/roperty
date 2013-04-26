package com.parship.roperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class Roperty {

	private Map<String, KeyValues> map = new ConcurrentHashMap<>();
	private List<String> domains = new CopyOnWriteArrayList<>();
	private Resolver resolver;

	public <T> T get(final String key, final T defaultValue) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			return defaultValue;
		}
		return (T)keyValues.get(domains, resolver);
	}

	public <T> T get(final String key) {
		return get(key, null);
	}

	public <T> T getOrDefine(final String key, final T defaultValue) {
		T value = get(key);
		if (value != null) {
			return value;
		}
		set(key, defaultValue);
		return defaultValue;
	}

	public Roperty addDomain(final String domain) {
		domains.add(domain);
		return this;
	}

	public void set(final String key, final Object value, final String... domains) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			synchronized (map) {
				keyValues = map.get(key);
				if (keyValues == null) {
					keyValues = new KeyValues();
					map.put(key, keyValues);
				}
			}
		}
		keyValues.put(value, domains);
	}

	public void setResolver(final Resolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public String toString() {
		return "Roperty{" +
			"domains=" + domains +
			", map=" + map +
			'}';
	}
}
