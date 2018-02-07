package com.matson.tos.tests;

import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.*;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.RefDataLookup;
import com.matson.cas.refdata.mapping.TosDestQueLookup;
import com.matson.tos.exception.TosException;

public class JMSSender {
	private static Logger logger = Logger.getLogger(JMSSender.class);

	public static int REAL_TIME_QUEUE = 1;
	public static int BATCH_QUEUE = 2;
	//private static String DEST_PORT = "HON";
	private TosDestQueLookup queLookup = null;

	//private String qnameIn = "jms.queue.cluster1";
	private String qnameIn = "jms.distqueue.tdp.N4QueueOut";

	public JMSSender() throws TosException {

	}

	public  void send(String msg) throws JMSException, Exception
	{
	    String  cfName                  = "jms/WLQueueConnectionFactory";
	    //String  cfName                  = "jms.cf.cluster1";
	    //String  url						= "t3://10.3.4.179:9301/";
	    //String  url						= "t3://10.201.0.14:9301/";
	    String  url						= "t3://10.3.4.179:9301/";

	    Session                session    = null;
	    Connection             connection = null;
	    ConnectionFactory      cf         = null;
	    MessageProducer        mp         = null;
	 
	    Destination            destination = null;

	    try {

	    	Hashtable env = new Hashtable();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    	     "weblogic.jndi.WLInitialContextFactory");
	    	env.put(Context.PROVIDER_URL, url);
	    	Context initialContext = new InitialContext(env);
	    	//logger.debug( "Getting Connection Factory");

			cf= (ConnectionFactory)initialContext.lookup( cfName );

			//logger.debug( "Getting Queue");
			destination =(Destination)initialContext.lookup(qnameIn);

			//logger.debug( "Getting Connection for Queue");
			connection = cf.createConnection();

			//logger.debug( "staring the connection");
			connection.start();

			//logger.debug( "creating session");
			session = connection.createSession(false, 1);

			//logger.debug( "creating messageProducer");
			//Queue q = session.createQueue("jms.module.cluster!jms.server.tdp/jms.queue.cluster1" );
			//Queue q = session.createQueue("jms.module.cluster!JMSServer-0/jms.queue.cluster1" );
			
			mp = session.createProducer(destination);
			//mp = session.createProducer(q);

			//logger.debug( "creating TextMessage");
			TextMessage outMessage = session.createTextMessage( msg);

			//logger.debug( "sending Message to queue: " + qnameIn);
			
			//for(int j=0;j<10;j++) {
				//System.out.println(j+" Start");
				mp.send(outMessage);		
			//}
			//mp.send(outMessage);

			mp.close();
			session.close();
			connection.close();
	    }
	    catch (Exception je)
	    {
	    	je.printStackTrace();
	    	logger.error( "Exception: ", je);
	    }
	}
	
	public static void main(String[] args) throws Exception {
		JMSSender s = new JMSSender();
		
		//for(int i=0;i<1;i++) {
		  // s.send("<GroovyMsg msgType='UNIT_RECEIVE'  unitClass='CONTAINER' ctrNo='512724' checkDigit='6' chassisNumber='null' chassisCd='null' category='STRGE' fieldsChanged='null' fieldsPrevoiusValue='null' accessory='null' mgWeight='null'  tareWeight='10360' typeCode='%' hgt='%' strength='A' owner='MATU' damageCode='null' srv='MAT' temp='null' tempMeasurementUnit='null'  locationRow='MAT' cWeight='10360' seal='null' stowRestCode='null' stowFlag='%' odf='null'  bookingNumber='null' consignee='null' shipper='null' cneeCode='null' hazF='null' hazImdg='null' hazUnNum='null' flex01='null' locationCategory='null' arrDate='null' consigneePo='null' restow='NONE'  commodity='MTY' dir='MTY' dsc='null' planDisp='null' ds='%' orientation='E'  shipperPool='null' dischargePort='OPT' dPort='OPT' loadPort='null' retPort='null' overLongBack='null' overLongFront='null' overWideLeft='null' overWideRight='null' overHeight='null'  loc='S414D' cell='null' locationTier='null' locationStatus='1' locationStallConfig='null'  vesvoy='null' truck='null' misc1='null' actualVessel='null' actualVoyage='null' leg='null'  hsf7='null' pmd='null' locationRun='%' misc2='null' misc3='null'  action='null' aDate='11/11/2009' aTime='11:19:27' doer='RTACTCS' sectionCode='Z' lastAction='null' lastADate='11/11/2009' lastATime='11:19:27' lastDoer='RTACTCS' hazOpenCloseFlag='null' aei='null' dss='%' erf='null'  comments='null' crStatus='null' cargoNotes='null'  safetyExpiry='%' lastInspection='%' lastAnnual='%' chassisAlert='%' mgp='null' chassisHold='MAT' chassisNotes='%' chassTareWeight='null'  gateTruckCmpyCd='%' gateTruckCmpyName='%' gateTruckId='%' batNumber='%' turnTime='%' gateSeqNo='%' laneGateId='SI GATE' deptTruckCode='null' deptDport='null' deptVesVoy='null' deptOutGatedDt='null' deptConsignee='null' deptCneeCode='null' deptBkgNum='null' deptMisc3='null' deptCargoNotes='null' />");
		 //  Thread.sleep(10000);
		}
