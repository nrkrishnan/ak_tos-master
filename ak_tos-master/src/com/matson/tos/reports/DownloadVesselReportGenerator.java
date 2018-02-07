package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.tos.dao.NewVesselDao;

public class DownloadVesselReportGenerator implements JRDataSource
{
	ArrayList<TosGumRdsDataFinalMt> jrDownloadVesselCellsDetail = new ArrayList<TosGumRdsDataFinalMt>();
	int index = -1;
	private static Logger logger = Logger.getLogger(DownloadVesselReportGenerator.class);
	
	public DownloadVesselReportGenerator(String vesvoy) 
	{
		if(vesvoy!=null && vesvoy.length()>=6)
			vesvoy = vesvoy.substring(0, 6);
		logger.info("vesvoy in DownloadVesselReportGenerator:::"+vesvoy);
		String buttonAction = "DownloadVessel";
		List<?> downloadVesselCellsList = NewVesselDao.getVVDInformation(vesvoy, null, null,buttonAction, null);
		logger.info("downloadVesselCellsList Size:::"+downloadVesselCellsList.size());
		if ((downloadVesselCellsList == null ) || (downloadVesselCellsList.size() <= 0)) 
		{ // no data 
			return;
		}
		for (int i = 0; i < downloadVesselCellsList.size(); i++) {
			TosGumRdsDataFinalMt jrTosGumRdsDataFinalMt = new TosGumRdsDataFinalMt();
			jrTosGumRdsDataFinalMt.setContainerNumber(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getContainerNumber());
			jrTosGumRdsDataFinalMt.setBookingNumber(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getBookingNumber());
			jrTosGumRdsDataFinalMt.setConsigneeName(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getConsigneeName());
			jrTosGumRdsDataFinalMt.setTruck(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getTruck());
			jrTosGumRdsDataFinalMt.setShipper(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getShipper());
			jrTosGumRdsDataFinalMt.setCargoNotes(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getCargoNotes());
			jrTosGumRdsDataFinalMt.setDport(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getDport());
			jrTosGumRdsDataFinalMt.setSealNumber(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getSealNumber());
			jrTosGumRdsDataFinalMt.setDs(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getDs());
			jrTosGumRdsDataFinalMt.setTypeCode(((TosGumRdsDataFinalMt)downloadVesselCellsList.get(i)).getTypeCode());
			jrDownloadVesselCellsDetail.add(jrTosGumRdsDataFinalMt);					
		}  
	}
	
		public Object getFieldValue(JRField jrField) throws JRException 
		{
		
			TosGumRdsDataFinalMt tosGumRdsDataFinalMt = (TosGumRdsDataFinalMt) jrDownloadVesselCellsDetail.get(index);
			//logger.info("jrField::Name::"+jrField.getName());
			
			if (jrDownloadVesselCellsDetail == null) {
				return null;
			}
			if (jrField.getName().equals("ContainerNumber")) {
				return tosGumRdsDataFinalMt.getContainerNumber();
			}
			if (jrField.getName().equals("BookingNumber")) {
				return tosGumRdsDataFinalMt.getBookingNumber();
			}	
			if (jrField.getName().equalsIgnoreCase("ConsigneeName")) {
				//logger.info("tosGumRdsDataFinalMt.getConsigneeName():::"+tosGumRdsDataFinalMt.getConsigneeName());
				//logger.info("tosGumRdsDataFinalMt.getConsignee:::"+tosGumRdsDataFinalMt.getConsignee());
				return tosGumRdsDataFinalMt.getConsigneeName();
			}	
			if (jrField.getName().equals("Truck")) {
				//logger.info("tosGumRdsDataFinalMt.getTruck:::"+tosGumRdsDataFinalMt.getTruck());
				return tosGumRdsDataFinalMt.getTruck();
			}	
			if (jrField.getName().equals("Shipper")) {
				return tosGumRdsDataFinalMt.getShipper();
			}	
			if (jrField.getName().equals("Remarks")) {
				return tosGumRdsDataFinalMt.getCargoNotes();
			}	
			if (jrField.getName().equals("DPort")) {
				//logger.info("jrField.getName():::"+jrField.getName());
				return tosGumRdsDataFinalMt.getDport();
			}
			if (jrField.getName().equals("Seal")) {
				return tosGumRdsDataFinalMt.getSealNumber();
			}
			if (jrField.getName().equals("Ds")) {
				return tosGumRdsDataFinalMt.getDs();
			}
			if (jrField.getName().equals("TypeCode")) {
				return tosGumRdsDataFinalMt.getTypeCode();
			}
			return null;
		
	}

	public boolean next() throws JRException 
	{
		
		index++;
		if (jrDownloadVesselCellsDetail != null && index < jrDownloadVesselCellsDetail.size()) {
			return true;
		}
		return false;
	}
}
