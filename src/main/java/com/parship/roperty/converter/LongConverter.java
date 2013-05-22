package com.parship.roperty.converter;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:59
 */
public class LongConverter extends AbstractPropertyConverter<Long> {
	@Override
	public Long toObject(final String value) {
		return Long.valueOf(value);
	}

	@Override
	public String toString(final Long value) {
		return value.toString();
	}
}
