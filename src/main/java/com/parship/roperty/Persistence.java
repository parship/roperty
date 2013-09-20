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

import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-05-17 13:01
 */
public interface Persistence {
	/**
	 * Load all overridden values for a single key. Is called by Roperty when an unknown key is queried.
	 */
	KeyValues load(final String key, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory);

	/**
	 * Load all values persisted (Preloading). Is called by Roperty when it is started.
	 * @return A map that is either empty or prefilled with some data.
	 */
	Map<String, KeyValues> loadAll(KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory);

	/**
	 * Reload the data from persistence to synchronize changes.
	 * Reload may change the existing collection and give back a reference to the same collection passed as a parameter or it might create a new map.
	 * @param keyValuesMap current keyValuesMap with keys already known
	 */
	Map<String, KeyValues> reload(Map<String, KeyValues> keyValuesMap, KeyValuesFactory keyValuesFactory, DomainSpecificValueFactory domainSpecificValueFactory);

	void store(final String key, final KeyValues keyValues);
}
