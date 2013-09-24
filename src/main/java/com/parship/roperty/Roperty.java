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
public class Roperty {

	private static final Logger LOGGER = LoggerFactory.getLogger(Roperty.class);
	private volatile Map<String, KeyValues> keyValuesMap;
	private List<String> domains;
	private Persistence persistence;
	private KeyValuesFactory keyValuesFactory;
	private DomainSpecificValueFactory domainSpecificValueFactory;

	public Roperty(final Persistence persistence, final DomainInitializer domainInitializer, final FactoryProvider factoryProvider) {
		this(persistence, domainInitializer, factoryProvider.getKeyValuesFactory(), factoryProvider.getDomainSpecificValueFactory());
	}

	public Roperty(final Persistence persistence, final DomainInitializer domainInitializer, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory
		domainSpecificValueFactory) {
		Ensure.notNull(domainInitializer, "domainInitializer");
		this.domains = domainInitializer.getInitialDomains();
		initFromPersistence(persistence, keyValuesFactory, domainSpecificValueFactory);
	}

	public Roperty(final Persistence persistence, final DomainInitializer domainInitializer) {
		this(persistence, domainInitializer, new DefaultKeyValuesFactory(), new DefaultDomainSpecificValueFactory());
	}

	public Roperty(final Persistence persistence, final FactoryProvider factoryProvider, final String... domains) {
		this(persistence, factoryProvider.getKeyValuesFactory(), factoryProvider.getDomainSpecificValueFactory(), domains);
	}

	public Roperty(final Persistence persistence, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory, final String... domains) {
		initDomains(domains);
		initFromPersistence(persistence, keyValuesFactory, domainSpecificValueFactory);
	}

	private void initDomains(final String[] domains) {
		this.domains = new CopyOnWriteArrayList<>();
		for (String domain : domains) {
			addDomain(domain);
		}
	}

	public Roperty(final Persistence persistence, final String... domains) {
		this(persistence, new DefaultKeyValuesFactory(), new DefaultDomainSpecificValueFactory(), domains);
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

	public Roperty(final String... domains) {
		initDomains(domains);
		initWithoutPersistence();
		RopertyManager.getInstance().add(this);
	}

	public Roperty() {
		this.domains = new CopyOnWriteArrayList<>();
		initWithoutPersistence();
		RopertyManager.getInstance().add(this);
	}

	private void initWithoutPersistence() {
		this.keyValuesFactory = new DefaultKeyValuesFactory();
		this.domainSpecificValueFactory = new DefaultDomainSpecificValueFactory();
		this.keyValuesMap = new HashMap<>();
	}

	public <T> T get(final String key, final T defaultValue, DomainResolver resolver) {
		Ensure.notEmpty(key, "key");
		final String trimmedKey = key.trim();
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

	public <T> T get(final String key, DomainResolver resolver) {
		return get(key, null, resolver);
	}

	public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver) {
		return getOrDefine(key, defaultValue, resolver, null);
	}

	public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver, String description) {
		T value = get(key, resolver);
		if (value != null) {
			return value;
		}
		set(key, defaultValue, description);
		return defaultValue;
	}

	public Roperty addDomain(final String domain) {
		Ensure.notEmpty(domain, "domain");
		domains.add(domain);
		return this;
	}

	public void set(final String key, final Object value, final String description, final String... domains) {
		Ensure.notEmpty(key, "key");
		final String trimmedKey = key.trim();
		LOGGER.debug("Storing value: '{}' for key: '{}' with given domains: '{}'.", value, trimmedKey, domains);
		KeyValues keyValues = getOrCreateKeyValues(description, trimmedKey);
		keyValues.put(value, domains);
		store(trimmedKey, keyValues);
	}

	public void setWithChangeSet(final String key, final Object value, final String description, String changeSet, final String... domains) {
		Ensure.notEmpty(key, "key");
		final String trimmedKey = key.trim();
		LOGGER.debug("Storing value: '{}' for key: '{}' for change set: '{}' with given domains: '{}'.", value, trimmedKey, changeSet, domains);
		KeyValues keyValues = getOrCreateKeyValues(description, trimmedKey);
		keyValues.putWithChangeSet(changeSet, value, domains);
		store(trimmedKey, keyValues);
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
			persistence.store(key, keyValues);
		}
	}

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

	public void reload() {
		if (persistence != null) {
			keyValuesMap = persistence.reload(keyValuesMap, keyValuesFactory, domainSpecificValueFactory);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Roperty{domains=").append(domains).append("}");
		return builder.toString();
	}

	public StringBuilder dump() {
		StringBuilder builder = new StringBuilder("Roperty{domains=").append(domains);
		for (Map.Entry<String, KeyValues> entry : keyValuesMap.entrySet()) {
			builder.append("\n").append("KeyValues for \"").append(entry.getKey()).append("\": ").append(entry.getValue());
		}
		builder.append("\n}");
		return builder;
	}

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

	public Map<String, KeyValues> getKeyValues() {
		return keyValuesMap;
	}

	public void remove(final String key, final String... domainValues) {
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
		if (keyValues != null) {
			keyValues.remove(domainValues);
		}
	}
}
