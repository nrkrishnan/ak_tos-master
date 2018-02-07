/*
************************************************************************************
* Srno   Date	    AuthorName  Change Description
* A1     04/07/10   GR          Added NULL and Empty Check In Insert Msg
*************************************************************************************
*/

package com.matson.tos.dao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import com.matson.cas.refdata.mapping.TosAcetsNewVesMsg;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;

/*
 * This Class Holds the Acets Messages(BDA,BDB,LNK) for Vesvoys between the window period
 * of the RDS trigger to the Actual Time the newves Enters N4.
 * These Acets MEssages are stored in a Database table.
 */
public class AcetsNewVesMsgDao {
	private static Logger logger = Logger.getLogger(AcetsNewVesMsgDao.class);
	private static String PORT ="HON";
	private static String PENDING = "PENDING";
	
	public static boolean insertMsg(String msgType,String acetsXmlMsg)
	{
	  boolean holdMsg = false;
	  Map parameterMap = null;
 	  try
 	  {
 		parameterMap = GroovyXmlUtil.getResponseMap(acetsXmlMsg);
		TosAcetsNewVesMsg tosAcetsMsgNewVes = new TosAcetsNewVesMsg();
		String vesvoy = (String)parameterMap.get("vesvoy");
		String equipId = (String)parameterMap.get("equipment-id");
		logger.debug(" insertMsg:-vesvoy :"+vesvoy);	
 		if((vesvoy != null && vesvoy.trim().length() > 0) && (equipId != null && equipId.trim().length() > 0)
 			&& (msgType != null && msgType.trim().length() > 0))
 	    { 
			tosAcetsMsgNewVes.setVesvoy(vesvoy);
			tosAcetsMsgNewVes.setCntrNbr(equipId);
			tosAcetsMsgNewVes.setMsgType(msgType);
			tosAcetsMsgNewVes.setMsgText(acetsXmlMsg);
			tosAcetsMsgNewVes.setStatus(PENDING);
			tosAcetsMsgNewVes.setCreatedDate(Calendar.getInstance(TimeZone.getTimeZone("America/Hawaii")).getTime());
			
  		   tosAcetsMsgNewVes.insertDetails();
		   holdMsg = true;
		   logger.debug(" insertMsg:-Ends :"+holdMsg);
 		}
 		
	  }catch(Exception e){
		e.printStackTrace();
	  }
      return holdMsg;
	}
	
	
	public static ArrayList sendNewVesAcetsMsg(String carrierId, Date rdsTriggerDtTime)
	{
		logger.debug("sendNewVesAcetsMsg Starts");
		List acetMsgLst = null;
		JMSSender sender = null;
		ArrayList msgList = null; 
		String emailAddr = null;
		String mailhost = null;
		try{
		  TosAcetsNewVesMsg tosAcetsMsgNewVes = new TosAcetsNewVesMsg();  	
		  acetMsgLst = tosAcetsMsgNewVes.getAcetsMsgForVesvoy(carrierId);
		  
		  if(acetMsgLst == null && acetMsgLst.size() == 0){
		     return null;
		  }
		  
		  sender = new JMSSender(JMSSender.REAL_TIME_QUEUE, PORT);
		  msgList = new ArrayList();
		  logger.debug("sendNewVesAcetsMsg Queue Size : "+acetMsgLst.size());
		  HashSet containerSet = new HashSet(); 
		  Iterator<TosAcetsNewVesMsg> iterator = acetMsgLst.iterator();
		  while ( iterator.hasNext() ){
			  TosAcetsNewVesMsg aAcetsMsg = iterator.next();
			  long msgId = aAcetsMsg.getMsgId();
			  try{
				  long createdDate =  aAcetsMsg.getCreatedDate()!= null ? aAcetsMsg.getCreatedDate().getTime() : 0;
				  long postedDate = rdsTriggerDtTime != null ? rdsTriggerDtTime.getTime() : 0;
				  /*Do not post Queued up messages before the RDS download */
				  logger.debug("msgId:"+msgId+" CreateDate:"+aAcetsMsg.getCreatedDate()+" PostedDate:"+rdsTriggerDtTime);
				  if(postedDate > createdDate){
                    logger.debug("Delete Msg before RDS Download :"+msgId+" CreateDate:"+aAcetsMsg.getCreatedDate()+" PostedDate:"+rdsTriggerDtTime);					  
					aAcetsMsg.deleteMessage(); 
					continue;  
				  }
				  //Wait till the first Msg for the same Unit is executed
				  if(containerSet.contains(aAcetsMsg.getCntrNbr())){
					 logger.debug("sendNewVesAcetsMsg same Cntr Msg Thread sleep :"+aAcetsMsg.getCntrNbr()+" Type:"+aAcetsMsg.getMsgType());
					 Thread.sleep(10000); 
				  }
				  logger.debug("MSG ID:"+aAcetsMsg.getMsgId()+" Newves("+aAcetsMsg.getVesvoy()+")Acets Msg Posted : "+aAcetsMsg.getMsgText());
				  //Post Acets Queue Msg
		          sender.send(aAcetsMsg.getMsgText());
		          //Set Cntr Nbr to Hold Message 
		          containerSet.add(aAcetsMsg.getCntrNbr());
		          //Set Msg POsted Information to Post Email 
		          msgList.add(aAcetsMsg.getCntrNbr()+" "+aAcetsMsg.getMsgType());
		          
			    }catch(Exception ex){
				  ex.printStackTrace();
				  emailAddr = emailAddr == null ? TosRefDataUtil.getValue( "SUPPORT_EMAIL") : emailAddr;
				  mailhost = mailhost == null ? TosRefDataUtil.getValue( "MAIL_HOST") : mailhost;
				  EmailSender.mailAttachment(emailAddr,emailAddr,mailhost,"AcetsNewvesMsgError.txt",aAcetsMsg.getMsgType(),ex.getMessage(),"AcetsNewves Message Submit error");
			    }
		      //Delete Msg from table
		      aAcetsMsg.deleteMessage();    		      
		      logger.debug("sendNewVesAcetsMsg Deleted Msg:"+msgId);
		  }
		}catch(Exception e){
			e.printStackTrace();
            //Add Error Emailing Code
			emailAddr = emailAddr == null ? TosRefDataUtil.getValue( "SUPPORT_EMAIL") : emailAddr;
			EmailSender.sendMail(emailAddr,emailAddr,"Newves Acets Queue messages Error","Error In Posting Acets Queued Messages for the Newves to run");
		}
		return msgList;
	}

}
