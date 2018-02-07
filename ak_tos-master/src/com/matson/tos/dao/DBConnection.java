package com.matson.tos.dao;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;


/**
 * This class returns the  Connection from the Connection pool
 * dp.properties file used to get the connection pool name.
 * This file should be in server class path.
 *
 * @author Ravi Kanth
 * @version 1.0
 * @since June, 2005
 * 
 * SKB 3/1/13  Update for JBOSS/MYSQL
 */
public class DBConnection{

    /**
     * log4j Logger class
     */
    public static Logger LOGGER = Logger.getLogger(DBConnection.class);
    private static Properties m_dbProps=null;
    private static boolean testConnection = false;

    static {
        try {
        	 // This file doesn't exist currently
        	 // Could be added to do unit test or to change databases.
			 URL url = DBConnection.class.getClassLoader().getResource("tosdb.properties");
			 
			 m_dbProps = new Properties();
			 if(url != null) {
				 InputStream in = url.openStream();
				 m_dbProps.load(in);
				 in.close();
				 String testConnectionString = m_dbProps.getProperty("TestConnection");
				 if("TRUE".equalsIgnoreCase(testConnectionString )) {
					 testConnection = true;
				 }	
			 }
		} catch (Exception e) {
			LOGGER.error("Exception",e);
		}
    }
    /**
     * Private constructor used to initilize the properties value which is required for weblogic connection pool.
     *
     * @exception java.lang.Exception
     */
    private DBConnection()throws Exception{

    }

    /**
     * This method returns the java.sql.connection object.
     * The autocommit is set as false by default.
     * if any error occurs java.lang.Exception is thrown.
     *
     * @return java.sql.Connection
     * @exception java.lang.Exception
     */
    public static Connection getConnection() throws Exception{
        return  getConnection(false);
    }


    /**
     * This method returns the java.sql.connection object.
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public static Connection getConnection(boolean autoCommit) throws Exception {
    	String dataSource = "jdbc.apexDS";
    	return  getConnection(false,dataSource);
    }
    
    /**
     * This method returns the java.sql.connection object.
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public static Connection getCstmConnection(boolean autoCommit) throws Exception {
    	String dataSource = "jdbc.ds.tdp.nontx";
    	return  getConnection(false,dataSource);
    }


    /**
     * This method returns the java.sql.connection object.
     * @param autoCommit
     * @param dataSource
     * @return
     * @throws Exception
     */
    public static Connection getConnection(boolean autoCommit,String dataSource) throws Exception {
    	return getLocalConnection(autoCommit, dataSource);
    }

    /**
     * This method returns the java.sql.connection object.
     * Uses the local JNDI context
     * @param autoCommit
     * @param dataSource
     * @return
     * @throws Exception
     */
    public static Connection getLocalConnection(boolean autoCommit, String dataSource) throws Exception {
    	if(testConnection) return getDirectConnection(dataSource);
        Hashtable ht = new Hashtable();
    	return  getConnection(autoCommit,dataSource,ht);
    }

/**
 * Safely close a connection
 * @param conn
 */
 public static void closeConnection( Connection conn)
	 {
        try
        {

			if ( conn != null )
			{
				//conn.rollback();
        		conn.close();
			}
        }
        catch (Exception ex)
        {
        	LOGGER.error("got exception in closing conn");ex.printStackTrace();
        }
    }

    /**
     * This method returns the java.sql.connection object.
     * if any error occurs java.lang.Exception is thrown.
     *
     * @param autoCommit true-set autocommit true
     * false -sets auto commit false
     * @return java.sql.Connection
     * @exception java.lang.Exception
     */
   /* public static Connection getConnection(boolean autoCommit) throws Exception{
        if(m_dbProps==null) new DBConnection();
        weblogic.jdbc.pool.Driver d=new weblogic.jdbc.pool.Driver();
        Connection con= (Connection) d.connect("jdbc:weblogic:pool",m_dbProps);
        con.setAutoCommit(autoCommit);
        return con;
    }*/

    //This Method is added on 06/08/2005
    public static Connection getConnection(boolean autoCommit, String dataSource, Hashtable ht) throws Exception{
    	if(testConnection) return getDirectConnection(dataSource);
    	try {
           // if(m_dbProps==null) new DBConnection();
            Context ctx = null;
            ctx = new InitialContext(ht);
            javax.sql.DataSource ds  = (javax.sql.DataSource) ctx.lookup (dataSource);
            LOGGER.debug("after Data source");
            java.sql.Connection conn = ds.getConnection();
            LOGGER.debug("after Data connection");
            conn.setAutoCommit(autoCommit);
            ctx.close();
            return conn;
        } catch (NamingException e) {
            // Print the stack trace.
            LOGGER.error("Could not find the database driver",e);
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
            // Print the stack trace.
            LOGGER.error("Could not connect to the database",e);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            // Print the stack trace.
            LOGGER.error("Could not connect to the database",e);
            e.printStackTrace();
            throw e;
        }
    }
    
    public static Connection getDirectConnection(String datasource){
        Connection connection = null;
        try {
            if(m_dbProps==null) new DBConnection();
            
            // Load the JDBC driver
            String driverName = (String)m_dbProps.get(datasource+".driverClass");
            if(driverName == null) driverName = "oracle.jdbc.OracleDriver";  // Default to oracle for backward compatability
            Class.forName(driverName);
            String url = (String)m_dbProps.get(datasource+".DBUrl");
            String username = (String)m_dbProps.get(datasource+".User");
            String password = (String)m_dbProps.get(datasource+".Password");
            LOGGER.debug(datasource+".URL="+url);
            connection = DriverManager.getConnection(url,username,password);
            connection.setAutoCommit(false);
            return connection;
        } catch (ClassNotFoundException e) {
             LOGGER.error("Could not find the database driver",e);
        } catch (SQLException e) {
            LOGGER.error("Could not connect to the database",e);
        }catch (Exception e) {
            LOGGER.error("Could not connect to the database",e);
        }
        return null;

   }

}
