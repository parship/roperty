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

package com.parship.roperty.jmx;

import com.parship.roperty.Roperty;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author mfinsterwalder
 * @since 2013-06-07 08:49
 */
public class RopertyManagerTest {

	private RopertyManager manager = RopertyManager.getInstance();

	@Before
	public void before() {
		manager.reset();
	}

	@Test
	public void ropertyInstancesRegisterThemselvesWithTheManager() {
		assertThat(manager.dump(), is(""));
		new Roperty();
		assertThat(manager.dump(), is("Roperty{domains=[]\n}\n\n"));
	}

	@Test
	public void ropertyInstancesAreRemovedFromManagerAfterDestruction() {
		Roperty roperty = new Roperty();
		assertThat(manager.dump(), is("Roperty{domains=[]\n}\n\n"));
		roperty = null;
		System.gc();
		assertThat(manager.dump(), is(""));
	}

	@Test
	public void dumpSingleKeyIsDelegatedToAllRoperties() {
		final String key = "key";
		Roperty roperty1 = new Roperty();
		roperty1.set(key, "value1", "descr");
		Roperty roperty2 = new Roperty();
		roperty2.set(key, "value2", "descr");
		String dump = manager.dump("key");
		assertThat(dump, containsString("KeyValues{description=\"descr\"\n\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value1\"}\n}"));
		assertThat(dump, containsString("KeyValues{description=\"descr\"\n\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value2\"}\n}"));
	}

	@Test
	public void reloadIsDelegatedToAllRoperties() {
		Roperty ropertyMock1 = mock(Roperty.class);
		manager.add(ropertyMock1);
		Roperty ropertyMock2 = mock(Roperty.class);
		manager.add(ropertyMock2);
		manager.reload();
		verify(ropertyMock1).reload();
		verify(ropertyMock2).reload();
	}

	@Test
	public void removedRopertiesAreNotCalled() {
		Roperty ropertyMock1 = mock(Roperty.class);
		manager.add(ropertyMock1);
		Roperty ropertyMock2 = mock(Roperty.class);
		manager.add(ropertyMock2);
		manager.reload();
		verify(ropertyMock1).reload();
		verify(ropertyMock2).reload();
		manager.remove(ropertyMock1);
		manager.reload();
		verify(ropertyMock1).reload();
		verify(ropertyMock2, times(2)).reload();
	}

	@Test
	public void listRoperties() {
		Roperty r1 = new Roperty().addDomain("dom1");
		Roperty r2 = new Roperty().addDomain("dom2");
		assertThat(manager.listRoperties(), containsString("Roperty{domains=[dom1]}"));
		assertThat(manager.listRoperties(), containsString("Roperty{domains=[dom2]}"));
	}
}
