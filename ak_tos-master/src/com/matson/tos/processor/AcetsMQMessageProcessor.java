package com.matson.tos.processor;

import java.io.IOException;

import org.apache.log4j.Logger;
import com.ibm.mq.*;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;

/**
 * Pools MQ for Acets messages
 * Change History 
 * A1  SKB  2/27/2009   Added error email
 * A2  SKB  9/03/2009	Limit the number of records in a single run.
 * A3  GR   9/03/2009   Error Mailing condition for long down time Period #102578
 * A4  GR   10/12/10    Updated Processor Class
 * @author xw8
 *
 */
public class AcetsMQMessageProcessor {
	private static Logger logger = Logger.getLogger(AcetsMQMessageProcessor.class);
	private static int RECORD_MAX = 2000;
	
	public void processMsg() {
		String supportMail = "1sn4support@matson.com";
		String acetsMail = "1sn4support@matson.com";
		MQQueueManager qMgr = null;
		MQQueue queue = null;
		
		try {
			MQEnvironment.hostname = TosRefDataUtil.getValue( "MQ_HOSTNAME");
			MQEnvironment.channel = TosRefDataUtil.getValue( "MQ_CHANNEL");
			MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
			String queueManagerName = TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
			String queueName = TosRefDataUtil.getValue( "MQ_QUEUE_NAME");
			supportMail = TosRefDataUtil.getValue("SUPPORT_EMAIL");
			acetsMail = TosRefDataUtil.getValue("ACETS_ERROR_EMAIL");
			
	    
	    	qMgr = new MQQueueManager( queueManagerName);
	    	int openOptions = MQC.MQOO_INPUT_SHARED | MQC.MQOO_INQUIRE ;
	    	MQGetMessageOptions gmo = new MQGetMessageOptions();
	    	gmo.options = MQC.MQGMO_CONVERT | MQC.MQGMO_NO_WAIT;

	    	queue = qMgr.accessQueue(queueName, // queue name
	    			openOptions, // open options
	    			queueManagerName, // queue manager
	    			null, // dynamic q name
	    			null // alternate user id
              	);
	    	String msg = null;
	    	AcetsMessageProcessor proc = new AcetsMessageProcessor(); //A4
    		for(int i=0;i<RECORD_MAX;i++) {
    			MQMessage mqMessage = new MQMessage();
    			//mqMessage.characterSet
    			queue.get(mqMessage,gmo);
    			byte[] b = new byte[mqMessage.getMessageLength()];
    			mqMessage.readFully(b);
    			//byte[] utf8 = new String(b, "ISO-8859-1").getBytes("UTF-8");
    			//msg = new String(utf8);
    			msg = new String(b, "ISO-8859-1");
    			logger.debug( "Received new MQ message: " + msg);
    			proc.processMsg( msg,"MQ");
    		    mqMessage.clearMessage();
    		}
	    } catch ( IOException ioe) {
	    	logger.error("Error while reading MQ. ", ioe);
	    	EmailSender.sendMail(supportMail, acetsMail, "TDP MQException for Acets" , "IOException is:\n"+ioe.getMessage());
	    } catch (MQException mqe) {
	    	//logger.debug( "An MQSeries error occurred : Completion code " + mqe.completionCode + " Reason code: " + mqe.reasonCode);        
	    	if (mqe.completionCode == 2 && mqe.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
	            //logger.debug( "All messages read.");
	        } else {
	            logger.error("Error while reading MQ. ", mqe);
	            EmailSender.sendMail(supportMail, acetsMail, "TDP MQException for Acets" , "MQException is:\n"+mqe.getMessage());
	        }
	    } finally {
	    	//logger.debug( "disconecting the queue manager.");
	        try {
	        	queue.close();
	        	qMgr.disconnect();
	        } catch ( Exception ex) { }
	    }

	}
	
}
