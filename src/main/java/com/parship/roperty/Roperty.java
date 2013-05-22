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
	private volatile Map<String, KeyValues> map = new ConcurrentHashMap<>();
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
			synchronized (map) {
				keyValues = map.get(key);
				if (keyValues == null) {
					keyValues = new KeyValues();
					map.put(key, keyValues);
				}
			}
		}
		keyValues.put(value, domains);
		store(keyValues);
	}

	private KeyValues getKeyValuesFromMapOrPersistence(final String key) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			keyValues = load(key);
			if (keyValues != null) {
				map.put(key, keyValues);
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

	public void setPersistence(final Persistence persistence) {
		Ensure.notNull(persistence, "persistence");
		this.persistence = persistence;
	}

	@Override
	public String toString() {
		return "Roperty{" +
			"domains=" + domains +
			", map=" + map +
			'}';
	}
}
