package com.freiheit.superoperty.converter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-21 15:27
 */
public class LongConverterTest {

	private LongConverter converter = new LongConverter();

	@Test
	public void toStringTest() {
		assertThat(converter.toString(new Long(345)), is("345"));
	}

	@Test
	public void toObjectTest() {
		assertThat((Long)converter.toObject("363"), is(new Long(363)));
	}
}
