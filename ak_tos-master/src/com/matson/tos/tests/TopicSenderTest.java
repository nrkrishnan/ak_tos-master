package com.matson.tos.tests;

import com.matson.tos.util.JMSTopicSender;

public class TopicSenderTest {
	public static void main(String[] args) throws Exception{
		JMSTopicSender sender = new JMSTopicSender("jms.topic.tdp.n4");
		for(int i=0;i<100;i++) sender.send("test"+i);
	}
}
