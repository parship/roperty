package com.parship.roperty;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValuesStoreTest {

    @InjectMocks
    private ValuesStore valuesStore = new ValuesStore();
    @Mock
    private Persistence persistence;

    private DomainSpecificValueFactory domainSpecificValueFactory = new DefaultDomainSpecificValueFactory();
    private KeyValues keyValues = new KeyValues("key", domainSpecificValueFactory);

    @BeforeEach
    void before() {
        valuesStore.setDomainSpecificValueFactory(domainSpecificValueFactory);
    }

    @Test
    public void valuesAreEmptyOnInitialization() {
        assertThat(valuesStore.getAllValues().size(), is(0));
        assertThat(valuesStore.getValuesFor("key"), nullValue());
    }

    @Test
    public void cannotModifyMapFromOuterClass() {
        assertThrows(UnsupportedOperationException.class, () -> valuesStore.getAllValues().put("key", mock(KeyValues.class)));
        assertThat(valuesStore.getAllValues().size(), is(0));
    }

    @Test
    public void valuesShouldBeAdded() {
        Collection<KeyValues> values = new ArrayList<>();
        values.add(keyValues);

        valuesStore.setAllValues(values);

        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.dump(), is("\nKeyValues for \"key\": KeyValues{\n\tdescription=\"\"\n}"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadUnknownValuesFromPersistence() {
        when(persistence.load("key", domainSpecificValueFactory)).thenReturn(keyValues);

        KeyValues result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        verify(persistence).load("key", domainSpecificValueFactory);
        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadKnownValuesFromMap() {
        Collection<KeyValues> values = new ArrayList<>();
        values.add(keyValues);
        valuesStore.setAllValues(values);

        KeyValues result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeAdded() {
        KeyValues result = valuesStore.getOrCreateKeyValues("key", "description");

        assertThat(result.getDescription(), is("description"));

        assertThat(valuesStore.getValuesFor("key"), is(result));
    }

    @Test
    public void dumpShouldBeFormatted() {
        valuesStore.getOrCreateKeyValues("key", "description");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteArrayOutputStream);

        valuesStore.dump(out);

        assertThat(new String(byteArrayOutputStream.toByteArray()),
            is("\nKeyValues for \"key\": KeyValues{\n\tdescription=\"description\"\n}"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeRemoved() {
        valuesStore.getOrCreateKeyValues("key", "description");

        KeyValues result = valuesStore.remove("key");

        assertThat(valuesStore.getValuesFor("key"), not(is(result)));
        assertThat(valuesStore.getValuesFor("key"), not(is(keyValues)));
        assertThat(valuesStore.getAllValues().size(), is(0));
    }

    @Test
    public void valuesShouldBeReloaded() {
        Collection<KeyValues> values = new ArrayList<>();
        values.add(keyValues);
        when(persistence.reload(any(Map.class), eq(domainSpecificValueFactory))).thenReturn(values);

        valuesStore.getOrCreateKeyValues("another key", null);
        valuesStore.reload();

        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.getAllValues().size(), is(1));
    }


}
