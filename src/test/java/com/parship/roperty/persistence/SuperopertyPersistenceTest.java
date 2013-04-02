package com.parship.roperty.persistence;

import com.parship.roperty.Roperty;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 15:15
 */
public class SuperopertyPersistenceTest {

	@Test
	public void init() {
		Roperty roperty = new Roperty();
		SuperopertyPersistence persistence = new SuperopertyPersistence(roperty);
		persistence.loadAll();
		assertThat((String)roperty.get("key"), is("value"));
	}
}
