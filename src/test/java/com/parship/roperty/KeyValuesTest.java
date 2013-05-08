/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.parship.roperty;

import org.junit.Test;

import java.util.Collections;

import static com.parship.commons.util.CollectionUtil.arrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 22:32
 */
public class KeyValuesTest {

	private KeyValues keyValues = new KeyValues();
	private Resolver resolver = new Resolver() {
		@Override
		public String getDomainValue(final String domain) {
			return domain;
		}
	};

	@Test
	public void callingGetWithAnEmtpyDomainListDoesNotUseTheResolver() {
		assertThat((String)keyValues.get(Collections.EMPTY_LIST, null), is("[value undefined]"));
		keyValues.put("val");
		assertThat((String)keyValues.get(Collections.EMPTY_LIST, null), is("val"));
	}

	@Test(expected = NullPointerException.class)
	public void callingGetWithoutAResolverGivesNullPointerException() {
		keyValues.get(arrayList("dom1", "dom2"), null);
	}

	@Test
	public void getAWildcardOverriddenValueIsReturned() {
		keyValues.put("value", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value"));
	}

	@Test
	public void getAWildcardOverriddenValueIsReturned_2() {
		keyValues.put("value_1", "*", "*", "domain3");
		keyValues.put("value_2", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value_2"));
	}

	@Test
	public void getAWildcardOverriddenValueIsReturned_3() {
		keyValues.put("value_1", "aaa", "*", "domain3");
		keyValues.put("value_2", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value_2"));
	}

}
