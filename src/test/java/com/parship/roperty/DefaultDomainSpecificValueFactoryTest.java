package com.parship.roperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * User: mjaeckel Date: 15.11.13 Time: 10:47
 */
public class DefaultDomainSpecificValueFactoryTest {

    private final DomainSpecificValueFactory factory = new DefaultDomainSpecificValueFactory();

    @Test
    void factoryCreatesCorrectDSVForBaseKey() {
        String value = "value";
        DomainSpecificValue dsv = factory.create(value, null);
        assertThat((String) dsv.getValue(), is(value));
        assertThat(dsv.getPattern(), is(""));
    }

    @Test
    void factoryCreatesCorrectDSVForOverriddenKey() {
        String value = "overriddenValue";

        DomainSpecificValue dsv = factory.create(value, null, "DE", "de_DE");

        assertThat((String) dsv.getValue(), is(value));
        assertThat(dsv.getPattern(), is("DE|de_DE|"));
    }

    @Test
    void factorySetReverseOrderForMoreSpecificValues() {
        String value = "overriddenValue";

        DomainSpecificValue dsv = factory.create(value, null, "DE");
        DomainSpecificValue moreSpecificDsv = factory.create(value, null, "DE", "de_DE");

        assertThat(dsv.compareTo(moreSpecificDsv), greaterThan(0));
    }
}
