package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
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
    <T> T getOrDefault(String key, T defaultValue, DomainResolver resolver);
    <T> T getOrDefault(String key, T defaultValue, String... domainValues);

    /**
     * Get a value for a given key from Roperty.
     * Same as calling get(key, null, resolver);
     * @param key key to query
     * @param resolver resolver to determine domain values to use during resolution
     * @param <T> type of the objects stored under the provided key
     * @return object retrieved from Roperty or defaultValue
     */
	<T> T get(String key, DomainResolver resolver);
    <T> T get(String key, String... domainValues);

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
     * @param <T> type of the objects stored under the provided key
     * @param key key to query
     * @param defaultValue defaultValue is returned, when no value for the key is found
     * @param resolver resolver to determine domain values to use during resolution
     * @return object retrieved from Roperty or defaultValue
     */
    <T> T getOrDefine(String key, T defaultValue, String description, DomainResolver resolver);
    <T> T getOrDefine(String key, T defaultValue, String description, String... domainValues);

	Roperty addDomains(String... domains);

	void set(String key, Object value, String description, String... domains);

	void setWithChangeSet(String key, Object value, String description, String changeSet, String... domainValues);

	void reload();

	StringBuilder dump();

	void dump(PrintStream out);

	KeyValues getKeyValues(String key);

    /**
     * Get all KeyValues stored in this Roperty instance.
     */
    Collection<KeyValues> getAllKeyValues();

    /**
     * Get those KeyValues stored in this Roperty instance, with only those DomainSpecificValues, where the provided resolver
     * domains match or are wildcarded. The Default value of the KeyValues object is always returned, when present.
     */
    Collection<KeyValues> getAllKeyValues(DomainResolver resolver);
    Collection<KeyValues> getAllKeyValues(String... domainValues);

    <T> Map<String, T> getAllMappings(DomainResolver resolver);
    <T> Map<String, T> getAllMappings(String... domainValues);

    void removeWithChangeSet(String key, String changeSet, String... domainValues);

	void remove(String key, String... domainValues);

	void removeKey(String key);

	void removeChangeSet(String changeSet);

    DomainResolver resolverFor(String... domainValues);

    List<String> getDomains();
}
