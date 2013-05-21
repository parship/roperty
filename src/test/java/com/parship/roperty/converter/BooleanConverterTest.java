package com.parship.roperty.converter;

import org.junit.Test;

import com.parship.roperty.converter.BooleanConverter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-21 15:31
 */
public class BooleanConverterTest {

	private BooleanConverter converter = new BooleanConverter();

	@Test
	public void toStringTest() {
		assertThat(converter.toString(true), is("true"));
		assertThat(converter.toString(false), is("false"));
	}

	@Test
	public void toObjectTest() {
		assertThat((Boolean)converter.toObject("true"), is(true));
		assertThat((Boolean)converter.toObject("false"), is(false));
	}
}
