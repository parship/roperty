package com.parship.roperty;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-15 15:26
 */
public class MapBackedDomainResolverTest {

	@Test
	public void setAndGetDomainValues() {
		MapBackedDomainResolver resolver = new MapBackedDomainResolver().set("dom1", "val1").set("dom2", "val2");
		assertThat(resolver.getDomainValue("dom1"), is("val1"));
	}
}
