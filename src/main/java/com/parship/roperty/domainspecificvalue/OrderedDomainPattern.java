package com.parship.roperty.domainspecificvalue;

public class OrderedDomainPattern {
	private final String domainPattern;
	private final int order;

	public OrderedDomainPattern(final String domainPattern, final int order) {
		this.domainPattern = domainPattern;
		this.order = order;
	}

	public String getDomainPattern() {
		return domainPattern;
	}

	public int getOrder() {
		return order;
	}
}
