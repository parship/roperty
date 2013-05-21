package com.parship.roperty.converter;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.parship.roperty.converter.MultipleSelectionListConverter;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 11:21
 */
public class MultipleSelectionListConverterTest {

	private MultipleSelectionListConverter converter = new MultipleSelectionListConverter();

	@Test
	public void getString() {
		String string = converter.toString(Arrays.asList(ListConverterTestEnum.VALUE1, ListConverterTestEnum.VALUE2));
		assertThat(string, is("VALUE1,VALUE2"));
	}

	@Test
	public void getObject() {
		converter.setConfig("com.parship.roperty.converter.ListConverterTestEnum$ListProvider");
		Collection<ListConverterTestEnum> col = (Collection<ListConverterTestEnum>)converter.toObject("VALUE1,VALUE2");
		assertThat(col, Matchers.contains(ListConverterTestEnum.VALUE1, ListConverterTestEnum.VALUE2));
	}
}
