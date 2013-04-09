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
		int order = this.ordering - other.ordering;
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
		boolean equals = patternStr.equals(domainStr.substring(0, Math.min(domainStr.length(), patternStr.length())));
		if (!equals && patternStr.contains("*")) {
			String patternStr = domainStr.replaceAll("\\|", "\\\\|").replaceAll("\\*", "[^|]*") + ".*";
			return domainStr.matches(patternStr);
		}
		return equals;
	}
}
