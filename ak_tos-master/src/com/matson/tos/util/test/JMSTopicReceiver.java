package com.matson.tos.util.test;

import java.util.Hashtable;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.*;

import org.apache.log4j.Logger;


public class JMSTopicReceiver {
	private static Logger logger = Logger.getLogger(JMSTopicReceiver.class);
    private static String url;
    
    static {
    	//url						  = "t3://10.201.0.6:8001/";
    	url						  = "t3://10.8.7.145:8001/";
    	//url						  = "t3://10.3.4.179:9301/";
    	
    }
	
	public  void receive() throws JMSException, Exception
	{
		   String  cfName                    = "jms.cf.fss";
		    String  qnameOut                  =  "jms.topic.fss";


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
			System.out.println("Get");
			TextMessage inMessage;
			inMessage = (TextMessage)mc.receive( 10);
			if(inMessage != null) {
				System.out.println(inMessage.getText());
				logger.debug( inMessage.getText());
			} else {
				System.out.println("no message");
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
	
	public static void main(String[] args) throws Exception {
		JMSTopicReceiver r = new JMSTopicReceiver();
		while(true) {
			System.out.println("Test");
		   r.receive();
		}
	}
}
