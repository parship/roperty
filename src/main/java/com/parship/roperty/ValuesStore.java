package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The internal in memory storage for Ropertys KeyValues
 */
public class ValuesStore {

    private final Map<String, KeyValues> keyValuesMap = new HashMap<>();
    private DomainSpecificValueFactory domainSpecificValueFactory;
    private Persistence persistence;

    public Collection<KeyValues> getAllValues() {
        return Collections.unmodifiableCollection(keyValuesMap.values());
    }

    public Collection<KeyValues> getAllValues(List<String> domains, DomainResolver resolver) {
        return keyValuesMap.values().stream()
            .map(keyValues -> keyValues.copy(domains, resolver))
            .filter(keyValues -> !keyValues.getDomainSpecificValues().isEmpty())
            .collect(Collectors.toUnmodifiableList());
    }

    public void setAllValues(Collection<? extends KeyValues> values) {
        synchronized (keyValuesMap) {
            keyValuesMap.clear();
            values.forEach(kv -> keyValuesMap.put(kv.getKey(), kv));
        }
    }

    public KeyValues getOrCreateKeyValues(final String key, final String description) {
        KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
        if (keyValues == null) {
            synchronized (keyValuesMap) {
                keyValues = keyValuesMap.computeIfAbsent(key, k -> new KeyValues(key, domainSpecificValueFactory, description));
            }
        }
        return keyValues;
    }

    public KeyValues getKeyValuesFromMapOrPersistence(final String key) {
        KeyValues keyValues = keyValuesMap.get(key);
        if (keyValues == null) {
            keyValues = load(key);
            if (keyValues != null) {
                synchronized (keyValuesMap) {
                    KeyValues keyValuesSecondTry = keyValuesMap.get(key);
                    if (keyValuesSecondTry == null) {
                        keyValuesMap.put(key, keyValues);
                    } else {
                        return keyValuesSecondTry;
                    }
                }
            }
        }
        return keyValues;
    }

    public String dump() {
        StringBuilder builder = new StringBuilder(keyValuesMap.size() * 16);
        for (Map.Entry<String, KeyValues> entry : keyValuesMap.entrySet()) {
            builder.append('\n').append("KeyValues for \"").append(entry.getKey()).append("\": ").append(entry.getValue());
        }
        return builder.toString();
    }

    public void dump(PrintStream out) {
        for (Map.Entry<String, KeyValues> entry : keyValuesMap.entrySet()) {
            out.println();
            out.print("KeyValues for \"");
            out.print(entry.getKey());
            out.print("\": ");
            out.print(entry.getValue());
        }
    }

    public KeyValues getValuesFor(String key) {
        return keyValuesMap.get(key);
    }

    public KeyValues remove(String key) {
        synchronized (keyValuesMap) {
            return keyValuesMap.remove(key);
        }
    }

    private KeyValues load(final String key) {
        if (persistence != null) {
            return persistence.load(key, domainSpecificValueFactory);
        }
        return null;
    }

    public void setDomainSpecificValueFactory(DomainSpecificValueFactory domainSpecificValueFactory) {
        this.domainSpecificValueFactory = domainSpecificValueFactory;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public void reload() {
        if (persistence != null) {
            setAllValues(persistence.reload(getAllValues(), domainSpecificValueFactory));
        }
    }

    public void removeChangeSet(String changeSet) {
        for (KeyValues keyValues : keyValuesMap.values()) {
            for (DomainSpecificValue value : keyValues.removeChangeSet(changeSet)) {
                persistence.remove(keyValues.getKey(), value);
            }
        }
    }
}
