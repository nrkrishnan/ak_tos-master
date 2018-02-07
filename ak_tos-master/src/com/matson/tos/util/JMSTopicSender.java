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

public class JMSTopicSender {
	private static Logger logger = Logger.getLogger(JMSTopicSender.class);
	private String topicName;
	private static String url;

	static {
		url = TosRefDataUtil.getValue("JMS_URL");
		if (url == null) {
			url = "t3://localhost:9301/";
		}
	}

	public JMSTopicSender(String topicName) {
		this.topicName = topicName;
	}

	public void send(String msg) throws JMSException, Exception {
		// String cfName = "jms/WLQueueConnectionFactory";
		// String cfName = "weblogic.examples.jms.TopicConnectionFactory";
		String cfName = "ak.jms.conn.tdp.udq"; // A1

		TopicSession session = null;
		TopicConnection connection = null;
		TopicConnectionFactory cf = null;
		MessageProducer mp = null;
		Destination destination = null;

		try {

			Hashtable env = new Hashtable();
			// env.put(Context.INITIAL_CONTEXT_FACTORY,
			// "weblogic.jndi.WLInitialContextFactory");
			// env.put(Context.PROVIDER_URL, url);
			// Context initialContext = new InitialContext(env);
			Context initialContext = new InitialContext();
			// logger.debug( "Getting Connection Factory");

			cf = (TopicConnectionFactory) initialContext.lookup(cfName);

			// logger.debug( "Getting Queue");
			destination = (Destination) initialContext.lookup(topicName);

			// logger.debug( "Getting Connection for Queue");
			connection = cf.createTopicConnection();

			// logger.debug( "staring the connection");
			connection.start();

			// logger.debug( "creating session");
			session = connection.createTopicSession(false, 1);

			// logger.debug( "creating messageProducer");
			mp = session.createProducer(destination);

			// logger.debug( "creating TextMessage");
			TextMessage outMessage = session.createTextMessage(msg);

			logger.debug("sending Message to queue: " + topicName);
			mp.send(outMessage);

			mp.close();
			session.close();
			connection.close();
		} catch (Exception je) {
			je.printStackTrace();
			logger.error("Exception: ", je);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
