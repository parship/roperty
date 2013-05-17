/*
 * Roperty - An advanced property management and retrival system
 * Copyright (C) 2013 PARSHIP GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.parship.roperty.persistence;

import com.parship.roperty.DomainResolver;
import com.parship.roperty.Roperty;
import com.parship.roperty.RopertyWithResolver;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 15:15
 */
public class SuperopertyPersistenceTest {

	private static final String URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
	private static final String USER = "user";
	private static final String PASSWORD = "xxx";

	private Roperty r = new Roperty();
	private RopertyWithResolver roperty;
	private SuperopertyPersistence persistence;

	private static final String CREATE_BASE_TABLE = "CREATE TABLE base_property ( " +
		"id bigint, " +
		"property_name character varying(255) NOT NULL, " +
		"converter_class character varying(255), " +
		"converter_config character varying(255), " +
		"description character varying(1000), " +
		"default_value text, " +
		"inheritance_type character varying(255), " +
		"container_name character varying(255) NOT NULL, " +
		"last_changed timestamp, " +
		"change_user character varying(255), " +
		"version bigint DEFAULT 0 NOT NULL, " +
		"app_version character varying(30), " +
		"ctime timestamp DEFAULT now())";

	private static final String CREATE_DOMAIN_TABLE = "CREATE TABLE domain_property ( " +
		"id bigint, " +
		"base_property bigint NOT NULL, " +
		"domain character varying(255) NOT NULL, " +
		"overridden_value text, " +
		"last_changed timestamp, " +
		"change_user character varying(255), " +
		"version bigint DEFAULT 0 NOT NULL, " +
		"app_version character varying(30))";

	private static SqlPersistence PERSISTENCE;

	@BeforeClass
	public static void beforeClass() throws SQLException {
		PERSISTENCE = getPersistence();
	}

	@Before
	public void before() {
		persistence = new SuperopertyPersistence(PERSISTENCE);
		DomainResolver resolverMock = mock(DomainResolver.class);
		when(resolverMock.getDomainValue("container")).thenReturn("container");
		when(resolverMock.getDomainValue("country")).thenReturn("DE");
		when(resolverMock.getDomainValue("language")).thenReturn("de");
		when(resolverMock.getDomainValue("orientation")).thenReturn("hetero");
		when(resolverMock.getDomainValue("owner")).thenReturn("1234");
		roperty = new RopertyWithResolver(r, resolverMock);
	}

	@Test
	public void basePropertiesAreRead() throws SQLException {
		PERSISTENCE.executeSql("INSERT INTO base_property (property_name, container_name, default_value, converter_class) VALUES ('key', 'container', 'value', " +
			"'com.freiheit.superoperty.converter.TextareaRegexCheckConverter')");
		this.persistence.load(r);
		assertThat((String)roperty.get("key"), is("value"));
	}

	@Test
	public void domainPropertiesAreRead() throws SQLException {
		PERSISTENCE.executeSql("INSERT INTO base_property (id, property_name, container_name, default_value) VALUES (1, 'key', 'container', 'value')");
		PERSISTENCE.executeSql("INSERT INTO domain_property (base_property, domain, overridden_value) VALUES (1, 'LOCALE_de_DE', 'overridden')");
		this.persistence.load(r);
		assertThat((String)roperty.get("key"), is("overridden"));
	}

	private static SqlPersistence getPersistence() throws SQLException {
		SqlPersistence persistence = new SqlPersistence(URL, USER, PASSWORD);
		persistence.executeSql(CREATE_BASE_TABLE);
		persistence.executeSql(CREATE_DOMAIN_TABLE);
		return persistence;
	}

	@Test
	public void stripPrefix() {
		assertThat(persistence.stripPrefix("COUNTRY_DE"), is("DE"));
	}

	@Test
	public void suffix() {
		assertThat(persistence.suffix("LOCALE_de_DE"), is("DE"));
	}

	@Test
	public void prefix() {
		assertThat(persistence.prefix("LOCALE_de_DE"), is("LOCALE"));
	}

	@Test
	public void countryDomain() {
		assertThat(persistence.buildDomainKey("container", "COUNTRY_DE"), is(new String[]{"container", "DE"}));
	}

	@Test
	public void localeDomain() {
		assertThat(persistence.buildDomainKey("container", "LOCALE_de_DE"), is(new String[]{"container", "DE", "de"}));
	}

	@Test
	public void orientationDomain() {
		assertThat(persistence.buildDomainKey("container", "ORIENTATION_GAY_es_MX"), is(new String[]{"container", "MX", "es", "GAY"}));
	}

	@Test
	public void partnerDomain() {
		assertThat(persistence.buildDomainKey("container", "PARTNER_103_de_AT"), is(new String[]{"container", "AT", "de", "*", "103"}));
	}
}
