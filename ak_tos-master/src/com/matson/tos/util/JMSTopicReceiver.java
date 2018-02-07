package com.matson.tos.util;

import java.io.*;
import java.util.*;
import javax.transaction.*;
import javax.naming.*;
import javax.jms.*;
import javax.rmi.PortableRemoteObject;

/**
 *
 * This example shows how to establish a connection to and receive
 * messages from a JMS topic. The classes in this package operate
 * on the same JMS topic. Run the classes together to observe messages
 * being sent and received. This class is used to receive messages from
 * the topic.
 * @author Copyright (c) 1999-2005 by BEA Systems, Inc. All Rights Reserved.
 */
public class JMSTopicReceiver extends Thread implements MessageListener  {

 // Defines the JNDI context factory.
  public final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
  // Defines the JMS connection factory for the topic.
  public final static String JMS_FACTORY="jms/WLQueueConnectionFactory";
  // Defines the topic.
  public final static String TOPIC="jms.topic.tdp.newVesselHon";

  private TopicConnectionFactory tconFactory;
  private TopicConnection tcon;
  private TopicSession tsession;
  private TopicSubscriber tsubscriber;
  private Topic topic;
  private boolean quit = false;

  public void run() {}
 /**
  * Message listener interface.
  * @param msg message
  */
  public void onMessage(Message msg) {
    try {
      String msgText;
      try {
		Thread.sleep(10);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      
      if (msg instanceof TextMessage) {
        msgText = ((TextMessage)msg).getText();
      } else {
        msgText = msg.toString();
      }
      System.out.print("ID="+Thread.currentThread().getId()+" "+Thread.currentThread().getName());
      System.out.println("JMS Message Received: "+ msgText );

      if (msgText.equalsIgnoreCase("quit")) {
        synchronized(this) {
          quit = true;
          this.notifyAll(); // Notify main thread to quit
        }
      }
    } catch (JMSException jmse) {
      System.err.println("An exception occurred: "+jmse.getMessage());
    }
  }

  /**
   * Creates all the necessary objects for sending
   * messages to a JMS topic.
   *
   * @param  ctx  JNDI initial context
   * @param topicName	name of topic
   * @exception NamingException	if problem occurred with JNDI context interface
   * @exception JMSException if JMS fails to initialize due to internal error
   */
  public void init(Context ctx, String topicName)
    throws NamingException, JMSException
  {
    tconFactory = (TopicConnectionFactory) 
      PortableRemoteObject.narrow(ctx.lookup(JMS_FACTORY), 
                                  TopicConnectionFactory.class);
    tcon = tconFactory.createTopicConnection();
    tsession = tcon.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    topic = (Topic) 
      PortableRemoteObject.narrow(ctx.lookup(topicName), 
                                  Topic.class);
    tsubscriber = tsession.createSubscriber(topic);
    tsubscriber.setMessageListener(this);
    tcon.start();
  }

  /**
   * Closes JMS objects.
   *
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  public void close() throws JMSException {
    tsubscriber.close();
    tsession.close();
    tcon.close();
  }

 /**
  * main() method.
  *
  * @param args WebLogic Server URL
  * @exception Exception if execution fails
  */
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: java examples.jms.topic.TopicReceive WebLogicURL");
      return;
    }
    InitialContext ic = getInitialContext(args[0]);
    JMSTopicReceiver tr = new JMSTopicReceiver();
    tr.init(ic, TOPIC);

    System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");

    // Wait until a "quit" message has been received.
    synchronized(tr) {
      while (! tr.quit) {
        try {
          tr.wait();
        } catch (InterruptedException ie) {}
      }
    }
    tr.close();
  }

  private static InitialContext getInitialContext(String url)
    throws NamingException
  {
    Hashtable<String,String> env = new Hashtable<String,String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
    env.put(Context.PROVIDER_URL, url);
    env.put("weblogic.jndi.createIntermediateContexts", "true");
    return new InitialContext(env);
  }

}


