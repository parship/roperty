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

/**
 * Callback interface to resolve values for the different domains for the current query to Roperty.
 * Passed as a paramter to Roperty.get-methods this interface is called for each domain configured
 * in the Roperty instance queried. Roperty will call this interface once for each domain configured.
 *
 * @author mfinsterwalder
 * @since 2013-03-25 08:13
 */
public interface DomainResolver {
	/**
	 * This method is called by Roperty once for each configured domain.
	 *
	 * @param domain The domain to translate to a value
	 * @return The value for the domain or null, when the domain should be ignored
	 */
	String getDomainValue(String domain);
}
