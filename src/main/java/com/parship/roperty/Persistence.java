package com.parship.roperty;

/**
 * @author mfinsterwalder
 * @since 2013-05-17 13:01
 */
public interface Persistence {
	void loadAll(final Roperty roperty);
	void load(final Roperty roperty, final String key);
}
