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
public class Persistence {

	private final DataSource dataSource;

	public static interface ResultSetHandler {
		void handle(ResultSet rs) throws SQLException;
	}

	public Persistence(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Persistence(final String url, final String user, final String password) {
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

	public synchronized void executeSql(final String sql) throws SQLException {
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

	public synchronized void executeQuery(final String sql, final ResultSetHandler rsh) throws SQLException {
		Connection con = dataSource.getConnection();
		try {
			Statement stmt = con.createStatement();
			stmt.setFetchSize(1000);
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
