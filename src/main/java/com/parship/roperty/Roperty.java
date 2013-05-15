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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class Roperty {

	private final Map<String, KeyValues> map = new ConcurrentHashMap<>();
	private final List<String> domains = new CopyOnWriteArrayList<>();
	private Resolver resolver;

	@SuppressWarnings("unchecked")
	public <T> T get(final String key, final T defaultValue) {
		KeyValues keyValues = map.get(key);
		if (keyValues == null) {
			return defaultValue;
		}
		return keyValues.get(domains, resolver);
	}

	public <T> T get(final String key) {
		return get(key, null);
	}

	public <T> T getOrDefine(final String key, final T defaultValue) {
		T value = get(key);
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
		KeyValues keyValues = map.get(key);
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
	}

	public void setResolver(final Resolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public String toString() {
		return "Roperty{" +
			"domains=" + domains +
			", map=" + map +
			'}';
	}
}
