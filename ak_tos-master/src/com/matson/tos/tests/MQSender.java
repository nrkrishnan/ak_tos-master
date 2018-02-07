package com.matson.tos.tests;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.matson.tos.processor.MdbAcetsMessageProcessor;
import java.util.ArrayList;

public class MQSender {
	private static Logger logger = Logger.getLogger(MQSender.class);

	/**
	 * @param args
	 */
	public void send( String text) {
		MQQueueManager qMgr = null;
	   
		/*DEV */
		
		MQEnvironment.hostname = "10.8.7.145"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "TOS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
	    MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
	    String queueManagerName = "UNXDEV1"; //TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
	    String queueName = "ACETS.TOS.N4.FORMATTED"; //TosRefDataUtil.getValue( "MQ_QUEUE_NAME");
	    
          
		
		/* QA */
		/*
		MQEnvironment.hostname = "10.201.0.6"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "TOS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
	    MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
	    String queueManagerName = "SUNDEV2"; //TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
	    String queueName = "ACETS.TOS.N4.FORMATTED.QL"; //TosRefDataUtil.getValue( "MQ_QUEUE_NAME");
	   */
		
		/* Preprod  */
		/* 
	    MQEnvironment.hostname = "10.201.2.6"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "TOS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
	    MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
	    String queueManagerName = "SUNDEV1"; //TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
	    String queueName = "ACETS.TOS.N4.FORMATTED.QL"; //TosRefDataUtil.getValue( "MQ_QUEUE_NAME");
	  	 */  
	    
	    /*
		MQEnvironment.hostname = "10.201.1.4"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "TOS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
	    MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
	    String queueManagerName = "SUNPROD1"; //TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
	    String queueName = "ACETS.TOS.N4.FORMATTED"; //TosRefDataUtil.getValue( "MQ_QUEUE_NAME");
   */
	    
	    
	    MQQueue queue = null;
	    int depth = 0;
	    try {
	    	qMgr = new MQQueueManager( queueManagerName);
	    	int openOptions = MQC.MQOO_INPUT_AS_Q_DEF + MQC.MQOO_OUTPUT;
	    	queue = qMgr.accessQueue(queueName, // queue name
	    			openOptions, // open options
	    			queueManagerName, // queue manager
	    			null, // dynamic q name
	    			null // alternate user id
              	);
	    	
			MQMessage mqMessage = new MQMessage();
			mqMessage.writeString( text);
			MQPutMessageOptions pmo = new MQPutMessageOptions(); // accept the defaults,

			
			queue.put(mqMessage, pmo);
			
			logger.debug( "Send MQ message: " + text);
	    	
	    } catch ( IOException ioe) {
	    	logger.error("Error while reading MQ. ", ioe);
	    } catch (MQException mqe) {
	    	logger.debug( "An MQSeries error occurred : Completion code " + mqe.completionCode + " Reason code: " + mqe.reasonCode);        
	    	if (mqe.completionCode == 2 && mqe.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
	            if (depth > 0) {
	            	logger.debug( "All messages read.");
	            }
	        } else {
	            logger.error("Error while reading MQ. ", mqe);
	        }
	    } finally {
	    	logger.debug( "disconecting the queue manager.");
	        try {
	        	queue.close();
	        	qMgr.disconnect();
	        } catch ( Exception ex) { }
	    }

	}
	
	public  void receive() throws Exception{
		MQQueueManager qMgr = null;
		   
		/*DEV */
		
		MQEnvironment.hostname = "10.8.7.145"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "CUSTOMS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
	    MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
	    String queueManagerName = "UNXDEV1"; //TosRefDataUtil.getValue( "MQ_QUEUE_MGR_NAME");
	    String queueName = "ACETS.TOS.N4.FORMATTED"; //TosRefDataUtil.getValue( "MQ_QUEUE_NAME");

	qMgr = new MQQueueManager( queueManagerName);
	int openOptions = MQC.MQOO_INPUT_SHARED | MQC.MQOO_INQUIRE ;
	MQGetMessageOptions gmo = new MQGetMessageOptions();
	gmo.options = MQC.MQGMO_CONVERT | MQC.MQGMO_NO_WAIT;
	  MQQueue queue = null;
	queue = qMgr.accessQueue(queueName, // queue name
			openOptions, // open options
			queueManagerName, // queue manager
			null, // dynamic q name
			null // alternate user id
      	);
	String msg = null;
	for(int i=0;i<1000;i++) {
		MQMessage mqMessage = new MQMessage();
		//mqMessage.characterSet
		queue.get(mqMessage,gmo);
		byte[] b = new byte[mqMessage.getMessageLength()];
		mqMessage.readFully(b);
		msg = new String(b);
		logger.debug( "Received new MQ message: " + msg);
		System.out.println("Got message");
	    mqMessage.clearMessage();
	}
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		MQSender sender = new MQSender();
		//FileInputStream f = new FileInputStream(args[0]);
		//DataInputStream r = new DataInputStream(f);
		
		 String msg = "ABDB¦09/22/09¦15:05:02¦NEQR2¦046062161¦MATU226727 ¦5868540          ¦MHI¦185¦W¦HON ¦HIL ¦LAX ¦UNIVERSAL TRANSPORTATION SERVICE¦UNIVERSAL TRANSPORTATION SERVICE¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦0¦025039400¦025039400¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦";
		msg = "ABDB¦09/22/09¦15:05:03¦NEQR2¦056062161¦MATU226727 ¦8623205          ¦MHI¦185¦W¦HON ¦HIL ¦LAX ¦UNIVERSAL TRANSPORTATION SERVICE¦UNIVERSAL TRANSPORTATION SERVICE¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦0¦025039400¦025039400¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦";
		 //  msg = "ATRU¦04/06/11¦16:05:22¦NEQOL¦000000000¦TAKG¦T & A TRUCKING SERVICES, INC.                     ¦MAT  ¦ ¦SANFORD ANTWAN                  ¦(201)669-5410";
		   sender.send( msg);
		   System.out.println("MST SENT5 ::");

		   
	}

}

