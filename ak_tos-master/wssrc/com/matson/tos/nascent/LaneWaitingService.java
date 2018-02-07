package com.matson.tos.nascent;

import com.matson.cas.refdata.mapping.TosLaneStatus;
import com.matson.tos.dao.LaneLightManagerDao;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/lanedetails")
public class LaneWaitingService {

    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";
    private static final Logger logger = Logger.getLogger(LaneWaitingService.class);

    public static String determineQueue(String workstation, String lampInd, String signalled, String laneTimestamp) {
        logger.debug("determineQueue method: workstation value " + workstation);

        if ((workstation == null || ("".equals(workstation.trim()))) && "0".equals(signalled) && validLightSignal(lampInd)) {
            logger.debug("Queue: Ready");
            return "READY";
        } else if (workstation != null && !"".equals(workstation.trim()) && !"Waiting".equalsIgnoreCase(workstation)
                && "1".equals(signalled) && validLightSignal(lampInd)) {
            logger.debug("Queue: Working");
            return "WORKING";
        } else if ("1".equals(signalled) && validLightSignal(lampInd)) {
            logger.debug("Queue: Waiting");

            return "WAITING";
        }

        logger.debug("Queue: return empty value");
        return "NOT IN SERVICE";

    }

    public static String getWaitingTime(String queuestatus, String laneTimestamp) {
        Date laneArrivalDate = null;
        Date currentDate = null;

        logger.debug("getWaitingTime Method: " + "Lane Time stamp value: " + laneTimestamp);

        if ("READY".equalsIgnoreCase(queuestatus) || "NOT IN SERVICE".equalsIgnoreCase(queuestatus))
            return "00:00";

        try {

            //Removing milli seconds from input string
            String laneTimestampStr = laneTimestamp.substring(0, laneTimestamp.indexOf('.'));
            TimeZone UTC = TimeZone.getTimeZone("UTC");

            // Create a UTC formatter
            final SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(UTC);

            laneArrivalDate = formatter.parse(laneTimestampStr);

            logger.debug("Lane Time stamp value after parsing it : " + laneArrivalDate);


        } catch (Exception e) {
            logger.error("determineQueue:: Exception while parsing laneTimestamp", e);
            laneArrivalDate = new Date();
        }

        currentDate = new java.util.Date();

        logger.debug("Current date value : " + currentDate);

        long diff = currentDate.getTime() - laneArrivalDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);

        String formattedSec = String.valueOf(diffSeconds);
        String formattedMin = String.valueOf(diffMinutes);
        String formattedHours = String.valueOf(diffHours);

        if (formattedSec.length() == 1) {
            formattedSec = "0" + formattedSec;
        }
        if (formattedMin.length() == 1) {
            formattedMin = "0" + formattedMin;
        }
        if (formattedHours.length() == 1) {
            formattedHours = "0" + formattedHours;
        }

        logger.debug("Time in seconds after format : " + formattedSec + " seconds.");
        logger.debug("Time in minutes after format : " + formattedMin + " minutes.");
        logger.debug("Time in hours after format : " + formattedHours + " hours.");

        //return just the minutes and seconds
        return formattedMin + ":" + formattedSec;
    }

    public static String getWaitingTimeForSound(String queuestatus, String laneTimestamp) {
        Date laneArrivalDate = null;
        Date currentDate = null;

        logger.debug("getWaitingTime Method: " + "Lane Time stamp value: " + laneTimestamp);

        if ("READY".equalsIgnoreCase(queuestatus) || "NOT IN SERVICE".equalsIgnoreCase(queuestatus))
            return "00:00";

        try {

            //Removing milli seconds from input string
            String laneTimestampStr = laneTimestamp.substring(0, laneTimestamp.indexOf('.'));
            TimeZone UTC = TimeZone.getTimeZone("UTC");

            // Create a UTC formatter
            final SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(UTC);

            laneArrivalDate = formatter.parse(laneTimestampStr);

            logger.debug("Lane Time stamp value after parsing it : " + laneArrivalDate);


        } catch (Exception e) {
            logger.error("determineQueue:: Exception while parsing laneTimestamp", e);
            laneArrivalDate = new Date();
        }

        currentDate = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();

        logger.info("Current date value : " + currentDate);

        long diff = currentDate.getTime() - laneArrivalDate.getTime();
        /*if (diff > 1000 * 60 * 5) {
            return HIGH;
        } else */
        if (diff >= 1000 * 60 * 2) {
            return HIGH;
        } else if (diff > 0) {
            return LOW;
        }
        return LOW;
    }

    public static boolean validLightSignal(String lampIndicator) {
        if ("G".equalsIgnoreCase(lampIndicator))
            return true;

        return false;
    }

    @GET
    @Path("/lanestatus/{laneid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLaneStatus(@javax.ws.rs.PathParam("laneid") String inLaneId) {
        TosLaneStatus tosLaneStatus = getPageCapacityAndPageRemaining(inLaneId);
        String queue = determineQueue(tosLaneStatus.getWorkstation(), tosLaneStatus.getLamp_Indicator(),
                Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp());
        String volume = getWaitingTimeForSound(queue, tosLaneStatus.getLane_Timestamp());
        String workStation = tosLaneStatus.getWorkstation();
        String consoleId = TosRefDataUtil.getValue(workStation);
        logger.debug("Console found : "+consoleId);
        if (consoleId == null || consoleId.trim() == "") {
            consoleId = workStation;
        }

        return "{\"lanestatus\": \"" + queue + "\","+
                "\"workstation\":\"" + consoleId + "\"," +
                "\"volume\":\"" + volume + "\"}";
    }

    @GET
    @Path("/lanewaittime/{laneid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getLaneWaitingTime(@PathParam("laneid") String inLaneId) {
        TosLaneStatus tosLaneStatus = getPageCapacityAndPageRemaining(inLaneId);
        String time = getWaitingTime(determineQueue(tosLaneStatus.getWorkstation(), tosLaneStatus.getLamp_Indicator(),
                Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp()), tosLaneStatus.getLane_Timestamp());
        return "{\"lanewaittime\": \"" + time + "\"}";
    }

    @GET
    @Path("/pages/{laneid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOtherLaneDeatils(@PathParam("laneid") String inLaneId) {
        TosLaneStatus tosLaneStatus = getPageCapacityAndPageRemaining(inLaneId);
        return "{\"pageremaining\": \"" + tosLaneStatus.getPage_Remaining() + "\"," +
                "\"pagecapacity\":\"" + tosLaneStatus.getPage_Capacity() + "\"," +
                "\"lightstatus\":\"" + tosLaneStatus.getLamp_Indicator() + "\"}";
    }

    public TosLaneStatus getPageCapacityAndPageRemaining(String laneId) {

        List<TosLaneStatus> result1 = new ArrayList();
        LaneLightManagerDao waitingService = new LaneLightManagerDao();
        result1 = waitingService.getTosLaneStatusDetails(laneId);
        logger.debug("getPageCapacityAndPageRemaining:: Set result1");
        Iterator itr1 = result1.iterator();
        while (itr1.hasNext()) {
            TosLaneStatus tosLaneStatus = (TosLaneStatus) itr1.next();
            logger.debug("getPageCapacityAndPageRemaining:: Set in else");
            if (tosLaneStatus != null) {
                return tosLaneStatus;
            }
        }
        return new TosLaneStatus();
    }
}
