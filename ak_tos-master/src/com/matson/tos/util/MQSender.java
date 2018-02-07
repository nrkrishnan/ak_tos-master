package com.matson.tos.util;

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

public class MQSender {
	private static Logger logger = Logger.getLogger(MQSender.class);

	/**
	 * @param args
	 */
	public void send( String text) {
		MQQueueManager qMgr = null;
	   
		/*DEV */
		
		MQEnvironment.hostname = "10.8.7.145"; //TosRefDataUtil.getValue( "MQ_HOSTNAME");
	    MQEnvironment.channel = "CUSTOMS.SVRCON"; //TosRefDataUtil.getValue( "MQ_CHANNEL");
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
		
		//String msg = "ABOB¦04/03/09¦11:16:03¦NEQV2¦062526451¦MAT¦4543050          ¦        ¦B¦HFM FOODSERVICE                    ¦MLI¦050¦GUM ¦MAJ ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                        ITN YES   ¦HFMFOO¦W¦                                ¦   ¦             ¦FOODSTUFFS                       ¦MAT ¦M¦F¦                    ¦N¦N¦N¦N¦HOME GARDEN                     ¦058306800¦083663600¦HFM FOODSERVICE                                   ¦                                        ¦HOME GARDEN                                       ¦                                        ¦036310000¦058306800¦015668800¦083663600¦0001¦D0020 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
		String msg = "AARD¦04/01/09¦20:56:48¦NEQAR¦075520451¦FSCU965169 ¦8¦HON ¦JNK¦903¦A¦01/01/01¦AUT¦   ¦        ¦";
		//String msg = "AULK¦04/15/09¦16:22:27¦NEQR2¦015561151¦MATU687240 ¦$UTISH  ¦7";
		msg = "ABOB¦04/21/09¦15:41:21¦NEQV2¦054578651¦MAT¦6556064          ¦        ¦B¦AIR LIQUIDE AMERICA CORPORATION    ¦MAU¦722¦SEA ¦PDX ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦AIRLIQ¦E¦                                ¦   ¦             ¦GET WB TRLU629761-3      ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦AIR LIQUIDE AMERICA CORPORATION ¦000001325¦000001325¦AIR LIQUIDE AMERICA CORPORATION                   ¦                                        ¦AIR LIQUIDE AMERICA CORPORATION                   ¦                                        ¦000001687¦000001325¦000018246¦000001325¦0001¦D0040 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦ITN_YES¦Y¦";
		//
		
		
		//msg = "ALNK¦04/03/09¦11:26:50¦NEQR2¦040526551¦MATU246551 ¦1181161          ¦$ULWEI  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦65119900¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦1¦MKA¦142¦W¦OAK ¦OAK ¦HON ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		//msg = "ALNK¦04/03/09¦11:26:50¦NEQR2¦040526551¦MATU687240 ¦1181161          ¦$ULWEI  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦65119900¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦1¦MKA¦142¦W¦HON ¦UUK ¦HON ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		
		
		//msg = "ALNK¦04/03/09¦11:26:50¦NEQR2¦040526551¦MATU246062 ¦2023728          ¦$ULWEI  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦65119900¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦8¦MKA¦142¦W¦OAK ¦OAK ¦HON ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		
		//msg = "ALNK¦04/03/09¦11:26:50¦NEQR2¦040526551¦MATU453451 ¦1134711          ¦$ULWEI  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦65119900¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦9¦MNA¦163¦W¦SEA ¦PDX ¦HON ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		
		//msg = "ALNK¦04/03/09¦11:26:50¦NEQR2¦040526551¦GATU005950 ¦6791215          ¦$ULWEI  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦65119900¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦1¦HAL¦915¦C¦KAH ¦KAH ¦HON ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		
		
		//msg = "ABOB¦04/16/09¦11:41:35¦NEQV2¦008563251¦MAT¦9519834          ¦        ¦B¦BETTER BRANDS LTD                  ¦HAL¦917¦KAH ¦KAH ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦BETBRA¦C¦                                ¦   ¦             ¦                                 ¦MAT ¦H¦E¦                    ¦N¦N¦N¦N¦BETTER BRANDS LTD               ¦000001282¦000001282¦BETTER BRANDS LTD                                 ¦                                        ¦BETTER BRANDS LTD                                 ¦                                        ¦000001638¦000001282¦053030000¦000001282¦0001¦D0040 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
		msg = "ABDA¦04/20/09¦10:13:25¦NEQR2¦023573151¦MLKU185002 ¦7137894          ¦MHI¦174¦W¦NAW ¦HIL ¦LAX ¦     ¦ ¦647613    ¦                                ¦                                ¦                                 ¦$UDSCI  ¦2¦000008490¦000006450¦BASF CONST CHEMICALS ADMIXTURES                   ¦DEGUSSA CONST CHEM OPERTIONS INC        ¦BASF CONST CHEMICALS ADMIXTURES¦T¦T¦";
		//msg = "ABDB¦04/28/09¦08:43:39¦NEQR2¦005597451¦MATU245374 ¦7959988          ¦MKA¦150¦E¦LAX ¦LAX ¦HON ¦ROYAL HAWAIIAN MOVERS INC       ¦SOUTHWEST PORT SERVICES INC     ¦$UCBUC  ¦MAT ¦H¦Y¦N¦N¦N¦2¦000008812¦000008812¦ROYAL HAWAIIAN MOVERS INC                         ¦                                        ¦ROYAL HAWAIIAN MOVERS INC                         ¦                   ";
		//msg = "ALNK¦05/14/09¦11:26:50¦NEQR2¦9600928010¦MATU687525 ¦7416114          ¦$ULWEI  ¦NELSON & SONS INC    ¦N¦P  ¦CY ¦N¦CAROL                           ¦808¦      ¦877¦    ¦2822¦    ¦MAT ¦H¦V¦00003551¦                                ¦NELSON & SONS INC ¦65119900¦                                ¦                    ¦NELSON & SONS INC ¦                                ¦                                ¦475 HUKILIKE STREET             ¦    ¦KAHULUI MAUI             ¦HI¦US               ¦96732    ¦                    ¦6¦HAL¦920¦A¦NAW ¦NAW ¦HON ¦0000013105¦0000013105¦NELSON & SONS INC                   ¦                                        ¦NELSON & SONS INC                   ¦                                        ¦";
		
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU207595 ¦6555354          ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦6¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
	//MATU2070240
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU207024 ¦6364073          ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦0¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦TRLU655018 ¦3417358          ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦8¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦TCNU935712 ¦5898353000       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦3¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU512522 ¦1584435       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦2¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU511028 ¦2668977000       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦8¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU512952 ¦6585706000       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦6¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦HLXU677233 ¦N1233211         ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦8¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU687525 ¦7416114       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦8¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU453330 ¦4150755       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦1¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		
		
		//msg = "ABDB¦05/19/09¦08:04:22¦NEQR2¦065659851¦MATU453553 ¦3971697004       ¦MHI¦176¦W¦HON ¦NAW ¦LAX ¦HAWAIIAN OCEAN TRANSPORT INC    ¦HAWAIIAN OCEAN TRANSPORT INC    ¦$ULWEI  ¦MAT ¦H¦Y¦N¦N¦N¦6¦000004485¦000004485¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦HAWAIIAN OCEAN TRANSPORT INC                      ¦                                        ¦";
		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦TRLU807728 ¦1174933          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦5¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";
		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦TRLU807728 ¦1174933          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦5¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";
		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦MATU207319 ¦8216749          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦3¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";
		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦EADE900035 ¦1404493          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦9¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";
		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦MATU511319 ¦8687749          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦7¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";

		//msg = "ABDB¦05/13/09¦15:41:02¦NEQR2¦025645751¦TCNU908357 ¦1010453          ¦MHI¦176¦E¦OAK ¦OAK ¦HON ¦HAWAIIAN EXPRESS SERVICE INC    ¦HAWAIIAN EXPRESS SERVICE INC    ¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦3¦000004429¦000004429¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦HAWAIIAN EXPRESS SERVICE INC                      ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU251695 ¦4144257          ¦MLI¦051¦W¦SHA ¦HKG ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦9¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU251695 ¦4144257          ¦MLI¦051¦W¦SHA ¦NGB ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦9¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦TTNU394866 ¦4495510          ¦MLI¦051¦W¦HON ¦HON ¦LAX ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦7¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU370140 ¦8122089          ¦MLI¦051¦W¦HON ¦HON ¦LAX ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦0¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦15:12:21¦NEQR2¦019642051¦MATU207001 ¦4980651          ¦MKA¦172¦E¦SHA ¦HKG ¦HON ¦THE HOME DEPOT INC              ¦MS INTERNATIONAL INC            ¦$UCCOU  ¦MAT ¦H¦N¦N¦N¦N¦8¦097826700¦000007031¦THE HOME DEPOT INC                                ¦#1705 (USING DHX OCEAN)                 ¦MS INTERNATIONAL INC                              ¦                                        ¦";
		
		//msg = "ABDB¦05/12/09¦15:12:21¦NEQR2¦019642051¦TRLU807727 ¦9191191          ¦MKA¦172¦E¦SHA ¦HKG ¦HON ¦THE HOME DEPOT INC              ¦MS INTERNATIONAL INC            ¦$UCCOU  ¦MAT ¦H¦N¦N¦N¦N¦0¦097826700¦000007031¦THE HOME DEPOT INC                                ¦#1705 (USING DHX OCEAN)                 ¦MS INTERNATIONAL INC                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU251358 ¦6662841          ¦MLI¦051¦W¦HON ¦HON ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦7¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦CRXU527398 ¦4497280          ¦MLI¦051¦W¦HON ¦HON ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦4¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU454949 ¦7954023          ¦MLI¦051¦W¦HON ¦HON ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦0¦001154810¦011154810¦MAC FARMS OF HAWAII                               ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		//msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU454949 ¦7954023          ¦MLI¦051¦W¦HON ¦HON ¦HON ¦Styeve HAWAII             ¦STEVE CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦0¦321154810¦31154810¦Styeve HAWAII                               ¦                                        ¦Steve TRADING CO. LTD.                              ¦                                        ¦";
		
		//  Really long message -- msg = "ABDB¦05/12/09¦08:29:51¦NEQR2¦026639651¦MATU45494a ¦7954023          ¦MLI¦051¦W¦HON ¦HON ¦HON ¦MAC FARMS OF HAWAII             ¦CHK TRADING CO. LTD.            ¦$UMJEN  ¦MAT ¦F¦N¦N¦N¦N¦0¦001154810¦011154810¦MAC FARMS OF HAWAII Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111Test                                                             f                                                                                                                                                                                                                                      f                                                                                                                                 11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111                              ¦                                        ¦CHK TRADING CO. LTD.                              ¦                                        ¦";
		
		//msg = "AARD¦04/01/09¦20:56:48¦NEQAR¦075520451¦FSCU965169 ¦8¦HON ¦HAL¦921¦B¦01/01/01¦AUT¦   ¦        ¦";
		
		//msg = "AUP1¦05/26/09¦17:10:54¦NEQR2¦099682051¦GLDU222675  ¦MTY ¦APL ¦05/26/09¦D40 86XX¦                                                  ¦1¦APLU¦HSD¦APLU";
		//msg = "ACAR¦05/29/09¦10:58:13¦NEQR2¦065691751¦MATU250118 ¦3¦F¦    ¦SEA ¦                                ¦                                ¦MNA¦173¦E¦AUT¦          ¦       ¦12485¦     ¦ ¦Y¦ ¦ULD   ¦HON ¦20090529¦075800¦CARS    ¦                 ¦          ¦SEA ¦1 AUTO                           ¦MNA¦173¦E¦HON ¦   ¦                                                                              ¦000¦000¦000¦000¦000";
		//msg = "ACAR¦05/29/09¦10:58:13¦NEQR2¦065691751¦MATU250118 ¦3¦F¦    ¦SEA ¦                                ¦                                ¦MNA¦173¦E¦AUT¦          ¦       ¦12485¦     ¦ ¦Y¦ ¦LDA   ¦HON ¦20090529¦075800¦CARS    ¦                 ¦          ¦SEA ¦1 AUTO                           ¦MNA¦173¦E¦HON ¦   ¦                                                                              ¦000¦000¦000¦000¦000";
		
		//for(int i=0;i<1;i++) 
		
		//msg = "ABOB¦06/24/09¦07:24:12¦NEQV2¦090769551¦MAT¦3981119          ¦        ¦B¦UTC OVERSEAS                       ¦MHI¦179¦LAX ¦HOUS¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦Y¦000¦CP Debtor not chosen for Booking                  ¦UTCOVE¦E¦                                ¦   ¦             ¦HAZ CLASS 4.2/UN3190 IN DRUMS    ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦UTC OVERSEAS                    ¦012188810¦022188810¦UTC OVERSEAS                                      ¦                                        ¦UTC OVERSEAS                                      ¦                                        ¦062200110¦012188810¦072200110¦022188810¦0001¦D0020 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦XX  ¦XX        ¦     ¦   ¦               ¦          ¦       ¦N¦";
		//msg = "ABDA¦08/03/09¦12:15:36¦NEQR2¦096887451¦FSCU981288 ¦6462712          ¦HAL¦932¦A¦NAW ¦NAW ¦HON ¦     ¦ ¦644156    ¦BETTER BRA                      ¦BETTER BRANDS LTD               ¦                                 ¦$UNAWG  ¦0¦000001282¦000001282¦BETTER BRANDS LTD                                 ¦                                        ¦BETTER BRANDS LTD                                 ¦                                        ¦";
		//msg = "ABDA¦08/10/09¦17:44:36¦NEQR2¦013912051¦MATU207806 ¦5237805          ¦MHI¦182¦E¦LAX ¦LAX ¦HON ¦     ¦ ¦334789    ¦ALL POINTS VAN LINES INC        ¦                                ¦HHGDS                            ¦$URPHI  ¦6¦044179210¦044179210¦ALL POINTS VAN LINES INC                          ¦                                        ¦ALL POINTS VAN LINES INC                          ¦                                        ¦";
		//msg = "ABDA¦08/13/09¦08:19:22¦NEQR2¦002920451¦MATU250143 ¦2811657          ¦MAU¦730¦W¦HON ¦NAW ¦SEA ¦     ¦ ¦895685    ¦HONOLULU F                      ¦HONOLULU FREIGHT SERVICE        ¦SIT-NAW                          ¦$UCJAC  ¦4¦000004713¦000004713¦HONOLULU FREIGHT SERVICE                          ¦                                        ¦HONOLULU FREIGHT SERVICE                          ¦                                        ¦";
		//msg = "ABDA¦08/13/09¦09:04:56¦NEQR2¦091920551¦MATU248742 ¦5025697          ¦MHI¦182¦W¦HON ¦KHI ¦LAX ¦     ¦ ¦ABF73222  ¦ABF CARTAG                      ¦ABF CARTAGE INC                 ¦SIT-KHI                          ¦$UCJAC  ¦3¦000000148¦000000148¦ABF CARTAGE INC                                   ¦                                        ¦ABF CARTAGE INC                                   ¦                                        ¦";
		//msg = "ABDA¦08/07/09¦14:00:46¦NEQR2¦056905251¦MATU248371 ¦9687071          ¦MKI¦098¦W¦SHA ¦DUMP¦HON ¦     ¦ ¦567260    ¦KOYO USA                        ¦                                ¦BOTTLED WATER                    ¦$ULLER  ¦0¦010880100¦024000110¦KOYO USA                                          ¦                                        ¦KOYO SHA                                          ¦                                        ¦";
		
		//MHI185
		//msg = "ALNK¦09/21/09¦08:41:17¦NEQR2¦048055761¦TEXU739332 ¦7884017          ¦$UCJAC  ¦PACIFIC TRANSPORTATION LINES INC   ¦N¦CY ¦CY ¦N¦ELLEN HIGA                      ¦808¦      ¦935¦    ¦0010¦    ¦MAT ¦H¦V¦00010282¦                                ¦PACIFIC TRANSPORTATION LINES INC¦00010274¦                                ¦                    ¦PACIFIC TRANSPORTATION LINES INC¦                                ¦                                ¦144 MAKAALA STREET              ¦    ¦HILO                     ¦HI¦US               ¦96720    ¦                    ¦0¦MHI¦185¦W¦HIL ¦HIL ¦OAK ¦000007828¦000007828¦PACIFIC TRANSPORTATION LINES INC                  ¦                                        ¦PACIFIC TRANSPORTATION LINES INC                  ¦                                        ¦";
		//msg = "ABDA¦09/21/09¦08:41:37¦NEQR2¦078055761¦TEXU739332 ¦7884017          ¦MHI¦185¦W¦HON ¦HIL ¦LAX ¦     ¦ ¦24825     ¦PACIFIC TRANSPORTATION LINES INC¦PACIFIC TRANSPORTATION LINES INC¦                                 ¦$UCJAC  ¦0¦000007828¦000007828¦PACIFIC TRANSPORTATION LINES INC                  ¦                                        ¦PACIFIC TRANSPORTATION LINES INC                  ¦                                        ¦";
		 //Departed unit 
		 msg = "ALNK¦09/21/09¦09:54:09¦NEQR2¦049056261¦MATU227893 ¦1530851          ¦$UCJAC  ¦DHX-DEPENDABLE HAWAIIAN EXPRESS    ¦N¦P  ¦CY ¦N¦                                ¦   ¦      ¦   ¦    ¦    ¦    ¦MAT ¦H¦V¦00003551¦                                ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦00088343¦                                ¦                    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦KAUAI FREIGHT SERVICE INC       ¦                                ¦2956 AUKELE STREET UNIT 103     ¦    ¦LIHUE                    ¦HI¦US               ¦96766    ¦                    ¦2¦MHI¦185¦W¦NAW ¦NAW ¦LAX ¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		//msg = "ABDA¦09/21/09¦09:54:34¦NEQR2¦001056361¦MATU227893 ¦1530851          ¦MHI¦185¦W¦HON ¦NAW ¦LAX ¦     ¦ ¦246571    ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦DHX-DEPENDABLE HAWAIIAN EXPRESS ¦                                 ¦$UCJAC  ¦2¦000002772¦000002772¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦DHX-DEPENDABLE HAWAIIAN EXPRESS                   ¦                                        ¦";
		 //Deleted Unit
		 msg = "ABDB¦09/22/09¦15:05:02¦NEQR2¦046062161¦MATU226727 ¦5868540          ¦MHI¦185¦W¦HON ¦HIL ¦LAX ¦UNIVERSAL TRANSPORTATION SERVICE¦UNIVERSAL TRANSPORTATION SERVICE¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦0¦025039400¦025039400¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦";
		//msg = "ABDB¦09/22/09¦15:05:03¦NEQR2¦056062161¦MATU226727 ¦5868540          ¦MHI¦185¦W¦HON ¦HIL ¦LAX ¦UNIVERSAL TRANSPORTATION SERVICE¦UNIVERSAL TRANSPORTATION SERVICE¦$UCJAC  ¦MAT ¦H¦Y¦N¦N¦N¦0¦025039400¦025039400¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦UNIVERSAL TRANSPORTATION SERVICES                 ¦                                        ¦";
		
		//HAL942
		//msg = "ABDA¦10/09/09¦18:08:16¦NEQR2¦015116261¦MATU228053 ¦                 ¦HAL¦942¦C¦HON ¦HON ¦KAH ¦     ¦ ¦HCS005719 ¦HAWAIIAN COMMERCIAL & SUGAR CO. ¦COBUS                           ¦CUBUS                            ¦$UKSAK  ¦9¦000000000¦000000000¦                                                  ¦                                        ¦                                                  ¦                                        ¦";
		//msg = "ABDA¦10/09/09¦18:09:15¦NEQR2¦065116261¦MATU228014 ¦                 ¦HAL¦942¦C¦HON ¦HON ¦KAH ¦     ¦ ¦HCS005720 ¦HAWAIIAN COMMERCIAL & SUGAR CO. ¦COBUS                           ¦COBUS                            ¦$UKSAK  ¦3¦000000000¦000000000¦                                                  ¦                                        ¦                                                  ¦                                        ¦";
		
		//LUR623
		//msg = "ALNK¦11/05/09¦14:17:46¦NEQR2¦071194361¦MATU249026 ¦7089110          ¦$UTERE  ¦C & S WHOLESALE GROCERS INC        ¦ ¦CY ¦CY ¦N¦RENE MORENO                     ¦510¦1     ¦834¦    ¦9212¦    ¦MAT ¦H¦V¦53843300¦                                ¦C & S WHOLESALE GROCERS INC     ¦00013554¦                                ¦                    ¦UNICOLD CORPORATION             ¦                                ¦                                ¦555 MARITIME STREET BLDG D-516  ¦    ¦OAKLAND                  ¦CA¦US               ¦94607    ¦                    ¦3¦LUR¦623¦E¦OAK ¦OAK ¦HON ¦099835200¦000002159¦C & S WHOLESALE GROCERS INC                       ¦                                        ¦UNICOLD CORPORATION                               ¦                                        ¦";
		//msg = "ALNK¦11/05/09¦14:19:07¦NEQR2¦062194361¦FSCU963116 ¦7089110          ¦$UTERE  ¦C & S WHOLESALE GROCERS INC        ¦ ¦CY ¦CY ¦N¦RENE MORENO                     ¦510¦1     ¦834¦    ¦9212¦    ¦MAT ¦H¦V¦53843300¦                                ¦C & S WHOLESALE GROCERS INC     ¦00013554¦                                ¦                    ¦UNICOLD CORPORATION             ¦                                ¦                                ¦555 MARITIME STREET BLDG D-516  ¦    ¦OAKLAND                  ¦CA¦US               ¦94607    ¦                    ¦1¦LUR¦623¦E¦OAK ¦OAK ¦HON ¦099835200¦000002159¦C & S WHOLESALE GROCERS INC                       ¦                                        ¦UNICOLD CORPORATION                               ¦                                        ¦";
		//msg = "ABDB¦11/05/09¦14:20:08¦NEQR2¦003194361¦FSCU963116 ¦7089110          ¦LUR¦623¦E¦OAK ¦OAK ¦HON ¦C & S WHOLESALE GROCERS INC     ¦UNICOLD CORPORATION             ¦$UTERE  ¦MAT ¦H¦N¦N¦N¦N¦1¦099835200¦000002159¦C & S WHOLESALE GROCERS INC                       ¦                                        ¦UNICOLD CORPORATION                               ¦                                        ¦";
		
		/*for(int i=0;i<100;i++) {
			msg = "ATRU¦09/02/09¦13:42:39¦NEQOL¦076001461¦EVGA¦EVOLUTION LOGISTICS INC "+i+"                         ¦MAT  ¦Y¦MONGE HAIRON (713)45            ¦(713)453-8700";
			
			sender.send( msg);b
			
			
			msg = "ATRU¦09/02/09¦13:42:39¦NEQOL¦076001461¦EVG1¦EVOLUTION LOGISTICS INC "+i+"                         ¦PAC  ¦N¦MONGE HAIRON X                  ¦(713)453-8711";
			
			sender.send( msg);
		}*/
		//UPU TEST 
		/*
		msg ="AUP1¦10/29/09¦15:48:28¦NEQR2¦014701380¦CAXU409325  ¦    ¦MAT ¦10/29/09¦D40 86ST¦                                                  ¦8¦MATU¦MAT¦MATU¦H3";
		msg ="AUP1¦10/29/09¦16:14:00¦NEQR2¦024701380¦DCIU736032  ¦    ¦MAT ¦10/29/09¦T20 86ST¦                                                  ¦6¦SHOW¦MAT¦TEXU¦";
		msg = "AUP2¦10/30/09¦11:11:28¦NEQR2¦044701380¦MATZ901257  ¦    ¦MAT ¦10/30/09¦C40 00TA¦                                                  ¦X¦APLU¦MAT¦MATU¦";
		msg = "AUP3¦10/30/09¦11:13:14¦NEQR2¦054701380¦MATG6198    ¦    ¦MAT ¦10/30/09¦M00 00MG¦                                                  ¦ ¦APLU¦MAT¦MATU¦";
		msg = "AUP1¦10/30/09¦11:15:28¦NEQR2¦064701380¦MATU307318  ¦    ¦MAT ¦10/30/09¦B40 46RR¦                                                  ¦2¦APLU¦MAT¦MATU¦";
		msg= "AUP1¦10/29/09¦16:15:35¦NEQR2¦034701380¦MATU625236  ¦    ¦MAT ¦10/29/09¦D24 86H3¦                                                  ¦6¦MATU¦MAT¦MATU¦H3";
		 */
      
		/* msg = "ABOB¦05/26/10¦15:38:36¦NEQV2¦061784061¦MAT¦8368856          ¦        ¦B¦ROSS FURNITURE AND APPLIANCE       ¦MLI¦061¦GUM ¦PNP ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                        ITN NO    ¦ROSFUR¦W¦                                ¦   ¦             ¦FURNITURE                        ¦MAT ¦G¦F¦                    ¦N¦N¦N¦N¦OCEAN VIEW HOTEL                ¦071229510¦081229510¦ROSS FURNITURE AND APPLIANCE                      ¦                                        ¦OCEAN VIEW HOTEL                                  ¦                                        ¦063241710¦071229510¦073241710¦081229510¦0002¦D0020 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦ITN NO ¦N¦";
        // msg = "ABOB¦05/26/10¦15:38:36¦NEQV2¦061784061¦MAT¦8368856          ¦        ¦B¦ROSS FURNITURE AND APPLIANCE       ¦MLI¦061¦GUM ¦PNP ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                        ITN YES    ¦ROSFUR¦W¦                                ¦   ¦             ¦FURNITURE                        ¦MAT ¦G¦F¦                    ¦N¦N¦N¦N¦OCEAN VIEW HOTEL                ¦071229510¦081229510¦ROSS FURNITURE AND APPLIANCE                      ¦                                        ¦OCEAN VIEW HOTEL                                  ¦                                        ¦063241710¦071229510¦073241710¦081229510¦0002¦D0020 ¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦ITN YES ¦N¦";

		 msg = "AHLR¦06/04/10¦10:18:33¦NEQOL¦047808661¦5115552          ¦MATU208091  ¦MWI081¦     ¦CC ¦MAT¦H¦N¦Y¦ ¦                                                  ¦06/03/10¦10:17:00¦06/04/10¦10:18:33¦D¦$UCBI¦ ¦HON ¦0¦C¦D¦HON";
		 msg = "AHLP¦06/15/10¦14:31:09¦NEQOL¦062019651¦5817753          ¦MATU999100  ¦      ¦     ¦ITN¦MAT¦H¦N¦Y¦ ¦                                                  ¦06/15/10¦11:31:00¦06/15/10¦14:31:09¦A¦$CARA¦ ¦HON ¦2¦C¦D¦GUM";
         msg = "AHLR¦06/15/10¦14:38:37¦NEQOL¦072019651¦5817753          ¦MATU999100  ¦      ¦     ¦ITN¦MAT¦H¦N¦Y¦ ¦                                                  ¦06/15/10¦11:31:00¦06/15/10¦14:38:37¦D¦$CARA¦ ¦HON ¦2¦C¦D¦GUM";
         */
		 msg="ABOB¦06/24/10¦09:54:43¦NEQV2¦036868561¦MAT¦2201752          ¦        ¦B¦APPROVED FREIGHT FORWARDERS OF HAWA¦MHI¦205¦LAX ¦LAX ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦APPFRE¦E¦                                ¦   ¦             ¦PHARMACEUTICALS                  ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦APPROVED FREIGHT FORWARDERS OF H¦017006310¦017006310¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦034015010¦017006310¦034015010¦017006310¦0001¦R0040H¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
//Good -	 msg="ABOB¦06/24/10¦09:55:47¦NEQV2¦096868561¦MAT¦2201752          ¦        ¦B¦APPROVED FREIGHT FORWARDERS OF HAWA¦MHI¦205¦LAX ¦LAX ¦HON ¦WA   ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦APPFRE¦E¦                                ¦   ¦             ¦PHARMACEUTICALS                  ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦APPROVED FREIGHT FORWARDERS OF H¦017006310¦017006310¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦034015010¦017006310¦034015010¦017006310¦0001¦R0040H¦F¦WA   ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
//BAD		 msg="ABOB¦06/24/10¦09:55:47¦NEQV2¦096868561¦MAT¦2201752          ¦        ¦B¦APPROVED FREIGHT FORWARDERS OF HAWA¦MHI¦205¦LAX ¦LAX ¦HON ¦WA   ¦WA    ¦WA    ¦     ¦     ¦ ¦N¦000¦                                                  ¦APPFRE¦E¦                                ¦   ¦             ¦PHARMACEUTICALS                  ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦APPROVED FREIGHT FORWARDERS OF H¦017006310¦017006310¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦APPROVED FREIGHT FORWARDERS OF HAWAII             ¦                                        ¦034015010¦017006310¦034015010¦017006310¦0001¦R0040H¦F¦WA   ¦     ¦        ¦0002¦ R0040H ¦F¦WA     ¦     ¦        ¦0003¦ R0040H ¦F ¦WA     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
		   msg = "ABOB¦11/30/10¦10:14:19¦NEQV2¦096411371¦MAT¦9435413          ¦        ¦B¦MAERSK LINE.                       ¦MLE¦048¦SHA ¦SHA ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                        ITN YES    ¦MAELIN¦W¦                                ¦   ¦             ¦                                 ¦MAT ¦F¦F¦861169019           ¦N¦N¦N¦N¦MAERSK LINE. test                    ¦047254810¦047254810¦MAERSK LINE.                                      ¦                                        ¦MAERSK LINE.                                      ¦                                        ¦045267710¦047254810¦045267710¦047254810¦0001¦D0045H¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦ITN YES ¦N¦";
		   
		   msg = "ABOB¦01/07/11¦10:28:53¦NEQV2¦072522771¦MAT¦9654860          ¦        ¦B¦HAWAII FOODSERVICE ALLIANCE        ¦KAU¦708¦SEA ¦PDX ¦HON ¦AMB  ¦     ¦     ¦     ¦     ¦F¦N¦000¦                                                  ¦HAWFOO¦E¦                                ¦   ¦             ¦BREAD TRAYS                      ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦OROWEAT FOODS COMPANY           ¦069734400¦000007577¦HAWAII FOODSERVICE ALLIANCE                       ¦                                        ¦OROWEAT FOODS COMPANY                             ¦                                        ¦037740100¦069734400¦091929100¦000007577¦0001¦R0040H¦F¦AMB  ¦F    ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";

		   //msg="ABOB¦01/07/11¦10:32:27¦NEQV2¦004522771¦MAT¦5656269          ¦        ¦B¦WAL-MART STORES INC #3290          ¦MHI¦219¦OAK ¦OAK ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦CNEE                                              ¦WALMAR¦E¦#3290                           ¦   ¦             ¦CARDBOARD BALES : MATU455430     ¦MAT ¦H¦F¦                    ¦N¦N¦N¦N¦HARMON ASSOCIATES CORPORATION   ¦080676000¦000004253¦WAL-MART STORES INC                               ¦#3290                                   ¦HARMON ASSOCIATES CORPORATION                     ¦                                        ¦080676000¦000012384¦026249410¦000004253¦0001¦D0045H¦F¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
          
		  /* msg = "ACAR¦01/10/11¦18:53:05¦NEQR2¦057528371¦MATU880157 ¦3¦F¦    ¦GUM ¦                                ¦                                ¦MLE¦049¦W¦AUT¦          ¦       ¦16656¦     ¦ ¦Y¦ ¦LDA   ¦HON ¦20110110¦165200¦CARS    ¦                 ¦          ¦GUM ¦2 AUTOS                          ¦MLE¦049¦W¦HON ¦   ¦                                                                              ¦000¦000¦000¦000¦000";
		   
		   msg="ACAR¦01/11/11¦18:29:25¦NEQR2¦096531771¦MATU880167 ¦6¦E¦    ¦MIX ¦                                ¦                                ¦MNA¦215¦W¦   ¦          ¦       ¦11900¦     ¦ ¦ ¦ ¦ULD   ¦HON ¦20110111¦162000¦CARS    ¦                 ¦          ¦HON ¦                                 ¦   ¦   ¦ ¦OAK ¦   ¦                                                                              ¦000¦000¦000¦000¦000";

		   msg="ACAR¦01/11/11¦18:48:58¦NEQR2¦080531871¦MATU880167 ¦6¦F¦    ¦LAX ¦                                ¦                                ¦MHI¦219¦E¦AUT¦          ¦       ¦25427¦     ¦ ¦Y¦ ¦LDA   ¦HON ¦20110111¦164800¦CARS    ¦                 ¦          ¦LAX ¦4 AUTOS                          ¦MHI¦219¦E¦HON ¦   ¦                                                                              ¦000¦000¦000¦000¦000";
		   
		   msg ="ACAR¦01/11/11¦18:28:50¦NEQR2¦056531771¦MATU882193 ¦9¦E¦    ¦MIX ¦                                ¦                                ¦MNA¦215¦W¦   ¦          ¦       ¦13180¦     ¦ ¦ ¦ ¦ULD   ¦HON ¦20110111¦161700¦CARS    ¦                 ¦          ¦HON ¦                                 ¦   ¦   ¦ ¦OAK ¦   ¦                                                                           ";
		   
		   
		   msg="ABOB¦06/21/11¦12:09:19¦NEQV2¦033804271¦MAT¦9324934          ¦        ¦B¦ABF CARTAGE INC                    ¦MKA¦204¦LAX ¦LAX ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦ABFCAR¦E¦                                ¦   ¦             ¦EMPTY VAN RETURNING              ¦MAT ¦H¦E¦                    ¦N¦N¦N¦N¦ABF CARTAGE INC                 ¦000000148¦000000148¦ABF CARTAGE INC                                   ¦                                        ¦ABF CARTAGE INC                                   ¦                                        ¦000000181¦000000148¦000000174¦000000148¦0000¦V0028 ¦E¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
		 */
		   msg="ABOB¦06/21/11¦12:09:24¦NEQV2¦043804271¦MAT¦9324934          ¦        ¦B¦ABF CARTAGE INC                    ¦MKA¦204¦LAX ¦LAX ¦HON ¦     ¦     ¦     ¦     ¦     ¦ ¦N¦000¦                                                  ¦ABFCAR¦E¦                                ¦   ¦             ¦EMPTY VAN RETURNING              ¦MAT ¦H¦E¦                    ¦N¦N¦N¦N¦ABF CARTAGE INC                 ¦000000148¦000000148¦ABF CARTAGE INC                                   ¦                                        ¦ABF CARTAGE INC                                   ¦                                        ¦000000181¦000000148¦000000174¦000000148¦0001¦V0028 ¦E¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦0000¦ 0000 ¦ ¦     ¦     ¦        ¦    ¦          ¦     ¦   ¦               ¦          ¦       ¦N¦";
           //msg="ACAR¦08/05/11¦18:59:29¦NEQR2¦         ¦MATU882530     ¦1¦E¦    ¦MIX ¦                                ¦                                ¦MHI¦194¦W¦   ¦          ¦       ¦13180¦     ¦ ¦N¦N¦ULD   ¦HON ¦20110802¦164000¦CARS    ¦                 ¦          ¦HON ¦MTY                              ¦   ¦   ¦ ¦OAK ¦   ¦                                                                              ¦000¦000¦000¦000¦000";
		   //for(int i=0; i<10 ; i++){
		   sender.send( msg);
		   //System.out.println("MST SENT ::"+i);
		  // }

		   
	}

}
