package com.parship.roperty.jmx;

/**
 * @author mfinsterwalder
 * @since 2013-05-28 12:08
 */
public interface RopertyJmxMBean {
	void dumpToSystemOut();
	String dump();
	String dump(String key);
	void reload();
}
