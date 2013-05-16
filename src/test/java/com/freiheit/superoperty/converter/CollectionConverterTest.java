package com.freiheit.superoperty.converter;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 17:25
 */
public class CollectionConverterTest {

	private CollectionConverter converter = new CollectionConverter();

	@Test
	public void toObject() {
		Collection<String> actual = (Collection<String>)converter.toObject("a,b,c,d");
		assertThat(actual, contains("a", "b", "c", "d"));
	}

	@Test
	public void toStringTesten() {
		assertThat(converter.toString(Arrays.asList("a", "b", "c")), is("a,b,c"));
	}
}
