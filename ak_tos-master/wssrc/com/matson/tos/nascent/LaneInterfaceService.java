package com.matson.tos.nascent;

import com.matson.cas.refdata.mapping.TosLaneStatus;
import com.matson.tos.nascent.webservice.InterfaceControlLocator;
import com.matson.tos.nascent.webservice.InterfaceControlSoap_BindingStub;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;

@Path("/laneinterface")
public class LaneInterfaceService {

    private static Logger logger = Logger.getLogger(com.matson.tos.nascent.LaneInterfaceService.class);

    @GET
    @Path("/connect")
    @Produces(MediaType.TEXT_XML)
    public Response connectToLane(
            @QueryParam("clerkId") String clerkId,
            @QueryParam("laneId") String laneId) throws Exception{

        InterfaceControlLocator laneInterfaceCtrlLoc = new InterfaceControlLocator();
        InterfaceControlSoap_BindingStub proxy = null;
        boolean isConnected = false;

        logger.info("Start of the connectToLane method");

        //validate input
        if (clerkId == null || clerkId.isEmpty() || laneId == null || laneId.isEmpty()) {

            String output = "<?xml version=\"1.0\"?><Laneconnect><Error>Invalid Input</Error></Laneconnect>";
            logger.info("Input details ::::: clerkId " + clerkId + " laneId :" + laneId);
            logger.info("Lane connect service Responce :::::" + output);

            logger.info("End of the connectToLane method");
            return Response.status(400).entity(output).build();
        }

        // do not allow N4 request to connect Nascent webservice while previous request is already in-progress.
        if (!isValidStateToCallNascentSvc(laneId)){

            String output = "<?xml version=\"1.0\"?><Laneconnect><Error>Lane \" + laneId + \" doesn't have active light indicator to connect</Error></Laneconnect>";
            logger.info("Lane connect service Responce :::::" + output);

            logger.info("End of the connectToLane method");
            return Response.status(409).entity(output).build();
        }

        try {
            proxy = (InterfaceControlSoap_BindingStub)laneInterfaceCtrlLoc.getInterfaceControlSoap();
            // Time out after a minute
            proxy.setTimeout(60000);

            logger.info("Before calling Nascent Lane Connect Service: " + "ClerkId : " + clerkId
                    + " laneId: " + laneId);

            isConnected = proxy.laneConnect(clerkId, laneId, "", "");
        }
        catch (javax.xml.rpc.ServiceException jre) {
            logger.info("Exception occurred while calling Nascent Lane connect webservice");
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new Exception("Remote procedure ServiceException caught: " + jre);
        }

        String output = "<?xml version=\"1.0\"?><Laneconnect><WorkstationConfigResult>" +isConnected + "</WorkstationConfigResult></Laneconnect>";
        logger.info("Lane connect service Responce :::::" + output);

        //update Nascent SQL DB with client workstation name for a given laneId
        com.matson.tos.dao.LaneLightManagerDao laneLightManagerDao = new com.matson.tos.dao.LaneLightManagerDao();
        laneLightManagerDao.updateClientWorkstation(laneId, clerkId, 1);

        logger.info("End of the connectToLane method");
        return Response.status(200).entity(output).build();
    }

    @GET
    @Path("/disconnect")
    @Produces(MediaType.TEXT_XML)
    public Response disconnectFromLane(
            @QueryParam("laneId") String laneId,
            @QueryParam("clerkId") String clerkId,
            @QueryParam("printTicketCount") String printTicketCount) throws Exception{

        InterfaceControlLocator laneInterfaceCtrlLoc = new InterfaceControlLocator();
        InterfaceControlSoap_BindingStub proxy = null;
        boolean isDisConnected = false;
        int printTktCt = 0;
        if(printTicketCount != null && !printTicketCount.isEmpty())
            printTktCt = Integer.valueOf(printTicketCount);

        logger.info("Start of the disconnectFromLane method");

        //validate input
        if (clerkId == null || clerkId.isEmpty() || laneId == null || laneId.isEmpty()) {

            String output = "<?xml version=\"1.0\"?><LaneDisconnect><Error>Invalid Input</Error></LaneDisconnect>";
            logger.info("Input details ::::: clerkId " + clerkId + " laneId :" + laneId);
            logger.info("Lane disconnect service Responce :::::" + output);

            logger.info("End of the disconnectFromLane method");
            return Response.status(400).entity(output).build();
        }

        // check if lane is good to allow N4 request to disconnect camera.
        if (!isValidStateToCallNascentSvc(laneId)){

            String output = "<?xml version=\"1.0\"?><LaneDisconnect><Error>Lane " + laneId + " doesn't have active light indicator to disconnect</Error></LaneDisconnect>";
            logger.info("Lane disconnect service Responce :::::" + output);

            logger.info("End of the disconnectFromLane method");
            return Response.status(409).entity(output).build();
        }

        try {
            proxy = (InterfaceControlSoap_BindingStub)laneInterfaceCtrlLoc.getInterfaceControlSoap();
            proxy.setTimeout(60000);
            logger.info("Before calling Nascent Lane disconnect Service: " + "ClerkId : " + clerkId );
            isDisConnected = proxy.laneDisconnect(clerkId);
        }
        catch (javax.xml.rpc.ServiceException jre) {
            logger.info("Exception occurred while calling Nascent Lane disconnect webservice");
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new Exception("Remote procedure ServiceException caught: " + jre);
        }

        String output = "<?xml version=\"1.0\"?><LaneDisconnect><WorkstationConfigResult>" +isDisConnected + "</WorkstationConfigResult></LaneDisconnect>";
        logger.info("Lane disconnect service Responce :::::" + output);

        //update Nascent SQL DB with client workstation name as empty for a given laneId
        com.matson.tos.dao.LaneLightManagerDao laneLightManagerDao = new com.matson.tos.dao.LaneLightManagerDao();
        laneLightManagerDao.updateClientWorkstation(laneId, "", 0);

        //reduce the print ticket count
        laneLightManagerDao.updatePageRemaining(laneId, false, printTktCt);

        logger.info("End of the disconnectFromLane method");
        return Response.status(200).entity(output).build();
    }

    private boolean isValidStateToCallNascentSvc(String laneId) {
        com.matson.tos.dao.LaneLightManagerDao laneLightManagerDao =
                new com.matson.tos.dao.LaneLightManagerDao();

        ArrayList<TosLaneStatus> list = null;
        String  workStationName = null;
        int  signalId = -1;
        String lampIndicator = null;

        logger.info("start of the isValidStateToCallNascentSvc method");
        list = laneLightManagerDao.getTosLaneStatusDetails(laneId);

        //have a code to validate the line status
        Iterator itr = list.iterator();

        while (itr.hasNext()) {

            TosLaneStatus tosLaneStatus = (TosLaneStatus) itr.next();
            if (tosLaneStatus != null) {
                logger.info("tosLaneStatus details: " + tosLaneStatus.toString());
                workStationName = tosLaneStatus.getWorkstation();
                signalId = tosLaneStatus.getSignaled();
                lampIndicator = tosLaneStatus.getLamp_Indicator();
                break;
            }
        }

        //Check the lamp indicator value -- cleanUp
        if(!"G".equalsIgnoreCase(lampIndicator)) {
            logger.info("Lamp Indicator is not Green. Hence returning back");
            return false;
        }

        return true;
    }
}