/*		
		for(int j=0;j<1000;j++) {
			System.out.println(j+" Done");
			for(int i=0;i<100;i++) s.send("<GroovyMsg msgType='UNIT_PROPERTY_UPDATE'  unitClass='CONTAINER' ctrNo='247836' checkDigit='0' chassisNumber='MATZ906164' chassisCd='4' category='IMPRT' fieldsChanged='null' fieldsPrevoiusValue='null' accessory='null' mgWeight='null'  tareWeight='8160' typeCode='%' hgt='%' strength='BA' owner='MATU' damageCode='null' srv='MAT' temp='null' tempMeasurementUnit='null'  locationRow='MAT' cWeight='51006' seal='11546' stowRestCode='null' stowFlag='%' odf='null'  bookingNumber='5158576004' consignee='C &amp; S WHOLESALE G%TEST REL TO PARTY' shipper='C &amp; S WHOLESALE GROCERS INC' cneeCode='0099835200' hazF='null' hazImdg='null' hazUnNum='null' locationCategory='09007' arrDate='null' consigneePo='null' restow='NONE'  commodity='SIT' dir='IN' dsc='S' planDisp='null' ds='CY' orientation='F'  shipperPool='OAK' dischargePort='HON' dPort='HON' loadPort='OAK' retPort='null' overLongBack='null' overLongFront='null' overWideLeft='null' overWideRight='null' overHeight='null'  loc='%' cell='%' locationTier='%' locationStatus='%'  vesvoy='%' truck='%' misc1='%' actualVessel='%' actualVoyage='%' leg='%'  hsf7='null' pmd='null' locationRun='%' misc2='RCR' misc3='0836409013'  action='EDT' aDate='08/04/2009' aTime='15:20:13' doer='graposo' sectionCode='Z' lastAction='EDT' lastADate='08/04/2009' lastATime='15:20:13' lastDoer='graposo' hazOpenCloseFlag='null' aei='null' dss='%' erf='null'  comments='null' crStatus='null' cargoNotes='SIT-HON MIXED GROCERIES &amp; WHOLESALE S-12/26/08 TEST14'  safetyExpiry='%' lastInspection='%' lastAnnual='%' chassisAlert='%' mgp='null' chassisHold='MAT' chassisNotes='%' chassTareWeight='6613'  gateTruckCmpyCd='%' gateTruckCmpyName='%' gateTruckId='%' batNumber='%' turnTime='%' gateSeqNo='%' laneGateId='%' deptTruckCode='null' deptDport='null' deptVesVoy='null' deptOutGatedDt='null' deptConsignee='null' deptCneeCode='null' deptBkgNum='null' deptMisc3='null' deptCargoNotes='null' />");
		}
		*/
	//}
	java.util.Map map = new java.util.HashMap();
	
	
}
