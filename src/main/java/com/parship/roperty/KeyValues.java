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
 * A collection of domain specifically overridden values for a single key.
 * The different DomainSpecificValues are queried according to their ordering and changeSet.
 * @see DomainSpecificValue
 *
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues {

	private static final String DOMAIN_SEPARATOR = "|";
	private String description;
	private Set<DomainSpecificValue> domainSpecificValues = new ConcurrentSkipListSet<>();
	private DomainSpecificValueFactory domainSpecificValueFactory;

	public KeyValues(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public DomainSpecificValue put(Object value, String... domainValues) {
		return putWithChangeSet(null, value, domainValues);
	}

	public DomainSpecificValue putWithChangeSet(final String changeSet, final Object value, final String... domainValues) {
		for (String domain : domainValues) {
			Ensure.notEmpty(domain, "domain");
		}
		return createAndAddDomainSpecificValue(value, domainValues, changeSet);
	}

	private String buildDomain(final Iterable<String> domains, final DomainResolver resolver) {
		StringBuilder builder = new StringBuilder();
		for (String domain : domains) {
			String domainValue = resolver.getDomainValue(domain);
			if (domainValue == null) {
				domainValue = "";
			}
			Ensure.that(!domainValue.contains(DOMAIN_SEPARATOR), "domainValues can not contain '" + DOMAIN_SEPARATOR + "'");
			builder.append(domainValue).append(DOMAIN_SEPARATOR);
		}
		return builder.toString();
	}

	private DomainSpecificValue createAndAddDomainSpecificValue(final Object value, final String[] domainValues, String changeSet) {
		int order = 1;
		if (domainValues.length == 0) {
			return addDomainSpecificValue("", order, value, changeSet);
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (String domain : domainValues) {
			i++;
			if (!"*".equals(domain)) {
				order = order | (int)Math.pow(2, i);
			}
			builder.append(domain).append(DOMAIN_SEPARATOR);
		}
		return addDomainSpecificValue(builder.toString(), order, value, changeSet);
	}

	private synchronized DomainSpecificValue addDomainSpecificValue(final String pattern, final int order, final Object value, String changeSet) {
		DomainSpecificValue domainSpecificValue = domainSpecificValueFactory.create(pattern, order, value, changeSet);
		domainSpecificValues.remove(domainSpecificValue); // this needs to be done, so I can override values with the same key
		domainSpecificValues.add(domainSpecificValue);
		return domainSpecificValue;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(List<String> domains, T defaultValue, final DomainResolver resolver) {
		Ensure.notNull(domains, "domains");
		String domainStr = buildDomain(domains, resolver);
		for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
			if ((resolver == null || domainSpecificValue.isInChangeSets(resolver.getActiveChangeSets())) && domainSpecificValue.matches(domainStr)) {
				return (T)domainSpecificValue.getValue();
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
