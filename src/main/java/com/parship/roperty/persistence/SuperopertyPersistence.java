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
		try {
			persistence.executeQuery("SELECT property_name, default_value, container_name, domain, overridden_value " +
				"FROM base_property base left outer join domain_property domain ON base.id = domain.base_property limit 1000",
				new Persistence.ResultSetHandler() {
					@Override
					public void handle(final ResultSet rs) throws SQLException {
						while (rs.next()) {
							roperty.set(rs.getString(1), rs.getString(2));
							String overriddenValue = rs.getString(5);
							if (overriddenValue != null) {
								roperty.set(rs.getString(1), overriddenValue, domain(rs.getString(3), rs.getString(4)));
							}
						}
					}
				});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	String domain(final String container, final String domain) {
		if (domain == null || domain.length() == 0) {
			return container;
		}
		if (domain.startsWith("COUNTRY")) {
			return container + "|" + stripPrefix(domain);
		}
		if (domain.startsWith("LOCALE")) {
			return container + "|" + suffix(domain) + "|" + stripPrefix(domain);
		}
		if (domain.startsWith("ORIENTATION")) {
			return container + "|" + suffix(domain) + "|" + stripPrefix(stripPrefix(domain)) + "|" + prefix(stripPrefix(domain));
		}
		if (domain.startsWith("PARTNER")) {
			return container + "|" + suffix(domain) + "|" + stripPrefix(stripPrefix(domain)) + "|*|" + prefix(stripPrefix(domain));
		}
		return null;
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
