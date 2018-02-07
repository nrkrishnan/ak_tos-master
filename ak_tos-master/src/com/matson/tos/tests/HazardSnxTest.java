package com.matson.tos.tests;

import java.io.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;

import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TCarrier;
import com.matson.tos.jaxb.snx.TDirection;
import com.matson.tos.jaxb.snx.THazard;
import com.matson.tos.jaxb.snx.THazards;
import com.matson.tos.jaxb.snx.TUnitHazards;
import com.matson.tos.jaxb.snx.TUnitIdentity;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.SnxMessageHandler;

public class HazardSnxTest {

	/**
	 * @param args
	 */
/*	public static void main(String[] args) throws Exception{
		Snx x = new Snx();
		THazard h = new THazard();
		h.setImdg("5.1");
		h.setUn("1980");
		h.setProperName("Test Hazard");
		THazards hazName = new THazards();
		hazName.getHazard().add(h);
		TUnitIdentity id = new TUnitIdentity();
		TCarrier vv = new TCarrier();
		vv.setDirection(TDirection.IB);
		vv.setId("MKA155");
		vv.setMode("VESSEL");
		vv.setQualifier("DECLARED");
		
		id.setCarrier(vv);
		id.setId("MATU1234567");
		id.setType("CONTAINERIZED");
		
		TUnitHazards haz = new TUnitHazards();
		haz.setHazards(hazName);
		haz.setUnitIdentity(id);
		x.getUnitHazards().add(haz);

		SnxMessageHandler snxMsgHandler = new SnxMessageHandler(
				"com.matson.tos.jaxb.snx", "com.matson.tos.jatb", "",
				AbstractMessageHandler.TEXT_TO_XML);
		snxMsgHandler.setXmlObj(x);
		System.out.println(snxMsgHandler.getXmlStr());
	}
*/
	
	public static void main(String args[]){
		try{
		File file = new File("C:/A_INSTALL/NV_MKA209_part1.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String temp = br.readLine().toString();
		String done = temp.replaceAll("f2", "\n");
		
		File fileOut = new File("C:/A_INSTALL/NV_MKA209_part2.txt");
		BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
	    output.write(done);
	    output.close();
	    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
