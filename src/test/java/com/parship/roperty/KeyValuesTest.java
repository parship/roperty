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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

/**
 * @author mfinsterwalder
 * @since 2013-04-02 22:32
 */
public class KeyValuesTest {

	private final KeyValues keyValues = new KeyValues("key", new DefaultDomainSpecificValueFactory(), null);

	private DomainResolver resolver = new DomainResolver() {
		@Override
		public String getDomainValue(final String domain) {
			return domain;
		}

		@Override
		public Collection<String> getActiveChangeSets() {
			return new ArrayList<>();
		}
	};

	@Test
	public void descriptionIsNeverNullButIsTheEmptyString() {
		KeyValues keyValues = new KeyValues("key", new DefaultDomainSpecificValueFactory(), null);
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
		assertThat(keyValues.get(Collections.singletonList("dom1"), null, resolver), nullValue());
	}

	@Test
	public void whenNoPatternMatchesTheDefaultValueIsReturned() {
		keyValues.put("value", "domain");
		assertThat(keyValues.get(Collections.singletonList("x1"), "default", resolver), is("default"));
	}

	@Test
	public void whenAPatternMatchesItIsReturnedAndNotTheDefault() {
		keyValues.put("text");
		assertThat(keyValues.get(Collections.singletonList("x1"), "default", resolver), is("text"));
	}

	@Test
	public void whenNoPatternMatchesItIsReturnedAndNotTheDefault() {
		keyValues.put("text", "domain");
		assertThat(keyValues.get(Collections.singletonList("domain"), "default", resolver), is("text"));
	}

	@Test
	public void whenNoValuesAreDefinedGettingAllDomainSpecificValuesGivesAnEmptySet() {
		Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
		assertThat(domainSpecificValues, hasSize(0));
	}

	@Test
	public void gettingAllDomainSpecificValuesGivesSetInLongestMatchFirstOrder() {
		keyValues.put("value1", "dom1");
		keyValues.put("value2", "dom1", "dom2");
		keyValues.put("value*", "*", "dom2");
		Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
		assertThat(domainSpecificValues, hasSize(3));
		Iterator<DomainSpecificValue> iterator = domainSpecificValues.iterator();
		DomainSpecificValue value = iterator.next();
		assertThat((String)value.getValue(), is("value2"));
		assertThat(value.getPatternStr(), is("dom1|dom2|"));
		value = iterator.next();
		assertThat((String)value.getValue(), is("value*"));
		assertThat(value.getPatternStr(), is("*|dom2|"));
		value = iterator.next();
		assertThat((String)value.getValue(), is("value1"));
		assertThat(value.getPatternStr(), is("dom1|"));
	}

	@Test
	public void wildcardsAlsoMatchNullDomainValues() {
		DomainResolver resolverMock = mock(DomainResolver.class);
		when(resolverMock.getDomainValue("dom3")).thenReturn("domVal3");
		keyValues.put("value", "*", "*", "domVal3");
		assertThat(keyValues.get(asList("dom1", "dom2", "dom3"), "default", resolverMock), is("value"));
	}

	@Test
	public void callingGetWithAnEmptyDomainListDoesNotUseTheResolver() {
		assertThat(keyValues.<String>get(Collections.emptyList(), null, null), nullValue());
		keyValues.put("val");
		assertThat(keyValues.get(Collections.emptyList(), null, null), is("val"));
	}

	@Test
	public void callingGetWithDomainsButWithoutAResolverGivesNullPointerException() {
		assertThrows(IllegalArgumentException.class, () -> keyValues.get(asList("dom1", "dom2"), null, null));
	}

	@Test
	public void getAWildcardOverriddenValueIsReturnedByBestMatch() {
		keyValues.put("value_1", "*", "*", "domain3");
		keyValues.put("value_2", "domain1", "*", "domain3");
		assertThat(keyValues.get(asList("domain1", "domain2", "domain3"), null, resolver), is("value_2"));
	}

	@Test
	public void getAWildcardOverriddenValueIsReturnedWhenAllDomainsMatch() {
		keyValues.put("other value", "aaa", "*", "domain3");
		keyValues.put("value", "domain1", "*", "domain3");
		assertThat(keyValues.get(asList("domain1", "domain2", "domain3"), null, resolver), is("value"));
	}

	@Test
	public void domainValuesMustNotContainPipe() {
		DomainResolver resolverMock = mock(DomainResolver.class);
		when(resolverMock.getDomainValue("x1")).thenReturn("abc|def");
		assertThrows(IllegalArgumentException.class, () -> keyValues.get(Collections.singletonList("x1"), null, resolverMock));
	}

	@Test
	public void resolvingToNullMatchesEmptyStringAndThatNeverMatchesSoTheRestOfTheDomainsAreIgnored() {
		resolver = mock(DomainResolver.class);
		when(resolver.getDomainValue("domain1")).thenReturn("domain1");
		when(resolver.getDomainValue("domain2")).thenReturn(null);
		when(resolver.getDomainValue("domain3")).thenReturn("domain3");
		keyValues.put("value");
		keyValues.put("overridden1", "domain1");
		keyValues.put("overridden2", "domain1", "domain2");
		keyValues.put("overridden3", "domain1", "domain2", "domain3");
		String value = keyValues.get(asList("domain1", "domain2", "domain3"), null, resolver);
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
		DefaultDomainSpecificValueFactory factoryMock = mock(DefaultDomainSpecificValueFactory.class);
		keyValues.setDomainSpecificValueFactory(factoryMock);
		String value = "value";
		when(factoryMock.create(value, null)).thenReturn(new DomainSpecificValue(new OrderedDomainPattern("", 1), value));
		keyValues.put(value);
		verify(factoryMock).create(value, null);
	}

	@Test
	public void domainsWithTheSamePrefixReturnTheCorrectValue() {
		keyValues.put("valuePrefix", "dom1", "prefix");
		keyValues.put("value1", "dom1", "prefixDom2");
		assertThat(keyValues.get(asList("dom1", "prefix"), null, resolver), is("valuePrefix"));
		assertThat(keyValues.get(asList("dom1", "prefixDom2"), null, resolver), is("value1"));
	}
}
