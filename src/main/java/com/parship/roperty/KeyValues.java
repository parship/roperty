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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
	private final Set<DomainSpecificValue> domainSpecificValues = new ConcurrentSkipListSet<>();
	private DomainSpecificValueFactory domainSpecificValueFactory;

	public KeyValues(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public DomainSpecificValue put(Object value, String... domainKeyParts) {
		return putWithChangeSet(null, value, domainKeyParts);
	}

	public DomainSpecificValue putWithChangeSet(final String changeSet, final Object value, final String... domainKeyParts) {
		Objects.requireNonNull(domainKeyParts, "Domain key parts may no be null");
		for (String domain : domainKeyParts) {
			Ensure.notEmpty(domain, "domain");
		}
		return addOrChangeDomainSpecificValue(changeSet, value, domainKeyParts);
	}

	private DomainSpecificValue addOrChangeDomainSpecificValue(final String changeSet, final Object value, final String[] domainKeyParts) {
		DomainSpecificValue domainSpecificValue = domainSpecificValueFactory.create(value, changeSet, domainKeyParts);

		if (domainSpecificValues.contains(domainSpecificValue)) {
			for (DomainSpecificValue d: domainSpecificValues) {
				if(d.compareTo(domainSpecificValue) == 0) {
					d.setValue(domainSpecificValue.getValue());
				}
			}
		} else {
			domainSpecificValues.add(domainSpecificValue);
		}
		return domainSpecificValue;
	}

	public <T> T get(Iterable<String> domains, T defaultValue, final DomainResolver resolver) {
        Objects.requireNonNull(domains, "\"domains\" must not be null");
		Iterator<String> domainsIterator = domains.iterator();
		if (domainsIterator.hasNext() && resolver == null) {
			throw new IllegalArgumentException("If a domain is specified, the domain resolver must not be null");
		}
        String domainStr = buildDomain(domainsIterator, resolver);
		for (DomainSpecificValue domainSpecificValue : domainSpecificValues) {
			if ((resolver == null || domainSpecificValue.isInChangeSets(resolver.getActiveChangeSets())) && domainSpecificValue.matches(domainStr)) {
				return (T)domainSpecificValue.getValue();
			}
		}
		return defaultValue;
	}

	private static String buildDomain(final Iterator<String> domainsIterator, final DomainResolver resolver) {
		StringBuilder builder = new StringBuilder();
		while (domainsIterator.hasNext()) {
			String domain = domainsIterator.next();
			String domainValue = resolver.getDomainValue(domain);
			if (domainValue == null) {
				domainValue = "";
			}
			if (domainValue.contains(DOMAIN_SEPARATOR)) {
                throw new IllegalArgumentException("domainValues may not contain '" + DOMAIN_SEPARATOR + '\'');
            }
			builder.append(domainValue).append(DOMAIN_SEPARATOR);
		}
		return builder.toString();
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
			builder.append('\t').append(entry).append('\n');
		}
		builder.append('}');
		return builder.toString();
	}

	public Set<DomainSpecificValue> getDomainSpecificValues() {
		return Collections.unmodifiableSet(domainSpecificValues);
	}

	public void setDomainSpecificValueFactory(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public <T> T getDefaultValue() {
		List<String> emptyList = Collections.emptyList();
		return get(emptyList, null, null);
	}

	public DomainSpecificValue remove(final String changeSet, final String[] domainKeyParts) {
		StringBuilder builder = new StringBuilder(domainKeyParts.length * 8);
		for (String domainValue : domainKeyParts) {
			builder.append(domainValue).append(DOMAIN_SEPARATOR);
		}
		Iterator<DomainSpecificValue> iterator = domainSpecificValues.iterator();
		while(iterator.hasNext()) {
			DomainSpecificValue value = iterator.next();
			if (value.changeSetIs(changeSet) && builder.toString().equals(value.getPatternStr())) {
				iterator.remove();
				return value;
			}
		}
		return null;
	}

	public Collection<DomainSpecificValue> removeChangeSet(final String changeSet) {
		Collection<DomainSpecificValue> removedValues = new ArrayList<>(domainSpecificValues.size());
		Iterator<DomainSpecificValue> iterator = domainSpecificValues.iterator();
		while(iterator.hasNext()) {
			DomainSpecificValue value = iterator.next();
			if (value.changeSetIs(changeSet)) {
				removedValues.add(value);
				iterator.remove();
			}
		}
		return removedValues;
	}
}
