package com.parship.roperty;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ValuesStoreTest {

    @InjectMocks
    private ValuesStore valuesStore = new ValuesStore();

    @Mock
    private KeyValues keyValues;

    @Mock
    private Persistence persistence;

    @Mock
    private KeyValuesFactory keyValuesFactory;
    
    @Mock
    private DomainSpecificValueFactory domainSpecificValueFactory;

    @Test
    public void valuesAreEmptyOnInitialization() {
        assertThat(valuesStore.getAllValues().size(), is(0));
        assertThat(valuesStore.getValuesFor("key"), nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cannotModifyMapFromOuterClass() {
        valuesStore.getAllValues().put("key", mock(KeyValues.class));
        assertThat(valuesStore.getAllValues().size(), is(0));
    }

    @Test
    public void valuesShouldBeAdded() {
        when(keyValues.toString()).thenReturn("keyValues.toString()");
        Map<String, KeyValues> values = new HashMap<>();
        values.put("key", keyValues);

        valuesStore.setAllValues(values);

        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.dump(), is("\nKeyValues for \"key\": keyValues.toString()"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadUnknownValuesFromPersistence() {
        when(persistence.load("key", keyValuesFactory, domainSpecificValueFactory)).thenReturn(keyValues);

        KeyValues result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        verify(persistence).load("key", keyValuesFactory, domainSpecificValueFactory);
        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadKnownValuesFromMap() {
        Map<String, KeyValues> values = new HashMap<>();
        values.put("key", keyValues);
        valuesStore.setAllValues(values);

        KeyValues result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeAdded() {
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        KeyValues result = valuesStore.getOrCreateKeyValues("key", "description");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(keyValues).setDescription(captor.capture());

        assertThat(valuesStore.getValuesFor("key"), is(result));
        assertThat(captor.getValue(), is("description"));
    }

    @Test
    public void dumpShouldBeFormatted() {
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);
        valuesStore.getOrCreateKeyValues("key", "description");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteArrayOutputStream);


        valuesStore.dump(out);

        assertThat(new String(byteArrayOutputStream.toByteArray()), is("\nKeyValues for \"key\": keyValues"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeRemoved() {
        when(keyValuesFactory.create(domainSpecificValueFactory)).thenReturn(keyValues);

        valuesStore.getOrCreateKeyValues("key", "description");

        KeyValues result = valuesStore.remove("key");

        assertThat(valuesStore.getValuesFor("key"), not(is(result)));
        assertThat(valuesStore.getValuesFor("key"), not(is(keyValues)));
        assertThat(valuesStore.getAllValues().size(), is(0));
    }
    
    @Test
    public void valuesShouldBeReloaded() {
        Map<String, KeyValues> values = new HashMap<>();
        values.put("key", keyValues);
        when(persistence.reload(any(Map.class), eq(keyValuesFactory), eq(domainSpecificValueFactory))).thenReturn(values);

        valuesStore.getOrCreateKeyValues("another key", null);
        valuesStore.reload();

        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.getAllValues().size(), is(1));
    }


}