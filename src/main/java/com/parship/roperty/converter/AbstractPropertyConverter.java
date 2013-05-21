package com.parship.roperty.converter;

import com.parship.roperty.persistence.PropertyConverter;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 10:52
 */
public abstract class AbstractPropertyConverter implements PropertyConverter {
	@Override
	public Object toObject(final String value) {
		return value;
	}

	@Override
	public String toString(final Object value) {
		return value.toString();
	}

	@Override
	public void setConfig(final String configString) {
		// ignore config by default
	}
}
