package com.matson.tos.reports.barge;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestPodData;
import com.matson.cas.refdata.mapping.TosStowPlanChassisMt;
import com.matson.cas.refdata.mapping.TosStowPlanCntrMt;
import com.matson.tos.dao.NewReportVesselDao;
import com.matson.tos.processor.CommonBusinessProcessor;
import com.matson.tos.util.TDPConstants;
import com.matson.tos.util.TDPProperties;

public class CyDiscSumGenerator implements JRDataSource {
	public ArrayList<CyDiscSum> cyDiscSumList = new ArrayList<CyDiscSum>();
	public int totalMoves = 0;
	int index = -1;

	public CyDiscSumGenerator(String vvd)
	{
		ArrayList<TosStowPlanCntrMt> cntrList = null;
		ArrayList<TosStowPlanChassisMt> chsList = null;
		// list - DIR= MTY, OUT, XXX
		ArrayList<TosStowPlanCntrMt> mtyList = new ArrayList<TosStowPlanCntrMt>();
		ArrayList<TosStowPlanCntrMt> nonmtyList = new ArrayList<TosStowPlanCntrMt>();
		ArrayList<TosStowPlanCntrMt> outList = new ArrayList<TosStowPlanCntrMt>();
		ArrayList<TosStowPlanCntrMt> xxxList = new ArrayList<TosStowPlanCntrMt>();
		ArrayList<String> mtyVvList = new ArrayList<String>();
		ArrayList<String> nonmtyVvList = new ArrayList<String>();
		ArrayList<CyDiscSum> tempDiscSumList = new ArrayList<CyDiscSum>();
		cntrList = NewReportVesselDao.getStowDataForCyLines(vvd);
		String typeCodeKey="";
		String[] type = new String[2];
		if(cntrList!=null) {
			System.out.println("cntrList size before : "+cntrList.size());
			chsList = NewReportVesselDao.getBareChassisDataForCyLines(vvd);
			if(chsList!=null) {
				// Add all the bare chassis records to the container list if any
				for(int b=0; b<chsList.size(); b++)
				{
					TosStowPlanChassisMt chsMt = chsList.get(b);
					TosStowPlanCntrMt cntrMt = new TosStowPlanCntrMt();
					cntrMt.setContainerNumber(chsMt.getChassisNumber());
					cntrMt.setCell(chsMt.getLoc());
					cntrMt.setTypeCode(chsMt.getTypeCode());
					cntrMt.setDir("CHS");
					cntrMt.setVesvoy("CHAS");
					cntrMt.setMisc1(chsMt.getVesvoy());
					cntrMt.setLoadPort(chsMt.getDport());
					cntrMt.setDport(chsMt.getDport());
					cntrList.add(b, cntrMt);
				}
				System.out.println("cntrList size after : "+cntrList.size());
				System.out.println("Bare chassis records added : " + chsList.size());
			}
			String cliOwner = NewReportVesselDao.getCliOwnerData();
			for(Iterator<TosStowPlanCntrMt> itr = cntrList.iterator(); itr.hasNext();)
			{
				TosStowPlanCntrMt cntrMt = itr.next();
				boolean clientCtr = false;
				boolean leasedCtr = false;
				boolean liveReefer = false;
				String matCtr = "";
				String orgSrv = "";
				String description = "";
				String vesvoy = cntrMt.getVesvoy();
				TosDestPodData refData = null;
				if(vesvoy!=null && !vesvoy.equals("CHAS"))
				{
					if(cntrMt.getActualVessel()!=null && cntrMt.getActualVoyage()!=null)
					{
						vesvoy = cntrMt.getActualVessel()+cntrMt.getActualVoyage();
					}
					else
					{
						vesvoy = "";
					}
					cntrMt.setVesvoy(vesvoy);
				}
				String ctrno = cntrMt.getContainerNumber();
				String owner = cntrMt.getOwner();
				String typeCode = cntrMt.getTypeCode();
				String origTypeCode = typeCode;
				String cargoNotes = cntrMt.getCargoNotes();
				String origCargoNotes = cntrMt.getCargoNotes();
				String dir = cntrMt.getDir();
				String ds = cntrMt.getDs();
				ds = ds==null?"":ds;
				String temp = cntrMt.getTemp();
				String commodity = cntrMt.getCommodity();
				commodity = commodity==null?"":commodity;
				String comments = cntrMt.getComments();
				comments = comments==null?"":comments;
				String shipper = cntrMt.getShipper();
				shipper = shipper==null?"":shipper;
				String hazf = cntrMt.getHazf();
				String dmgCode = cntrMt.getDamageCode();
				String retPort = cntrMt.getRetPort();
				retPort = retPort==null?"":retPort;
				String dPort = cntrMt.getDport();
				String dsc = cntrMt.getDsc();
				dsc = dsc==null?"":dsc;
				//
				if(owner!=null && cliOwner.contains(owner))
				{
					clientCtr = true;
					orgSrv = "MAT";
				}
				if(!ctrno.startsWith("MATU"))
				{
					leasedCtr = true;
				}
				if(typeCode==null || typeCode.equals("UNKNOWN"))
					typeCode = "UNKNOWN";
				else {
					typeCode = typeCode.substring(0, 6);
					if (typeCode.contains(" ")) {
						typeCodeKey = typeCode.replace(" ", ".");
						System.out.println("Replaced type code is "+typeCodeKey);
						type = typeCode.split(" ");
						
					}
				}
				
				System.out.println("typeCode 0 "+typeCode);
				liveReefer = (!dir.equals("MTY")&& temp!=null && !temp.equals("AMB") && !temp.equals(""));
				if(clientCtr || leasedCtr || owner.equals("APLU"))
					matCtr = "N";
				else
					matCtr = "Y";
				// Get type code description
				description = typeCode;

				//Raghu
				if ("N".equalsIgnoreCase(matCtr)) {
					typeCodeKey = "N"+typeCodeKey;
					System.out.println("typeCode 1 "+typeCodeKey);
				}
				if (typeCodeKey != null && !"".equalsIgnoreCase(typeCodeKey)) {
					System.out.println("typeCode 2 "+typeCodeKey);
					typeCode = TDPProperties.getInstance().getProperty(typeCodeKey);
					System.out.println("typeCode 3 "+typeCode);
				}
				
				ctrno = ctrno.replaceAll("[^\\.0123456789]","");
				if(ctrno.length()>=3)
				{
					if(ctrno.startsWith("5500"))
						typeCode = "550 RFR";
					else if(ctrno.startsWith("5510"))
						typeCode = "551 RFR";
					else if(ctrno.startsWith("683"))
						typeCode = "683 RFR";
					else if(ctrno.startsWith("684"))
						typeCode = "684 RFR";
					else if(ctrno.startsWith("688"))
						typeCode = "688 RFR";
					else if(ctrno.startsWith("625") && dir.equals("MTY"))
						typeCode = "625 DRY";
					else if(ctrno.startsWith("685") && dir.equals("MTY"))
						typeCode = "685 RFR (MDA)";					
				}
				if((commodity.equals("MTY CASS") || shipper.contains("AUTO LOGISTICS")) && ds.equals("CY") && origTypeCode.startsWith("D40"))
					typeCode = "MTY CASSETTES";
				try {
					int intCtrno = Integer.parseInt(ctrno);
					if((intCtrno>=500000 && intCtrno<=500410) && dir.equals("MTY"))
						typeCode = "500000-500410 (MDA)";
					else if((intCtrno>=683001 && intCtrno<=683375) && dir.equals("MTY"))
						typeCode = "683001-683375 (MDA)";					
				}catch(Exception e) {
					System.out.println("Parse error - Barge CY disc sum report - \n"+e);
				}
				// Put typecode description to cargo notes field
				cargoNotes = typeCode;
				if(dir.equals("MTY"))
					vesvoy = "";
				if(clientCtr)
				{
					cargoNotes = orgSrv + "-" + cargoNotes;
					if(dir.equals("MTY"))
						vesvoy = "LEASED";
				}
				if(leasedCtr && dir.equals("MTY"))
					vesvoy = "LEASED";
				if(dir.equals("MTY") && (owner.equals("SOSE")||owner.equals("NYKU")||owner.equals("NORA")||owner.equals("PMOU")))
				{
					if(owner.equals("SOSE"))
						vesvoy = "SOSE";
					else if(owner.equals("NYKU"))
						vesvoy = "NYKU";
					else if(owner.equals("NORA"))
						vesvoy = "NORA";
					else if(owner.equals("PMOU"))
						vesvoy = "PMOU";
					if(cargoNotes.equals("40' H/C"))
					{
						if(origTypeCode!=null && origTypeCode.endsWith("AL"))
							cargoNotes = cargoNotes + " (ALUM)";
						else if(origTypeCode!=null && origTypeCode.endsWith("ST"))
							cargoNotes = cargoNotes + " (STEEL)";
					}
				}
				if(origTypeCode!=null && origTypeCode.startsWith("R") && !liveReefer && !dir.equals("MTY"))
					cargoNotes = cargoNotes + " DRY";
				if(ds.equals("AUT") && hazf!=null && hazf.equals("Y"))
					cargoNotes = cargoNotes + " AUT HZ";
				else if(hazf!=null && hazf.equals("Y"))
					cargoNotes = cargoNotes + " (HZ: "+retPort+")";
				if(dmgCode!=null)
					cargoNotes = cargoNotes + " DMG";
				if(origTypeCode!=null && origTypeCode.length()>3)
				{
					if(commodity.contains("COBUS")||commodity.contains("CO BIZ")||commodity.contains("COBIZ"))
						cargoNotes = "COBIZ -"+origTypeCode.substring(1, 4)+"'";
					else if(commodity.contains("ASTRAY"))
						cargoNotes = "ASTRAY -"+origTypeCode.substring(1, 4)+"'";
					else if(commodity.contains("SHRED"))
						cargoNotes = "SHRED -"+origTypeCode.substring(1, 4)+"'";
					else
					{
						if(dir.equals("IN"))
						{
							if(dPort.equals("HON") && ds.equals("AUT"))
								cargoNotes = dPort + "-" + origTypeCode.substring(1, 4)+"' AUT";
							else
								cargoNotes = dPort + "-" + cargoNotes;
						}
					}
				}
				if(dir.equals("OUT"))
				{
					if(dsc.equals("S"))
					{
						cargoNotes = "EB SIT-"+origTypeCode.substring(1, 4)+"'";
						dir = "XXX";
						vesvoy = "";
					}
					else if(origCargoNotes!=null && origCargoNotes.contains("EB PRIORITY"))
					{
						cargoNotes = "EB PRIORITY-"+origTypeCode.substring(1, 4)+"'";
						dir = "XXX";
						vesvoy = "";
					}
					else
					{
						try{
							//Set Pod-1 value from TosDestPodData by sending Dport as input
							refData = (TosDestPodData)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestPodData", cntrMt.getDport());
							System.out.println("refdata from TosDestPodData"+refData);
						} catch (Exception e)
						{
							e.printStackTrace();
						}
						
						if (refData!=null && !"OPT".equals(cntrMt.getDischargePort()) && !CommonBusinessProcessor.vaidateDischargePort(cntrMt.getDischargePort())) {
							cargoNotes = refData.getPod1()+ " " + cargoNotes;;
						}else {
							cargoNotes = cntrMt.getDischargePort() + " " + cargoNotes;
						}
						
						//cargoNotes = cntrMt.getDischargePort() + " " + cargoNotes;
					}
				}
				if(dir.equals("IN"))
				{
					dir = "XXX";
					vesvoy = "";
				}
				if(origTypeCode!=null && !origTypeCode.startsWith("B") && !origTypeCode.startsWith("B"))
				{
					if(comments.contains("SPACER"))
						cargoNotes = cargoNotes + " (ON SPACER)";
					else if(comments.contains("LOOSE")||comments.contains(" IN 40"))
					{
						if(ds.equals("AUT") && dPort.equals("HON"))
						{}
						else
							cargoNotes = cargoNotes + " (LOOSE STOW)";
					}
				}
				if(origTypeCode!=null && origTypeCode.startsWith("R") && liveReefer)
				{
					if(cargoNotes.contains("EB SIT-"))
						cargoNotes = cargoNotes + " (LIVE REEFER)";
					else
					{
						cargoNotes = "LIVE REEFER-"+origTypeCode.substring(1, 4)+"'";
						dir = "XXX";
						vesvoy = "";
					}
				}
				if(dir.equals("CHS"))
				{
					dir = "MTY";
					vesvoy = "CHAS";
					if(origTypeCode!=null && !origTypeCode.equals("UNKNOWN"))
						cargoNotes = origTypeCode.substring(1, 4)+"'";
				}
				if(commodity.contains("LIVEST")||commodity.contains("WEANER")||commodity.contains("LIVE CAT"))
					cargoNotes = cargoNotes + " LIVESTOCK";
				// Assign all the manipulated fields back
				System.out.println("type[0] :"+type[0]);
				cntrMt.setOversizeFrontInches(new BigDecimal(type[0].substring(1,3)));
				cntrMt.setDir(dir);
				cntrMt.setVesvoy(vesvoy);
				if(cargoNotes==null || cargoNotes.length()==0)//In case type code doesn't have a description populated,assign typecode as description
					cargoNotes = typeCode.substring(0, 6);
				cntrMt.setCargoNotes(cargoNotes);
				System.out.println(dir + "\t" + vesvoy + "\t" + cargoNotes);
				if(dir.equals("MTY"))
				{
					mtyList.add(cntrMt);
					if(!mtyVvList.contains(vesvoy))
						mtyVvList.add(vesvoy);
				}
				else if(dir.equals("OUT"))
				{
					outList.add(cntrMt);
					if(!nonmtyVvList.contains(vesvoy))
						nonmtyVvList.add(vesvoy);
				}
				else if(dir.equals("XXX"))
				{
					xxxList.add(cntrMt);
					if(!nonmtyVvList.contains(vesvoy))
						nonmtyVvList.add(vesvoy);
				}
			}
			nonmtyList.addAll(outList);
			nonmtyList.addAll(xxxList);
			//Comparator ro=Collections.reverseOrder();
			Collections.sort(mtyVvList);
			Collections.sort(nonmtyVvList); 
			// FOR EMPTIES
			for(int v=0; v<mtyVvList.size(); v++)
			{
				CyDiscSum cyDisc = new CyDiscSum();
				String emptyHeader = "";
				String vv = mtyVvList.get(v);
				if(vv.equals("LEASED"))
					emptyHeader = "LEASE EMPTIES";
				else if(vv.equals("CHAS"))
					emptyHeader = "BARE CHASSIS";
				else if(vv.equals("SOSE"))
					emptyHeader = "SOSE ";
				else if(vv.equals("NYKU"))
					emptyHeader = "NYKU EMPTIES";
				else if(vv.equals("NORA"))
					emptyHeader = "NORA EMPTIES";
				else if(vv.equals("PMOU"))
					emptyHeader = "PMOU EMPTIES";
				else if(vv.equals(""))
					emptyHeader = "MATSON EMPTIES";
				cyDisc.setEmptyDesc(emptyHeader);
				cyDiscSumList.add(cyDisc);
				ArrayList<String> vvCargoNotesList = new ArrayList<String>();
				for(int t=0; t<mtyList.size(); t++)
				{
					TosStowPlanCntrMt cmt = mtyList.get(t);
					if(cmt.getVesvoy().equals(vv))
					{
						String tempCN = cmt.getCargoNotes();
						tempCN = tempCN==null?"":tempCN;
						if(!vvCargoNotesList.contains(tempCN))
							vvCargoNotesList.add(tempCN);
					}
				}
				Collections.sort(vvCargoNotesList);
				for(int c=0; c<vvCargoNotesList.size(); c++)
				{
					String desc = vvCargoNotesList.get(c);
					cyDisc = new CyDiscSum();
					cyDisc.setEmptyDesc("   "+ desc);
					if(desc!=null && desc.equals("UNKNOWN"))
					{
						if(emptyHeader.equals("SOSE EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN SOSE MTY TC");
						else if(emptyHeader.equals("LEASE EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN LEASE TC");
						else if(emptyHeader.equals("BARE CHASSIS"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN CHAS TC");
						else if(emptyHeader.equals("MATSON EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN EMPTY TC");
						else if(emptyHeader.equals("NYKU EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN NYKU MTY TC");
						else if(emptyHeader.equals("NORA EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN NORA MTY TC");
						else if(emptyHeader.equals("PMOU EMPTIES"))
							cyDisc.setEmptyDesc("   "+"UNKNOWN PMOU MTY TC");
					}
					int cnTotal = 0;
					for(int t=0; t<mtyList.size(); t++)
					{
						TosStowPlanCntrMt cmt = mtyList.get(t);
						String tempCN = cmt.getCargoNotes();
						tempCN = tempCN==null?"":tempCN;
						if(cmt.getVesvoy().equals(vv) && desc.equals(tempCN))
						{
							cnTotal = cnTotal + 1;
						}
					}
					cyDisc.setEmptyAmt(cnTotal+"");
					cyDiscSumList.add(cyDisc);
					totalMoves = totalMoves + cnTotal;
				}
			}
			// FOR FULL
			for(int n=0; n<nonmtyVvList.size(); n++)
			{
				CyDiscSum cyDisc = new CyDiscSum();
				String vv = nonmtyVvList.get(n);
				cyDisc.setFullDesc(vv);
				tempDiscSumList.add(cyDisc);
				ArrayList<String> vvCargoNotesList = new ArrayList<String>();
				for(int t=0; t<nonmtyList.size(); t++)
				{
					TosStowPlanCntrMt cmt = nonmtyList.get(t);
					if(cmt.getVesvoy().equals(vv))
					{
						String tempCN = cmt.getCargoNotes();
						tempCN = tempCN==null?"":tempCN;
						if(!vvCargoNotesList.contains(tempCN))
							vvCargoNotesList.add(tempCN);
					}
				}
				Collections.sort(vvCargoNotesList);
				for(int c=0; c<vvCargoNotesList.size(); c++)
				{
					String desc = vvCargoNotesList.get(c);
					cyDisc = new CyDiscSum();
					int cnTotal = 0;
					String tempDir = "";
					for(int t=0; t<nonmtyList.size(); t++)
					{
						TosStowPlanCntrMt cmt = nonmtyList.get(t);
						String tempCN = cmt.getCargoNotes();
						tempCN = tempCN==null?"":tempCN;
						if(cmt.getVesvoy().equals(vv) && desc.equals(tempCN))
						{
							cnTotal = cnTotal + 1;
							tempDir = cmt.getDir();
						}
					}
					if(tempDir.equals("OUT"))
						cyDisc.setFullDesc("   "+ desc);
					else
						cyDisc.setFullDesc(desc);
					cyDisc.setFullAmt(cnTotal+"");
					tempDiscSumList.add(cyDisc);
					totalMoves = totalMoves + cnTotal;
				}
			}
		}
		System.out.println("MTY ** cyDiscSumList.size() Before -- "+cyDiscSumList.size());
		System.out.println("FULL ** tempDiscSumList.size() -- "+ tempDiscSumList.size());
		// Add the tempDiscSumList data to cyDiscSumList
		if(tempDiscSumList.size()<=cyDiscSumList.size())
		{
			for(int te=0; te<tempDiscSumList.size(); te++)
			{
				CyDiscSum tempcyDisc = tempDiscSumList.get(te);
				CyDiscSum mtycyDisc = cyDiscSumList.get(te);
				mtycyDisc.setFullDesc(tempcyDisc.getFullDesc());
				mtycyDisc.setFullAmt(tempcyDisc.getFullAmt());
				cyDiscSumList.set(te, mtycyDisc);
			}
		}
		else
		{
			for(int te=0; te<cyDiscSumList.size(); te++)
			{
				CyDiscSum tempcyDisc = tempDiscSumList.get(te);
				CyDiscSum mtycyDisc = cyDiscSumList.get(te);
				mtycyDisc.setFullDesc(tempcyDisc.getFullDesc());
				mtycyDisc.setFullAmt(tempcyDisc.getFullAmt());
				cyDiscSumList.set(te, mtycyDisc);
			}
			for(int te=cyDiscSumList.size(); te<tempDiscSumList.size(); te++)
			{
				CyDiscSum tempcyDisc = tempDiscSumList.get(te);
				CyDiscSum mtycyDisc = new CyDiscSum();
				mtycyDisc.setFullDesc(tempcyDisc.getFullDesc());
				mtycyDisc.setFullAmt(tempcyDisc.getFullAmt());
				cyDiscSumList.add(mtycyDisc);
			}
		}		
		System.out.println("BOTH MTY,FULL ** cyDiscSumList.size() After -- "+cyDiscSumList.size());
		// PRINT SUMMARY
		for(int cy=0;cy<cyDiscSumList.size();cy++)
		{
			CyDiscSum cyDisc = cyDiscSumList.get(cy);
			System.out.println(cyDisc.getEmptyDesc() + "\t" + cyDisc.getEmptyAmt() + "\t" + cyDisc.getFullDesc() + "\t" + cyDisc.getFullAmt());
		}
	}

	public Object getFieldValue(JRField jrField) throws JRException {

		CyDiscSum cyDisc = (CyDiscSum) cyDiscSumList.get(index);
		if (cyDisc == null) {
			return null;
		}
		if (jrField.getName().equals("EmptyDesc")) {
			return cyDisc.getEmptyDesc();
		}
		if (jrField.getName().equals("EmptyAmt")) {
			return cyDisc.getEmptyAmt();
		}
		if (jrField.getName().equals("EmptyCode")) {
			return cyDisc.getEmptyCode();
		}
		if (jrField.getName().equals("EmptyToLine")) {
			return cyDisc.getEmptyToLine();
		}
		if (jrField.getName().equals("FullDesc")) {
			return cyDisc.getFullDesc();
		}
		if (jrField.getName().equals("FullAmt")) {
			return cyDisc.getFullAmt();
		}
		if (jrField.getName().equals("FullCode")) {
			return cyDisc.getFullCode();
		}
		if (jrField.getName().equals("FullToLine")) {
			return cyDisc.getFullToLine();
		}
		return null;

	}

	public boolean next() throws JRException {

		index++;
		if (cyDiscSumList != null && index < cyDiscSumList.size()) {
			return true;
		}
		return false;
	}
}

class CyDiscSum 
{
	private String emptyDesc;
	private String emptyAmt;
	private String emptyCode;
	private String emptyToLine;
	private String fullDesc;
	private String fullAmt;
	private String fullCode;
	private String fullToLine;
	public String getEmptyDesc() {
		return emptyDesc;
	}
	public void setEmptyDesc(String emptyDesc) {
		this.emptyDesc = emptyDesc;
	}
	public String getEmptyAmt() {
		return emptyAmt;
	}
	public void setEmptyAmt(String emptyAmt) {
		this.emptyAmt = emptyAmt;
	}
	public String getEmptyCode() {
		return emptyCode;
	}
	public void setEmptyCode(String emptyCode) {
		this.emptyCode = emptyCode;
	}
	public String getEmptyToLine() {
		return emptyToLine;
	}
	public void setEmptyToLine(String emptyToLine) {
		this.emptyToLine = emptyToLine;
	}
	public String getFullDesc() {
		return fullDesc;
	}
	public void setFullDesc(String fullDesc) {
		this.fullDesc = fullDesc;
	}
	public String getFullAmt() {
		return fullAmt;
	}
	public void setFullAmt(String fullAmt) {
		this.fullAmt = fullAmt;
	}
	public String getFullCode() {
		return fullCode;
	}
	public void setFullCode(String fullCode) {
		this.fullCode = fullCode;
	}
	public String getFullToLine() {
		return fullToLine;
	}
	public void setFullToLine(String fullToLine) {
		this.fullToLine = fullToLine;
	}

}