package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {
	private Map<String, Object> values = new HashMap<>();

	public KeyValues() {
		this("[value undefined]");
	}

	public KeyValues(Object value) {
		Ensure.notNull(value, "value");
		values.put("", value);
	}

	public void put(String domain, Object value) {
		Ensure.notNull(domain, "domain");
		Ensure.notNull(value, "value");
		values.put(domain, value);
	}

	public <T> T get(List<String> domains, final Resolver resolver) {
		T value = (T)values.get("");
		if (resolver == null) {
			return value;
		}
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			if (builder.length() > 0) {
				builder.append("|");
			}
			builder.append(resolver.getDomainValue(domain));
			T overriddenValue = (T)values.get(builder.toString());
			if (overriddenValue != null) {
				value = overriddenValue;
			}
		}
		return (T)value;
	}
}
