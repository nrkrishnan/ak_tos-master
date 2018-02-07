package com.matson.tos.util.test;

import java.io.BufferedReader;
import java.io.FileReader;


public class TestJMSReceiver {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		/*JMSReceiver r = new JMSReceiver();
		while(true) {
		   r.receive();
		}*/
		
		BufferedReader reader = new BufferedReader(new FileReader("C:/A_INSTALL/MKA205.TXT"));
		String text = null;
		while ((text = reader.readLine()) != null) {

			if (text != null && text.contains("VSLSTOW")) {
				//return reader.readLine();
				String[] vesDetails = reader.readLine().split("\t");
				String vesselName = vesDetails[1];
				System.out.println(vesselName);
			}
		}
		
	}
	
	
	
	

}
