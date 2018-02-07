package com.matson.tos.nascent;

import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.messageHandler.AbdxJSONMessageHandler;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.JMSSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by psethuraman on 3/30/2016.
 */
@Path("/hazmatinterface")
public class HazmatHazRefreshInterface {
    private static Logger logger = Logger.getLogger(HazmatHazRefreshInterface.class);
    String supportTOSMail = "1aktosdevteam@matson.com";//TosRefDataUtil.getValue("SUPPORT_EMAIL");
    String hazErrorHazMails = "1aktosdevteam@matson.com";;

    @GET
    @Path("/refreshunithaz")
    @Produces(MediaType.TEXT_XML)
    public List<Snx> refreshUnitHazmatData (
            @QueryParam("equipmentId") String equipmentId,
            @QueryParam("facilityId") String facilityId,
            @QueryParam("transitState") String transitState) throws Exception{
        try {
            logger.info("Received request for Haz-Refresh with EqId : "+equipmentId
            + ", facilityId : "+facilityId + ", transit State : "+transitState);
            AbdxJSONMessageHandler jsonMessageHandler = new AbdxJSONMessageHandler();
            logger.info("AbdxJSONMessageHandler instance created" + jsonMessageHandler);
            List<Snx> snxList = jsonMessageHandler.getSNXMessage(null, equipmentId, facilityId);
            for (Snx snx : snxList) {
                String fromSnxObject = jsonMessageHandler.getXMLStringFromSnxObject(snx);
                logger.info("Sending Unit HAZ SNX : "+fromSnxObject);
                logger.info("To Facility ==> " + facilityId);
                JMSSender sender = new JMSSender(JMSSender.REAL_TIME_QUEUE, facilityId);
                sender.send(fromSnxObject);
            }
            return snxList;
        } catch (Exception e) {
            logger.error("Errored out while finding hazard details from HAZMAT System for : "
                    +equipmentId);
            e.printStackTrace();
            String mailText = "Car Message : Errored out while finding hazard details from HAZMAT System for : "+equipmentId;
            mailText+="\nException is\n"+e.getMessage();
            mailText+="\nStackTarce\n"+e.getStackTrace();
            EmailSender.sendMail(supportTOSMail, hazErrorHazMails, "Exception Processing HAZ for ACAR Message: " + e.getMessage(), mailText);
        }
        return null;
    }
}
