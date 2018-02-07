package com.matson.tos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

public class CargoStatusDao extends BaseDAO {
    private static final String insert = "insert into TOSCSTMMGR.TOS_CARGO_STATUS_ARCHIEVE_MT (ID, CTRID, VESVOY, BOOKING, LOCATION, DISTRIBUTION, BODY, CREATE_USER, CREATE_DATE) values (TOSCSTMMGR.TOS_CARGO_STATUS_ARCHIEVE_SEQ.nextval,?,?,?,?,?,?,?,?)";
	private static final String select = "select BODY from TOSCSTMMGR.TOS_CARGO_STATUS_ARCHIEVE_MT where ID = ?";
    
	private static Logger LOGGER = Logger.getLogger(CargoStatusDao.class);
	/**
     * Constructor
     * @param conn
     */
    public CargoStatusDao(Connection conn) {
        super(conn);
    }

    /**
     * Constructor
     *
     */
    public CargoStatusDao() throws Exception { 
    	super(); 
    	setConnection(DBConnection.getCstmConnection(true));
    }
    
    /**
     * Get a list of searches
     * @return
     * @throws SQLException
     */
    public String insertMsg(String CTRID,String VESVOY, String BOOKING, String location, String distribution, String body ,String creator, Timestamp date) throws SQLException {
    	 PreparedStatement pstmt = null;
      try {
    	  if(distribution != null && distribution.length() > 1000) distribution = distribution.substring(0,1000);
    	  if(body != null && body.length() > 4000) body = body.substring(0,4000);
     	 
    	  LOGGER.debug("Insert for cnt "+CTRID);
          pstmt = conn.prepareStatement(insert);
	      pstmt.setString(1, CTRID); 
	      pstmt.setString(2, VESVOY);
	      pstmt.setString(3, BOOKING);
	      pstmt.setString(4, location);
	      pstmt.setString(5, distribution);
	      pstmt.setString(6, body);
	      pstmt.setString(7, creator);
	      pstmt.setTimestamp(8, date);
          
          pstmt.execute();
          conn.commit();
         } catch (SQLException e) {
             LOGGER.error("Could not insert cargo status "+body,e);
         } finally {
             close(pstmt);
         }
         return null;
    }
    
    public String getBody(String id) throws SQLException {
      	   PreparedStatement pstmt = null;
           ResultSet rs = null;
           String errorMessage = "<h3>Could not locate message body.</h3>";
        try {
            pstmt = conn.prepareStatement(select);
   	        pstmt.setString(1, id); 
            
            String body = null;
           	rs = pstmt.executeQuery();
           	
           	if(rs.next()) body = rs.getString(1);
           	else body = errorMessage;       
          	return body;
            
            
           } catch (SQLException e) {
               LOGGER.error("Could not get message body",e);
           } finally {
               close(rs);
               close(pstmt);
           }
           return errorMessage;
      }    
}
