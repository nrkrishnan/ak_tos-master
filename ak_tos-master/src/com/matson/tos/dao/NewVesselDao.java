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
 * 	A1     	09/26/2012		Raghu Pattangi		Initial creation
 *  A2     	10/01/2012   	Raghu Pattangi      Optimized the session code
 *  A3		10/08/2012		Karthik Rajendran	Added: getAllRdsData, getDcmDataForCntr methods
 *  A4		10/10/2012		Karthik Rajendran	Added: getAllRdsDataFinal, getAllRdsDataFinalForVesvoy
 *  A5      11/16/2012      Meena Kumari        Added: getRdsDataFinalForVesvoySupplemental, getRdsDataFinalForVesvoy
 *  A6		11/30/2012		Karthik Rajendran	Added: isPrimaryVessel, getRdsDataFinalForContainers
 *  A7		01/07/2013		Karthik Rajendran	Added: getEastBoundContainers
 */
import com.matson.tos.exception.NewVesselLogger;

public class NewVesselDao extends BaseDAO {

	private static Logger logger = Logger.getLogger(NewVesselDao.class);
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
	public static void insertRDSData(ArrayList<TosRdsDataMt> rdsDataList)
	{
		logger.info("NewvesselDao.insertRDSData begin");
		Session session = null;
		int i=0;
		String oldCtrno = "";
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataList.size(); i++)
			{
				TosRdsDataMt data = rdsDataList.get(i);
				String newCtrno = "";
				newCtrno = data.getCtrno();
				if(!newCtrno.equals(oldCtrno)) {
					String qry = "From TosRdsDataMt WHERE ctrno='"+newCtrno+"' AND leg='W' AND ves='"+data.getVes()+"' AND voy='"+data.getVoy()+"'";
					ArrayList<TosRdsDataMt> tempList = (ArrayList<TosRdsDataMt>) session.createQuery(qry).list();
					if(tempList!=null && tempList.size()>0)
					{
						for(int t=0; t<tempList.size(); t++)
						{
							TosRdsDataMt tempRds = tempList.get(t);
							session.delete(tempRds);
						}
					}
				}
				//logger.info("CTRNO:"+data.getCtrno()+", DataSource:"+data.getDatasource()+", Credit:"+data.getCreditStatus());
				session.merge(data);//changed by Meena
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosRdsDataMt data = rdsDataList.get(i);
			nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertRDSData end");
	}
	public static void insertEbRDSData(ArrayList<TosRdsDataMt> rdsDataList)
	{
		logger.info("NewvesselDao.insertRDSData begin");
		Session session = null;
		int i=0;
		String oldCtrno = "";
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataList.size(); i++)
			{
				TosRdsDataMt data = rdsDataList.get(i);
				String newCtrno = "";
				newCtrno = data.getCtrno();
				if(!newCtrno.equals(oldCtrno)) {
					String qry = "From TosRdsDataMt WHERE ctrno='"+newCtrno+"' AND leg='E'";
					ArrayList<TosRdsDataMt> tempList = (ArrayList<TosRdsDataMt>) session.createQuery(qry).list();
					if(tempList!=null && tempList.size()>0)
					{
						for(int t=0; t<tempList.size(); t++)
						{
							TosRdsDataMt tempRds = tempList.get(t);
							session.delete(tempRds);
						}
					}
				}
				oldCtrno = newCtrno;
				logger.info("CTRNO:"+data.getCtrno()+", DataSource:"+data.getDatasource()+", Credit:"+data.getCreditStatus());
				session.saveOrUpdate(data);//changed by Meena
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosRdsDataMt data = rdsDataList.get(i);
			nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertRDSData end");
	}
	public static void insertDCMData(ArrayList<TosDcmMt> dcmDataList)
	{
		logger.info("NewvesselDao.insertDCMData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<dcmDataList.size(); i++)
			{
				TosDcmMt data = dcmDataList.get(i);
                if (data == null) logger.info("Error with date " + data + " i " + i);
                if (data != null) {
					logger.info("Container " + data.getContainerNumber() + "getVesvoy " + data.getVesvoy() + " Seq " + data.getCnseq() + "dport " + data.getDport());
				}
                session.saveOrUpdate(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);

		}catch(Exception ex) {
			ex.printStackTrace();
			TosDcmMt data = dcmDataList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertDCMData end");
	}
	public static void insertOCRData(ArrayList<TosStowPlanCntrMt> stowPlanDataList)
	{
		logger.info("NewvesselDao.insertOCRData begin");
		Session session = null;
		int i = 0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<stowPlanDataList.size(); i++)
			{
				TosStowPlanCntrMt data = stowPlanDataList.get(i);
				session.saveOrUpdate(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);

		}catch(Exception ex){
			ex.printStackTrace();
			TosStowPlanCntrMt data = stowPlanDataList.get(i);
			nvLogger.addError(data.getVesvoy()+data.getLeg(), data.getContainerNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertOCRData end");
	}
	public static void insertOCHData(ArrayList<TosStowPlanChassisMt> stowBareChassisList)
	{
		logger.info("NewvesselDao.insertOCHData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<stowBareChassisList.size(); i++)
			{
				TosStowPlanChassisMt data = stowBareChassisList.get(i);
				session.saveOrUpdate(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosStowPlanChassisMt data = stowBareChassisList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getChassisNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertOCHData end");
	}
	public static void insertOHZData(ArrayList<TosStowPlanHazMt> stowHazDataList)
	{
		logger.info("NewvesselDao.insertOHZData begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(int i=0; i<stowHazDataList.size(); i++)
			{
				TosStowPlanHazMt data = stowHazDataList.get(i);
				session.saveOrUpdate(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertOHZData end");
	}
	public static void insertOHLData(TosStowPlanHoldMt ohl)
	{
		logger.info("NewvesselDao.insertOHLData begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			session.saveOrUpdate(ohl);
			commitTransaction(transaction);

		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertOHLData end");
	}
	public static void insertRdsDataFinal(ArrayList<TosRdsDataFinalMt> rdsDataFinalList)
	{
		logger.info("NewvesselDao.insertRdsDataFinal begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataFinalList.size(); i++)
			{
				TosRdsDataFinalMt data = rdsDataFinalList.get(i);
				// Delete existing container record for the same vvd assuming that new record is chosen from Eastbound logic.
				String qry = "From TosRdsDataFinalMt WHERE containerNumber='"+data.getContainerNumber()+"' AND vesvoy='"+data.getVesvoy()+"'";
				ArrayList<TosRdsDataFinalMt> tempList = (ArrayList<TosRdsDataFinalMt>) session.createQuery(qry).list();
				if(tempList!=null && tempList.size()>0)
				{
					logger.info("Deleting old record : "+data.getContainerNumber());
					for(int t=0; t<tempList.size(); t++)
					{
						TosRdsDataFinalMt tempRds = tempList.get(t);
						session.delete(tempRds);
					}
				}
				logger.info("Insert :"+data.getContainerNumber()+","+data.getVesvoy()+","+data.getLeg()+","+data.getLoadPort()+","+data.getDischargePort()
						+","+ data.getHsf2()+","+data.getHsf3()+","+data.getHsf4()+","+data.getHsf5()+" "+data.getHsf6());

				session.saveOrUpdate(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosRdsDataFinalMt data = rdsDataFinalList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertRdsDataFinal end");
	}
	public static void deleteRdsContainers(ArrayList<String> containersList, String leg)
	{
		logger.info("NewvesselDao.deleteRdsContainers begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();			
			Transaction transaction = beginTransaction(session);
			
			String qry = "DELETE TosRdsDataMt WHERE ctrno IN (:ctrsList) AND leg='"+ leg +"'";
			int count = session.createQuery(qry).setParameterList("ctrsList", containersList).executeUpdate();
			logger.info(count + "containers deleted.");
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error(ex);
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.deleteRdsContainers end");
	}
	public static void updateRdsData(ArrayList<TosRdsDataMt> rdsDataList)
	{
		logger.info("NewvesselDao.updateRdsData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataList.size(); i++)
			{
				TosRdsDataMt data = rdsDataList.get(i);
				session.merge(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosRdsDataMt data = rdsDataList.get(i);
			nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to update: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.updateRdsData end");
	}
	public static void updateRdsDataFinal(ArrayList<TosRdsDataFinalMt> rdsDataFinalList)
	{
		logger.info("NewvesselDao.updateRdsDataFinal begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataFinalList.size(); i++)
			{
				TosRdsDataFinalMt data = rdsDataFinalList.get(i);
				session.merge(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosRdsDataFinalMt data = rdsDataFinalList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), "Unable to update: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.updateRdsDataFinal end");
	}
	public static void updateSupRdsDataFinal(ArrayList<TosRdsDataFinalMt> rdsDataFinalList)
	{
		logger.info("NewvesselDao.updateSupRdsDataFinal begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(int i=0; i<rdsDataFinalList.size(); i++)
			{
				TosRdsDataFinalMt data = rdsDataFinalList.get(i);
				logger.info(data.getContainerNumber()+" " + data.getVesvoy() +" "+data.getDischargePort()+" "+data.getLoadPort());
				session.merge(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.updateSupRdsDataFinal end");
	}

	public static boolean isPrimaryVesvoy(String vesvoy)
	{
		logger.info("NewVesselDao.isPrimaryVessel BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosRdsDataFinalMt.class.getName() +" WHERE vesvoy=\'"+ vesvoy+"\'";
			logger.info(qryStr);
			Query qry = session.createQuery(qryStr);
			if(qry.list().size()>=1)
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find records for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
		return true;
	}
	public static boolean isStowPlanAvailForVesvoy(String vesvoy)
	{
		logger.info("NewVesselDao.isStowPlanAvailForVessel BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosStowPlanCntrMt.class.getName() +" WHERE vesvoy=\'"+ vesvoy +"\' ";
			logger.info(qryStr);
			Query qry = session.createQuery(qryStr);
			if(qry.list().size()>=1)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find Stowplan records for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
		return false;
	}

	public static boolean isStowPlanAvailForVesselWithVVD(String vessel)
	{
		logger.info("NewVesselDao.isStowPlanAvailForVesselWithVVD BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosStowPlanCntrMt.class.getName() +" WHERE ACTUAL_VESSEL =:ves and ACTUAL_VOYAGE = :voy ";
			logger.info(qryStr);
			//logger.info("ves:"+vessel.substring(0, 3) +" and voy:"+vessel.substring(3, 6) +"and leg:"+vessel.substring(6, 7));
			Query qry = session.createQuery(qryStr)
					.setParameter("ves", vessel.substring(0, 3))
					.setParameter("voy", vessel.substring(3, 6));
			if(qry.list().size()>=1)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find Stowplan records for "+ vessel);
		}finally {
			closeHibernateSession(session);
		}
		return false;
	}

	public static boolean isDCMAvailForVesselWithVVD(String vessel)
	{
		logger.info("NewVesselDao.isDCMAvailForVesselWithVVD BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosDcmMt.class.getName() +"  WHERE vesvoy like ( \'"+ vessel+"%\' )";
			logger.info(qryStr);
			Query qry = session.createQuery(qryStr);
			if(qry.list().size()>=1)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find Stowplan records for "+ vessel);
		}finally {
			closeHibernateSession(session);
		}
		return false;
	}
	public static ArrayList<TosRdsDataMt> getRdsDataForCtrno(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDao.getRdsDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataMt> rdsData;
				String qryStr = "FROM "+ TosRdsDataMt.class.getName() +" WHERE CTRNO=:ctrno AND VES=:ves AND VOY=:voy";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno)
							.setParameter("ves", vesvoy.substring(0, 3))
							.setParameter("voy", vesvoy.substring(3, 6));

				rdsData = (ArrayList<TosRdsDataMt>) qry.list();

				return rdsData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getRdsDataForCtrno: "+ctrno+ " ia not a valid container number");
		}
		return null;
	}
	public static ArrayList<TosRdsDataFinalMt> getRdsDataFinalForCtrno(String ctrno)
	{
		logger.info("NewvesselDao.getRdsDataFinalForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataFinalMt> rdsDataFinal;
				String qryStr = "FROM "+ TosRdsDataFinalMt.class.getName() +" WHERE container_number=:ctrno";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno);

				rdsDataFinal = (ArrayList<TosRdsDataFinalMt>) qry.list();

				return rdsDataFinal;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getRdsDataFinalForCtrno: "+ctrno+ " ia not a valid container number");
		}
		return null;
	}
	public static ArrayList<TosRdsDataFinalMt> getRdsDataFinalForContainers(List<String> ctrList)
	{
		logger.info("NewvesselDao.getRdsDataFinalForCtrno begin");
		Session session = null;
		if(ctrList.size()>0)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataFinalMt> rdsDataFinal;
				/*String cnumbers = "";
				for(int i=0; i<ctrList.size(); i++)
				{
					cnumbers = cnumbers + "\'" + ctrList.get(i) + "\' ";
				}
				cnumbers = cnumbers.trim();
				cnumbers = cnumbers.replace(" ", ",");
				logger.info(cnumbers);*/
				String qryStr = "FROM TosRdsDataFinalMt rds WHERE rds.containerNumber IN ( :ctrlist )";
				Query qry = session.createQuery(qryStr)
							.setParameterList("ctrlist", ctrList);
				//List<TosRdsDataFinalMt> cats = session.createCriteria(TosRdsDataFinalMt.class)
				//	    .add( Restrictions.in("containerNumber", new String[] {"MATU458648", "MATU458654", "MATU511197"}))
				//	    .list();
				rdsDataFinal = (ArrayList<TosRdsDataFinalMt>) qry.list();

				return rdsDataFinal;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError("", "", e.toString());
				//logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			//logger.error("Error: getRdsDataFinalForCtrno: "+ctrno+ " ia not a valid container number");
		}
		return null;
	}
	public static ArrayList<TosRdsDataFinalMt> getRdsDataFinalForVesvoy(String vesvoy, Date tDate)
	{
		logger.info("NewvesselDao.getRdsDataFinalForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataFinalMt> rdsData;
				String qryStr = "FROM "+ TosRdsDataFinalMt.class.getName() +" WHERE vesvoy=:vesvoy AND TRIGGER_DATE=:tdate AND ROB_FLAG IS null ORDER BY CONTAINER_NUMBER";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy)
							.setDate("tdate", tDate);

				rdsData = (ArrayList<TosRdsDataFinalMt>) qry.list();


				return rdsData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", e.toString());
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error:getRdsDataFinalForVesvoy: "+vesvoy+ " ia not a valid vesvoy");
		}
		return null;
	}

	public static ArrayList<TosRdsDataFinalMt> getRobRdsDataFinalForVesvoy(String vesvoy, Date tDate)
	{
		logger.info("NewvesselDao.getRobRdsDataFinalForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataFinalMt> rdsData;
				String qryStr = "FROM "+ TosRdsDataFinalMt.class.getName() +" WHERE vesvoy=:vesvoy AND TRIGGER_DATE=:tdate AND ROB_FLAG = 'Y'";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy)
							.setDate("tdate", tDate);

				rdsData = (ArrayList<TosRdsDataFinalMt>) qry.list();

				return rdsData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", e.toString());
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getRobRdsDataFinalForVesvoy: "+vesvoy+ " ia not a valid vesvoy");
		}
		return null;
	}
	public static ArrayList<TosDcmMt> getDcmDataForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDao.getDcmDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosDcmMt> dcmData;
				String qryStr = "FROM "+ TosDcmMt.class.getName() +" WHERE vesvoy=:vesvoy ORDER BY hazClass asc, containerNumber asc, cnseq asc";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);

				dcmData = (ArrayList<TosDcmMt>) qry.list();

				return dcmData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", e.toString());
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getDcmDataForVesvoy(): " + vesvoy + " is not a valid vesvoy");
		}
		return null;
	}
	public static ArrayList<TosStowPlanCntrMt> getOCRDataForVesvoy(String vesvoy, String leg, Date tDate)
	{
		logger.info("NewvesselDao.getOCRDataForVesvoy begin"+vesvoy);
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosStowPlanCntrMt> ocrData;
				String qryStr = "FROM TosStowPlanCntrMt WHERE vesvoy=:vesvoy";
				if(leg!=null && leg.length()>0)
					qryStr = qryStr + " AND leg=:leg";
				if(tDate!=null)
					qryStr = qryStr + " AND createDate=:tDate";
				Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy);


				if(leg!=null && leg.length()>0)
					qry.setParameter("leg", leg);
				if(tDate!=null)
					qry.setDate("tDate", tDate);
				ocrData = (ArrayList<TosStowPlanCntrMt>) qry.list();

				return ocrData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
				nvLogger.addError(vesvoy, "", e.toString());
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOCRDataForVesvoy: "+ vesvoy + " is not a valid vesvoy");
		}
		return null;
	}
	public static ArrayList<TosStowPlanChassisMt> getOCHDataForVesvoy(String vesvoy, Date tDate)
	{
		logger.info("NewvesselDao.getOCHDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosStowPlanChassisMt> ochData;
				String qryStr = "FROM "+ TosStowPlanChassisMt.class.getName() +" WHERE vesvoy=:vesvoy AND CONTAINER_NUMBER IS NOT null";
				if(tDate!=null)
					qryStr = qryStr + " AND createDate=:tDate";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);

				if(tDate!=null)
					qry.setDate("tDate", tDate);
				ochData = (ArrayList<TosStowPlanChassisMt>) qry.list();

				return ochData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOCHDataForVesvoy: "+ vesvoy + " is not a valid vesvoy");
		}
		return null;
	}

	public static ArrayList<TosStowPlanChassisMt> getBareOCHDataForVesvoy(String vesvoy, Date tDate)
	{
		logger.info("NewvesselDao.getBareOCHDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosStowPlanChassisMt> ochData;
				String qryStr = "FROM "+ TosStowPlanChassisMt.class.getName() +" WHERE vesvoy=:vesvoy AND CONTAINER_NUMBER IS null";
				if(tDate!=null)
					qryStr = qryStr + " AND createDate=:tDate";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);

				if(tDate!=null)
					qry.setDate("tDate", tDate);
				ochData = (ArrayList<TosStowPlanChassisMt>) qry.list();

				return ochData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getBareOCHDataForVesvoy: "+ vesvoy + " is not a valid vesvoy");
		}
		return null;
	}
	public static ArrayList<TosDcmMt> getDcmDataForCntr(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDao.getDcmDataForCntr begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosDcmMt> dcmData;
				String qryStr = "FROM "+ TosDcmMt.class.getName() +" WHERE CONTAINER_NUMBER=:ctrno AND vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno)
							.setParameter("vesvoy", vesvoy);

				dcmData = (ArrayList<TosDcmMt>) qry.list();

				return dcmData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error:getDcmDataForCntr(): " + ctrno + " is not a valid container number");
		}
		return null;
	}
	public static ArrayList<TosStowPlanCntrMt> getOCRDataForCtrno(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDao.getOCRDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosStowPlanCntrMt> ocrData;
				String qryStr = "FROM "+ TosStowPlanCntrMt.class.getName() +" WHERE container_number=:ctrno and vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno)
							.setParameter("vesvoy", vesvoy);

				ocrData = (ArrayList<TosStowPlanCntrMt>) qry.list();

				return ocrData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, ctrno, e.toString());
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOCRDataForCtrno: "+ctrno);
		}
		return null;
	}
	public static ArrayList<TosStowPlanChassisMt> getOCHDataForCtrno(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDao.getOCHDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosStowPlanChassisMt> ochData;
				String qryStr = "FROM "+ TosStowPlanChassisMt.class.getName() +" WHERE container_number=:ctrno and vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno)
							.setParameter("vesvoy", vesvoy);

				ochData = (ArrayList<TosStowPlanChassisMt>) qry.list();

				return ochData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOCHDataForCtrno: ");
		}
		return null;
	}
	public static ArrayList<TosStowPlanHazMt> getOHZDataForCtrno(String ctrno)
	{
		logger.info("NewvesselDao.getOHZDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosStowPlanHazMt> ohzData;
				String qryStr = "FROM "+ TosStowPlanHazMt.class.getName() +" WHERE container_number=:ctrno";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno);

				ohzData = (ArrayList<TosStowPlanHazMt>) qry.list();


				return ohzData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOHZDataForCtrno: ");
		}
		return null;
	}
	public static ArrayList<TosStowPlanHoldMt> getOHLDataForCtrno(String ctrno)
	{
		logger.info("NewvesselDao.getOHLDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosStowPlanHoldMt> ohlData;
				String qryStr = "FROM "+ TosStowPlanHoldMt.class.getName() +" WHERE container_number=:ctrno";
				Query qry = session.createQuery(qryStr)
							.setParameter("ctrno", ctrno);

				ohlData = (ArrayList<TosStowPlanHoldMt>) qry.list();

				return ohlData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ ctrno);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getOHLDataForCtrno: "+ctrno);
		}
		return null;

	}
	public static void updateMixPortContainers(String vesvoy)
	{
		logger.info("NewvesselDao.updateMixPortContainers begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String qryStr = "UPDATE "+ TosRdsDataFinalMt.class.getName()
								+ " SET DPORT='MIX'"
								+" WHERE DSC IN ('M', 'G') AND DPORT <> 'MIX' AND VESVOY=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " MIX Port containers DPORT to MIX on "+vesvoy);
				commitTransaction(transaction);

			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to update MIX Port containers records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updateMixPortContainers: "+vesvoy);
		}
	}
	public static void updatePartLotContainers(String vesvoy)
	{
		logger.info("NewvesselDao.updatePartLotContainers begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String qryStr = "UPDATE "+ TosRdsDataFinalMt.class.getName()
								+ " SET DPORT='MIX', DIR='IN', CONSIGNEE='PARTLOT MIX'"
								+" WHERE DSC ='P' AND DPORT <> 'MIX' AND VESVOY=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " Part Lot containers DPORT to MIX on "+vesvoy);
				commitTransaction(transaction);

			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to update Part Lot containers records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updatePartLotContainers: "+vesvoy);
		}
	}
	public static void updateFixMtys(String vesvoy)
	{
		logger.info("NewvesselDao.updateFixMtys begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String qryStr = "UPDATE "+ TosRdsDataFinalMt.class.getName()
								+ " SET CARGO_NOTES='', CONSIGNEE='', HSF2='', HSF6='', SRSTATUS='OK'"
								+" WHERE DIR='MTY' AND VESVOY=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " MTY containers on "+vesvoy);
				commitTransaction(transaction);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to update MTY containers records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updateFixMtys: "+vesvoy);
		}
	}
	public static void updateProcessAutos(String vesvoy)
	{
		logger.info("NewvesselDao.updateProcessAutos begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String qryStr = "UPDATE "+ TosRdsDataFinalMt.class.getName()
								+ " SET HSF2='V', HSF6=''"
								+" WHERE DS='AUT' AND VESVOY=:vesvoy";
				Query qry = session.createQuery(qryStr)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " AUT containers on "+vesvoy);
				commitTransaction(transaction);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to update AUT containers records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updateProcessAutos: "+vesvoy);
		}
	}
	public static void updateHazfByDcmCheck(String vesvoy)
	{
		logger.info("NewvesselDao.updateHazfByDcmCheck begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String str = "UPDATE TosRdsDataFinalMt SET hazf='Y' WHERE vesvoy=:vesvoy AND containerNumber IN (SELECT containerNumber FROM TosDcmMt WHERE vesvoy=:vesvoy)";
				Query qry = session.createQuery(str)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " containers HAZF flag on "+vesvoy);
				commitTransaction(transaction);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", e.toString());
				logger.error("Error: Unable to update containers HAZF flag records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updateHazfByDcmCheck: "+vesvoy);
		}
	}
	public static void updateALSByDcmCheck(String vesvoy, Date tdate)
	{
		logger.info("NewvesselDao.updateALSByDcmCheck begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				Transaction transaction = beginTransaction(session);
				String str = "UPDATE TosRdsDataFinalMt SET commodity='ALS ?' WHERE dir='IN' AND ds='CY' AND (bookingNumber is null or bookingNumber = 'null' ) AND vesvoy=:vesvoy AND triggerDate=:tdate AND containerNumber IN (SELECT containerNumber FROM TosDcmMt WHERE hazClass='9' AND vesvoy=:vesvoy AND createDate=:tdate)";
				Query qry = session.createQuery(str)
							.setDate("tdate", tdate)
							.setParameter("vesvoy", vesvoy);
				int rowCount = qry.executeUpdate();
				if(rowCount > 0)
					logger.debug("Updated " + rowCount+ " containers Commodity = ALS ? on "+vesvoy);
				commitTransaction(transaction);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError(vesvoy, "", e.toString());
				logger.error("Error: Unable to update containers Commodity = ALS ? on "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: updateALSByDcmCheck: "+vesvoy);
		}
	}
	public static void updateTosAppParameter(String key, String value)
	{
		logger.info("NewvesselDao.updateTosAppParameter begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			if(value==null || value.equals(""))
				value="false";
			String str = "UPDATE TosRefData SET value='"+value+"' WHERE key='"+key+"'";
			Query qry = session.createQuery(str);
			int rowCount = qry.executeUpdate();
			if(rowCount > 0)
				logger.debug("Key "+key+" updated with "+value);
			commitTransaction(transaction);
			logger.info("NewvesselDao.updateTosAppParameter End");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to update the key "+ key);
		}finally {
			closeHibernateSession(session);
		}
	}
	public static ArrayList<TosRdsDataMt> getEastBoundContainers()
	{
		logger.info("NewvesselDao.getEastBoundContainers begin");
		Session session = null;
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosRdsDataMt> rdsData;
				String qryStr = "FROM TosRdsDataMt WHERE leg='E' AND createDate >= SYSDATE - 60 order by ctrno, shipno";
				Query qry = session.createQuery(qryStr);

				rdsData = (ArrayList<TosRdsDataMt>) qry.list();

				return rdsData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find east bound container records");
			}finally {
				closeHibernateSession(session);
	         }
		return null;
	}

	public static HashMap getTosShedularParameters()
	{
		logger.info("NewvesselDao.getTosShedularParameters begin");
		Session session = null;
		HashMap paramMap = new HashMap();
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosRefData> rdsData;
				String qryStr = "FROM TosRefData WHERE key in ('IS_STOWPLAN_JOB_ON','IS_DCM_JOB_ON','IS_RDS_JOB_ON','IS_GUM_STPLAN_JOB_ON'," +
						"'IS_GUM_DCM_JOB_ON','IS_GUM_RDS_JOB_ON','RUN_STIF_PROC_IN','RUN_NEWVES_PROC_IN','RUN_DCM_CON_PROC_IN'" +
						",'RUN_FTP_CMIS_IN','RUN_ACETSMQ_PROC_IN','RUN_CMISGEMS_PROC_IN','RUN_GUMGATE_PROC_IN','RUN_STOW_PLAN_IN'" +
						",'RUN_DCM_IN','RUN_RDS_IN','RUN_GUMSTOW_IN','RUN_GUMDCM_IN','RUN_GUMRDS_IN','RUN_NASCENT_PROC', 'RUN_VGX_PROCESS','RUN_VGX_CLEANUP') order by key desc";
																
				Query qry = session.createQuery(qryStr);
				rdsData = (ArrayList<TosRefData>) qry.list();
				if (rdsData != null && rdsData.size() > 0) {
					for (int i=0;i<rdsData.size();i++) {
						TosRefData localRdsData = rdsData.get(i);
						paramMap.put(localRdsData.getKey(),localRdsData.getValue());
					}
				}

				return paramMap;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find east bound container records");
			}finally {
				closeHibernateSession(session);
	         }
			logger.info("NewvesselDao.getTosShedularParameters end");
			return null;
	}

	public static HashMap getTosCopyParameters()
	{
		logger.info("NewvesselDao.getTosCopyParameters begin");
		Session session = null;
		HashMap paramMap = new HashMap();
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosRefData> rdsData;
				String qryStr = "FROM TosRefData WHERE key in ('IS_PRIMARY','IS_BARGE','IS_SUPPLEMENTAL') order by key desc";
				Query qry = session.createQuery(qryStr);
				rdsData = (ArrayList<TosRefData>) qry.list();
				if (rdsData != null && rdsData.size() > 0) {
					for (int i=0;i<rdsData.size();i++) {
						TosRefData localRdsData = rdsData.get(i);
						paramMap.put(localRdsData.getKey(),localRdsData.getValue());
					}
				}

				return paramMap;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find east bound container records");
			}finally {
				closeHibernateSession(session);
	         }
			logger.info("NewvesselDao.getTosCopyParameters end");
			return null;
	}
	public static TosProcessLogger getProcessLoggerRecord(String vesvoy)
	{
		logger.info("NewvesselDao.getProcessLoggerRecord begin");
		Session session = null;
		TosProcessLogger loggerRecord = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			List<TosProcessLogger> loggerList;
			String qryStr = "FROM TosProcessLogger WHERE vesvoy='"+vesvoy+"' AND processType NOT IN('NV', 'STIF') order by endTime desc";
			Query qry = session.createQuery(qryStr).setMaxResults(1);
			loggerList = (ArrayList<TosProcessLogger>) qry.list();
			if(loggerList!=null && loggerList.size()==1)
			{
				loggerRecord = loggerList.get(0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find process logger records for : " + vesvoy);
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.getProcessLoggerRecord end");
		return loggerRecord;
	}
	public static TosProcessLogger getLoggerRecordForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDao.getProcessLoggerRecord begin");
		Session session = null;
		TosProcessLogger loggerRecord = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			List<TosProcessLogger> loggerList;
			String qryStr = "FROM TosProcessLogger WHERE vesvoy='"+vesvoy+"' AND processType <> 'STIF' order by endTime desc";
			Query qry = session.createQuery(qryStr).setMaxResults(1);
			loggerList = (ArrayList<TosProcessLogger>) qry.list();
			if(loggerList!=null && loggerList.size()==1)
			{
				loggerRecord = loggerList.get(0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find process logger records for : " + vesvoy);
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.getProcessLoggerRecord end");
		return loggerRecord;
	}
	public static void updateProcessLoggerRecord(TosProcessLogger loggerRecord)
	{
		logger.info("NewvesselDao.updateProcessLoggerRecord begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			loggerRecord.setEndTime(new Date());
			session.update(loggerRecord);
			commitTransaction(transaction);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to update process logger records for : " + loggerRecord.getVesvoy());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.updateProcessLoggerRecord end");
	}
	public static void deleteDCMData(String vesvoy)
	{
		logger.info("NewvesselDao.deleteDCMData begin"+vesvoy);
		Session session = null;
		ArrayList<TosDcmMt> dcmList;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "FROM TosDcmMt WHERE vesvoy='"+vesvoy+"'";
			Query qry = session.createQuery(str);
			dcmList = (ArrayList<TosDcmMt>)qry.list();
			if(dcmList!=null)
			{
				if(dcmList.size()>0){
					logger.info("Deleting DCM records : "+dcmList.size());
					for(int i=0; i<dcmList.size(); i++)
					{
						TosDcmMt dcm = dcmList.get(i);
						session.delete(dcm);
					}
				}
			}

			commitTransaction(transaction);
			logger.info("NewvesselDao.deleteDCMData End");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to delete DCM data for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
	}

	public static void deleteStowPlanData(String vesvoy)
	{
		logger.info("NewvesselDao.deleteStowPlanData begin"+vesvoy);
		Session session = null;
		ArrayList<TosStowPlanCntrMt> stowPlanList;
		ArrayList<TosStowPlanChassisMt> stowPlanBareChassis;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "FROM TosStowPlanCntrMt WHERE vesvoy='"+vesvoy+"'";
			Query qry = session.createQuery(str);
			stowPlanList = (ArrayList<TosStowPlanCntrMt>)qry.list();
			if(stowPlanList!=null)
			{
				if(stowPlanList.size()>0){
					logger.info("Deleting Stow Plan records : "+stowPlanList.size());
					for(int i=0; i<stowPlanList.size(); i++)
					{
						TosStowPlanCntrMt stowData = stowPlanList.get(i);
						session.delete(stowData);
					}
				}
			}
			str = "FROM TosStowPlanChassisMt WHERE vesvoy='"+vesvoy+"'";
			Query qry1 = session.createQuery(str);
			stowPlanBareChassis = (ArrayList<TosStowPlanChassisMt>) qry1.list();
			if(stowPlanBareChassis!=null)
			{
				if(stowPlanBareChassis.size()>0)
				{
					logger.info("Deleting Stow Plan Chassis records : "+stowPlanBareChassis.size());
					for(int i=0; i<stowPlanBareChassis.size(); i++)
					{
						TosStowPlanChassisMt stowDataCh = stowPlanBareChassis.get(i);
						session.delete(stowDataCh);
					}
				}
			}
			commitTransaction(transaction);
			logger.info("NewvesselDao.deleteStowPlanData End");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to delete Stow Plan data for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
	}
	
	/**
	 * This method is used to get the Consignee Information
	 * @param consigneeName
	 * @return
	 */
	public static ArrayList<TosConsgineeTrucker> getConsigneeInformation(String consigneeName)
	{
		logger.info("NewvesselDao.getConsigneeInformation begin");
		Session session = null;
		if(consigneeName!=null)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosConsgineeTrucker> consigneeInfoList = null;
				String qryStr = "FROM "+ TosConsgineeTrucker.class.getName() +"  WHERE consigneeName LIKE UPPER ( \'%"+ consigneeName+"%\' ) ORDER BY LAST_UPDATE_DATE DESC";
				logger.info("qryStr------>>>>"+qryStr);
				Query qry = session.createQuery(qryStr);
				consigneeInfoList = (ArrayList<TosConsgineeTrucker>) qry.list();
				//logger.info("consigneeInfoList:::::"+consigneeInfoList);
				logger.info("consigneeInfoList::Size:::"+consigneeInfoList.size());
				return consigneeInfoList;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ consigneeName);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getConsigneeInformation: "+consigneeName);
		}
		return null;

	}
	
	public static String updateConsigneeInformation(ArrayList<TosConsgineeTrucker> updateList)
	{
		logger.info("NewvesselDao.updateConsigneeInformation begin");
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
				TosConsgineeTrucker data = updateList.get(i);
				session.update(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
			isUpdate = "success";
			logger.info("NewvesselDao.updateConsigneeInformation isupdate"+isUpdate);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			TosConsgineeTrucker data = updateList.get(i);
			isUpdate = "failure";
			logger.info("NewvesselDao.updateConsigneeInformation isupdate"+isUpdate);
			//nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to update: <br />"+ex.toString());
		}
		
		finally 
		{
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.updateConsigneeInformation end");
		return isUpdate;
	}
	
	
	public static ArrayList<TosGumRdsDataFinalMt> getVVDInformation(String vesvoy,String containerNumber,String bookingNumber,String buttonAction,String consigneeName)
	{
		logger.info("NewvesselDao.getVVDInformation begin");
		Session session = null;
		if(vesvoy!=null && !vesvoy.equals(""))
		{
			try {
				vesvoy = vesvoy.toUpperCase();
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				String qryStr = null;
				ArrayList<TosGumRdsDataFinalMt> gumRdsDataFinalList = null;
				
				/*if(vesvoy!=null && bookingNumber!=null && !bookingNumber.equals("") && containerNumber!=null && !containerNumber.equalsIgnoreCase(""))
				{
					logger.info("When containerNumber, VesyVoy and bookingNumber is not empty");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy and containerNumber=:containerNumber and bookingNumber=:bookingNumber order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy).setParameter("containerNumber", containerNumber).setParameter("bookingNumber", bookingNumber);
					logger.info("qryStr------>>>>"+qryStr);
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}
				else if(vesvoy!=null && bookingNumber!=null && !bookingNumber.equals("") && (containerNumber==null || containerNumber.equals("")))
				{
					logger.info("When containerNumber is empty and VesyVoy and bookingNumber is not empty");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy and bookingNumber=:bookingNumber order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy).setParameter("bookingNumber", bookingNumber);
					logger.info("qryStr------>>>>"+qryStr);
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}
				else if(vesvoy!=null && (bookingNumber==null || bookingNumber.equals("")) && containerNumber!=null  && !containerNumber.equals(""))
				{
					logger.info("When booking is empty and VesyVoy and Container Number is not empty");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy and containerNumber=:containerNumber order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy).setParameter("containerNumber", containerNumber);
					logger.info("qryStr------>>>>"+qryStr);
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}
				else if(vesvoy!=null  && buttonAction!=null && buttonAction.equals("Search") && (bookingNumber==null || bookingNumber.equals("")) && (containerNumber==null || containerNumber.equals("")))
				{
					logger.info("When booking and Container Number is empty and VesyVoy is not empty");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy and dischargePort='GUM' order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy);
					logger.info("qryStr------>>>>"+qryStr);
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}
				else if(vesvoy!=null && buttonAction!=null && buttonAction.equals("DownloadVessel"))
				{
					logger.info("When VesyVoy is not empty and buttonAction is DownloadVessel");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy);
					logger.info("qryStr------>>>>"+qryStr);
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}*/
				// Modified - Nov 6, 2013 By KRAJENDRAN
				// Below is the optimized code of the above commented code
				if(vesvoy!=null && buttonAction!=null && buttonAction.equals("DownloadVessel"))
				{
					logger.info("Action :---> DownloadVessel");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy order by truck desc nulls first";
					Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy);
					logger.info("qryStr------>>>>"+qry.getQueryString());
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());
				}
				else if(vesvoy!=null  && buttonAction!=null && buttonAction.equals("Search"))
				{
					logger.info("Action :---> Search");
					qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy=:vesvoy";
					if ( (bookingNumber==null || "".equalsIgnoreCase(bookingNumber))
							&& (containerNumber==null || "".equalsIgnoreCase(containerNumber))
								&& (consigneeName==null || "".equalsIgnoreCase(consigneeName))) {
						qryStr = qryStr + " AND dischargePort='GUM' ORDER BY truck DESC NULLS FIRST";
					} else {
						if(containerNumber!=null && containerNumber.length()>0) {
							qryStr = qryStr + " AND containerNumber=:containerNumber";
						}
						if(bookingNumber!=null && bookingNumber.length()>0) {
							qryStr = qryStr + " AND bookingNumber=:bookingNumber";
						}
						if(consigneeName!=null && consigneeName.length()>0) {
							qryStr = qryStr + " AND consignee LIKE ( '%"+consigneeName+"%' )";
						}
						qryStr = qryStr + " ORDER BY truck DESC NULLS FIRST";
					}
					Query qry = session.createQuery(qryStr);
					qry.setParameter("vesvoy", vesvoy);
					if(containerNumber!=null && containerNumber.length()>0)
						qry.setParameter("containerNumber", containerNumber);
					if(bookingNumber!=null && bookingNumber.length()>0)
						qry.setParameter("bookingNumber", bookingNumber);
					logger.info("qryStr------>>>>"+qry.getQueryString());
					gumRdsDataFinalList = (ArrayList<TosGumRdsDataFinalMt>)qry.list();
					logger.info("gumRdsDataFinalList------>>>>"+gumRdsDataFinalList.size());					
				}
				else
				{
					
				}
				return gumRdsDataFinalList;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				closeHibernateSession(session);
	         }
		}
		else
		{
			logger.error("Error: getVVDInformation: "+vesvoy);
		}
		return null;

	}
	
	public static String updateVVDInformation(ArrayList<TosGumRdsDataFinalMt> updateList)
	{
		logger.info("NewvesselDao.updateVVDInformation begin");
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
				TosGumRdsDataFinalMt data = updateList.get(i);
				session.update(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
			}
			commitTransaction(transaction);
			isUpdate = "success";
			logger.info("NewvesselDao.updateVVDInformation isupdate"+isUpdate);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			TosGumRdsDataFinalMt data = updateList.get(i);
			isUpdate = "failure";
			logger.info("NewvesselDao.updateVVDInformation isupdate"+isUpdate);
			//nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), "Unable to update: <br />"+ex.toString());
		}
		
		finally 
		{
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.updateVVDInformation end");
		return isUpdate;
	}
	public static void updateUserPrefVvd(String vvd)
	{
		logger.info("NewvesselDao.updateUserPrefVvd begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "UPDATE TosRefData SET value=:vvd WHERE key='USER_PREF_VVD'";
			Query qry = session.createQuery(str).setParameter("vvd", vvd);
			int rowCount = qry.executeUpdate();
			if(rowCount > 0)
				logger.debug("Updated USER_PREF_VVD value with "+vvd);
			commitTransaction(transaction);
		}
		catch(Exception e)
		{
			logger.error("Error: Unable to update UserPrefVvd -"+ vvd + "\n" + e);
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.updateUserPrefVvd end");
	}
	public static String getUserPrefVvdFromAppParam()
	{
		logger.info("NewvesselDao.getUserPrefVvdFromAppParam begin");
		Session session = null;
		String userPrefVvd = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			ArrayList<TosRefData> refData;
			String qryStr = "FROM TosRefData WHERE key='USER_PREF_VVD'";
			Query qry = session.createQuery(qryStr);
			refData = (ArrayList<TosRefData>) qry.list();
			if (refData != null && refData.size() > 0) {
				for (int i=0;i<refData.size();i++) {
					userPrefVvd = refData.get(i).getValue();
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error: Unable to find USER_PREF_VVD records");
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.getUserPrefVvdFromAppParam end");
		return userPrefVvd;
	}
	public static void updateRdsJobOnFlag(String value)
	{
		logger.info("NewvesselDao.updateRdsJobOnFlag begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "UPDATE TosRefData SET value=:flag WHERE key='IS_RDS_JOB_ON'";
			Query qry = session.createQuery(str).setParameter("flag", value);
			int rowCount = qry.executeUpdate();
			if(rowCount > 0)
				logger.debug("Updated IS_RDS_JOB_ON value with "+value);
			commitTransaction(transaction);
		}
		catch(Exception e)
		{
			logger.error("Error: Unable to update IS_RDS_JOB_ON -"+ value + "\n" + e);
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.updateRdsJobOnFlag end");
	}
	public static void scheduleNewves(String vvd) {
		logger.info("*********************> scheduleNewves start");
		NewVesselDao.updateUserPrefVvd(vvd);
		NewVesselDao.updateRdsJobOnFlag("Y");
		logger.info("*********************< scheduleNewves end");
	}
	public static void clearScheduleNewves() {
		logger.info("*********************> clearScheduleNewves start");
		NewVesselDao.updateUserPrefVvd(null);
		NewVesselDao.updateRdsJobOnFlag("N");
		logger.info("*********************< clearScheduleNewves end");
	}
	public static boolean verifyCHProcessExecution(String vvd) {
		logger.info("NewvesselDao.verifyCHProcessExecution begin");
		Session session = null;
		boolean loggerRecord = false;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			List<TosRdsChLoggerMt> loggerList;
			String qryStr = "FROM TosRdsChLoggerMt WHERE vesvoy='"+vvd+"' AND status='Processed'";
			Query qry = session.createQuery(qryStr).setMaxResults(1);
			loggerList = (ArrayList<TosRdsChLoggerMt>) qry.list();
			if(loggerList!=null && loggerList.size()>0)
			{
				loggerRecord = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to find cash hold logger records for : " + vvd);
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDao.verifyCHProcessExecution end");
		return loggerRecord;
	}
	public static void insertCHProcessExecution(TosRdsChLoggerMt loggerRecord)
	{
		logger.info("NewvesselDao.insertCHProcessExecution begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			session.save(loggerRecord);
			commitTransaction(transaction);
		}catch(Exception ex){
			logger.error("Error while inserting cash hold process execution :- ", ex);
		}finally {
			closeHibernateSession(session);
        }
		logger.info("NewvesselDao.insertCHProcessExecution end");
	}
}
