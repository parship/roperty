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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
@ExtendWith(MockitoExtension.class)
public class RopertyImplTest {

    @Mock(lenient = true)
    private DomainResolver resolverMock;

    @Mock
    private Persistence persistenceMock;

    private RopertyImpl ropertyImpl;

    @BeforeEach
    void setUp() {
        when(resolverMock.getActiveChangeSets()).thenReturn(new ArrayList<>());
        when(resolverMock.getDomainValue(anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        ropertyImpl = new RopertyImpl();
    }

    @Test
    void keyMayNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.get(null));
    }

    @Test
    void keyMayNotBeNullWithDefault() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.getOrDefine(null, "default", resolverMock));
    }

    @Test
    void keyMayNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.get(""));
    }

    @Test
    void keyMayNotBeEmptyWithDefault() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.getOrDefine("", "default", resolverMock));
    }

    @Test
    void canNotSetValueForNullKey() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.set(null, "value", "descr"));
    }

    @Test
    void canNotSetValueForEmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.set("", "value", "descr"));
    }

    @Test
    void gettingAPropertyThatDoesNotExistGivesNull() {
        String value = ropertyImpl.get("key", resolverMock);
        assertThat(value, nullValue());
    }

    @Test
    void gettingAPropertyThatDoesNotExistQueriesPersistence() {
        ropertyImpl.setPersistence(persistenceMock);
        ropertyImpl.get("key", resolverMock);
        verify(persistenceMock).load(eq("key"), any(DomainSpecificValueFactory.class));
    }

    @Test
    void gettingAPropertyThatDoesNotExistGivesDefaultValue() {
        String text = "default";
        String value = ropertyImpl.getOrDefault("key", text, resolverMock);
        assertThat(value, is(text));
    }

    @Test
    void settingNullAsValue() {
        ropertyImpl.set("key", "value", null);
        assertThat(ropertyImpl.get("key", resolverMock), is("value"));
        ropertyImpl.set("key", null, null);
        assertThat(ropertyImpl.get("key", resolverMock), nullValue());
    }

    @Test
    void settingAnEmptyString() {
        ropertyImpl.set("key", "", null);
        assertThat(ropertyImpl.get("key", resolverMock), is(""));
    }

    @Test
    void keysAreAlwaysTrimmed() {
        ropertyImpl.set("  key   ", "val", "descr");
        assertThat(ropertyImpl.get(" key", resolverMock), is("val"));
    }

    @Test
    void definingAndGettingAStringValue() {
        String key = "key";
        String text = "some Value";
        ropertyImpl.set(key, text, null);
        String value = ropertyImpl.getOrDefault(key, "default", resolverMock);
        assertThat(value, is(text));
    }

    @Test
    void settingAValueCallsStoreOnPersistence() {
        String key = "key";
        ropertyImpl.setPersistence(persistenceMock);
        KeyValues keyValue = new KeyValues(key, new DefaultDomainSpecificValueFactory(), null);
        when(persistenceMock.load(eq(key), any(DomainSpecificValueFactory.class))).thenReturn(keyValue);
        ropertyImpl.set(key, "value", null);
        verify(persistenceMock).store(key, keyValue);
    }

    @Test
    void gettingAValueWithoutAGivenDefaultGivesValue() {
        String text = "value";
        ropertyImpl.set("key", text, null);
        String value = ropertyImpl.get("key", resolverMock);
        assertThat(value, is(text));
    }

    @Test
    void changingAStringValue() {
        ropertyImpl.set("key", "first", null);
        ropertyImpl.set("key", "other", null);
        String value = ropertyImpl.getOrDefault("key", "default", resolverMock);
        assertThat(value, is("other"));
    }

    @Test
    void gettingAnIntValueThatDoesNotExistGivesDefault() {
        int value = ropertyImpl.getOrDefault("key", 3, resolverMock);
        assertThat(value, is(3));
    }

    @Test
    void settingAndGettingAnIntValueWithDefaultGivesStoredValue() {
        ropertyImpl.set("key", 7, null);
        int value = ropertyImpl.getOrDefault("key", 3, resolverMock);
        assertThat(value, is(7));
    }

    @Test
    void gettingAValueThatHasADifferentTypeGivesAClassCastException() {
        String text = "value";
        ropertyImpl.set("key", text, null);
        assertThrows(ClassCastException.class, () -> {Integer value = ropertyImpl.get("key", resolverMock);});
    }

    @Test
    void getOrDefineSetsAValueWithTheGivenDefault() {
        String text = "text";
        String value = ropertyImpl.getOrDefine("key", text, "descr", resolverMock);
        assertThat(value, is(text));
        value = ropertyImpl.getOrDefine("key", "other default", resolverMock);
        assertThat(value, is(text));
    }

    @Test
    void nullDomainsAreNotAllowed() {
        assertThrows(NullPointerException.class, () -> ropertyImpl.addDomains((String[]) null));
    }

    @Test
    void emptyDomainsAreNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> ropertyImpl.addDomains(""));
    }

    @Test
    void getOverriddenValue() {
        ropertyImpl.addDomains("domain1");
        String defaultValue = "default value";
        String overriddenValue = "overridden value";
        ropertyImpl.set("key", defaultValue, null);
        ropertyImpl.set("key", overriddenValue, null, "domain1");
        String value = ropertyImpl.get("key", resolverMock);
        assertThat(value, is(overriddenValue));
    }

    @Test
    void whenAKeyForASubdomainIsSetTheRootKeyGetsANullValue() {
        ropertyImpl.set("key", "value", "descr", "subdomain");
        assertThat(ropertyImpl.get("key", resolverMock), nullValue());
    }

    @Test
    void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist() {
        ropertyImpl.addDomains("domain1");
        String overriddenValue = "overridden value";
        ropertyImpl.set("key", "other value", null, "other");
        ropertyImpl.set("key", overriddenValue, null, "domain1");
        ropertyImpl.set("key", "yet another value", null, "yet another");
        String value = ropertyImpl.get("key", resolverMock);
        assertThat(value, is(overriddenValue));
    }

    @Test
    void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExistWithTwoDomains() {
        ropertyImpl.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        when(mockResolver.getDomainValue("domain1")).thenReturn("domVal1");
        when(mockResolver.getDomainValue("domain2")).thenReturn("domVal2");
        String overriddenValue = "overridden value";
        ropertyImpl.set("key", "other value", null, "other");
        ropertyImpl.set("key", "domVal1", null, "domVal1");
        ropertyImpl.set("key", overriddenValue, null, "domVal1", "domVal2");
        ropertyImpl.set("key", "yet another value", null, "domVal1", "other");
        String value = ropertyImpl.get("key", mockResolver);
        assertThat(value, is(overriddenValue));
    }

    @Test
    void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden() {
        ropertyImpl.addDomains("domain1", "domain2");
        String defaultValue = "default value";
        String overriddenValue1 = "overridden value domain1";
        ropertyImpl.set("key", defaultValue, null);
        ropertyImpl.set("key", overriddenValue1, null, "domain1");
        String value = ropertyImpl.get("key", resolverMock);
        assertThat(value, is(overriddenValue1));
    }

    @Test
    void domainValuesAreRequestedFromAResolver() {
        ropertyImpl.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        ropertyImpl.set("key", "value", null);
        ropertyImpl.get("key", mockResolver);
        verify(mockResolver).getDomainValue("domain1");
        verify(mockResolver).getDomainValue("domain2");
        verify(mockResolver).getActiveChangeSets();
        verifyNoMoreInteractions(mockResolver);
    }

    @Test
    void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
        ropertyImpl.addDomains("domain1", "domain2");
        DomainResolver mockResolver = mock(DomainResolver.class);
        ropertyImpl.get("key", mockResolver);
        verifyNoMoreInteractions(mockResolver);
    }

    @Test
    void wildcardIsResolvedWhenOtherDomainsMatch() {
        ropertyImpl.addDomains("domain1", "domain2");
        String value = "overridden value";
        ropertyImpl.set("key", value, null, "*", "domain2");
        assertThat(ropertyImpl.get("key", resolverMock), is(value));
    }

    @Test
    void aKeyThatIsNotPresentIsLoadedFromPersistenceAndThenInsertedIntoTheValueStore() {
        String key = "key";
        KeyValues keyValues = new KeyValues(key, new DefaultDomainSpecificValueFactory(), null);
        when(persistenceMock.load(eq(key), any(DomainSpecificValueFactory.class))).thenReturn(keyValues);
        ropertyImpl.setPersistence(persistenceMock);
        ropertyImpl.get(key);
        assertThat(ropertyImpl.getKeyValues(key), notNullValue());
    }

    @Test
    void domainsThatAreInitializedAreUsed() {
        Roperty roperty1 = new RopertyImpl(persistenceMock, "dom1", "dom2");
        roperty1.set("key", "value", "dom1");
        assertThat(roperty1.get("key", resolverMock), is("value"));
    }

    @Test
    void persistenceThatIsInitializedIsUsed() {
        Roperty roperty1 = new RopertyImpl(persistenceMock, "dom1", "dom2");
        verify(persistenceMock).loadAll(any(DomainSpecificValueFactory.class));
        roperty1.get("key", resolverMock);
        verify(persistenceMock).load(eq("key"), any(DomainSpecificValueFactory.class));
    }

    @Test
    void domainInitializerAndPersistenceAreUsedDuringInitialization() {
        new RopertyImpl(persistenceMock);
        verify(persistenceMock).loadAll(any(DomainSpecificValueFactory.class));
    }

    @Test
    void reloadReplacesKeyValuesMap() {
        RopertyImpl roperty1 = new RopertyImpl(persistenceMock);
        verify(persistenceMock).loadAll(any(DomainSpecificValueFactory.class));
        roperty1.set("key", "value", "descr");
        assertThat(roperty1.get("key"), is("value"));
        roperty1.reload();
        verify(persistenceMock).reload(any(Collection.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    void reloadWithoutPersistenceDoesNothing() {
        ropertyImpl.set("key", "value", "descr");
        ropertyImpl.reload();
        assertThat(ropertyImpl.get("key"), is("value"));
    }

    @Test
    void domainsThatAreInitializedArePresent() {
        RopertyImpl roperty = new RopertyImpl("domain1", "domain2");
        assertThat(roperty.dump().toString(), is("Roperty{domains=[domain1, domain2]\n}"));
    }

    @Test
    void getKeyValues() {
        String key = "key";
        ropertyImpl.set(key, "value", null);
        KeyValues keyValues = ropertyImpl.getKeyValues(key);
        assertThat(keyValues.getDomainSpecificValues(), hasSize(1));
        String value = keyValues.get(new ArrayList<>(), null, null);
        assertThat(value, is("value"));
    }

    @Test
    void getKeyValuesTrimsTheKey() {
        ropertyImpl.set("key", "value", null);
        assertThat(ropertyImpl.getKeyValues("  key"), notNullValue());
    }

    @Test
    void ropertyToString() {
        assertThat(ropertyImpl.toString(), is("Roperty{domains=[]}"));
    }

    @Test
    void toStringEmptyRoperty() {
        assertThat(ropertyImpl.dump().toString(), is("Roperty{domains=[]\n}"));
        ropertyImpl.addDomains("domain");
        assertThat(ropertyImpl.dump().toString(), is("Roperty{domains=[domain]\n}"));
    }

    @Test
    void toStringFilledRoperty() {
        ropertyImpl.addDomains("domain1", "domain2");
        ropertyImpl.set("key", "value", null);
        ropertyImpl.set("key", "value2", null, "domain1");
        ropertyImpl.set(" otherKey ", "otherValue", null); // keys are always trimmed
        assertThat(ropertyImpl.dump().toString(), containsString(""));

        assertThat(ropertyImpl.dump().toString(), containsString("Roperty{domains=[domain1, domain2]\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("KeyValues for \"otherKey\": KeyValues{\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tdescription=\"\"\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"otherValue\", domains=[]"));

        assertThat(ropertyImpl.dump().toString(), containsString("KeyValues for \"key\": KeyValues{\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tdescription=\"\"\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"domain1|\", ordering=3, value=\"value2\", domains=[domain1]"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value\", domains=[]"));

    }

    @Test
    void dumpToStdout() throws UnsupportedEncodingException {
        ropertyImpl.addDomains("dom1");
        ropertyImpl.set("key", "value", "descr");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ropertyImpl.dump(new PrintStream(os));
        String output = os.toString("UTF8");
        assertThat(output, is("Roperty{domains=[dom1]\nKeyValues for \"key\": KeyValues{\n\tdescription=\"descr\"\n\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value\", domains=[]}\n}\n}\n"));
    }

    @Test
    void domainResolverToNullIsIgnored() {
        DomainResolver domainResolver = new MapBackedDomainResolver().set("dom", "domVal");
        ropertyImpl.addDomains("dom", "dom2", "dom3");
        ropertyImpl.get("key", domainResolver);
        ropertyImpl.set("key", "value", "desc");
        ropertyImpl.set("key", "valueDom", "desc", "domVal");
        ropertyImpl.set("key", "valueDom2", "desc", "domVal", "dom2");
        ropertyImpl.set("key", "valueDom3", "desc", "domVal", "dom2", "dom3");
        assertThat(ropertyImpl.get("key", domainResolver), is("valueDom"));
    }

    @Test
    void removeDefaultValue() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.addDomains("dom1");
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue", "desc", "dom1");

        ropertyWithPersistence.remove("key");

        verify(persistenceMock).remove("key", new DefaultDomainSpecificValueFactory().create("value", null));
        assertThat(ropertyWithPersistence.get("key", mock(DomainResolver.class)), nullValue());
        assertThat(ropertyWithPersistence.get("key", resolverMock), is("domValue"));
    }

    @Test
    void removeDomainSpecificValue() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.addDomains("dom1", "dom2");
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue1", "desc", "dom1");
        ropertyWithPersistence.set("key", "domValue2", "desc", "dom1", "dom2");

        ropertyWithPersistence.remove("key", "dom1");

        verify(persistenceMock).remove("key", new DefaultDomainSpecificValueFactory().create("domValue1", null, "dom1"));
        assertThat(ropertyWithPersistence.get("key", mock(DomainResolver.class)), is("value"));
        assertThat(ropertyWithPersistence.get("key", resolverMock), is("domValue2"));
    }

    @Test
    void removeDoesNotCallPersistenceWhenNoDomainSpecificValueExists() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.remove("key", "dom1");
        verify(persistenceMock, never()).remove(any(), any());
    }

    @Test
    void removeACompleteKey() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue1", "desc", "dom1");
        ropertyWithPersistence.removeKey("key");
        verify(persistenceMock).remove("key");
        assertThat(ropertyWithPersistence.get("key", resolverMock), nullValue());
    }

    @Test
    void removeCallsPersistenceEvenWhenNoKeyExists() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.removeKey("key");
        verify(persistenceMock).remove("key");
    }

    @Test
    void removeKeyFromChangeSet() {
        ropertyImpl.set("key", "value", "descr");
        ropertyImpl.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(ropertyImpl.get("key", resolver), is("valueChangeSet"));
        ropertyImpl.removeWithChangeSet("key", "changeSet");
        assertThat(ropertyImpl.get("key", resolver), is("value"));
    }

    @Test
    void removeAChangeSet() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.set("key", "value", "descr");
        ropertyWithPersistence.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        ropertyWithPersistence.setWithChangeSet("otherKey", "otherValueChangeSet", "descr", "changeSet");
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(ropertyWithPersistence.get("key", resolver), is("valueChangeSet"));
        assertThat(ropertyWithPersistence.get("otherKey", resolver), is("otherValueChangeSet"));
        ropertyWithPersistence.removeChangeSet("changeSet");
        verify(persistenceMock).remove("key", new DefaultDomainSpecificValueFactory().create("valueChangeSet", "changeSet"));
        verify(persistenceMock).remove("otherKey", new DefaultDomainSpecificValueFactory().create("otherValueChangeSet", "changeSet"));
        assertThat(ropertyWithPersistence.get("key", resolver), is("value"));
        assertThat(ropertyWithPersistence.<String>get("otherKey", resolver), nullValue());
    }

    @Test
    void removeChangeSetThrowsIllegalArgumentException() {
        RopertyImpl ropertyImpl = new RopertyImpl(persistenceMock);

        assertThrows(NullPointerException.class, () -> ropertyImpl.removeChangeSet(null));
    }

    @Test
    void removeUnexistingChangeSet() {
        ropertyImpl.removeChangeSet("notExistingChangeSet");

        verifyNoInteractions(persistenceMock);
    }
}
