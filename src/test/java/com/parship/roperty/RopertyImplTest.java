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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.parship.roperty.domainresolver.DomainResolver;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
@RunWith(MockitoJUnitRunner.class)
public class RopertyImplTest {

    private static final String KEY = "key";
    private static final String OTHER_KEY_WITHOUT_SPACES = "otherKey";
    private static final String OTHER_KEY_WITH_SPACES = "  otherKey  ";
    private static final String DESCRIPTION = "description";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String VALUE_3 = "value3";
    private static final String VALUE_4 = "value3";
    private static final String CHANGE_SET = "changeSet";
    private static final String DOMAIN_NAME_1 = "domainName1";
    private static final String DOMAIN_NAME_2 = "domainName2";
    private static final String DOMAIN_NAME_3 = "domainName3";
    private static final String DOMAIN_KEY_PART_1 = "domainKeyPart1";
    private static final String DOMAIN_KEY_PART_2 = "domainKeyPart2";
    private static final String DOMAIN_KEY_PART_3 = "domainKeyPart3";

    private RopertyImpl<DomainSpecificValue, KeyValues<DomainSpecificValue>> ropertyImpl = new RopertyImpl<>();

    @Mock
    private DomainResolver domainResolver;

    @Mock
    private DomainResolver anotherDomainResolver;

    @Mock
    private KeyValuesFactory<DomainSpecificValue, KeyValues<DomainSpecificValue>> keyValueFactory;

    @Mock
    private DomainSpecificValueFactory<DomainSpecificValue> domainSpecificValueFactory;

    @Mock
    private Persistence<DomainSpecificValue, KeyValues<DomainSpecificValue>> persistence;

    @Mock
    private KeyValues<DomainSpecificValue> keyValues;

    @Mock
    private DomainSpecificValue domainSpecificValue;

    @Before
    public void initalizeMocks() {
        ropertyImpl.setKeyValuesFactory(keyValueFactory);
        ropertyImpl.setDomainSpecificValueFactory(domainSpecificValueFactory);
        ropertyImpl.setPersistence(persistence);

        given(keyValueFactory.create(domainSpecificValueFactory)).willReturn(keyValues);
        given(keyValues.get(any(Iterable.class), isNull(), eq(domainResolver))).willReturn(VALUE_1);

    }

