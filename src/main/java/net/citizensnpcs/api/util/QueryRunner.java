/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.citizensnpcs.api.util;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

/**
 * Executes SQL queries with pluggable strategies for handling
 * <code>ResultSet</code>s. This class is thread safe.
 * 
 * @see ResultSetHandler
 */
public class QueryRunner {

    /**
     * Is {@link ParameterMetaData#getParameterType(int)} broken (have we tried
     * it yet)?
     */
    private volatile boolean pmdKnownBroken = false;

    /**
     * Constructor for QueryRunner.
     */
    public QueryRunner() {
        super();
    }

    /**
     * Close a <code>Connection</code>. This implementation avoids closing if
     * null and does <strong>not</strong> suppress any exceptions. Subclasses
     * can override to provide special handling like logging.
     * 
     * @param conn
     *            Connection to close
     * @throws SQLException
     *             if a database access error occurs
     * @since DbUtils 1.1
     */
    private void close(Connection conn) throws SQLException {
        if (conn != null)
            conn.close();
    }

    /**
     * Close a <code>ResultSet</code>. This implementation avoids closing if
     * null and does <strong>not</strong> suppress any exceptions. Subclasses
     * can override to provide special handling like logging.
     * 
     * @param rs
     *            ResultSet to close
     * @throws SQLException
     *             if a database access error occurs
     * @since DbUtils 1.1
     */
    private void close(ResultSet rs) throws SQLException {
        if (rs != null)
            rs.close();
    }

    /**
     * Close a <code>Statement</code>. This implementation avoids closing if
     * null and does <strong>not</strong> suppress any exceptions. Subclasses
     * can override to provide special handling like logging.
     * 
     * @param stmt
     *            Statement to close
     * @throws SQLException
     *             if a database access error occurs
     * @since DbUtils 1.1
     */
    private void close(Statement stmt) throws SQLException {
        if (stmt != null)
            stmt.close();
    }

