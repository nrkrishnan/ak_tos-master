package com.matson.tos.groovies

import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.business.model.GeneralReference
import com.navis.argo.business.reference.Chassis
import com.navis.argo.web.ArgoGuiMetafield
import com.navis.framework.persistence.HibernateApi
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

class GvyInjCupd extends GroovyInjectionBase {

    private static final Logger LOGGER = Logger.getLogger(GvyInjCupd.class);
    public static final String SAVE_EXECEPTION = "Exception during saving the update of chassis with field";
    public static final String EQUIPMENT_NOT_FOUND = "The Equipment not found";
    public String execute(Map inParameters) {

        LOGGER.info("Calling GvyInjChsUpdt start for unitId: ");

        String equipmentNumber = (String) inParameters.get("chassisNumber");
        String inspectionDuedate = (String) inParameters.get("inspectionDuedate");
        String inspectionDate = (String) inParameters.get("inspectionDate");
        String threadUser = "ACETS";

        equipmentNumber = equipmentNumber != null && !equipmentNumber.isEmpty() ? equipmentNumber.trim() : equipmentNumber;

        LOGGER.info("GvyInjChsUpdt CUPD values : " + equipmentNumber + "  inspectionDuedate-->  " + inspectionDuedate + " inspectionDate -- " + inspectionDate);

        if(equipmentNumber==null || equipmentNumber== ""){

            LOGGER.error("GvyInjChsUpdt equipment number not found "+equipmentNumber);

            return "Fail" ;
        }

        if(inspectionDuedate==null || inspectionDuedate== ""){

            LOGGER.error("GvyInjChsUpdt inspectionDuedate not found "+inspectionDuedate);

            return "Fail" ;
        }

        if (inspectionDate == null  || inspectionDate== "" ) {

            LOGGER.error("GvyInjChsUpdt inspectionDate not found " + inspectionDate);

        }

        com.navis.argo.ContextHelper.setThreadExternalUser(threadUser); // ACETS or ???

        LOGGER.info("GvyInjChsUpdt start for unitId: " + equipmentNumber + "  inspectionDuedate-->  " + inspectionDuedate + " inspectionDate -- " + inspectionDate);

        Chassis groovyChassis = Chassis.findChassis(Chassis.findFullIdOrPadCheckDigit(equipmentNumber));

        LOGGER.info("groovyChassis" + groovyChassis);

        if (groovyChassis != null) {
            LOGGER.error("1 groovyChassis" + groovyChassis);

            if (inspectionDuedate.trim().size() != 8) {

                LOGGER.error("GvyInjChsUpdt invalid inspectionDuedate  format: " +inspectionDuedate );

                return "Fail" ;
            }

            if (inspectionDuedate != null && !inspectionDuedate.isEmpty() && inspectionDuedate.trim().size() == 8) {

                LOGGER.info("new inside for update:" + inspectionDuedate);

                TimeZone tz = Calendar.getInstance().getTimeZone();

                LOGGER.info("new tz -->:" + tz.displayName);

                Date date = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(inspectionDuedate);

                if(date==null) {

                    LOGGER.info("GvyInjChsUpdt invalid date  found : " +date + " for the inspection due date " +inspectionDuedate );

                    return "Fail" ;

                }
                  LOGGER.info("date -->:" + date);
                  Calendar cal = Calendar.getInstance();
                  cal.setTime(date);
                  cal.add(Calendar.HOUR, 1);


                LOGGER.info("inside for date-->:" + cal.getTime());
                groovyChassis.setFieldValue(ArgoGuiMetafield.EQ_FED_INSPECT_EXP, cal.getTime());

                try {
                    HibernateApi.getInstance().save(groovyChassis);
                } catch (Exception ex) {
                    LOGGER.error(SAVE_EXECEPTION + " ", ex);
                }
            } else {
                LOGGER.error("Invalid CUPD message  inspectionDuedate " + inspectionDuedate)

            }
        } else {
            LOGGER.error("Invalid CUPD message Chassis id not found " + equipmentNumber);
            sendFailureMail(equipmentNumber, EQUIPMENT_NOT_FOUND);
            return "Fail" ;
        }
        LOGGER.error ( "End GvyInjChsUpdt post event: " + equipmentNumber );
        return "SUCCESS";
    }

    public void sendFailureMail(String inEquipmentId, String inErrorMessage) {
        GeneralReference genRef = GeneralReference.findUniqueEntryById("ENV", "ENVIRONMENT");
        String environment = genRef.getRefValue1();
        genRef = GeneralReference.findUniqueEntryById("MATSON", "EMAIL", "NOTIFICATION");
        String emailFrom = "1aktosdevteam@matson.com";
        String emailTo = "1aktosdevteam@matson.com";
        String emailSubject = environment + " - CHASSIS UPDATE Failure for the chassis id " + inEquipmentId;
        String emailBody = inErrorMessage;
        sendEmail(emailTo, emailFrom, emailSubject, emailBody);
    }

}