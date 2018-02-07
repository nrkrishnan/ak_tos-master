package com.matson.tos.upload;

import javax.jws.WebService;

@WebService(name="HelloWorld", serviceName="HelloWorldService")

public class HelloWorld {

	public String hello(String name) {
		return "Hello "+name;
	}
}
