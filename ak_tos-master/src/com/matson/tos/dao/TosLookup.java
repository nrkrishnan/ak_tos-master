package com.matson.tos.dao;

import com.matson.cas.refdata.mapping.TosRdsDataFinalMt;
import com.matson.tos.constants.TransitState;
import com.matson.tos.exception.TosException;
import com.matson.tos.vo.CalendarVO;
import com.matson.tos.vo.CommodityVO;
import com.matson.tos.vo.VesselVisitVO;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TosLookup
 *
 * @author Steven Bauer
 *
 *
 *   File: CasSearchDao.java
 *
 * <PRE>
 *  Change History
 *  Ver Name    Date     Comment
 *  1.0 SKB     Sep 13, 2006  Created
 *  2.0 SKB		Dec 18, 2008  Added tstate
 *  3.0 SKB     May 5,  2009  Added check for exact id before wildcard id.
 *  3.1 SKB		Oct 01, 2009  Added lookup of operator id.
 *  3.2 GR      Nov 11  2009  Added lookup for Active unit Category
 *  3.3 GR      Jan 03  2011  Added Query and method[hasDiffBkgLineOperator]for BOB lineOperator update
 *  	Karthik	Dec 17	2012  Added lookup for Trucker
 *  	Karthik	Dec 31	2012  Changes: Get Trucker function and query
 *  	Karthik Apr 17	2013  Added: getBeginReceive()
 *  	Karthik	Sep 05	2013  Added: getRestowMarkedContainersForVesVoy()
 *  	Karthik	Oct 14  2013  Added: getUnitTruckingCo()
 *  	Karthik Oct 22  2013  Changed query "GET_TSTATE_CARR_MODE" to get all unit properties
 *  	Karthik Oct 24  2013  Changed: Renamed method name getTstateCarrierMode to getUnitDetails, query change
 *  						  Added: getUnitOBCarrierIDMode()
 *  	Karthik Oct 29  2013  Changed: Renamed method name getUnitOBCarrierIDMode to getUnitCarrierDetails and query changed
 *  	Karthik Jan 10  2014  Added: getSITByBooking()
 * </PRE>
 */
public class TosLookup extends com.matson.cas.common.dao.BaseDAO{

	public static final String VST_ID = "VST_ID";
	public static final String CURRENT_FACILITY = "CURRENT_FACILITY";
	public static final String PHASE = "PHASE";
	public static final String NEXT_FACILITY = "NEXT_FACILITY";
	public static final String IB_VYG = "IB_VYG";
	public static final String OB_VYG = "OB_VYG";
	public static final String SVC_ID = "SVC_ID";
	public static final String CLASSIFICATION = "CLASSIFICATION";
	public static final String NONE = "NONE";
	public static final String CARRIER_ID = "CARRIER_ID";
	/**
     * log4j class
     */
    private static Logger LOGGER = Logger.getLogger(TosLookup.class);

	private static final String SEARCH_SHIPPER_BY_BN = "select b.id from tosmgr.ref_bizunit_scoped b left outer join tosmgr.inv_eq_base_order bk on b.gkey = bk.shipper_gkey where bk.nbr = ? ";
	private static final String SEARCH_CONSIGNEE_BY_BN = "select b.id from tosmgr.ref_bizunit_scoped b left outer join tosmgr.inv_eq_base_order bk on b.gkey = bk.CONSIGNEE_GKEY where bk.nbr = ? ";

	private static final String SEARCH_BY_NAME = "select id from tosmgr.ref_bizunit_scoped where name=? and role = 'SHIPPER' and reference_set = 363";
	private static final String SEARCH_BY_ID = "select id from tosmgr.ref_bizunit_scoped where id=? and role = 'SHIPPER' and reference_set = 363";

//	private static final String SEARCH_TSTATE = "select transit_state from inv_unit_fcy_visit where UNIT_GKEY in (select gkey from tosmgr.inv_unit where ID in (?)) and FCY_GKEY = (select gkey from argo_facility where id = ?) order by transit_state";
	private static final String SEARCH_TSTATE = "select transit_state from inv_unit_fcy_visit where UNIT_GKEY in (select gkey from tosmgr.inv_unit where ID in (?)) and visit_state='1ACTIVE' order by transit_state";
	private static final String SEARCH_TSTATE_COMMODITY = "select fcy.transit_state, com.id from inv_unit_fcy_visit fcy join inv_unit unit  on  fcy.UNIT_GKEY = unit.gkey left outer join inv_goods goods  on unit.goods = goods.GKEY left outer join ref_commodity com on goods.commodity_gkey = com.gkey where unit.ID =  ? and visit_state='1ACTIVE' order by fcy.transit_state";

	private static final String SEARCH_EQUIPMENT = "select id_full from ref_equipment where  id like ? and LIFE_CYCLE_STATE = 'ACT'";

	//private static final String SEARCH_ACTIVE_VV = "select vst.id, fcy.id, vst.phase from argo_carrier_visit vst join argo_facility fcy on vst.FCY_GKEY = fcy.gkey join argo_visit_details vv on vst.CVCVD_GKEY = vv.GKEY where vst.phase in ('10CREATED','20INBOUND','30ARRIVED','40WORKING' ,'50COMPLETE') and vst.carrier_mode = 'VESSEL'";
//	private static final String SEARCH_ACTIVE_VV = "select vst.id, fcy.id, vst.phase, nxt_fcy.id, vst.ata, vst.atd, vv.eta,  vv.etd, dtl.IB_VYG, dtl.OB_VYG, svc.ID from argo_carrier_visit vst left outer join argo_facility fcy on vst.FCY_GKEY = fcy.gkey left outer join argo_facility nxt_fcy on vst.NEXT_FCY_GKEY = nxt_fcy.gkey join argo_visit_details vv on vst.CVCVD_GKEY = vv.GKEY join ref_carrier_service svc on vv.SERVICE = svc.GKEY left outer join vsl_vessel_visit_details dtl on vv.GKEY = dtl.VVD_GKEY where vst.phase in ('10CREATED','20INBOUND','30ARRIVED','40WORKING' ,'50COMPLETE') and vst.carrier_mode = 'VESSEL'";
	private static final String SEARCH_ACTIVE_VV = "select vst.id, fcy.id, vst.phase, nxt_fcy.id, vst.ata, vst.atd, vv.eta,  vv.etd, dtl.IB_VYG, dtl.OB_VYG, svc.ID from argo_carrier_visit vst left outer join argo_facility fcy on vst.FCY_GKEY = fcy.gkey left outer join argo_facility nxt_fcy on vst.NEXT_FCY_GKEY = nxt_fcy.gkey join argo_visit_details vv on vst.CVCVD_GKEY = vv.GKEY join ref_carrier_service svc on vv.SERVICE = svc.GKEY left outer join vsl_vessel_visit_details dtl on vv.GKEY = dtl.VVD_GKEY where vst.carrier_mode = 'VESSEL'";

	private static final String SEARCH_VESSEL_OP = "select biz.id from vsl_vessels vs join ref_bizunit_scoped biz on biz.gkey = vs.owner_gkey where vs.id = ? and vs.reference_set = 363";

