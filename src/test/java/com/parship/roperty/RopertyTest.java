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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyTest {

	private DomainResolver resolver = new DomainResolver() {
		@Override
		public String getDomainValue(final String domain) {
			return domain;
		}
	};
	private Roperty r = new Roperty();
	private RopertyWithResolver roperty = new RopertyWithResolver(r, resolver);

	@Test
	public void toStringTest() {
		assertThat(roperty.toString(), startsWith("RopertyWithResolver{roperty=Roperty{domains=[], map={}}, domainResolver="));
	}

	@Test
	public void gettingAPropertyThatDoesNotExistGivesNull() {
		String value = roperty.get("key");
		assertThat(value, nullValue());
	}

	@Test
	public void gettingAPropertyThatDoesNotExistGivesDefaultValue() {
		String text = "default";
		String value = roperty.get("key", text);
		assertThat(value, is(text));
	}

	@Test
	public void definingAndGettingAStringValue() {
		String text = "some Value";
		roperty.set("key", text);
		String value = roperty.get("key", "default");
		assertThat(value, is(text));
	}

	@Test
	public void gettingAValueWithoutAGivenDefaultGivesValue() {
		String text = "value";
		roperty.set("key", text);
		String value = roperty.get("key");
		assertThat(value, is(text));
	}

	@Test
	public void changingAStringValue() {
		roperty.set("key", "first");
		roperty.set("key", "other");
		String value = roperty.get("key", "default");
		assertThat(value, is("other"));
	}

	@Test
	public void gettingAnIntValueDefault() {
		int value = roperty.get("key", 3);
		assertThat(value, is(3));
	}

	@Test
	public void settingAndGettingAnIntValue() {
		roperty.set("key", 7);
		int value = roperty.get("key", 3);
		assertThat(value, is(7));
	}

	@Test(expected = ClassCastException.class)
	public void gettingAValueThatHasADifferentTypeGivesAClassCastException() {
		String text = "value";
		roperty.set("key", text);
		@SuppressWarnings("unused")
		Integer value = roperty.get("key");
	}

	@Test
	public void getOrDefineSetsAValueWithTheGivenDefault() {
		String text = "text";
		String value = roperty.getOrDefine("key", text);
		assertThat(value, is(text));
		value = roperty.getOrDefine("key", "other default");
		assertThat(value, is(text));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullDomainsAreNotAllowed() {
		r.addDomain(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyDomainsAreNotAllowed() {
		r.addDomain("");
	}

	@Test
	public void getOverriddenValue() {
		r.addDomain("domain1");
		roperty = new RopertyWithResolver(r, resolver);
		String defaultValue = "default value";
		String overriddenValue = "overridden value";
		roperty.set("key", defaultValue);
		roperty.set("key", overriddenValue, "domain1");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void whenAKeyForASubdomainIsSetTheRootKeyGetsAnUndefinedValue() {
		roperty.set("key", "value", "subdomain");
		assertThat((String)roperty.get("key"), is("[value undefined]"));
	}

	@Test
	public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist() {
		r.addDomain("domain1");
		roperty = new RopertyWithResolver(r, resolver);
		String overriddenValue = "overridden value";
		roperty.set("key", "other value", "other");
		roperty.set("key", overriddenValue, "domain1");
		roperty.set("key", "yet another value", "yet another");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExistWithTwoDomains() {
		r.addDomain("domain1").addDomain("domain2");
		DomainResolver mockResolver = mock(DomainResolver.class);
		when(mockResolver.getDomainValue("domain1")).thenReturn("domVal1");
		when(mockResolver.getDomainValue("domain2")).thenReturn("domVal2");
		roperty = new RopertyWithResolver(r, mockResolver);
		String overriddenValue = "overridden value";
		roperty.set("key", "other value", "other");
		roperty.set("key", "domVal1", "domVal1");
		roperty.set("key", overriddenValue, "domVal1", "domVal2");
		roperty.set("key", "yet another value", "domVal1", "other");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden() {
		r.addDomain("domain1").addDomain("domain2");
		roperty = new RopertyWithResolver(r, resolver);
		String defaultValue = "default value";
		String overriddenValue1 = "overridden value domain1";
		roperty.set("key", defaultValue);
		roperty.set("key", overriddenValue1, "domain1");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue1));
	}

	@Test
	public void domainValuesAreRequestedFromAResolver() {
		roperty.getRoperty().addDomain("domain1").addDomain("domain2");
		DomainResolver mockResolver = mock(DomainResolver.class);
		roperty = new RopertyWithResolver(r, mockResolver);
		roperty.set("key", "value");
		roperty.get("key");
		verify(mockResolver).getDomainValue("domain1");
		verify(mockResolver).getDomainValue("domain2");
		verifyNoMoreInteractions(mockResolver);
	}

	@Test
	public void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
		r.addDomain("domain1").addDomain("domain2");
		DomainResolver mockResolver = mock(DomainResolver.class);
		roperty = new RopertyWithResolver(r, mockResolver);
		roperty.get("key");
		verifyNoMoreInteractions(mockResolver);
	}

	@Test
	public void wildcardIsResolvedWhenOtherDomainsMatch() {
		r.addDomain("domain1").addDomain("domain2");
		roperty = new RopertyWithResolver(r, resolver);
		String value = "overridden value";
		roperty.set("key", value, "*", "domain2");
		assertThat((String)roperty.get("key"), is(value));
	}
}
