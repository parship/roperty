package com.parship.roperty;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class ValuesStoreTest {

    @InjectMocks
    private ValuesStore<DomainSpecificValue, KeyValues<DomainSpecificValue>> valuesStore = new ValuesStore<>();

    @Mock
    private KeyValues<DomainSpecificValue> keyValues;

    @Mock
    private Persistence<DomainSpecificValue, KeyValues<DomainSpecificValue>> persistence;

    @Mock
    private KeyValuesFactory<DomainSpecificValue, KeyValues<DomainSpecificValue>> keyValuesFactory;

    @Mock
    private DomainSpecificValueFactory<DomainSpecificValue> domainSpecificValueFactory;

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
        given(keyValues.toString()).willReturn("keyValues.toString()");
        Map<String, KeyValues<DomainSpecificValue>> values = new HashMap<>();
        values.put("key", keyValues);

        valuesStore.setAllValues(values);

        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.dump(), is("\nKeyValues for \"key\": keyValues.toString()"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadUnknownValuesFromPersistence() {
        given(persistence.load("key", keyValuesFactory, domainSpecificValueFactory)).willReturn(keyValues);

        KeyValues<DomainSpecificValue> result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        verify(persistence).load("key", keyValuesFactory, domainSpecificValueFactory);
        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void loadKnownValuesFromMap() {
        Map<String, KeyValues<DomainSpecificValue>> values = new HashMap<>();
        values.put("key", keyValues);
        valuesStore.setAllValues(values);

        KeyValues<DomainSpecificValue> result = valuesStore.getKeyValuesFromMapOrPersistence("key");

        assertThat(result, is(keyValues));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeAdded() {
        given(keyValuesFactory.create(domainSpecificValueFactory)).willReturn(keyValues);
        KeyValues<DomainSpecificValue> result = valuesStore.getOrCreateKeyValues("key", "description");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(keyValues).setDescription(captor.capture());

        assertThat(valuesStore.getValuesFor("key"), is(result));
        assertThat(captor.getValue(), is("description"));
    }

    @Test
    public void dumpShouldBeFormatted() {
        given(keyValuesFactory.create(domainSpecificValueFactory)).willReturn(keyValues);
        valuesStore.getOrCreateKeyValues("key", "description");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteArrayOutputStream);

        valuesStore.dump(out);

        assertThat(new String(byteArrayOutputStream.toByteArray()), is("\nKeyValues for \"key\": keyValues"));
        assertThat(valuesStore.getAllValues().size(), is(1));
    }

    @Test
    public void valueShouldBeRemoved() {
        given(keyValuesFactory.create(domainSpecificValueFactory)).willReturn(keyValues);

        valuesStore.getOrCreateKeyValues("key", "description");

        KeyValues<DomainSpecificValue> result = valuesStore.remove("key");

        assertThat(valuesStore.getValuesFor("key"), not(is(result)));
        assertThat(valuesStore.getValuesFor("key"), not(is(keyValues)));
        assertThat(valuesStore.getAllValues().size(), is(0));
    }

    @Test
    public void valuesShouldBeReloaded() {

        // given
        Map<String, KeyValues<DomainSpecificValue>> values = new HashMap<>();
        values.put("key", keyValues);
        given(persistence.reload(any(Map.class), eq(keyValuesFactory), eq(domainSpecificValueFactory))).willReturn(values);
        given(keyValuesFactory.create(domainSpecificValueFactory)).willReturn(keyValues);

        // when
        valuesStore.getOrCreateKeyValues("another key", null);
        valuesStore.reload();

        // then
        assertThat(valuesStore.getValuesFor("key"), is(keyValues));
        assertThat(valuesStore.getValuesFor("another key"), nullValue());
        assertThat(valuesStore.getAllValues().size(), is(1));
        verify(keyValuesFactory).create(domainSpecificValueFactory);

    }

}
