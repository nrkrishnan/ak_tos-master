package com.matson.tos.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.matson.cas.refdata.mapping.TosConsigneeTruckerMz;
import com.matson.cas.refdata.mapping.TosGumDcmMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumStPlanChasMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.tos.exception.NewVesselLogger;

/*
 * 
 * #		Date			Author						Description
 * ---		-----------		------------------			---------------------
 * 1		03/19/2013		Karthik Rajendran			Class file created from HON Newves NewVesselDao
 * 2		03/25/2013		Karthik Rajendran			Query Changed: getGumRdsDataFinalForVesvoy - DIR='IN' AND DPORT='GUM'
 * 3		03/27/2013		Karthik Rajendran			Added: getTruckers()
 * 4		04/03/2013		Karthik Rajendran			Removed: Date parameter
 * 5		04/04/2013		Karthik Rajendran			Added: insertNewConsigneeTruckers
 * 
 *
 */
public class NewVesselDaoGum extends  NewVesselDao{

	private static Logger logger = Logger.getLogger(NewVesselDaoGum.class);
	private static NewVesselLogger nvLogger = NewVesselLogger.getInstance();

	// *********** DCM ***********
	public static void deleteDCMData(String vesvoy)
	{
		logger.info("NewVesselDaoGum.deleteDCMData begin"+vesvoy);
		Session session = null;
		ArrayList<TosGumDcmMt> dcmList;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "FROM TosGumDcmMt WHERE vesvoy='"+vesvoy+"'";
			Query qry = session.createQuery(str);
			dcmList = (ArrayList<TosGumDcmMt>)qry.list();
			if(dcmList!=null)
			{
				if(dcmList.size()>0){
					logger.info("Deleting DCM records : "+dcmList.size());
					for(int i=0; i<dcmList.size(); i++)
					{
						TosGumDcmMt dcm = dcmList.get(i);
						session.delete(dcm);
					}
				}
			}

			commitTransaction(transaction);
			logger.info("NewVesselDaoGum.deleteDCMData End");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to delete DCM data for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
	}
	public static void insertDCMData(ArrayList<TosGumDcmMt> dcmDataList)
	{
		logger.info("NewvesselDaoGum.insertDCMData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<dcmDataList.size(); i++)
			{
				TosGumDcmMt data = dcmDataList.get(i);
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
			TosGumDcmMt data = dcmDataList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), " Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertDCMData end");
	}
	public static boolean isDCMAvailForVesselWithVVD(String vessel)
	{
		logger.info("NewvesselDaoGum.isDCMAvailForVesselWithVVD BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosGumDcmMt.class.getName() +"  WHERE vesvoy like ( \'"+ vessel+"%\' )";
			logger.info(qryStr);
			Query qry = session.createQuery(qryStr);
			if(qry.list().size()>=1)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("NewvesselDaoGum.isDCMAvailForVesselWithVVD Error: Unable to find DCM records for "+ vessel);
		}finally {
			closeHibernateSession(session);
		}
		return false;
	}
	public static ArrayList<TosGumDcmMt> getGumDcmDataForCntr(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDaoGum.getDcmDataForCntr begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumDcmMt> dcmData;
				String qryStr = "FROM "+ TosGumDcmMt.class.getName() +" WHERE CONTAINER_NUMBER=:ctrno AND vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
						.setParameter("ctrno", ctrno)
						.setParameter("vesvoy", vesvoy);

				dcmData = (ArrayList<TosGumDcmMt>) qry.list();

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
			logger.error("Error:NewvesselDaoGum getGumDcmDataForCntr(): " + ctrno + " is not a valid container number");
		}
		return null;
	}
	public static ArrayList<TosGumDcmMt> getGumDcmDataForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDaoGum.getGumDcmDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosGumDcmMt> dcmData;
				String qryStr = "FROM "+ TosGumDcmMt.class.getName() +" WHERE vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
						.setParameter("vesvoy", vesvoy);

