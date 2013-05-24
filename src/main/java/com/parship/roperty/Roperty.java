/*
 * Roperty - An advanced property management and retrieval system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty;

import com.parship.commons.util.Ensure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class Roperty {
	private static final Logger LOGGER = LoggerFactory.getLogger(Roperty.class);
	private volatile Map<String, KeyValues> keyValuesMap = new ConcurrentHashMap<>();
	private final List<String> domains = new CopyOnWriteArrayList<>();
	private Persistence persistence;

	public <T> T get(final String key, final T defaultValue, DomainResolver resolver) {
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
		if (keyValues == null) {
			return defaultValue;
		}
		return keyValues.get(domains, resolver);
	}

	public <T> T get(final String key, DomainResolver resolver) {
		return get(key, null, resolver);
	}

	public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver) {
		T value = get(key, resolver);
		if (value != null) {
			return value;
		}
		set(key, defaultValue);
		return defaultValue;
	}

	public Roperty addDomain(final String domain) {
		Ensure.notEmpty(domain, "domain");
		domains.add(domain);
		return this;
	}

	public void set(final String key, final Object value, final String... domains) {
		KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
		if (keyValues == null) {
			synchronized (keyValuesMap) {
				keyValues = keyValuesMap.get(key);
				if (keyValues == null) {
					keyValues = new KeyValues();
					keyValuesMap.put(key, keyValues);
				}
			}
		}
		keyValues.put(value, domains);
		store(keyValues);
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
			return persistence.load(key);
		}
		return null;
	}

	private void store(final KeyValues keyValues) {
		if (persistence != null) {
			persistence.store(keyValues);
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
	}

	@Override
	public String toString() {
		return "Roperty{" +
			"domains=" + domains +
			", map=" + keyValuesMap +
			'}';
	}
}
