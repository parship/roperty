package com.parship.roperty.factory;

import com.parship.roperty.Persistence;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


/**
 * Created by dheid on 6/15/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RopertyFactoryTest {

    @Mock
    private DomainInitializer domainInitializer;

    @Mock
    private Persistence<DomainSpecificValue, KeyValues<DomainSpecificValue>> persistence;

    @Mock
    private FactoryProvider<DomainSpecificValue, KeyValues<DomainSpecificValue>> factoryProvider;

    @Mock
    private KeyValuesFactory<DomainSpecificValue, KeyValues<DomainSpecificValue>> keyValueFactory;

    @Mock
    private DomainSpecificValueFactory<DomainSpecificValue> domainSpecificValueFactory;

    @After
    public void noInteractions() {
        verifyNoMoreInteractions(domainInitializer,  persistence);
    }

    @Test
    public void domainInitializerAndPersistenceAreUsedDuringInitialization() {
        RopertyFactory.createRoperty(persistence, domainInitializer);
        verify(domainInitializer).getInitialDomains();
        verify(persistence).loadAll(any(KeyValuesFactory.class), any(DomainSpecificValueFactory.class));
    }

    @Test
    public void usesKeyValueFactoryToCreateObjectsViaFactoryProvider() {

        // given
        given(factoryProvider.getKeyValuesFactory()).willReturn(keyValueFactory);
        given(factoryProvider.getDomainSpecificValueFactory()).willReturn(domainSpecificValueFactory);

        // when
        RopertyFactory.createRoperty(persistence, domainInitializer, factoryProvider);

        // then
        verify(persistence).loadAll(keyValueFactory, domainSpecificValueFactory);
        verify(domainInitializer).getInitialDomains();
        verify(factoryProvider).getKeyValuesFactory();
        verify(factoryProvider).getDomainSpecificValueFactory();

    }

}
