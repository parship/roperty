package com.parship.roperty;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;
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
        roperty.set("key1", "value_dom2", "desc", "domval2");
        Collection<KeyValues> allKeyValues = roperty.getKeyValues();
        assertThat(allKeyValues).hasSize(1);
        final KeyValues keyValues = allKeyValues.iterator().next();
        assertThat(keyValues.getKey()).isEqualTo("key1");
        assertThat((String) keyValues.getDefaultValue()).isEqualTo("value_1");
        final Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("value_1", null),
            new DefaultDomainSpecificValueFactory().create("value_dom1", null, "domval1"),
            new DefaultDomainSpecificValueFactory().create("value_dom2", null, "domval2")
        );
    }

    @Test
    void getAllKeyValuesWithResolverGivesFilteredResult() {
        roperty.set("key1", "value_1", "desc");
        roperty.set("key1", "value_dom1", "desc", "domval1");
        roperty.set("key1", "value_dom_2", "desc", "domval1", "domval2");
        roperty.set("key1", "value_dom_*", "desc", "*", "domval_other_2");
        roperty.set("key1", "value_dom_another", "desc", "domval_another");
        roperty.set("key1", "value_dom_other", "desc", "domval_other", "domval2");
        final Object o = roperty.get("key1", new MapBackedDomainResolver().set("dom1", "any").set("dom2", "domval_other_2"));

        Collection<KeyValues> allKeyValues = roperty.getKeyValues(new MapBackedDomainResolver().set("dom1", "domval1"));
        assertThat(allKeyValues).hasSize(1);

        final KeyValues keyValues = allKeyValues.iterator().next();
        assertThat(keyValues.getKey()).isEqualTo("key1");
        final Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("value_1", null),
            new DefaultDomainSpecificValueFactory().create("value_dom1", null, "domval1"),
            new DefaultDomainSpecificValueFactory().create("value_dom_2", null, "domval1", "domval2"),
            new DefaultDomainSpecificValueFactory().create("value_dom_*", null, "*", "domval_other_2")
        );
    }

    @Test
    void getAllKeyValuesWithResolverWithMultipleKeysGivesFilteredResult() {
        roperty.set("key1", "value_1", "desc");
        roperty.set("key1", "value_dom1", "desc", "domval1");
        roperty.set("key1", "value_dom_other", "desc", "domval_other");

        roperty.set("key2", "key2_dom1", "desc", "domval1");
        roperty.set("key2", "key2_dom_other", "desc", "domval_other");

        Collection<KeyValues> allKeyValues = roperty.getKeyValues(new MapBackedDomainResolver().set("dom1", "domval1"));
        assertThat(allKeyValues).hasSize(2);

        final KeyValues keyValues = allKeyValues.stream().filter(kv -> kv.getKey().equals("key1")).findFirst().orElseThrow();
        assertThat(keyValues.getKey()).isEqualTo("key1");
        final Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("value_1", null),
            new DefaultDomainSpecificValueFactory().create("value_dom1", null, "domval1")
        );

        final KeyValues keyValues2 = allKeyValues.stream().filter(kv -> kv.getKey().equals("key2")).findFirst().orElseThrow();
        assertThat(keyValues2.getKey()).isEqualTo("key2");
        final Set<DomainSpecificValue> domainSpecificValues2 = keyValues2.getDomainSpecificValues();
        assertThat(domainSpecificValues2).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("key2_dom1", null, "domval1")
        );
    }

    @Test
    void keysInAChangeSetAreNotReturnedWhenTheChangeSetIsNotActive() {
        roperty.set("key1", "value_dom1", "desc", "domval1");
        roperty.setWithChangeSet("key1", "CS_value", "desc", "ChangeSet", "domval1");

        Collection<KeyValues> allKeyValues = roperty.getKeyValues(new MapBackedDomainResolver().set("dom1", "domval1"));
        assertThat(allKeyValues).hasSize(1);

        final KeyValues keyValues = allKeyValues.iterator().next();
        assertThat(keyValues.getKey()).isEqualTo("key1");
        final Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("value_dom1", null, "domval1")
        );
    }

    @Test
    void theHighestPrecedenceKeysInAChangeSetAreReturnedWhenTheChangeSetIsActive() {
        roperty.set("key1", "value_dom1", "desc", "domval1");
        roperty.set("key1", "val", "desc", "domval1", "domval2");
        roperty.setWithChangeSet("key1", "CS_value", "desc", "ChangeSet", "domval1");
        roperty.setWithChangeSet("key1", "ACS_value", "desc", "AChangeSet", "domval1");

        final Object o = roperty.get("key1",
            new MapBackedDomainResolver().set("dom1", "domval1").addActiveChangeSets("ChangeSet", "AChangeSet"));

        Collection<KeyValues> allKeyValues = roperty.getKeyValues(new MapBackedDomainResolver()
            .set("dom1", "domval1")
            .addActiveChangeSets("ChangeSet", "AChangeSet"));
        assertThat(allKeyValues).hasSize(1);

        final KeyValues keyValues = allKeyValues.iterator().next();
        assertThat(keyValues.getKey()).isEqualTo("key1");
        final Set<DomainSpecificValue> domainSpecificValues = keyValues.getDomainSpecificValues();
        assertThat(domainSpecificValues).containsExactlyInAnyOrder(
            new DefaultDomainSpecificValueFactory().create("val", null, "domval1", "domval2"),
            new DefaultDomainSpecificValueFactory().create("ACS_value", "AChangeSet", "domval1")
        );
    }
}
