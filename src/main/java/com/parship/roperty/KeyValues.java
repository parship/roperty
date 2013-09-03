/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty;

import com.parship.commons.util.Ensure;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {

	private String description;
	private Set<DomainSpecificValue> domainSpecificValues = new ConcurrentSkipListSet<>();
	private DomainSpecificValueFactory domainSpecificValueFactory;

	public KeyValues(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public void put(Object value, String... domains) {
		for (String domain : domains) {
			Ensure.notEmpty(domain, "domain");
		}
		createAndAddDomainSpecificValue(value, domains);
	}

	private void createAndAddDomainSpecificValue(final Object value, final String[] domains) {
		int order = 1;
		if (domains.length == 0) {
			addDomainSpecificValue("", order, value);
			return;
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String domain : domains) {
			i++;
			if (!"*".equals(domain)) {
				order = order | (int)Math.pow(2, i);
			}
			builder.append(domain);
			appendSeparatorIfNeeded(builder);
		}
		addDomainSpecificValue(builder.toString(), order, value);
	}

	private String buildDomain(final Iterable<String> domains, final DomainResolver resolver) {
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			String domainValue = resolver.getDomainValue(domain);
			if (domainValue == null) {
				domainValue = "";
			}
			Ensure.that(!domainValue.contains("|"), "domainValues can not contain '|'");
			builder.append(domainValue);
			appendSeparatorIfNeeded(builder);
		}
		return builder.toString();
	}

	private void appendSeparatorIfNeeded(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append("|");
		}
	}

	private synchronized void addDomainSpecificValue(final String pattern, final int order, final Object value) {
		DomainSpecificValue domainSpecificValue = domainSpecificValueFactory.create(pattern, order, value);
		domainSpecificValues.remove(domainSpecificValue); // this needs to be done, so I can override values with the same key
		domainSpecificValues.add(domainSpecificValue);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(List<String> domains, final DomainResolver resolver, T defaultValue) {
		Ensure.notNull(domains, "domains");
		String domainStr = buildDomain(domains, resolver);
		for (DomainSpecificValue pattern : domainSpecificValues) {
			if (pattern.matches(domainStr)) {
				return (T)pattern.getValue();
			}
		}
		return defaultValue;
	}

	public String getDescription() {
		return description == null ? "" : description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("KeyValues{\n\tdescription=\"");
		builder.append(getDescription()).append("\"\n");
		for(DomainSpecificValue entry:domainSpecificValues) {
			builder.append("\t").append(entry).append("\n");
		}
		builder.append("}");
		return builder.toString();
	}

	public Set<DomainSpecificValue> getDomainSpecificValues() {
		return domainSpecificValues;
	}

	public void setDomainSpecificValueFactory(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public <T> T getDefaultValue() {
		List<String> emptyList = Collections.emptyList();
		return get(emptyList, null, null);
	}
}
