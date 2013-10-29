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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author mfinsterwalder
 * @since 2013-06-07 16:05
 */
public class RopertyFactoriesTest {

	final KeyValuesFactory keyValueFactoryMock = mock(KeyValuesFactory.class);
	final DomainSpecificValueFactory domainSpecificValueFactoryMock = mock(DomainSpecificValueFactory.class);
	final String key = "key";
	final String defaultValue = "default";

	@Before
	public void before() {
		when(keyValueFactoryMock.create(domainSpecificValueFactoryMock)).thenReturn(new KeyValues(domainSpecificValueFactoryMock));
		when(domainSpecificValueFactoryMock.create("", 1, defaultValue, null)).thenReturn(new DomainSpecificValue("", 1, defaultValue));
	}

	@Test
	public void factoriesAreUsedToCreateObjectsViaFactoryProvider() {
		Roperty r = new Roperty(new Persistence() {
			HashMap<String, KeyValues> stringKeyValuesHashMap = new HashMap<>();
			@Override
			public KeyValues load(final String key, final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return stringKeyValuesHashMap.get(key);
			}

			@Override
			public Map<String, KeyValues> loadAll(final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return stringKeyValuesHashMap;
			}

			@Override
			public Map<String, KeyValues> reload(final Map<String, KeyValues> keyValuesMap, final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return keyValuesMap;
			}

			@Override
			public void store(final String key, final KeyValues keyValues, final String changeSet) {
				stringKeyValuesHashMap.put(key, keyValues);
			}

			@Override
			public void remove(final String key, final KeyValues keyValues) {
			}

			@Override
			public void remove(final String key, final DomainSpecificValue domainSpecificValue) {
			}
		}, new DomainInitializer() {
			@Override
			public CopyOnWriteArrayList<String> getInitialDomains() {
				return new CopyOnWriteArrayList<>();
			}
		}, new FactoryProvider() {
			@Override
			public KeyValuesFactory getKeyValuesFactory() {
				return keyValueFactoryMock;
			}

			@Override
			public DomainSpecificValueFactory getDomainSpecificValueFactory() {
				return domainSpecificValueFactoryMock;
			}
		});
		checkFactoryAccess(r);
	}

	@Test
	public void factoriesAreUsedToCreateObjectsViaFactoryProviderAndDomainInitializer() {
		Roperty r = new Roperty(new Persistence() {
			HashMap<String, KeyValues> stringKeyValuesHashMap = new HashMap<>();
			@Override
			public KeyValues load(final String key, final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return stringKeyValuesHashMap.get(key);
			}

			@Override
			public Map<String, KeyValues> loadAll(final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return stringKeyValuesHashMap;
			}

			@Override
			public Map<String, KeyValues> reload(final Map<String, KeyValues> keyValuesMap, final KeyValuesFactory keyValuesFactory, final DomainSpecificValueFactory domainSpecificValueFactory) {
				return keyValuesMap;
			}

			@Override
			public void store(final String key, final KeyValues keyValues, final String changeSet) {
				stringKeyValuesHashMap.put(key, keyValues);
			}

			@Override
			public void remove(final String key, final KeyValues keyValues) {
			}

			@Override
			public void remove(final String key, final DomainSpecificValue domainSpecificValue) {
			}
		}, new FactoryProvider() {
			@Override
			public KeyValuesFactory getKeyValuesFactory() {
				return keyValueFactoryMock;
			}

			@Override
			public DomainSpecificValueFactory getDomainSpecificValueFactory() {
				return domainSpecificValueFactoryMock;
			}
		});
		checkFactoryAccess(r);
	}

	@Test
	public void factoriesAreUsedToCreateObjectsViaSet() {
		Roperty r = new Roperty();
		r.setKeyValuesFactory(keyValueFactoryMock);
		r.setDomainSpecificValueFactory(domainSpecificValueFactoryMock);
		checkFactoryAccess(r);
	}

	private void checkFactoryAccess(final Roperty r) {
		r.getOrDefine(key, defaultValue, new MapBackedDomainResolver());
		verify(keyValueFactoryMock).create(domainSpecificValueFactoryMock);
		verify(domainSpecificValueFactoryMock).create("", 1, defaultValue, null);
	}
}
