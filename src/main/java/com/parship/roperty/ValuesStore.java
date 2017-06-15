package com.parship.roperty;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import static java.util.Objects.requireNonNull;


public class ValuesStore<D extends DomainSpecificValue, K extends KeyValues<D>> {

    private final Map<String, K> keyValuesMap = new HashMap<>();
    private KeyValuesFactory<D, K> keyValuesFactory;
    private DomainSpecificValueFactory<D> domainSpecificValueFactory;
    private Persistence<D, K> persistence;

    public Map<String, K> getAllValues() {
        return Collections.unmodifiableMap(keyValuesMap);
    }

    public void setAllValues(Map<String, K> values) {
        keyValuesMap.clear();
        keyValuesMap.putAll(values);
    }

    public K getOrCreateKeyValues(final String key, final String description) {
        K keyValues = getKeyValuesFromMapOrPersistence(key);
        if (keyValues == null) {
            synchronized (keyValuesMap) {
                keyValues = keyValuesMap.get(key);
                if (keyValues == null) {
                    requireNonNull(keyValuesFactory, "keyValuesFactory must not be null");
                    keyValues = keyValuesFactory.create(domainSpecificValueFactory);
                    requireNonNull(keyValues, "keyValues must not be null");
                    if (description != null && !description.trim().isEmpty()) {
                        keyValues.setDescription(description);
                    }
                    keyValuesMap.put(key, keyValues);
                }
            }
        }
        return keyValues;
    }

    public K getKeyValuesFromMapOrPersistence(final String key) {
        K keyValues = keyValuesMap.get(key);
        if (keyValues == null) {
            keyValues = load(key);
            if (keyValues != null) {
                synchronized (keyValuesMap) {
                    K keyValuesSecondTry = keyValuesMap.get(key);
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
        for (Entry<String, K> entry : keyValuesMap.entrySet()) {
            builder.append('\n').append("KeyValues for \"").append(entry.getKey()).append("\": ").append(entry.getValue());
        }
        return builder.toString();
    }

    public void dump(PrintStream out) {
        for (Entry<String, K> entry : keyValuesMap.entrySet()) {
            out.println();
            out.print("KeyValues for \"");
            out.print(entry.getKey());
            out.print("\": ");
            out.print(entry.getValue());
        }
    }

    public K getValuesFor(String key) {
        return keyValuesMap.get(key);
    }

    public K remove(String key) {
        return keyValuesMap.remove(key);
    }

    private K load(final String key) {
        if (persistence != null) {
            return persistence.load(key, keyValuesFactory, domainSpecificValueFactory);
        }
        return null;
    }

    public void setKeyValuesFactory(KeyValuesFactory<D, K> keyValuesFactory) {
        this.keyValuesFactory = keyValuesFactory;
    }

    public void setDomainSpecificValueFactory(DomainSpecificValueFactory<D> domainSpecificValueFactory) {
        this.domainSpecificValueFactory = domainSpecificValueFactory;
    }

    public void setPersistence(Persistence<D, K> persistence) {
        this.persistence = persistence;
    }

    public void reload() {
        if (persistence != null) {
            Map<String, K> oldValues = getAllValues();
            Map<String, K> newValues = persistence.reload(oldValues, keyValuesFactory, domainSpecificValueFactory);
            setAllValues(newValues);
        }
    }
}
