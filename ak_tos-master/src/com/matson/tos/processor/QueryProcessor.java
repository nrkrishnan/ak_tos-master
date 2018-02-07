package com.matson.tos.processor;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;




public class QueryProcessor   {

	private static Logger logger = Logger.getLogger(QueryProcessor.class);
	private static SessionFactory sessionFact;
	public static String  CONIG_FILE = "/tosrefdata.cfg.xml";
	
	public synchronized static void createHibernateSessionFactory()
	{
		try
		{
			if (sessionFact == null) {
	            try {
	            	logger.info("session factory creation begins");
	                Configuration cfg = new Configuration();
	                cfg.configure(CONIG_FILE);
	                sessionFact = cfg.buildSessionFactory();
	                //sessionFactoryMap.put(CONIG_FILE,sessionFact);
	                logger.info("session factory creation ends");
	            } catch (Exception e) {
	            	logger.error("%%%% Error Creating SessionFactory %%%%", e);
	            }
	        }
		}
		catch (Exception e)
		{
			logger.error("Error in creating hibernate session: ", e);
		}
	}
		
	public static void closeHibernateSession(Session session)
	{
		session.close();
	}
	public static Transaction beginTransaction(Session session)
	{
		return session.beginTransaction();
	}
	public static void commitTransaction(Transaction transaction)
	{
		transaction.commit();
	}
    

//	public String executeQuery(String query) throws SQLException {
//		logger.info("executeQuery::Start");
//		
//		String result=null;
//		ResultSet rs=null;
//		try{
//		if (query == null || query.toUpperCase().indexOf("WHERE") < 1 ) 
//		    	throw new SQLException("Where what?");
//			DataSource  ds=	getSqlMapClient().getDataSource();
//		   Connection con=	ds.getConnection();//getSqlMapClient().getCurrentConnection();
//			Statement st=con.createStatement();
//			rs=st.executeQuery(query);
//			//result=getDecoratedResult(rs);
//		}finally{
//			try{if(!rs.isClosed())rs.close();}catch(Exception ignore){}
//		}
//		logger.info("executeQuery::End");
//		return result;
//	}

	
	public static void executeUpdate(String query, boolean canCommit) {

		logger.info("executeUpdate::Start");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			
			Transaction transaction = beginTransaction(session);
			System.out.println("query:"+query);
			if (query == null || query.trim().length()<= 0 ) 
		    	throw new SQLException("Where what?");
			 //Statement st=session.connection().createStatement();
			 //int result = st.executeUpdate(query);
			 
			 int result = session.createSQLQuery(query).executeUpdate();
			 System.out.println("rows updated:"+result);
//			Query qry = session.createQuery(query);
//			int rowCount = qry.executeUpdate();
//			if(rowCount > 0)
//				logger.debug("nothing updated");
//		
		if(!canCommit){
			transaction.rollback();
			throw new Exception(" It is Rollbacked \n");
		}else{
				commitTransaction(transaction);
			}
	
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			closeHibernateSession(session);
		}
		logger.info("executeUpdate::End");
		
	}
	
	
	
//	private String getDecoratedResult(ResultSet rs,EnablerDecorator decorator )throws SQLException{
//		StringBuffer sb = new StringBuffer();
//		int count = 0;
//		ResultSetMetaData md = rs.getMetaData();
//		// add in the header
//		sb.append(decorator.getHeader());
//		sb.append(decorator.getRowPrepend());
//		for (int i = 1; i <= md.getColumnCount(); i++) {
//			sb.append(decorator.getHeaderColumnPrepend());
//			sb.append(md.getColumnLabel(i).toLowerCase());
//			sb.append(decorator.getHeaderColumnAppend());
//		}
//		sb.append(decorator.getRowAppend());
//
//		while (rs.next()) {
//			sb.append(decorator.getRowPrepend());
//			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
//				sb.append(decorator.getColumnPrepend());
//				sb.append(rs.getString(i));
//				sb.append(decorator.getColumnAppend());
//			}
//			sb.append(decorator.getRowAppend());
//			count++;
//		}
//		sb.append(decorator.getFooter());
//		sb.append(decorator.getLineSeparator()).append("(Q) Number of rows processed: ").append(count).append(
//				decorator.getLineSeparator());
//		return sb.toString();
//	}
	

}

