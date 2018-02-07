package com.matson.tos.messageHandler;

import com.matson.cas.refdata.mapping.TosVgxMessageMt;
import com.matson.tos.dao.VgxDAO;
import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Avgx;
import com.matson.tos.processor.AvgxMessageProcessor;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.GroovyXmlUtil;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by psethuraman on 5/21/2016.
 */
public class AvgxMessageHandler extends AbstractMessageHandler {
    public static final String CLASS_LOC = "database";
    public static final String CLASS_NAME = "GvyInjAvgx";
    private static Logger logger = Logger.getLogger(AvgxMessageHandler.class);

    private String facilityToPost = "ANK";
    private static String emailAddr = TosRefDataUtil.getValue( "VGX_SUPPORT_EMAIL");
    public AvgxMessageHandler(String xmlObjPackageName,
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
        Avgx textObj = (Avgx)createTextObj();
        populateData(data, textObj);
        logger.info("Populating SNX with data of size " + data.size());
        for(String s:data.keySet()){
            logger.trace(s + " " + data.get(s));
        }
        return GroovyXmlUtil.getInjectionXmlStr( CLASS_NAME, CLASS_LOC, data);
    }

    private void populateData(Map<String, String> data, Avgx textObj) throws TosException {
        AvgxMessageProcessor avgxProcessor = new AvgxMessageProcessor();
        Boolean activeUnit = isActiveUnit(textObj, avgxProcessor);
        if (activeUnit)
            data.putAll(populateAndReturnValueMap(textObj, avgxProcessor));
        if (avgxProcessor.getErrorMessages().toString().length() > 0) {
            String mailBody = "For Inout String " + _textStr + " the following error occurred " + avgxProcessor.getErrorMessages().toString();
            EmailSender.sendMail(emailAddr, emailAddr, "VGX - Process failures", mailBody);
        }
    }

