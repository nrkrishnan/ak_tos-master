package com.matson.tos.processor;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.matson.cas.refdata.mapping.TosConsgineeTrucker;
import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.gems.api.GEMSInterface;
import com.matson.gems.api.carrier.GEMSCarrier;
import com.matson.gems.api.results.GEMSResults;
import com.matson.tos.dao.NewVesselDao;


public class ConsigneeTruckerProcessor 
{
	private static Logger logger = Logger.getLogger(ConsigneeTruckerProcessor.class);
	
	public ArrayList<TosConsgineeTrucker> getConsigneeInfo(String consigneeName)
	{
		logger.info("ConsigneeTruckerProcessor | getConsigneeInfo | Start ");
		ArrayList<TosConsgineeTrucker> resultList = null;
		NewVesselDao newVesselDao = null;
		try
		{
			resultList = new ArrayList<TosConsgineeTrucker>();
			newVesselDao = new NewVesselDao();
			resultList = newVesselDao.getConsigneeInformation(consigneeName);
			//logger.info("Result Size:::::"+resultList.size());
			if(resultList!=null && !resultList.isEmpty() && resultList.size()>0)
			{
				return resultList;
			}
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("ConsigneeTruckerProcessor | getConsigneeInfo | End ");
		return resultList;
	}
	
	public ArrayList<TosGumRdsDataFinalMt> getVVDInformation(String vesvoy,String containerNumber,String bookingNumber,String buttonAction,String consigneeName)
	{
		logger.info("ConsigneeTruckerProcessor | getVVDInformation | Start ");
		ArrayList<TosGumRdsDataFinalMt> resultList = null;
		NewVesselDao newVesselDao = null;
		try
		{
			resultList = new ArrayList<TosGumRdsDataFinalMt>();
			newVesselDao = new NewVesselDao();
			resultList = newVesselDao.getVVDInformation(vesvoy,containerNumber,bookingNumber,buttonAction,consigneeName);
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
	
	
	public String updateTruckerInfo(ArrayList<TosConsgineeTrucker> updateList)
	{
		logger.info("ConsigneeTruckerProcessor | updateTruckerInfo | Start ");
		String isUpdated = "";
		NewVesselDao newVesselDao = null;
		try
		{
			newVesselDao = new NewVesselDao(); 
			if(updateList!=null && !updateList.isEmpty())
			{
				isUpdated = newVesselDao.updateConsigneeInformation(updateList);
				logger.info("ConsigneeTruckerProcessor | updateTruckerInfo | isUpdated:: "+isUpdated);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("ConsigneeTruckerProcessor | updateTruckerInfo | End ");
		return isUpdated;
	}
	
	public String updateVVDInformation(ArrayList<TosGumRdsDataFinalMt> updateList)
	{
		logger.info("ConsigneeTruckerProcessor | updateVVDInformation | Start ");
		String isUpdated = "";
		NewVesselDao newVesselDao = null;
		try
		{
			newVesselDao = new NewVesselDao(); 
			if(updateList!=null && !updateList.isEmpty())
			{
				isUpdated = newVesselDao.updateVVDInformation(updateList);
				logger.info("ConsigneeTruckerProcessor | updateVVDInformation | isUpdated:: "+isUpdated);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logger.info("ConsigneeTruckerProcessor | updateVVDInformation | End ");
		return isUpdated;
	}
	
	/**
	 * This method is used to validate the trucker entered with the trucker code in GEMS
	 * @param carrierCode
	 * @return
	 */
	public static GEMSCarrier validateCarrierCodeWithGems(String carrierCode) 
	{
		logger.info("validateCarrierCodeWithGems begin :" + carrierCode);
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/gems-app-context.xml");
		GEMSInterface gemsInterface = (GEMSInterface) ctx.getBean("gemsInterface");
		GEMSCarrier gemsCarrier = null;
		try 
		{
			if(carrierCode!=null && !carrierCode.equals(""))
			{
				carrierCode = carrierCode.toUpperCase();
				GEMSResults result = gemsInterface.getCarrierByCode(carrierCode);
				if (result != null && result.getResults().size() > 0) 
				{
					logger.info("result size is :" + result.getResults().size());
					for (int k = 0; k < result.getResults().size(); k++) 
					{
						gemsCarrier = (GEMSCarrier) result.getResults().get(k);
						if (gemsCarrier==null) 
						{
							logger.info("No Carrier Code from GEMS");
						}
					}
				}
				else
				{
					logger.info("Result is null from GEMS");
				}
			}
		} 
		catch (Exception ex) 
		{
			logger.error("validateCarrierCodeWithGems exception :" + ex);
			ex.printStackTrace();
		}
		logger.info("validateCarrierCodeWithGems end");
		return gemsCarrier;
	}
	
	
}
