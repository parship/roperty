package com.parship.roperty.persistence;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 11:29
 */
public interface PropertyConverter {
	Object toObject(String value);
	String toString(Object value);
	void setConfig(String configString);
}
