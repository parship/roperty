package com.parship.roperty;

import com.parship.commons.util.Ensure;


/**
 * @author mfinsterwalder
 * @since 2013-05-15 15:33
 */
public class RopertyWithResolver {

	private final Roperty roperty;
	private final DomainResolver domainResolver;

	public RopertyWithResolver(final Roperty roperty, final DomainResolver domainResolver) {
		Ensure.notNull(roperty, "roperty");
		Ensure.notNull(domainResolver, "domainResolver");
		this.roperty = roperty;
		this.domainResolver = domainResolver;
	}

	public <T> T get(final String key) {
		return roperty.get(key, domainResolver);
	}

	public <T> T get(final String key, final T defaultValue) {
		return roperty.get(key, defaultValue, domainResolver);
	}

	public <T> T getOrDefine(final String key, final T defaultValue) {
		return roperty.getOrDefine(key, defaultValue, domainResolver);
	}

	public void set(final String key, final Object value, final String... domains) {
		roperty.set(key, value, domains);
	}

	public Roperty getRoperty() {
		return roperty;
	}

	@Override
	public String toString() {
		return "RopertyWithResolver{" +
			"roperty=" + roperty +
			", domainResolver=" + domainResolver +
			'}';
	}
}
