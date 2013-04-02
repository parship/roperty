package com.parship.roperty;

import com.parship.commons.util.CollectionUtil;
import org.junit.Test;

import static com.parship.commons.util.CollectionUtil.arrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 22:32
 */
public class KeyValuesTest {

	private KeyValues keyValues = new KeyValues();
	private Resolver resolver = new Resolver() {
		@Override
		public String getDomainValue(final String domain) {
			return domain;
		}
	};

	@Test
	public void getAWildcardOverriddenValueIsReturned() {
		keyValues.put("value", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value"));
	}

}