    private Boolean isActiveUnit(Object textObj, AvgxMessageProcessor avgxProcessor) throws TosException {
        Avgx avgx = (Avgx) textObj;
        logger.info("Avgx object created : "+avgx);
        //AvgxMessageProcessor avgxProcessor = new AvgxMessageProcessor();
        Boolean isActiveUnit = Boolean.FALSE;
        try {
            TosVgxMessageMt vgxEntity = avgxProcessor.copyVGXMessagetoEntity(avgx);
            vgxEntity.setCreateDate(new Date());
            vgxEntity.setUpdatedDate(new Date());
            avgxProcessor.setErrorMessages(new StringBuffer());
            isActiveUnit = avgxProcessor.isActiveUnit(vgxEntity);
            facilityToPost = avgxProcessor.getFacilityToPost();
            if (isActiveUnit) {
                Map<String, String> ctrbooking = new HashMap<String, String>();
                ctrbooking.put(vgxEntity.getContainerNumber(), vgxEntity.getBookingNumber());
                VgxDAO.deleteVgxData(ctrbooking);
            }else{
                avgxProcessor.persistVgxEntity(vgxEntity);
                avgxProcessor.getErrorMessages().append(vgxEntity.getContainerNumber() + vgxEntity.getContainerCheckDigit() + ">> No ACTIVE/YARD unit found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("\n"+avgx.getEquipmentNumber()+" >> Exception while populating " +
                    "SNX for AVGX :"+e.getMessage()+avgxProcessor.getErrorMessages().toString());
            avgxProcessor.getErrorMessages().append("\n\r"+avgx.getEquipmentNumber()+" >> Exception while populating " +
                    "SNX for AVGX :"+e.getMessage());

            throw new TosException(avgx.getEquipmentNumber()+" >> Exception while populating " +
                    "SNX for AVGX :"+e.getMessage()+"\n"+avgxProcessor.getErrorMessages().toString());
        }
        try {
            if (avgxProcessor.getErrorMessages() != null && avgxProcessor.getErrorMessages().length() > 0) {
                logger.error(" Error while processing AVGX messages : "+ avgxProcessor.getErrorMessages().toString());
                EmailSender.sendMail(emailAddr, emailAddr, "VGX - Process failures", avgxProcessor.getErrorMessages().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isActiveUnit;
    }

    /**
     * <p>Method to return Map of required values for Groovy Injection, here it is populated with values for with equipmentNumber,bookingNumber,grossWeight(KG)</p>
     * <p>return Collections.EMPTY_MAP if the values are not present</p>
     *
     * @param inAvgx AVGX object
     * @return Map with values equipmentNumber,bookingNumber,grossWeight(KG)
     */
    private Map<String, String> populateAndReturnValueMap(Avgx inAvgx, AvgxMessageProcessor processor) {
        if (inAvgx == null) return Collections.EMPTY_MAP;
        if (inAvgx.getEquipmentNumber() == null) return Collections.EMPTY_MAP;
        //AvgxMessageProcessor processor = new AvgxMessageProcessor();
        Map<String, String> data = new HashMap<String, String>();
        if (inAvgx.getEquipmentNumber() != null && inAvgx.getEquipmentNumber() != null) {
            data.put("equipmentNumber", inAvgx.getEquipmentNumber());
            logger.info("equipmentNumber    " + inAvgx.getEquipmentNumber());
            data.put("bookingNumber", inAvgx.getBookingNumber());
            logger.info("bookingNumber  " + inAvgx.getBookingNumber());
            if (inAvgx.getVerifierId() != null && !inAvgx.getVerifierId().isEmpty())
                data.put("verifierId", inAvgx.getVerifierId().trim());
            logger.info("verifierId" + inAvgx.getVerifierId().trim());
            logger.info("UOM" + inAvgx.getWeightUOM());
            if (inAvgx.getWeightUOM() != null && !inAvgx.getWeightUOM().isEmpty() &&
                    ("LBS".equalsIgnoreCase(inAvgx.getWeightUOM().trim()) || "L".equalsIgnoreCase(inAvgx.getWeightUOM().trim()) || "LB".equalsIgnoreCase(inAvgx.getWeightUOM().trim()))) {
                if (inAvgx.getGrossWeight() != null && !inAvgx.getGrossWeight().isEmpty() && !"0".equals(inAvgx.getGrossWeight().trim())) {
                    logger.info("UOM is LB and grossweight is" + inAvgx.getGrossWeight());
                    data.put("grossWeight", processor.convertLbstoKg(inAvgx.getGrossWeight().trim()));
                } else if (inAvgx.getTareWeight() != null && !inAvgx.getTareWeight().isEmpty() && !"0".equals(inAvgx.getTareWeight().trim())
                        && inAvgx.getCargoWeight() != null && !inAvgx.getCargoWeight().isEmpty() && !"0".equals(inAvgx.getCargoWeight().trim())) {
                    logger.info("UOM is LB and cargoweight is" + inAvgx.getCargoWeight() + " dunage is " + inAvgx.getDunnageWeight() + " tareWeight is " + inAvgx.getTareWeight());
                    String combinedDunnageCargoWt = processor.calculateCargoWithDunnage(inAvgx.getCargoWeight().trim(), inAvgx.getDunnageWeight());
                    data.put("grossWeight", processor.convertLbstoKg(String.valueOf(Double.parseDouble(combinedDunnageCargoWt) + Double.parseDouble(inAvgx.getTareWeight().trim()))));
                }
            } else if (inAvgx.getGrossWeight() != null && !inAvgx.getGrossWeight().isEmpty() && !"0".equals(inAvgx.getGrossWeight().trim())) {
                data.put("grossWeight", processor.convertLbstoKg(inAvgx.getGrossWeight().trim()));
                data.put("grossWeight", inAvgx.getGrossWeight().trim());
            } else if (inAvgx.getTareWeight() != null && !inAvgx.getTareWeight().isEmpty() && !"0".equals(inAvgx.getTareWeight().trim())
                    && inAvgx.getCargoWeight() != null && !inAvgx.getCargoWeight().isEmpty() && !"0".equals(inAvgx.getCargoWeight().trim())) {
                logger.info("UOM is KGS and cargoweight is" + inAvgx.getCargoWeight() + " dunage is " + inAvgx.getDunnageWeight() + " tareWeight is " + inAvgx.getTareWeight());
                String combinedDunnageCargoWt = processor.calculateCargoWithDunnage(inAvgx.getCargoWeight().trim(), inAvgx.getDunnageWeight());
                data.put("grossWeight", String.valueOf(Double.parseDouble(combinedDunnageCargoWt) + Double.parseDouble(inAvgx.getTareWeight().trim())));
            } else {
                processor.getErrorMessages().append("The VGX cannot be parsed it is missing one of the required parameters, " +
                        "Booking Number or Equipment Number or [(Unit of Measure and) " +
                        " grossweight or [(Unit of Measure and) Tare and Cargo Weight");
                data = Collections.EMPTY_MAP;
            }
        }
        return data;
    }
}
