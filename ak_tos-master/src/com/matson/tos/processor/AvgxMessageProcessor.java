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
 * Created by psethuraman on 5/24/2016.
 */
public class AvgxMessageProcessor extends AbstractFileProcessor {
    private static Logger logger = Logger.getLogger(AvgxMessageProcessor.class);
    private static String emailAddr = TosRefDataUtil.getValue( "VGX_SUPPORT_EMAIL");
    public static final String CLASS_LOC = "database";
    public static final String CLASS_NAME = "GvyInjAvgx";
    private String ANK = "ANK";
    private String KDK = "KDK";
    private String DUT = "DUT";
    private String facilityToPost = ANK;
    private StringBuffer errorMessages = new StringBuffer();

    public  AvgxMessageProcessor() {
    }

    @Override
    protected void processLine(String aLine, int lineNum) throws TosException {
        // TODO Auto-generated method stub

    }

    public void processFiles() {
        reProcessVgxData();
    }

    public void reProcessVgxData() {
        logger.debug("VGX Process triggered to reprocess stored data : ");
        List<TosVgxMessageMt> vgxData = VgxDAO.getAllVgxRecords();
        boolean processedVGX = false;
        try {
            if (vgxData != null && vgxData.size() > 0) {
                Map<String, String> ctrBooking = new HashMap<String, String>();
                List<String> ankList = new ArrayList<String>();
                List<String> kdkList = new ArrayList<String>();
                List<String> dutList = new ArrayList<String>();
                errorMessages = new StringBuffer();
                for (TosVgxMessageMt vgx : vgxData) {
                    if (vgx.getContainerNumber() != null) {
                        logger.debug("Processing Stored VGX data to send : " + vgx.getContainerNumber());
                        Boolean isUnitInYard = isActiveUnit(vgx);
                        //only if unit active
                        if (isUnitInYard) {
                        Map<String, String> stringMap = populateAndReturnValueMap(vgx);
                        String getSnxString = GroovyXmlUtil.getInjectionXmlStr(CLASS_NAME, CLASS_LOC, stringMap);

                            if (ANK.equalsIgnoreCase(facilityToPost)) {
                                ankList.add(getSnxString);
                            } else if (KDK.equalsIgnoreCase(facilityToPost)) {
                                kdkList.add(getSnxString);
                            } else if (DUT.equalsIgnoreCase(facilityToPost)) {
                                dutList.add(getSnxString);
                            }
                            ctrBooking.put(vgx.getContainerNumber().trim(), vgx.getBookingNumber());
                        }
                    }
                }
                try {
                    sendSnxToTos(ankList, ANK);
                    sendSnxToTos(kdkList, KDK);
                    sendSnxToTos(dutList, DUT);
                    processedVGX = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error during sending SNX with VGX to TOS : " + e);
                    errorMessages.append("\nError during sending SNX with VGX to TOS : " + e);
                }
                if (processedVGX && ctrBooking.size() > 0) {
                    VgxDAO.deleteVgxData(ctrBooking);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "\nException is\n" + e.getMessage();
            errorMessages.append(errorMessage);
        }
        if (errorMessages != null && errorMessages.length() > 0) {
//            EmailSender.sendMail(emailAddr, emailAddr, "VGX - Process failures", errorMessages.toString());
        }
    }

    public Boolean isActiveUnit(TosVgxMessageMt vgxData) {
        logger.debug("Validation for Yard Unit Availability : Start");
        TosLookup tosLookUp = null;

        try {
            if (tosLookUp == null) {
                tosLookUp = new TosLookup();
            }
            if (vgxData.getContainerNumber() != null) {
                HashMap<String, String> unitCarrierDetails = tosLookUp.getUnitCarrierDetailsForFacility(vgxData.getContainerNumber(), vgxData.getBookingNumber());
                if (unitCarrierDetails != null) {
                    facilityToPost = unitCarrierDetails.get("FACILITY");
                    return Boolean.TRUE;
                }
            } else {
                logger.info("No ACTIVE/YARD unit found for " + vgxData.getContainerNumber() + " : So, persiting the VGX data into table");
                errorMessages.append("\n\r" + vgxData.getContainerNumber() + " >> No ACTIVE/YARD unit found \n");
                persistVgxEntity(vgxData);
                return Boolean.FALSE;
            }

        } catch (Exception e) {
            logger.error("Error while populating SNX for VGX : " + e.getMessage());
            e.printStackTrace();
            errorMessages.append("\n" + vgxData.getContainerNumber() + " >> Unable to populate VGX-SNX :" + e.getMessage());
            persistVgxEntity(vgxData);
        } finally {
            if (tosLookUp != null) {
                tosLookUp.close();
            }
        }
        logger.debug("Validation for Yard Unit Availability : End");
        return Boolean.FALSE;
    }

    public void persistVgxEntity(TosVgxMessageMt vgxEntity) {
        VgxDAO.insertVgxData(vgxEntity);
    }

    public TosVgxMessageMt copyVGXMessagetoEntity(Avgx avgx) {
        logger.debug("Copying VGX properties to VGX entity for : <"+avgx.getEquipmentNumber()+">");
        TosVgxMessageMt vgxEntity = VgxDAO.getTosVgxMessageMt(avgx.getEquipmentNumber(), avgx.getBookingNumber());
        if (vgxEntity == null) {
            logger.info("New VGX message for container");
            vgxEntity = new TosVgxMessageMt();
        } else {
            logger.info("Container for VGX message exists in DB, using that");
        }
        vgxEntity.setContainerNumber(avgx.getEquipmentNumber() != null ? avgx.getEquipmentNumber().trim() : "");
        vgxEntity.setBookingNumber(avgx.getBookingNumber() != null ? avgx.getBookingNumber().trim(): "");
        vgxEntity.setGrossWt(avgx.getGrossWeight() != null ? avgx.getGrossWeight().trim() : "");//todo grosswt = cargowt + tareweight
        vgxEntity.setCargoWt(avgx.getCargoWeight() != null ?
                calculateCargoWithDunnage(avgx.getCargoWeight().trim(), avgx.getDunnageWeight()) : "");
        vgxEntity.setTareWt(avgx.getTareWeight() != null ? avgx.getTareWeight().trim() : "");
        vgxEntity.setWeightUOM(avgx.getWeightUOM() != null ? avgx.getWeightUOM().trim() : "");
        vgxEntity.setVgmVerifier(avgx.getVerifierId() != null ? avgx.getVerifierId().trim() : "");
        logger.debug("Copied VGX properties to VGX entity for : <"+vgxEntity.getContainerNumber()+">");
        return vgxEntity;
    }

    public String calculateCargoWithDunnage(String cargoWt, String dunnageWt) {
        String totalCargo = cargoWt;
        Double cargoWtVal = new Double("0.0");
        Double dunnageWtVal = new Double("0.0");
        if (cargoWt != null && !cargoWt.isEmpty()) {
            try {
                cargoWtVal = Double.parseDouble(cargoWt);
            } catch (Exception e) {
                logger.error("ERROR : while converting cargo wt "+cargoWt);
            }
        }
        if (dunnageWt != null && !dunnageWt.isEmpty()) {
            try {
                dunnageWtVal = Double.parseDouble(dunnageWt);
            } catch (Exception e) {
                logger.error("ERROR : while converting dunnage wt "+dunnageWt);
            }
        }
        try {
            if (cargoWtVal != null && dunnageWtVal != null && dunnageWtVal > 0) {
                cargoWtVal = cargoWtVal + dunnageWtVal;
                totalCargo = cargoWtVal.toString();
            }
        } catch (Exception e) {
            logger.error("ERROR : while calculating cargo + dunnage wt "+totalCargo);
        }
        return totalCargo;
    }

    private void sendSnxToTos(List<String> unitSnxs, String facility) throws Exception {
        if (unitSnxs != null && unitSnxs.size() > 0) {
            for (String tUnit : unitSnxs) {
                logger.info("Sending SNX to : " + facility);
                JMSSender sender1 = new JMSSender(JMSSender.REAL_TIME_QUEUE, facility);
                logger.info(sender1.toString());
                logger.info("XML for MQ :" + tUnit);
                sender1.send(tUnit);
                Thread.sleep(5000L);
            }
        }
    }

    public String convertLbstoKg(String lbs) {
        try {
            double e = Double.parseDouble(lbs);
            double kg = e / 2.2046D;
            return Double.toString(kg);
        } catch (Exception var6) {
            return lbs;
        }
    }

    public StringBuffer getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(StringBuffer errorMessages) {
        this.errorMessages = errorMessages;
    }

    public String getFacilityToPost() {
        return facilityToPost;
    }

    public void setFacilityToPost(String facilityToPost) {
        this.facilityToPost = facilityToPost;
    }

    private Map<String, String> populateAndReturnValueMap(TosVgxMessageMt inTosVgxMessageMt) {
        if (inTosVgxMessageMt == null) return Collections.EMPTY_MAP;
        if (inTosVgxMessageMt.getContainerNumber() == null) return Collections.EMPTY_MAP;
        Map<String, String> data = new HashMap<String, String>();
        if (inTosVgxMessageMt.getContainerNumber() != null && inTosVgxMessageMt.getBookingNumber() != null) {
            data.put("equipmentNumber", inTosVgxMessageMt.getContainerNumber());
            data.put("bookingNumber", inTosVgxMessageMt.getBookingNumber());
            if (inTosVgxMessageMt.getVgmVerifier() != null && !inTosVgxMessageMt.getVgmVerifier().isEmpty()) {
                data.put("verifierId", inTosVgxMessageMt.getVgmVerifier().trim());
            }
            if (inTosVgxMessageMt.getWeightUOM() != null && !inTosVgxMessageMt.getWeightUOM().isEmpty() &&
                    ("LBS".equalsIgnoreCase(inTosVgxMessageMt.getWeightUOM()) || "L".equalsIgnoreCase(inTosVgxMessageMt.getWeightUOM()) || "LB".equalsIgnoreCase(inTosVgxMessageMt.getWeightUOM()))) {
                if (inTosVgxMessageMt.getGrossWt() != null && !inTosVgxMessageMt.getGrossWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getGrossWt().trim())) {
                    data.put("grossWeight", convertLbstoKg(inTosVgxMessageMt.getGrossWt().trim()));
                } else if (inTosVgxMessageMt.getTareWt() != null && !inTosVgxMessageMt.getTareWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getTareWt().trim())
                        && inTosVgxMessageMt.getCargoWt() != null && !inTosVgxMessageMt.getCargoWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getCargoWt().trim())) {
                    data.put("grossWeight", String.valueOf(Double.parseDouble(inTosVgxMessageMt.getTareWt().trim()) + Double.parseDouble(inTosVgxMessageMt.getCargoWt().trim())));
                }
            } else if (inTosVgxMessageMt.getGrossWt() != null && !inTosVgxMessageMt.getGrossWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getGrossWt().trim())) {
                data.put("grossWeight", inTosVgxMessageMt.getGrossWt().trim());
            } else if (inTosVgxMessageMt.getTareWt() != null && !inTosVgxMessageMt.getTareWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getTareWt().trim())
                    && inTosVgxMessageMt.getCargoWt() != null && !inTosVgxMessageMt.getCargoWt().isEmpty() && !"0".equals(inTosVgxMessageMt.getCargoWt().trim())) {
                data.put("grossWeight", String.valueOf(Double.parseDouble(inTosVgxMessageMt.getTareWt().trim()) + Double.parseDouble(inTosVgxMessageMt.getCargoWt().trim())));
            } else {
                errorMessages.append("The VGX cannot be parsed it is missing one of the required parameters, " +
                        "Booking Number or Equipment Number or [(Unit of Measure and) " +
                        " grossweight or [(Unit of Measure and) Tare and Cargo Weight");
                data = Collections.EMPTY_MAP;
            }
        }
        return data;
    }
}
