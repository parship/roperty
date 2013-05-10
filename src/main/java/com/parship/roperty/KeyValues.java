/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {

	private Set<DomainPattern> patterns = new ConcurrentSkipListSet<>();

	public KeyValues() {
		this("[value undefined]");
	}

	public KeyValues(Object value) {
		Ensure.notNull(value, "value");
		put(value);
	}

	public void put(Object value, String... domains) {
		Ensure.notNull(domains, "domain");
		Ensure.notNull(value, "value");
		createAndAddDomainPattern(value, domains);
	}

	private void createAndAddDomainPattern(final Object value, final String[] domains) {
		int order = 1;
		if (domains.length == 0) {
			addDomainPattern("", order, value);
			return;
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String domain : domains) {
			++i;
            appendSeparatorIfNeeded(builder);
			if (!"*".equals(domain)) {
				order = order | (int)Math.pow(2, i);
			}
			builder.append(domain);
		}
		addDomainPattern(builder.toString(), order, value);
	}

	private void appendSeparatorIfNeeded(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append("|");
		}
	}

	private synchronized void addDomainPattern(final String pattern, final int order, final Object value) {
		DomainPattern domainPattern = new DomainPattern(pattern, order, value);
		patterns.remove(domainPattern);
		patterns.add(domainPattern);
	}

	private String buildDomain(final Iterable<String> domains, final Resolver resolver) {
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			appendSeparatorIfNeeded(builder);
			String domainValue = resolver.getDomainValue(domain);
			Ensure.notEmpty(domainValue, "domainValue from Resolver");
			builder.append(domainValue);
		}
		return builder.toString();
	}

	public <T> T get(List<String> domains, final Resolver resolver) throws IllegalArgumentException {
        if (domains.contains("*"))   {
            throw new IllegalArgumentException();
        }


		String domainStr = buildDomain(domains, resolver);
		for (DomainPattern pattern : patterns) {
			if (pattern.matches(domainStr)) {
				return (T)pattern.getValue();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "KeyValues{" +
			"patterns=" + patterns +
			'}';
	}
}
