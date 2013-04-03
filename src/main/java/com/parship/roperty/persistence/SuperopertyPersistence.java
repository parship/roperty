package com.parship.roperty.persistence;

import com.parship.roperty.Roperty;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 15:16
 */
public class SuperopertyPersistence {

	private final Roperty roperty;
	private final Persistence persistence;

	public SuperopertyPersistence(final Roperty roperty) {
		this(roperty, new Persistence("jdbc:postgresql://localhost/superoperty", "parship", "freiheit"));
	}

	public SuperopertyPersistence(final Roperty roperty, Persistence persistence) {
		this.roperty = roperty;
		this.persistence = persistence;
	}

	public void loadAll() {
		long start = System.currentTimeMillis();
		try {
			persistence.executeQuery("SELECT property_name, default_value, container_name, domain, overridden_value " +
				"FROM base_property base left outer join domain_property domain ON base.id = domain.base_property",
				new Persistence.ResultSetHandler() {
					@Override
					public void handle(final ResultSet rs) throws SQLException {
						while (rs.next()) {
							String key = rs.getString(1);
							String defaultValue = rs.getString(2);
							String domain = rs.getString(4);
							String overriddenValue = rs.getString(5);
							if (defaultValue != null) {
								roperty.set(key, defaultValue);
							}
							if (overriddenValue != null && domain != null) {
								String container = rs.getString(3);
								try {
									roperty.set(key, overriddenValue, buildDomainKey(container, domain));
								} catch (Exception ex) {
									System.out.println("Could not build domain key for: " + domain);
								}
							}
						}
					}
				});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.out.println("Loading took: " + (end - start) + "ms");
	}

	String[] buildDomainKey(final String container, final String domain) {
		if (domain == null || domain.length() == 0) {
			return new String[]{container};
		}
		if (domain.startsWith("COUNTRY")) {
			return new String[]{container, stripPrefix(domain)};
		}
		if (domain.startsWith("LOCALE")) {
			return new String[]{container, suffix(domain), stripPrefix(domain)};
		}
		if (domain.startsWith("ORIENTATION")) {
			return new String[]{container, suffix(domain), stripPrefix(stripPrefix(domain)), prefix(stripPrefix(domain))};
		}
		if (domain.startsWith("PARTNER")) {
			return new String[]{container, suffix(domain), stripPrefix(stripPrefix(domain)), "*", prefix(stripPrefix(domain))};
		}
		throw new RuntimeException("Could not find mapping for domain key: " + domain);
	}

	String prefix(final String domain) {
		return domain.substring(0, domain.indexOf('_'));
	}

	String suffix(final String domain) {
		return domain.substring(domain.lastIndexOf("_") + 1);
	}

	String stripPrefix(final String domain) {
		return domain.substring(domain.indexOf('_') + 1);
	}
}
