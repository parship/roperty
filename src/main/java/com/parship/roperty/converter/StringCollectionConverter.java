package com.parship.roperty.converter;

import java.util.Arrays;
import java.util.Collection;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class StringCollectionConverter extends AbstractPropertyConverter<Collection<String>> {
	@Override
	public Collection<String> toObject(final String value) {
		String[] split = value.split(",");
		return Arrays.asList(split);
	}

	@Override
	public String toString(final Collection<String> value) {
		return buildString(value);
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
