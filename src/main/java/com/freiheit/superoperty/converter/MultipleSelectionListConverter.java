package com.freiheit.superoperty.converter;

import com.parship.roperty.persistence.PropertyConverter;


/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:59
 */
public class MultipleSelectionListConverter implements PropertyConverter {
	@Override
	public Object toObject(final String value) {
		return value;
	}

	@Override
	public String toString(final Object value) {
		return value.toString();
	}
}
