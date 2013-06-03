package com.parship.roperty;

/**
 * @author mfinsterwalder
 * @since 2013-06-03 17:48
 */
public interface FactoryProvider {
	KeyValuesFactory getKeyValuesFactory();
	DomainSpecificValueFactory getDomainSpecificValueFactory();
}
