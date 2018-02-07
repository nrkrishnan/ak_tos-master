package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Cupd;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.*;

/**
 * Created by brajamanickam 7/17/2017.
 */
public class ChassisUpdateMessageHandler extends AbstractMessageHandler {



    public static final String CLASS_LOC = "database";
    public static final String CLASS_NAME = "GvyInjCupd";
    String mailBody;
    private static Logger logger = Logger.getLogger(ChassisUpdateMessageHandler.class);

    private String facilityToPost = "ANK";
    private static String emailAddr = TosRefDataUtil.getValue("VGX_SUPPORT_EMAIL");

    public ChassisUpdateMessageHandler(String xmlObjPackageName,
                                       String textObjPackageName, String fmtFile, int convertDir)
            throws TosException {
        super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Object textObjToXmlObj(Object textObj) throws TosException {
        return createXmlStr();
    }

    @Override
    protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
        return null;
    }

    public String getFacilityToPost() {
        return facilityToPost;
    }

    public void setFacilityToPost(String facilityToPost) {
        this.facilityToPost = facilityToPost;
    }

    protected String createXmlStr() throws TosException {
        Map<String, String> data = new HashMap<String, String>();
        Cupd textObj = (Cupd) createTextObj();
        logger.debug("ChassisUpdt  textObj " + textObj);
        populateData(data, textObj);
        logger.debug("Populating SNX with data of size " + data.size());
        for (String s : data.keySet()) {
            logger.debug(s + " " + data.get(s));
        }
        return GroovyXmlUtil.getInjectionXmlStr(CLASS_NAME, CLASS_LOC, data);
    }

    private void populateData(Map<String, String> data, Cupd textObj) throws TosException {

        try {

            data.putAll(populateAndReturnValueMap(textObj));

        } catch (Exception e) {
            logger.error("Error occured while processing the CUPD message " + e.getMessage());
            mailBody = "For Input String " + getTextStr() + " the following error occurred while processing message " + e.getMessage();
            EmailSender.sendMail(emailAddr, emailAddr, "CUPD - Process failures", mailBody);
            throw new TosException("Exception while populating SNX for CUPD :" + e.getMessage());
        }

    }


    /**
     * <p>Method to return Map of required values for Groovy Injection, here it is populated with values for with chassisnumber,inspectionduedate,inspectiondate</p>
     * <p>return Collections.EMPTY_MAP if the values are not present</p>
     *
     * @param inChassisUpdateObj Chassisupdate object
     * @return Map with values chassisnumber,inspectionduedate,inspectiondate
     */
    private Map<String, String> populateAndReturnValueMap(Cupd inChassisUpdateObj) throws TosException {

        Map<String, String> data = new HashMap<String, String>();
        try {

            if (inChassisUpdateObj == null) {
                logger.error("Cupd object found null   " + inChassisUpdateObj);
                return Collections.EMPTY_MAP;
            }


             String chassisNumber=inChassisUpdateObj.getChassisNumber()==null? "" : inChassisUpdateObj.getChassisNumber();
             String inspectionDueDate=inChassisUpdateObj.getInspectionDuedate()==null ? "" :inChassisUpdateObj.getInspectionDuedate();
             String inspectionDate=inChassisUpdateObj.getInspectionDate()==null ? "" :inChassisUpdateObj.getInspectionDate();


            logger.debug("Calling chassisNumber " + chassisNumber +"inspectionDueDate-->"+inspectionDueDate +"inspectionDate -->"+inspectionDate);


            if (chassisNumber.trim().isEmpty()) {
                logger.error(" CUPD message has no value for the chassis number  ");
                mailBody = "Chassis number not found in the  received  GEMS message : " ;
                logger.error("mailBody "+ mailBody);
                EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Chassis Number not found", mailBody);
                logger.error("After sending mailBody");
                return Collections.EMPTY_MAP;
            }

            if (inspectionDueDate.trim().isEmpty()) {
                logger.error(" CUPD message has no value for the inspection due-date   " + inspectionDueDate + " chassis id " + chassisNumber);
                mailBody = "Inspection due-date not found in the  received  GEMS message for the chassis id "+ chassisNumber;
                EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Inspection due-date not found", mailBody);
                return Collections.EMPTY_MAP;
            }

            if (inspectionDueDate != null && !inspectionDueDate.isEmpty()
                    && inspectionDueDate.trim().length() != 8) {

                    logger.error("Invalid chassis inspection due date received from GEMS"+ inspectionDueDate);
                    mailBody = " Invalid chassis inspection due date in received from GEMS  : " + inspectionDueDate  + " for the chassis id "+ chassisNumber;
                    EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Inspection due-date format invalid", mailBody);
                    return Collections.EMPTY_MAP;

            }


            if (inspectionDueDate != null && !inspectionDueDate.isEmpty()
                    && inspectionDueDate.trim().length() == 8) {

                try {

                    Date date = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(inspectionDueDate);

                }catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error occured while parsing the chassis inspection due date"+e.getMessage());

                    mailBody = " Error while parsing chassis inspection due-date as "+inspectionDueDate + " for chassis id "+chassisNumber +" received from GEMS message  : Error :" +e.getMessage();
                    EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Inspection due-date  parsing error ", mailBody);
                    return Collections.EMPTY_MAP;

                }


            }

            if (inspectionDate.trim().isEmpty()) {
                logger.debug(" CUPD message has no value for the inspection date  for chassisNumber  " + chassisNumber +" and  inspectionDate "+inspectionDate);
                mailBody = "Inspection date not found in the  received  GEMS message for the  chassis number  : " + chassisNumber ;
               // EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Inspection date not found", mailBody);
            }



            logger.debug("Calling populateAndReturnValueMap chassis number from CUPD message " + chassisNumber);


            if (chassisNumber != null && !chassisNumber.isEmpty()) {

                data.put("chassisNumber", chassisNumber.trim());

                logger.debug("chassisNumber    " + chassisNumber.trim());

                if (inspectionDueDate != null && !inspectionDueDate.isEmpty()) {
                    data.put("inspectionDuedate", inspectionDueDate.trim());
                    logger.debug("inspectionDuedate" + inspectionDueDate.trim());

                }

                if (inspectionDate != null && !inspectionDate.isEmpty()) {

                    data.put("inspectionDate", inspectionDate.trim());
                    logger.debug("inspectionDate" + inspectionDate.trim());

                }

            }

            logger.debug("Chassis CUPD parameter size    " + data.size());

            if (data.isEmpty()) {
                data = Collections.EMPTY_MAP;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error occured while populating chassis update data  in the method populateAndReturnValueMap ");
            mailBody = "Error occured while processing chassis update  message error as : " +e.getMessage() ;
            EmailSender.sendMail(emailAddr, emailAddr, "CUPD message - Inspection date not found", mailBody);

            throw new TosException("Exception while populating SNX for CUPD :" + e.getMessage());

        }
        return data;
    }
}