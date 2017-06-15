package com.parship.roperty.domainspecificvalue;

import java.util.Objects;


/**
 * User: mjaeckel
 * Date: 15.11.13
 * Time: 11:43
 */
public class OrderedDomainPatternFactory {
	public static OrderedDomainPattern calculateOrderedDomainPattern(final String[] domainValues) {
		StringBuilder builder = new StringBuilder(domainValues.length * 8);
		int order = 1;
		int i = 0;
		for (String domainValue : domainValues) {
			i++;
			if (!Objects.equals("*", domainValue)) {
                order |= (int)Math.pow(2, i);
			}
			builder.append(domainValue).append('|');
		}
		return new OrderedDomainPattern(builder.toString(), order);
	}
}
