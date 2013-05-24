package com.parship.roperty;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author mfinsterwalder
 * @since 2013-05-24 08:52
 */
public interface DomainInitializer {
	CopyOnWriteArrayList<String> getInitialDomains();
}
