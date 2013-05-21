package com.parship.roperty;

/**
 * @author mfinsterwalder
 * @since 2013-05-17 13:01
 */
public interface Persistence {
	/**
	 * This method can only be called once per roperty instance. Usualy domains are initialized here.
	 * @param roperty The roperty instance to initialize with data from persistent storage.
	 */
	void init(final Roperty roperty);
	void loadAll(final Roperty roperty);
	void load(final Roperty roperty, final String key);
}
