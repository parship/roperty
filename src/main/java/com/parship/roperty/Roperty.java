package com.parship.roperty;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import com.parship.roperty.domainresolver.DomainResolver;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.keyvalues.KeyValues;


public interface Roperty<D extends DomainSpecificValue, K extends KeyValues<D>> {

	<T> T get(String key, T defaultValue, DomainResolver resolver);

	<T> T get(String key, DomainResolver resolver);

	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver);

	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver, String description);

	Roperty<D, K> addDomains(String... domains);

	void set(String key, Object value, String description, String... domains);

	void setWithChangeSet(String key, Object value, String description, String changeSet, String... domains);

	void setKeyValuesMap(Map<String, K> keyValuesMap);

	void reload();

	StringBuilder dump();

	void dump(PrintStream out);

	K getKeyValues(String key);

	Map<String, K> getKeyValues();

	void removeWithChangeSet(String key, String changeSet, String... domainValues);

	void remove(String key, String... domainValues);

	void removeKey(String key);

	void removeChangeSet(String changeSet);

    List<String> findKeys(String substring);

}
