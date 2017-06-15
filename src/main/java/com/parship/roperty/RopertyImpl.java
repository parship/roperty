package com.parship.roperty;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.parship.roperty.domainresolver.DomainResolver;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.jmx.RopertyManagerFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;


/**
 * The central class to access Roperty.
 * Manages domains and key-to-value mappings.
 *
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyImpl<D extends DomainSpecificValue, K extends KeyValues<D>> implements Roperty<D, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RopertyImpl.class);

    private volatile ValuesStore<D, K> valuesStore = new ValuesStore<>();
    private List<String> domains = new CopyOnWriteArrayList<>();
    private Persistence<D, K> persistence;
    private Map<String, Collection<String>> changeSets = new ConcurrentHashMap<>();

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#get(java.lang.String, T, com.parship.roperty.DomainResolver)
	 */
    @Override
    public <T> T get(String key, T defaultValue, DomainResolver resolver) {
        String trimmedKey = trimKey(key);
        K keyValues = valuesStore.getKeyValuesFromMapOrPersistence(trimmedKey);
        T result;
        if (keyValues == null) {
            result = defaultValue;
        } else {
            result = keyValues.get(domains, defaultValue, resolver);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting value for key: '{}' with given default: '{}'. Returning value: '{}'", trimmedKey, defaultValue, result);
            StringBuilder builder = new StringBuilder("DomainValues: ");
            for (String domain : domains) {
                builder.append(domain).append(" => ").append(resolver.getDomainKey(domain)).append("; ");
            }
            LOGGER.debug(builder.toString());
        }
        return result;
    }

    private static String trimKey(String key) {
        Ensure.notEmpty(key, "key");
        return key.trim();
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#get(java.lang.String, com.parship.roperty.DomainResolver)
	 */
    @Override
    public <T> T get(String key, DomainResolver resolver) {
        return get(key, null, resolver);
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver)
	 */
    @Override
    public <T> T getOrDefine(String key, T defaultValue, DomainResolver resolver) {
        return getOrDefine(key, defaultValue, resolver, null);
    }

    /* (non-Javadoc)
	 * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver, java.lang.String)
	 */
    @Override
    public <T> T getOrDefine(String key, T defaultValue, DomainResolver resolver, String description) {
        T value = get(key, resolver);
        if (value != null) {
            return value;
        }
        set(key, defaultValue, description);
        return defaultValue;
    }

    @Override
    public Roperty<D, K> addDomains(String... domains) {
        requireNonNull(domains, "\"domains\" must not be null");
        for (String domain : domains) {
            Ensure.notEmpty(domain, "domain");
            this.domains.add(domain);
        }
        return this;
    }

    @Override
    public void set(String key, Object value, String description, String... domains) {
        String trimmedKey = trimKey(key);
        LOGGER.debug("Storing value: '{}' for key: '{}' with given domains: '{}'.", value, trimmedKey, domains);
        K keyValues = valuesStore.getOrCreateKeyValues(trimmedKey, description);
        requireNonNull(keyValues, "keyValues must not be null");
        keyValues.put(value, domains);
        store(trimmedKey, keyValues);
    }

    @Override
    public void setWithChangeSet(String key, Object value, String description, String changeSet, String... domains) {
        String trimmedKey = trimKey(key);
        LOGGER.debug("Storing value: '{}' for key: '{}' for change set: '{}' with given domains: '{}'.", value, trimmedKey, changeSet, domains);
        K keyValues = valuesStore.getOrCreateKeyValues(trimmedKey, description);
        keyValues.putWithChangeSet(changeSet, value, domains);
        changeSets.computeIfAbsent(changeSet, newKey -> new ArrayList<>()).add(trimmedKey);
        store(trimmedKey, keyValues, changeSet);
    }

    private void store(String key, K keyValues) {
        store(key, keyValues, "");
    }

    private void store(String key, K keyValues, String changeSet) {
        if (persistence != null) {
            persistence.store(key, keyValues, changeSet);
        }
    }

    private void remove(String key, K keyValues) {
        if (persistence != null) {
            persistence.remove(key, keyValues, null);
        }
    }

    private void remove(String key, DomainSpecificValue domainSpecificValue, String changeSet) {
        if (persistence != null) {
            persistence.remove(key, domainSpecificValue, changeSet);
        }
    }

    @Override
    public void setKeyValuesMap(Map<String, K> keyValuesMap) {
        requireNonNull(keyValuesMap, "\"keyValuesMap\" must not be null");
        valuesStore.setAllValues(keyValuesMap);
    }

    public void setPersistence(Persistence<D, K> persistence) {
        requireNonNull(persistence, "\"persistence\" must not be null");
        this.persistence = persistence;
        valuesStore.setPersistence(persistence);
        RopertyManagerFactory.getRopertyManager().add(this);
    }

    @Override
    public void reload() {
        valuesStore.reload();
    }

    @Override
    public String toString() {
        return "Roperty{domains=" + domains + '}';
    }

    @Override
    public StringBuilder dump() {
        StringBuilder builder = new StringBuilder("Roperty{domains=").append(domains);
        builder.append(valuesStore.dump());
        builder.append("\n}");
        return builder;
    }

    @Override
    public void dump(PrintStream out) {
        out.print("Roperty{domains=");
        out.print(domains);
        valuesStore.dump(out);
        out.println("\n}");
    }

    @Override
    public K getKeyValues(String key) {
        Ensure.notEmpty(key, "key");
        return valuesStore.getValuesFor(trimKey(key));
    }

    public void setKeyValuesFactory(KeyValuesFactory<D, K> keyValuesFactory) {
        valuesStore.setKeyValuesFactory(keyValuesFactory);
    }

    public void setDomainSpecificValueFactory(DomainSpecificValueFactory<D> domainSpecificValueFactory) {
        valuesStore.setDomainSpecificValueFactory(domainSpecificValueFactory);
    }

    @Override
    public Map<String, K> getKeyValues() {
        return valuesStore.getAllValues();
    }

    @Override
    public void removeWithChangeSet(String key, String changeSet, String... domainKeyParts) {
        String trimmedKey = trimKey(key);
        K keyValues = valuesStore.getKeyValuesFromMapOrPersistence(trimmedKey);
        if (keyValues != null) {
            D domainSpecificValue = keyValues.remove(changeSet, domainKeyParts);
            remove(trimmedKey, domainSpecificValue, changeSet);
        }
    }

    @Override
    public void remove(String key, String... domainValues) {
        removeWithChangeSet(key, null, domainValues);
    }

    @Override
    public void removeKey(String key) {
        String trimmedKey = trimKey(key);
        remove(trimmedKey, valuesStore.remove(trimmedKey));
    }

    @Override
    public void removeChangeSet(String changeSet) {
        for (String key : changeSets.get(changeSet)) {
            K keyValues = valuesStore.getKeyValuesFromMapOrPersistence(key);
            if (keyValues != null) {
                for (DomainSpecificValue value : keyValues.removeChangeSet(changeSet)) {
                    remove(key, value, changeSet);
                }
            }
        }
    }

    @Override
    public List<String> findKeys(String substring) {
        return persistence.findKeys(substring);
    }

}
