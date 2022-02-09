/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty;

import com.parship.roperty.jmx.RopertyManager;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central class to access Roperty. Manages domains and key-to-value mappings.
 *
 * @author mfinsterwalder
 * @since 2013-03-25 08:07
 */
public class RopertyImpl implements Roperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(RopertyImpl.class);
    private volatile ValuesStore valuesStore;
    private List<String> domains;
    private Persistence persistence;
    private final Map<String, Collection<String>> changeSets = new HashMap<>();

    public RopertyImpl(final Persistence persistence, final DomainInitializer domainInitializer, DomainSpecificValueFactory
        domainSpecificValueFactory) {
        Objects.requireNonNull(domainInitializer, "\"domainInitializer\" must not be null");
        domains = domainInitializer.getInitialDomains();
        initFromPersistence(persistence, domainSpecificValueFactory);
    }

    public RopertyImpl(final Persistence persistence, final DomainInitializer domainInitializer) {
        this(persistence, domainInitializer, createDomainSpecificValueFactory());
    }

    public RopertyImpl(final Persistence persistence, DomainSpecificValueFactory domainSpecificValueFactory, final String... domains) {
        initDomains(domains);
        initFromPersistence(persistence, domainSpecificValueFactory);
    }

    private void initDomains(final String[] domains) {
        this.domains = new CopyOnWriteArrayList<>();
        addDomains(domains);
    }

    public RopertyImpl(final Persistence persistence, final String... domains) {
        this(persistence, createDomainSpecificValueFactory(), domains);
    }

    private void initFromPersistence(final Persistence persistence, final DomainSpecificValueFactory domainSpecificValueFactory) {
        Objects.requireNonNull(domainSpecificValueFactory, "\"domainSpecificValueFactory\" must not be null");
        Objects.requireNonNull(persistence, "\"persistence\" must not be null");
        this.persistence = persistence;
        valuesStore = new ValuesStore();
        valuesStore.setDomainSpecificValueFactory(domainSpecificValueFactory);
        valuesStore.setPersistence(persistence);
        valuesStore.setAllValues(persistence.loadAll(domainSpecificValueFactory));
        RopertyManager.getInstance().add(this);
    }

    public RopertyImpl(final String... domains) {
        initDomains(domains);
        initWithoutPersistence();
        RopertyManager.getInstance().add(this);
    }

    public RopertyImpl() {
        domains = new CopyOnWriteArrayList<>();
        initWithoutPersistence();
        RopertyManager.getInstance().add(this);
    }

    private void initWithoutPersistence() {
        valuesStore = new ValuesStore();
        valuesStore.setDomainSpecificValueFactory(createDomainSpecificValueFactory());
    }

    private static DomainSpecificValueFactory createDomainSpecificValueFactory() {
        return new DomainSpecificValueFactoryWithStringInterning();
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#get(java.lang.String, T, com.parship.roperty.DomainResolver)
     */
    @Override
    public <T> T get(final String key, final T defaultValue, DomainResolver resolver) {
        final String trimmedKey = trimKey(key);
        KeyValues keyValues = valuesStore.getKeyValuesFromMapOrPersistence(trimmedKey);
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
                builder.append(domain).append(" => ").append(resolver.getDomainValue(domain)).append("; ");
            }
            LOGGER.debug(builder.toString());
        }
        return result;
    }

    private static String trimKey(final String key) {
        Ensure.notEmpty(key, "key");
        return key.trim();
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#get(java.lang.String, com.parship.roperty.DomainResolver)
     */
    @Override
    public <T> T get(final String key, DomainResolver resolver) {
        return get(key, null, resolver);
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver)
     */
    @Override
    public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver) {
        return getOrDefine(key, defaultValue, resolver, null);
    }

    /* (non-Javadoc)
     * @see com.parship.roperty.Roperty#getOrDefine(java.lang.String, T, com.parship.roperty.DomainResolver, java.lang.String)
     */
    @Override
    public <T> T getOrDefine(final String key, final T defaultValue, DomainResolver resolver, String description) {
        T value = get(key, resolver);
        if (value != null) {
            return value;
        }
        set(key, defaultValue, description);
        return defaultValue;
    }

    @Override
    public Roperty addDomains(final String... domains) {
        Objects.requireNonNull(domains, "\"domains\" must not be null");
        for (String domain : domains) {
            Ensure.notEmpty(domain, "domain");
            this.domains.add(domain);
        }
        return this;
    }

    @Override
    public void set(final String key, final Object value, final String description, final String... domains) {
        final String trimmedKey = trimKey(key);
        LOGGER.debug("Storing value: '{}' for key: '{}' with given domains: '{}'.", value, trimmedKey, domains);
        KeyValues keyValues = valuesStore.getOrCreateKeyValues(trimmedKey, description);
        keyValues.put(value, domains);
        store(trimmedKey, keyValues);
    }

    @Override
    public void setWithChangeSet(final String key, final Object value, final String description, String changeSet,
        final String... domains) {
        final String trimmedKey = trimKey(key);
        LOGGER.debug("Storing value: '{}' for key: '{}' for change set: '{}' with given domains: '{}'.", value, trimmedKey, changeSet,
            domains);
        KeyValues keyValues = valuesStore.getOrCreateKeyValues(trimmedKey, description);
        keyValues.putWithChangeSet(changeSet, value, domains);
        getChangeSetKeys(changeSet).add(trimmedKey);
        store(trimmedKey, keyValues, changeSet);
    }

    private synchronized Collection<String> getChangeSetKeys(final String changeSet) {
        return changeSets.computeIfAbsent(changeSet, k -> new ArrayList<>());
    }

    private void store(final String key, final KeyValues keyValues) {
        if (persistence != null) {
            persistence.store(key, keyValues, "");
        }
    }

    private void store(final String key, final KeyValues keyValues, final String changeSet) {
        if (persistence != null) {
            persistence.store(key, keyValues, changeSet);
        }
    }

    private void remove(final String key, final DomainSpecificValue domainSpecificValue, final String changeSet) {
        if (persistence != null) {
            persistence.remove(key, domainSpecificValue, changeSet);
        }
    }

    @Override
    public void setKeyValuesMap(final Map<String, KeyValues> keyValuesMap) {
        Objects.requireNonNull(keyValuesMap, "\"keyValuesMap\" must not be null");
        valuesStore.setAllValues(keyValuesMap);
    }

    public void setPersistence(final Persistence persistence) {
        Objects.requireNonNull(persistence, "\"persistence\" must not be null");
        this.persistence = persistence;
        valuesStore.setPersistence(persistence);
        RopertyManager.getInstance().add(this);
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
    public void dump(final PrintStream out) {
        out.print("Roperty{domains=");
        out.print(domains);
        valuesStore.dump(out);
        out.println("\n}");
    }

    @Override
    public KeyValues getKeyValues(final String key) {
        Ensure.notEmpty(key, "key");
        return valuesStore.getValuesFor(key.trim());
    }

    public void setDomainSpecificValueFactory(final DomainSpecificValueFactory domainSpecificValueFactory) {
        valuesStore.setDomainSpecificValueFactory(domainSpecificValueFactory);
    }

    @Override
    public Map<String, KeyValues> getKeyValues() {
        return valuesStore.getAllValues();
    }

    @Override
    public void removeWithChangeSet(final String key, final String changeSet, final String... domainValues) {
        final String trimmedKey = trimKey(key);
        KeyValues keyValues = valuesStore.getKeyValuesFromMapOrPersistence(trimmedKey);
        if (keyValues != null) {
            remove(trimmedKey, keyValues.remove(changeSet, domainValues), changeSet);
        }
    }

    @Override
    public void remove(final String key, final String... domainValues) {
        removeWithChangeSet(key, null, domainValues);
    }

    @Override
    public void removeKey(final String key) {
        final String trimmedKey = trimKey(key);
        valuesStore.remove(trimmedKey);
        persistence.remove(trimmedKey, null);
    }

    @Override
    public void removeChangeSet(String changeSet) {
        Objects.requireNonNull(changeSet, "\"changeSet\" must not be null");
        Collection<String> changeSetKeyValues = changeSets.get(changeSet);
        if (changeSetKeyValues == null) {
            LOGGER.warn("No key/values found for changeSet: {}", changeSet);
            return;
        }
        for (String key : changeSetKeyValues) {
            KeyValues keyValues = valuesStore.getKeyValuesFromMapOrPersistence(key);
            if (keyValues != null) {
                for (DomainSpecificValue value : keyValues.removeChangeSet(changeSet)) {
                    remove(key, value, changeSet);
                }
            }
        }
    }
}
