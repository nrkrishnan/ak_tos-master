package com.matson.tos.dao;
 
import java.sql.*;

/**
 *   BaseDAO
 *   Base class for Database access.
 *   Created on Nov 1, 2005
 *
 *   File: BaseDAO.java
 * 
 * <PRE>
 *  Change History
 *  Ver Name    Date     Comment
 *  1.0 SKB     Nov 1, 2005  Created
 * </PRE>
 */
public class BaseDAO {
	protected Connection conn;
	
    /**
     * Constructor
     * @param conn
     */
	public BaseDAO(Connection conn) {
		this.conn = conn;
	}
    
    /**
     * Constructor
     *
     */
	public BaseDAO(){}
		
    /**
     * setConnection, Set the Connection
     * @param conn
     */
	public void setConnection(Connection conn){
		this.conn = conn;
	}
    
	/***
	 * Close the connection
	 *
	 */
    public void close() {
        close(conn);
    }
	/**
	 * @param connection
	 * Closes connection
	 */
	public void close(final Connection connection) {
		try {
			DBUtil.close(connection);
		} catch (final SQLException sqlException) {
			
		}
	}
	/**
	* @param pstmt Prepared Statement
	* Closes connection
	*/
	public void close(final PreparedStatement pstmt) {
		try {
			DBUtil.close(pstmt);
		} catch (final SQLException sqlException) {
			
		}
	}
	/**
	 * @param stmt
	 * Closes statement
	 */
	public void close(final Statement stmt) {
		try {
			DBUtil.close(stmt);
		} catch (final SQLException sqlException) {
			
		}
	}
	/**
	 * @param rs
	 * Closes ResultSet
	 */
	public void close(final ResultSet rs) {
		try {
			DBUtil.close(rs);
		} catch (final SQLException sqlException) {
			
		}
	}
	/**
	 * @param stmt
	 * @param cn
	 */
	public void close(final Statement stmt, final Connection cn) {
		close(stmt);
		close(cn);
	}
	/**
	 * @param pstmt
	 * @param rs
	 */
	public void close(final PreparedStatement pstmt, final ResultSet rs) {
		close(pstmt);
		close(rs);
	}
	/**
	 * @param pstmt
	 * @param cn
	 */
	public void close(final PreparedStatement pstmt, final Connection cn) {
		close(pstmt);
		close(cn);
	}
	/**
	 * @param cn
	 * @param stmt
	 * @param rs
	 * Closes resultset, statement connection
	 */
	public void close(final Connection cn, final Statement stmt, ResultSet rs) {
		close(stmt);
		close(rs);
		close(cn);
	}
	/**
	 * @param cn
	 * @param pstmt
	 * @param rs
	 * closes prepared statement, resultset, connection
	 */
	public void close(final Connection cn, final PreparedStatement pstmt, ResultSet rs) {
		close(pstmt);
		close(rs);
		close(cn);
	}

	/**
	 * @param pst
	 * @param index
	 * @param floatParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, Float floatParam) throws SQLException {
		if (floatParam == null) {
			pst.setNull(index, Types.FLOAT);
		} else {
			pst.setFloat(index, floatParam.floatValue());
		}
	}
	/**
	 * @param pst
	 * @param index
	 * @param floatParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, float floatParam) throws SQLException {
		pst.setFloat(index, floatParam);
	}
	/**
	 * @param pst
	 * @param index
	 * @param integerParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, Integer integerParam) throws SQLException {
		if (integerParam == null) {
			pst.setNull(index, Types.INTEGER);
		} else {
			pst.setInt(index, integerParam.intValue());
		}
	}
	/**
	 * @param pst
	 * @param index
	 * @param intParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, int intParam) throws SQLException {
		pst.setInt(index, intParam);
	}
	/**
	 * @param pst
	 * @param index
	 * @param stringParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, String stringParam) throws SQLException {
		if (stringParam == null) {
			pst.setNull(index, Types.VARCHAR);
		} else {
			pst.setString(index, stringParam.trim());
		}
	}
	/**
	 * @param pst
	 * @param index
	 * @param dateParam
	 * @throws SQLException
	 */
	public final void setParam(PreparedStatement pst, int index, Date dateParam) throws SQLException {
		if (dateParam == null) {
			pst.setNull(index, Types.DATE);
		} else {
			pst.setDate(index, new java.sql.Date(dateParam.getTime()));
		}
	}
	/**
	 * @param pst
	 * @param index
	 * @param dateParam
	 * @throws SQLException
	 */
	public final void setTimestampParam(PreparedStatement pst, int index, Timestamp dateParam) throws SQLException {
		if (dateParam == null) {
			pst.setNull(index, Types.DATE);
		} else {
			pst.setTimestamp(index, new Timestamp(dateParam.getTime()));
		}
	}
	/**
	 * @param rs
	 * @param column
	 * @return The Result Set Value
	 * @throws SQLException
	 */
	public final String getString(ResultSet rs, String column) throws SQLException {
		if (rs.getString(column) != null)
			return rs.getString(column).trim();
		return rs.getString(column);
	}
	/**
	 * @param rs
	 * @param column
	 * @return The Result Set Value
	 * @throws SQLException
	 */
	public final Integer getInteger(ResultSet rs, String column) throws SQLException {
		int intValue = rs.getInt(column);
		Integer integerValue = null;
		if (!rs.wasNull()) {
			integerValue = new Integer(intValue);
		}
		return integerValue;
	}
	/**
	 * @param rs
	 * @param column
	 * @return Result Set Value
	 * @throws SQLException
	 */
	public final int getInt(ResultSet rs, String column) throws SQLException {
		int intValue = rs.getInt(column);
		return intValue;
	}
	/**
	 * @param rs
	 * @param column
	 * @return Result Set Value
	 * @throws SQLException
	 */
	public final Date getTimestampAsDate(ResultSet rs, String column) throws SQLException {
		Date date = null;
		Timestamp timestamp = rs.getTimestamp(column);
		if (timestamp != null) {
			long time = timestamp.getTime() + (timestamp.getNanos() / 1000000);
			date = new Date(time);
		}
		return date;
	}
	/**
	 * @param rs
	 * @param column
	 * @return Result Set Value
	 * @throws SQLException
	 */
	public final Float getFloat(ResultSet rs, String column) throws SQLException {
		float fValue = rs.getFloat(column);
		Float floatValue = null;
		if (!rs.wasNull()){
			floatValue = new Float(fValue);
		}
		return floatValue;
	}
	
	
	
}

