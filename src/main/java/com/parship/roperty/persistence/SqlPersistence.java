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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;


/**
 * @author mfinsterwalder
 * @since 2013-04-02 20:19
 */
public class SqlPersistence {

	private final DataSource dataSource;
	private Boolean autoCommit;

	public static interface ResultSetHandler {
		void handle(ResultSet rs) throws SQLException;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public SqlPersistence(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SqlPersistence(final String url, final String user, final String password) {
		this(new DataSource() {
			@Override
			public Connection getConnection() throws SQLException {
				return DriverManager.getConnection(url, user, password);
			}

			@Override
			public Connection getConnection(final String username, final String password) throws SQLException {
				return null;
			}

			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}

			@Override
			public void setLogWriter(final PrintWriter out) throws SQLException {
			}

			@Override
			public void setLoginTimeout(final int seconds) throws SQLException {
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}

			@Override
			public <T> T unwrap(final Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(final Class<?> iface) throws SQLException {
				return false;
			}
		});
	}

	public void executeSql(final String sql) throws SQLException {
		Connection con = dataSource.getConnection();
		try {
			executeSql(con, sql);
		} finally {
			close(con);
		}
	}

	static void executeSql(final Connection conn, final String sql) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(sql);
		} finally {
			close(stmt);
		}
	}

	public void executeQuery(final String sql, final ResultSetHandler rsh) throws SQLException {
		executeQuery(sql, rsh, 0);
	}

	public void executeQuery(final String sql, final ResultSetHandler rsh, int fetchSize) throws SQLException {
		Connection con = dataSource.getConnection();
		setAutoCommitIfNeeded(con);
		try {
			Statement stmt = con.createStatement();
			stmt.setFetchSize(fetchSize);
			try {
				final ResultSet rs = stmt.executeQuery(sql);
				try {
					rsh.handle(rs);
				} finally {
					close(rs);
				}
			} finally {
				close(stmt);
			}
		} finally {
			close(con);
		}
	}

	private void setAutoCommitIfNeeded(final Connection con) throws SQLException {
		if (autoCommit != null) {
			con.setAutoCommit(autoCommit);
		}
	}

	private static void close(final ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				//
			}
		}
	}

	static void close(final Statement rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				//
			}
		}
	}

	private static void close(final Connection rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				//
			}
		}
	}
}