	private static final String SEARCH_ACTIVE_UNIT_CATEGORY= "select b.category from inv_unit_fcy_visit a, inv_unit b WHERE a.UNIT_GKEY=b.gkey AND a.UNIT_GKEY IN (SELECT MAX(gkey) FROM inv_unit WHERE ID= ? AND visit_state NOT IN('2ADVISED')) AND a.FCY_GKEY = (SELECT gkey FROM argo_facility WHERE ID = ?) AND a.visit_state='1ACTIVE'";
	//3.3
	private static final String SEARCH_BKG = "SELECT I.LINE_GKEY,I.NBR FROM TOSMGR.INV_EQ_BASE_ORDER I WHERE VESSEL_VISIT_GKEY = (SELECT A.GKEY FROM TOSMGR.ARGO_CARRIER_VISIT A WHERE A.CARRIER_MODE='VESSEL' AND A.FCY_GKEY=(SELECT A.GKEY FROM TOSMGR.ARGO_FACILITY A WHERE ID='HON') AND ID=?) AND NBR=?";
	private static final String SEARCH_LINEOP = "SELECT R.ID FROM TOSMGR.REF_BIZUNIT_SCOPED R WHERE R.ROLE='LINEOP' AND R.REFERENCE_SET=363 AND R.GKEY=?";
	private static final String GET_CALENDER = "SELECT NAME,OCC_START,REPEAT_INTERVAL FROM ARGO_CAL_EVENT WHERE CALENDAR_GKEY IN (SELECT GKEY FROM ARGO_CALENDAR WHERE CALENDAR_TYPE='STORAGE') ORDER BY OCC_START ASC";
	private static final String GET_EDIT_FLAG="select gkey,id,FLEX_STRING11,CREATE_TIME from tosmgr.inv_unit where id like ? order by CREATE_TIME desc";
	private static final String GET_HOLDS = "select ID,IMPED_ROAD from tosmgr.INV_UNIT where ID like ? and visit_state='1ACTIVE' order by time_state_change desc";
	//private static final String GET_EQUIPMENT_TYPE = "select equipment.id_prefix||equipment.id_nbr_only,equiptype.id,equiptype.height_mm,equipment.strength_code,equipment.tare_kg,equipment.material,equiptype.nominal_height  from ref_equipment equipment, ref_equip_type equiptype where equipment.id_prefix||equipment.id_nbr_only = ? and equiptype.gkey = equipment.eqtyp_gkey";
	private static final String GET_EQUIPMENT_TYPE_BY_IDFULL = "SELECT EQUIPMENT.ID, EQUIPTYPE.ID,EQUIPTYPE.HEIGHT_MM,EQUIPMENT.STRENGTH_CODE,EQUIPMENT.TARE_KG,EQUIPMENT.MATERIAL,EQUIPTYPE.NOMINAL_HEIGHT  FROM REF_EQUIPMENT EQUIPMENT, REF_EQUIP_TYPE EQUIPTYPE WHERE (EQUIPMENT.ID_FULL = ? OR EQUIPMENT.ID = ?) AND EQUIPTYPE.GKEY = EQUIPMENT.EQTYP_GKEY AND EQUIPMENT.EQ_SUBCLASS='CTR'";
	//private static final String GET_EQUIPMENT_TYPE_BY_ID =  "SELECT EQUIPMENT.ID, EQUIPTYPE.ID,EQUIPTYPE.HEIGHT_MM,EQUIPMENT.STRENGTH_CODE,EQUIPMENT.TARE_KG,EQUIPMENT.MATERIAL,EQUIPTYPE.NOMINAL_HEIGHT  FROM REF_EQUIPMENT EQUIPMENT, REF_EQUIP_TYPE EQUIPTYPE WHERE EQUIPMENT.ID = ? AND EQUIPTYPE.GKEY = EQUIPMENT.EQTYP_GKEY AND EQUIPMENT.EQ_SUBCLASS='CTR'";
	//private static final String GET_TRUCKER1 = "SELECT  * FROM (SELECT  rbs.id,rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE   (rbs.name LIKE ? OR rbs.name LIKE ?) AND rbs.ROLE = 'SHIPPER' AND rbst.ID IS NOT NULL ORDER BY rar.created) WHERE   ROWNUM = 1";
	//private static final String GET_TRUCKER = "SELECT  consignee,trucker_id,trucker_name FROM (SELECT 1 ord,rbs.id,rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE   rbs.name = ? AND rbs.ROLE = 'SHIPPER' AND rbst.ID IS NOT NULL  UNION SELECT 2 ord,rbs.id,rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE rbs.name LIKE ? AND rbs.ROLE = 'SHIPPER' AND rbst.ID IS NOT NULL ORDER BY ord,created) WHERE   ROWNUM = 1";
	//--private static final String GET_TRUCKER = "SELECT rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE   rbs.name =? AND rbs.ROLE = 'SHIPPER' AND rbst.ID IS NOT NULL ORDER BY trucker_name nulls last,created desc";
	//--private static final String GET_TRUCKER1 = "SELECT rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE rbs.name LIKE ? AND rbs.ROLE = 'SHIPPER' AND rbst.ID IS NOT NULL ORDER BY trucker_name nulls last,created desc";
	private static final String GET_TRUCKER = "SELECT  distinct consignee,trucker_id FROM (SELECT rbs.id,rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE   rbs.name =? AND rbs.ROLE = 'SHIPPER'  AND RBS.LIFE_CYCLE_STATE = 'ACT' AND rbst.ID IS NOT NULL ORDER BY trucker_name nulls last,created desc )";
	private static final String GET_TRUCKER1 = "SELECT  distinct consignee,trucker_id FROM (SELECT rbs.id,rbs.name consignee,rbst.ID trucker_id,rbst.NAME trucker_name,rar.created FROM ref_bizunit_scoped rbs LEFT OUTER JOIN ref_agent_representation rar ON rbs.gkey = rar.bzu_gkey LEFT OUTER JOIN Ref_Agent ra ON ra.agent_id = rar.agent_gkey LEFT OUTER JOIN ref_bizunit_scoped rbst ON rbst.gkey = rar.agent_gkey AND rbst.role = 'AGENT' WHERE   rbs.name like ? AND rbs.ROLE = 'SHIPPER' AND RBS.LIFE_CYCLE_STATE = 'ACT' AND rbst.ID IS NOT NULL ORDER BY trucker_name nulls last,created desc )";
	//private static final String VALIDATE_TRUCKER = "SELECT * FROM REF_BIZUNIT_SCOPED WHERE ID=? and life_cycle_state = 'ACT' and PER_UNIT_GUARANTEE_LIMIT = 1 and role = 'HAULIER'";
	//"SELECT a.id,a.name consignee,a.notes trucker_id,b.NAME trucker_name FROM ref_bizunit_scoped a LEFT OUTER JOIN ref_bizunit_scoped b ON a.notes = b.id AND b.role = 'AGENT' WHERE a.role = 'SHIPPER' AND a.name = ?";
	private static final String GET_DUPLICATE_CONTAINER = "SELECT iu.ID, "
			+ "       iu.VISIT_STATE, "
			+ "       iu.CATEGORY, "
			+ "       iufv.TRANSIT_STATE "
			+ "FROM   INV_UNIT iu "
			+ "       JOIN INV_UNIT_FCY_VISIT iufv "
			+ "         ON iu.GKEY = iufv.UNIT_GKEY "
			+ "WHERE  iu.VISIT_STATE = '1ACTIVE' "
			+ "       AND iufv.TRANSIT_STATE = 'S40_YARD' "
			+ "       AND iu.ID = ? ";	
	private static final String VALIDATE_VESVOY_DEPARTED = "select vst.id, fcy.id, vst.phase, nxt_fcy.id, vst.ata, vst.atd, vv.eta,  vv.etd, dtl.IB_VYG, dtl.OB_VYG, svc.ID from argo_carrier_visit vst left outer join argo_facility fcy on vst.FCY_GKEY = fcy.gkey left outer join argo_facility nxt_fcy on vst.NEXT_FCY_GKEY = nxt_fcy.gkey join argo_visit_details vv on vst.CVCVD_GKEY = vv.GKEY join ref_carrier_service svc on vv.SERVICE = svc.GKEY left outer join vsl_vessel_visit_details dtl on vv.GKEY = dtl.VVD_GKEY where vst.carrier_mode = 'VESSEL' and vst.phase = '60DEPARTED' and vst.id = ?";
	private static final String GET_CONTAINERS_FOR_VESSEL= "select  unit.id,fecility.id from ARGO_CARRIER_VISIT fecility, INV_UNIT_FCY_VISIT ufv,INV_UNIT unit where fecility.gkey = ufv.ACTUAL_IB_CV and fecility.id =? and unit.gkey = ufv.unit_gkey order by unit.id";
	private static final String GET_NEXT_OB_BARGE_TO_HON1 = "select T1.ID||OB_VYG from VSL_VESSELS T1,VSL_VESSEL_VISIT_DETAILS T2 where t1.id = ? and t1.gkey = t2.vessel_gkey and t2.ib_vyg = ?";
	private static final String GET_NEXT_OB_BARGE_TO_HON = "SELECT t1.ID||ob_vyg  FROM tosmgr.vsl_vessels t1, tosmgr.vsl_vessel_visit_details t2,tosmgr.argo_visit_details argvis,tosmgr.argo_carrier_visit argcarr,tosmgr.argo_facility argfac WHERE t1.ID = ? AND t1.gkey = t2.vessel_gkey AND t2.ib_vyg = ? AND argvis.gkey = t2.vvd_gkey AND argcarr.cvcvd_gkey = argvis.gkey AND argfac.gkey = argcarr.fcy_gkey AND UPPER (argfac.NAME) = UPPER ('honolulu')";
	private static final String GET_VESSEL_SERVICE = "SELECT  c.ID,b.Id FROM  tosmgr.ARGO_VISIT_DETAILS a JOIN tosmgr.Ref_Carrier_Service b ON      a.service = b.gkey JOIN tosmgr.ARGO_CARRIER_VISIT c ON c.cvcvd_gkey = a.gkey WHERE   c.id = ? ";
	//private static final String GET_BEGIN_RECEIVE = "SELECT T2.BEGIN_RECEIVE FROM VSL_VESSELS T1,VSL_VESSEL_VISIT_DETAILS T2 WHERE T1.ID = ? AND T1.GKEY = T2.VESSEL_GKEY AND T2.IB_VYG = ?";
    private static final String GET_BEGIN_RECEIVE = "SELECT T2.BEGIN_RECEIVE FROM VSL_VESSELS T1,VSL_VESSEL_VISIT_DETAILS T2, ARGO_CARRIER_VISIT T3, ARGO_FACILITY T4  WHERE T1.ID = ? AND T1.GKEY = T2.VESSEL_GKEY AND T2.IB_VYG = ? AND T3.CVCVD_GKEY=T2.VVD_GKEY AND T3.FCY_GKEY=T4.GKEY AND T4.ID = ?";
//	private static final String GET_BEGIN_RECEIVE = "SELECT T2.BEGIN_RECEIVE FROM VSL_VESSELS T1,VSL_VESSEL_VISIT_DETAILS T2, ARGO_CARRIER_VISIT T3, ARGO_FACILITY T4  WHERE T1.ID = ? AND T1.GKEY = T2.VESSEL_GKEY AND T2.IB_VYG = ? AND T3.CVCVD_GKEY=T2.VVD_GKEY AND T3.FCY_GKEY=T4.GKEY AND (T4.ID = 'ANK' OR T4.ID = 'KDK' OR T4.ID = 'DUT') AND T3.PHASE = '40WORKING' AND T2.BEGIN_RECEIVE IS NOT NULL";
	private static final String GET_PHASE_FACILITY = "SELECT T4.ID, T3.PHASE FROM ARGO_CARRIER_VISIT T3, ARGO_FACILITY T4 ,VSL_VESSEL_VISIT_DETAILS T2 " +
		"WHERE T3.ID = ? AND T3.FCY_GKEY=T4.GKEY AND T3.CVCVD_GKEY=T2.VVD_GKEY order by T3.PHASE desc";
	private static final String GET_CONSIGNEE_NOTES = "SELECT rbs.name consignee,rbs.notes FROM tosmgr.ref_bizunit_scoped rbs where rbs.name = ? AND     rbs.gkey NOT IN (SELECT rar.bzu_gkey FROM Ref_Agent_Representation rar) and rbs.life_cycle_state = 'ACT' and rbs.role = 'SHIPPER' and rownum=1";
	private static final String GET_SIT = "select ID,DRAY_STATUS from tosmgr.INV_UNIT where ID like ? and visit_state='1ACTIVE' order by time_state_change desc";
	private static final String GET_WO_SI_GRP_DSTATUS = "SELECT U.ID, U.DRAY_STATUS, G.ID AS GRP FROM TOSMGR.INV_UNIT U, TOSMGR.REF_GROUPS G WHERE U.ID LIKE ? AND U.VISIT_STATE='1ACTIVE' AND U.GROUP_GKEY = G.GKEY ORDER BY TIME_STATE_CHANGE DESC";
	private static final String GET_SIT_INFO = "select T1.ID, T1.DRAY_STATUS,T3.ID from INV_UNIT T1, INV_GOODS T2 , REF_COMMODITY T3 where T1.ID like ? AND T1.VISIT_STATE = '1ACTIVE' and T1.GOODS = T2.GKEY AND T2.COMMODITY_GKEY = T3.GKEY";
	private static final String GET_CARRIER_ID_FOR_UNIT = "select T1.ID,T2.ID from TOSMGR.INV_UNIT T1,TOSMGR.ARGO_CARRIER_VISIT T2 WHERE T1.ID LIKE ? AND T1.VISIT_STATE = '1ACTIVE' AND T1.CV_GKEY = T2.GKEY";
	private static final String GET_ACTIVE_CATEGORY = "SELECT T1.ID, T1.CATEGORY FROM TOSMGR.INV_UNIT T1 WHERE T1.ID LIKE ? AND T1.VISIT_STATE = '1ACTIVE'";
	private static final String GET_RESTOW_MARKED_UNITS_FOR_VV = "SELECT UNIT.ID FROM ARGO_CARRIER_VISIT CV, INV_UNIT_FCY_VISIT UFV,INV_UNIT UNIT WHERE CV.ID =? AND CV.GKEY = UFV.ACTUAL_IB_CV AND UNIT.GKEY = UFV.UNIT_GKEY AND UFV.RESTOW_TYP = 'RESTOW' AND UNIT.VISIT_STATE='1ACTIVE' ORDER BY UNIT.ID";
	private static final String GET_DEPT_CARR_ID_FOR_UNIT = "select * from (select T1.ID unitid,T2.ID carrierid  from TOSMGR.INV_UNIT T1,TOSMGR.ARGO_CARRIER_VISIT T2 WHERE T1.ID LIKE ? AND T1.VISIT_STATE = '3DEPARTED' AND T1.CV_GKEY = T2.GKEY ORDER BY CREATE_TIME DESC) where rownum = 1";
	private static final String GET_POL_FOR_DPET_UNIT = "select * from (select a.id unit_id,b.id pol from tosmgr.inv_unit a join tosmgr.REF_ROUTING_POINT b on a.pol_gkey = b.gkey where a.id like ? and a.VISIT_STATE = '3DEPARTED' order by a.create_time desc) where rownum = 1";
	private static final String IS_LATEST_DEPARTED = "select * from (select T2.ID,T1.TRANSIT_STATE from tosmgr.INV_UNIT_FCY_VISIT T1, tosmgr.inv_unit T2 , TOSMGR.ARGO_FACILITY T3 where T1.unit_gkey = T2.gkey and t2.id like ?  and T1. fcy_gkey = t3.gkey and t3.id = 'HON' order by t1.create_time desc) where rownum = 1";
	//private static final String GET_TSTATE_CARR_MODE = "SELECT * FROM ( SELECT UNIT.ID, CV.CARRIER_MODE, UFV.TRANSIT_STATE, UFV.TIME_MOVE, UNIT.GKEY, UFV.UNIT_GKEY FROM ARGO_CARRIER_VISIT CV, INV_UNIT_FCY_VISIT UFV,INV_UNIT UNIT, ARGO_FACILITY FAC WHERE UNIT.ID LIKE ? AND UNIT.CV_GKEY = CV.GKEY AND UNIT.GKEY = UFV.UNIT_GKEY AND FAC.ID = 'HON' AND UFV.FCY_GKEY = FAC.GKEY ORDER BY UFV.CREATE_TIME DESC ) WHERE ROWNUM=1";
	private static final String GET_UNIT_REMARK = "SELECT  * FROM    ( SELECT ID, remark, visit_state, CREATE_TIME FROM tosmgr.inv_unit WHERE ID LIKE ?  ORDER BY create_time DESC) WHERE ROWNUM = 1";
	private static final String GET_TRUCKING_CO = "SELECT * FROM (SELECT UNIT.TRUCKING_COMPANY, BIZUNIT.ID, BIZUNIT.NAME FROM TOSMGR.INV_UNIT UNIT  LEFT OUTER JOIN  TOSMGR.REF_BIZUNIT_SCOPED BIZUNIT  ON UNIT.TRUCKING_COMPANY=BIZUNIT.GKEY  WHERE UNIT.ID LIKE ? ORDER BY UNIT.CREATE_TIME DESC) WHERE ROWNUM=1";
	//private static final String GET_TSTATE_CARR_MODE = "SELECT * FROM (SELECT UNIT.ID, CV.CARRIER_MODE, UFV.TRANSIT_STATE, UFV.TIME_MOVE, UNIT.GKEY, UFV.UNIT_GKEY, GRP.ID GRP FROM ARGO_CARRIER_VISIT CV, INV_UNIT_FCY_VISIT UFV, INV_UNIT UNIT, ARGO_FACILITY FAC, REF_GROUPS GRP WHERE UNIT.ID LIKE ? AND UNIT.CV_GKEY = CV.GKEY AND UNIT.GKEY = UFV.UNIT_GKEY AND FAC.ID = 'HON' AND UFV.FCY_GKEY = FAC.GKEY AND UNIT.GROUP_GKEY = GRP.GKEY (+) ORDER BY UFV.CREATE_TIME DESC) WHERE ROWNUM = 1";
	private static final String GET_UNIT_ACTIVE_FCY = "select unit.id, unit.category, facility.id, unit.visit_state from tosmgr.inv_unit unit " +
			"inner join tosmgr.inv_unit_fcy_visit ufv on unit.active_ufv = ufv.gkey " +
			"inner join argo_facility facility on facility.gkey = ufv.fcy_gkey " +
			"inner join argo_carrier_visit visit on visit.gkey = ufv.actual_ib_cv " +
			"where unit.id = ? and visit.id = ? and UNIT.VISIT_STATE = '1ACTIVE'";

	private static final String GET_UNIT_ACTIVE_FCY_ALL_CATEGORY = "select unit.id, unit.category, facility.id, unit.visit_state from tosmgr.inv_unit unit " +
			"inner join tosmgr.inv_unit_fcy_visit ufv on unit.active_ufv = ufv.gkey " +
			"inner join argo_facility facility on facility.gkey = ufv.fcy_gkey " +
			"inner join argo_carrier_visit visit on visit.gkey = ufv.actual_ib_cv " +
			"where unit.id = ? and UNIT.VISIT_STATE = '1ACTIVE'";

