package com.matson.tos.tests;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.error.SnxError;
import com.matson.tos.util.SnxUnmarshaller;

public class TestSnxUnmarshaller {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		byte[] b = new byte[100000];
		long time = System.currentTimeMillis();
		FileInputStream file = new FileInputStream("test.xml");
		int size = file.read(b);
		//SnxError err = SnxUnmarshaller.unmarshallError(file);
		String msg = new String(b,0,size);
		
	//	InputStream stream = new ByteArrayInputStream(err.getPayload().getBytes());
	//	Snx snx = SnxUnmarshaller.unmarshall(stream);
		time = System.currentTimeMillis() - time;
		
		
		//err.getPayload()
		Pattern p = Pattern.compile("<unit.*?id=\"(.*?)\"");
		//Pattern p = Pattern.compile("unit");
		Matcher m = p.matcher(msg);
		if(m.find()) {
			System.out.println(m.group(1));
		} else {
			System.out.println("No Match");
		}
		
		p = Pattern.compile("<carrier.*?id=\"(.*?)\"");
		m = p.matcher(msg);
		if(m.find()) {
			System.out.println(m.group(1));
		} else {
			System.out.println("No Match");
		}
		
		System.out.println("Finsihed "+time);
		System.out.println("SNX="+msg);
		
		
		// <carrier id="MHI176" mode="VESSEL" qualifier="ACTUAL" direction="IB"/>
		// <unit snx-update-note="NewVes Data" line="MAT" freight-kind="FCL" category="IMPORT" id="TRLU7192704">
	}

}
