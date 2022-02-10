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

import java.util.Collection;
import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 13:01
 */
public interface Persistence {
    /**
     * Load all overridden values for a single key. Is called by Roperty when an unknown key is queried.
     */
    KeyValues load(final String key, DomainSpecificValueFactory domainSpecificValueFactory);

    /**
     * Load all values persisted (Preloading). Is called by Roperty when it is started.
     *
     * @return A map that is either empty or prefilled with some data.
     */
    Collection<KeyValues> loadAll(DomainSpecificValueFactory domainSpecificValueFactory);

    /**
     * Reload the data from persistence to synchronize changes.
     * Reload may change the existing collection and give back a reference to the same collection passed as a parameter or it might create a new map.
     *
     * @param keyValuesMap current keyValuesMap with keys already known
     */
    Collection<KeyValues> reload(Map<String, KeyValues> keyValuesMap, DomainSpecificValueFactory domainSpecificValueFactory);

    void store(final String key, final KeyValues keyValues, final String changeSet);

    /**
     * Remove a complete key from persistence.
     *  @param key       the key to remove
     * @param changeSet the changeSet to remove
     */
    void remove(String key, final String changeSet);

    /**
     * Remove a DomainSpecificValue from persistence.
     *
     * @param key                 the key for which to remove the overwritten value
     * @param domainSpecificValue the DomainSpecificValue to remove
     * @param changeSet           the changeSet for which to remove the overwritten value
     */
    void remove(String key, DomainSpecificValue domainSpecificValue, final String changeSet);
}
