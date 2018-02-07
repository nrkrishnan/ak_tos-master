package com.matson.vinsight.webservice;

import java.net.URL;

import javax.xml.rpc.ServiceException;

import com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity;

public class TestVinsightWebService {
	public static void main(String[] args) throws Exception {
		VINSightHDOAWebServiceLocator service = new VINSightHDOAWebServiceLocator();
		VINSightHDOAWebServicePort serv;
		

		serv = service.getVINSightHDOAWebServicePort(new URL("http://10.8.7.144:8080/service/VINSightHDOAWebService?wsdl"));
		
		/*
		VINSightHDOACommodity[] commodity = serv.getHDOACommodities("RJP380W");
		if(commodity == null) {
			System.out.println("No results");
			return;
		}
		System.out.println("Results");
		for(VINSightHDOACommodity item : commodity) {
			System.out.println("Item:"+item);
			int[] ids = new int[1];
			ids[0] = item.getVinsightId();
			VINSightHDOACommodity[] results =  serv.putHDOACommoditiesOnHold(ids);
			System.out.println("Result ="+results.length);
		}
		*/
		
		int[] ids = new int[1];
		ids[0] = 10953982;
		VINSightHDOACommodity[] commodity =  serv.putHDOACommoditiesOnHold(ids);
		System.out.println("Result ="+commodity.length);
		for(VINSightHDOACommodity item : commodity) {
			System.out.println("Item:"+item);
		}
		
		
	}
}
