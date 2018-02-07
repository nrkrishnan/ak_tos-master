package com.matson.tos.util.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.matson.tos.util.SnxUnmarshaller;
import com.matson.tos.jaxb.snx.*;

public class TestSnxUnmarshaller {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws JAXBException, ParserConfigurationException, SAXException, IOException{
		
		Snx s = SnxUnmarshaller.unmarshall(new FileInputStream("snx.xml"));
		System.out.println("Unit="+s.getUnit().get(0).getId());

	}

}
