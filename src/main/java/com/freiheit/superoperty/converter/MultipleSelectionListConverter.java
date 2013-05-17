package com.freiheit.superoperty.converter;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:59
 */
public class MultipleSelectionListConverter extends ListConverter {

	@Override
	public Object toObject(final String value) {
		String[] split = value.split(",");
		Collection<Object> result = new ArrayList<>();
		for (String s : split) {
			result.add(ListConverter.toEnum(s, config));
		}
		return result;
	}

	@Override
	public String toString(final Object value) {
		return CollectionConverter.buildString((Collection<Enum>)value);
	}
}
