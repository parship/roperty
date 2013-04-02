package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class Roperty {

	private Map<String, KeyValues> map = new HashMap<>();
	private List<String> domains = new ArrayList<>();
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

	public void set(final String key, final Object value) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			map.put(key, new KeyValues(value));
		} else {
			keyValues.put("", value);
		}
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

	public void set(final String key, final Object value, final String domain) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			keyValues = new KeyValues();
			map.put(key, keyValues);
		}
		keyValues.put(domain, value);
	}

	public void setResolver(final Resolver resolver) {
		this.resolver = resolver;
	}
}
