package com.matson.tos.util.test;

import com.matson.tos.util.JMSTopicSender;

public class TestJMSTopicSender {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String topic = "jms.topic.tdp.newVesselHon";
		JMSTopicSender sender = new JMSTopicSender(topic);
		sender.setUrl("t3://10.3.4.179:9301/");
		sender.send("test");
	}

}
