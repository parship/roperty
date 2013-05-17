package com.freiheit.superoperty.converter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 11:05
 */
public class ListConverterTest {

	private ListConverter converter = new ListConverter();

	@Test
	public void testConfig() {
		converter.setConfig("com.freiheit.parship.common.model.InstanceOrientation$ListProvider");
		assertThat(converter.config, is("com.freiheit.parship.common.model.InstanceOrientation"));
	}

	@Test
	public void getEnum() {
		converter.setConfig("com.freiheit.superoperty.converter.ListConverterTestEnum$ListProvider");
		assertThat((ListConverterTestEnum)converter.toObject("VALUE1"), is(ListConverterTestEnum.VALUE1));
		assertThat((ListConverterTestEnum)converter.toObject("VALUE2"), is(ListConverterTestEnum.VALUE2));
	}


	@Test
	public void getString() {
		converter.setConfig("com.freiheit.superoperty.converter.ListConverterTestEnum$ListProvider");
		assertThat(converter.toString(ListConverterTestEnum.VALUE1), is("VALUE1"));
		assertThat(converter.toString(ListConverterTestEnum.VALUE2), is("VALUE2"));
	}
}
