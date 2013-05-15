package com.parship.roperty;

import java.util.HashMap;
import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-05-15 15:08
 */
public class MapBackedDomainResolver implements DomainResolver {

	private Map<String, String> map = new HashMap<>();

	@Override
	public String getDomainValue(final String domain) {
		return map.get(domain);
	}

	public MapBackedDomainResolver set(final String domain, final String domainValue) {
		map.put(domain, domainValue);
		return this;
	}
}
