package com.freiheit.superoperty.converter;

import com.parship.roperty.persistence.PropertyConverter;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 10:52
 */
public abstract class AbstractPropertyConverter implements PropertyConverter {
	@Override
	public void setConfig(final String configString) {
		// ignore config by default
	}
}
