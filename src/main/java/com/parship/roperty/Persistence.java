package com.parship.roperty;

/**
 * @author mfinsterwalder
 * @since 2013-05-17 13:01
 */
public interface Persistence {
//	Map<String, KeyValues> loadAll();
	KeyValues load(final String key);
	void store(final KeyValues keyValues);
}
