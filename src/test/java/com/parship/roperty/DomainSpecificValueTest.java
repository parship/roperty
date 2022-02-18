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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @author mfinsterwalder
 * @since 2013-09-24 11:35
 */
public class DomainSpecificValueTest {

    @Test
    void valuesWithChangeSetAreOrderedBeforeValuesWithoutChangeSet() {
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet("value");
        DomainSpecificValue dsvWithChangeSet = DomainSpecificValue.withChangeSet("value", "changeSet");
        assertThat(dsv.compareTo(dsvWithChangeSet), greaterThan(0));
        assertThat(dsvWithChangeSet.compareTo(dsv), lessThan(0));
    }

    @Test
    void valuesWithChangeAreComparedByChangeSetName() {
        DomainSpecificValue dsvWithChangeSet_A = DomainSpecificValue.withChangeSet("value", "a_changeSet");
        DomainSpecificValue dsvWithChangeSet_B = DomainSpecificValue.withChangeSet("value", "b_changeSet");
        assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), lessThan(0));
        assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), greaterThan(0));
    }

    @Test
    void valuesWithSameChangeAreComparedByPattern() {
        DomainSpecificValue dsvWithChangeSet_A = DomainSpecificValue.withChangeSet("value", "changeSet", "a");
        DomainSpecificValue dsvWithChangeSet_B = DomainSpecificValue.withChangeSet("value", "changeSet", "b");
        assertThat(dsvWithChangeSet_A.compareTo(dsvWithChangeSet_B), not(equalTo(0)));
        assertThat(dsvWithChangeSet_B.compareTo(dsvWithChangeSet_A), not(equalTo(0)));
    }

    @Test
    void valuesWithoutChangeSetAreComparedByOrder() {
        DomainSpecificValue dsv_A = DomainSpecificValue.withoutChangeSet("value", "a", "*");
        DomainSpecificValue dsv_B = DomainSpecificValue.withoutChangeSet("value", "a", "b");
        assertThat(dsv_A.compareTo(dsv_B), greaterThan(0));
        assertThat(dsv_B.compareTo(dsv_A), lessThan(0));
    }

    @Test
    void changeSetIs() {
        DomainSpecificValue dsv = DomainSpecificValue.withChangeSet("val", "changeSet");
        assertThat(dsv.getChangeSet(), is("changeSet"));
        assertThat(dsv.changeSetIs("changeSet"), is(true));
        assertThat(dsv.changeSetIs("other"), is(false));
        assertThat(dsv.changeSetIs(""), is(false));
        assertThat(dsv.changeSetIs(null), is(false));
    }

    @Test
    void changeSetIsWithoutChangeSet() {
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet("val");
        assertThat(dsv.changeSetIs(null), is(true));
        assertThat(dsv.changeSetIs("other"), is(false));
    }

    @Test
    void sameValueObjectsAreEqual() {
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet("val");
        assertThat(dsv, is(dsv));
    }

    @Test
    void nullValueIsNotEqual() {
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet("val");
        assertThat(dsv.equals(null), is(false));
    }

    @Test
    void valuesWithDifferentPatternsAreNotEqual() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withoutChangeSet("val", "a", "b");
        DomainSpecificValue dsv2 = DomainSpecificValue.withoutChangeSet("val", "a", "*");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    void differentValuesAreNotEqual() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withoutChangeSet("val1");
        DomainSpecificValue dsv2 = DomainSpecificValue.withoutChangeSet("val2");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    void valuesWithDifferentChangeSetsAreNotEqual() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withChangeSet("val", "changeSet1", "a");
        DomainSpecificValue dsv2 = DomainSpecificValue.withChangeSet("val", "changeSet2", "a");
        assertThat(dsv1.equals(dsv2), is(false));
    }

    @Test
    void consistentHashCode() {
        DomainSpecificValue dsv = DomainSpecificValue.withoutChangeSet("val", "b");
        int hashCode = dsv.hashCode();
        assertThat(hashCode, is(97813928));
    }

    @Test
    void createFromPattern() {
        DomainSpecificValue dsv = DomainSpecificValue.withPattern("val", "changeSet", "pattern|");
        assertThat(dsv.getPattern(), is("pattern|"));
    }

    @Test
    void createFromPatternMustEndWithPipe() {
        assertThrows(IllegalArgumentException.class, () -> DomainSpecificValue.withPattern("val", "changeSet", "pattern"));
    }

    @Test
    void orderingCorrectlyCalculatedForEmptyPattern() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withPattern("val", "changeSet", null);
        DomainSpecificValue dsv2 = DomainSpecificValue.withChangeSet("val", "changeSet");
        assertThat(dsv1.getPattern(), is(dsv2.getPattern()));
    }

    @Test
    void orderingCorrectlyCalculatedForShortPattern() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withPattern("val", "changeSet", "pattern|");
        DomainSpecificValue dsv2 = DomainSpecificValue.withChangeSet("val", "changeSet", "pattern");
        assertThat(dsv1.getPattern(), is(dsv2.getPattern()));
    }

    @Test
    void orderingCorrectlyCalculatedForMoreComplexPattern() {
        DomainSpecificValue dsv1 = DomainSpecificValue.withPattern("val", "changeSet", "pattern|*|b|*|x|");
        DomainSpecificValue dsv2 = DomainSpecificValue.withChangeSet("val", "changeSet", "pattern", "*", "b", "*", "x");
        assertThat(dsv1.getPattern(), is(dsv2.getPattern()));
    }
}
