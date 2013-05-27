/*
 * Roperty - An advanced property management and retrieval system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parship.roperty;

import com.parship.commons.util.Ensure;


/**
 * @author mfinsterwalder
 * @since 2013-04-09 18:20
 */
public class DomainSpecificValue implements Comparable<DomainSpecificValue> {
	private final String patternStr;
	private final int ordering;
	private final Object value;

	public DomainSpecificValue(final String domainPattern, final int order, Object value) {
		Ensure.notNull(domainPattern, "domainPattern");
		this.patternStr = domainPattern;
		this.ordering = order;
		this.value = value;
	}

	@Override
	public int compareTo(final DomainSpecificValue other) {
		int order = other.ordering - this.ordering;
		if (order == 0) {
			return patternStr.compareTo(other.patternStr);
		}
		return order;
	}

	@Override
	public String toString() {
		return "DomainSpecificValue{" +
			"pattern=" + patternStr +
			", ordering=" + ordering +
			", value=" + value +
			'}';
	}

	public String getPatternStr() {
		return patternStr;
	}

	public Object getValue() {
		return value;
	}

	public boolean matches(final String domainStr) {
		boolean equals = prefixEquals(patternStr, domainStr);
		if (!equals && patternStr.contains("*")) {
			return domainStr.matches(toRegEx(patternStr));
		}
		return equals;
	}

	private boolean prefixEquals(final String patternStr, final String domainStr) {
		return patternStr.equals(domainStr.substring(0, Math.min(domainStr.length(), patternStr.length())));
	}

	private String toRegEx(final String domainStr) {
		return domainStr.replaceAll("\\|", "\\\\|").replaceAll("\\*", "[^|]*") + ".*";
	}
}
