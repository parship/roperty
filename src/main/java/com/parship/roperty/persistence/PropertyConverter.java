package com.parship.roperty.persistence;

/**
 * @author mfinsterwalder
 * @since 2013-05-16 11:29
 */
public interface PropertyConverter<T> {
	T toObject(String value);
	String toString(T value);
	void setConfig(String configString);
}
