package com.parship.roperty.converter;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class BooleanConverter extends AbstractPropertyConverter<Boolean> {
	@Override
	public Boolean toObject(final String value) {
		return "true".equals(value) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String toString(final Boolean value) {
		return value ? "true" : "false";
	}
}
