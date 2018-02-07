package com.matson.tos.dao;

import java.sql.*;

import org.apache.log4j.Logger;

/**
 *   DBUtil
 *   Utility Class to manage the database connections
 */
public class DBUtil {
    /**
     * log4j class
     */
    public static Logger LOGGER = Logger.getLogger(DBUtil.class);
	/**
     * Close a Connection, avoid closing if null.
     */
    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Close a ResultSet, avoid closing if null.
     */
    public static void close(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    /**
     * Close a Statement, avoid closing if null.
     */
    public static void close(Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    /**
     * Close a Connection, avoid closing if null and hide
     * any SQLExceptions that occur.
     */
    public static void closeQuietly(Connection conn) {
        try {
            close(conn);
        } catch (SQLException sqle) {
            // quiet
        }
    }

    /**
     * Close a Connection, Statement and
     * ResultSet.  Avoid closing if null and hide any
     * SQLExceptions that occur.
     */
    public static void closeQuietly(Connection conn, Statement stmt,ResultSet rs) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    /**
     * Close a ResultSet, avoid closing if null and hide
     * any SQLExceptions that occur.
     */
    public static void closeQuietly(ResultSet rs) {
        try {
            close(rs);
        } catch (SQLException sqle) {
            // quiet
        }
    }

    /**
     * Close a Statement, avoid closing if null and hide
     * any SQLExceptions that occur.
     */
    public static void closeQuietly(Statement stmt) {
        try {
            close(stmt);
        } catch (SQLException sqle) {
            // quiet
        }
    }

    /**
     * Commits a Connection then closes it, avoid closing if null.
     */
    public static void commitAndClose(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.close();
        }
    }

    /**
     * Commits a Connection then closes it, avoid closing if null
     * and hide any SQLExceptions that occur.
     */
    public static void commitAndCloseQuietly(Connection conn) {
        try {
            commitAndClose(conn);
        } catch (SQLException sqle) {
            // quiet
        }
    }

    /**
     * Print the stack trace to the logger class.
     * @param sqle
     */
    public static void printStackTrace(SQLException sqle) {

        SQLException next = sqle;
        String message = "Exception";
        while (next != null) {
            LOGGER.error(message,next);
            next = next.getNextException();
            message = "Next Exception";
        }
    }

    /**
     * Print the warnings to logger class.
     * @param conn
     */
    public static void printWarnings(Connection conn) {
        if (conn != null) {
            try {
                printStackTrace(conn.getWarnings());
            } catch (SQLException sqle) {
                printStackTrace(sqle);
            }
        }
    }

    /**
     * Rollback any changes made on the given connection.
     * @param conn The database Connection to rollback.  A null value is legal.
     * @throws SQLException
     */
    public static void rollback(Connection conn) throws SQLException {
        if (conn != null) {
            conn.rollback();
        }
    }

}
