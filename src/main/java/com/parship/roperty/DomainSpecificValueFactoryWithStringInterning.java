package com.parship.roperty;

/**
 * Created by Benjamin Jochheim on 10.11.15.
 * <p>
 * Uses String.intern() representation on String values, to save memory.
 */
public class DomainSpecificValueFactoryWithStringInterning implements DomainSpecificValueFactory {

    @Override
    public DomainSpecificValue create(final Object value, final String changeSet, final String... domainKeyParts) {
        return DomainSpecificValue.withChangeSet(internIfString(value), intern(changeSet), domainKeyParts);
    }

    @Override
    public DomainSpecificValue createFromPattern(Object value, String changeSet, String pattern) {
        return DomainSpecificValue.withPattern(internIfString(value), intern(changeSet), intern(pattern));
    }

    private static String intern(String s) {
        return s == null ? null : s.intern();
    }

    private static <T> T internIfString(T o) {
        if (o instanceof String) {
            return (T) ((String) o).intern();
        } else {
            return o;
        }
    }
}
