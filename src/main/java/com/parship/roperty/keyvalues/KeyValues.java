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

package com.parship.roperty.keyvalues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import com.parship.roperty.Ensure;
import com.parship.roperty.domainresolver.DomainResolver;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;


/**
 * A collection of domain specifically overridden values for a single key.
 * The different DomainSpecificValues are queried according to their ordering and changeSet.
 * @see DomainSpecificValue
 *
 * @author mfinsterwalder
 * @since 2013-03-26 09:18
 */
public class KeyValues<D extends DomainSpecificValue> {

	private static final String DOMAIN_SEPARATOR = "|";
	private String description;
	private final Set<D> domainSpecificValues = new ConcurrentSkipListSet<>();
	private DomainSpecificValueFactory<D> domainSpecificValueFactory;

	public KeyValues(final DomainSpecificValueFactory<D> domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public D put(Object value, String... domainKeyParts) {
		return putWithChangeSet(null, value, domainKeyParts);
	}

	public D putWithChangeSet(final String changeSet, final Object value, final String... domainKeyParts) {
		Objects.requireNonNull(domainKeyParts, "Domain key parts may no be null");
		for (String domain : domainKeyParts) {
			Ensure.notEmpty(domain, "domain");
		}
		return addOrChangeDomainSpecificValue(changeSet, value, domainKeyParts);
	}

	private D addOrChangeDomainSpecificValue(final String changeSet, final Object value, final String[] domainKeyParts) {
		D newDomainSpecificValue = domainSpecificValueFactory.create(value, changeSet, domainKeyParts);

		if (domainSpecificValues.contains(newDomainSpecificValue)) {
			for (D domainSpecificValue: domainSpecificValues) {
				if(domainSpecificValue.compareTo(newDomainSpecificValue) == 0) {
					domainSpecificValue.setValue(newDomainSpecificValue.getValue());
				}
			}
		} else {
			domainSpecificValues.add(newDomainSpecificValue);
		}
		return newDomainSpecificValue;
	}

	public <T> T get(Iterable<String> domainNames, T defaultValue, final DomainResolver resolver) {
        Objects.requireNonNull(domainNames, "\"domainNames\" must not be null");
		Iterator<String> domainNamesIterator = domainNames.iterator();
		if (domainNamesIterator.hasNext() && resolver == null) {
			throw new IllegalArgumentException("If a domain is specified, the domain resolver must not be null");
		}
        String domainStr = buildDomain(domainNamesIterator, resolver);
		for (D domainSpecificValue : domainSpecificValues) {
			if ((resolver == null || domainSpecificValue.isInChangeSets(resolver.getActiveChangeSets())) && domainSpecificValue.matches(domainStr)) {
				return (T)domainSpecificValue.getValue();
			}
		}
		return defaultValue;
	}

	private static String buildDomain(final Iterator<String> domainNamesIterator, final DomainResolver resolver) {
		StringBuilder builder = new StringBuilder();
		while (domainNamesIterator.hasNext()) {
			String domain = domainNamesIterator.next();
			String domainValue = resolver.getDomainKey(domain);
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
		for(D entry:domainSpecificValues) {
			builder.append('\t').append(entry).append('\n');
		}
		builder.append('}');
		return builder.toString();
	}

	public Set<D> getDomainSpecificValues() {
		return Collections.unmodifiableSet(domainSpecificValues);
	}

	public void setDomainSpecificValueFactory(final DomainSpecificValueFactory<D> domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	public <T> T getDefaultValue() {
		List<String> emptyList = Collections.emptyList();
		return get(emptyList, null, null);
	}

	public D remove(final String changeSet, final String... domainKeyParts) {
		StringBuilder builder = new StringBuilder(domainKeyParts.length * 8);
		for (String domainValue : domainKeyParts) {
			builder.append(domainValue).append(DOMAIN_SEPARATOR);
		}
		Iterator<D> iterator = domainSpecificValues.iterator();
		while(iterator.hasNext()) {
			D value = iterator.next();
			if (value.changeSetIs(changeSet) && Objects.equals(builder.toString(), value.getPatternStr())) {
				iterator.remove();
				return value;
			}
		}
		return null;
	}

	public Collection<D> removeChangeSet(final String changeSet) {
		Collection<D> removedValues = new ArrayList<>(domainSpecificValues.size());
		Iterator<D> iterator = domainSpecificValues.iterator();
		while(iterator.hasNext()) {
			D value = iterator.next();
			if (value.changeSetIs(changeSet)) {
				removedValues.add(value);
				iterator.remove();
			}
		}
		return removedValues;
	}
}