    @After
    public void noInteractions() {
        verifyNoMoreInteractions(domainResolver, anotherDomainResolver, keyValueFactory, domainSpecificValueFactory, persistence, keyValues, domainSpecificValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeNullWithDefault() {
        ropertyImpl.getOrDefine(null, "default", domainResolver);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeEmpty() {
        ropertyImpl.get("", domainResolver);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeEmptyWithDefault() {
        ropertyImpl.getOrDefine("", "default", domainResolver);
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesNull() {
        String value = ropertyImpl.get(KEY, domainResolver);
        assertThat(value, nullValue());

        thenLoadsKeyUsingPersistence();
    }

    @Test
    public void gettingAPropertyThatDoesNotExistGivesDefaultValue() {
        String text = "default";
        String value = ropertyImpl.get(KEY, text, domainResolver);
        assertThat(value, is(text));

        thenLoadsKeyUsingPersistence();
    }

    @Test
    public void settingAnEmptyString() {

        given(keyValues.get(any(Iterable.class), isNull(), eq(domainResolver))).willReturn("");

        ropertyImpl.set(KEY, "", null);
        assertThat(ropertyImpl.get(KEY, domainResolver), is(""));

        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        verify(keyValues).put("");
        thenGetsValueFromKeyValues();

    }

    @Test
    public void keysAreAlwaysTrimmed() {
        ropertyImpl.set("  key   ", VALUE_1, DESCRIPTION);
        assertThat(ropertyImpl.get(" key", domainResolver), is(VALUE_1));

        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        thenGetsValueFromKeyValues();

    }

    @Test
    public void gettingAValueWithoutAGivenDefaultGivesValue() {
        String text = VALUE_1;
        ropertyImpl.set(KEY, text, null);
        String value = ropertyImpl.get(KEY, domainResolver);
        assertThat(value, is(text));

        thenLoadsKeyUsingPersistence();
        thenCreatesKeyValues();
        thenStoresKeyWithoutChangeSet();
        thenPutsValue1WithoutDomains();
        thenGetsValueFromKeyValues();
    }

    @Test
    public void settingAndGettingAnIntValueWithDefaultGivesStoredValue() {

        given(keyValues.get(any(Iterable.class), eq(3), eq(domainResolver))).willReturn(7);

        ropertyImpl.set(KEY, 7, null);
        int value = ropertyImpl.get(KEY, 3, domainResolver);
        assertThat(value, is(7));

        thenLoadsKeyUsingPersistence();
        thenCreatesKeyValues();
        thenStoresKeyWithoutChangeSet();
        verify(keyValues).put(7);
        verify(keyValues).get(isA(Iterable.class), eq(3), eq(domainResolver));
    }

    @Test
    public void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
        ropertyImpl.get(KEY, domainResolver);

        thenLoadsKeyUsingPersistence();
    }

    @Test
    public void aKeyThatIsNotPresentIsLoadedFromPersistenceAndThenInsertedIntoTheValueStore() {
        ropertyImpl.get(KEY, domainResolver);
        thenLoadsKeyUsingPersistence();
    }

    @Test
    public void ropertyImplToString() {
        assertThat(ropertyImpl.toString(), is("Roperty{domains=[]}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetValueForEmptyKey() {
        ropertyImpl.set("", VALUE_1, "descr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void canNotSetValueForNullKey() {
        ropertyImpl.set(null, VALUE_1, "descr");
    }


    @Test
    public void gettingAnIntValueThatDoesNotExistGivesDefault() {
        int value = ropertyImpl.get(KEY, 3, domainResolver);
        assertThat(value, is(3));
        thenLoadsKeyUsingPersistence();
    }

    private void thenLoadsKeyUsingPersistence() {
        verify(persistence).load(KEY, keyValueFactory, domainSpecificValueFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keyMayNotBeNull() {
        ropertyImpl.get(null, domainResolver);
    }

    @Test
    public void domainsThatAreInitializedAreUsed() {

        // given

        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2);

        // when
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);

        // then
        assertThat(ropertyImpl.get(KEY, domainResolver), is(VALUE_1));
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        thenGetsValueFromKeyValues();
    }

    private void thenSetsDescription() {
        verify(keyValues).setDescription(DESCRIPTION);
    }

    private void thenGetsValueFromKeyValues() {
        verify(keyValues).get(isA(Iterable.class), isNull(), eq(domainResolver));
    }

    private void thenPutsValue1WithoutDomains() {
        verify(keyValues).put(VALUE_1);
    }

    private void thenStoresKeyWithoutChangeSet() {
        verify(persistence).store(KEY, keyValues, "");
    }

    private void thenCreatesKeyValues() {
        verify(keyValueFactory).create(domainSpecificValueFactory);
    }

    @Test
    public void persistenceThatIsInitializedIsUsed() {
        // given
        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2);

        // when
        ropertyImpl.get(KEY, domainResolver);

        // then
        thenLoadsKeyUsingPersistence();
    }

    @Test
    public void domainsThatAreInitializedArePresent() {

        // given
        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2);

        // when
        String actual = ropertyImpl.dump().toString();

        // then
        assertThat(actual, is("Roperty{domains=[domainName1, domainName2]\n}"));

    }

    @Test
    public void removesOnlyDefaultValue() {

        // given


        // when
        ropertyImpl.addDomains(DOMAIN_NAME_1);
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.set(KEY, VALUE_2, DESCRIPTION, DOMAIN_KEY_PART_1);
        ropertyImpl.remove(KEY);

        // then
        verify(persistence).remove(eq(KEY), (DomainSpecificValue) isNull(), isNull());
        thenLoadsKeyUsingPersistence();
        verify(persistence, times(2)).store(KEY, keyValues, "");
        thenCreatesKeyValues();
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        verify(keyValues).put(VALUE_2, DOMAIN_KEY_PART_1);
        verify(keyValues).remove(isNull());

    }

    @Test
    public void removesDomainSpecificValue() {

        // given

        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2);
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION, DOMAIN_KEY_PART_1);
        ropertyImpl.set(KEY, VALUE_2, DESCRIPTION, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);

        // when
        ropertyImpl.remove(KEY, DOMAIN_KEY_PART_1);

        // then
        thenCreatesKeyValues();
        verify(persistence).remove(eq(KEY), (DomainSpecificValue) isNull(), isNull());
        thenLoadsKeyUsingPersistence();
        verify(persistence, times(3)).store(KEY, keyValues, "");
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        verify(keyValues).put(VALUE_1, DOMAIN_KEY_PART_1);
        verify(keyValues).put(VALUE_2, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        verify(keyValues).remove(isNull(), eq(DOMAIN_KEY_PART_1));
    }

    @Test
    public void doesNotCallPersistenceWhenNoDomainSpecificValueExists() {

        // when
        ropertyImpl.remove(KEY, DOMAIN_KEY_PART_1);

        // then
        verify(persistence, never()).remove(anyString(), any(DomainSpecificValue.class), anyString());
        thenLoadsKeyUsingPersistence();

    }

    @Test
    public void removesCompleteKey() {
        // given

        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.set(KEY, VALUE_2, DESCRIPTION, DOMAIN_KEY_PART_1);

        // when
        ropertyImpl.removeKey(KEY);

        // then
        assertThat(ropertyImpl.get(KEY, domainResolver), nullValue());
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        verify(keyValues).put(VALUE_2, DOMAIN_KEY_PART_1);
        verify(persistence).remove(eq(KEY), any(KeyValues.class), isNull());
        verify(persistence, times(2)).store(KEY, keyValues, "");
        verify(persistence, times(2)).load(KEY, keyValueFactory, domainSpecificValueFactory);
        thenCreatesKeyValues();
    }

    @Test
    public void callsPersistenceEvenWhenNoKeyExists() {

        // when
        ropertyImpl.removeKey(KEY);

        // then
        verify(persistence).remove(KEY, (KeyValues<DomainSpecificValue>)null, null);

    }

    @Test
    public void returnsValuesOfChangeSet() {

        // given


        // when
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.setWithChangeSet(KEY, VALUE_2, DESCRIPTION, CHANGE_SET);
        ropertyImpl.setWithChangeSet(OTHER_KEY_WITHOUT_SPACES, VALUE_3, DESCRIPTION, CHANGE_SET);

        // then
        verify(keyValueFactory, times(2)).create(domainSpecificValueFactory);
        thenPutsValue1WithoutDomains();
        verify(keyValues).putWithChangeSet(CHANGE_SET, VALUE_2);
        verify(keyValues).putWithChangeSet(CHANGE_SET, VALUE_3);
        verify(keyValues, times(2)).setDescription(DESCRIPTION);
        thenLoadsKeyUsingPersistence();
        verify(persistence).load(OTHER_KEY_WITHOUT_SPACES, keyValueFactory, domainSpecificValueFactory);
        thenStoresKeyWithoutChangeSet();
        verify(persistence).store(KEY, keyValues, CHANGE_SET);
        verify(persistence).store(OTHER_KEY_WITHOUT_SPACES, keyValues, CHANGE_SET);

    }

    @Test
    public void removesChangeSet() {
        // given


        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.setWithChangeSet(KEY, VALUE_2, DESCRIPTION, CHANGE_SET);

        // when
        ropertyImpl.removeChangeSet(CHANGE_SET);

        // then
        assertThat(ropertyImpl.get(KEY, domainResolver), is(VALUE_1));

        thenPutsValue1WithoutDomains();
        verify(keyValues).putWithChangeSet(CHANGE_SET, VALUE_2);
        thenSetsDescription();
        verify(keyValues).removeChangeSet(CHANGE_SET);
        verify(keyValues).get(any(Iterable.class), isNull(), eq(domainResolver));
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        verify(persistence).store(KEY, keyValues, CHANGE_SET);
    }

    @Test
    public void findKeysUsingPersistence() {
        String substring = "substring";
        ropertyImpl.findKeys(substring);
        verify(persistence).findKeys(substring);
    }

    @Test
    public void reloadReplacesKeyValuesMap() {

        // when
        ropertyImpl.reload();

        verify(persistence).reload(any(Map.class), eq(keyValueFactory), eq(domainSpecificValueFactory));

    }

    @Test(expected = NullPointerException.class)
    public void nullDomainsAreNotAllowed() {

        // when
        ropertyImpl.addDomains((String[])null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyDomainsAreNotAllowed() {

        // when
        ropertyImpl.addDomains("");

    }

    @Test
    public void reloadWithoutPersistenceDoesNothing() {

        // given
        given(persistence.reload(any(Map.class), eq(keyValueFactory), eq(domainSpecificValueFactory))).willReturn(singletonMap(KEY, keyValues));
        Map<String, KeyValues<DomainSpecificValue>> oldKeyValues = ropertyImpl.getKeyValues();

        // when
        ropertyImpl.reload();

        //then
        assertThat(ropertyImpl.getKeyValues(), hasEntry(KEY, this.keyValues));
        verify(persistence).reload(oldKeyValues, keyValueFactory, domainSpecificValueFactory);

    }

    @Test
    public void getKeyValues() {

        // given


        String key = KEY;
        ropertyImpl.set(key, VALUE_1, null);
        KeyValues<DomainSpecificValue> keyValues = ropertyImpl.getKeyValues(key);
        assertThat(keyValues, is(keyValues));
        verify(keyValues).put(VALUE_1);
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        verify(persistence).store(KEY, keyValues, "");

    }

    @Test
    public void getKeyValuesTrimsTheKey() {

        // given

        ropertyImpl.set(KEY, VALUE_1, null);
        assertThat(ropertyImpl.getKeyValues("  key"), is(keyValues));
        thenPutsValue1WithoutDomains();
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
    }

    @Test
    public void toStringReturnsEmptyDomains() {

        // when
        String actual = ropertyImpl.dump().toString();

        // then
        assertThat(actual, is("Roperty{domains=[]\n}"));

    }

    @Test
    public void toStringReturnsDomainNames() {

        // when
        ropertyImpl.addDomains(DOMAIN_NAME_1);

        // then
        assertThat(ropertyImpl.dump().toString(), is("Roperty{domains=[domainName1]\n}"));

    }

    @Test
    public void toStringFilledRoperty() {

        // given

        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2);
        ropertyImpl.set(KEY, VALUE_1, null);
        ropertyImpl.set(KEY, VALUE_2, null, DOMAIN_NAME_1);
        ropertyImpl.set(OTHER_KEY_WITH_SPACES, "otherValue", null); // keys are always trimmed

        // when
        String actual = ropertyImpl.dump().toString();

        // then
        assertThat(actual, containsString(""));
        assertThat(actual, containsString("Roperty{domains=[domainName1, domainName2]\n"));
        assertThat(actual, containsString("KeyValues for \"otherKey\": keyValues"));
        assertThat(actual, containsString("KeyValues for \"key\": keyValues"));

        verify(keyValueFactory, times(2)).create(domainSpecificValueFactory);
        thenLoadsKeyUsingPersistence();
        verify(persistence).load(OTHER_KEY_WITHOUT_SPACES, keyValueFactory, domainSpecificValueFactory);
        verify(persistence, times(2)).store(KEY, keyValues, "");
        verify(persistence).store(OTHER_KEY_WITHOUT_SPACES, keyValues, "");
        thenPutsValue1WithoutDomains();
        verify(keyValues).put("otherValue");
        verify(keyValues).put(VALUE_2, DOMAIN_NAME_1);

    }

    @Test
    public void dumpToStdout() throws UnsupportedEncodingException {

        // given

        ropertyImpl.addDomains(DOMAIN_NAME_1);
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ropertyImpl.dump(new PrintStream(os));
        String output = os.toString("UTF8");
        assertThat(output,
            is("Roperty{domains=[domainName1]\nKeyValues for \"key\": keyValues\n}\n"));
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
    }

    @Test
    public void iterate() {

        // given

        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        Map<String, KeyValues<DomainSpecificValue>> keyValuesMap = ropertyImpl.getKeyValues();
        assertThat(keyValuesMap.size(), is(1));
        assertThat(keyValuesMap.containsKey(KEY), is(true));
        assertThat(keyValuesMap.get(KEY), is(keyValues));
        thenPutsValue1WithoutDomains();
        thenSetsDescription();
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
    }

    @Test
    public void domainResolverToNullIsIgnored() {

        // given
        given(keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2, DOMAIN_NAME_3), null, domainResolver)).willReturn("mockValue");


        ropertyImpl.addDomains(DOMAIN_NAME_1, DOMAIN_NAME_2, DOMAIN_NAME_3);

        // when
        ropertyImpl.get(KEY, domainResolver);
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.set(KEY, VALUE_2, DESCRIPTION, DOMAIN_KEY_PART_1);
        ropertyImpl.set(KEY, VALUE_3, DESCRIPTION, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        ropertyImpl.set(KEY, VALUE_4, DESCRIPTION, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2, DOMAIN_KEY_PART_3);

        // then
        assertThat(ropertyImpl.get(KEY, domainResolver), is("mockValue"));

        thenCreatesKeyValues();
        verify(persistence, times(2)).load(KEY, keyValueFactory, domainSpecificValueFactory);
        verify(persistence, times(4)).store(KEY, keyValues, "");
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        verify(keyValues).put(VALUE_2, DOMAIN_KEY_PART_1);
        verify(keyValues).put(VALUE_3, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        verify(keyValues).put(VALUE_4, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2, DOMAIN_KEY_PART_3);
        thenGetsValueFromKeyValues();
    }

    @Test
    public void storesKeyWithChangeSet() throws Exception {

        // given


        // when
        ropertyImpl.setWithChangeSet(KEY, VALUE_1, DESCRIPTION, CHANGE_SET, DOMAIN_KEY_PART_1);

        // then
        thenCreatesKeyValues();
        verify(keyValues).putWithChangeSet(CHANGE_SET, VALUE_1, DOMAIN_KEY_PART_1);
        thenSetsDescription();
        thenLoadsKeyUsingPersistence();
        verify(persistence).store(KEY, keyValues, CHANGE_SET);

    }

    @Test
    public void removesKeyFromChangeSet() {

        // given
        given(persistence.load(KEY, keyValueFactory, domainSpecificValueFactory)).willReturn(keyValues);
        given(keyValues.remove(CHANGE_SET, DOMAIN_KEY_PART_1)).willReturn(domainSpecificValue);

        // when
        ropertyImpl.removeWithChangeSet(KEY, CHANGE_SET, DOMAIN_KEY_PART_1);

        // then
        thenLoadsKeyUsingPersistence();
        verify(persistence).remove(KEY, domainSpecificValue, CHANGE_SET);
        verify(keyValues).remove(CHANGE_SET, DOMAIN_KEY_PART_1);

    }

    @Test
    public void usesKeyValueFactoryToCreateObjects() {

        // given


        // when
        ropertyImpl.getOrDefine(KEY, VALUE_1, domainResolver);

        // then
        thenCreatesKeyValues();
        verify(persistence, times(2)).load(KEY, keyValueFactory, domainSpecificValueFactory);
        thenStoresKeyWithoutChangeSet();
        thenPutsValue1WithoutDomains();

    }

    @Test
    public void whenChangeSetsAreActiveTheValuesForTheChangeSetAreReturned() {

        // given

        given(keyValues.get(any(Iterable.class), isNull(), eq(anotherDomainResolver))).willReturn(VALUE_2);


        // when
        ropertyImpl.set(KEY, VALUE_1, DESCRIPTION);
        ropertyImpl.setWithChangeSet(KEY, VALUE_2, DESCRIPTION, CHANGE_SET);

        // then
        assertThat(ropertyImpl.get(KEY, domainResolver), is(VALUE_1));
        assertThat(ropertyImpl.get(KEY, anotherDomainResolver), is(VALUE_2));
        thenCreatesKeyValues();
        thenLoadsKeyUsingPersistence();
        thenStoresKeyWithoutChangeSet();
        verify(persistence).store(KEY, keyValues, CHANGE_SET);
        thenSetsDescription();
        thenPutsValue1WithoutDomains();
        verify(keyValues).putWithChangeSet(CHANGE_SET, VALUE_2);
        thenGetsValueFromKeyValues();
        verify(keyValues).get(isA(Iterable.class), isNull(), eq(anotherDomainResolver));
    }

}
