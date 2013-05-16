package com.freiheit.superoperty.converter;

import com.parship.roperty.persistence.PropertyConverter;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class CollectionConverter implements PropertyConverter {
	@Override
	public Object toObject(final String value) {
		String[] split = value.split(",");
		return Arrays.asList(split);
	}

	@Override
	public String toString(final Object value) {
		Collection<String> collection = (Collection<String>)value;
		StringBuilder builder = new StringBuilder();
		for (String s : collection) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(s);
		}
		return builder.toString();
	}
}
