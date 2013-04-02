package com.parship.roperty;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyTest {

	private Roperty roperty = new Roperty();
	private Resolver resolver = new Resolver() {
		@Override
		public String getDomainValue(final String domain) {
			return domain;
		}
	};

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
	public void changingAStringValue() {
		roperty.set("key", "first");
		roperty.set("key", "other");
		String value = roperty.get("key", "default");
		assertThat(value, is("other"));
	}

	@Test
	public void gettingAnIntValue() {
		int value = roperty.get("key", 3);
		assertThat(value, is(3));
	}

	@Test
	public void gettingAValueThatDoesNotExistWithoutADefaultGivesNull() {
		Integer value = roperty.get("key");
		assertThat(value, nullValue());
	}

	@Test
	public void gettingAValueWithoutAGivenDefaultGivesValue() {
		String text = "value";
		roperty.set("key", text);
		String value = roperty.get("key");
		assertThat(value, is(text));
	}


	@Test(expected = ClassCastException.class)
	public void gettingAValueThatHasADifferentTypeGivesAClassCastException() {
		String text = "value";
		roperty.set("key", text);
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

	@Test
	public void getOverriddenValue() {
		roperty.addDomain("domain1");
		roperty.setResolver(resolver);
		String defaultValue = "default value";
		String overriddenValue = "overridden value";
		roperty.set("key", defaultValue);
		roperty.set("key", overriddenValue, "domain1");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist() {
		roperty.addDomain("domain1");
		roperty.setResolver(resolver);
		String overriddenValue = "overridden value";
		roperty.set("key", "other value", "other");
		roperty.set("key", overriddenValue, "domain1");
		roperty.set("key", "yet another value", "yet another");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void whenAKeyForASubdomainIsSetTheRootKeyGetsAnUndefinedValue() {
		roperty.set("key", "value", "subdomain");
		assertThat((String)roperty.get("key"), is("[value undefined]"));
	}

	@Test
	public void theCorrectValueIsSelectedWhenAlternativeOverriddenValuesExist_2() {
		roperty.addDomain("domain1").addDomain("domain2");
		Resolver mockResolver = mock(Resolver.class);
		when(mockResolver.getDomainValue("domain1")).thenReturn("domVal1");
		when(mockResolver.getDomainValue("domain2")).thenReturn("domVal2");
		roperty.setResolver(mockResolver);
		String overriddenValue = "overridden value";
		roperty.set("key", "other value", "other");
		roperty.set("key", "domVal1", "domVal1");
		roperty.set("key", overriddenValue, "domVal1", "domVal2");
		roperty.set("key", "yet another value", "domVal1", "other");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue));
	}

	@Test
	public void getOverriddenValueTwoDomains() {
		roperty.addDomain("domain1").addDomain("domain2");
		roperty.setResolver(resolver);
		String defaultValue = "default value";
		String overriddenValue1 = "overridden value domain1";
		String overriddenValue2 = "overridden value domain2";
		roperty.set("key", defaultValue);
		roperty.set("key", overriddenValue1, "domain1");
		roperty.set("key", overriddenValue2, "domain1|domain2");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue2));
	}

	@Test
	public void getOverriddenValueTwoDomainsOnlyFirstDomainIsOverridden() {
		roperty.addDomain("domain1").addDomain("domain2");
		roperty.setResolver(resolver);
		String defaultValue = "default value";
		String overriddenValue1 = "overridden value domain1";
		roperty.set("key", defaultValue);
		roperty.set("key", overriddenValue1, "domain1");
		String value = roperty.get("key");
		assertThat(value, is(overriddenValue1));
	}

	@Test
	public void domainValuesAreRequestedFromAResolver() {
		roperty.addDomain("domain1").addDomain("domain2");
		Resolver mockResolver = mock(Resolver.class);
		roperty.setResolver(mockResolver);
		roperty.set("key", "value");
		roperty.get("key");
		verify(mockResolver).getDomainValue("domain1");
		verify(mockResolver).getDomainValue("domain2");
		verifyNoMoreInteractions(mockResolver);
	}

	@Test
	public void noDomainValuesAreRequestedWhenAKeyDoesNotExist() {
		roperty.addDomain("domain1").addDomain("domain2");
		Resolver mockResolver = mock(Resolver.class);
		roperty.setResolver(mockResolver);
		roperty.get("key");
		verifyNoMoreInteractions(mockResolver);
	}

	@Ignore
	@Test
	public void wildcard() {
		roperty.addDomain("domain1").addDomain("domain2");
		roperty.setResolver(resolver);
		String value = "overridden value";
		roperty.set("key", value, "*", "domain2");
		assertThat((String)roperty.get("key"), is(value));
	}
}
