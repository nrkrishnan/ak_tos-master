package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.DcmConvert;
import com.matson.tos.jaxb.snx.THazard;
import com.matson.tos.jaxb.snx.THazardNumberType;
import com.matson.tos.util.DcmFormatter;
import com.matson.tos.util.StrUtil;
import com.matson.tos.util.UnitConversion;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A1  SKB	07/17/09	Added messages.
 * A2  SKB	07/24/09	Fixed secondary IMO.
 * A3  GR   10/03/09    Added Haz Item Nbr Type  
 * A4  GR   07/15/11    Set only 35 Char in Emergency contact as this is max Field size in N4 
 * @author skb
 *
 */
public class DcmLateMessageHandler  extends AbstractMessageHandler {
	
   private static Logger logger = Logger.getLogger(DcmLateMessageHandler.class);
   
   public DcmLateMessageHandler( String s1, String s2, String s3, int dir) throws TosException {
		super( s1, s2, s3, dir);
	}

  protected Object textObjToXmlObj(Object textObj) throws TosException 
  {
	Object retObj = null;
	
	try
	{  
	  DcmConvert dcmConvt = (DcmConvert)textObj;
	  THazard aHaz = new THazard();
	  
	  if ( StrUtil.trimQuotes( dcmConvt.getHazClass()).trim().length() > 0)
		  aHaz.setImdg( StrUtil.trimQuotes( dcmConvt.getHazClass()));
	  if ( StrUtil.trimQuotes( dcmConvt.getHazCode()).trim().length() > 0) {
		  //aHaz.setUn( new Short( StrUtil.trimQuotes( dcmConvt.getHazCode())));
		  aHaz.setUn(StrUtil.trimQuotes( dcmConvt.getHazCode()));
	  }
	  if ( StrUtil.trimQuotes( dcmConvt.getHazCodeType()).trim().length() > 0) {
	     aHaz.setHazNbrType(THazardNumberType.fromValue((StrUtil.trimQuotes(dcmConvt.getHazCodeType()))));
	  }

	  //Limited Quantity & Marine Pollutants  
	  if ( StrUtil.trimQuotes( dcmConvt.getRemarks()).trim().length() > 0){
		 String []remarks = StrUtil.trimQuotes( dcmConvt.getRemarks()).trim().split(",");
		 for(int i= 0; i<remarks.length; i++){
		   if(remarks[i].equalsIgnoreCase("MARINE POLLUTANTS")){
			   aHaz.setMarinePollutants("true"); 
		   }if(remarks[i].equalsIgnoreCase("LIMITED QUANTITY") || remarks[i].equalsIgnoreCase("LIMITED QTY") || (remarks[i].contains("LTD") && remarks[i].contains("QTY")) ){
			   aHaz.setLtdQtyFlag("true"); 
		   }
		 }
	  }
	  //Quantity & Package Type
	  try {
	  if ( StrUtil.trimQuotes( dcmConvt.getPack()).trim().length() > 0){
	    String temp = StrUtil.trimQuotes( dcmConvt.getPack()).trim();
	    if(temp.indexOf(" ") != -1){
		  String qty = temp.substring(0,temp.indexOf(" "));
	      if (qty.length()> 0 && hasNumeric(qty)) {
		   aHaz.setQuantity( BigInteger.valueOf( Integer.parseInt(qty)));
	      }
	       aHaz.setPackageType(temp.substring(temp.indexOf(" ")+1));
	    }else{
		   aHaz.setPackageType(temp);   
	    }
	 }
	  } catch (Exception e ){}
	//Flash Point - Celcius and Fahrenhit unit conversion
	try {
	if ( StrUtil.trimQuotes( dcmConvt.getFlashPoint()).trim().length() > 0){
		String flashPoint = StrUtil.trimQuotes( dcmConvt.getFlashPoint()).trim();
		if(flashPoint.endsWith("F")){
		  double flashpointFarh = UnitConversion.fahrenheitToCelsius(flashPoint.substring(0,flashPoint.length()-1));
		  logger.debug("FlashPoint convt Farh-Celcius :"+flashpointFarh);
		  aHaz.setFlashPoint(new BigDecimal(flashpointFarh).setScale(2, BigDecimal.ROUND_HALF_UP));
		}else{
		   aHaz.setFlashPoint( new BigDecimal(flashPoint.substring(0,flashPoint.length()-1)).setScale(2, BigDecimal.ROUND_HALF_UP));
		}
	}
	} catch (Exception e) {}
	
	if ( StrUtil.trimQuotes( dcmConvt.getTechName()).trim().length() > 0){
		String techName = DcmFormatter.replaceQuotes(StrUtil.trimQuotes( dcmConvt.getTechName()).trim());
		aHaz.setTechName(techName);
	}
	if ( StrUtil.trimQuotes( dcmConvt.getShippingName()).trim().length() > 0){
		String properName = DcmFormatter.replaceQuotes(StrUtil.trimQuotes( dcmConvt.getShippingName()).trim());
		aHaz.setProperName(properName);
	}
	if ( StrUtil.trimQuotes( dcmConvt.getEmergencyResponse()).trim().length() > 0){ 
		aHaz.setEmsResponseGuideNbr(StrUtil.trimQuotes(dcmConvt.getEmergencyResponse()));
	}

	if ( StrUtil.trimQuotes(dcmConvt.getGrossWeight()).trim().length() > 0){
		String weightStr = dcmConvt.getGrossWeight().trim();
		weightStr = stripNum(weightStr);
		
		String weightKg = UnitConversion.weightFromLBToKg(weightStr);
		aHaz.setWeightKg(new BigDecimal(weightKg)) ;
	}
		if (dcmConvt.getPackagingGrpMarks()!=null && !dcmConvt.getPackagingGrpMarks().isEmpty()
		&& StrUtil.trimQuotes(dcmConvt.getPackagingGrpMarks()).trim().length() > 0) {
			String packagingGrpMarks = StrUtil.trimQuotes(dcmConvt.getPackagingGrpMarks()).trim();
			if (packagingGrpMarks != null && !packagingGrpMarks.isEmpty()) {
				if (packagingGrpMarks.equalsIgnoreCase("1"))
					packagingGrpMarks = "I";
				if (packagingGrpMarks.equalsIgnoreCase("2"))
					packagingGrpMarks = "II";
				if (packagingGrpMarks.equalsIgnoreCase("3"))
					packagingGrpMarks = "III";
				if (packagingGrpMarks.trim().equalsIgnoreCase("I") ||
						packagingGrpMarks.trim().equalsIgnoreCase("II") ||
						packagingGrpMarks.trim().equalsIgnoreCase("III")) {
					packagingGrpMarks = packagingGrpMarks.trim();
					aHaz.setPackingGroup(packagingGrpMarks);
				}
			}
	}
	//IMO1 & IMO2
	if ( StrUtil.trimQuotes( dcmConvt.getHazSubClass()).trim().length() > 0){
	    String imoTemp = StrUtil.trimQuotes(dcmConvt.getHazSubClass());
	    //String imoValue = imoTemp.substring(0,imoTemp.length()-1);
	    String imoValue = imoTemp.trim();
	   	String []imoArray = imoValue.split("!");
	   	if(imoArray.length > 1){
		   aHaz.setSecondaryImo1(imoArray[0] );
		   aHaz.setSecondaryImo2(imoArray[1] );
	    }else{
	       aHaz.setSecondaryImo1(imoArray[0] );
	    }
	}
    
    if ( StrUtil.trimQuotes( dcmConvt.getEmergencyContact()).trim().length() > 0){
    	String temp = StrUtil.trimQuotes( dcmConvt.getEmergencyContact()); //A4
		temp = temp.length() >= 35 ? temp.substring(0,34) : temp;
		aHaz.setEmergencyTelephone(temp);
    }
    
    retObj = aHaz; 
	}
	catch(Exception e){
		//e.printStackTrace();
		logger.error("DcmLateMessageHandler.textObjToXmlObj()error:",e);
		throw new TosException(""+e);
	}
	 return retObj; 
	
	}
	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void setTextStr( String text) throws TosException {
		_textStr = DcmFormatter.modifyData(text);
		createTextObj();
		_xmlObj = null;
	}
	
	private boolean hasNumeric(String strValue){
      boolean numFlag = false;
      for (int j = 0;j < strValue.length();j++){ 
 		if (Character.isDigit(strValue.charAt(j))){
 	      numFlag=true;
 	    }else{
 		  numFlag=false;
 		}
      }
     return numFlag;
    }
	
	private String stripNum(String strValue){
	      String num = "";
	      for (int j = 0;j < strValue.length();j++){ 
	 		if (Character.isDigit(strValue.charAt(j)) || ".".equals(strValue.charAt(j))){
	 	      num += strValue.charAt(j);
	 	    }
	      }
	     return num;
	    }
	
}
