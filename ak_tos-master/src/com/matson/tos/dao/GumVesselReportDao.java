package com.matson.tos.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.HibernateSessionFactory;
import com.matson.cas.refdata.mapping.TosDcmMt;
import com.matson.cas.refdata.mapping.TosGumDcmMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosGumRdsDataMt;
import com.matson.cas.refdata.mapping.TosGumStPlanCntrMt;
import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;

/*
 * Srno		Date				AuthorName					Change Description
 *  1		03/29/2013			Karthik Rajendran			Class created
 *  2		04/02/2013			Karthik Rajendran			Added: getListForTAGManifest,getImpRdsListForTAGManifest
 *  3		04/03/2013			Karthik Rajendran			Removed: Date parameters
 															Added: getReeferContainers
 															Changed: getListForTAGManifest Query: dport='GUM' to dischargePort='GUM'
 *	4		04/04/2013			Karthik Rajendran			Query Changed: getReeferContainers, getGuamInboundByDportDs
 *	5		04/08/2013			Karthik Rajendran			Query Changed: getListForTAGManifest()-order by containerNumber
 *															Removed: getImpRdsListForTAGManifest()
 *	6		04/12/2013			Karthik Rajendran			Added:getGumCmisFactsDifferences()
 *	7		04/15/2013			Karthik Rajendran			Added: getGumFactsCmisDifferences(),getGumFactsCmisDifferences(), getGumDischargeMTYContainers(),
 *																	getUnApprovedVariances(), getBlankCyFactsRecords()
 *	8		04/16/2013			Karthik Rajendran			Added: getDcmDataForUSCG()
 *	9		04/19/2013			Karthik Rajendran			Query changed: getGumCmisFactsDifferences()-with actualVessel,actualVoyage
 *
 * 10		05/01/2013			Raghu Iyer					Added: getRiderReportData
 */

public class GumVesselReportDao {
	private static Logger logger = Logger.getLogger(GumVesselReportDao.class);
	private static final String configFileLocation = "/tosrefdata.cfg.xml";
	// for junit testing comment the above line and uncomment the below line
	//private static final String configFileLocation = "/hibernate.cfg.xml";


	protected static Session getSession(){
		Session session = HibernateSessionFactory.currentSessionFactoryByCfg(configFileLocation).openSession();
		return session;
	}
	// *************************************

