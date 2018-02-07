/**
 * 
 */
package com.matson.tos.messageHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.bluecast.xml.XMLStreamReader;
import com.matson.text.bind.JATBContext;
import com.matson.text.bind.JATBException;
import com.matson.tos.exception.TosException;

/**
 * @author AZA
 *
 */
public class ManifestMessageHandler extends AbstractMessageHandler {
	private static Logger logger = Logger.getLogger(ManifestMessageHandler.class);
	public ManifestMessageHandler(String textObjPackageName, String fmtFile,
			int convertDir) throws TosException {
		super(textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	public ManifestMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);		
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getXmlObj() throws TosException {
		if (this.getTextObj() == null) throw new TosException("Manifest Text Object Cannot be Null");
		//this.setXmlStr((String)this.getTextObj());
		JAXBContext jc = null;Unmarshaller u = null;
		try {
			jc = JAXBContext.newInstance( "com.matson.tos.jaxb.snx" );
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			u = jc.createUnmarshaller();
		} catch (JAXBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		try {						
			return u.unmarshal(new StringReader((String)getTextObj()));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {

		return null;
	}

}