	private static final String GET_UNIT_DETAILS =
			"SELECT  B.*,SUBSTR(SYS_CONNECT_BY_PATH(FLAG,','),2) HOLDS"
			+" FROM    ("
			+" SELECT  A.*,SFT.ID FLAG, ROW_NUMBER() OVER(ORDER BY SFT.ID) RNO"
			+" FROM    ("
			+"        SELECT  UNIT.ID,CV.CARRIER_MODE,UFV.TRANSIT_STATE,UFV.TIME_MOVE,UNIT.GKEY,UFV.UNIT_GKEY,GRP.ID GRP,"
			+"                RANK () OVER (PARTITION BY UNIT.ID ORDER BY UFV.CREATE_TIME DESC) RA,GOODS.DESTINATION,CON.NAME CONSIGNEE,GOODS.BL_NBR,"
			+"               UNIT.FREIGHT_KIND,UNIT.REMARK,UNIT.CATEGORY,CON.ID CONSIGNEE_ID,CV.ID OB_DECLARED,CVF.ID OB_ACTUAL,UFV.VISIT_STATE,UNIT.SEAL_NBR1,COMM.ID COMM_ID,COMM.DESCRIPTION COMM_DESC,EQTYPE.ID EQUIP_TYPE,ROUTING.ID POL,PODROUTING.ID POD1,REFEQ.STRENGTH_CODE,SHPR.NAME SHIPPER,SHPR.ID SHIPPER_ID,"
			+"               CVF1. ID IB_ACTUAL,CVF2. ID IB_DECLRD, UNIT.DRAY_STATUS, UNIT.TRUCKING_COMPANY TRUCK, UNIT.FLEX_STRING11 MISC2, LINE.ID LINEOP,"
			+"               UFV.LAST_POS_LOCTYPE, UFV.LAST_POS_SLOT, UNIT.REQUIRES_POWER, GOODS.TEMP_REQD_C, UNIT.FLEX_STRING01 CNEEPO, UNIT.FLEX_STRING03 CSRID, UNIT.FLEX_STRING12 MILTCN,"
			+"               UNIT.GOODS_AND_CTR_WT_KG CWEIGHT_KG, UFV.LAST_FREE_DAY, UFV.FLEX_DATE02 AVAIL_DATE, UFV.FLEX_DATE03 DUE_DATE,"
			+"               SPECIAL_STOW.ID STOW_RESTRICTION_CODE, UFV.LAST_POS_LOCID, VSLC.BASIC_CLASS VES_CLASS_TYPE"
			+"        FROM    TOSMGR.INV_UNIT UNIT JOIN TOSMGR.ARGO_CARRIER_VISIT CV " 
			+"        ON      UNIT.CV_GKEY = CV.GKEY"
			+"        JOIN    TOSMGR.INV_UNIT_FCY_VISIT UFV"
			+"        ON      UNIT.GKEY = UFV.UNIT_GKEY"
			+"        JOIN    TOSMGR.ARGO_FACILITY  FAC"
			+"        ON      UFV.FCY_GKEY = FAC.GKEY"
			+"        JOIN    TOSMGR.ARGO_CARRIER_VISIT CVF"
			+"        ON      UFV.ACTUAL_OB_CV = CVF.GKEY"
			+"           JOIN ARGO_CARRIER_VISIT CVF1 ON UFV.ACTUAL_IB_CV = CVF1.GKEY"
			+"           JOIN ARGO_CARRIER_VISIT CVF2 ON UNIT.DECLRD_IB_CV = CVF2.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_GROUPS GRP"
			+"        ON UNIT.GROUP_GKEY = GRP.GKEY"
			+"        JOIN TOSMGR.INV_GOODS GOODS"
			+"        ON UNIT.GOODS = GOODS.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_COMMODITY COMM"
			+"        ON GOODS.COMMODITY_GKEY = COMM.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_BIZUNIT_SCOPED CON"
			+"        ON GOODS.CONSIGNEE_BZU = CON.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_BIZUNIT_SCOPED SHPR"
			+"        ON GOODS.SHIPPER_BZU = SHPR.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_BIZUNIT_SCOPED LINE"
			+"        ON UNIT.LINE_OP = LINE.GKEY"
			+"        JOIN TOSMGR.INV_UNIT_EQUIP EQ"
			+"        ON  UNIT.PRIMARY_UE = EQ.GKEY"
			+"        JOIN TOSMGR.REF_EQUIPMENT REFEQ"
			+"        ON  EQ.EQ_GKEY = REFEQ.GKEY"
			+"        JOIN TOSMGR.REF_EQUIP_TYPE EQTYPE"
			+"        ON  REFEQ.EQTYP_GKEY = EQTYPE.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_ROUTING_POINT ROUTING"
			+"        ON UNIT.POL_GKEY = ROUTING.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_ROUTING_POINT PODROUTING"
			+"        ON UNIT.POD1_GKEY = PODROUTING.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.REF_SPECIAL_STOWS SPECIAL_STOW"
			+"        ON UNIT.SPECIAL_STOW_GKEY = SPECIAL_STOW.GKEY"
			+"        LEFT OUTER JOIN TOSMGR.VSL_VESSELS VSL"
			+"        ON VSL.ID = SUBSTR(UFV.LAST_POS_LOCID, 1, 3) AND VSL.REFERENCE_SET='363'"
			+"        LEFT OUTER JOIN TOSMGR.VSL_VESSEL_CLASSES VSLC"
			+"        ON VSLC.GKEY = VSL.VESCLASS_GKEY"
			+"        WHERE   UNIT.ID = ?"
		//	+"        AND     FAC.ID = 'HON'"
		//	+"        AND     FAC.ID = 'ANK'"
			+"        ) A"
			+" LEFT OUTER JOIN   TOSMGR.SRV_FLAGS SF"
			+" ON     A.GKEY = SF.APPLIED_TO_GKEY"
			+" AND    SF.gkey NOT IN (SELECT sv.blocked_flag_gkey FROM srv_vetos SV WHERE sv.applied_to_gkey = A.GKEY)"
			+" LEFT OUTER JOIN   TOSMGR.SRV_FLAG_TYPES SFT"
			+" ON     SF.FLAG_TYPE_GKEY = SFT.GKEY"
			+" AND    SFT.PURPOSE = 'HOLD'"
			+" WHERE   RA = 1) B"
			+" WHERE   CONNECT_BY_ISLEAF = 1"
			+" START WITH RNO = 1"
			+" CONNECT BY ID = PRIOR ID"
			+" AND     RNO = PRIOR RNO+1";
	private static final String GET_UNIT_CARRIER_DETAILS = 
			"SELECT"
			+" A.*"
			+" FROM"
			+" ("
			+" 	SELECT"
			+" 		UNIT. ID,UNIT.CATEGORY,"
			+" 		UFV.TRANSIT_STATE,"
			+" 		CVF. ID OB_ACTUAL,"
			+" 		CVF.CARRIER_MODE OB_ACTUAL_MODE,"
			+" 		CVF3. ID OB_DECLRD,"
			+" 		CVF3.CARRIER_MODE OB_DECLRD_MODE,"
			+" 		CVF1. ID IB_ACTUAL,"
			+" 		CVF1.CARRIER_MODE IB_ACTUAL_MODE,"
			+" 		CVF2. ID IB_DECLRD,"
			+" 		CVF2.CARRIER_MODE IB_DECLRD_MODE,"
			+" 		RANK () OVER (PARTITION BY UNIT.ID ORDER BY UFV.CREATE_TIME DESC) RA,"
			+" 		UFV.CREATE_TIME"
			+" 	FROM"
			+" 		INV_UNIT UNIT"
			+" 		JOIN INV_UNIT_FCY_VISIT UFV ON UNIT.GKEY = UFV.UNIT_GKEY"
			+" 		JOIN ARGO_FACILITY FAC ON UFV.FCY_GKEY = FAC.GKEY"
			+" 		JOIN ARGO_CARRIER_VISIT CVF ON UFV.ACTUAL_OB_CV = CVF.GKEY"
			+" 		JOIN ARGO_CARRIER_VISIT CVF1 ON UFV.ACTUAL_IB_CV = CVF1.GKEY"
			+" 		JOIN ARGO_CARRIER_VISIT CVF2 ON UNIT.DECLRD_IB_CV = CVF2.GKEY"
			+" 		JOIN ARGO_CARRIER_VISIT CVF3 ON UNIT.CV_GKEY = CVF3.GKEY"
			+" 	WHERE"
			+" 		UNIT. ID = ?"
//			+" 		AND FAC. ID = 'HON'"
			+" ) A"
			+" WHERE"
			+" RA = 1";

	private static final String UNIT_DETAILS_FOR_FACILITY =
			"SELECT  B.*,SUBSTR(SYS_CONNECT_BY_PATH(FLAG,','),2) HOLDS" +
					"    FROM    (" +
					"        SELECT " +
					"             A.*, SFT.ID FLAG, ROW_NUMBER() OVER(ORDER BY SFT.ID) RNO " +
					"             FROM " +
					"             ( " +
					"             SELECT " +
					"             UNIT.ID,UNIT.CATEGORY, " +
					"             UFV.TRANSIT_STATE, " +
					"             CVF. ID OB_ACTUAL, " +
					"             CVF.CARRIER_MODE OB_ACTUAL_MODE, " +
					"             CVF3. ID OB_DECLRD, " +
					"             CVF3.CARRIER_MODE OB_DECLRD_MODE, " +
					"             CVF1. ID IB_ACTUAL, " +
					"             CVF1.CARRIER_MODE IB_ACTUAL_MODE, " +
					"             CVF2. ID IB_DECLRD, " +
					"             CVF2.CARRIER_MODE IB_DECLRD_MODE, " +
					"             RANK () OVER (PARTITION BY UNIT.ID ORDER BY UFV.CREATE_TIME DESC) RA, " +
					"             UFV.CREATE_TIME, " +
					"             FCY.ID FCYID, " +
					"             EQORDER.NBR BOOKING,UNIT.GKEY " +
					"             FROM " +
					"             INV_UNIT UNIT " +
					"             JOIN INV_UNIT_FCY_VISIT UFV ON UNIT.GKEY = UFV.UNIT_GKEY " +
					"             JOIN INV_UNIT_EQUIP UE ON UNIT.GKEY = UE.UNIT_GKEY " +
					"             JOIN INV_EQ_BASE_ORDER_ITEM ITEM ON UE.DEPART_ORDER_ITEM_GKEY = ITEM.GKEY " +
					"             JOIN INV_EQ_BASE_ORDER EQORDER ON ITEM.EQO_GKEY = EQORDER.GKEY " +
					"             JOIN ARGO_FACILITY FAC ON UFV.FCY_GKEY = FAC.GKEY " +
					"             JOIN ARGO_CARRIER_VISIT CVF ON UFV.ACTUAL_OB_CV = CVF.GKEY " +
					"             JOIN ARGO_CARRIER_VISIT CVF1 ON UFV.ACTUAL_IB_CV = CVF1.GKEY " +
					"             JOIN ARGO_CARRIER_VISIT CVF2 ON UNIT.DECLRD_IB_CV = CVF2.GKEY " +
					"             JOIN ARGO_CARRIER_VISIT CVF3 ON UNIT.CV_GKEY = CVF3.GKEY " +
					"             JOIN ARGO_FACILITY FCY ON UFV.FCY_GKEY = FCY.GKEY " +
					"             WHERE " +
					"             UNIT. ID LIKE ? AND  " +
					"             EQORDER.NBR = ? AND  " +
					"             UFV.VISIT_STATE = '1ACTIVE' AND  " +
					"             (UFV.TRANSIT_STATE = 'S40_YARD' OR UFV.TRANSIT_STATE = 'S20_INBOUND') " +
					"             ) A             " +
					"     LEFT OUTER JOIN   TOSMGR.SRV_FLAGS SF" +
					"     ON     A.GKEY = SF.APPLIED_TO_GKEY" +
					"     AND    SF.gkey NOT IN (SELECT sv.blocked_flag_gkey FROM srv_vetos SV WHERE sv.applied_to_gkey = A.GKEY)" +
					"     LEFT OUTER JOIN   TOSMGR.SRV_FLAG_TYPES SFT" +
					"     ON     SF.FLAG_TYPE_GKEY = SFT.GKEY" +
					"     AND    SFT.PURPOSE = 'HOLD'" +
					"     WHERE   RA = 1) B" +
					" WHERE   CONNECT_BY_ISLEAF = 1" +
					" START WITH RNO = 1" +
					" CONNECT BY ID = PRIOR ID" +
					" AND     RNO = PRIOR RNO+1";

	private static final String GET_SIT_BKG = 
			"SELECT  A.NBR BOOKING_NUMBER, D.ID COMMODITY,D.DESCRIPTION"
			+ " FROM    INV_EQ_BASE_ORDER A JOIN INV_EQ_BASE_ORDER_ITEM B"
			+ "        ON B.EQO_GKEY = A.GKEY"
			+ "        JOIN ORD_EQUIPMENT_ORDER_ITEMS C"
			+ "        ON B.GKEY = C.GKEY"
			+ "        LEFT OUTER JOIN REF_COMMODITY D"
			+ "        ON C.COMMODITY_GKEY = D.GKEY"
			+ " WHERE NBR = ?";

	private static final String VALIDATE_TRUCKER = "select distinct a.id"
			+" from tosmgr.ref_bizunit_scoped a join tosmgr.ROAD_TRUCKING_COMPANY_LINES b"
			+" on a.gkey = b.trkco_gkey"
			+" and a.role = 'HAULIER'"
			+" join tosmgr.ref_bizunit_scoped c"
			+" on c.gkey = b.line_gkey"
			+" join tosmgr.ROAD_TRUCKING_COMPANIES d"
			+" on a.gkey = d.trkc_id"
			+" where b.status in ('OK','RCVONLY') "
			+" and  c.id = 'MAT'"
			+" AND a.life_cycle_state = 'ACT'"
			+" and a.id = ?";
	
	private static final String VALID_TRUCKER_LIST = "select distinct a.id"
			+" from tosmgr.ref_bizunit_scoped a join tosmgr.ROAD_TRUCKING_COMPANY_LINES b"
			+" on a.gkey = b.trkco_gkey"
			+" and a.role = 'HAULIER'"
			+" join tosmgr.ref_bizunit_scoped c"
			+" on c.gkey = b.line_gkey"
			+" join tosmgr.ROAD_TRUCKING_COMPANIES d"
			+" on a.gkey = d.trkc_id"
			+" where b.status in ('OK','RCVONLY') "
			+" and  c.id = 'MAT'"
			+" AND a.life_cycle_state = 'ACT'"
			+" and a.id IN (%s)";
	private static final String GET_CONTAINERS_STOWED_RORO_FOR_VV = "SELECT B.*, SUBSTR(SYS_CONNECT_BY_PATH(FLAG,','),2) HOLDS FROM ("
			+" SELECT A.*, SFT.ID FLAG, ROW_NUMBER() OVER(PARTITION BY A.ID ORDER BY SFT.ID) AS SEQ FROM ( "
			+" SELECT  UNIT.GKEY, UNIT.ID,EQTYPE.ID EQUIP_TYPE, ROUND(UNIT.GOODS_AND_CTR_WT_KG*2.20462262185) CWEIGHT_KG, UFV.ARRIVE_POS_SLOT, CON.NAME CONSIGNEE, GOODS.DESTINATION"
			+" FROM ARGO_CARRIER_VISIT CV, INV_UNIT_FCY_VISIT UFV, INV_UNIT UNIT, INV_GOODS GOODS, REF_BIZUNIT_SCOPED CON, INV_UNIT_EQUIP EQ, REF_EQUIPMENT REFEQ, REF_EQUIP_TYPE EQTYPE"
			+" WHERE CV.GKEY = UFV.ACTUAL_IB_CV AND CV.ID =? AND UNIT.GKEY = UFV.UNIT_GKEY AND UNIT.GOODS = GOODS.GKEY AND GOODS.CONSIGNEE_BZU = CON.GKEY"
			+"      AND UNIT.PRIMARY_UE = EQ.GKEY AND EQ.EQ_GKEY = REFEQ.GKEY AND EQTYP_GKEY = EQTYPE.GKEY "
			+"      AND UFV.FLEX_STRING06 = 'RO' AND UNIT.VISIT_STATE = '1ACTIVE'"
			+" ORDER BY UNIT.ID ) A"
			+" LEFT OUTER JOIN   TOSMGR.SRV_FLAGS SF"
			+" ON     A.GKEY = SF.APPLIED_TO_GKEY"
			+" AND    SF.GKEY NOT IN (SELECT SV.BLOCKED_FLAG_GKEY FROM SRV_VETOS SV WHERE SV.APPLIED_TO_GKEY = A.GKEY)"
			+" LEFT OUTER JOIN   TOSMGR.SRV_FLAG_TYPES SFT"
			+" ON     SF.FLAG_TYPE_GKEY = SFT.GKEY"
			+" AND    SFT.PURPOSE = 'HOLD' ) B"
			+" WHERE  CONNECT_BY_ISLEAF = 1"
			+" CONNECT BY SEQ = PRIOR SEQ +1 AND ID = PRIOR ID"
			+" START WITH SEQ = 1";
	private static final String SEARCH_BKG_NBRS = "SELECT NBR FROM INV_EQ_BASE_ORDER WHERE NBR IN (%s)";
	private static final String SEARCH_BOOKING_DTLS_FOR_HAZ = "SELECT ARGO_CARRIER_VISIT.ID AS CARRIER_VISIT_ID ," +
			"      REF_BIZUNIT_SCOPED.ID AS LINE_OP," +
			"      INV_EQ_BASE_ORDER.EQ_STATUS, " +
			"      INV_EQ_BASE_ORDER.PREVENT_TYPE_SUBST "+
			" FROM 	INV_EQ_BASE_ORDER, " +
			" 		ARGO_CARRIER_VISIT, " +
			" 		REF_BIZUNIT_SCOPED" +
			" WHERE INV_EQ_BASE_ORDER.NBR = \'?\'" +
			" AND ARGO_CARRIER_VISIT.GKEY = INV_EQ_BASE_ORDER.VESSEL_VISIT_GKEY " +
			" AND REF_BIZUNIT_SCOPED.ROLE = \'LINEOP\' " +
			" AND REF_BIZUNIT_SCOPED.GKEY = INV_EQ_BASE_ORDER.LINE_GKEY;";

