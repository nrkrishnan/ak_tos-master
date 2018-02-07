package com.matson.tos.processor;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.matson.cas.refdata.mapping.TosNoFaxConsigneeMt;
import com.matson.tos.dao.NoFaxConsigneeDao;

/*
 * 	Srno   	Date			AuthorName			Change Description
 * 	A1     	10/14/2014		Raghu Iyer			Initial creation
 */

public class NoFaxConsigneeProcessor 
{
	private static Logger logger = Logger.getLogger(NoFaxConsigneeProcessor.class);
	
	public ArrayList<TosNoFaxConsigneeMt> getConsigneeInformation(String phone,String buttonAction,String consigneeName)
	{
		logger.info("NoFaxConsigneeProcessor | getConsigneeInformation | Start ");
		ArrayList<TosNoFaxConsigneeMt> resultList = null;
		NoFaxConsigneeDao noFaxConsigneeDao = null;
		try
		{
			resultList = new ArrayList<TosNoFaxConsigneeMt>();
			noFaxConsigneeDao = new NoFaxConsigneeDao();
			resultList = noFaxConsigneeDao.getConsigneeInformation(phone,buttonAction,consigneeName);
			logger.info("Result Size:::::"+resultList.size());
			if(resultList!=null && !resultList.isEmpty() && resultList.size()>0)
			{
				return resultList;
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("ConsigneeTruckerProcessor | getVVDInformation | End ");
		return resultList;
	}
	
	public String updateConsigneeInformation(ArrayList<TosNoFaxConsigneeMt> updateList)
	{
		logger.info("NoFaxConsigneeProcessor | updateConsigneeInformation | Start ");
		String isUpdated = "";
		NoFaxConsigneeDao noFaxConsigneeDao = null;
		try
		{
			noFaxConsigneeDao = new NoFaxConsigneeDao(); 
			if(updateList!=null && !updateList.isEmpty())
			{
				isUpdated = noFaxConsigneeDao.updateConsigneeInfo(updateList);
				logger.info("NoFaxConsigneeProcessor | updateConsigneeInformation | isUpdated:: "+isUpdated);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("NoFaxConsigneeProcessor | updateConsigneeInformation | End ");
		return isUpdated;
	}	
	
	public String deleteConsigneeInformation(int conId)
	{
		logger.info("NoFaxConsigneeProcessor | deleteConsigneeInformation | Start ");
		String isDeleted = "";
		NoFaxConsigneeDao noFaxConsigneeDao = null;
		try
		{
			isDeleted = noFaxConsigneeDao.deleteNoFaxConsignee(conId);
			logger.info("NoFaxConsigneeProcessor | deleteConsigneeInformation | isDeleted:: "+isDeleted);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("NoFaxConsigneeProcessor | deleteConsigneeInformation | End ");
		return isDeleted;
	}		
		
}