				dcmData = (ArrayList<TosGumDcmMt>) qry.list();

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
	// *********** STOW PLAN ***********
	public static boolean isStowPlanAvailForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDaoGum.isStowPlanAvailForVessel BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosGumStPlanCntrMt.class.getName() +" WHERE vesvoy=\'"+ vesvoy +"\' ";
			logger.info(qryStr);
			Query qry = session.createQuery(qryStr);
			if(qry.list().size()>=1)
				return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("NewvesselDaoGum.isStowPlanAvailForVessel Error: Unable to find Stowplan records for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
		return false;
	}
	public static ArrayList<TosGumStPlanCntrMt> getGumOCRDataForVesvoy(String vesvoy, String leg)
	{
		logger.info("NewvesselDaoGum.getGumOCRDataForVesvoy begin"+vesvoy);
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosGumStPlanCntrMt> ocrData;
				String qryStr = "FROM TosGumStPlanCntrMt WHERE vesvoy=:vesvoy";
				if(leg!=null && leg.length()>0)
					qryStr = qryStr + " AND leg=:leg";
				Query qry = session.createQuery(qryStr).setParameter("vesvoy", vesvoy);


				if(leg!=null && leg.length()>0)
					qry.setParameter("leg", leg);
				ocrData = (ArrayList<TosGumStPlanCntrMt>) qry.list();

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
			logger.error("Error: getGumOCRDataForVesvoy: "+ vesvoy + " is not a valid vesvoy");
		}
		return null;
	}
	public static void deleteStowPlanData(String vesvoy)
	{
		logger.info("NewvesselDaoGum.deleteStowPlanData begin"+vesvoy);
		Session session = null;
		ArrayList<TosGumStPlanCntrMt> stowPlanList;
		ArrayList<TosGumStPlanChasMt> stowPlanBareChassis;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Transaction transaction = beginTransaction(session);
			String str = "FROM TosGumStPlanCntrMt WHERE vesvoy='"+vesvoy+"'";
			Query qry = session.createQuery(str);
			stowPlanList = (ArrayList<TosGumStPlanCntrMt>)qry.list();
			if(stowPlanList!=null)
			{
				if(stowPlanList.size()>0){
					logger.info("Deleting Stow Plan records : "+stowPlanList.size());
					for(int i=0; i<stowPlanList.size(); i++)
					{
						TosGumStPlanCntrMt stowData = stowPlanList.get(i);
						session.delete(stowData);
					}
				}
			}
			str = "FROM TosGumStPlanChasMt WHERE vesvoy='"+vesvoy+"'";
			Query qry1 = session.createQuery(str);
			stowPlanBareChassis = (ArrayList<TosGumStPlanChasMt>) qry1.list();
			if(stowPlanBareChassis!=null)
			{
				if(stowPlanBareChassis.size()>0)
				{
					logger.info("Deleting Stow Plan Chassis records : "+stowPlanBareChassis.size());
					for(int i=0; i<stowPlanBareChassis.size(); i++)
					{
						TosGumStPlanChasMt stowDataCh = stowPlanBareChassis.get(i);
						session.delete(stowDataCh);
					}
				}
			}
			commitTransaction(transaction);
			logger.info("NewvesselDaoGum.deleteStowPlanData End");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to delete Stow Plan data for "+ vesvoy);
		}finally {
			closeHibernateSession(session);
		}
	}
	public static void insertGumOCRData(ArrayList<TosGumStPlanCntrMt> stowPlanDataList)
	{
		logger.info("NewvesselDaoGum.insertOCRData begin");
		Session session = null;
		int i = 0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<stowPlanDataList.size(); i++)
			{
				TosGumStPlanCntrMt data = stowPlanDataList.get(i);
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
			TosGumStPlanCntrMt data = stowPlanDataList.get(i);
			nvLogger.addError(data.getVesvoy()+data.getLeg(), data.getContainerNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertOCRData end");
	}
	public static void insertGumOCHData(ArrayList<TosGumStPlanChasMt> stowBareChassisList)
	{
		logger.info("NewvesselDaoGum.insertOCHData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<stowBareChassisList.size(); i++)
			{
				TosGumStPlanChasMt data = stowBareChassisList.get(i);
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
			TosGumStPlanChasMt data = stowBareChassisList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getChassisNumber(), "Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertOCHData end");
	}
	public static ArrayList<TosGumStPlanCntrMt> getGumOCRDataForCtrno(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDaoGum.getGumOCRDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumStPlanCntrMt> ocrData;
				String qryStr = "FROM "+ TosGumStPlanCntrMt.class.getName() +" WHERE container_number=:ctrno and vesvoy=:vesvoy";
				Query qry = session.createQuery(qryStr)
						.setParameter("ctrno", ctrno)
						.setParameter("vesvoy", vesvoy);

				ocrData = (ArrayList<TosGumStPlanCntrMt>) qry.list();

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
			logger.error("Error: getGumOCRDataForCtrno: "+ctrno);
		}
		return null;
	}
	public static ArrayList<TosGumStPlanChasMt> getGumOCHDataForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDaoGum.getGumOCHDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosGumStPlanChasMt> ochData;
				String qryStr = "FROM "+ TosGumStPlanChasMt.class.getName() +" WHERE vesvoy=:vesvoy AND CONTAINER_NUMBER IS NOT null";
				Query qry = session.createQuery(qryStr)
						.setParameter("vesvoy", vesvoy);
				ochData = (ArrayList<TosGumStPlanChasMt>) qry.list();

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
			logger.error("Error: getGumOCHDataForVesvoy: "+ vesvoy + " is not a valid vesvoy");
		}
		return null;
	}

	public static ArrayList<TosGumStPlanChasMt> getGumBareOCHDataForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDao.getBareOCHDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumStPlanChasMt> ochData;
				String qryStr = "FROM "+ TosGumStPlanChasMt.class.getName() +" WHERE vesvoy=:vesvoy AND CONTAINER_NUMBER IS null ";
				Query qry = session.createQuery(qryStr)
						.setParameter("vesvoy", vesvoy);
				ochData = (ArrayList<TosGumStPlanChasMt>) qry.list();

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
	// *********** RDS ***********
	public static boolean isPrimaryVesvoy(String vesvoy)
	{
		logger.info("NewVesselDaoGum.isPrimaryVessel BEGIN");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			String qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +" WHERE vesvoy=\'"+ vesvoy+"\'";
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
	public static void insertRDSData(ArrayList<TosGumRdsDataMt> rdsDataList)
	{
		logger.info("NewvesselDaoGum.insertRDSData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataList.size(); i++)
			{
				TosGumRdsDataMt data = rdsDataList.get(i);
				//session.save(data);
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
			TosGumRdsDataMt data = rdsDataList.get(i);
			nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), " Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertRDSData end");
	}
	public static void updateRdsData(ArrayList<TosGumRdsDataMt> rdsDataList)
	{
		logger.info("NewvesselDaoGum.updateRdsData begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataList.size(); i++)
			{
				TosGumRdsDataMt data = rdsDataList.get(i);
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
			TosGumRdsDataMt data = rdsDataList.get(i);
			nvLogger.addError(data.getVes()+data.getVoy(), data.getCtrno(), " Unable to update: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.updateRdsData end");
	}
	public static void insertRdsDataFinal(ArrayList<TosGumRdsDataFinalMt> rdsDataFinalList)
	{
		logger.info("NewvesselDaoGum.insertRdsDataFinal begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataFinalList.size(); i++)
			{
				TosGumRdsDataFinalMt data = rdsDataFinalList.get(i);
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
			TosGumRdsDataFinalMt data = rdsDataFinalList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), " Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertRdsDataFinal end");
	}
	public static boolean addToRdsDataFinal(TosGumRdsDataFinalMt rdsDataFinal)
	{
		logger.info("NewvesselDaoGum.addToRdsDataFinal begin");
		Session session = null;
		boolean isAdded = false;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			logger.info("Insert :"+rdsDataFinal.getContainerNumber()+","+rdsDataFinal.getVesvoy()+","+rdsDataFinal.getLeg()+","+rdsDataFinal.getLoadPort()+","+rdsDataFinal.getDischargePort()
						+","+ rdsDataFinal.getHsf2()+","+rdsDataFinal.getHsf3()+","+rdsDataFinal.getHsf4()+","+rdsDataFinal.getHsf5()+" "+rdsDataFinal.getHsf6());
			String qry = "From TosGumRdsDataFinalMt WHERE containerNumber='"+rdsDataFinal.getContainerNumber()+"' AND vesvoy='"+rdsDataFinal.getVesvoy()+"' AND dischargePort='"+rdsDataFinal.getDischargePort()+"' AND loadPort='"+rdsDataFinal.getLoadPort()+"'";
			ArrayList<TosGumRdsDataFinalMt> tempList = (ArrayList<TosGumRdsDataFinalMt>)session.createQuery(qry).list();
			if(tempList!=null && tempList.size() > 0) {
				isAdded = false;
			} else {
				session.saveOrUpdate(rdsDataFinal);
				commitTransaction(transaction);
				isAdded = true;
			}
		}catch(Exception ex){
			logger.error("Error in NewvesselDaoGum.addToRdsDataFinal - ", ex);
			isAdded = false;
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.addToRdsDataFinal end");
		return isAdded;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getGumRdsDataFinalForVesvoy(String vesvoy)
	{
		logger.info("NewvesselDaoGum.getRdsDataFinalForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();
				ArrayList<TosGumRdsDataFinalMt> rdsData;
				String qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +" WHERE DIR='IN' AND DPORT='GUM' AND vesvoy=:vesvoy ORDER BY CONTAINER_NUMBER";
				Query qry = session.createQuery(qryStr)
						.setParameter("vesvoy", vesvoy);
				rdsData = (ArrayList<TosGumRdsDataFinalMt>) qry.list();
				logger.info("NewvesselDaoGum.getRdsDataFinalForVesvoy end");
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
			logger.error("Error:NewvesselDaoGum.getRdsDataFinalForVesvoy: "+vesvoy+ " ia not a valid vesvoy");
		}
		return null;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getGumRobRdsDataFinalForVesvoy(String vesvoy, Date tDate)
	{
		logger.info("NewvesselDaoGum.getGumRobRdsDataFinalForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumRdsDataFinalMt> rdsData;
				String qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +" WHERE vesvoy=:vesvoy AND TRIGGER_DATE=:tdate AND ROB_FLAG = 'Y'";
				Query qry = session.createQuery(qryStr)
						.setParameter("vesvoy", vesvoy)
						.setDate("tdate", tDate);

				rdsData = (ArrayList<TosGumRdsDataFinalMt>) qry.list();

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
	public static void updateRdsDataFinal(ArrayList<TosGumRdsDataFinalMt> rdsDataFinalList)
	{
		logger.info("NewvesselDaoGum.updateRdsDataFinal begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<rdsDataFinalList.size(); i++)
			{
				TosGumRdsDataFinalMt data = rdsDataFinalList.get(i);
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
			TosGumRdsDataFinalMt data = rdsDataFinalList.get(i);
			nvLogger.addError(data.getVesvoy(), data.getContainerNumber(), "Unable to update: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.updateRdsDataFinal end");
	}
	public static ArrayList<TosGumRdsDataFinalMt> getGumRdsDataFinalForContainers(List<String> ctrList)
	{
		logger.info("NewvesselDao.getRdsDataFinalForCtrno begin");
		Session session = null;
		if(ctrList.size()>0)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumRdsDataFinalMt> rdsDataFinal;
				/*String cnumbers = "";
				for(int i=0; i<ctrList.size(); i++)
				{
					cnumbers = cnumbers + "\'" + ctrList.get(i) + "\' ";
				}
				cnumbers = cnumbers.trim();
				cnumbers = cnumbers.replace(" ", ",");
				logger.info(cnumbers);*/
				String qryStr = "FROM TosGumRdsDataFinalMt rds WHERE rds.containerNumber IN ( :ctrlist )";
				Query qry = session.createQuery(qryStr)
						.setParameterList("ctrlist", ctrList);
				//List<TosRdsDataFinalMt> cats = session.createCriteria(TosRdsDataFinalMt.class)
				//	    .add( Restrictions.in("containerNumber", new String[] {"MATU458648", "MATU458654", "MATU511197"}))
				//	    .list();
				rdsDataFinal = (ArrayList<TosGumRdsDataFinalMt>) qry.list();

				return rdsDataFinal;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				nvLogger.addError("", "", e.toString());
			}finally {
				closeHibernateSession(session);
			}
		}
		return null;
	}
	public static ArrayList<TosGumRdsDataMt> getGumRdsDataForCtrno(String ctrno, String vesvoy)
	{
		logger.info("NewvesselDaoGum.getGumRdsDataForCtrno begin");
		Session session = null;
		if(ctrno.length()>=6)
		{
			try {
				createHibernateSessionFactory();
				session = sessionFact.openSession();

				ArrayList<TosGumRdsDataMt> rdsData;
				String qryStr = "FROM "+ TosGumRdsDataMt.class.getName() +" WHERE CTRNO=:ctrno AND VES=:ves AND VOY=:voy";
				Query qry = session.createQuery(qryStr)
						.setParameter("ctrno", ctrno)
						.setParameter("ves", vesvoy.substring(0, 3))
						.setParameter("voy", vesvoy.substring(3, 6));

				rdsData = (ArrayList<TosGumRdsDataMt>) qry.list();

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
	// *********** MISC ***********
	public static ArrayList<TosConsigneeTruckerMz> getTruckers()
	{
		ArrayList<TosConsigneeTruckerMz> truckers = null;
		logger.info("NewvesselDaoGum.getTruckers begin");
		Session session = null;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();
			Query qry = session.createQuery("FROM TosConsigneeTruckerMz");
			truckers = (ArrayList<TosConsigneeTruckerMz>) qry.list();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Error: Unable to get Truckers " + e);
		}finally {
			closeHibernateSession(session);
		}
		return truckers;
	}
	public static void insertNewConsigneeTruckers(ArrayList<TosConsigneeTruckerMz> cneeList)
	{
		logger.info("NewvesselDaoGum.insertNewConsigneeTruckers begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<cneeList.size(); i++)
			{
				TosConsigneeTruckerMz data = cneeList.get(i);
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
			TosConsigneeTruckerMz data = cneeList.get(i);
			nvLogger.addError("", data.getConsigneeName(), " Unable to persist: <br />"+ex.toString());
		}finally {
			closeHibernateSession(session);
		}
		logger.info("NewvesselDaoGum.insertNewConsigneeTruckers end");
	}
	

}
