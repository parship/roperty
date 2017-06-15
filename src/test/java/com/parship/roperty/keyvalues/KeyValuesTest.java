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

package com.parship.roperty.keyvalues;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.parship.roperty.domainresolver.DomainResolver;
import com.parship.roperty.domainspecificvalue.DefaultDomainSpecificValueFactory;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.OrderedDomainPattern;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 22:32
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyValuesTest {

    private static final String DOMAIN_NAME_1 = "domainName1";
    private static final String DOMAIN_NAME_2 = "domainName2";
    private static final String DOMAIN_NAME_2_A = "domainName2a";
    private static final String DOMAIN_NAME_2_B = "domainName2b";
    private static final String DOMAIN_NAME_3 = "domainName3";
    private static final String DOMAIN_KEY_PART_1 = "domainKeyPart1";
    private static final String DOMAIN_KEY_PART_1_A = "domainKeyPart1a";
    private static final String DOMAIN_KEY_PART_1_B = "domainKeyPart1b";
    private static final String DOMAIN_KEY_PART_2 = "domainKeyPart2";
    private static final String DOMAIN_KEY_PART_2_A = "domainKeyPart2a";
    private static final String DOMAIN_KEY_PART_2_B = "domainKeyPart2b";
    private static final String DOMAIN_KEY_PART_3 = "domainKeyPart3";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String VALUE = "value";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    @Mock
    private DefaultDomainSpecificValueFactory domainSpecificValueFactory;

    @Mock
    private DomainResolver domainResolver;

    @Mock
    private DomainResolver anotherDomainResolver;

    private KeyValues<DomainSpecificValue> keyValues = new KeyValues<>(new DefaultDomainSpecificValueFactory());

    @Test
    public void descriptionIsNeverNullButIsTheEmptyString() {
        KeyValues<DomainSpecificValue> keyValues = new KeyValues<>(new DefaultDomainSpecificValueFactory());
        assertThat(keyValues.getDescription(), is(""));
    }

    @Test
    public void toStringEmpty() {
        assertThat(keyValues.toString(), CoreMatchers.is("KeyValues{\n\tdescription=\"\"\n" +
            "}"));
    }

    @Test
    public void toStringFilled() {
        keyValues.setDescription("description");
        keyValues.put("text", "domain1", "domain2");
        assertThat(keyValues.toString(), CoreMatchers.is("KeyValues{\n" +
            "\tdescription=\"description\"\n" +
            "\tDomainSpecificValue{pattern=\"domain1|domain2|\", ordering=7, value=\"text\"}\n" +
            "}"));
    }

    @Test
    public void gettingFromAnEmptyKeyValuesGivesNull() {
        assertThat(keyValues.get(singletonList("dom1"), null, domainResolver), nullValue());
    }

    @Test
    public void whenNoPatternMatchesTheDefaultValueIsReturned() {
        keyValues.put(VALUE, "domain");
        assertThat(keyValues.get(singletonList("x1"), "default", domainResolver), is("default"));
    }

    @Test
    public void whenAPatternMatchesItIsReturnedAndNotTheDefault() {
        keyValues.put("text");
        assertThat(keyValues.get(singletonList("x1"), "default", domainResolver), is("text"));
    }

    @Test
    public void whenNoPatternMatchesItIsReturnedAndNotTheDefault() {
        //given
        given(domainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1);

        // when
        keyValues.put(VALUE, DOMAIN_KEY_PART_1);

        //then
        assertThat(keyValues.get(singletonList(DOMAIN_NAME_1), DEFAULT_VALUE, domainResolver), is(VALUE));
    }

    @Test
    public void whenNoValuesAreDefinedGettingAllDomainSpecificValuesGivesAnEmptySet() {
        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues, hasSize(0));
    }

    @Test
    public void gettingAllDomainSpecificValuesGivesSetInLongestMatchFirstOrder() {
        keyValues.put(VALUE_1, "dom1");
        keyValues.put(VALUE_2, "dom1", "dom2");
        keyValues.put("value*", "*", "dom2");
        Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues, hasSize(3));
        Iterator<DomainSpecificValue> iterator = domainSpecificValues.iterator();
        DomainSpecificValue value = iterator.next();
        assertThat(value.getValue(), is(VALUE_2));
        assertThat(value.getPatternStr(), is("dom1|dom2|"));
        value = iterator.next();
        assertThat(value.getValue(), is("value*"));
        assertThat(value.getPatternStr(), is("*|dom2|"));
        value = iterator.next();
        assertThat(value.getValue(), is(VALUE_1));
        assertThat(value.getPatternStr(), is("dom1|"));
    }

    @Test
    public void wildcardsAlsoMatchNullDomainValues() {
        given(domainResolver.getDomainKey("dom3")).willReturn("domVal3");
        keyValues.put(VALUE, "*", "*", "domVal3");
        assertThat(keyValues.get(asList("dom1", "dom2", "dom3"), "default", domainResolver), is(VALUE));
    }

    @Test
    public void callingGetWithAnEmtpyDomainListDoesNotUseTheResolver() {
        assertThat(keyValues.<String>get(Collections.<String>emptyList(), null, null), nullValue());
        keyValues.put("val");
        assertThat(keyValues.<String>get(Collections.<String>emptyList(), null, null), is("val"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void callingGetWithDomainsButWithoutAResolverGivesNullPointerException() {
        keyValues.get(asList("dom1", "dom2"), null, null);
    }

    @Test
    public void returnsAWildcardOverriddenValueByBestMatch() {
        //given
        given(domainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1);
        given(domainResolver.getDomainKey(DOMAIN_NAME_2)).willReturn(DOMAIN_KEY_PART_2);
        given(domainResolver.getDomainKey(DOMAIN_NAME_3)).willReturn(DOMAIN_KEY_PART_3);

        // when
        keyValues.put(VALUE_1, "*", "*", DOMAIN_KEY_PART_3);
        keyValues.put(VALUE_2, DOMAIN_KEY_PART_1, "*", DOMAIN_KEY_PART_3);

        // then
        assertThat(keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2, DOMAIN_NAME_3), null, domainResolver), is(VALUE_2));
    }

    @Test
    public void returnsAWildcardOverriddenValueWhenAllDomainsMatch() {
        //given
        given(domainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1_B);
        given(domainResolver.getDomainKey(DOMAIN_NAME_2)).willReturn(DOMAIN_KEY_PART_2);
        given(domainResolver.getDomainKey(DOMAIN_NAME_3)).willReturn(DOMAIN_KEY_PART_3);

        // when
        keyValues.put(VALUE_1, DOMAIN_KEY_PART_1_A, "*", DOMAIN_KEY_PART_3);
        keyValues.put(VALUE_2, DOMAIN_KEY_PART_1_B, "*", DOMAIN_KEY_PART_3);

        // then
        assertThat(keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2, DOMAIN_NAME_3), null, domainResolver), is(VALUE_2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void domainValuesMustNotContainPipe() {
        given(domainResolver.getDomainKey("x1")).willReturn("abc|def");
        keyValues.get(singletonList("x1"), null, domainResolver);
    }

    @Test
    public void resolvingToNullMatchesEmptyStringAndThatNeverMatchesSoTheRestOfTheDomainsAreIgnored() {
        given(domainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1);
        given(domainResolver.getDomainKey(DOMAIN_NAME_2)).willReturn(null);
        given(domainResolver.getDomainKey(DOMAIN_NAME_3)).willReturn(DOMAIN_KEY_PART_3);
        keyValues.put(VALUE);
        keyValues.put("overridden1", DOMAIN_KEY_PART_1);
        keyValues.put("overridden2", DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2);
        keyValues.put("overridden3", DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2, DOMAIN_KEY_PART_3);
        String value = keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2, DOMAIN_NAME_3), null, domainResolver);
        assertThat(value, is("overridden1"));
    }

    @Test
    public void getDefaultValue() {
        assertThat(keyValues.getDefaultValue(), nullValue());
        keyValues.put("default");
        keyValues.put("other", "domain");
        assertThat(keyValues.getDefaultValue(), is("default"));
    }

    @Test
    public void newValuesAreCreatedThroughTheSuppliedFactory() {
        keyValues.setDomainSpecificValueFactory(domainSpecificValueFactory);
        String value = VALUE;
        given(domainSpecificValueFactory.create(value, null)).willReturn(new DomainSpecificValue(new OrderedDomainPattern("", 1), value));
        keyValues.put(value);
        verify(domainSpecificValueFactory).create(value, null);
    }

    @Test
    public void domainsWithTheSameDomainKeyReturnTheCorrectValue() {
        // given
        given(domainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1);
        given(domainResolver.getDomainKey(DOMAIN_NAME_2_A)).willReturn(DOMAIN_KEY_PART_2_A);
        given(anotherDomainResolver.getDomainKey(DOMAIN_NAME_1)).willReturn(DOMAIN_KEY_PART_1);
        given(anotherDomainResolver.getDomainKey(DOMAIN_NAME_2_B)).willReturn(DOMAIN_KEY_PART_2_B);

        // when
        keyValues.put(VALUE_1, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2_A);
        keyValues.put(VALUE_2, DOMAIN_KEY_PART_1, DOMAIN_KEY_PART_2_B);

        // then
        assertThat(keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2_A), null, domainResolver), is(VALUE_1));
        assertThat(keyValues.get(asList(DOMAIN_NAME_1, DOMAIN_NAME_2_B), null, anotherDomainResolver), is(VALUE_2));
    }

}
