package com.matson.tos.processor;

import com.matson.cas.refdata.mapping.TosVgxMessageMt;
import com.matson.tos.dao.TosLookup;
import com.matson.tos.dao.VgxDAO;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Avgx;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.messageHandler.AbstractJSONMessageHandler;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by brajamanickam on 7/17/2017.
 */
public class ChassisUpdateMessageProcessor extends AbstractFileProcessor {
    private static Logger logger = Logger.getLogger(AvgxMessageProcessor.class);
    private static String emailAddr = TosRefDataUtil.getValue( "VGX_SUPPORT_EMAIL");
    public static final String CLASS_LOC = "database";
    public static final String CLASS_NAME = "GvyInjChsUpdt";
    private String ANK = "ANK";
    private String KDK = "KDK";
    private String DUT = "DUT";
    private String facilityToPost = ANK;
    private StringBuffer errorMessages = new StringBuffer();

    public  ChassisUpdateMessageProcessor() {
    }

    @Override
    protected void processLine(String aLine, int lineNum) throws TosException {
        // TODO Auto-generated method stub

    }

    public void validateChassisInspectionDate() {
     //   reProcessChassisUpdateData();
    }








}