    /**
     * Fill the <code>PreparedStatement</code> replacement parameters with the
     * given objects.
     * 
     * @param stmt
     *            PreparedStatement to fill
     * @param params
     *            Query replacement parameters; <code>null</code> is a valid
     *            value to pass in.
     * @throws SQLException
     *             if a database access error occurs
     */
    private void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {

        // check the parameter count, if we can
        ParameterMetaData pmd = null;
        if (!pmdKnownBroken) {
            pmd = stmt.getParameterMetaData();
            int stmtCount = pmd.getParameterCount();
            int paramsCount = params == null ? 0 : params.length;

            if (stmtCount != paramsCount) {
                throw new SQLException("Wrong number of parameters: expected " + stmtCount + ", was given "
                        + paramsCount);
            }
        }

        // nothing to do here
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                stmt.setObject(i + 1, params[i]);
            } else {
                // VARCHAR works with many drivers regardless
                // of the actual column type. Oddly, NULL and
                // OTHER don't work with Oracle's drivers.
                int sqlType = Types.VARCHAR;
                if (!pmdKnownBroken) {
                    try {
                        sqlType = pmd.getParameterType(i + 1);
                    } catch (SQLException e) {
                        pmdKnownBroken = true;
                    }
                }
                stmt.setNull(i + 1, sqlType);
            }
        }
    }

    /**
     * Calls query after checking the parameters to ensure nothing is null.
     * 
     * @param conn
     *            The connection to use for the query call.
     * @param closeConn
     *            True if the connection should be closed, false otherwise.
     * @param sql
     *            The SQL statement to execute.
     * @param params
     *            An array of query replacement parameters. Each row in this
     *            array is one set of batch replacement values.
     * @return The results of the query.
     * @throws SQLException
     *             If there are database or parameter errors.
     */
    private <T> T query(Connection conn, boolean closeConn, String sql, ResultSetHandler<T> rsh, Object... params)
            throws SQLException {
        if (conn == null) {
            throw new SQLException("Null connection");
        }

        if (sql == null) {
            if (closeConn) {
                close(conn);
            }
            throw new SQLException("Null SQL statement");
        }

        if (rsh == null) {
            if (closeConn) {
                close(conn);
            }
            throw new SQLException("Null ResultSetHandler");
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        T result = null;

        try {
            stmt = conn.prepareStatement(sql);
            this.fillStatement(stmt, params);
            rs = stmt.executeQuery();
            result = rsh.handle(rs);

        } catch (SQLException e) {
            this.rethrow(e, sql, params);

        } finally {
            try {
                close(rs);
            } finally {
                close(stmt);
                if (closeConn) {
                    close(conn);
                }
            }
        }

        return result;
    }

    /**
     * Execute an SQL SELECT query with replacement parameters. The caller is
     * responsible for closing the connection.
     * 
     * @param <T>
     *            The type of object that the handler returns
     * @param conn
     *            The connection to execute the query in.
     * @param sql
     *            The query to execute.
     * @param rsh
     *            The handler that converts the results into an object.
     * @param params
     *            The replacement parameters.
     * @return The object returned by the handler.
     * @throws SQLException
     *             if a database access error occurs
     */
    public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
        return this.query(conn, false, sql, rsh, params);
    }

    /**
     * Throws a new exception with a more informative error message.
     * 
     * @param cause
     *            The original exception that will be chained to the new
     *            exception when it's rethrown.
     * 
     * @param sql
     *            The query that was executing when the exception happened.
     * 
     * @param params
     *            The query replacement parameters; <code>null</code> is a valid
     *            value to pass in.
     * 
     * @throws SQLException
     *             if a database access error occurs
     */
    private void rethrow(SQLException cause, String sql, Object... params) throws SQLException {
        String causeMessage = cause.getMessage();
        if (causeMessage == null) {
            causeMessage = "";
        }
        StringBuilder msg = new StringBuilder(causeMessage);

        msg.append(" Query: ");
        msg.append(sql);
        msg.append(" Parameters: ");

        if (params == null) {
            msg.append("[]");
        } else {
            msg.append(Arrays.deepToString(params));
        }

        SQLException e = new SQLException(msg.toString(), cause.getSQLState(), cause.getErrorCode());
        e.setNextException(cause);

        throw e;
    }

    /**
     * Calls update after checking the parameters to ensure nothing is null.
     * 
     * @param conn
     *            The connection to use for the update call.
     * @param closeConn
     *            True if the connection should be closed, false otherwise.
     * @param sql
     *            The SQL statement to execute.
     * @param params
     *            An array of update replacement parameters. Each row in this
     *            array is one set of update replacement values.
     * @return The number of rows updated.
     * @throws SQLException
     *             If there are database or parameter errors.
     */
    private int update(Connection conn, boolean closeConn, String sql, Object... params) throws SQLException {
        if (conn == null) {
            throw new SQLException("Null connection");
        }

        if (sql == null) {
            if (closeConn) {
                close(conn);
            }
            throw new SQLException("Null SQL statement");
        }

        PreparedStatement stmt = null;
        int rows = 0;

        try {
            stmt = conn.prepareStatement(sql);
            this.fillStatement(stmt, params);
            rows = stmt.executeUpdate();

        } catch (SQLException e) {
            this.rethrow(e, sql, params);

        } finally {
            close(stmt);
            if (closeConn) {
                close(conn);
            }
        }

        return rows;
    }

    /**
     * Execute an SQL INSERT, UPDATE, or DELETE query with a single replacement
     * parameter.
     * 
     * @param conn
     *            The connection to use to run the query.
     * @param sql
     *            The SQL to execute.
     * @param param
     *            The replacement parameter.
     * @return The number of rows updated.
     * @throws SQLException
     *             if a database access error occurs
     */
    public int update(Connection conn, String sql, Object param) throws SQLException {
        return this.update(conn, false, sql, param);
    }

    /**
     * Execute an SQL INSERT, UPDATE, or DELETE query.
     * 
     * @param conn
     *            The connection to use to run the query.
     * @param sql
     *            The SQL to execute.
     * @param params
     *            The query replacement parameters.
     * @return The number of rows updated.
     * @throws SQLException
     *             if a database access error occurs
     */
    public int update(Connection conn, String sql, Object... params) throws SQLException {
        return update(conn, false, sql, params);
    }
}
