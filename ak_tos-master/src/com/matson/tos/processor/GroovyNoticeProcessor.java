package com.matson.tos.processor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.matson.tos.util.EmailSender;

public class GroovyNoticeProcessor 
{
	private String subject = "Gate Booked Temp. Discrepancy";
//	private String toEmailList = "1DATA INTEGRITY GROUP at HON; John Robinette at HON x1305; " +
//			"1SYSTEMS SUPPORT at HON; 1CMC GROUP at HON; 1REEFERS Group at PHX; Systems Reports at HON";
//	private String EmailSender = "1CMC GROUP at HON";
	//A1 Test
	private String emailSender = "graposo@matson.com";
	private String toEmailList = "graposo@matson.com";
    private String emailContent;
    private String xmlMsgData = "<container bkgReqTemp='7.22' gateReqTemp='1.67' unit='MATU5108842' commdity='MAUI PINE' bkgNum='1255224' eventType='InGateReqTemp' port='LAX' />";
	
	public void process(String xmlData)
	{
		try{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		//For Testing Reading through XML File
		//InputStream input = new FileInputStream("C:/TestDCM/xml/ingate.xml");
		InputStream input = new ByteArrayInputStream ( xmlMsgData.getBytes());
		Document doc = db.parse(input);

		NodeList nodes = doc.getElementsByTagName("container");
		NamedNodeMap attrNodes = nodes.item(0).getAttributes();
        //Verifies the event Type and processes event data.
		if(attrNodes.getNamedItem("eventType").getNodeValue().equals("InGateReqTemp")){
			String port = attrNodes.getNamedItem("port").getNodeValue();
			String unit = attrNodes.getNamedItem("unit").getNodeValue();
			String bookingNum = attrNodes.getNamedItem("bkgNum").getNodeValue();
			String commodity = attrNodes.getNamedItem("commdity").getNodeValue();
			//Converts Required temperature from Celsius to Fahrenheit
			String gateTemp = celsiusToFahrenheit(attrNodes.getNamedItem("gateReqTemp").getNodeValue());
			String bkgTemp = celsiusToFahrenheit(attrNodes.getNamedItem("bkgReqTemp").getNodeValue());
			
			emailContent = "From:	Gate\nCtr:"+unit+"	BookNo:"+bookingNum+
					"	Gate Temp:"+gateTemp+"	Book Temp:"+bkgTemp+"	Port:"+port;
	
			//EmailSender.mailSender(toEmailList, emailSender,"mailhost.matson.com",emailContent, subject);
		  }
		}
        catch(SAXException saxe){ saxe.printStackTrace();}
		catch(ParserConfigurationException pce){ pce.printStackTrace(); }
        catch(FileNotFoundException fe){ fe.printStackTrace(); }
        catch(IOException ioex){ ioex.printStackTrace(); }
	}
	/*
	 * Method Converts value from celsius to Fahrenheit
	 */
	public String celsiusToFahrenheit(String strCelsius)
	{
		float celsius = Float.parseFloat(strCelsius);
	    float fahr = (celsius * 9/5) + 32;

	    int r = Math.round(fahr * 100)/100;
	    Integer i = new Integer(r);
	    return i.toString(); 
	}
/*	public static void main(String args[])
	{
		GroovyNoticeProcessor ingate = new GroovyNoticeProcessor();
		ingate.process("Test");
		System.out.println("After Process");
	}
*/
}
