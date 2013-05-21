package com.parship.roperty.converter;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class CollectionConverter extends AbstractPropertyConverter {
	@Override
	public Object toObject(final String value) {
		String[] split = value.split(",");
		return Arrays.asList(split);
	}

	@Override
	public String toString(final Object value) {
		return buildString((Collection<String>)value);
	}

	public static String buildString(final Collection<?> collection) {
		StringBuilder builder = new StringBuilder();
		for (Object s : collection) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(s);
		}
		return builder.toString();
	}
}
