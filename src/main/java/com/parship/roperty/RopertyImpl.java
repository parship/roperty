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
import com.parship.roperty.jmx.RopertyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The central class to access Roperty.
 * Manages domains and key-to-value mappings.
 *
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyImpl implements Roperty {

	private static final Logger LOGGER = LoggerFactory.getLogger(RopertyImpl.class);
	private volatile Map<String, KeyValues> keyValuesMap;
	private List<String> domains;
	private Persistence persistence;
	private KeyValuesFactory keyValuesFactory;
	private DomainSpecificValueFactory domainSpecificValueFactory;
	private final Map<String, Collection<String>> changeSets = new HashMap<>();

	public RopertyImpl(final Persistence persistence, final DomainInitializer domainInitializer, final FactoryProvider factoryProvider) {
		this(persistence, domainInitializer, factoryProvider.getKeyValuesFactory(), factoryProvider.getDomainSpecificValueFactory());
	}

	public RopertyImpl(final Persistence persistence, final DomainInitializer domainInitializer, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory
		domainSpecificValueFactory) {
		Ensure.notNull(domainInitializer, "domainInitializer");
		this.domains = domainInitializer.getInitialDomains();
		initFromPersistence(persistence, keyValuesFactory, domainSpecificValueFactory);
	}

	public RopertyImpl(final Persistence persistence, final DomainInitializer domainInitializer) {
		this(persistence, domainInitializer, new DefaultKeyValuesFactory(), createDomainSpecificValueFactory());
	}

	public RopertyImpl(final Persistence persistence, final FactoryProvider factoryProvider, final String... domains) {
		this(persistence, factoryProvider.getKeyValuesFactory(), factoryProvider.getDomainSpecificValueFactory(), domains);
	}

	public RopertyImpl(final Persistence persistence, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory, final String... domains) {
		initDomains(domains);
		initFromPersistence(persistence, keyValuesFactory, domainSpecificValueFactory);
	}

	private void initDomains(final String[] domains) {
		this.domains = new CopyOnWriteArrayList<>();
		addDomains(domains);
	}

	public RopertyImpl(final Persistence persistence, final String... domains) {
		this(persistence, new DefaultKeyValuesFactory(), createDomainSpecificValueFactory(), domains);
	}

	private void initFromPersistence(final Persistence persistence, final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
		Ensure.notNull(keyValuesFactory, "keyValuesFactory");
		Ensure.notNull(domainSpecificValueFactory, "domainSpecificValueFactory");
		Ensure.notNull(persistence, "persistence");
		this.keyValuesFactory = keyValuesFactory;
		this.domainSpecificValueFactory = domainSpecificValueFactory;
		this.persistence = persistence;
		this.keyValuesMap = persistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
		RopertyManager.getInstance().add(this);
	}

	public RopertyImpl(final String... domains) {
		initDomains(domains);
		initWithoutPersistence();
		RopertyManager.getInstance().add(this);
	}

	public RopertyImpl() {
		this.domains = new CopyOnWriteArrayList<>();
		initWithoutPersistence();
		RopertyManager.getInstance().add(this);
	}

	private void initWithoutPersistence() {
		this.keyValuesFactory = new DefaultKeyValuesFactory();
		this.domainSpecificValueFactory = createDomainSpecificValueFactory();
		this.keyValuesMap = new HashMap<>();
	}

	private static DomainSpecificValueFactory createDomainSpecificValueFactory() {
		return new DomainSpecificValueFactoryWithStringInterning();
	}

	/* (non-Javadoc)
	 * @see com.parship.roperty.Roperty#get(java.lang.String, T, com.parship.roperty.DomainResolver)
	 */
	@Override
	public <T> T get(final String key, final T defaultValue, DomainResolver resolver) {
		final String trimmedKey = trimKey(key);
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(trimmedKey);
		T result;
		if (keyValues == null) {
			result = defaultValue;
		} else {
			result = keyValues.get(domains, defaultValue, resolver);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting value for key: '{}' with given default: '{}'. Returning value: '{}'", trimmedKey, defaultValue, result);
			StringBuilder builder = new StringBuilder("DomainValues: ");
			for (String domain : domains) {
				builder.append(domain).append(" => ").append(resolver.getDomainValue(domain)).append("; ");
			}
			LOGGER.debug(builder.toString());
		}
		return result;
	}

	private String trimKey(final String key) {
		Ensure.notEmpty(key, "key");
		return key.trim();
	}

	/* (non-Javadoc)
	 * @see com.parship.roperty.Roperty#get(java.lang.String, com.parship.roperty.DomainResolver)
	 */
	@Override
	public <T> T get(final String key, DomainResolver resolver) {
		return get(key, null, resolver);
	}

	/* (non-Javadoc)
	 * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver)
	 */
	@Override
	public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver) {
		return getOrDefine(key, defaultValue, resolver, null);
	}

	/* (non-Javadoc)
	 * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver, java.lang.String)
	 */
	@Override
	public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver, String description) {
		T value = get(key, resolver);
		if (value != null) {
			return value;
		}
		set(key, defaultValue, description);
		return defaultValue;
	}
	
	@Override
	public Roperty addDomains(final String... domains) {
		Ensure.notNull(domains, "domains");
		for (String domain : domains) {
			Ensure.notEmpty(domain, "domain");
			this.domains.add(domain);
		}
		return this;
	}
	
	@Override
	public void set(final String key, final Object value, final String description, final String... domains) {
		final String trimmedKey = trimKey(key);
		LOGGER.debug("Storing value: '{}' for key: '{}' with given domains: '{}'.", value, trimmedKey, domains);
		KeyValues keyValues = getOrCreateKeyValues(description, trimmedKey);
		keyValues.put(value, domains);
		store(trimmedKey, keyValues);
	}

	@Override
	public void setWithChangeSet(final String key, final Object value, final String description, String changeSet, final String... domains) {
		final String trimmedKey = trimKey(key);
		LOGGER.debug("Storing value: '{}' for key: '{}' for change set: '{}' with given domains: '{}'.", value, trimmedKey, changeSet, domains);
		KeyValues keyValues = getOrCreateKeyValues(description, trimmedKey);
		keyValues.putWithChangeSet(changeSet, value, domains);
		getChangeSetKeys(changeSet).add(trimmedKey);
		store(trimmedKey, keyValues, changeSet);
	}

	private synchronized Collection<String> getChangeSetKeys(final String changeSet) {
		Collection<String> keys = changeSets.get(changeSet);
		if (keys == null) {
			keys = new ArrayList<>();
			changeSets.put(changeSet, keys);
		}
		return keys;
	}

	private KeyValues getOrCreateKeyValues(final String description, final String trimmedKey) {
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(trimmedKey);
		if (keyValues == null) {
			synchronized (keyValuesMap) {
				keyValues = keyValuesMap.get(trimmedKey);
				if (keyValues == null) {
					keyValues = keyValuesFactory.create(domainSpecificValueFactory);
					if (description != null && description.trim().length() > 0) {
						keyValues.setDescription(description);
					}
					keyValuesMap.put(trimmedKey, keyValues);
				}
			}
		}
		return keyValues;
	}

	private KeyValues getKeyValuesFromMapOrPersistence(final String key) {
		KeyValues keyValues = keyValuesMap.get(key);
		if (keyValues == null) {
			keyValues = load(key);
			if (keyValues != null) {
				synchronized (keyValuesMap) {
					KeyValues keyValuesSecondTry = keyValuesMap.get(key);
					if (keyValuesSecondTry == null) {
						keyValuesMap.put(key, keyValues);
					} else {
						return keyValuesSecondTry;
					}
				}
			}
		}
		return keyValues;
	}

	private KeyValues load(final String key) {
		if (persistence != null) {
			return persistence.load(key, keyValuesFactory, domainSpecificValueFactory);
		}
		return null;
	}

	private void store(final String key, final KeyValues keyValues) {
		if (persistence != null) {
			persistence.store(key, keyValues, "");
		}
	}

	private void store(final String key, final KeyValues keyValues, final String changeSet) {
		if (persistence != null) {
			persistence.store(key, keyValues, changeSet);
		}
	}

	private void remove(final String key, final KeyValues keyValues, final String changeSet) {
		if (persistence != null) {
			persistence.remove(key, keyValues, changeSet);
		}
	}

	private void remove(final String key, final DomainSpecificValue domainSpecificValue, final String changeSet) {
		if (persistence != null) {
			persistence.remove(key, domainSpecificValue, changeSet);
		}
	}

	@Override
	public void setKeyValuesMap(final Map<String, KeyValues> keyValuesMap) {
		Ensure.notNull(keyValuesMap, "keyValuesMap");
		synchronized (keyValuesMap) {
			this.keyValuesMap = keyValuesMap;
		}
	}

	public void setPersistence(final Persistence persistence) {
		Ensure.notNull(persistence, "persistence");
		this.persistence = persistence;
		RopertyManager.getInstance().add(this);
	}

	@Override
	public void reload() {
		if (persistence != null) {
			keyValuesMap = persistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
		}
	}

	@Override
	public String toString() {
		return "Roperty{domains=" + domains + "}";
	}

	@Override
	public StringBuilder dump() {
		StringBuilder builder = new StringBuilder("Roperty{domains=").append(domains);
		for (Map.Entry<String, KeyValues> entry : keyValuesMap.entrySet()) {
			builder.append("\n").append("KeyValues for \"").append(entry.getKey()).append("\": ").append(entry.getValue());
		}
		builder.append("\n}");
		return builder;
	}

	@Override
	public void dump(final PrintStream out) {
		out.print("Roperty{domains=");
		out.print(domains);
		for (Map.Entry<String, KeyValues> entry : keyValuesMap.entrySet()) {
			out.println();
			out.print("KeyValues for \"");
			out.print(entry.getKey());
			out.print("\": ");
			out.print(entry.getValue());
		}
		out.println("\n}");
	}

	@Override
	public KeyValues getKeyValues(final String key) {
		Ensure.notEmpty("key", key);
		return keyValuesMap.get(key.trim());
	}

	public void setKeyValuesFactory(final KeyValuesFactory keyValuesFactory) {
		this.keyValuesFactory = keyValuesFactory;
	}

	public void setDomainSpecificValueFactory(final DomainSpecificValueFactory domainSpecificValueFactory) {
		this.domainSpecificValueFactory = domainSpecificValueFactory;
	}

	@Override
	public Map<String, KeyValues> getKeyValues() {
		return keyValuesMap;
	}

	@Override
	public void removeWithChangeSet(final String key, final String changeSet, final String... domainValues) {
		final String trimmedKey = trimKey(key);
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(trimmedKey);
		if (keyValues != null) {
			remove(trimmedKey, keyValues.remove(changeSet, domainValues), changeSet);
		}
	}

	@Override
	public void remove(final String key, final String... domainValues) {
		removeWithChangeSet(key, null, domainValues);
	}

	@Override
	public void removeKey(final String key) {
		final String trimmedKey = trimKey(key);
		remove(trimmedKey, keyValuesMap.remove(trimmedKey), null);
	}

	@Override
	public void removeChangeSet(String changeSet) {
		for (String key : changeSets.get(changeSet)) {
			KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
			if (keyValues != null) {
				for (DomainSpecificValue value : keyValues.removeChangeSet(changeSet)) {
					remove(key, value, changeSet);
				}
			}
		}
	}
}
