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

import java.util.HashMap;
import java.util.Map;


/**
 * @author mfinsterwalder
 * @since 2013-05-15 15:08
 */
public class MapBackedDomainResolver implements DomainResolver {

	private Map<String, String> map = new HashMap<>();

	@Override
	public String getDomainValue(final String domain) {
		return map.get(domain);
	}

	public MapBackedDomainResolver set(final String domain, final String domainValue) {
		map.put(domain, domainValue);
		return this;
	}

	private void dumpMap(StringBuilder sb) {
		sb.append('{');
		boolean first = true;
		for (Map.Entry<String,String> entry : map.entrySet()) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(entry.getKey()).append('=').append(entry.getValue());
			first = false;
		}
		sb.append('}');
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(this.getClass().getName());
		sb.append(" with ");
		dumpMap(sb);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapBackedDomainResolver other = (MapBackedDomainResolver)obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}
	
	
}
