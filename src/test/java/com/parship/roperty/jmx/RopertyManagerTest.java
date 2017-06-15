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

import java.io.PrintStream;

import com.parship.roperty.Roperty;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.keyvalues.KeyValues;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * @author mfinsterwalder
 * @since 2013-06-07 08:49
 */
@RunWith(MockitoJUnitRunner.class)
public class RopertyManagerTest {

    private RopertyManager manager = new RopertyManager();

    @Mock
    private Roperty<DomainSpecificValue, KeyValues<DomainSpecificValue>> roperty;

    @Mock
    private Roperty<DomainSpecificValue, KeyValues<DomainSpecificValue>> anotherRoperty;

    @Mock
    private KeyValues<DomainSpecificValue> keyValues;

    @After
    public void noInteractions() {
        verifyNoMoreInteractions(roperty, anotherRoperty);
    }

    @Test
    public void reloadIsDelegatedToAllRoperties() {

        manager.add(roperty);
        manager.add(anotherRoperty);
        manager.reload();
        verify(roperty).reload();
        verify(anotherRoperty).reload();

    }

    @Test
    public void removedRopertiesAreNotCalled() {
        manager.add(roperty);
        manager.add(anotherRoperty);
        manager.reload();
        verify(roperty).reload();
        verify(anotherRoperty).reload();
        manager.remove(roperty);
        manager.reload();
        verify(roperty).reload();
        verify(anotherRoperty, times(2)).reload();
    }

    @Test
    public void dumpsToSystemOut() {
        PrintStream out = mock(PrintStream.class);
        System.setOut(out);

        manager.add(roperty);
        manager.add(anotherRoperty);

        manager.dumpToSystemOut();

        verify(roperty).dump(out);
        verify(anotherRoperty).dump(out);

        verify(out, times(2)).println();
    }

    @Test
    public void dumpsKey() {

        // given
        given(roperty.getKeyValues("key")).willReturn(keyValues);

        // when
        manager.add(roperty);
        String dump = manager.dump("key");

        // then
        assertThat(dump, is("keyValues\n\n"));
        verify(roperty).getKeyValues("key");
    }

    @Test
    public void dumpsRoperties() {

        // given
        given(roperty.dump()).willReturn(new StringBuilder("dump"));

        // when
        manager.add(roperty);
        String dump = manager.dump();

        // then
        assertThat(dump, is("dump\n\n"));
        verify(roperty).dump();
    }

    @Test
    public void listsRoperties() {

        // when
        manager.add(roperty);
        String roperties = manager.listRoperties();

        // then
        assertThat(roperties, is("[roperty]"));

    }

}
