package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The internal in memory storage for Ropertys KeyValues
 */
public class ValuesStore {

    private final Map<String, KeyValues> keyValuesMap = new HashMap<>();
    private DomainSpecificValueFactory domainSpecificValueFactory;
    private Persistence persistence;

    public Map<String, KeyValues> getAllValues() {
        return Collections.unmodifiableMap(keyValuesMap);
    }

    public void setAllValues(Map<? extends String, ? extends KeyValues> values) {
        synchronized (keyValuesMap) {
            keyValuesMap.clear();
            keyValuesMap.putAll(values);
        }
    }

    public KeyValues getOrCreateKeyValues(final String key, final String description) {
        KeyValues keyValues = getKeyValuesFromMapOrPersistence(key);
        if (keyValues == null) {
            synchronized (keyValuesMap) {
                keyValues = keyValuesMap.computeIfAbsent(key, k -> {
                    KeyValues kv = new KeyValues(domainSpecificValueFactory);
                    if (description != null && description.trim().length() > 0) {
                        kv.setDescription(description);
                    }
                    return kv;
                });
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
}
