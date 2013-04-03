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

	@Test
	public void getAWildcardOverriddenValueIsReturned_2() {
		keyValues.put("value_1", "*", "*", "domain3");
		keyValues.put("value_2", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value_2"));
	}

	@Test
	public void getAWildcardOverriddenValueIsReturned_3() {
		keyValues.put("value_1", "aaa", "*", "domain3");
		keyValues.put("value_2", "domain1", "*", "domain3");
		assertThat((String)keyValues.get(arrayList("domain1", "domain2", "domain3"), resolver), is("value_2"));
	}

}
