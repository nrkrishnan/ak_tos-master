package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.Snx;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * @author Keerthi Ramachandran
 * @since 6/25/2015
 * <p>AbstractJSONMessageHandler is ..</p>
 */

public abstract class AbstractJSONMessageHandler implements IJSONMessageHandler {
    private static Logger logger = Logger.getLogger(AbdxJSONMessageHandler.class);

    @Override
    public String getXMLStringFromSnxObject(Snx inSnx) throws TosException {
        StringWriter xmlTemp = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(Snx.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(inSnx, xmlTemp);
            return xmlTemp.toString();

        } catch (JAXBException e) {
            e.printStackTrace();
            logger.error("Error in creating xml string for " + inSnx, e);
            throw new TosException("Error in creating xml string for " + inSnx);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unknown Error in creating xml string for " + inSnx, e);
            throw new TosException("Unknown Error in creating xml string for " + inSnx);
        }
    }
}
