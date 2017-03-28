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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyImplTest {

    @Mock
    private DomainResolver resolverMock;

    @Mock
    private Persistence persistenceMock;

    @Mock
    private DomainResolver domainResolverMock;

    private RopertyImpl ropertyImpl;
    private RopertyWithResolver ropertyWithResolver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(resolverMock.getActiveChangeSets()).thenReturn(new ArrayList<>());
        when(resolverMock.getDomainValue(anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[0]);

        ropertyImpl = new RopertyImpl();
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, resolverMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeNull() {
        ropertyWithResolver.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeNullWithDefault() {
        ropertyWithResolver.getOrDefine(null, "default");
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeEmpty() {
        ropertyWithResolver.get("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeEmptyWithDefault() {
        ropertyWithResolver.getOrDefine("", "default");
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetValueForNullKey() {
        ropertyWithResolver.set(null, "value", "descr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetValueForEmptyKey() {
        ropertyWithResolver.set("", "value", "descr");
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesNull() {
        String value = ropertyWithResolver.get("key");
        assertThat(value, nullValue());
    }

    @Test
    public void gettingAPropertyThatDoesNotExistQueriesPersistence() {
        ropertyImpl.setPersistence(persistenceMock);
        ropertyWithResolver.get("key");
        verify(persistenceMock).load(eq("key"), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesDefaultValue() {
        String text = "default";
        String value = ropertyWithResolver.get("key", text);
        assertThat(value, is(text));
    }

    @Test
    public void settingNullAsValue() {
        ropertyWithResolver.set("key", "value", null);
        assertThat((String) ropertyWithResolver.get("key"), is("value"));
        ropertyWithResolver.set("key", null, null);
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @Test
    public void settingAnEmptyString() {
        ropertyWithResolver.set("key", "", null);
        assertThat((String) ropertyWithResolver.get("key"), is(""));
    }

    @Test
    public void keysAreAlwaysTrimmed() {
        ropertyWithResolver.set("  key   ", "val", "descr");
        assertThat((String) ropertyWithResolver.get(" key"), is("val"));
    }

    @Test
    public void definingAndGettingAStringValue() {
        String key = "key";
        String text = "some Value";
        ropertyWithResolver.set(key, text, null);
        String value = ropertyWithResolver.get(key, "default");
        assertThat(value, is(text));
    }

    @Test
    public void settingAValueCallsStoreOnPersistence() {
        String key = "key";
        ropertyImpl.setPersistence(persistenceMock);
        KeyValues keyValue = new KeyValues(new DefaultDomainSpecificValueFactory());
        when(persistenceMock.load(eq(key), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class))).thenReturn(keyValue);
        ropertyWithResolver.set(key, "value", null);
        verify(persistenceMock).store(key, keyValue, "");
    }

    @Test
    public void gettingAValueWithoutAGivenDefaultGivesValue() {
        String text = "value";
        ropertyWithResolver.set("key", text, null);
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(text));
    }

    @Test
    public void changingAStringValue() {
        ropertyWithResolver.set("key", "first", null);
        ropertyWithResolver.set("key", "other", null);
        String value = ropertyWithResolver.get("key", "default");
        assertThat(value, is("other"));
    }

    @Test
    public void gettingAnIntValueThatDoesNotExistGivesDefault() {
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(3));
    }

    @Test
    public void settingAndGettingAnIntValueWithDefaultGivesStoredValue() {
        ropertyWithResolver.set("key", 7, null);
        int value = ropertyWithResolver.get("key", 3);
        assertThat(value, is(7));
    }

    @Test(expected = ClassCastException.class)
    public void gettingAValueThatHasADifferentTypeGivesAClassCastException() {
        String text = "value";
        ropertyWithResolver.set("key", text, null);
        Integer value = ropertyWithResolver.get("key");
    }

    @Test
    public void getOrDefineSetsAValueWithTheGivenDefault() {
        String text = "text";
        String value = ropertyWithResolver.getOrDefine("key", text, "descr");
        assertThat(value, is(text));
        value = ropertyWithResolver.getOrDefine("key", "other default");
        assertThat(value, is(text));
    }

    @Test(expected = NullPointerException.class)
    public void nullDomainsAreNotAllowed() {
        ropertyImpl.addDomains((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDomainsAreNotAllowed() {
        ropertyImpl.addDomains("");
    }

    @Test
    public void getOverriddenValue() {
        ropertyImpl.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, resolverMock);
        String defaultValue = "default value";
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @Test
    public void whenAKeyForASubdomainIsSetTheRootKeyGetsANullValue() {
        ropertyWithResolver.set("key", "value", "descr", "subdomain");
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @Test
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist() {
        ropertyImpl.addDomains("domain1");
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, resolverMock);
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", "other value", null, "other");
        ropertyWithResolver.set("key", overriddenValue, null, "domain1");
        ropertyWithResolver.set("key", "yet another value", null, "yet another");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @Test
    public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExistWithTwoDomains() {
        ropertyImpl.addDomains("domain1", "domain2");
        DomainResolver mockResolver = domainResolverMock;
        when(mockResolver.getDomainValue("domain1")).thenReturn("domVal1");
        when(mockResolver.getDomainValue("domain2")).thenReturn("domVal2");
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, mockResolver);
        String overriddenValue = "overridden value";
        ropertyWithResolver.set("key", "other value", null, "other");
        ropertyWithResolver.set("key", "domVal1", null, "domVal1");
        ropertyWithResolver.set("key", overriddenValue, null, "domVal1", "domVal2");
        ropertyWithResolver.set("key", "yet another value", null, "domVal1", "other");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue));
    }

    @Test
    public void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden() {
        ropertyImpl.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, resolverMock);
        String defaultValue = "default value";
        String overriddenValue1 = "overridden value domain1";
        ropertyWithResolver.set("key", defaultValue, null);
        ropertyWithResolver.set("key", overriddenValue1, null, "domain1");
        String value = ropertyWithResolver.get("key");
        assertThat(value, is(overriddenValue1));
    }

    @Test
    public void domainValuesAreRequestedFromAResolver() {
        ((RopertyImpl) ropertyWithResolver.getRoperty()).addDomains("domain1", "domain2");
        DomainResolver mockResolver = domainResolverMock;
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, mockResolver);
        ropertyWithResolver.set("key", "value", null);
        ropertyWithResolver.get("key");
        verify(mockResolver).getDomainValue("domain1");
        verify(mockResolver).getDomainValue("domain2");
        verify(mockResolver).getActiveChangeSets();
        verifyNoMoreInteractions(mockResolver);
    }

    @Test
    public void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
        ropertyImpl.addDomains("domain1", "domain2");
        DomainResolver mockResolver = domainResolverMock;
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, mockResolver);
        ropertyWithResolver.get("key");
        verifyNoMoreInteractions(mockResolver);
    }

    @Test
    public void wildcardIsResolvedWhenOtherDomainsMatch() {
        ropertyImpl.addDomains("domain1", "domain2");
        ropertyWithResolver = new RopertyWithResolver(ropertyImpl, resolverMock);
        String value = "overridden value";
        ropertyWithResolver.set("key", value, null, "*", "domain2");
        assertThat((String) ropertyWithResolver.get("key"), is(value));
    }

    @Test
    public void settingANewKeyMapReplacesAllMappings() {
        ropertyWithResolver.set("key", "value", null);
        ropertyImpl.setKeyValuesMap(new ConcurrentHashMap<>());
        assertThat(ropertyWithResolver.get("key"), nullValue());
    }

    @Test
    public void aKeyThatIsNotPresentIsLoadedFromPersistenceAndThenInsertedIntoTheValueStore() {
        String key = "key";
        KeyValues keyValues = new KeyValues(new DefaultDomainSpecificValueFactory());
        when(persistenceMock.load(eq(key), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class))).thenReturn(keyValues);
        ropertyImpl.setPersistence(persistenceMock);
        ropertyWithResolver.get(key);
        assertThat(ropertyImpl.getKeyValues(key), notNullValue());
    }

    @Test
    public void whenKeyValueIsNotInTheMapButCanBeLoadedFromPersistenceItIsOnlyInsertedInsideTheSynchronizedBlockWhenNotAlreadyThere() {
        String key = "key";
        Map<String, KeyValues> mockMap = mock(HashMap.class);
        KeyValues keyValues = new KeyValues(new DefaultDomainSpecificValueFactory());
        when(persistenceMock.load(eq(key), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class))).thenReturn(keyValues);
        ropertyImpl.setPersistence(persistenceMock);
        when(mockMap.get(key)).thenReturn(null).thenReturn(new KeyValues(new DefaultDomainSpecificValueFactory()));
        ropertyImpl.setKeyValuesMap(mockMap);
        ropertyWithResolver.get(key);
        verify(mockMap, never()).put(key, keyValues);
    }

    @Test
    public void domainsThatAreInitializedAreUsed() {
        Roperty roperty1 = new RopertyImpl(persistenceMock, "dom1", "dom2");
        roperty1.set("key", "value", "dom1");
        assertThat((String) roperty1.get("key", resolverMock), is("value"));
    }

    @Test
    public void persistenceThatIsInitializedIsUsed() {
        Roperty roperty1 = new RopertyImpl(persistenceMock, "dom1", "dom2");
        verify(persistenceMock).loadAll(any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
        roperty1.get("key", resolverMock);
        verify(persistenceMock).load(eq("key"), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    public void domainInitializerAndPersistenceAreUsedDuringInitialization() {
        DomainInitializer domainInitializerMock = mock(DomainInitializer.class);
        new RopertyImpl(persistenceMock, domainInitializerMock);
        verify(domainInitializerMock).getInitialDomains();
        verify(persistenceMock).loadAll(any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    public void reloadReplacesKeyValuesMap() {
        RopertyImpl roperty1 = new RopertyImpl(persistenceMock);
        verify(persistenceMock).loadAll(any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
        roperty1.set("key", "value", "descr");
        assertThat((String) roperty1.get("key", null), is("value"));
        roperty1.reload();
        verify(persistenceMock).reload(any(Map.class), any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
//		assertThat(roperty1.get("key", null), nullValue());
//		verify(persistenceMock, times(2)).loadAll(any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    public void reloadWithoutPersistenceDoesNothing() {
        ropertyImpl.set("key", "value", "descr");
        ropertyImpl.reload();
        assertThat((String) ropertyImpl.get("key", null), is("value"));
    }

    @Test
    public void domainsThatAreInitializedArePresent() {
        RopertyImpl roperty = new RopertyImpl("domain1", "domain2");
        assertThat(roperty.dump().toString(), is("Roperty{domains=[domain1, domain2]\n}"));
    }

    @Test
    public void getKeyValues() {
        String key = "key";
        ropertyImpl.set(key, "value", null);
        KeyValues keyValues = ropertyImpl.getKeyValues(key);
        assertThat(keyValues.getDomainSpecificValues(), hasSize(1));
        String value = keyValues.get(new ArrayList<>(), null, null);
        assertThat(value, is("value"));
    }

    @Test
    public void getKeyValuesTrimsTheKey() {
        ropertyImpl.set("key", "value", null);
        assertThat(ropertyImpl.getKeyValues("  key"), notNullValue());
    }

    @Test
    public void ropertyWithResolverToString() {
        assertThat(ropertyWithResolver.toString(), is("RopertyWithResolver{roperty=Roperty{domains=[]}}"));
    }

    @Test
    public void toStringEmptyRoperty() {
        assertThat(ropertyImpl.dump().toString(), is("Roperty{domains=[]\n}"));
        ropertyImpl.addDomains("domain");
        assertThat(ropertyImpl.dump().toString(), is("Roperty{domains=[domain]\n}"));
    }

    @Test
    public void toStringFilledRoperty() {
        ropertyImpl.addDomains("domain1", "domain2");
        ropertyImpl.set("key", "value", null);
        ropertyImpl.set("key", "value2", null, "domain1");
        ropertyImpl.set(" otherKey ", "otherValue", null); // keys are always trimmed
        assertThat(ropertyImpl.dump().toString(), containsString(""));

        assertThat(ropertyImpl.dump().toString(), containsString("Roperty{domains=[domain1, domain2]\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("KeyValues for \"otherKey\": KeyValues{\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tdescription=\"\"\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"otherValue\"}\n"));

        assertThat(ropertyImpl.dump().toString(), containsString("KeyValues for \"key\": KeyValues{\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tdescription=\"\"\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"domain1|\", ordering=3, value=\"value2\"}\n"));
        assertThat(ropertyImpl.dump().toString(), containsString("\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value\"}\n"));

    }

    @Test
    public void dumpToStdout() throws UnsupportedEncodingException {
        ropertyImpl.addDomains("dom1");
        ropertyImpl.set("key", "value", "descr");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ropertyImpl.dump(new PrintStream(os));
        String output = os.toString("UTF8");
        assertThat(output, is("Roperty{domains=[dom1]\nKeyValues for \"key\": KeyValues{\n\tdescription=\"descr\"\n\tDomainSpecificValue{pattern=\"\", ordering=1, value=\"value\"}\n}\n}\n"));
    }

    @Test
    public void iterate() {
        ropertyImpl.set("key1", "value_1", "desc");
        Map<String, KeyValues> keyValues = ropertyImpl.getKeyValues();
        assertThat(keyValues.size(), is(1));
        assertThat(keyValues.containsKey("key1"), is(true));
        assertThat(keyValues.get("key1").<String>getDefaultValue(), is("value_1"));
    }

    @Test
    public void domainResolverToNullIsIgnored() {
        DomainResolver domainResolver = new MapBackedDomainResolver().set("dom", "domVal");
        ropertyImpl.addDomains("dom", "dom2", "dom3");
        ropertyImpl.get("key", domainResolver);
        ropertyImpl.set("key", "value", "desc");
        ropertyImpl.set("key", "valueDom", "desc", "domVal");
        ropertyImpl.set("key", "valueDom2", "desc", "domVal", "dom2");
        ropertyImpl.set("key", "valueDom3", "desc", "domVal", "dom2", "dom3");
        assertThat(ropertyImpl.<String>get("key", domainResolver), is("valueDom"));
    }

    @Test
    public void removeDefaultValue() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.addDomains("dom1");
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue", "desc", "dom1");

        ropertyWithPersistence.remove("key");

        verify(persistenceMock).remove(eq("key"), any(DomainSpecificValue.class), isNull());
        assertThat(ropertyWithPersistence.get("key", domainResolverMock), nullValue());
        assertThat(ropertyWithPersistence.<String>get("key", resolverMock), is("domValue"));
    }

    @Test
    public void removeDomainSpecificValue() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.addDomains("dom1", "dom2");
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue1", "desc", "dom1");
        ropertyWithPersistence.set("key", "domValue2", "desc", "dom1", "dom2");

        ropertyWithPersistence.remove("key", "dom1");

        verify(persistenceMock).remove(eq("key"), any(DomainSpecificValue.class), isNull());
        assertThat(ropertyWithPersistence.<String>get("key", domainResolverMock), is("value"));
        assertThat(ropertyWithPersistence.<String>get("key", resolverMock), is("domValue2"));
    }

    @Test
    public void removeDoesNotCallPersistenceWhenNoDomainSpecificValueExists() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.remove("key", "dom1");
        verify(persistenceMock, never()).remove(anyString(), any(DomainSpecificValue.class), anyString());
    }

    @Test
    public void removeACompleteKey() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.set("key", "value", "desc");
        ropertyWithPersistence.set("key", "domValue1", "desc", "dom1");
        ropertyWithPersistence.removeKey("key");
        verify(persistenceMock).remove(eq("key"), any(KeyValues.class), isNull());
        assertThat(ropertyWithPersistence.get("key", resolverMock), nullValue());
    }

    @Test
    public void removeCallsPersistenceEvenWhenNoKeyExists() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.removeKey("key");
        verify(persistenceMock).remove("key", (KeyValues) null, null);
    }

    @Test
    public void removeKeyFromChangeSet() {
        ropertyImpl.set("key", "value", "descr");
        ropertyImpl.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(ropertyImpl.<String>get("key", resolver), is("valueChangeSet"));
        ropertyImpl.removeWithChangeSet("key", "changeSet");
        assertThat(ropertyImpl.<String>get("key", resolver), is("value"));
    }

    @Test
    public void removeAChangeSet() {
        RopertyImpl ropertyWithPersistence = new RopertyImpl(persistenceMock);
        ropertyWithPersistence.set("key", "value", "descr");
        ropertyWithPersistence.setWithChangeSet("key", "valueChangeSet", "descr", "changeSet");
        ropertyWithPersistence.setWithChangeSet("otherKey", "otherValueChangeSet", "descr", "changeSet");
        DomainResolver resolver = new MapBackedDomainResolver().addActiveChangeSets("changeSet");
        assertThat(ropertyWithPersistence.<String>get("key", resolver), is("valueChangeSet"));
        assertThat(ropertyWithPersistence.<String>get("otherKey", resolver), is("otherValueChangeSet"));
        ropertyWithPersistence.removeChangeSet("changeSet");
        verify(persistenceMock).remove(eq("key"), any(DomainSpecificValue.class), eq("changeSet"));
        verify(persistenceMock).remove(eq("otherKey"), any(DomainSpecificValue.class), eq("changeSet"));
        assertThat(ropertyWithPersistence.<String>get("key", resolver), is("value"));
        assertThat(ropertyWithPersistence.<String>get("otherKey", resolver), nullValue());
    }
}
