/*
 * Roperty - An advanced property management and retrival system
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
