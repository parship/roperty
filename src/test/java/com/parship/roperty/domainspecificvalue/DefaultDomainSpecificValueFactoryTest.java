package com.parship.roperty.domainspecificvalue;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by Benjamin Jochheim on 10.11.15.
 */
public class DefaultDomainSpecificValueFactoryTest {

    private final DefaultDomainSpecificValueFactory factory = new DefaultDomainSpecificValueFactory();

    @Test
    public void factoryCreatesCorrectDSVForBaseKey() {
        String value = "value";
        DomainSpecificValue dsv = factory.create(value, null);
        assertThat((String)dsv.getValue(), is(value));
        assertThat(dsv.getPatternStr(), is(""));
    }

    @Test
    public void factoryCreatesCorrectDSVForOverriddenKey() {
        String value = "overriddenValue";

        DomainSpecificValue dsv = factory.create(value, null, "DE", "de_DE");

        assertThat((String)dsv.getValue(), is(value));
        assertThat(dsv.getPatternStr(), is("DE|de_DE|"));
    }

    @Test
    public void factorySetReverseOrderForMoreSpecificValues() {
        String value = "overriddenValue";

        DomainSpecificValue dsv = factory.create(value, null, "DE");
        DomainSpecificValue moreSpecificDsv = factory.create(value, null, "DE", "de_DE");

        assertThat(dsv.compareTo(moreSpecificDsv), greaterThan(0));
    }

    @Test
    public void testInterningOfStrings() throws Exception {

        final String value1 = new String("testString");
        final String value2 = new String("testString");
        assertThat("precondition", value1 == (value2), is(false));
        assertThat("precondition", value1, is(value2));

        DomainSpecificValue dsv1 = factory.create(value1, null, "DE", "de_DE");
        DomainSpecificValue dsv2 = factory.create(value2, null, "DE", "de_DE");

        assertThat("both strings should map to the same object", dsv1.getValue() == dsv2.getValue(), is(true));
        assertThat((String)dsv1.getValue(), is("testString"));
    }
}
