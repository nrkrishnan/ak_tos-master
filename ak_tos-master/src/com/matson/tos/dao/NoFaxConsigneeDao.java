package com.matson.tos.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;




import com.matson.cas.refdata.mapping.*;
/*
 * 	Srno   	Date			AuthorName			Change Description
 * 	A1     	10/14/2014		Raghu Iyer			Initial creation
 */
import com.matson.tos.exception.NewVesselLogger;

public class NoFaxConsigneeDao extends BaseDAO {

	private static Logger logger = Logger.getLogger(NoFaxConsigneeDao.class);
	public static SessionFactory sessionFact;
//	public static Session session = null;
//	public static Transaction transaction;
	public static String  CONIG_FILE = "/tosrefdata.cfg.xml";
	public static NewVesselLogger nvLogger = NewVesselLogger.getInstance();
//    private static HashMap sessionFactoryMap;
//	private static HashMap sessionMap;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

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
		
	public static ArrayList<TosNoFaxConsigneeMt> getConsigneeInformation(String phone,String buttonAction,String consigneeName)
	{
		logger.info("NoFaxConsigneeDao.getConsigneeInformation begin");
		Session session = null;
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				String qryStr = null;
				ArrayList<TosNoFaxConsigneeMt> noFaxConList = null;

				if(buttonAction.equals("Search") || buttonAction.equals("Save & Email Report") )					
				{
					logger.info("Action :---> "+buttonAction);
					qryStr = "FROM "+ TosNoFaxConsigneeMt.class.getName() +" WHERE 1=1";

						if(phone!=null && phone.length()>0) {
							qryStr = qryStr + " AND phone LIKE ( '%"+phone+"%' )";
						}
						if(consigneeName!=null && consigneeName.length()>0) {
							qryStr = qryStr + " AND upper(consigneeName) LIKE ( '%"+consigneeName+"%' )";
						}
						qryStr = qryStr + " ORDER BY consigneeName";

					Query qry = session.createQuery(qryStr);

					logger.info("qryStr------>>>>"+qry.getQueryString());
					noFaxConList = (ArrayList<TosNoFaxConsigneeMt>)qry.list();
					logger.info("noFaxConList------>>>>"+noFaxConList.size());					
				}
				else
				{
					
				}
				return noFaxConList;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ consigneeName);
			}finally {
				closeHibernateSession(session);
	         }

		return null;

	}	
	
	public static String updateConsigneeInfo(ArrayList<TosNoFaxConsigneeMt> updateList)
	{
		logger.info("NoFaxConsigneeDao.updateConsigneeInfo begin");
		Session session = null;
		int i=0;
		String isUpdate = "";
		try 
		{
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<updateList.size(); i++)
			{
				TosNoFaxConsigneeMt data = updateList.get(i);
				logger.info(data.getConsigneeId() +":"+ data.getConsigneeName()+":"+data.getPhone()+":"+data.getType());
				session.update(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
			isUpdate = "Updated";
			logger.info("NoFaxConsigneeDao.updateConsigneeInfo isupdate"+isUpdate);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			TosNoFaxConsigneeMt data = updateList.get(i);
			isUpdate = "failure";
			logger.info("NoFaxConsigneeDao.updateConsigneeInfo isupdate"+isUpdate);
			//nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to update: <br />"+ex.toString());
		}
		
		finally 
		{
			closeHibernateSession(session);
        }
		logger.info("NoFaxConsigneeDao.updateConsigneeInfo end");
		return isUpdate;
	}	
	
	public static boolean addToNoFaxConsignee(TosNoFaxConsigneeMt noFaxConsignee)
	{
		logger.info("NoFaxConsigneeDao.addToNoFaxConsignee begin");
		Session session = null;
		boolean isAdded = false;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			logger.info("Insert :"+noFaxConsignee.getConsigneeName()+","+noFaxConsignee.getPhone()+","+noFaxConsignee.getType());
			String qry = "From TosNoFaxConsigneeMt WHERE consigneeName='"+noFaxConsignee.getConsigneeName()+"'";
			ArrayList<TosNoFaxConsigneeMt> tempList = (ArrayList<TosNoFaxConsigneeMt>)session.createQuery(qry).list();
			if(tempList!=null && tempList.size() > 0) {
				isAdded = false;
			} else {
				session.saveOrUpdate(noFaxConsignee);
				commitTransaction(transaction);
				isAdded = true;
			}
		}catch(Exception ex){
			logger.error("Error in NoFaxConsigneeDao.addToNoFaxConsignee - ", ex);
			isAdded = false;
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NoFaxConsigneeDao.addToNoFaxConsignee end");
		return isAdded;
	}	
	
	public static String deleteNoFaxConsignee(int conId)
	{
		logger.info("NoFaxConsigneeDao.deleteNoFaxConsignee begin");
		Session session = null;
		String isDeleted= "";
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String qry = "From TosNoFaxConsigneeMt WHERE consigneeId="+conId;
			logger.info("Delete Query " + qry);
			ArrayList<TosNoFaxConsigneeMt> tempList = (ArrayList<TosNoFaxConsigneeMt>) session.createQuery(qry).list();
			if(tempList!=null && tempList.size()>0)
			{
				logger.info("Delete Query results " + tempList.size());		
				for(int t=0; t<tempList.size(); t++)
				{
					logger.info("Delete Query inside delete " + t);
					TosNoFaxConsigneeMt noFaxCon = tempList.get(t);
					session.delete(noFaxCon);
					session.flush();
					session.clear();
					isDeleted= "Deleted";
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.info("Unable to delete no Fax Consignee Id :: " + conId);
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NoFaxConsigneeDao.deleteNoFaxConsignee end");
		return isDeleted;
	}	
}
