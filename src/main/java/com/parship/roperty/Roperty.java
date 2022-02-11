package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collection;

public interface Roperty {

	<T> T get(String key, T defaultValue, DomainResolver resolver);

	<T> T get(String key, DomainResolver resolver);

	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver);

	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver, String description);

	Roperty addDomains(String... domains);

	void set(String key, Object value, String description, String... domains);

	void setWithChangeSet(String key, Object value, String description, String changeSet, String... domains);

	void reload();

	StringBuilder dump();

	void dump(PrintStream out);

	KeyValues getKeyValues(String key);

    Collection<KeyValues> getKeyValues();

    Collection<KeyValues> getKeyValues(DomainResolver resolver);

    void removeWithChangeSet(String key, String changeSet, String... domainValues);

	void remove(String key, String... domainValues);

	void removeKey(String key);

	void removeChangeSet(String changeSet);
}