	private static final String BARGE_VISIT_BY_ID = "SELECT vst.ID VST_ID, fcy.ID CURRENT_FACILITY,  vst.PHASE,  nxt_fcy.ID NEXT_FACILITY,  dtl.IB_VYG,  dtl.ob_vyg,  svc.ID SVC_ID,  dtl.CLASSIFICATION" +
			" FROM argo_carrier_visit vst " +
			" LEFT OUTER JOIN argo_facility fcy " +
			" ON vst.FCY_GKEY = fcy.GKEY " +
			" LEFT OUTER JOIN argo_facility nxt_fcy " +
			" ON vst.NEXT_FCY_GKEY = nxt_fcy.GKEY " +
			" INNER JOIN argo_visit_details vv " +
			" ON vst.CVCVD_GKEY = vv.GKEY " +
			" INNER JOIN ref_carrier_service svc " +
			" ON vv.SERVICE = svc.GKEY " +
			" LEFT OUTER JOIN vsl_vessel_visit_details dtl " +
			" ON vv.GKEY           = dtl.VVD_GKEY " +
			" WHERE vst.ID         = ? " +
			" AND vst.CARRIER_MODE = \'VESSEL\'";

	private static final String BARGE_VISIT_BY_VSL_N_NXT_FCY = "SELECT vst.ID VST_ID,   fcy.ID CURRENT_FACILITY,   vst.PHASE,   nxt_fcy.ID NEXT_FACILITY,   dtl.IB_VYG,   dtl.OB_VYG,   svc.ID SVC_ID,   dtl.classification," +
			"   dtl.vessel_gkey,   VSL.ID vsl_id" +
			" FROM argo_carrier_visit vst " +
			" LEFT OUTER JOIN argo_facility fcy ON vst.FCY_GKEY = fcy.GKEY " +
			" LEFT OUTER JOIN argo_facility nxt_fcy ON vst.NEXT_FCY_GKEY = nxt_fcy.GKEY " +
			" INNER JOIN argo_visit_details vv ON vst.CVCVD_GKEY = vv.GKEY " +
			" INNER JOIN ref_carrier_service svc ON vv.SERVICE = svc.GKEY " +
			" left outer join vsl_vessel_visit_details dtl on vv.gkey = dtl.vvd_gkey " +
			" inner join vsl_vessels vsl  on vsl.gkey = dtl.vessel_gkey " +
			" WHERE vsl.id = ? " +
			" and nxt_fcy.ID = ? " +
			" AND dtl.IB_VYG       = ? " +
			" and vst.carrier_mode = \'VESSEL\'";

	private static String FILTERED_BARGES = "SELECT vst.ID VST_ID," +
			"  fcy.ID CURRENT_FACILITY," +
			"  vst.PHASE," +
			"  nxt_fcy.ID NEXT_FACILITY," +
			"  dtl.IB_VYG," +
			"  dtl.OB_VYG," +
			"  svc.ID SVC_ID," +
			"  dtl.CLASSIFICATION," +
			"  vv.ETA," +
			"  vv.ETD," +
			" VSL.ID CARRIER_ID" +
			" FROM argo_carrier_visit vst" +
			" LEFT OUTER JOIN argo_facility fcy" +
			" ON vst.FCY_GKEY = fcy.GKEY" +
			" LEFT OUTER JOIN argo_facility nxt_fcy" +
			" ON vst.NEXT_FCY_GKEY = nxt_fcy.GKEY" +
			" INNER JOIN argo_visit_details vv" +
			" ON vst.CVCVD_GKEY = vv.GKEY" +
			" INNER JOIN ref_carrier_service svc" +
			" ON vv.SERVICE = svc.GKEY" +
			" left outer join VSL_VESSEL_VISIT_DETAILS DTL" +
			" on VV.GKEY           = DTL.VVD_GKEY " +
			" inner join vsl_vessels vsl  on vsl.gkey = dtl.vessel_gkey," +
			" (SELECT vst.ID VST_ID," +
			"  fcy.ID CURRENT_FACILITY," +
			"  vst.PHASE," +
			"  nxt_fcy.ID NEXT_FACILITY," +
			"  dtl.IB_VYG," +
			"  dtl.OB_VYG," +
			"  svc.ID SVC_ID," +
			"  dtl.CLASSIFICATION," +
			"  vv.ETA," +
			"  vv.ETD" +
			" FROM argo_carrier_visit vst" +
			" LEFT OUTER JOIN argo_facility fcy" +
			" ON vst.FCY_GKEY = fcy.GKEY" +
			" LEFT OUTER JOIN argo_facility nxt_fcy" +
			" ON vst.NEXT_FCY_GKEY = nxt_fcy.GKEY" +
			" INNER JOIN argo_visit_details vv" +
			" ON vst.CVCVD_GKEY = vv.GKEY" +
			" INNER JOIN ref_carrier_service svc" +
			" ON vv.SERVICE = svc.GKEY" +
			" LEFT OUTER JOIN vsl_vessel_visit_details dtl" +
			" on VV.GKEY           = DTL.VVD_GKEY" +
			" where VST.id         = ?" +
			" AND fcy.ID           = ?" +
			" and VST.CARRIER_MODE = \'VESSEL\'" +
			" AND vst.PHASE != \'80CANCELED\') SUB1" +
			" where " +
			" fcy.ID           = ?" +
			" and NXT_FCY.id           = ?"+
			" and VST.CARRIER_MODE = \'VESSEL\'" +
			" and VST.PHASE != \'80CANCELED\'" +
			" and VV.ETA <= SUB1.ETD" +
			" and VV.ETA >=SUB1.ETA" +
			" and DTL.CLASSIFICATION = \'BARGE'" +
			" ORDER BY VV.ETA DESC";

	private static final String DELETE_HAZARD_BDX = "delete from inv_hazard_items where hzrd_gkey in  " +
			"(select gkey from inv_hazards where owner_gkey = " +
			"(select gkey from inv_eq_base_order where nbr=?))";
	
	private static final String GET_VVD_LINES ="select linedtls.id LINE_ID,lines.line_in_voy_nbr IB_VOY,LINES.LINE_OUT_VOY_NBR OB_VOY from " +
	 "tosmgr.argo_carrier_visit cv,"+
	"tosmgr.ARGO_VISIT_DETAILS avdtls,"+
	  "tosmgr.vsl_vessel_visit_details vslvvddtls,"+
	  "tosmgr.VSL_VESSEL_VISIT_LINES lines,"+
	  "tosmgr.REF_BIZUNIT_SCOPED linedtls,"+
	  "tosmgr.argo_facility facility"+
	  " where cv.id = ?"+
	  " and cv.cvcvd_gkey = avdtls.gkey"+
	  " and avdtls.gkey = vslvvddtls.vvd_gkey"+
	  " and vslvvddtls.vvd_gkey = lines.vvd_gkey"+
	  " and lines.line_gkey = linedtls.gkey"+
	  " and cv.fcy_gkey = facility.gkey"+
	  " and FACILITY.ID = ? "+
	  " order by linedtls.id asc";
	 

	
	
			/*
			 * 
     * Constructor
     * @param conn
     */
    public TosLookup(Connection conn) {
        super(conn);
    }

    /**
     * Constructor
	 *
	 */
	public TosLookup() throws Exception {
    	super();
    	setConnection(DBConnection.getConnection());
    }

