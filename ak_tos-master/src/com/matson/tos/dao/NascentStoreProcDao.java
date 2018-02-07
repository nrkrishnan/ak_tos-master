package com.matson.tos.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;

public class NascentStoreProcDao {
	
	private static Logger logger = Logger.getLogger(NascentStoreProcDao.class);
	public static SessionFactory sessionFact;
	public static String  CONIG_FILE = "alkrefdata.cfg.xml";
	public static final String FACILITY_ID= "3";
			//+ "/alkrefdata.cfg.xml";
	         // /tosrefdata.cfg.xml;

	/**
	 * @param args
	 */

	public synchronized static void createHibernateSessionFactory()
	{
		try
		{
			if (sessionFact == null) {
	            try {
	            	logger.debug("session factory creation begins");
	                Configuration cfg = new Configuration();
	                cfg.configure(CONIG_FILE);
	                sessionFact = cfg.buildSessionFactory();
	                logger.debug("Sessionfactory is:"+ sessionFact);
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
	
	public static boolean callStoreProc(int lane)
    {
        logger.info("NascentStoreProcDao.callStoreProc begin "+lane);
        
        //String result = "";
        Session session= null;

            try {
                createHibernateSessionFactory();
                logger.info("SESSION FACTORY INTI");

                session = sessionFact.openSession();
                logger.info("OPENED SESSION");
                
                Transaction transaction = beginTransaction(session);
                logger.info("Begin Transaction");
                
                String qryStr = "FROM DUAL;";

		        //SQLQuery qry = session.createSQLQuery(qryStr);
                Query qry = session.createSQLQuery("{CALL UP_EDIT_LANE_STATUS_CALL(:laneId, :facilityId)}")
                        .setParameter("laneId", lane)
                        .setParameter("facilityId", 3);
		       
		        int result=qry.executeUpdate();
		        
		        logger.info("result"+result);

                commitTransaction(transaction);
                logger.info("Commit Transaction");
                return true;
                
            }
            
            catch(Exception e)
            {
                e.printStackTrace();
                
                logger.error("Error: Unable to find records for ");
                return false;
            }finally {
                closeHibernateSession(session);
             }
        //return list;
    }

}
