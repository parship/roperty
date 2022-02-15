package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

public interface Roperty {

    /**
     * Get a value for a given key from Roperty
     * @param key key to query
     * @param defaultValue defaultValue is returned, when no value for the key is found
     * @param resolver resolver to determine domain values to use during resolution
     * @param <T> type of the objects stored under the provided key
     * @return object retrieved from Roperty or defaultValue
     */
	<T> T get(String key, T defaultValue, DomainResolver resolver);

    /**
     * Get a value for a given key from Roperty.
     * Same as calling get(key, null, resolver);
     * @param key key to query
     * @param resolver resolver to determine domain values to use during resolution
     * @param <T> type of the objects stored under the provided key
     * @return object retrieved from Roperty or defaultValue
     */
	<T> T get(String key, DomainResolver resolver);

    /**
     * Get a value for a given key from Roperty. When no value is found in Roperty, the provided default is stored in Roperty.
     * Same as calling getOfDefine(key, defaultValue, resolver, null);
     * @param key key to query
     * @param defaultValue defaultValue is returned, when no value for the key is found
     * @param resolver resolver to determine domain values to use during resolution
     * @param <T> type of the objects stored under the provided key
     * @return object retrieved from Roperty or defaultValue
     */
	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver);

    /**
     * Get a value for a given key from Roperty. When no value is found in Roperty, the provided default is stored in Roperty
     * along with the provided description.
     * Same as calling get(key, null, resolver);
     * @param key key to query
     * @param defaultValue defaultValue is returned, when no value for the key is found
     * @param resolver resolver to determine domain values to use during resolution
     * @param <T> type of the objects stored under the provided key
     * @return object retrieved from Roperty or defaultValue
     */
	<T> T getOrDefine(String key, T defaultValue, DomainResolver resolver, String description);

	Roperty addDomains(String... domains);

	void set(String key, Object value, String description, String... domains);

	void setWithChangeSet(String key, Object value, String description, String changeSet, String... domains);

	void reload();

	StringBuilder dump();

	void dump(PrintStream out);

	KeyValues getKeyValues(String key);

    /**
     * Get all KeyValues stored in this Roperty instance.
     */
    Collection<KeyValues> getKeyValues();

    /**
     * Get those KeyValues stored in this Roperty instance, with only those DomainSpecificValues, where the provided resolver
     * domains match or are wildcarded. The Default value of the KeyValues object is always returned, when present.
     */
    Collection<KeyValues> getKeyValues(DomainResolver resolver);

    <T> Map<String, T> getAllMappings(DomainResolver resolver);

    void removeWithChangeSet(String key, String changeSet, String... domainValues);

	void remove(String key, String... domainValues);

	void removeKey(String key);

	void removeChangeSet(String changeSet);
}
