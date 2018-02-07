package com.matson.tos.upload;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosLaneStatus;
import com.matson.tos.dao.LaneLightManagerDao;

public class LaneManager extends HttpServlet {

    private static final long serialVersionUID = 123678;
    private static Logger logger = Logger.getLogger(LaneManager.class);
    public static final String PAGE_REMAINING_RESET_DEFAULT = "814";

    // public static final PAGE_REMAINING = 834;

    public void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String page = "LaneManager.jsp";
        try {
            response.setContentType("text/html");
            String laneId = "";
            String pageRemaining = "0" ;
            String pageCapacity = "0";
            //String inLaneCam = "0";
            //String outLaneCam = "0";

            request.getSession().setAttribute("facilityID", "3");
            try {
                String localServerIp = TosRefDataUtil.getValue("LOCAL_SERVER_IP");
                logger.info(localServerIp);
                request.getSession().setAttribute("localServerIp", localServerIp);
            } catch (Exception e){
                logger.error("Not able to set local server IP");
            }

            if (request.getParameter("lane1") != null) {
                laneId = "1";
            }else if(request.getParameter("lane2") != null){
                laneId = "2";
            }else if(request.getParameter("lane3") != null){
                laneId = "3";
            }else if(request.getParameter("lane4") != null){
                laneId = "4";
            }

            logger.info("Servlet started:"+ laneId);
            LaneLightManagerDao laneLightManagerDao = new LaneLightManagerDao();

            // result =
            // laneLightManagerDao.getTosLaneStatusDetails(laneId);

            List<TosLaneStatus> result = new ArrayList();

            if (laneId != null && !laneId.equals("")) {
                result = laneLightManagerDao.getTosLaneStatusDetails(laneId);
                logger.info("Set1");
            }


            Iterator itr = result.iterator();

            while (itr.hasNext()) {

                TosLaneStatus tosLaneStatus = (TosLaneStatus) itr.next();
                logger.info("Set2");
                if (tosLaneStatus != null) {
                    pageRemaining = Integer.toString(tosLaneStatus.getPage_Remaining());
                    pageCapacity = Integer.toString(tosLaneStatus.getPage_Capacity());

                    //inLaneCam = tosLaneStatus.getInLane();
                    //outLaneCam = tosLaneStatus.getOutLane();

                    logger.info("pagereamain after submit"+ pageRemaining);
                    logger.info("pagerCapacity after submit"+ pageCapacity);
                    break;
                }
            }

            if (("1").equalsIgnoreCase(laneId))
            {
                if(laneLightManagerDao.updatePageRemaining(laneId, true, 0)){
                    logger.info("Set4");
                    request.getSession().setAttribute("pageRemaining1",
                            PAGE_REMAINING_RESET_DEFAULT);
                    request.getSession().setAttribute("pageCapacity1",
                            pageCapacity);
                }

            } else if ("2".equalsIgnoreCase(laneId))
            {
                if(laneLightManagerDao.updatePageRemaining(laneId, true, 0))
                {
                    logger.info("Set5");
                    request.getSession().setAttribute("pageRemaining2",
                            PAGE_REMAINING_RESET_DEFAULT);
                    request.getSession().setAttribute("pageCapacity2",
                            pageCapacity);
                }

            } else if ("3".equalsIgnoreCase(laneId)) {

                if(laneLightManagerDao.updatePageRemaining(laneId, true, 0))
                {
                    logger.info("Set6");
                    request.getSession().setAttribute("pageRemaining3",
                            PAGE_REMAINING_RESET_DEFAULT);
                    request.getSession().setAttribute("pageCapacity3",
                            pageCapacity);
                }

            } else if ("4".equalsIgnoreCase(laneId))
            {
                if(laneLightManagerDao.updatePageRemaining(laneId, true, 0))
                {
                    logger.info("Set7");
                    request.getSession().setAttribute("pageRemaining4",
                            PAGE_REMAINING_RESET_DEFAULT);
                    request.getSession().setAttribute("pageCapacity4",
                            pageCapacity);
                    logger.info("Setting lane4");
                }

            } else {



                TosLaneStatus tosLaneStatus = getPageCapacityAndPageRemaining("1");
                request.getSession().setAttribute("pageRemaining1", Integer.toString(tosLaneStatus.getPage_Remaining()));
                request.getSession().setAttribute("pageCapacity1", Integer.toString(tosLaneStatus.getPage_Capacity()));

                request.getSession().setAttribute("INLANE1", tosLaneStatus.getInLane());
                request.getSession().setAttribute("lightStatus1", tosLaneStatus.getLamp_Indicator());

                logger.info("Before determining queue for Lane1:");
                String queuestatus1 = determineQueue(tosLaneStatus.getWorkstation(),
                        tosLaneStatus.getLamp_Indicator(), Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp());
                request.getSession().setAttribute("queueStatus1", queuestatus1);
                request.getSession().setAttribute("time1", getWaitingTime(queuestatus1, tosLaneStatus.getLane_Timestamp()));

                tosLaneStatus = getPageCapacityAndPageRemaining("2");
                request.getSession().setAttribute("pageRemaining2", Integer.toString(tosLaneStatus.getPage_Remaining()));
                request.getSession().setAttribute("pageCapacity2", Integer.toString(tosLaneStatus.getPage_Capacity()));
                request.getSession().setAttribute("INLANE2", tosLaneStatus.getInLane());
                request.getSession().setAttribute("lightStatus2", tosLaneStatus.getLamp_Indicator());
                String queuestatus2 = determineQueue(tosLaneStatus.getWorkstation(),
                        tosLaneStatus.getLamp_Indicator(), Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp());
                request.getSession().setAttribute("queueStatus2",queuestatus2);
                request.getSession().setAttribute("time2", getWaitingTime(queuestatus2, tosLaneStatus.getLane_Timestamp()));



                tosLaneStatus = getPageCapacityAndPageRemaining("3");
                request.getSession().setAttribute("pageRemaining3", Integer.toString(tosLaneStatus.getPage_Remaining()));
                request.getSession().setAttribute("pageCapacity3", Integer.toString(tosLaneStatus.getPage_Capacity()));
                request.getSession().setAttribute("INLANE3", tosLaneStatus.getInLane());
                request.getSession().setAttribute("lightStatus3", tosLaneStatus.getLamp_Indicator());
                String queuestatus3 = determineQueue(tosLaneStatus.getWorkstation(),
                        tosLaneStatus.getLamp_Indicator(), Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp());
                //String queuestatus3 = determineQueue(null,"Y","1");
                request.getSession().setAttribute("queueStatus3", queuestatus3);
                request.getSession().setAttribute("time3", getWaitingTime(queuestatus3, tosLaneStatus.getLane_Timestamp()));



                tosLaneStatus = getPageCapacityAndPageRemaining("4");
                request.getSession().setAttribute("pageRemaining4", Integer.toString(tosLaneStatus.getPage_Remaining()));
                request.getSession().setAttribute("pageCapacity4", Integer.toString(tosLaneStatus.getPage_Capacity()));
                request.getSession().setAttribute("INLANE4", tosLaneStatus.getInLane());
                request.getSession().setAttribute("lightStatus4", tosLaneStatus.getLamp_Indicator());
                request.getSession().setAttribute("OUTLANE1", tosLaneStatus.getOutLane());

                String queuestatus4 = determineQueue(tosLaneStatus.getWorkstation(),
                        tosLaneStatus.getLamp_Indicator(), Integer.toString(tosLaneStatus.getSignaled()), tosLaneStatus.getLane_Timestamp());
                //String queuestatus4 = determineQueue(null,"Y","0");

                request.getSession().setAttribute("queueStatus4", queuestatus4);
                request.getSession().setAttribute("time4", getWaitingTime(queuestatus4, tosLaneStatus.getLane_Timestamp()));

                logger.info("Set8");

            }


            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            if (dispatcher != null) {
                logger.info("TESTTEST:::::::");
                dispatcher.forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TosLaneStatus getPageCapacityAndPageRemaining(String laneId)
    {

        List<TosLaneStatus> result1 = new ArrayList();
        LaneLightManagerDao laneLightManagerDao = new LaneLightManagerDao();

        result1 = laneLightManagerDao.getTosLaneStatusDetails(laneId);
        logger.info("getPageCapacityAndPageRemaining:: Set result1");

        Iterator itr1 = result1.iterator();

        while (itr1.hasNext()) {

            TosLaneStatus tosLaneStatus = (TosLaneStatus) itr1.next();
            logger.info("getPageCapacityAndPageRemaining:: Set in else");
            if (tosLaneStatus != null) {

                return tosLaneStatus;
				/*logger.info("table not null");
				pageRemainingX1 = Integer.toString(tosLaneStatus.getPage_Remaining());
				pageCapacityX1 = Integer.toString(tosLaneStatus.getPage_Capacity());
				logger.info("pagereamaining while loading"+ pageRemainingX1);
				logger.info("pagerCapacity while loading"+ pageCapacityX1);
				break; */
            }
        }
        return new TosLaneStatus();
    }

    public String determineQueue( String workstation, String lampInd, String signalled, String laneTimestamp)
    {
        logger.info("determineQueue method: workstation value " + workstation);

        if ((workstation == null || ("".equals(workstation.trim()))) && "0".equals(signalled) && validLightSignal(lampInd))
        {
            logger.info("Queue: Ready");
            return "READY";
        }
        else if (workstation != null && !"".equals(workstation.trim()) && !"Waiting".equalsIgnoreCase(workstation)
                && "1".equals(signalled) && validLightSignal(lampInd))
        {
            logger.info("Queue: Working");
            return "WORKING " + "( " + workstation + " )";
        }
        else if ("1".equals(signalled) && validLightSignal(lampInd))
        {
            logger.info("Queue: Waiting");

            return "WAITING";
        }

        logger.info("Queue: return empty value");
        return "NOT IN SERVICE";

    }

    public String getWaitingTime(String queuestatus, String laneTimestamp)
    {
        Date laneArrivalDate = null;
        Date currentDate = null;

        logger.info("getWaitingTime Method: " + "Lane Time stamp value: " + laneTimestamp);

        if("READY".equalsIgnoreCase(queuestatus) || "NOT IN SERVICE".equalsIgnoreCase(queuestatus))
            return "00:00";

        try{

            //Removing milli seconds from input string
            String laneTimestampStr = laneTimestamp.substring(0, laneTimestamp.indexOf('.'));
            TimeZone UTC = TimeZone.getTimeZone("UTC");

            // Create a UTC formatter
            final SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            formatter.setTimeZone(UTC);

            laneArrivalDate = formatter.parse(laneTimestampStr);

            logger.info("Lane Time stamp value after parsing it : " + laneArrivalDate);


        }catch(Exception e){
            logger.info("determineQueue:: Exception while parsing laneTimestamp", e);
            laneArrivalDate = new Date();
        }

        currentDate= new java.util.Date();

        logger.info("Current date value : " + currentDate);

        long diff = currentDate.getTime() - laneArrivalDate.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);

        String formattedSec=String.valueOf(diffSeconds);
        String formattedMin=String.valueOf(diffMinutes);
        String formattedHours=String.valueOf(diffHours);

        if(formattedSec.length()==1){
            formattedSec = "0"+formattedSec;
        }
        if(formattedMin.length()==1){
            formattedMin ="0"+formattedMin;
        }
        if(formattedHours.length()==1){
            formattedHours = "0"+formattedHours;
        }

        logger.info("Time in seconds after format : " + formattedSec + " seconds.");
        logger.info("Time in minutes after format : " + formattedMin + " minutes.");
        logger.info("Time in hours after format : " + formattedHours + " hours.");

        //return just the minutes and seconds
        return formattedMin + ":" + formattedSec;
   }

    public boolean validLightSignal(String lampIndicator)
    {
        if("G".equalsIgnoreCase(lampIndicator))
           return true;

        return false;
    }
}