	public static List getGuamInboundByDportDs(String vesvoy){
		logger.info("getGuamInboundByDportDs::::BEGIN");
		Session session = null;
		List tcList = null;
		List tempList = null;
		try
		{
			session = getSession();
			String qry = "";
			// Get GUM In loads
			qry = "SELECT DPORT, NVL(DS,'MTY'), COUNT(*) AS DSCOUNT FROM TOSCSTMMGR.TOS_GUM_RDSDATA_FINAL_MT WHERE VESVOY='" + vesvoy + "' AND DPORT='GUM' AND DIR='IN' GROUP BY DPORT, NVL(DS,'MTY') ORDER BY DPORT";
			tcList = session.createSQLQuery(qry).list();
			// Get other destination loads
			qry = "SELECT DPORT, NVL(DS,'MTY'), COUNT(*) AS DSCOUNT FROM TOSCSTMMGR.TOS_GUM_RDSDATA_FINAL_MT WHERE VESVOY='" + vesvoy + "' AND DPORT<>'GUM' GROUP BY DPORT, NVL(DS,'MTY') ORDER BY DPORT";
			tempList = session.createSQLQuery(qry).list();
			if(tcList!=null)
			{
				if(tempList!=null)
				{
					tcList.addAll(tempList);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return tcList;
	}
	public static List getListForTAGManifest(String vesvoy) {
		logger.info("getListForTAGManifest begin");
		Session session = null;
		List list = null;
		try
		{
			session = getSession();
			//String qry = "FROM TosGumRdsDataMt WHERE ves='" + vesvoy.substring(0, 3) + "' AND voy='" + vesvoy.substring(3, 6) +"' ORDER BY ctrno asc";
			String qry = "SELECT F.CNEE_CODE, F.CONSIGNEE, F.CONTAINER_NUMBER, F.CHECK_DIGIT, F.DPORT, F.VESVOY, F.CARGO_NOTES, F.BOOKING_NUMBER, F.TYPE_CODE, F.OWNER," +
					" R.CONSIGNEE_ADDR, R.CONSIGNEE_CITY, R.CONSIGNEE_STATE, CONSIGNEE_ZIP_CODE FROM TOSCSTMMGR.TOS_GUM_RDSDATA_FINAL_MT F, TOSCSTMMGR.TOS_GUM_RDS_DATA_MT R " +
					" WHERE F.VESVOY='" + vesvoy + "' AND R.VES='" + vesvoy.substring(0, 3) + "' AND R.VOY='" + vesvoy.substring(3, 6) +"' AND F.CONTAINER_NUMBER = R.CTRNO AND " +
					" F.DISCHARGE_PORT='GUM' AND F.DS<>'AUT' AND F.DIR<>'MTY' "+
					" ORDER BY F.CONTAINER_NUMBER";
			logger.info("Query --> " +qry);
			list = session.createSQLQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getListForTAGManifest end");
		return list;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getReeferContainers(String vesvoy) {
		logger.info("getListForTAGManifest begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataFinalMt WHERE vesvoy='" + vesvoy + "' AND dport IN ('GUM','SPN','RTA','TIN','KWJ','MAJ','EBY','KMI','PNP','UUK','YAP','PUX') AND typeCode LIKE 'R%' ORDER BY dport ASC, consignee ASC";
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getListForTAGManifest end");
		return list;
	}
	public static ArrayList<TosGumStPlanCntrMt> getGumCmisFactsDifferences(String vesvoy)
	{
		logger.info("getGumCmisFactsDifferences begin");
		Session session = null;
		ArrayList<TosGumStPlanCntrMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumStPlanCntrMt WHERE actualVessel||actualVoyage='" + vesvoy + "' AND containerNumber not in (SELECT ctrno FROM TosGumRdsDataMt WHERE ves='" + vesvoy.substring(0, 3) + "' AND voy='" + vesvoy.substring(3, 6) + "') ";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC";
			list = (ArrayList<TosGumStPlanCntrMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getGumCmisFactsDifferences end");
		return list;
	}
	public static ArrayList<TosGumRdsDataMt> getGumFactsCmisDifferences(String vesvoy)
	{
		logger.info("getGumFactsCmisDifferences begin");
		Session session = null;
		ArrayList<TosGumRdsDataMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataMt WHERE ves='" + vesvoy.substring(0, 3) + "' AND voy='" + vesvoy.substring(3, 6) + "' AND ctrno not in (SELECT containerNumber FROM TosGumStPlanCntrMt WHERE vesvoy='" + vesvoy + "' ) ";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(ctrno, 1, 4)='MATU' THEN SUBSTR(ctrno, 5, LENGTH(ctrno)) ELSE ctrno END ASC";
			list = (ArrayList<TosGumRdsDataMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getGumFactsCmisDifferences end");
		return list;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getGumDischargeMTYContainers(String vesvoy) {
		logger.info("getMTYGumDischargeContainers begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataFinalMt WHERE vesvoy='" + vesvoy + "' AND dport IN ('GUM','SPN','TIN','PUX','YAP','RTA','UUK','PNP') AND dir='MTY' AND typeCode NOT LIKE '%GB%' AND typeCode NOT LIKE '%GR%' ";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC";
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getMTYGumDischargeContainers end");
		return list;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getUnApprovedVariances(String vesvoy) {
		logger.info("getMTYGumDischargeContainers begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataFinalMt WHERE vesvoy='" + vesvoy + "' AND (consignee like '%UNAPPROVED%' or cargoNotes like '%INVALID%ASSIGN%'or cargoNotes like '%UNAPPROVED%')AND typeCode NOT LIKE '%GB%' AND typeCode NOT LIKE '%GR%' ";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC";
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getMTYGumDischargeContainers end");
		return list;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getBlankCyFactsRecords(String vesvoy) {
		logger.info("getBlankCyFactsRecords begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataFinalMt WHERE vesvoy='" + vesvoy + "' AND ds='CY' AND dischargePort='GUM' AND typeCode NOT LIKE '%GB%' AND typeCode NOT LIKE '%GR%' "+
					"AND containerNumber IN (SELECT ctrno FROM TosGumRdsDataMt WHERE ves='" + vesvoy.substring(0, 3) + "' AND voy='" + vesvoy.substring(3, 6) + "' AND loadDischServ is null AND "+
					"ctrno in (SELECT containerNumber FROM TosGumStPlanCntrMt WHERE vesvoy='" + vesvoy + "' AND ds is not null))";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC";
			logger.info("getBlankCyFactsRecords query \n"+qry);
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getBlankCyFactsRecords end");
		return list;
	}
	public static ArrayList<TosGumRdsDataFinalMt> getUnknownConsignees(String vesvoy) {
		logger.info("getUnknownConsignees begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qry = "FROM TosGumRdsDataFinalMt WHERE vesvoy='" + vesvoy + "' AND dir='IN' AND ds='CY' AND (consignee like 'X %' or consignee like 'UNKNOWN' or consignee like 'WA' or consignee like 'WILL ADVISE' or consignee is null) " +
					"AND hazardousOpenCloseFlag <>'F' AND typeCode NOT LIKE '%GB%' AND typeCode NOT LIKE '%GR%' AND cargoNotes not like '%INVALID%ASSIGN%'";
			qry = qry + " ORDER BY CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC";
			logger.info("getUnknownConsignees query \n"+qry);
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getUnknownConsignees end");
		return list;
	}
	public static ArrayList<TosGumDcmMt> getDcmDataForUSCG(String vesvoy, boolean autos)
	{
		logger.info("getDcmDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				session = getSession();	
				ArrayList<TosGumDcmMt> dcmData;
				String qryStr = "FROM TosGumDcmMt WHERE vesvoy='" + vesvoy + "'";
				if(autos==false)
				{
					qryStr = qryStr + " AND nvl(carf,'N')<>'Y'";
				}
				qryStr = qryStr + " ORDER BY hazClass ASC,";
				qryStr = qryStr + " CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC,";
				qryStr = qryStr + " cnseq ASC";
				logger.info(qryStr);
				dcmData = (ArrayList<TosGumDcmMt>) session.createQuery(qryStr).list();
				if(dcmData!=null)
					logger.info("Result size:"+dcmData.size());
				else
					logger.info("Result :"+dcmData);
				logger.info("getDcmDataForUSCG end");
				return dcmData;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to find records for "+ vesvoy);
			}finally {
				if(session != null){ session.close();}
	         }
		}
		else
		{
			logger.error("Error: getDcmDataForUSCG(): " + vesvoy + " is not a valid vesvoy");
		}
		return null;
	}
	
	public static List getRiderReportData(String vesvoy) {
		logger.info("getRiderReportData begin");
		Session session = null;
		List list = null;
		try
		{
			session = getSession();
			String qry = "SELECT  vesvoy, booking_number, container_number, seal_number, type_code FROM ( " +
								"SELECT  vesvoy, booking_number, container_number||check_digit container_number, seal_number, type_code, COUNT(booking_number) over (PARTITION BY booking_number) rnk " +
								"FROM    tos_gum_rdsdata_final_mt WHERE   vesvoy ='" + vesvoy + "' AND dport = 'GUM' AND booking_number IS NOT NULL) " +
							"WHERE rnk > 1 ORDER BY booking_number"	;
				
			logger.info("Query --> " +qry);
			list = session.createSQLQuery(qry).list();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getRiderReportData end");
		return list;
	}
	
	/*public static ArrayList<TosGumRdsDataFinalMt> getRiderReportData(String vesvoy) {
		logger.info("getRiderReportData begin");
		Session session = null;
		ArrayList<TosGumRdsDataFinalMt> list = null;
		try
		{
			session = getSession();
			String qryStr = "FROM "+ TosGumRdsDataFinalMt.class.getName() +"  WHERE vesvoy='"+vesvoy+"' and dport='GUM' and booking_number IS NOT NULL order by booking_number desc";
			list = (ArrayList<TosGumRdsDataFinalMt>) session.createQuery(qryStr).list();
		
		}catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getRiderReportData end");
		return list;
	}*/
	// *************************************

	public static Transaction beginTransaction(Session session)
	{
		return session.beginTransaction();
	}
	public static void commitTransaction(Transaction transaction)
	{
		transaction.commit();
	}




}