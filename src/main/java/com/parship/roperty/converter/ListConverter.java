package com.parship.roperty.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class ListConverter extends AbstractPropertyConverter<Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListConverter.class);
	/*package*/ String config;

	@Override
	public Object toObject(final String value) {
		return toEnum(value, config);
	}

	public static <T extends Enum<T>> Object toEnum(final String value, String config) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> aClass = (Class<T>)Class.forName(config);
			return Enum.valueOf(aClass, value);
		} catch (ClassNotFoundException e) {
			LOGGER.error("could not convert value, because the Enum was not found: " + config, e);
			return null;
		}
	}

	@Override
	public String toString(final Object value) {
		return value.toString();
	}

	@Override
	public void setConfig(final String string) {
		this.config = string.substring(0, string.indexOf('$'));
	}
}
