package com.parship.roperty;

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
			keyValues = new KeyValues();
			map.put(key, keyValues);
		}
		keyValues.put(buildDomain(domains), value);
	}

	private String buildDomain(final String[] domains) {
		if (domains.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			if (builder.length() > 0) {
				builder.append("|");
			}
			builder.append(domain);
		}
		return builder.toString();
	}

	public void setResolver(final Resolver resolver) {
		this.resolver = resolver;
	}
}
