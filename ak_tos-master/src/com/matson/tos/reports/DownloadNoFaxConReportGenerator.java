package com.matson.tos.reports;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import com.matson.cas.refdata.mapping.TosNoFaxConsigneeMt;
import com.matson.tos.dao.NoFaxConsigneeDao;

/*
 * 	Srno   	Date			AuthorName			Change Description
 * 	A1     	10/14/2014		Raghu Iyer			Initial creation
 */
public class DownloadNoFaxConReportGenerator implements JRDataSource
{
	ArrayList<TosNoFaxConsigneeMt> jrdownloadConsigneeDetail = new ArrayList<TosNoFaxConsigneeMt>();
	int index = -1;
	private static Logger logger = Logger.getLogger(DownloadNoFaxConReportGenerator.class);
	
	public DownloadNoFaxConReportGenerator(String consignee, String phone) 
	{

		logger.info("Consignee in DownloadNoFaxConReportGenerator:::"+consignee);
		String buttonAction = "Save & Email Report";
		List<?> downloadConsigneeList = NoFaxConsigneeDao.getConsigneeInformation(phone,buttonAction,consignee);
		logger.info("downloadConsigneeList Size:::"+downloadConsigneeList.size());
		if ((downloadConsigneeList == null ) || (downloadConsigneeList.size() <= 0)) 
		{ // no data 
			return;
		}
		for (int i = 0; i < downloadConsigneeList.size(); i++) {
			TosNoFaxConsigneeMt jrTosNoFaxConsigneeMt = new TosNoFaxConsigneeMt();
			jrTosNoFaxConsigneeMt.setConsigneeName(((TosNoFaxConsigneeMt)downloadConsigneeList.get(i)).getConsigneeName());
			jrTosNoFaxConsigneeMt.setPhone(((TosNoFaxConsigneeMt)downloadConsigneeList.get(i)).getPhone());
			jrTosNoFaxConsigneeMt.setType(((TosNoFaxConsigneeMt)downloadConsigneeList.get(i)).getType());
			jrTosNoFaxConsigneeMt.setSpeed(((TosNoFaxConsigneeMt)downloadConsigneeList.get(i)).getSpeed());
			jrdownloadConsigneeDetail.add(jrTosNoFaxConsigneeMt);					
		}  
	}
	
		public Object getFieldValue(JRField jrField) throws JRException 
		{
		
			TosNoFaxConsigneeMt tosNoFaxConsigneeMt = (TosNoFaxConsigneeMt) jrdownloadConsigneeDetail.get(index);
			//logger.info("jrField::Name::"+jrField.getName());
			
			if (jrdownloadConsigneeDetail == null) {
				return null;
			}
			if (jrField.getName().equals("Consignee")) {
				return tosNoFaxConsigneeMt.getConsigneeName();
			}
			if (jrField.getName().equals("PhoneNumber")) {
				return tosNoFaxConsigneeMt.getPhone();
			}	
			if (jrField.getName().equalsIgnoreCase("Type")) {
				return tosNoFaxConsigneeMt.getType();
			}	
			if (jrField.getName().equalsIgnoreCase("Speed")) {
				return tosNoFaxConsigneeMt.getSpeed();
			}			
			return null;
		
	}

	public boolean next() throws JRException 
	{
		
		index++;
		if (jrdownloadConsigneeDetail != null && index < jrdownloadConsigneeDetail.size()) {
			return true;
		}
		return false;
	}
}
