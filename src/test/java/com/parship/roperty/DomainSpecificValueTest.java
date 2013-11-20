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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-09-24 11:35
 */
public class DomainSpecificValueTest {

	@Test
	public void valuesWithChangeSetAreOrderedBeforeValuesWithoutChangeSet() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsv = new DomainSpecificValue(pattern, "value");
		DomainSpecificValue dsvWithChangeSet = new DomainSpecificValue(pattern, "value", "changeSet");
		assertThat(dsv.compareTo(dsvWithChangeSet), greaterThan(0));
		assertThat(dsvWithChangeSet.compareTo(dsv), lessThan(0));
	}

	@Test
	public void valuesWithChangeAreComparedByChangeSetName() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsvWithChangeSet_A = new DomainSpecificValue(pattern, "value", "a_changeSet");
		DomainSpecificValue dsvWithChangeSet_B = new DomainSpecificValue(pattern, "value", "b_changeSet");
		assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), greaterThan(0));
		assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), lessThan(0));
	}

	@Test
	public void valuesWithSameChangeAreComparedByPattern() {
		OrderedDomainPattern aPattern = new OrderedDomainPattern("a_pattern", 45);
		DomainSpecificValue dsvWithChangeSet_A = new DomainSpecificValue(aPattern, "value", "changeSet");
		OrderedDomainPattern bPattern = new OrderedDomainPattern("b_pattern", 45);
		DomainSpecificValue dsvWithChangeSet_B = new DomainSpecificValue(bPattern, "value", "changeSet");
		assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), not(equalTo(0)));
		assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), not(equalTo(0)));
	}

	@Test
	public void valuesWithoutChangeSetAreComparedByOrder() {
		OrderedDomainPattern aPattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsv_A = new DomainSpecificValue(aPattern, "value");
		OrderedDomainPattern bPattern = new OrderedDomainPattern("pattern", 67);
		DomainSpecificValue dsv_B = new DomainSpecificValue(bPattern, "value");
		assertThat(dsv_A.compareTo(dsv_B), greaterThan(0));
		assertThat(dsv_B.compareTo(dsv_A), lessThan(0));
	}

	@Test
	public void changeSetIs() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
		DomainSpecificValue dsv = new DomainSpecificValue(pattern, "val", "changeSet");
		assertThat(dsv.changeSetIs("changeSet"), is(true));
		assertThat(dsv.changeSetIs("other"), is(false));
	}

	@Test
	public void changeSetIsWithoutChangeSet() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
		DomainSpecificValue dsv = new DomainSpecificValue(pattern, "val");
		assertThat(dsv.changeSetIs(null), is(true));
		assertThat(dsv.changeSetIs("other"), is(false));
	}
}
