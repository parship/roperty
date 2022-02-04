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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * @author mfinsterwalder
 * @since 2013-09-23 16:45
 */
public class RopertyChangeSetTest {

	private final Roperty roperty = new RopertyImpl();

	@Test
	public void whenChangeSetsAreActiveTheValuesForTheChangeSetAreReturned() {
		roperty.set("key", "value", "descr");
		roperty.setWithChangeSet("key", "valueForChangeSet", "descr", "changeSet");

		DomainResolver resolverWithoutChangeSet = mock(DomainResolver.class);
		assertThat(roperty.get("key", resolverWithoutChangeSet), is("value"));

		DomainResolver resolver = mock(DomainResolver.class);
		when(resolver.getActiveChangeSets()).thenReturn(Collections.singletonList("changeSet"));
		assertThat(roperty.get("key", resolver), is("valueForChangeSet"));
	}

	@Test
	public void whenSetWithChangeSetIsCalledChangeSetWillBePersisted() {
		RopertyImpl ropertyWithPersistence = new RopertyImpl();
		Persistence persistenceMock = mock(Persistence.class);
		ropertyWithPersistence.setPersistence(persistenceMock);

		ropertyWithPersistence.setWithChangeSet("key", "valueForChangeSet", "descr", "changeSet");

		verify(persistenceMock).store(eq("key"), ArgumentMatchers.any(KeyValues.class), eq("changeSet"));
	}
}
