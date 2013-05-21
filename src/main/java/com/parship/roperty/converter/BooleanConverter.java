package com.parship.roperty.converter;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class BooleanConverter extends AbstractPropertyConverter {
	@Override
	public Object toObject(final String value) {
		return "true".equals(value) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String toString(final Object value) {
		return ((Boolean) value) ? "true" : "false";
	}
}
