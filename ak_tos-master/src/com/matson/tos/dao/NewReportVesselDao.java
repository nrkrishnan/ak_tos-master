package com.matson.tos.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.HibernateSessionFactory;
import com.matson.cas.refdata.mapping.TosDcmMt;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.cas.refdata.mapping.TosRefData;
import com.matson.cas.refdata.mapping.TosStowPlanChassisMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.exception.NLTErrors;
import com.matson.tos.processor.CommonBusinessProcessor;

public class NewReportVesselDao {
	private static Logger logger = Logger.getLogger(NewReportVesselDao.class);
	private static final String configFileLocation = "/tosrefdata.cfg.xml";
	// for junit testing comment the above line and uncomment the below line
	//private static final String configFileLocation = "/hibernate.cfg.xml";
	

	protected static Session getSession(){
		Session session = HibernateSessionFactory.currentSessionFactoryByCfg(configFileLocation).openSession();
		return session;
	}
	
	public static ArrayList<TosDcmMt> getDcmDataForUSCG(String vesvoy, boolean autos)
	{
		logger.info("getDcmDataForVesvoy begin");
		Session session = null;
		if(vesvoy.length()>=6)
		{
			try {
				session = getSession();	
				ArrayList<TosDcmMt> dcmData;
				String qryStr = "FROM TosDcmMt WHERE vesvoy='" + vesvoy + "'";
				if(autos==false)
				{
					qryStr = qryStr + " AND nvl(carf,'N')<>'Y'";
				}
				qryStr = qryStr + " ORDER BY hazClass ASC,";
				qryStr = qryStr + " CASE WHEN SUBSTR(containerNumber, 1, 4)='MATU' THEN SUBSTR(containerNumber, 5, LENGTH(containerNumber)) ELSE containerNumber END ASC,";
				qryStr = qryStr + " cnseq ASC";
				logger.info(qryStr);
				dcmData = (ArrayList<TosDcmMt>) session.createQuery(qryStr).list();
				if(dcmData!=null)
					logger.info("Result size:"+dcmData.size());
				else
					logger.info("Result :"+dcmData);
				logger.info("getDcmDataForVesvoy end");
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
			logger.error("Error: getDcmDataForVesvoy(): " + vesvoy + " is not a valid vesvoy");
		}
		return null;
	}

	public static List getAGContainerInspectionsList1(String Vesvoy){

		Session session = null;
		List<?> agCtrInspList1 = null;
		try
		{
			session = getSession();	
			String qryStr = "";

			agCtrInspList1 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '"+Vesvoy+"' AND "+ 
							"((crstatus LIKE '%AG%' "+
							" AND cargoNotes NOT LIKE '%EMPTY LIVESTOCK%' ) "+				 
							" OR (crstatus NOT LIKE '%AG%' AND cargoNotes LIKE '%COFFEE%') "+
					" OR ( cargoNotes LIKE '%EMPTY LIVESTOCK%')) ORDER BY dPort asc").list();


		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return agCtrInspList1;
	}


	public static List getAGContainerInspectionsList2(String Vesvoy){

		Session session = null;
		List<?> agCtrInspList2 = null;
		try
		{
			session = getSession();	
			agCtrInspList2 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND (crstatus  NOT LIKE '%AG%' AND " +
					"(cargoNotes LIKE '%COFFEE%' OR cargoNotes LIKE '%EMPTY LIVESTOCK%'))").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return agCtrInspList2;
	}
	public static List getBlankConsigneeCSReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> lBlankConsigneeList = null;
		try
		{
			session = getSession();	
			lBlankConsigneeList = session.createQuery(
					"FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND (consignee LIKE 'REQUIRES CS ACTION%'  OR Consignee LIKE 'UNAPPROVED VARIANCE%' OR cargoNotes = '%INVAID%ASSIGN%' ) AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE') order by containerNumber asc").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return lBlankConsigneeList;
	}

	public static List getBlankConsigneeList(String Vesvoy){
		Session session = null;
		List<?> blankConsigneeList = null;
		try
		{
			session = getSession();
			blankConsigneeList = session.createQuery(
					"FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND dir='IN' AND ds='CY' AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB') AND (consignee LIKE 'X%' OR consignee is null OR consignee IN ( 'UNKNOWN','WA','WILL ADVICE',' '))").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return blankConsigneeList;
	}
	public static void saveOrUpdateTosRdsDatFinalMt(TosRdsDataFinalMt tosRdsDataFinalMt){
		Session session = null;
		try
		{
			session = getSession();
			Transaction transaction = beginTransaction(session);
			session.saveOrUpdate(tosRdsDataFinalMt);	
			commitTransaction(transaction);
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
	}
	public static List getCustomsList(String Vesvoy){
		Session session = null;
		List<?> customsList = null;
		try
		{
			session = getSession();
			customsList = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND (crstatus LIKE '%BND%' OR crstatus LIKE '%INB%'  OR crstatus LIKE '%PER%' ) AND robFlag is null ORDER BY ds asc, containerNumber asc )").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return customsList;
	}
	public static List getDamageReportList(String Vesvoy){
		Session session = null;
		List<?> sfTagList = null;
		try
		{
			session = getSession();
			//sfTagList = session.createQuery(
					//" FROM TosRdsDataFinalMt  WHERE  vesvoy = '" + Vesvoy + "' AND damageCode is not null ").list();		
			String qryStr = "SELECT SUBSTR(TYPE_CODE, 1,3) AS TYPECODE, DAMAGE_CODE, DS, COUNT(*) AS TCOUNT FROM TOS_RDS_DATA_FINAL_MT WHERE VESVOY='" + Vesvoy + "' AND DAMAGE_CODE IS NOT NULL AND SUBSTR(TYPE_CODE, 1,3) IS NOT NULL GROUP BY DAMAGE_CODE, SUBSTR(TYPE_CODE, 1,3), DS ORDER BY TYPECODE ASC, DAMAGE_CODE DESC";
			sfTagList = session.createSQLQuery(qryStr).list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return sfTagList;
	}

	public static List getDPortChangesList(String Vesvoy){
		Session session = null;
		List<?> results = null;
		try
		{
			session = getSession();
			String qryStr = "SELECT R.CONTAINER_NUMBER||R.CHECK_DIGIT, R.DPORT AS DPORTN, S.DPORT AS DPORTO, R.CONSIGNEE_NAME, R.CELL FROM TOS_RDS_DATA_FINAL_MT R,TOS_STOW_PLAN_CNTR_MT S"+
			" WHERE R.CONTAINER_NUMBER = S.CONTAINER_NUMBER AND R.DS<>'AUT' AND R.DPORT <> S.DPORT AND R.DPORT <> 'MIX' AND R.VESVOY='" + Vesvoy + "' AND S.VESVOY='" + Vesvoy + "'"+
			" AND R.CONSIGNEE_NAME IS NOT NULL AND R.CELL IS NOT NULL AND R.TYPE_CODE NOT LIKE '%GR' AND R.TYPE_CODE NOT LIKE '%GB' AND R.TYPE_CODE NOT LIKE '%BL'"+
			" UNION "+
			" SELECT S.CONTAINER_NUMBER||S.CHECK_DIGIT, R.DPORT AS DPORTN, S.DPORT AS DPORTO, R.CONSIGNEE_NAME, R.CELL FROM TOS_RDS_DATA_FINAL_MT R,TOS_STOW_PLAN_CNTR_MT S"+
			" WHERE S.CONTAINER_NUMBER = R.CONTAINER_NUMBER AND S.CONSIGNEE IS NOT NULL AND S.CELL IS NOT NULL AND R.DS<>'AUT' AND R.VESVOY='" + Vesvoy + "' AND S.VESVOY='" + Vesvoy + "'"+
			" AND R.DPORT <> 'MIX' AND S.DPORT <> R.DPORT AND R.TYPE_CODE NOT LIKE '%GR' AND R.TYPE_CODE NOT LIKE '%GB' AND R.TYPE_CODE NOT LIKE '%BL'";
			
			//logger.info("\n"+qryStr+"\n");
			results = session.createSQLQuery(qryStr).list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return results;
	}
	public static List getDuplicateContainerList(String Vesvoy){
		Session session = null;
		List<?> dupCtrList = null;
		try
		{
			session = getSession();
			//Query needs to be changed...
			dupCtrList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "'").list();		

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return dupCtrList;
	}

	public static List getNotCSXList1(String Vesvoy){
		Session session = null;
		List<?> notCSXList1 = null;
		try
		{
			session = getSession();

			notCSXList1 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND locationRowDeck <> 'CSX'   AND hazardousOpenCloseFlag NOT IN ('G','F','M') AND hazardousOpenCloseFlag <> null  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notCSXList1;
	}
	public static List getNotCSXList2(String Vesvoy){
		Session session = null;
		List<?> notCSXList2 = null;
		try
		{
			session = getSession();

			notCSXList2 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  locationRowDeck <> 'CSX' AND hazardousOpenCloseFlag  IN ('G','F') AND trade = 'H'  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notCSXList2;
	}

	public static List getNotCSXList3(String Vesvoy){
		Session session = null;
		List<?> notCSXList3 = null;
		try
		{
			session = getSession();

			notCSXList3 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  locationRowDeck <> 'CSX' AND hazardousOpenCloseFlag = 'F'  AND trade NOT IN ('F','G')  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notCSXList3;
	}

	public static List getNotCSXList4(String Vesvoy){
		Session session = null;
		List<?> notCSXList4 = null;
		try
		{
			session = getSession();

			notCSXList4 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  locationRowDeck <> 'CSX' AND hazardousOpenCloseFlag IN ('G','M')  AND loadPort is null AND  dPort IN ('G','M')  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notCSXList4;
	}

	public static List getCSXList1(String Vesvoy){
		Session session = null;
		List<?> CSXList1 = null;
		try
		{
			session = getSession();

			CSXList1 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  locationRowDeck = 'CSX' " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return CSXList1;
	}
	public static List getCSXList2(String Vesvoy){
		Session session = null;
		List<?> CSXList2 = null;
		try
		{
			session = getSession();

			CSXList2 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND locationRowDeck = 'CSX'  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return CSXList2;
	}

	public static List getTRIVESList1(String Vesvoy){
		Session session = null;
		List<?> TRIVESList1 = null;
		try
		{
			session = getSession();

			TRIVESList1 = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  locationRowDeck <> 'TRIVES'  " +
					" AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE')").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return TRIVESList1;
	}

	public static List getMISReeferReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> misReeferList = null;
		try
		{
			session = getSession();

			misReeferList = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND containerNumber NOT LIKE '687%' AND ((hazardousOpenCloseFlag = 'M' AND typeCode LIKE 'R%') OR (hazardousOpenCloseFlag  is null AND  dport IN( 'MAJ','KWJ','EBY','JIS') and typeCode LIKE 'R%')) " +
					" ORDER BY containerNumber").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return misReeferList;
	}


	public static List getMixPortContainerReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> mixPortCtrList = null;
		try
		{
			session = getSession();

			mixPortCtrList = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND dPort= 'MIX' AND ds <> 'AUT' AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE') order by containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mixPortCtrList;
	}

	public static List getMTYContainerReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			mtyCtrList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND dir = 'MTY' AND (typeCode NOT LIKE '%GR' AND typeCode NOT LIKE '%GB' AND typeCode NOT LIKE '%GE') order by containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static List getMultiContainerCellsReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> multiCtrCellsList = null;
		try
		{
			session = getSession();

			multiCtrCellsList = session.createQuery(
					" FROM TosRdsDataFinalMt  WHERE  vesvoy = '" + Vesvoy + "' AND cell IN (SELECT cell FROM TosRdsDataFinalMt where  vesvoy = '" + Vesvoy + "' GROUP BY Cell having count(*) > 1) ORDER BY cell, containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return multiCtrCellsList;
	}

	public static List getTotalNoOfContainersList(String Vesvoy){
		Session session = null;
		List<?> totalCtrList = null;
		try
		{
			session = getSession();
			totalCtrList = session.createSQLQuery(
					"SELECT count(distinct CONTAINER_NUMBER) FROM TOS_RDS_DATA_FINAL_MT where  VESVOY = '" + Vesvoy + "'").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return totalCtrList;
	}
	public static List getParadiseBeveragesCtrsList(String Vesvoy){
		Session session = null;
		List<?> paradiseBeveragesCtrsList = null;
		try
		{
			session = getSession();
			paradiseBeveragesCtrsList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND consignee LIKE  'PARADISE BE%'  AND loadPort IN ('SEA','PDX')").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return paradiseBeveragesCtrsList;
	}

	public static List getProduceCtrList(String Vesvoy){
		Session session = null;
		List<?> produceCtrList = null;
		try
		{
			session = getSession();
			produceCtrList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND typeCode LIKE 'R%' AND hsf4 is not null AND  cargoNotes LIKE 'SIT%'  ORDER BY consignee, containerNumber").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return produceCtrList;
	}

	/*public static List getSitpPoduceCtrList(String Vesvoy){
	Session session = null;
	 List<?> sitproduceCtrList = null;
	    try
	    {
	       session = getSession();
	       sitproduceCtrList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND typeCode LIKE 'R%' AND hsf4 is not null AND cargoNotes LIKE 'SIT%'  ORDER BY consignee ").list();

	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	    finally{
	      if(session != null){ session.close();}
	    }
	return sitproduceCtrList;
}*/

	public static List getReeferForFandMContainersReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> reeferList = null;
		try
		{
			session = getSession();
			reeferList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND temp <> 'AMB' AND temp is not null AND typeCode NOT LIKE 'D%' ORDER BY REPLACE (containerNumber, 'MATU','') asc ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return reeferList;
	}
	public static List getHoldsReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> holdList = null;
		try
		{
			session = getSession();
			holdList = session.createQuery(
					" FROM  TosRdsDataFinalMt WHERE vesvoy='" + Vesvoy + "' AND crstatus IS NOT NULL ORDER BY containerNumber").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return holdList;
	}
	public static List getNotifyPartyReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> notifyList = null;
		try
		{
			session = getSession();
			notifyList = session.createQuery(
					" FROM  TosRdsDataFinalMt WHERE vesvoy='" + Vesvoy + "' AND cargoNotes like '%N/P%' ORDER BY dport, containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notifyList;
	}
	public static List getAutomaticTruckerAssignmentReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> autoTruckAssignList = null;
		try
		{
			session = getSession();
			autoTruckAssignList = session.createQuery(
					" FROM  TosRdsDataFinalMt WHERE vesvoy='" + Vesvoy + "' AND truck is NOT NULL AND consignee IS NOT NULL ORDER BY consignee").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return autoTruckAssignList;
	}
	public static List getRobContainersOnVesselReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> robContainersList = null;
		try
		{
			session = getSession();
			robContainersList = (List<?>) session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND robFlag <>'Y' AND (typeCode LIKE '%GR' OR typeCode LIKE '%GB' OR typeCode LIKE '%BL') order by containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return robContainersList;
	}

	public static List getSFTagReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> sfTagList = null;
		try
		{
			session = getSession();
			sfTagList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE  vesvoy = '" + Vesvoy + "' AND  ds = 'CY' ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return sfTagList;
	}

	public static List getTagConsigneeCallSheetReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> tagList = null;
		try
		{
			session = getSession();
			tagList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND consignee is not null AND consignee<>'AUTOMOBILE' ORDER BY consignee,dport,containerNumber").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return tagList;
	}

	public static List getMTYContainerSegregationReportGeneratorList(String vvd){
		Session session = null;
		List mtyCtrList = new ArrayList();
		List mtyCtrList1 = null;
		String cliOwner = null;
		try
		{
			session = getSession();
			ArrayList<TosRefData> rdsData;
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			mtyCtrList = session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' and dir = 'MTY' and type_code LIKE 'R%' and substr(container_number,1,4) <> 'MATU')").list();
			String qryStr = "FROM TosRefData WHERE key = 'CLI_OWNER' ";
			Query qry = session.createQuery(qryStr);
			rdsData = (ArrayList)qry.list();
			cliOwner = rdsData.get(0).getValue();
			logger.info("cliOwner  is "+cliOwner);
			mtyCtrList1 = session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' and dir = 'MTY' and type_code not LIKE 'R%'").list();
			if (mtyCtrList1 != null && mtyCtrList1.size() >0) {
				for (int i=0;i<mtyCtrList1.size();i++) {
					TosStowPlanCntrMt localCntr = (TosStowPlanCntrMt)mtyCtrList1.get(i);
					logger.info("localCntr.owner :"+localCntr.getOwner());
					if (cliOwner.contains(localCntr.getOwner())) {
						mtyCtrList.add(localCntr); 
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}
	public static List getCYHonContainerReportGeneratorList(String vvd){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+ leg +"' AND dir <> 'MTY' AND  ds = 'CY'  ORDER BY containerNumber").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}
	public static List getDamageBargeReportList(String vvd){
		Session session = null;
		List<?> sfTagList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			sfTagList = session.createQuery(
					" FROM TosStowPlanCntrMt  WHERE  vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' AND damageCode is not null AND damageCode <> 'Y'").list();		
			logger.info("Damage data size:"+sfTagList.size());
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return sfTagList;
	}
	public static List getNoDamageReportList(String vvd){
		Session session = null;
		List<?> sfTagList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			sfTagList = session.createQuery(
					" FROM TosStowPlanCntrMt  WHERE  vesvoy = '" + vesvoy + "' AND leg = '"+leg+"'  AND damageCode = 'Y' ").list();		
			logger.info("No Damage data size:"+sfTagList.size());
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return sfTagList;
	}


	public static List getReeferContainerReportGeneratorList(String vvd){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' AND dir <> 'MTY' AND  typeCode LIKE 'R%' and temp <> 'AMB' ORDER BY containerNumber ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static List getModifiedFlatracksReportGeneratorList(String vvd){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);      
			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' AND (containerNumber LIKE '%399011' or CONTAINER_NUMBER LIKE '%399010') " +
					"OR ( TYPE_CODE LIKE 'F%' AND  (comments like '%MODIFIED%' OR cargoNotes LIKE '%MODIFIED%')) ORDER BY containerNumber ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static List getDamageContainersBargeReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + Vesvoy + "' AND damageCode is not null  AND damageCode <> 'Y' ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static List getMatuContainersReportGeneratorList(String vvd){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);	      
			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' AND dir = 'OUT' and owner = 'MATU'  AND hazardousOpenCloseFlag in ('G', 'F') ORDER BY containerNumber ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static List getClientContainersBargeReportGeneratorList(String vvd){
		Session session = null;
		List<?> mtyCtrList = null;
		try
		{
			session = getSession();

			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			mtyCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+ leg +"' AND srv <> 'MAT' ").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return mtyCtrList;
	}

	public static ArrayList<NLTErrors> getNLTDiscrepanciesBargeReportGeneratorList(String Vesvoy){
		logger.info("getNLTDiscrepanciesBargeReportGeneratorList - Vesvoy  "+Vesvoy);
		ArrayList<NLTErrors> resultList = new ArrayList<NLTErrors>();
		Session session = null;
		List<?> totalCtrList = null;
		try
		{
			session = getSession();
			totalCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + Vesvoy.substring(0, 6) + "' and leg = '"+Vesvoy.substring(6, 7) +"'  ").list();
			logger.info("tos ctr size:"+totalCtrList.size());
			resultList = CommonBusinessProcessor.extractBargeNLTDescripancies(totalCtrList, Vesvoy);
			if(resultList!=null)
				logger.info("NLT Discrepancies : " + resultList.size());
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			if(session != null){ session.close();}
		}

		return resultList;
	}

	public static List getInvalidCellList(String Vesvoy){
		logger.info("getInvalidCellList - Vesvoy  "+Vesvoy);
		Session session = null;
		List<?> totalCtrList = null;
		try
		{
			session = getSession();
			totalCtrList = session.createSQLQuery("SELECT distinct container_number, cell FROM TOS_STOW_PLAN_CNTR_MT WHERE vesvoy = '" + Vesvoy.substring(0, 6) + "' and leg = '"+Vesvoy.substring(6, 7) +"' and substr(vesvoy, 0,3)||cell not in ( select tosVesCell.cell from TOS_VES_CELL_LOC_MZ tosVesCell where tosVesCell.vessel = '" + Vesvoy.substring(0, 3) + "'   ) ").list();
			logger.info("tos ctr size:"+totalCtrList.size());

		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			if(session != null){ session.close();}
		}

		return totalCtrList;
	}



	public static ArrayList getNumberODRecords(String vvd) {
		logger.info("Vesvoy  "+vvd);
		ArrayList resultList = new ArrayList();
		Session session = null;
		HashMap map = new HashMap();
		List<?> totalCtrList = null;
		List<?> totalbareChaList = null;
		List<?> totalMarriedChaList = null;
		int noOfContainers = 0;
		int noOfBareChasis = 0;
		int noOfMarriedChasis = 0;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			totalCtrList = session.createQuery(
					" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+ leg +"'").list();
			noOfContainers = totalCtrList.size();
			map.put("NumberOfContainers", noOfContainers);

			totalbareChaList = session.createQuery(
					" FROM TosStowPlanChassisMt WHERE vesvoy = '" + vesvoy + "' and tosStowPlanCntrMt is null and createDate = TO_DATE(TO_CHAR(sysdate, 'MM/DD/YYYY'), 'MM/DD/YYYY') ").list();
			noOfBareChasis = totalbareChaList.size();
			map.put("NumberOfBareChasis", noOfBareChasis);


			totalMarriedChaList = session.createQuery(
					" FROM TosStowPlanChassisMt where  VESVOY = '" + vesvoy + "' and tosStowPlanCntrMt is not null and createDate = TO_DATE(TO_CHAR(sysdate, 'MM/DD/YYYY'), 'MM/DD/YYYY')").list();
			noOfMarriedChasis = totalMarriedChaList.size();

			map.put("NumberOfMarriedChasis", noOfMarriedChasis);
			logger.info("no o frecords map is "+map.toString());
			resultList.add(map);


		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			if(session != null){ session.close();}
		}

		return resultList;
	}

	public static ArrayList getMultiCellContainerBarge(String vvd) {
		logger.info("Vesvoy  "+vvd);
		ArrayList resultList = new ArrayList();
		Session session = null;
		HashMap map = null;
		List<?> multiCtrCellsList = null;

		try
		{

			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			multiCtrCellsList = session.createQuery(
					" FROM TosStowPlanCntrMt  WHERE  vesvoy = '" + vesvoy + "' AND leg = '"+ leg +"' AND cell IN (SELECT cell FROM TosStowPlanCntrMt where  vesvoy = '" +  vesvoy + "' AND leg = '"+ leg +"' GROUP BY Cell having count(*) > 1) ORDER BY cell, containerNumber asc").list();
			for(int i=0;i<multiCtrCellsList.size(); i++){
				map = new HashMap();
				TosStowPlanCntrMt tosStowPlan = (TosStowPlanCntrMt)multiCtrCellsList.get(i);
				String cd = tosStowPlan.getCheckDigit();
				cd = cd==null?"X":cd;
				map.put("ContainerNumber", tosStowPlan.getContainerNumber()+cd);
				map.put("Dir", tosStowPlan.getDir());
				map.put("DPort", tosStowPlan.getDport());
				map.put("Consignee", tosStowPlan.getConsignee());
				map.put("Cell", tosStowPlan.getCell());
				map.put("TypeCode", tosStowPlan.getTypeCode());
				resultList.add(map);

			}



		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			if(session != null){ session.close();}
		}

		return resultList;
	}

	public static ArrayList getDestPodList() {

		Session session = null;
		ArrayList<TosDestPodData> tosDestPodData = null;
		try {
			session = getSession();
			String str = "FROM TosDestPodData";
			Query qry = session.createQuery(str);
			tosDestPodData = (ArrayList<TosDestPodData>)qry.list();
		}catch(Exception ex){

		}
		return tosDestPodData;
	}

	public static ArrayList<TosRdsDataFinalMt> getRdsDataFinalForVesvoy(String vesvoy){
		Session session = null;
		ArrayList<TosRdsDataFinalMt> rdsDataFinalList = null;
		try
		{
			session = getSession();	
			rdsDataFinalList = (ArrayList<TosRdsDataFinalMt>) session.createQuery("FROM TosRdsDataFinalMt WHERE vesvoy = '" + vesvoy + "' order by containerNumber asc").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return rdsDataFinalList;
	}
	public static ArrayList<TosStowPlanCntrMt> getStowPlanForVesvoy(String vesvoy){
		Session session = null;
		ArrayList<TosStowPlanCntrMt> stowPlanList = null;
		try
		{
			session = getSession();	
			stowPlanList = (ArrayList<TosStowPlanCntrMt>) session.createQuery("FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "'").list();
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return stowPlanList;
	}
	public static String getLoadPort(String vvd)
	{
		Session session = null;
		List temp = null;
		String loadPort = "";
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);

			String qry = "SELECT DISTINCT LOAD_PORT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"'";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				loadPort = (String)temp.get(0);
				logger.info("LoadPort:::"+loadPort);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return loadPort;
	}
	public static List getInboundTypeCodeSummary(String vvd){
		logger.info("getInboundTypeCodeSummary::::BEGIN");
		Session session = null;
		List tcList = null;
		List temp = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			String qry = "SELECT 'D' AS REC, 'E' AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='MTY' AND DAMAGE_CODE IS NOT NULL AND OWNER<>'APLU' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY SUBSTR(TYPE_CODE, 1,6)";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'M' AS REC, 'E' AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='MTY' AND DAMAGE_CODE IS NULL AND OWNER<>'APLU' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY SUBSTR(TYPE_CODE, 1,6)";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'A' AS REC, 'E' AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='MTY' AND DAMAGE_CODE IS NULL AND OWNER='APLU' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY SUBSTR(TYPE_CODE, 1,6)";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'X' AS REC, CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='OUT' AND DS<>'AUT' AND CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) IS NOT NULL AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE), SUBSTR(TYPE_CODE, 1,6) ORDER BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) ASC";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'Y' AS REC, CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='OUT' AND DS='AUT' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE), SUBSTR(TYPE_CODE, 1,6) ORDER BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) ASC";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'Z' AS REC, ' ' AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='IN' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE), SUBSTR(TYPE_CODE, 1,6) ORDER BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) ASC";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'U' AS REC, CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CNTR_MT WHERE VESVOY='"+vesvoy+"' AND LEG='"+leg+"' AND DIR='IN' AND DS='AUT' AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE), SUBSTR(TYPE_CODE, 1,6) ORDER BY CONCAT(ACTUAL_VESSEL, ACTUAL_VOYAGE) ASC";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			qry = "SELECT 'H' AS REC, 'K' AS VV, SUBSTR(TYPE_CODE, 1,6) AS TYPECODE, COUNT(*) AS TCOUNT FROM TOS_STOW_PLAN_CHASSIS_MT WHERE VESVOY='"+vesvoy+"' AND TO_CHAR(CREATE_DATE,'MM-DD-YYYY') = TO_CHAR(SYSDATE,'MM-DD-YYYY') AND  CONTAINER_NUMBER IS NULL AND SUBSTR(TYPE_CODE, 1,6) IS NOT NULL GROUP BY SUBSTR(TYPE_CODE, 1,6)";
			temp = session.createSQLQuery(qry).list();
			if(temp!=null)
			{
				if(tcList==null)
					tcList = temp;
				else
					tcList.addAll(temp);
			}
			logger.info("TC Result size:::"+tcList.size());
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return tcList;
	}

	public static List getEBSITReportGeneratorList(String vvd){
		logger.info("getEBSITReportGeneratorList begin "+vvd);
		Session session = null;
		List ebSitList = null;
		try
		{
			session = getSession();
			ArrayList<TosRefData> rdsData;
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			ebSitList = session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' and (commodity like '%SIT%' OR commodity like '%S I T%' OR commodity like '%S.I.T%' )").list();
		}catch(Exception e) {
			logger.error("getEBSITReportGeneratorList excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getEBSITReportGeneratorList end ");
		return ebSitList;
	}
	
	public static List getHonAutoCntrsOnVV(String vvd){
		logger.info("getHonAutoCntrsOnVV begin "+vvd);
		Session session = null;
		List honAutiCntrsList = null;
		try
		{
			session = getSession();
			ArrayList<TosRefData> rdsData;
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			honAutiCntrsList = session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' and ds = 'AUT' )").list();
		}catch(Exception e) {
			logger.error("getHonAutoCntrsOnVV excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getHonAutoCntrsOnVV end ");
		return honAutiCntrsList;
	}
	
	public static List getNoraContainersOnVessel(String vvd){
		logger.info("getNoraContainersOnVessel begin "+vvd);
		Session session = null;
		List noraCntrList = null;
		try
		{
			session = getSession();
			ArrayList<TosRefData> rdsData;
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			noraCntrList = session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' and owner = 'NORA')").list();
		}catch(Exception e) {
			logger.error("getNoraContainersOnVessel excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getNoraContainersOnVessel end ");
		return noraCntrList;
	}
	
	public static List getAleReport(String vvd){
		logger.info("getAleReport begin "+vvd);
		Session session = null;
		List aleCellsListCntr = null;
		List<TosStowPlanCntrMt> cellList = new ArrayList();
		
		try
		{
			session = getSession();
			ArrayList<TosRefData> rdsData;
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			logger.info("select case when length(stow.cell) = 4 then substr(stow.cell,3,1) when length(stow.cell) = 5 then substr(stow.cell,1,1) when length(stow.cell) = 6 then cellloc.ves_row end as rowLoc ,  case when length(stow.cell) = 4 then substr(stow.cell,2,1)  when length(stow.cell) = 5 then substr(stow.cell,4,2) when length(stow.cell) = 6 then cellloc.ves_run end as runLoc ,  case when length(stow.cell) = 4 then substr(stow.cell,3,2)  when length(stow.cell) = 5 then substr(stow.cell,2,2)  when length(stow.cell) = 6 then cellloc.ves_tier  end as tier  from tos_stow_plan_cntr_mt stow LEFT OUTER JOIN TOS_VES_CELL_LOC_MZ cellloc ON stow.vesvoy||stow.cell = cellloc.cell where substr(stow.vesvoy,1,3) = 'ALE'and stow.vesvoy = '"+vesvoy+"' and stow.leg='"+leg+"'");;
			aleCellsListCntr = session.createSQLQuery("select stow.container_number||CHECK_DIGIT,stow.cell,commodity,actual_vessel||actual_voyage,cweight,stow.chassis_number,case when length(stow.cell) = 4 then substr(stow.cell,3,1) when length(stow.cell) = 5 then substr(stow.cell,1,1) when length(stow.cell) = 6 then cellloc.ves_row end as rowLoc ,  case when length(stow.cell) = 4 then substr(stow.cell,2,1)  when length(stow.cell) = 5 then substr(stow.cell,4,2) when length(stow.cell) = 6 then cellloc.ves_run end as runLoc ,  case when length(stow.cell) = 4 then substr(stow.cell,3,2)  when length(stow.cell) = 5 then substr(stow.cell,2,2)  when length(stow.cell) = 6 then cellloc.ves_tier  end as tier  from tos_stow_plan_cntr_mt stow LEFT OUTER JOIN TOS_VES_CELL_LOC_MZ cellloc ON stow.vesvoy||stow.cell = cellloc.cell where substr(stow.vesvoy,1,3) = 'ALE' and stow.vesvoy = '"+vesvoy+"' and stow.leg='"+leg+"' union select stow.chassis_number||stow.CHASSIS_CD, loc,null,null,null,null, case when length(stow.loc) = 4 then substr(stow.loc,3,1) when length(stow.loc) = 5 then substr(stow.loc,1,1) when length(stow.loc) = 6 then cellloc.ves_row end as rowLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,2,1)  when length(stow.loc) = 5 then substr(stow.loc,4,2) when length(stow.loc) = 6 then cellloc.ves_run end as runLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,3,2)  when length(stow.loc) = 5 then substr(stow.loc,2,2)  when length(stow.loc) = 6 then cellloc.ves_tier  end as tier  from tos_stow_plan_chassis_mt stow LEFT OUTER JOIN TOS_VES_CELL_LOC_MZ cellloc ON stow.vesvoy||stow.loc = cellloc.cell where stow.container_number is null and substr(stow.vesvoy,1,3) = 'ALE' and stow.vesvoy = '"+vesvoy+"' and to_char(create_date,'MM-DD-YYYY') = to_char(SYSDATE,'MM-DD-YYYY') order by 7 asc,8 desc,9 asc" ).list();
			if (aleCellsListCntr !=null)
				logger.info("aleCellsList size "+aleCellsListCntr.size());
			
			List<Object[]> results = aleCellsListCntr;
			for (Object[] result : results) {
				TosStowPlanCntrMt stowMt = new TosStowPlanCntrMt();
				stowMt.setContainerNumber((String)result[0]);
				stowMt.setCell((String)result[1]);
				stowMt.setCommodity((String)result[2]);
				stowMt.setMisc1((String)result[3]);
				stowMt.setCweight((BigDecimal)result[4]);
				stowMt.setChassisNumber((String)result[5]);
				cellList.add(stowMt);
			}
			logger.info("CHASSIS FROM HERE");
			/*System.out.println("select stow.chassis_number, case when length(stow.loc) = 4 then substr(stow.loc,3,1) when length(stow.loc) = 5 then substr(stow.loc,1,1) when length(stow.loc) = 6 then cellloc.ves_row end as rowLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,2,1)  when length(stow.loc) = 5 then substr(stow.loc,4,2) when length(stow.loc) = 6 then cellloc.ves_run end as runLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,3,2)  when length(stow.loc) = 5 then substr(stow.loc,2,2)  when length(stow.loc) = 6 then cellloc.ves_tier  end as tier  from tos_stow_plan_chassis_mt stow LEFT OUTER JOIN TOS_VES_CELL_LOC_MZ cellloc ON stow.vesvoy||stow.loc = cellloc.cell where stow.container_number is null and substr(stow.vesvoy,1,3) = 'ALE'and stow.vesvoy = 'ALE308' and to_char(create_date,'MM-DD-YYYY') = to_char(SYSDATE,'MM-DD-YYYY')");
			aleCellsListChassis = session.createSQLQuery("select stow.chassis_number, case when length(stow.loc) = 4 then substr(stow.loc,3,1) when length(stow.loc) = 5 then substr(stow.loc,1,1) when length(stow.loc) = 6 then cellloc.ves_row end as rowLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,2,1)  when length(stow.loc) = 5 then substr(stow.loc,4,2) when length(stow.loc) = 6 then cellloc.ves_run end as runLoc ,  case when length(stow.loc) = 4 then substr(stow.loc,3,2)  when length(stow.loc) = 5 then substr(stow.loc,2,2)  when length(stow.loc) = 6 then cellloc.ves_tier  end as tier  from tos_stow_plan_chassis_mt stow LEFT OUTER JOIN TOS_VES_CELL_LOC_MZ cellloc ON stow.vesvoy||stow.loc = cellloc.cell where stow.container_number is null and substr(stow.vesvoy,1,3) = 'ALE'and stow.vesvoy = 'ALE308' and to_char(create_date,'MM-DD-YYYY') = to_char(SYSDATE,'MM-DD-YYYY')  order by 1,2,3").list();
			List<Object[]> resultsChas = aleCellsListChassis;
			for (Object[] resultChas : resultsChas) {
				String unitNumber = (String)resultChas[0];
			    String rowLoc = (String)resultChas[1];
			    String runLoc = (String)resultChas[2];
			    String tier = (String)resultChas[3];
			    logger.info("getAleReport :"+unitNumber+" "+rowLoc + " "+runLoc +" "+tier);
			}*/
		}catch(Exception e) {
			logger.error("getAleReport excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getAleReport end");
		return cellList;
	}

	public static ArrayList<TosStowPlanCntrMt> getStowDataForCyLines(String vvd){
		logger.info("getStowDataForCyLines begin "+vvd);
		Session session = null;
		ArrayList<TosStowPlanCntrMt> cntrList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			cntrList = (ArrayList<TosStowPlanCntrMt>) session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' order by dir,type_code").list();
		}catch(Exception e) {
			logger.error("getStowDataForCyLines excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getStowDataForCyLines end ");
		return cntrList;
	}
	public static ArrayList<TosStowPlanChassisMt> getBareChassisDataForCyLines(String vvd){
		logger.info("getBareChassisDataForCyLines begin "+vvd);
		Session session = null;
		ArrayList<TosStowPlanChassisMt> chsList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			chsList = (ArrayList<TosStowPlanChassisMt>) session.createQuery(" FROM "+TosStowPlanChassisMt.class.getName()+" WHERE VESVOY='"+vesvoy+"' AND TO_CHAR(CREATE_DATE,'MM-DD-YYYY') = TO_CHAR(SYSDATE,'MM-DD-YYYY') AND  CONTAINER_NUMBER IS NULL").list();
		}catch(Exception e) {
			logger.error("getBareChassisDataForCyLines excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getBareChassisDataForCyLines end ");
		return chsList;
	}
	public static String getCliOwnerData(){
		logger.info("getCliOwnerData begin ");
		Session session = null;
		String cliOwner = "";
		ArrayList<TosRefData> cliList = null;
		try
		{
			session = getSession();
			cliList = (ArrayList<TosRefData>) session.createQuery(" FROM TosRefData WHERE key = 'CLI_OWNER' ").list();
			if(cliList!=null && cliList.size()>0)
			{
				cliOwner = cliList.get(0).getValue();
			}
		}catch(Exception e) {
			logger.error("getBareChassisDataForCyLines excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getBareChassisDataForCyLines end ");
		return cliOwner;
	}
	
	public static ArrayList<TosStowPlanCntrMt> getBargeStowageSummary(String vvd){
		logger.info("getStowDataForCyLines begin "+vvd);
		Session session = null;
		ArrayList<TosStowPlanCntrMt> cntrList = null;
		try
		{
			session = getSession();
			String vesvoy = vvd.substring(0, 6);
			String leg = vvd.substring(6, 7);
			cntrList = (ArrayList<TosStowPlanCntrMt>) session.createQuery(" FROM TosStowPlanCntrMt WHERE vesvoy = '" + vesvoy + "' AND leg = '"+leg+"' order by typeCode").list();
		}catch(Exception e) {
			logger.error("getStowDataForCyLines excetpion "+e);
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getStowDataForCyLines end ");
		return cntrList;
	}
	
	public static Transaction beginTransaction(Session session)
	{
		return session.beginTransaction();
	}
	public static void commitTransaction(Transaction transaction)
	{
		transaction.commit();
	}
	
	public static List getDistinctConsignees(String vesvoy){
		logger.info("getDistinctConsignees in dao begin: "+vesvoy);
		Session session = null;
		List<String> results = null;
		try
		{
			session = getSession();	
			
			String qryStr = "SELECT DISTINCT R.CONSIGNEE FROM TOS_RDS_DATA_FINAL_MT R"+
					" WHERE R.VESVOY='" + vesvoy + "' AND R.CONSIGNEE IS NOT NULL ORDER BY R.CONSIGNEE ASC";

			results = session.createSQLQuery(qryStr).list();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		logger.info("getDistinctConsignees in dao end");
		return results;
	}	
	
	public static List getNotifyPartyForInvalidNPReport(String Vesvoy){
		Session session = null;
		List<?> notifyList = null;
		try
		{
			session = getSession();
			notifyList = session.createQuery(
					" FROM  TosRdsDataFinalMt WHERE vesvoy='" + Vesvoy + "' AND notifyParty is not null ORDER BY dport, containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return notifyList;
	}
	
	public static List getInvalidNotifyPartyReportGeneratorList(String Vesvoy,ArrayList invalidNotifyList ){
		Session session = null;
		List<?> invalidNPList = null;
		if (invalidNotifyList !=null && invalidNotifyList.size() >= 1) {
			for (int k =0;k<invalidNotifyList.size();k++) {
				logger.info("Invalid trucker "+(String)invalidNotifyList.get(k));
			}
		}
		try
		{
			session = getSession();
			invalidNPList = session.createQuery(
					" FROM  TosRdsDataFinalMt WHERE vesvoy='" + Vesvoy + "' AND  notifyParty IN (:npList)  ORDER BY dport, containerNumber asc")
										.setParameterList("npList", invalidNotifyList).list();

			
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return invalidNPList;
	}
	
	public static List getReturnToShipperrReportGeneratorList(String Vesvoy){
		Session session = null;
		List<?> fullRtnShipperList= null;
		try
		{
			session = getSession();

			fullRtnShipperList = session.createQuery(
					" FROM TosRdsDataFinalMt WHERE vesvoy = '" + Vesvoy + "' AND orientation = 'F' AND PLAN_DISP IN ('9','A','B')  order by containerNumber asc").list();

		}catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			if(session != null){ session.close();}
		}
		return fullRtnShipperList;
	}

}