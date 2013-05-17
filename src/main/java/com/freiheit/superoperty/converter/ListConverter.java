package com.freiheit.superoperty.converter;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 15:58
 */
public class ListConverter extends AbstractPropertyConverter {

	/*package*/ String config;

	@Override
	public Object toObject(final String value) {
		return toEnum(value, config);
	}

	public static Object toEnum(final String value, String config) {
		try {
			Class<? extends Enum> aClass = (Class<? extends Enum>)Class.forName(config);
			return Enum.valueOf(aClass, value);
		} catch (ClassNotFoundException e) {
			e.printStackTrace(); // TODO implement
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
