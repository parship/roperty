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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import org.junit.jupiter.api.Test;

/**
 * @author mfinsterwalder
 * @since 2013-09-24 11:35
 */
public class DomainSpecificValueTest {

	@Test
	public void valuesWithChangeSetAreOrderedBeforeValuesWithoutChangeSet() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet(pattern, "value");
		DomainSpecificValue dsvWithChangeSet = DomainSpecificValue.withChangeSet(pattern, "value", "changeSet");
		assertThat(dsv.compareTo(dsvWithChangeSet), greaterThan(0));
		assertThat(dsvWithChangeSet.compareTo(dsv), lessThan(0));
	}

	@Test
	public void valuesWithChangeAreComparedByChangeSetName() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsvWithChangeSet_A = DomainSpecificValue.withChangeSet(pattern, "value", "a_changeSet");
		DomainSpecificValue dsvWithChangeSet_B = DomainSpecificValue.withChangeSet(pattern, "value", "b_changeSet");
		assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), lessThan(0));
		assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), greaterThan(0));
	}

	@Test
	public void valuesWithSameChangeAreComparedByPattern() {
		OrderedDomainPattern aPattern = new OrderedDomainPattern("a_pattern", 45);
		DomainSpecificValue dsvWithChangeSet_A = DomainSpecificValue.withChangeSet(aPattern, "value", "changeSet");
		OrderedDomainPattern bPattern = new OrderedDomainPattern("b_pattern", 45);
		DomainSpecificValue dsvWithChangeSet_B = DomainSpecificValue.withChangeSet(bPattern, "value", "changeSet");
		assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), not(equalTo(0)));
		assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), not(equalTo(0)));
	}

	@Test
	public void valuesWithoutChangeSetAreComparedByOrder() {
		OrderedDomainPattern aPattern = new OrderedDomainPattern("pattern", 45);
		DomainSpecificValue dsv_A = DomainSpecificValue.withoutChangeSet(aPattern, "value");
		OrderedDomainPattern bPattern = new OrderedDomainPattern("pattern", 67);
		DomainSpecificValue dsv_B = DomainSpecificValue.withoutChangeSet(bPattern, "value");
		assertThat(dsv_A.compareTo(dsv_B), greaterThan(0));
		assertThat(dsv_B.compareTo(dsv_A), lessThan(0));
	}

	@Test
	public void changeSetIs() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
		DomainSpecificValue dsv = DomainSpecificValue.withChangeSet(pattern, "val", "changeSet");
        assertThat(dsv.getChangeSet(), is("changeSet"));
        assertThat(dsv.changeSetIs("changeSet"), is(true));
        assertThat(dsv.changeSetIs("other"), is(false));
        assertThat(dsv.changeSetIs(""), is(false));
        assertThat(dsv.changeSetIs(null), is(false));
	}

	@Test
	public void changeSetIsWithoutChangeSet() {
		OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
		DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet(pattern, "val");
		assertThat(dsv.changeSetIs(null), is(true));
		assertThat(dsv.changeSetIs("other"), is(false));
	}

	@Test
    public void sameValueObjectsAreEqual() {
        OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet(pattern, "val");
        boolean equals = dsv.equals(dsv);
        assertThat(equals, is(true));
    }

    @Test
    public void nullValueIsNotEqual() {
        OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet(pattern, "val");
        assertThat(dsv.equals(null), is(false));
    }

    @Test
    public void differentlyOrderedValuesAreNotEqual() {
        OrderedDomainPattern pattern1 = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv1 = DomainSpecificValue.withoutChangeSet(pattern1, "val");
        OrderedDomainPattern pattern2 = new OrderedDomainPattern("p", 5);
        DomainSpecificValue dsv2 = DomainSpecificValue.withoutChangeSet(pattern2, "val");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    public void valuesWithDifferentPatternsAreNotEqual() {
        OrderedDomainPattern pattern1 = new OrderedDomainPattern("p1", 4);
        DomainSpecificValue dsv1 = DomainSpecificValue.withoutChangeSet(pattern1, "val");
        OrderedDomainPattern pattern2 = new OrderedDomainPattern("p2", 4);
        DomainSpecificValue dsv2 = DomainSpecificValue.withoutChangeSet(pattern2, "val");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    public void differentValuesAreNotEqual() {
        OrderedDomainPattern pattern1 = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv1 = DomainSpecificValue.withoutChangeSet(pattern1, "val1");
        OrderedDomainPattern pattern2 = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv2 = DomainSpecificValue.withoutChangeSet(pattern2, "val2");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    public void valuesWithDifferentChangeSetsAreNotEqual() {
        OrderedDomainPattern pattern1 = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv1 = DomainSpecificValue.withChangeSet(pattern1, "val", "changeSet1");
        OrderedDomainPattern pattern2 = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv2 = DomainSpecificValue.withChangeSet(pattern2, "val", "changeSet2");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    public void consistentHashCode() {
        OrderedDomainPattern pattern = new OrderedDomainPattern("p", 4);
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet(pattern, "val");
        int hashCode = dsv.hashCode();
        assertThat(hashCode, is(6952339));
    }

}
