package com.parship.roperty;

/**
 * Created by Benjamin Jochheim on 10.11.15.
 *
 * Uses String.intern() representation on String values, to save memory.
 */
public class DomainSpecificValueFactoryWithStringInterning extends AbstractDomainSpecificValueFactory implements DomainSpecificValueFactory {
    @Override
    public DomainSpecificValue create(final Object value, final String changeSet, final String... domainKeyParts) {
        Object internValue=internIfInstanceOfString(value);
        String internChangeSet=(String)internIfInstanceOfString(changeSet);
        String[] internDomainKeyParts=internDomainKeyParts(domainKeyParts);

        if (internDomainKeyParts.length == 0) {
            return new DomainSpecificValue(new OrderedDomainPattern("", 1), internValue, internChangeSet);
        }

        return new DomainSpecificValue(calculateOrderedDomainPattern(internDomainKeyParts), internValue, internChangeSet);
    }

    private static String[] internDomainKeyParts(String[] domainKeyParts) {
        String[] internDomainKeyParts;

        internDomainKeyParts=new String[domainKeyParts.length];
        int position=0;
        for (String domainKeyPart : domainKeyParts) {
            internDomainKeyParts[position] = domainKeyPart.intern();
            position++;
        }
        return internDomainKeyParts;
    }

    private static Object internIfInstanceOfString(Object o) {
        if(o instanceof String) {
            return ((String) o).intern();
        } else {
            return o;
        }
    }
}
