package com.parship.roperty;

/**
 * User: mjaeckel
 * Date: 15.11.13
 * Time: 11:43
 */
public abstract class AbstractDomainSpecificValueFactory {
	public static OrderedDomainPattern calculateOrderedDomainPattern(final String[] domainValues) {
		StringBuilder builder = new StringBuilder();
		int order = 1;
		int i = 0;
		for (String domainValue : domainValues) {
			i++;
			if (!"*".equals(domainValue)) {
				order = order | (int)Math.pow(2, i);
			}
			builder.append(domainValue).append('|');
		}
		return new OrderedDomainPattern(builder.toString(), order);
	}
}
