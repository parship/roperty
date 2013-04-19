package com.parship.roperty;

import com.parship.commons.util.Ensure;


/**
 * @author mfinsterwalder
 * @since 2013-04-09 18:20
 */
class DomainPattern implements Comparable<DomainPattern> {
	private final String patternStr;
	private final int ordering;
	private final Object value;

	public DomainPattern(final String domainPattern, final int order, Object value) {
		Ensure.notNull(domainPattern, "domainPattern");
		Ensure.notNull(value, "value");
		this.patternStr = domainPattern;
		this.ordering = order;
		this.value = value;
	}

	@Override
	public int compareTo(final DomainPattern other) {
		int order = other.ordering - this.ordering;
		if (order == 0) {
			return patternStr.compareTo(other.patternStr);
		}
		return order;
	}

	@Override
	public String toString() {
		return "DomainPattern{" +
			"pattern=" + patternStr +
			", ordering=" + ordering +
			", value=" + value +
			'}';
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
