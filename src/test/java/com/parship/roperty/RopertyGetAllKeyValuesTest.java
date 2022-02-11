package com.parship.roperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Collection;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RopertyGetAllKeyValuesTest {

    private DomainResolver resolver;
    private Roperty roperty;
    private RopertyWithResolver ropertyWithResolver;

    @BeforeEach
    void before() {
        resolver = new MapBackedDomainResolver()
            .set("dom1", "val1")
            .set("dom2", "val2")
            .set("dom3", "val3")
            .set("dom4", "val4");
        roperty = new RopertyImpl("dom1", "dom2", "dom3", "dom4");
        ropertyWithResolver = new RopertyWithResolver(roperty, resolver);
    }

    @Test
    void emptyRopertyGivesEmptyValues() {
        assertThat(roperty.getKeyValues()).isEmpty();
        assertThat(roperty.getKeyValues(resolver)).isEmpty();
    }

    @Test
    void getAllKeyValuesGivesEverythingUnfiltered() {
        roperty.set("key1", "value_1", "desc");
        roperty.set("key1", "value_dom1", "desc", "domval1");
        Collection<KeyValues> allKeyValues = roperty.getKeyValues();
        MatcherAssert.assertThat(allKeyValues.size(), is(1));
        final KeyValues keyValues = allKeyValues.iterator().next();
        MatcherAssert.assertThat(keyValues.getKey(), is("key1"));
        MatcherAssert.assertThat(keyValues.getDefaultValue(), is("value_1"));
        MatcherAssert.assertThat(keyValues.get(List.of("dom1"), "def", new MapBackedDomainResolver().set("dom1", "domval1")), is("value_dom1"));
    }

    @Test
    void getAllKeyValuesWithResolverGivesFilteredResult() {
        roperty.set("key1", "value_1", "desc");
        roperty.set("key1", "value_dom1", "desc", "domval1");
        Collection<KeyValues> allKeyValues = roperty.getKeyValues();
        MatcherAssert.assertThat(allKeyValues.size(), is(1));
        final KeyValues keyValues = allKeyValues.iterator().next();
        MatcherAssert.assertThat(keyValues.getKey(), is("key1"));
        MatcherAssert.assertThat(keyValues.getDefaultValue(), is("value_1"));
        MatcherAssert.assertThat(keyValues.get(List.of("dom1"), "def", new MapBackedDomainResolver().set("dom1", "domval1")), is("value_dom1"));
    }
}
