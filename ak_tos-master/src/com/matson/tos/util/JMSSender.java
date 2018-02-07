/*
*********************************************************************************************
*Srno   Date		AuthorName			Change Description
* A1    02/11/11    Glenn Raposo		Updated JMS Connection Facorty to Distributed 
                                            queue factory    
*********************************************************************************************
*/
package com.matson.tos.util;

import java.util.Hashtable;
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

	private String qnameIn;
	
	private static String url;
	private boolean activeMQ = false;
	
    
    static {
    	url = TosRefDataUtil.getValue("JMS_URL");
    	if(url == null) {
    		url	= "t3://localhost:9301/";
    	}
    }

    public static boolean isMappedPort(String destQueueId) {
    	TosDestQueLookup queLookup = null;
    	try {
    		queLookup = (TosDestQueLookup)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestQueLookup", destQueueId);
    		
    	} catch ( Exception ex) {
			return false;
		}
		if ( queLookup == null) {
			return false;
		}
		return true;
    }
	public JMSSender( int queueNameId, String destQueueId) throws TosException {
		this.activeMQ = true;
		logger.info("Getting Queue name for : "+destQueueId);
		try {
			queLookup = (TosDestQueLookup)RefDataLookup.queryById("com.matson.cas.refdata.mapping.TosDestQueLookup", destQueueId);
			if (queLookup != null) {
				logger.debug("Got the Queue : " + queLookup.toString());
				logger.debug("Got the Queue : " + queLookup.getJmsInUri());
			} else {
				logger.debug("Got the Queue : " + queLookup);
			}
		} catch ( Exception ex) {
			logger.error("Could not find JMS queues for current port: " + destQueueId,ex);
			throw new TosException( "Could not find JMS queues for current port: " + destQueueId);
		}
		if ( queLookup == null) {
			throw new TosException( "Could not find queue name for destination: " + destQueueId);
		}
		
		logger.info("Queuename Id is " + queueNameId);
		if ( queueNameId == REAL_TIME_QUEUE)
	    	qnameIn = queLookup.getJmsInUri();
	    else if ( queueNameId ==  BATCH_QUEUE)
	    	qnameIn = queLookup.getJmsLowInUri();
	    else {
	    	logger.error( "Invalid queue name id:" + queueNameId);
	    	throw new TosException( "Invalid queue name id:" + queueNameId);
	    }
		logger.info("Queue Name="+queLookup.getJmsInUri());
	}
	
	public JMSSender( String queueNameId, boolean activeMq) throws TosException {
		this.activeMQ = activeMQ;
		qnameIn = queueNameId;
	}

	public  void send(String msg) throws JMSException, Exception
	{
	    //String  cfName                  = "jms/WLQueueConnectionFactory";
		String  cfName                  = "ak.jms.conn.tdp.udq";  //A1
		logger.debug("activeMQ in JMSSender send method"+activeMQ);
		//if(activeMQ) cfName                  = "activemq/QueueConnectionFactory";  //A1

	    Session                session    = null;
	    Connection             connection = null;
	    ConnectionFactory      cf         = null;
	    MessageProducer        mp         = null;
	    Destination            destination = null;

	    try {

	    	/*
	    	Hashtable env = new Hashtable();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    	     "weblogic.jndi.WLInitialContextFactory");
	    	env.put(Context.PROVIDER_URL, url);
	    	*/
	    	Context initialContext = new InitialContext();
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
			mp = session.createProducer(destination);

			//logger.debug( "creating TextMessage");
			TextMessage outMessage = session.createTextMessage( msg);

			logger.debug( "sending Message to queue: " + qnameIn);
			mp.send(outMessage);

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
	
	public  void sendBob(String msg) throws JMSException, Exception
	{
	    //String  cfName                  = "jms/WLQueueConnectionFactory";
		String cfName = "ak.jms.conn.tdp.udq";  //A1
		logger.info("activeMQ in JMSSender send method"+activeMQ);
		//if(activeMQ) cfName                  = "activemq/QueueConnectionFactory";  //A1

	    Session                session    = null;
	    Connection             connection = null;
	    ConnectionFactory      cf         = null;
	    MessageProducer        mp         = null;
	    Destination            destination = null;

	    try {

	    	/*
	    	Hashtable env = new Hashtable();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    	     "weblogic.jndi.WLInitialContextFactory");
	    	env.put(Context.PROVIDER_URL, url);
	    	*/
	    	Context initialContext = new InitialContext();
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
			mp = session.createProducer(destination);

			//logger.debug( "creating TextMessage");
			TextMessage outMessage = session.createTextMessage( msg);

			logger.debug( "sending Message to queue: " + qnameIn);
			mp.send(outMessage);
			
			session.commit();
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

}
