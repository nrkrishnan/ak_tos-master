package com.matson.tos.util.test;

import java.util.Hashtable;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.*;

import org.apache.log4j.Logger;


public class JMSReceiver {
	private static Logger logger = Logger.getLogger(JMSReceiver.class);
    private static String url;
    
    static {
    		url						  = "t3://10.201.0.14:9302/";
    	
    }
	
	public  void receive() throws JMSException, Exception
	{
		System.out.println("receive start");
	    String  cfName                    = "jms/WLQueueConnectionFactory";
	    String  qnameOut                  =  "jms.queue.cluster1";


	    Session                session    = null;
	    Connection             connection = null;
	    ConnectionFactory      cf         = null;
	    MessageConsumer        mc         = null;
	    Destination            destination = null;

	    try {
	    	
	    	Hashtable env = new Hashtable();
	    	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    	     "weblogic.jndi.WLInitialContextFactory");
	    	env.put(Context.PROVIDER_URL, url);
	    	Context initialContext = new InitialContext(env);
	    	logger.debug( "Getting Connection Factory");

			cf= (ConnectionFactory)initialContext.lookup( cfName );
			System.out.println("cf="+cf);
			logger.debug( "Getting Queue");
			destination =(Destination)initialContext.lookup(qnameOut);

			logger.debug( "Getting Connection for Queue");
			connection = cf.createConnection();
			
			logger.debug( "staring the connection");
			connection.start();

			logger.debug( "creating session");
			session = connection.createSession(false, 1);

			logger.debug( "creating messageProducer");
			mc = session.createConsumer(destination, null);

			logger.debug( "receiving message");
			TextMessage inMessage;
			inMessage = (TextMessage)mc.receive( 1000000);
			if(inMessage != null) {
				System.out.println(inMessage.getText());
				logger.debug( inMessage.getText());
			}

			mc.close();
			session.close();
			connection.close();
	    }
	    catch (Exception je)
	    {
	    	je.printStackTrace();
	    }
	}
}
