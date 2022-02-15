package com.parship.roperty;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class RopertyGetAllMappings {

    private final RopertyImpl roperty = new RopertyImpl();
    private final MapBackedDomainResolver resolver = new MapBackedDomainResolver();

    @Test
    void getAnEmptyMapFromAnEmptyRoperty() {
        assertThat(roperty.getAllMappings(resolver)).isEmpty();
    }

    @Test
    void ropertyWithOneDefaultValueIsReturnedInMapping() {
        roperty.set("key", "value", "desc");
        assertThat(roperty.getAllMappings(resolver)).hasSize(1);
        assertThat(roperty.getAllMappings(resolver)).containsAllEntriesOf(Map.of("key", "value"));
    }

    @Test
    void onlyTheBestMatchingValueIsUsed() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key", "value", "desc");
        roperty.set("key", "value1", "desc", "*", "dom2");
        roperty.set("key", "value2", "desc", "dom1", "dom2");
        resolver.set("domain1", "dom1").set("domain2", "dom2");
        assertThat(roperty.getAllMappings(resolver)).hasSize(1);
        assertThat(roperty.getAllMappings(resolver)).containsAllEntriesOf(Map.of("key", "value2"));
    }

    @Test
    void partialMappingsAreReturned() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key", "value1", "desc", "*", "dom2");
        roperty.set("key", "value2", "desc", "dom1", "dom2");
        roperty.set("key", "value3", "desc", "dom1", "*");
        roperty.set("key2", "otherValue", "desc");
        assertThat(roperty.getAllMappings("dom1")).hasSize(2);
        assertThat(roperty.getAllMappings("dom1")).containsAllEntriesOf(Map.of("key", "value3", "key2", "otherValue"));
    }

    @Test
    void keysWithoutAMatchingDomainValueAreNotReturned() {
        roperty.addDomains("domain1", "domain2");
        roperty.set("key", "value1", "desc", "*", "dom2");
        roperty.set("key", "value2", "desc", "dom1", "dom2");
        roperty.set("key2", "otherValue", "desc");
        resolver.set("domain1", "dom1");
        assertThat(roperty.getAllMappings(resolver)).hasSize(1);
        assertThat(roperty.getAllMappings(resolver)).containsAllEntriesOf(Map.of("key2", "otherValue"));
    }
}