    /**
     * Get a list of searches
     * @return
     * @throws SQLException
     */
	
	
	public Map getVVDLines(String vvd, String facility) throws SQLException {
		LOGGER.info("getting lines for vvd " + vvd + " " + facility);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(GET_VVD_LINES);
			pstmt.setString(1, vvd);
			pstmt.setString(2, facility);
			rs = pstmt.executeQuery();
			Map<String, String> stringMap = new HashMap<String, String>();
			if (rs == null) {
				//return Collections.emptyList();
				return null;
			}
			List<Map> mapList = new ArrayList<Map>();
			while (rs.next()) {
				ResultSetMetaData metaData = rs.getMetaData();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					LOGGER.debug(metaData.getColumnName(i) + "\t" + metaData.getColumnTypeName(i));
				}
				stringMap.put(rs.getString("LINE_ID"),rs.getString("IB_VOY")+"-"+rs.getString("OB_VOY"));
				//stringMap.put("CURRENT_FACILITY", rs.getString("IB_VOY"));
				//stringMap.put("OB_VOY", rs.getString("OB_VOY"));
				
				LOGGER.debug("inside getVVDLines "+stringMap);
				//mapList.add(stringMap);
			}
			return stringMap;

		} catch (SQLException e) {
			LOGGER.error("Could not get lines", e);
		} finally {
			close(rs);
			close(pstmt);
		}
		return null;
	}
	
    public List getActiveVV() throws SQLException {
    	 PreparedStatement pstmt = null;
         ResultSet rs = null;
         ArrayList<VesselVisitVO> vvs = new ArrayList<VesselVisitVO>();
      try {
          pstmt = conn.prepareStatement(SEARCH_ACTIVE_VV);

           rs = pstmt.executeQuery();
           while(rs.next()) {
        	   VesselVisitVO vv = new VesselVisitVO();
        	   vv.setId(rs.getString(1));
        	   vv.setPort(rs.getString(2));
        	   vv.setDBPhase(rs.getString(3));
        	   vv.setNextPort(rs.getString(4));
        	   vv.setAta(rs.getTimestamp(5));
        	   vv.setAtd(rs.getTimestamp(6));
        	   vv.setEta(rs.getTimestamp(7));
        	   vv.setEtd(rs.getTimestamp(8));
        	   vv.setIbVyg(rs.getString(9));
        	   vv.setObVyg(rs.getString(10));
        	   vv.setCarrierService(rs.getString(11));
        	   vvs.add(vv);
           }
         } catch (SQLException e) {
             LOGGER.error("Could not get Consignee",e);
         } finally {
             close(rs);
             close(pstmt);
         }
         return vvs;
    }

    /**
     * Get a list of searches
     * @return
     * @throws SQLException
     */
    public String getConsignee(String bookingNumber,String consigneeName) throws SQLException {
    	 PreparedStatement pstmt = null;
         ResultSet rs = null;
      try {
          pstmt = conn.prepareStatement(SEARCH_CONSIGNEE_BY_BN);
	      pstmt.setString(1, bookingNumber);

          String name = null;
          if(bookingNumber != null) {
        	  rs = pstmt.executeQuery();
        	  if(rs.next())  name = rs.getString(1);
          }


          if(name == null ) {
        		pstmt = conn.prepareStatement(SEARCH_BY_NAME);
        		pstmt.setString(1, consigneeName);
        		rs = pstmt.executeQuery();
        		if(rs.next()) {
        			name = rs.getString(1);
        		}
           }
           return name;


         } catch (SQLException e) {
             LOGGER.error("Could not get Consignee",e);
         } finally {
             close(rs);
             close(pstmt);
         }
         return null;
    }

    public String getShipper(String bookingNumber,String consigneeName) throws SQLException {
   	 PreparedStatement pstmt = null;
        ResultSet rs = null;
     try {
         pstmt = conn.prepareStatement(SEARCH_SHIPPER_BY_BN);
	     pstmt.setString(1, bookingNumber);

         String name = null;
         if(bookingNumber != null) {
        	 rs = pstmt.executeQuery();
        	 if(rs.next()) name = rs.getString(1);
         }
       	 if(name == null ) {
       		pstmt = conn.prepareStatement(SEARCH_BY_NAME);
       		pstmt.setString(1, consigneeName);
       		rs = pstmt.executeQuery();
       		if(rs.next()) {
       			name = rs.getString(1);
       		}
       	 }
       	 return name;


        } catch (SQLException e) {
            LOGGER.error("Could not get Shipper",e);
        } finally {
            close(rs);
            close(pstmt);
        }
        return null;
   }

    public boolean checkId(String id) throws SQLException {
      	 PreparedStatement pstmt = null;
           ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(SEARCH_BY_ID);
   	     	pstmt.setString(1, id);

           	rs = pstmt.executeQuery();
           	if(rs.next()) return true;
           	return false;

           } catch (SQLException e) {
               LOGGER.error("Could not check Id",e);
               throw e;
           } finally {
               close(rs);
               close(pstmt);
           }
      }

	public int getShipperId(String name) {
		if(name == null) return 0;
		int code = name.hashCode()%1000000000;
		if(code <0 ) code = -code;
		return code;
	}

	public int incShipperId(int code) {
		return (code+1)%1000000000;
	}

	//A3
	 public String getActiveUnitCategory(String id, String port) throws SQLException {
	   	 LOGGER.debug("Getting Active unit Category for "+id);
		 PreparedStatement pstmt = null;
	     ResultSet rs = null;
	     try {
	         pstmt = conn.prepareStatement(SEARCH_ACTIVE_UNIT_CATEGORY);
		     pstmt.setString(1, id);
		     pstmt.setString(2, port);

	         rs = pstmt.executeQuery();

	         if(rs == null){
	        	 return null;
	         }
	         String category = null;
	         int cnt = 0;
	         while(rs.next()) {
	        	 if(cnt == 0){
	        	   category =rs.getString(1);
	        	   LOGGER.debug("Active unit category "+category+" in N4");
	        	 }
	        	 cnt++;
	         }

	         if(category == null)
	         { 	 LOGGER.debug("Category "+category+" in N4");
	        	 return null;
	         }

	         return category;

	    } catch (SQLException e) {
	            LOGGER.error("Could not get category",e);
	    } finally {
	            close(rs);
	            close(pstmt);
	    }
	    return null;
	}

	 public TransitState getMostActiveTstate(String id, String port) throws SQLException {
	   	 LOGGER.debug("Getting tstat for " + id);
		 PreparedStatement pstmt = null;
	        ResultSet rs = null;
	     try {
	         pstmt = conn.prepareStatement(SEARCH_TSTATE);
		     pstmt.setString(1, id);
//		     pstmt.setString(2, port);

	         rs = pstmt.executeQuery();

	         String tstate = null;

	         while(rs.next()) {
	        	 tstate =rs.getString(1);
	        	 if(!"S10_ADVISED".equals(tstate)) break;
	         }

	         if(tstate == null) return null;
	         return TransitState.getTransitState(tstate);


	    } catch (SQLException e) {
	            LOGGER.error("Could not get tstate",e);
	    } finally {
	            close(rs);
	            close(pstmt);
	    }
	    return null;
	}

	 public CommodityVO getMostActiveCommodity(String id, String port) throws SQLException {
		 LOGGER.debug("Getting tstat for "+id);
	   	 CommodityVO vo = new CommodityVO();
	   	 vo.setId(id);
		 PreparedStatement pstmt = null;
	        ResultSet rs = null;
	     try {
	         pstmt = conn.prepareStatement(SEARCH_TSTATE_COMMODITY);
		     pstmt.setString(1, id);
//		     pstmt.setString(2, port);

	         rs = pstmt.executeQuery();

	         String tstate = null;

	         while(rs.next()) {
	        	 tstate =rs.getString(1);
	        	 vo.setCommodity(rs.getString(2));
	        	 if(!"S10_ADVISED".equals(tstate)) break;
	         }

	         if(tstate == null) return null;
	         LOGGER.debug("ID="+id+" Tstate = "+tstate);
	         vo.setTstate(TransitState.getTransitState(tstate));
	         return vo;

	    } catch (SQLException e) {
	            LOGGER.error("Could not get tstate",e);
	    } finally {
	            close(rs);
	            close(pstmt);
	    }
	    return null;
	}

	 /** Change it to try current name then name minus check digit */
	 public String getCheckDigit(String id) throws TosException {
      	PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
        	if(id == null ) throw new TosException("Invalid Equipment Id");
        	String idStrip = id+'%';

        	pstmt = conn.prepareStatement(SEARCH_EQUIPMENT);
   	     	pstmt.setString(1, idStrip);
   	        rs = pstmt.executeQuery();
   	        String idFixed = null;
   	        if(rs.next()) idFixed =  rs.getString(1);
        	int cnt = 1;
        	while(rs.next()) cnt++;
        	if(cnt > 1) throw new TosException("Found "+cnt+" Equipment matching id "+id);
        	if(idFixed != null) return idFixed;

        	// If no results, try again with stripped name.
        	idStrip = id.substring(0, id.length()-1)+'%';
        	pstmt = conn.prepareStatement(SEARCH_EQUIPMENT);
   	     	pstmt.setString(1, idStrip);

           	rs = pstmt.executeQuery();

           	if(rs.next()) idFixed =  rs.getString(1);
           	cnt = 1;
           	while(rs.next()) cnt++;
           	if(cnt > 1) throw new TosException("Found "+cnt+" Equipment matching id "+id);
           	if(idFixed != null) return idFixed;

           	// Give up an return the original id.
           	return id;

           } catch (SQLException e) {
               LOGGER.error("Could not check Id",e);
               throw new TosException("Could not validate equipment id "+id);
           } finally {
               close(rs);
               close(pstmt);
           }
      }

	 /**
	     * Get a list of searches
	     * @return
	     * @throws SQLException
	     */
	    public String getVesselOperator(String vessel) throws SQLException {
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         String result = null;
	      try {
	           pstmt = conn.prepareStatement(SEARCH_VESSEL_OP);
	           pstmt.setString(1, vessel);

	           rs = pstmt.executeQuery();
	           if(rs.next()) {
	        	   result = rs.getString(1);
	           }
	         } catch (SQLException e) {
	             LOGGER.error("Could not get vessel operator",e);
	         } finally {
	             close(rs);
	             close(pstmt);
	         }
	         return result;
	    }


	    /**
	     * Method to check if N4 has an Existing Booking with a Different line operator
	     * @param lineOperator
	     * @param vesselId
	     * @param bookingNumber
	     * @return boolean
	     * @throws SQLException
	     * 3.3
	     */
	    public boolean hasDiffBkgLineOperator(String lineOperator,String vesselId,String bookingNumber)throws SQLException {
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         String extBkgLineOptGkey = null;
	         String extBkgLineOptName = null;
	      try {
	          pstmt = conn.prepareStatement(SEARCH_BKG);
	          pstmt.setString(1, vesselId);
		      pstmt.setString(2, bookingNumber);


	          if(bookingNumber != null) {
	        	  rs = pstmt.executeQuery();
	        	  if(rs.next()){
	        		  extBkgLineOptGkey = rs.getString(1);
	        	  }
	          }

	          if(extBkgLineOptGkey == null){
	        	  return false;
	          }
	          pstmt = conn.prepareStatement(SEARCH_LINEOP);
	          pstmt.setString(1, extBkgLineOptGkey);
	          if(extBkgLineOptGkey != null){
	        	  rs = pstmt.executeQuery();
	        	  if(rs.next()){
	        	   extBkgLineOptName = rs.getString(1);
	        	  }
	          }
	          //LOGGER.debug("extBkgLineOptName="+extBkgLineOptName+" lineOperator="+lineOperator);
	          if(extBkgLineOptName != null && !extBkgLineOptName.equals(lineOperator)){
	        	  return true;
	          }else {
	        	  return false;
	          }
	         } catch (SQLException e) {
	             LOGGER.error("Could not get LineOperator Booking",e);
	         } finally {
	             close(rs);
	             close(pstmt);
	         }
	         return false;
	    }

	    /**
	     * Get a list of searches
	     * @return
	     * @throws SQLException
	     */
	    public ArrayList<CalendarVO> getCalendar() throws SQLException {
	    	 LOGGER.info("TosLookup.getCalendar begin");
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         ArrayList<CalendarVO> calList = new ArrayList<CalendarVO>();
	      try {
	          pstmt = conn.prepareStatement(GET_CALENDER);
              rs = pstmt.executeQuery();
	          while(rs.next()) {
	        	  CalendarVO calVo = new CalendarVO();
	        	  LOGGER.info("result set :"+rs.getString(1)+" "+rs.getTimestamp(2)+" "+rs.getString(3));
	        	  //System.out.println("result set :"+rs.getString(1)+" "+rs.getTimestamp(2)+" "+rs.getString(3));
	        	  calVo.setNameOfHoliday(rs.getString(1));
	        	  calVo.setHlidayDate(rs.getTimestamp(2));
	        	  calVo.setRepeatInterval(rs.getString(3));
	        	  calList.add(calVo);
	          }
	         } catch (SQLException e) {
	             LOGGER.error("Could not get Calendar",e);
	         } finally {
	             close(rs);
	             close(pstmt);
	         }
	          LOGGER.info("TosLookup.getCalendar end");
	         return calList;
	    }

	    public String getEditFlag(String equipmentId) throws SQLException {
	    	 LOGGER.info("TosLookup.getEditFlag begin");
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         String misc2="";
	         equipmentId = equipmentId + "%";
	    	try {
	    		pstmt = conn.prepareStatement(GET_EDIT_FLAG);
	    		pstmt.setString(1, equipmentId);
	    		pstmt.setMaxRows(1);
	            rs = pstmt.executeQuery();
	            if (rs.next()) {
	            	//System.out.println("result-> GKEY, ID, FLEXT_STRING_11,CREATE_TIME "+rs.getString(1)+ " "+rs.getString(2)+ " "+rs.getString(3)+ " "+rs.getString(4));
	            	misc2 = rs.getString(3);
	            }

	    	}catch(SQLException e) {
	    		LOGGER.error("Could not get getEditFlag",e);
	    	}finally {
	             close(rs);
	             close(pstmt);
	         }
	    	return misc2;
	    }
	    /**
	     * This method takes fully qualified equipmentId (meaning equipment number along with check digit) and
	     * returns holds applied on this equipment in TOS system.
	     * @param equipmentId
	     * @return
	     * @throws SQLException
	     */
	    public String getHolds(String equipmentId) throws SQLException {
	    	 LOGGER.info("TosLookup.getHolds begin");
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         String equipmentHolds="";
	         equipmentId = equipmentId + "%";
	    	try {
	    		pstmt = conn.prepareStatement(GET_HOLDS);
	    		pstmt.setString(1, equipmentId);
	            rs = pstmt.executeQuery();
	            if (rs.next()) {
	            	System.out.println("result-> ID, HOLDS" + rs.getString(1) + " - " + rs.getString(2));
	            	equipmentHolds = rs.getString(2);
	            }

	    	}catch(SQLException e) {
	    		LOGGER.error("Could not get holds",e);
	    	}finally {
	             close(rs);
	             close(pstmt);
	         }
	    	return equipmentHolds;
	    }
	    /**
	     * This method takes fully qualified equipmentId (meaning equipment number along with check digit) and
	     * returns equipment type from TOS system.
	     * @param equipmentId
	     * @return
	     * @throws SQLException
	     */
	    public HashMap<String, String> getEquipmentType(String equipmentId,String nChkDigit) throws SQLException {
	    	
	    	
	    	 LOGGER.info("TosLookup.getEquipmentType begin "+equipmentId);
	    	 LOGGER.info("nChkDigit"+nChkDigit+"end");
	    	 PreparedStatement pstmt = null;
	         ResultSet rs = null;
	         String unitFullId = null;
	         if (nChkDigit==null||nChkDigit.equals(""))
	        	 nChkDigit = "X";
	         unitFullId = equipmentId+nChkDigit;
	         LOGGER.info("unitFullId"+unitFullId);
	         HashMap<String, String> equipTypeDtlMap = new HashMap<String, String>();
	         //equipmentId = equipmentId + "%";
	    	try {
	    		pstmt = conn.prepareStatement(GET_EQUIPMENT_TYPE_BY_IDFULL);
	    		pstmt.setString(1, unitFullId);
	    		pstmt.setString(2, equipmentId);
	            rs = pstmt.executeQuery();
	            if (rs.next()) {
	            	LOGGER.info("result-> ID,TYPE,HGT_MM,STRENGTH,TAREKG,MATERIAL,NOMHEIGHT : "+rs.getString(1)+ " "+rs.getString(2)+" "+rs.getString(3)+ " "+rs.getString(4)+" "+rs.getString(5)+ " "+rs.getString(6)+ " "+rs.getString(7));
	            	equipTypeDtlMap.put("equipmentId",rs.getString(1));
	            	equipTypeDtlMap.put("equipmentType", rs.getString(2));
	            	equipTypeDtlMap.put("equipmentHgtMm",rs.getString(3));
	            	equipTypeDtlMap.put("equipmentStrength",rs.getString(4));
					equipTypeDtlMap.put("equipmentTareKg",rs.getString(5));
	            	equipTypeDtlMap.put("equipmentMaterial", rs.getString(6));
	            	equipTypeDtlMap.put("equipmentNomHeight",rs.getString(7));
	            } 
	    	}catch(SQLException e) {
	    		LOGGER.error("Could not get equipment type",e);
	    	}finally {
	             close(rs);
	             close(pstmt);
	         }
	    	return equipTypeDtlMap;
	    }
	    /**
		*	Get trucker information associated with consignee
		*	@parameter:	consigneeName
		*	@parameter:	consigneeQual
		*	@return:
		*	@throws: SQLException
		*/
		public List getTrucker(String consigneeName) throws SQLException
		{
			LOGGER.info("TosLookup.getTrucker begin "+consigneeName);
			PreparedStatement pstmt = null;
	        ResultSet rs = null;
	        ArrayList truckerList = new ArrayList();  
	        ArrayList trlist = null;
         	try {
	    		pstmt = conn.prepareStatement(GET_TRUCKER);
	    		pstmt.setString(1, consigneeName);
	    		LOGGER.info("pstmt "+pstmt.toString());
	            rs = pstmt.executeQuery();
	            while(rs.next()) {
	            	trlist = new ArrayList(); 
	            	trlist.add(rs.getString(2)+"-"+rs.getString(1));
	            	truckerList.add(trlist);
					LOGGER.info("*** "+rs.getString(1)+"    "+rs.getString(2));
	            } 
	            close(rs);
	            close(pstmt);
	            if (truckerList != null) {
	            	LOGGER.info("truckerList size a "+truckerList.size());
	            }
	            if (truckerList == null || truckerList.size() == 0) {
	            	LOGGER.info("No exact match for " + consigneeName);
					pstmt = conn.prepareStatement(GET_TRUCKER1);
		    		pstmt.setString(1, consigneeName + "%");
		    		rs = pstmt.executeQuery();
		    		while (rs.next()) {
		    			trlist = new ArrayList(); 
		    			trlist.add(rs.getString(2)+"-"+rs.getString(1));
		            	truckerList.add(trlist);
		            	LOGGER.info("*** "+rs.getString(1)+"    "+rs.getString(2));
		            }
		    		if (truckerList != null) {
		            	LOGGER.info("truckerList size 2 "+truckerList.size());
		            }
	            }
	    	}catch(SQLException e) {
	    		LOGGER.error("Could not get trucker information for "+consigneeName,e);
	    	}finally {
				close(rs);
				close(pstmt);
			}
         	return truckerList;
		}
		
		  /**
			*	Check whether the container is a duplicate one
			*	@parameter:	containerNumber
			*	@return:
			*	@throws: SQLException
			*/
				public boolean checkDuplicateContainer(String containerNumber) throws SQLException
				{
					LOGGER.info("TosLookup.checkDuplicateContainer begin");
					PreparedStatement pstmt = null;
					ResultSet rs = null;

					try {
						pstmt = conn.prepareStatement(GET_DUPLICATE_CONTAINER);
			    		pstmt.setString(1, containerNumber);
			    	    rs = pstmt.executeQuery();
			            if(rs.next()) {
			            	return true;
			            }
			           
			    	}catch(SQLException e) {
			    		LOGGER.error("Could not get duplicate container number for " + containerNumber, e);
			    		//System.out.println("Could not get duplicate container number:"+containerNumber);
			    	}finally {
						close(rs);
						close(pstmt);
					}
			    	return false;
				}
				
				  public VesselVisitVO chkForDepartedOutboundVesvoy(String vesvoy) throws SQLException {
				    	 PreparedStatement pstmt = null;
				         ResultSet rs = null;
				         VesselVisitVO vv = null;
				      try {
				          pstmt = conn.prepareStatement(VALIDATE_VESVOY_DEPARTED);
				          pstmt.setString(1, vesvoy);
				           rs = pstmt.executeQuery();
				           while(rs.next()) {
				        	  // System.out.println("chkForDepartedOutboundVesvoy inside result set");
				        	   vv = new VesselVisitVO();
				        	   vv.setId(rs.getString(1));
				        	   vv.setPort(rs.getString(2));
				        	   vv.setDBPhase(rs.getString(3));
				        	   vv.setNextPort(rs.getString(4));
				        	   vv.setAta(rs.getTimestamp(5));
				        	   vv.setAtd(rs.getTimestamp(6));
				        	   vv.setEta(rs.getTimestamp(7));
				        	   vv.setEtd(rs.getTimestamp(8));
				        	   vv.setIbVyg(rs.getString(9));
				        	   vv.setObVyg(rs.getString(10));
				        	   vv.setCarrierService(rs.getString(11));
				           }
				         } catch (SQLException e) {
				             LOGGER.error("Could not get vessel vist",e);
				             e.printStackTrace();
				         } finally {
				             close(rs);
				             close(pstmt);
				         }
				         return vv;
				    }
				  
				  public List getUnitsforVesselVoyage(String vesvoy) {
					  LOGGER.info("TosLookup.getUnitsforVesselVoyage begin");
					  final List<String> unitList = new ArrayList<String>();
					  PreparedStatement pstmt = null;
				      ResultSet rs = null;
				        
					  try {
						  pstmt = conn.prepareStatement(GET_CONTAINERS_FOR_VESSEL);
						  pstmt.setString(1, vesvoy);
				          rs = pstmt.executeQuery();
				           while(rs.next()) {
				        	  // System.out.println("getUnitsforVesselVoyage Container from N4:"+rs.getString(1));
							   unitList.add(rs.getString(1));
						   }
					  }catch(Exception ex) {
						  LOGGER.error("Could not get vessel vist", ex);
					  }
					  finally {
				             close(rs);
				             close(pstmt);
				         }
					  LOGGER.info("TosLookup.getUnitsforVesselVoyage end");
					  return unitList;
				  }
				  
				  public ArrayList<TosRdsDataFinalMt> getUnitsStowedToRoRo(String  vesvoy) {
					  LOGGER.info("TosLookup.getUnitsStowedToRoRo begin - "+vesvoy);
					  PreparedStatement pstmt = null;
					  ResultSet rs = null;
					  ArrayList<TosRdsDataFinalMt> result = null;
					  try {
						  pstmt = conn.prepareStatement(GET_CONTAINERS_STOWED_RORO_FOR_VV);
						  pstmt.setString(1, vesvoy);
						  rs = pstmt.executeQuery();
						  if(rs != null) {
							  result = new ArrayList<TosRdsDataFinalMt>();
							  while(rs.next()) {
								  TosRdsDataFinalMt rds = new TosRdsDataFinalMt();
								  rds.setContainerNumber(rs.getString(2));
								  rds.setTypeCode(rs.getString(3));
								  rds.setHgt(rs.getString(4));
								  rds.setCell(rs.getString(5));
								  rds.setConsignee(rs.getString(6));
								  rds.setDport(rs.getString(7));
								  rds.setCrstatus(rs.getString(10));
								  result.add(rds);
							  }
						  }
					  }catch(Exception ex) {
						  LOGGER.error("Could not get units stowed to RORO for "+vesvoy,ex);
					  }finally {
						  close(rs);
						  close(pstmt);
					  }
					  LOGGER.info("No of containers ="+result.size());
					  LOGGER.info("TosLookup.getUnitsStowedToRoRo end");
					  return result;
				  }
				  
				  public String getNextObVesvoyToHon(String vesvoy) {
					  LOGGER.info("TosLookup.getNextObVesvoyToHon begin");
					  PreparedStatement pstmt = null;
				      ResultSet rs = null;
				      String vesselCode = null;
				      String voyage = null;
				      String outboundVesvoy = null;
					  try {
						  if (vesvoy!=null) {
							  vesselCode = vesvoy.substring(0,3);
							  voyage = vesvoy.substring(3,7);
						  }
						  pstmt = conn.prepareStatement(GET_NEXT_OB_BARGE_TO_HON);
						  pstmt.setString(1, vesselCode);
						  pstmt.setString(2, voyage);
				          rs = pstmt.executeQuery();
				           while(rs.next()) {
				        	  // System.out.println("getNextObVesvoyToHon inside result set is "+rs.getString(1));
				        	   return rs.getString(1);
				           }
					  }catch(Exception ex) {
						  LOGGER.error("Could not get vessel vist",ex);
					  }
					  finally {
				             close(rs);
				             close(pstmt);
				         }
					  LOGGER.info("TosLookup.getNextObVesvoyToHon end");
					  return null;
				  }
				  
				  public String getVesselService(String vesvoy) {
					  LOGGER.info("TosLookup.getVesselService begin :"+vesvoy);
					  PreparedStatement pstmt = null;
				      ResultSet rs = null;
				      String vesselCode = null;
				      String voyage = null;
				      String outboundVesvoy = null;
					  try {

						  pstmt = conn.prepareStatement(GET_VESSEL_SERVICE);
						  pstmt.setString(1, vesvoy);
				          rs = pstmt.executeQuery();
				           while(rs.next()) {
				        	  // System.out.println("getVesselService inside result set is "+rs.getString(2));
				        	   return rs.getString(2);
				           }
					  }catch(Exception ex) {
						  LOGGER.error("Could not get vessel service",ex);
					  }
					  finally {
				             close(rs);
				             close(pstmt);
				         }
					  LOGGER.info("TosLookup.getVesselService end");
					  return null;
				  }
				  public String getBeginReceive(String vesvoy, String facility)
				  {
					  LOGGER.info("****** TosLookup.getBeginReceive begin ******");
					  //System.out.println("****** TosLookup.getBeginReceive begin ******");
					  //
					  PreparedStatement pstmt = null;
					  ResultSet rs = null;
					  String availability = "";
					  try {
						  pstmt = conn.prepareStatement(GET_BEGIN_RECEIVE);
						  pstmt.setString(1, vesvoy.substring(0, 3));
						  pstmt.setString(2, vesvoy.substring(3, 6));
						  pstmt.setString(3, facility);
						  rs = pstmt.executeQuery();
						  //LOGGER.info("Begin Receive date  query"+pstmt.toString());
						  if (rs.next()) {
							  LOGGER.info("Begin Receive date  --> "+rs.getTimestamp(1));
							  if(rs.getTimestamp(1)!=null) {
								  availability = new SimpleDateFormat("MM/dd/yyyy").format(rs.getTimestamp(1));
								  //System.out.println("result --> "+availability);
								  LOGGER.info("result --> "+availability);
							  }
						  }
					  }catch(Exception ex) {
						  ex.printStackTrace();
						  LOGGER.error("Could not get vessel availability ",ex);
					  }
					  finally {
						  close(rs);
						  close(pstmt);
					  }
					  //
					  LOGGER.info("****** TosLookup.getBeginReceive end ******");
					  //System.out.println("****** TosLookup.getBeginReceive end ******");
					  return availability;
				  }

	//retrieve the active unit from all facility, based in the IB carrier
	public String getUnitActiveFacility(String unitNbr, String vesvoy)
	{
		LOGGER.info("****** TosLookup.getUnitActiveFacility begin ******");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String unitFacility = "";
		try {
			pstmt = conn.prepareStatement(GET_UNIT_ACTIVE_FCY);
			pstmt.setString(1, unitNbr);
			pstmt.setString(2, vesvoy);
			rs = pstmt.executeQuery();
			LOGGER.info("GET_UNIT_ACTIVE_FCY query"+pstmt.toString());

			if (rs.next()) {
				LOGGER.info(rs.getString(3) +" --> FACILITY  --> "+rs.getString(4));
				unitFacility = rs.getString(3);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Could not get unit active Facility ",ex);
		} finally {
			close(rs);
			close(pstmt);
		}
		//
		LOGGER.info("****** TosLookup.getUnitActiveFacility end ******");
		return unitFacility;
	}

	//retrieve the active unit from all facility, irrespective of carrier
	public String getUnitActiveFacilityAllCategory(String unitNbr)
	{
		LOGGER.info("****** TosLookup.getUnitActiveFacilityAllCategory begin ******");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String unitFacility = "";
		try {
			pstmt = conn.prepareStatement(GET_UNIT_ACTIVE_FCY_ALL_CATEGORY);
			pstmt.setString(1, unitNbr);
			rs = pstmt.executeQuery();
			LOGGER.info("GET_UNIT_ACTIVE_FCY_ALL_CATEGORY query"+pstmt.toString());

			if (rs.next()) {
				LOGGER.info(rs.getString(3) +" --> FACILITY  --> "+rs.getString(4));
				unitFacility = rs.getString(3);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Could not get unit active Facility ",ex);
		} finally {
			close(rs);
			close(pstmt);
		}
		//
		LOGGER.info("****** TosLookup.getUnitActiveFacilityAllCategory end ******");
		return unitFacility;
	}

	public String getVesselCurrentFacility(String vesvoy)
	{
		LOGGER.info("****** TosLookup.getVesselCurrentFacility begin ******");
		//System.out.println("****** TosLookup.getBeginReceive begin ******");
		//
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String vesselFacilityANK = null;
		String vesselFacilityKDK = null;
		String vesselFacilityDUT = null;
		String vesselFacility = "";
		try {
			pstmt = conn.prepareStatement(GET_PHASE_FACILITY);
			pstmt.setString(1, vesvoy);
			rs = pstmt.executeQuery();
			LOGGER.info("GET_PHASE_FACILITY query"+pstmt.toString());
			boolean hasWorking = false;
			boolean ankDeparted = false;
			boolean kdkDeparted = false;
			boolean dutDeparted = false;
			int VESSEL_VISIT_PHSE_COMPLETE = 50;
			int VESSEL_VISIT_PHSE_WORKING = 50;

			while (rs.next()) {
				LOGGER.info(rs.getString(1) +" --> PHASE  --> "+rs.getString(2));
				String facility = rs.getString(1);
				String phase = rs.getString(2);

				if(facility != null && phase != null) {
					int phaseNumber = Integer.parseInt(phase.substring(0,2));
					if (phaseNumber < VESSEL_VISIT_PHSE_COMPLETE) {
						if ("ANK".equalsIgnoreCase(facility)) {
							vesselFacilityANK = facility;
						} else if ("KDK".equalsIgnoreCase(facility)) {
							vesselFacilityKDK = facility;
						} else if ("DUT".equalsIgnoreCase(facility)) {
							vesselFacilityDUT = facility;
						}
						hasWorking = true;
					} else {
						if (phaseNumber > VESSEL_VISIT_PHSE_WORKING) {
							if ("DUT".equalsIgnoreCase(facility)) {
								dutDeparted = true;
								vesselFacilityDUT = facility;
							} else if ("KDK".equalsIgnoreCase(facility)) {
								kdkDeparted = true;
								vesselFacilityKDK = facility;
							} else if ("ANK".equalsIgnoreCase(facility)) {
								ankDeparted = true;
								vesselFacilityDUT = facility;
							}
						}
					}
					LOGGER.info("result --> "+phase);
				}
			}
			if (hasWorking) {
				if (vesselFacilityANK != null) {
					vesselFacility = vesselFacilityANK;
				} else if (vesselFacilityKDK != null) {
					vesselFacility = vesselFacilityKDK;
				} else if (vesselFacilityDUT != null) {
					vesselFacility = vesselFacilityDUT;
				}
			} else {
				if (dutDeparted) {
					vesselFacility = vesselFacilityDUT;
				} else if (kdkDeparted) {
					vesselFacility = vesselFacilityKDK;
				} else if (ankDeparted) {
					vesselFacility = vesselFacilityANK;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Could not get vessel Facility ",ex);
		}
		finally {
			close(rs);
			close(pstmt);
		}
		//
		LOGGER.info("****** TosLookup.getVesselCurrentFacility end ******");
		return vesselFacility;
	}
				  public boolean isValidTrucker(String trucker)
				  {
					  LOGGER.info("****** TosLookup.isValidTrucker begin ****** "+trucker);
					  PreparedStatement pstmt = null;
					  ResultSet rs = null;
					  boolean valid = false;
					  try {
						  pstmt = conn.prepareStatement(VALIDATE_TRUCKER);
						  pstmt.setString(1, trucker);
						  rs = pstmt.executeQuery();
						  if (rs.next()) {
							  valid = true;
							  LOGGER.info("result --> valid");
						  }
					  }catch(Exception ex) {
						  ex.printStackTrace();
						  LOGGER.error("Could not get trucker availability ",ex);
					  }
					  finally {
						  close(rs);
						  close(pstmt);
					  }
					  //
					  LOGGER.info("****** TosLookup.isValidTrucker end ******");
					  return valid;
				  }
				  
				  public String getConsigneeNotes(String consignee) {
					  
					  LOGGER.info("****** TosLookup.getConsigneeNotes begin ******: "+consignee);
					  //System.out.println("****** TosLookup.isValidTrucker begin ******");
					  //
					  PreparedStatement pstmt = null;
					  ResultSet rs = null;
					  String notes = null; 
					  try {
						  pstmt = conn.prepareStatement(GET_CONSIGNEE_NOTES);
						  pstmt.setString(1, consignee);
						  rs = pstmt.executeQuery();
						  if (rs.next()) {
							  //System.out.println("result --> valid");
							  LOGGER.info("Notes in tos look up : "+consignee+" --- "+rs.getString(2));
							  return rs.getString(2);
						  }
					  }catch(Exception ex) {
						  ex.printStackTrace();
						  LOGGER.error("Could not get consignee notes  ",ex);
					  }
					  finally {
						  close(rs);
						  close(pstmt);
					  }
					  //
					  LOGGER.info("****** TosLookup.getConsigneeNotes end ******");
					  //System.out.println("****** TosLookup.isValidTrucker end ******");
					  return notes;
				  }
				  
				    /**
				     * This method gets SIT status from TOS for the container.
				     * @param equipmentId
				     * @return
				     * @throws SQLException
				     */
				    public String getSITInfo(String equipmentId) throws SQLException {
				    	 LOGGER.info("TosLookup.getSITInfo begin");
				    	 PreparedStatement pstmt = null;
				         ResultSet rs = null;
				         String sitInfo="";
				         equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_SIT);
				    		pstmt.setString(1, equipmentId);
				            rs = pstmt.executeQuery();
				            if (rs.next()) {
				            	System.out.println("result-> ID, DRAY_STATUS "+rs.getString(1)+ " - "+rs.getString(2));
				            	sitInfo = rs.getString(2);
				            }

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get SIT Info",e);
				    	}finally {
				             close(rs);
				             close(pstmt);
				         }
				    	return sitInfo;
				    }
				    /**
				     * This method is used to get the Dray status, Group info from TOS for which containers group is either XFER-WO or XFER-SI
				     * @param equipmentId
				     * @return
				     * @throws SQLException
				     */
				    public HashMap<String, String> getWOSIGrpInfo(String equipmentId) throws SQLException {
				    	LOGGER.info("TosLookup.getWOSIGrpInfo begin");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	HashMap<String, String> resultMap = null;
				    	equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_WO_SI_GRP_DSTATUS);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		if (rs.next()) {
				    			LOGGER.info("result-> ID, DRAY_STATUS, GRP "+rs.getString(1)+ " - "+rs.getString(2)+ " - "+rs.getString(3));
				    			resultMap = new HashMap<String, String>();
								resultMap.put("ID", rs.getString(1));
				    			resultMap.put("DRAY_STATUS", rs.getString(2));
				    			resultMap.put("GRP", rs.getString(3));
				    		}

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get WO SI Grp Info ",e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getWOSIGrpInfo end");
				    	return resultMap;
				    }
				    
				    /**
				     * This method gets SIT status from TOS for the container.
				     * @param equipmentId
				     * @return
				     * @throws SQLException
				     */
				    public HashMap getSITDtls(String equipmentId) throws SQLException {
				    	 LOGGER.info("TosLookup.getSITInfo begin");
				    	 PreparedStatement pstmt = null;
				         ResultSet rs = null;
				         HashMap sitMap= new HashMap();
				         equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_SIT_INFO);
				    		pstmt.setString(1, equipmentId);
				            rs = pstmt.executeQuery();
				            if (rs.next()) {
				            	System.out.println("result-> ID, DRAY_STATUS "+rs.getString(1)+ " - "+rs.getString(2));
				            	sitMap.put("DRAYSTATUS", rs.getString(2));
				            	sitMap.put("COMMODITY", rs.getString(3));
				            }

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get SIT Info",e);
				    	}finally {
				             close(rs);
				             close(pstmt);
				         }
				    	return sitMap;
				    }
				    
				    public String getCarrierIdForUnit(String equipmentId) throws SQLException {
				    	 LOGGER.info("TosLookup.getCarrierIdForUnit begin");
				    	 PreparedStatement pstmt = null;
				         ResultSet rs = null;
				         String carrierId="";
				         equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_CARRIER_ID_FOR_UNIT);
				    		pstmt.setString(1, equipmentId);
				            rs = pstmt.executeQuery();
				            if (rs.next()) {
				            	LOGGER.info("result-> ACTIVE UNIT ID, CARRIER ID "+rs.getString(1)+ " - "+rs.getString(2));
				            	carrierId = rs.getString(2);
				            }
				            close(rs);
				            close(pstmt);
				            if (carrierId == null || "".equalsIgnoreCase(carrierId)) {
				            	LOGGER.info("There is no ACTIVE unit for the unit "+equipmentId);
				            }
				            
				            if (carrierId == null || "".equalsIgnoreCase(carrierId)) {
				            	LOGGER.info(" Get the carrier ID from the latest DEPARTED unit");
								pstmt = conn.prepareStatement(GET_DEPT_CARR_ID_FOR_UNIT);
				            	pstmt.setString(1, equipmentId);
					            rs = pstmt.executeQuery();
					            if (rs.next()) {
					            	LOGGER.info("result-> DEPARTED UNIT ID, CARRIER ID "+rs.getString(1)+ " - "+rs.getString(2));
					            	carrierId = rs.getString(2);
					            }
				            }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get SIT Info",e);
				    	}finally {
				             close(rs);
				             close(pstmt);
				         }
				    	return carrierId;
				    }
				    
				    public String getCategoryForUnit(String equipmentId) throws SQLException {
				    	 LOGGER.info("TosLookup.getCategoryForUnit begin");
				    	 PreparedStatement pstmt = null;
				         ResultSet rs = null;
				         String category="";
						equipmentId = equipmentId + "%";
						try {
							pstmt = conn.prepareStatement(GET_ACTIVE_CATEGORY);
				    		pstmt.setString(1, equipmentId);
							rs = pstmt.executeQuery();
							if (rs.next()) {
								System.out.println("result-> UNIT ID, CATEGORY " + rs.getString(1) + " - " + rs.getString(2));
				            	category = rs.getString(2);
				            }

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get Category Info",e);
				    	}finally {
				             close(rs);
				             close(pstmt);
				         }
				    	return category;
				    }
				    public ArrayList<String> getRestowMarkedContainersForVesVoy(String vesvoy) throws SQLException {
				    	LOGGER.info("TosLookup.getRestowMarkedContainersForVesVoy begin - VesVoy:"+vesvoy+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	ArrayList<String> unitsList = new ArrayList<String>();
				    	try {
				    		pstmt = conn.prepareStatement(GET_RESTOW_MARKED_UNITS_FOR_VV);
				    		pstmt.setString(1, vesvoy);
				    		LOGGER.info(pstmt.toString());
				    		rs = pstmt.executeQuery();
				    		while (rs.next()) {
				    			LOGGER.info("result-> UNIT ID "+rs.getString(1));
				    			unitsList.add(rs.getString(1));
				    		}

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get RestowContainers for "+vesvoy,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getRestowMarkedContainersForVesVoy end");
				    	return unitsList;
				    }

				    public String getPOLForDepartedUnit(String equipmentId) throws SQLException {
				    	LOGGER.info("TosLookup.getPOLForDepartedUnit begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	String pol="";
				    	equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_POL_FOR_DPET_UNIT);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			    LOGGER.info("result-> UNIT ID, POL "+rs.getString(1)+ " - "+rs.getString(2));
					            	pol = rs.getString(2);
					          }

				    	}catch(SQLException e) {
				    		LOGGER.error("Could not getPOLForDepartedUnit for "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getPOLForDepartedUnit end");
				    	return pol;
				    }
				    
				    public String isLatestUnitDeparted(String equipmentId) throws SQLException {
				      	LOGGER.info("TosLookup.isLatestUnitDeparted begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	String transitState=null;
				    	String isDeparted = "false";
				    	equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(IS_LATEST_DEPARTED);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			    LOGGER.info("result-> UNIT ID, TRANSIT_STATE "+rs.getString(1)+ " - "+rs.getString(2));
				    			    transitState = rs.getString(2);
					          }
				    		 if (transitState!=null && "S70_DEPARTED".equalsIgnoreCase(transitState)) {
				    			 isDeparted = "true";
				    		 }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not isLatestUnitDeparted for "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.isLatestUnitDeparted end");
				    	return isDeparted;
				    }
				    public HashMap<String, String> getUnitDetails(String equipmentId) throws SQLException {
				      	LOGGER.info("TosLookup.getUnitDetails begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	HashMap<String, String> resultMap = null;
				    	//equipmentId = equipmentId + "%";
				    	String eventDate = null;
				    	String availDate = null;
				    	String lastFreeDay = null;
				    	String dueDate = null;
				    	try {
				    		pstmt = conn.prepareStatement(GET_UNIT_DETAILS);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			 resultMap = new HashMap<String, String>();
				    			    //LOGGER.info("result-> UNIT ID, CARRIER_MODE, TRANSIT_STATE, TIME_MOVE, GROUP, DEST, CONSIGNEE, BLNBR, HOLDS, FREIGHT_KIND -->"+rs.getString(1)+ " - "+rs.getString(2)+ " - "+rs.getString(3)+ " - "+rs.getTimestamp(4)+ " - "+rs.getString(7)
				    			    	//	+ " - "+rs.getString(9)+ " - "+rs.getString(10)+ " - "+rs.getString(11)+ " - "+rs.getString(15)+ " - "+rs.getString(12));
				    			    if(rs.getTimestamp(4)!=null) {
				    			    	eventDate = new SimpleDateFormat("MM/dd/yyyy").format(rs.getTimestamp(4));
				    			    }	
				    			    if(rs.getTimestamp(42)!=null) {
				    			    	lastFreeDay = new SimpleDateFormat("MMddyyyy").format(rs.getTimestamp(42));
				    			    }	
				    			    if(rs.getTimestamp(43)!=null) {
				    			    	availDate = new SimpleDateFormat("MM/dd/yyyy").format(rs.getTimestamp(43));
				    			    }	
				    			    if(rs.getTimestamp(44)!=null) {
				    			    	dueDate = new SimpleDateFormat("MM/dd/yyyy").format(rs.getTimestamp(44));
				    			    }
				    			    resultMap.put("UNIT_ID",rs.getString(1));
				    			    resultMap.put("CARRIER_MODE",rs.getString(2));
				    			    resultMap.put("TRANSIT_STATE",rs.getString(3));
				    			    resultMap.put("TIME_MOVE",eventDate);
				    			    resultMap.put("GKEY",rs.getString(5));
				    			    resultMap.put("UNIT_GKEY",rs.getString(6));
				    			    resultMap.put("GRP",rs.getString(7));
				    			    resultMap.put("RA",rs.getString(8));
				    			    resultMap.put("DESTINATION",rs.getString(9));
				    			    resultMap.put("CONSIGNEE",rs.getString(10));
				    			    resultMap.put("BL_NBR",rs.getString(11));
				    			    resultMap.put("FREIGHT_KIND",rs.getString(12));
				    			    resultMap.put("REMARK",rs.getString(13));
				    			    resultMap.put("CATEGORY",rs.getString(14));
				    			    resultMap.put("CONSIGNEE_ID",rs.getString(15));
				    			    resultMap.put("OB_DECLARED",rs.getString(16));
				    			    resultMap.put("OB_ACTUAL",rs.getString(17));
				    			    resultMap.put("VISIT_STATE",rs.getString(18));
				    			    resultMap.put("SEAL_NBR1",rs.getString(19));
				    			    resultMap.put("COMM_ID",rs.getString(20));
				    			    resultMap.put("COMM_DESC",rs.getString(21));
				    			    resultMap.put("EQUIP_TYPE",rs.getString(22));
				    			    resultMap.put("POL",rs.getString(23));
				    			    resultMap.put("POD1",rs.getString(24));
				    			    resultMap.put("STRENGTH_CODE",rs.getString(25));
				    			    resultMap.put("SHIPPER",rs.getString(26));
				    			    resultMap.put("SHIPPER_ID",rs.getString(27));
				    			    resultMap.put("IB_ACTUAL", rs.getString(28));
				    			    resultMap.put("IB_DECLRD", rs.getString(29));
				    			    resultMap.put("DRAY_STATUS", rs.getString(30));
				    			    resultMap.put("TRUCK", rs.getString(31));
				    			    resultMap.put("MISC2", rs.getString(32));
				    			    resultMap.put("LINEOP", rs.getString(33));
				    			    resultMap.put("LAST_POS_LOCTYPE", rs.getString(34));
				    			    resultMap.put("LAST_POS_SLOT", rs.getString(35));
				    			    resultMap.put("REQUIRES_POWER", rs.getString(36));
				    			    resultMap.put("TEMP_REQD_C", rs.getString(37));
				    			    resultMap.put("CNEEPO", rs.getString(38));
				    			    resultMap.put("CSRID", rs.getString(39));
				    			    resultMap.put("MILTCN", rs.getString(40));
				    			    resultMap.put("CWEIGHT_KG", rs.getString(41));
				    			    resultMap.put("LAST_FREE_DAY", lastFreeDay);
				    			    resultMap.put("AVAIL_DATE", availDate);
				    			    resultMap.put("DUE_DATE", dueDate);
				    			    resultMap.put("STOW_RESTRICTION_CODE",rs.getString(45));
				    			    resultMap.put("LAST_POS_LOCID",rs.getString(46));
				    			    resultMap.put("VES_CLASS_TYPE",rs.getString(47));
				    			    resultMap.put("FLAG",rs.getString(48));
				    			    resultMap.put("RNO",rs.getString(49));
				    			    resultMap.put("HOLDS",rs.getString(50));
				    			    LOGGER.info("resultMap in getUnitDetails is "+resultMap.toString());
					          }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not unit details from TOS "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getUnitDetails end");
				    	return resultMap;
				    }
				    public String getUnitRemark(String equipmentId) throws SQLException {
				      	LOGGER.info("TosLookup.getUnitRemark begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	String result = null;
				    	equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_UNIT_REMARK);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			    LOGGER.info("result-> REMARK -->"+rs.getString(1));
				    			    result = rs.getString(1);
					          }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not Unit remark for "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getUnitRemark end");
				    	return result;
				    }
				    public String getUnitTruckingCo(String equipmentId) throws SQLException {
				      	LOGGER.info("TosLookup.getUnitTruckingCo begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	String result = null;
				    	equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_TRUCKING_CO);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			    LOGGER.info("result-> TRUCKING_COMPANY,ID,NAME -->"+rs.getString(1)+","+rs.getString(2)+","+rs.getString(3));
				    			    result = rs.getString(2);
					          }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not get Unit Trucking co for "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getUnitTruckingCo end");
				    	return result;
				    }
				    public HashMap<String, String> getUnitCarrierDetails(String equipmentId) throws SQLException {
				      	LOGGER.info("TosLookup.getUnitCarrierDetails begin - equipment id :"+equipmentId+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	HashMap<String, String> resultMap = null;
				    	//equipmentId = equipmentId + "%";
				    	try {
				    		pstmt = conn.prepareStatement(GET_UNIT_CARRIER_DETAILS);
				    		pstmt.setString(1, equipmentId);
				    		rs = pstmt.executeQuery();
				    		 if (rs.next()) {
				    			     resultMap = new HashMap<String, String>();
				    			     resultMap.put("UNIT_ID", rs.getString(1));
				    			     resultMap.put("CATEGORY", rs.getString(2));
				    			     resultMap.put("TRANSIT_STATE", rs.getString(3));
				    			     resultMap.put("OB_ACTUAL", rs.getString(4));
				    			     resultMap.put("OB_ACTUAL_MODE", rs.getString(5));
				    			     resultMap.put("OB_DECLRD", rs.getString(6));
				    			     resultMap.put("OB_DECLRD_MODE", rs.getString(7));
				    			     resultMap.put("IB_ACTUAL", rs.getString(8));
				    			     resultMap.put("IB_ACTUAL_MODE", rs.getString(9));
				    			     resultMap.put("IB_DECLRD", rs.getString(10));
				    			     resultMap.put("IB_DECLRD_MODE", rs.getString(11));
				    			     resultMap.put("RA", rs.getString(12));
				    			     resultMap.put("CREATE_TIME", rs.getString(13));
				    			     LOGGER.info("result-->"+resultMap.toString());
					          }
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not Unit Carrier details "+equipmentId,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getUnitCarrierDetails end");
				    	return resultMap;
				    }

	public HashMap<String, String> getUnitCarrierDetailsForFacility(String equipmentId, String bookingNbr) throws SQLException {
		LOGGER.debug("TosLookup.getUnitCarrierDetails begin - equipment id :"+equipmentId+"+");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> resultMap = null;
		equipmentId = equipmentId + "%";
		try {
			pstmt = conn.prepareStatement(UNIT_DETAILS_FOR_FACILITY);
			pstmt.setString(1, equipmentId);
			pstmt.setString(2, bookingNbr);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				resultMap = new HashMap<String, String>();
				resultMap.put("UNIT_ID", rs.getString(1));
				resultMap.put("CATEGORY", rs.getString(2));
				resultMap.put("TRANSIT_STATE", rs.getString(3));
				resultMap.put("OB_ACTUAL", rs.getString(4));
				resultMap.put("OB_ACTUAL_MODE", rs.getString(5));
				resultMap.put("OB_DECLRD", rs.getString(6));
				resultMap.put("OB_DECLRD_MODE", rs.getString(7));
				resultMap.put("IB_ACTUAL", rs.getString(8));
				resultMap.put("IB_ACTUAL_MODE", rs.getString(9));
				resultMap.put("IB_DECLRD", rs.getString(10));
				resultMap.put("IB_DECLRD_MODE", rs.getString(11));
				resultMap.put("RA", rs.getString(12));
				resultMap.put("CREATE_TIME", rs.getString(13));
				resultMap.put("FACILITY", rs.getString(14));
				resultMap.put("BOOKING_NBR", rs.getString(15));
				resultMap.put("HOLDS", rs.getString(19));
				LOGGER.info("result-->"+resultMap.toString());
			}
		}catch(SQLException e) {
			LOGGER.error("Could not find ACTIVE Unit with Carrier details "+equipmentId,e);
		}finally {
			close(rs);
			close(pstmt);
		}
		LOGGER.info("TosLookup.getUnitCarrierDetails end");
		return resultMap;
	}

				    public HashMap<String, String> getSITByBooking(String bkgNbr) throws SQLException {
				      	LOGGER.info("TosLookup.getSITByBooking begin - bkgNbr :"+bkgNbr+"+");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	HashMap<String, String> resultMap = null;
				    	try {
				    		pstmt = conn.prepareStatement(GET_SIT_BKG);
				    		pstmt.setString(1, bkgNbr);
				    		rs = pstmt.executeQuery();
				    		if (rs.next()) {
				    			resultMap = new HashMap<String, String>();
				    			resultMap.put("BOOKING_NUMBER", rs.getString(1));
				    			resultMap.put("COMMODITY", rs.getString(2));
				    			resultMap.put("DESCRIPTION", rs.getString(3));
				    		}
				    	}catch(SQLException e) {
				    		LOGGER.error("Could not SIT Cmdy details "+bkgNbr,e);
				    	}finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("TosLookup.getSITByBooking end");
				    	return resultMap;
				    }
				    public ArrayList<String> searchBkgNbrs(ArrayList<String> nbrs) {
				    	LOGGER.info("****** TosLookup.searchBkgNbrs begin ******");
				    	PreparedStatement pstmt = null;
				    	ResultSet rs = null;
				    	ArrayList<String> results = new ArrayList<String>();
				    	String sql = String.format(SEARCH_BKG_NBRS, preparePlaceHolders(nbrs.size()));
						try {
				    		pstmt = conn.prepareStatement(sql);
				    		setValues(pstmt, nbrs.toArray());
				    		rs = pstmt.executeQuery();
				    		while (rs.next()) {
				    			results.add(rs.getString(1));
				    		}
				    	}catch(Exception ex) {
				    		ex.printStackTrace();
				    		LOGGER.error("Could not search booking numbers ",ex);
				    	}
				    	finally {
				    		close(rs);
				    		close(pstmt);
				    	}
				    	LOGGER.info("****** TosLookup.searchBkgNbrs end ******");
				    	return results;
				    }

	public HashMap<String, String> getBookingDetailsForHazardous(String bkgNbr) throws SQLException {
		LOGGER.info("TosLookup.getBookingDetailsForHazardous begin - bkgNbr :"+bkgNbr+"+");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, String> resultMap = null;
		String SQL = "SELECT ARGO_CARRIER_VISIT.ID AS CARRIER_VISIT_ID ," +
				"      REF_BIZUNIT_SCOPED.ID AS LINE_OP," +
				"      INV_EQ_BASE_ORDER.EQ_STATUS, " +
				"      INV_EQ_BASE_ORDER.PREVENT_TYPE_SUBST, " +
				"	   ARGO_FACILITY.ID AS FACILITY " +
				" FROM 	INV_EQ_BASE_ORDER, " +
				" 		ARGO_CARRIER_VISIT, " +
				" 		REF_BIZUNIT_SCOPED, " +
				"  		ARGO_FACILITY " +
				" WHERE INV_EQ_BASE_ORDER.NBR = \'" + bkgNbr + "\'" +
				" AND ARGO_CARRIER_VISIT.GKEY = INV_EQ_BASE_ORDER.VESSEL_VISIT_GKEY " +
				" AND REF_BIZUNIT_SCOPED.ROLE = \'LINEOP\' " +
				" AND REF_BIZUNIT_SCOPED.GKEY = INV_EQ_BASE_ORDER.LINE_GKEY " +
				" AND ARGO_CARRIER_VISIT.FCY_GKEY = ARGO_FACILITY.GKEY";
		LOGGER.debug("SQL is " + SQL);

		try {
			pstmt = conn.prepareStatement(SQL);
			/*LOGGER.debug("pstmt prepared before adding booking\t" + pstmt);
			pstmt.setString(1, bkgNbr);
			LOGGER.debug("pstmt prepared\t" + pstmt);*/
			rs = pstmt.executeQuery();
			LOGGER.debug("pstmt executed\t"+pstmt);

			if (rs.next()) {
				resultMap = new HashMap<String, String>();
				resultMap.put("CARRIER_VISIT_ID", rs.getString("CARRIER_VISIT_ID"));
				LOGGER.debug(rs.getString("CARRIER_VISIT_ID"));
				resultMap.put("LINE_OP", rs.getString("LINE_OP"));
				LOGGER.debug(rs.getString("LINE_OP"));
				resultMap.put("EQ_STATUS", rs.getString("EQ_STATUS"));
				LOGGER.debug(rs.getString("EQ_STATUS"));
				resultMap.put("PREVENT_TYPE_SUBST", rs.getString("PREVENT_TYPE_SUBST"));
				LOGGER.debug(rs.getString("PREVENT_TYPE_SUBST"));
				resultMap.put("FACILITY", rs.getString("FACILITY"));
				LOGGER.debug(rs.getString("FACILITY"));
			}
		}catch(SQLException e) {
			LOGGER.error("Could not load required details "+bkgNbr,e);
		}finally {
			close(rs);
			close(pstmt);
		}
		LOGGER.info("TosLookup.getBookingDetailsForHazardous end");
		return resultMap;
	}

	public boolean deleteHazardsForBooking(String bkgNbr) throws SQLException {
		LOGGER.info("TosLookup.deleteHazardsForBooking begin - bkgNbr :"+bkgNbr+"+");
		PreparedStatement pstmt = null;
		boolean isDeleted = false;
		try {
			LOGGER.info("RAW SQL before set param : "+DELETE_HAZARD_BDX);
			pstmt = conn.prepareStatement(DELETE_HAZARD_BDX);
			LOGGER.info("SQL before set param : "+pstmt.toString());
			pstmt.setString(1, bkgNbr);
			LOGGER.info("SQL after set param : "+pstmt.toString());
			isDeleted = pstmt.execute();
			if (isDeleted) {
				LOGGER.info("All Hazards are deleted for Booking : "+bkgNbr);
			} else {
				LOGGER.info("Could not delete HAZARDS for Booking : " + bkgNbr);
			}
		}catch(SQLException e) {
			LOGGER.error("Could not delete HAZARDS for Booking : "+bkgNbr,e);
		}finally {
			conn.commit();
			conn.close();
			close(pstmt);
		}
		LOGGER.info("TosLookup.getSITByBooking end");
		return isDeleted;
	}

			    /*
			     *  This method returns all the valid truckers from the list of truckers passed.
			     */
				  public ArrayList getValidTruckerList(ArrayList<String> notifypartyList) throws SQLException 
				  {
						  LOGGER.info("****** TosLookup.getValidTruckerList begin ******");

						  PreparedStatement pstmt = null;
						  ResultSet rs = null;
						  ArrayList<String> validTruckerList = new ArrayList<String>();
						  String sql = String.format(VALID_TRUCKER_LIST, preparePlaceHolders(notifypartyList.size()));
						  try {
							  pstmt = conn.prepareStatement(sql);
							  setValues(pstmt, notifypartyList.toArray());
							  rs = pstmt.executeQuery();
							  while (rs.next()) {
								  LOGGER.info("Valid truckers "+rs.getString(1));
								  validTruckerList.add(rs.getString(1));
							  }
						  }catch(Exception ex) {
							  ex.printStackTrace();
							  LOGGER.error("Could not get valid truckers ",ex);
						  }
						  finally {
							  close(rs);
							  close(pstmt);
						  }
						  LOGGER.info("****** TosLookup.getValidTruckerList end ******");
						  return validTruckerList;
					  }
					  
					  public static String preparePlaceHolders(int length) {
						    StringBuilder builder = new StringBuilder();
						    for (int i = 0; i < length;) {
						        builder.append("?");
						        if (++i < length) {
						            builder.append(",");
						        }
						    }
						    return builder.toString();
						}

						public static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
						    for (int i = 0; i < values.length; i++) {
						        preparedStatement.setObject(i + 1, values[i]);
						    }
						}

	public List<Map> getBargeVisitsById(String id) throws SQLException {
		LOGGER.debug("Getting Barge Visit by ID for " + id);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(BARGE_VISIT_BY_ID);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs == null) {
				return Collections.emptyList();
			}
			List<Map> mapList = new ArrayList<Map>();
			while (rs.next()) {
				//SELECT vst.ID VST_ID, fcy.ID CURRENT_FACILITY,  vst.PHASE,  nxt_fcy.ID NEXT_FACILITY,  dtl.IB_VYG,  dtl.ob_vyg,  svc.ID SVC_ID,  dtl.CLASSIFICATION
				ResultSetMetaData metaData = rs.getMetaData();
				for (int i = 1; i <=metaData.getColumnCount() ; i++) {
					LOGGER.info(metaData.getColumnName(i)+"\t"+metaData.getColumnTypeName(i));
				}
				Map<String, String> stringMap = new HashMap<String, String>();
				stringMap.put(VST_ID, rs.getString(VST_ID));
				stringMap.put(CURRENT_FACILITY, rs.getString(CURRENT_FACILITY));
				stringMap.put(PHASE, rs.getString(PHASE));
				stringMap.put(NEXT_FACILITY, rs.getString(NEXT_FACILITY));
				stringMap.put(IB_VYG, rs.getString(IB_VYG));
				stringMap.put(OB_VYG, rs.getString(OB_VYG));
				stringMap.put(SVC_ID, rs.getString(SVC_ID));
				stringMap.put(CLASSIFICATION, rs.getString(CLASSIFICATION) == null ? NONE : rs.getString(CLASSIFICATION));
				mapList.add(stringMap);
			}
			return mapList;

		} catch (SQLException e) {
			LOGGER.error("Could not get barge visit", e);
		} finally {
			close(rs);
			close(pstmt);
		}
		return null;
	}

	public String getBargeVisitsByBargeAndNextFacility(String inBargeId, String inNextFcyId, String inIBVygNo) throws SQLException {
		LOGGER.debug("Getting Barge Visit by barge id " + inBargeId + " next facility" + inNextFcyId + " IB Vyg No" + inIBVygNo);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(BARGE_VISIT_BY_VSL_N_NXT_FCY);
			LOGGER.info("SQL Before Parameters being added" + pstmt.toString());
			pstmt.setString(1, inBargeId);
			pstmt.setString(2, inNextFcyId);
			pstmt.setString(3, inIBVygNo);
			LOGGER.info("SQL after Parameters being set" + pstmt.toString());
			rs = pstmt.executeQuery();
			if (rs == null) {
				LOGGER.error("No Visit is found for barge id " + inBargeId + " next facility" + inNextFcyId + " IB Vyg No" + inIBVygNo);
				return NONE;
			}
			int rowCount = 0;
			String visitId = "";
			while (rs.next()) {
				++rowCount;
				visitId = rs.getString(VST_ID);
			}
			if (rowCount == 1)
				return visitId;
			else {
				LOGGER.error("No Visit is found for barge id " + inBargeId + " next facility" + inNextFcyId + " IB Vyg No" + inIBVygNo);
				return "NO_UNIQUE";
			}

		} catch (SQLException e) {
			LOGGER.error("Could not get barge visit", e);
		} finally {
			close(rs);
			close(pstmt);
		}
		return "ERROR";
	}

	public List<Map> getBargeVisitsByTimeFrame(String inLongHaulVesselId, String inLoadPort, String inNextFacility) throws SQLException {
		LOGGER.info("Getting Barge Visit by Vessel ETA, using vessel ID \t" + inLongHaulVesselId + "\t Barge Load Port \t"+inLoadPort+"\t barge discharge port\t"+inNextFacility);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(FILTERED_BARGES);
			pstmt.setString(1, inLongHaulVesselId);
			pstmt.setString(2,inNextFacility);
			pstmt.setString(3,inLoadPort);
			pstmt.setString(4,inNextFacility);
			rs = pstmt.executeQuery();
			if (rs == null) {
				return Collections.emptyList();
			}
			List<Map> mapList = new ArrayList<Map>();
			while (rs.next()) {
				ResultSetMetaData metaData = rs.getMetaData();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					LOGGER.debug(metaData.getColumnName(i) + "\t" + metaData.getColumnTypeName(i));
				}
				Map<String, String> stringMap = new HashMap<String, String>();
				stringMap.put(VST_ID, rs.getString(VST_ID));
				stringMap.put(CURRENT_FACILITY, rs.getString(CURRENT_FACILITY));
				stringMap.put(PHASE, rs.getString(PHASE));
				stringMap.put(NEXT_FACILITY, rs.getString(NEXT_FACILITY));
				stringMap.put(IB_VYG, rs.getString(IB_VYG));
				stringMap.put(OB_VYG, rs.getString(OB_VYG));
				stringMap.put(SVC_ID, rs.getString(SVC_ID));
				stringMap.put(CLASSIFICATION, rs.getString(CLASSIFICATION) == null ? NONE : rs.getString(CLASSIFICATION));
				stringMap.put(CARRIER_ID,rs.getString(CARRIER_ID));

				
				LOGGER.debug("inside getBargeVisitsByTimeFrame "+stringMap);
				mapList.add(stringMap);
			}
			
			
			return mapList;

		} catch (SQLException e) {
			LOGGER.error("Could not get barge visit", e);
		} finally {
			close(rs);
			close(pstmt);
		}
		return null;
	}
	
	public String getUnitActiveFacilityinAlaska(String unitNbr, String vesvoy)
	{
		LOGGER.info("****** TosLookup.getUnitActiveFacilityinAlaska begin ******");
String queryGetActiveFacility  = "select unit.id, unit.category, facility.id, unit.visit_state from tosmgr.inv_unit unit " +
		"inner join tosmgr.inv_unit_fcy_visit ufv on unit.active_ufv = ufv.gkey " +
		"inner join argo_facility facility on facility.gkey = ufv.fcy_gkey " +
		"where unit.id = ? and UNIT.VISIT_STATE = '1ACTIVE'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String unitFacility = "";
		try {
			pstmt = conn.prepareStatement(queryGetActiveFacility);
			pstmt.setString(1, unitNbr);
			
			rs = pstmt.executeQuery();
			LOGGER.info("GetActiveFacility query"+pstmt.toString());

			if (rs.next()) {
				LOGGER.info(rs.getString(3) +" --> FACILITY  --> "+rs.getString(4));
				unitFacility = rs.getString(3);
			}

		}catch(Exception ex) {
			ex.printStackTrace();
			LOGGER.error("Could not get unit active Facility in Alaska",ex);
		} finally {
			close(rs);
			close(pstmt);
		}
		//
		LOGGER.info("****** TosLookup.getUnitActiveFacilityinAlaska end ******");
		return unitFacility;
	}
						
						
}
