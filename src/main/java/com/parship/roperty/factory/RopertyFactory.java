package com.parship.roperty.factory;

import java.util.List;
import java.util.Map;

import com.parship.roperty.Persistence;
import com.parship.roperty.RopertyImpl;
import com.parship.roperty.domainspecificvalue.DefaultDomainSpecificValueFactory;
import com.parship.roperty.domainspecificvalue.DomainSpecificValue;
import com.parship.roperty.domainspecificvalue.DomainSpecificValueFactory;
import com.parship.roperty.jmx.RopertyManagerFactory;
import com.parship.roperty.keyvalues.DefaultKeyValuesFactory;
import com.parship.roperty.keyvalues.KeyValues;
import com.parship.roperty.keyvalues.KeyValuesFactory;

import static java.util.Objects.requireNonNull;


/**
 * Created by dheid on 6/15/17.
 */
public class RopertyFactory {

    public static <D extends DomainSpecificValue, K extends KeyValues<D>> RopertyImpl<D, K> createRoperty(
        Persistence<D, K> persistence,
        DomainInitializer domainInitializer,
        FactoryProvider<D, K> factoryProvider
    ) {
        return createRoperty(
            persistence,
            domainInitializer,
            factoryProvider.getKeyValuesFactory(),
            factoryProvider.getDomainSpecificValueFactory()
        );
    }

    public static <D extends DomainSpecificValue, K extends KeyValues<D>> RopertyImpl<D, K> createRoperty(
        Persistence<D, K> persistence,
        FactoryProvider<D, K> factoryProvider,
        String... domains
    ) {
        return createRoperty(
            persistence,
            factoryProvider.getKeyValuesFactory(),
            factoryProvider.getDomainSpecificValueFactory(),
            domains
        );
    }

    public static <D extends DomainSpecificValue, K extends KeyValues<D>> RopertyImpl<D, K> createRoperty(
        Persistence<D, K> persistence,
        KeyValuesFactory<D, K> keyValuesFactory,
        DomainSpecificValueFactory<D> domainSpecificValueFactory,
        String... domains
    ) {
        RopertyImpl<D, K> roperty = createRoperty();
        roperty.addDomains(domains);

        requireNonNull(keyValuesFactory, "\"keyValuesFactory\" must not be null");
        requireNonNull(domainSpecificValueFactory, "\"domainSpecificValueFactory\" must not be null");
        requireNonNull(persistence, "\"persistence\" must not be null");

        roperty.setPersistence(persistence);
        roperty.setKeyValuesFactory(keyValuesFactory);
        roperty.setDomainSpecificValueFactory(domainSpecificValueFactory);

        Map<String, K> allValues = persistence.loadAll(keyValuesFactory, domainSpecificValueFactory);
        roperty.setKeyValuesMap(allValues);

        return roperty;
    }

    public static <D extends DomainSpecificValue, K extends KeyValues<D>> RopertyImpl<D, K> createRoperty(
        Persistence<D, K> persistence,
        DomainInitializer domainInitializer,
        KeyValuesFactory<D, K> keyValuesFactory,
        DomainSpecificValueFactory<D> domainSpecificValueFactory
    ) {

        requireNonNull(domainInitializer, "\"domainInitializer\" must not be null");
        List<String> initialDomains = domainInitializer.getInitialDomains();

        return createRoperty(
            persistence,
            keyValuesFactory,
            domainSpecificValueFactory,
            initialDomains.toArray(new String[initialDomains.size()])
        );
    }

    public static RopertyImpl<DomainSpecificValue, KeyValues<DomainSpecificValue>> createRopertyWithoutPersistence(
        String... domains
    ) {
        RopertyImpl<DomainSpecificValue, KeyValues<DomainSpecificValue>> roperty = createRoperty();
        roperty.addDomains(domains);
        roperty.setKeyValuesFactory(new DefaultKeyValuesFactory<>());
        roperty.setDomainSpecificValueFactory(new DefaultDomainSpecificValueFactory());
        return roperty;
    }

    public static RopertyImpl<DomainSpecificValue, KeyValues<DomainSpecificValue>> createRoperty(
        Persistence<DomainSpecificValue, KeyValues<DomainSpecificValue>> persistence,
        DomainInitializer domainInitializer
    ) {
        return createRoperty(
            persistence,
            domainInitializer,
            new DefaultKeyValuesFactory<>(),
            new DefaultDomainSpecificValueFactory());
    }

    public static RopertyImpl<DomainSpecificValue, KeyValues<DomainSpecificValue>> createRoperty(
        Persistence<DomainSpecificValue, KeyValues<DomainSpecificValue>> persistence,
        String... domains
    ) {
        return createRoperty(persistence,
            new DefaultKeyValuesFactory<>(),
            new DefaultDomainSpecificValueFactory(),
            domains);
    }

    public static <D extends DomainSpecificValue, K extends KeyValues<D>> RopertyImpl<D, K> createRoperty() {
        RopertyImpl<D, K> ropertyImpl = new RopertyImpl<>();
        RopertyManagerFactory.getRopertyManager().add(ropertyImpl);
        return ropertyImpl;
    }

}
