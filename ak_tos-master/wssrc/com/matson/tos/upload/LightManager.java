package com.matson.tos.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.matson.tos.nascent.webservice.LaneIndicatorWebServiceLocator;
import com.matson.tos.nascent.webservice.LaneIndicatorWebServiceSoap_BindingStub;
import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosLaneStatus;
import com.matson.tos.dao.LaneLightManagerDao;

public class LightManager extends HttpServlet
{
    private static final long serialVersionUID = 1234;
    private static Logger logger = Logger.getLogger(LightManager.class);
    private static String RED = "RED";
    private static String GREEN = "GREEN";
    private static String OFF = "O";

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        String page="LightManager.jsp";
        logger.info("LightManager starting of the service method");
        String lampInd1 = null;
        String laneId1 = null;
        String lampInd2 = null;
        String laneId2 = null;
        String lampInd3 = null;
        String laneId3 = null;
        String lampInd4 = null;
        String laneId4 = null;
        TosLaneStatus tosLaneStatus = null;
        boolean successfulUpdate = false;

        try
        {
            response.setContentType("text/html");

            String selection1 = (String)request.getParameter("lane1");
            tosLaneStatus = getLampIndicator("1");
            String dbLampIndLane1 = tosLaneStatus.getLamp_Indicator();
            if(selection1 != null)
            {
                logger.info("LightManager - processing selection lane one : " + selection1);

                lampInd1 = String.valueOf(selection1.charAt(0));
                laneId1 = String.valueOf(selection1.charAt(1));

                if (!lampInd1.equalsIgnoreCase(dbLampIndLane1)) {
                    successfulUpdate = callNascentLaneIndicatorSvc(lampInd1, laneId1);
                    if (successfulUpdate)
                        request.getSession().setAttribute("laneLight1", lampInd1);
                    else
                        request.getSession().setAttribute("laneLight1", dbLampIndLane1);
                }
            } else {
                request.getSession().setAttribute("laneLight1", dbLampIndLane1);
            }

            String selection2 = (String)request.getParameter("lane2");
            tosLaneStatus = getLampIndicator("2");
            String dbLampIndLane2 = tosLaneStatus.getLamp_Indicator();
            if(selection2 != null)
            {
                logger.info("LightManager - processing selection lane two : " + selection2);

                lampInd2 = String.valueOf(selection2.charAt(0));
                laneId2 = String.valueOf(selection2.charAt(1));

                if (!lampInd2.equalsIgnoreCase(dbLampIndLane2)) {
                    successfulUpdate = callNascentLaneIndicatorSvc(lampInd2, laneId2);
                    if (successfulUpdate)
                        request.getSession().setAttribute("laneLight2", lampInd2);
                    else
                        request.getSession().setAttribute("laneLight2", dbLampIndLane2);
                }
            } else {
                request.getSession().setAttribute("laneLight2", dbLampIndLane2);
            }

            String selection3 = (String)request.getParameter("lane3");
            tosLaneStatus = getLampIndicator("3");
            String dbLampIndLane3 = tosLaneStatus.getLamp_Indicator();
            if(selection3 != null)
            {
                logger.info("LightManager - processing selection lane third : " + selection3);

                lampInd3 = String.valueOf(selection3.charAt(0));
                laneId3 = String.valueOf(selection3.charAt(1));

                if (!lampInd3.equalsIgnoreCase(dbLampIndLane3)) {
                    successfulUpdate = callNascentLaneIndicatorSvc(lampInd3, laneId3);
                    if (successfulUpdate)
                        request.getSession().setAttribute("laneLight3", lampInd3);
                    else
                        request.getSession().setAttribute("laneLight3", dbLampIndLane3);
                }
            } else {
                request.getSession().setAttribute("laneLight3", dbLampIndLane3);
            }

            String selection4 = (String)request.getParameter("lane4");
            tosLaneStatus = getLampIndicator("4");
            String dbLampIndLane4 = tosLaneStatus.getLamp_Indicator();
            if(selection4 != null)
            {
                logger.info("LightManager - processing selection lane four : " + selection4);

                lampInd4 = String.valueOf(selection4.charAt(0));
                laneId4 = String.valueOf(selection4.charAt(1));

                if (!lampInd4.equalsIgnoreCase(dbLampIndLane4)) {
                    successfulUpdate = callNascentLaneIndicatorSvc(lampInd4, laneId4);
                    if (successfulUpdate)
                        request.getSession().setAttribute("laneLight4", lampInd4);
                    else
                        request.getSession().setAttribute("laneLight4", dbLampIndLane4);
                }
            } else {
                request.getSession().setAttribute("laneLight4", dbLampIndLane4);
            }

            logger.info("LightManager input values : "
                    + " Lane 1 :"  + laneId1 + lampInd1
                    + " Lane 2 :"  + laneId2 + lampInd2
                    + " Lane 3 :"  + laneId3 + lampInd3
                    + " Lane 4 :"  + laneId4 + lampInd4
            );

           logger.info("default variables");

            RequestDispatcher dispatcher = request.getRequestDispatcher(page);
            if(dispatcher!=null)
            {
                dispatcher.forward(request, response);
            }
        }
        catch(Exception e)
        {
            logger.info("LightManager service method: entered into exception", e);
            e.printStackTrace();
        }
    }

    private boolean callNascentLaneIndicatorSvc(String lampInd, String lane_id) throws Exception {

        boolean lightIndicatorRedOn = true;
        boolean lightIndicatorGreenOn = true;
        boolean lightIndicatorOff = true;

        String lampColor = GREEN;
        if("R".equalsIgnoreCase(lampInd)) {
            lightIndicatorRedOn = true;
            lightIndicatorGreenOn = false;
            lampColor = RED;
        } else if ("O".equalsIgnoreCase(lampInd)) {
            lightIndicatorOff = true;
            lightIndicatorGreenOn = false;
            lightIndicatorRedOn = false;
            lampColor = OFF;
        } else if ("G".equalsIgnoreCase(lampInd)) {
            lightIndicatorGreenOn = true;
            lightIndicatorRedOn = false;
            lampColor = GREEN;
        }

        logger.info("callNascentLaneIndicatorSvc Start of the method:");
        LaneLightManagerDao laneLightManagerDao = new LaneLightManagerDao();

        LaneIndicatorWebServiceLocator laneIndicatorCtrlLoc = new LaneIndicatorWebServiceLocator();
        LaneIndicatorWebServiceSoap_BindingStub proxy = null;

        try {
            proxy = (LaneIndicatorWebServiceSoap_BindingStub)laneIndicatorCtrlLoc.getLaneIndicatorWebServiceSoap();

            // Time out after a minute
            proxy.setTimeout(60000);
            logger.info("Before calling Nascent Lane Indicator ON Service: " + "laneId : " + lane_id
                    + " lampColor: " + lampColor);
            if ("G".equalsIgnoreCase(lampInd) || "R".equalsIgnoreCase(lampInd)) {
                logger.info("Called webservice first for Red : "+lightIndicatorRedOn);
                lightIndicatorRedOn = proxy.controlLaneIndicator(lane_id, RED, lightIndicatorRedOn);
                logger.info("Called webservice Second for Green : "+lightIndicatorGreenOn);
                lightIndicatorGreenOn = proxy.controlLaneIndicator(lane_id, GREEN, lightIndicatorGreenOn);
            } else {
                logger.info("Called webservice for Off : "+lightIndicatorOff);
                lightIndicatorOff = proxy.controlLaneIndicator(lane_id, OFF, lightIndicatorOff);
                lightIndicatorOff = proxy.controlLaneIndicator(lane_id, GREEN, lightIndicatorOff);
                lightIndicatorOff = proxy.controlLaneIndicator(lane_id, RED, lightIndicatorOff);
            }
        }
        catch (javax.xml.rpc.ServiceException jre) {
            logger.info("Exception occurred while calling Nascent Lane indicator ON webservice");
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new Exception("Remote procedure ServiceException caught: " + jre);
        }

        String result = laneLightManagerDao.updateTosLaneStatus(lampInd, lane_id);

        if (!"SUCCESS".equalsIgnoreCase(result)){
            logger.info("Database is NOT Updated");
            return false;
        }

        logger.info("Database Update Successfully");
        return true;
    }

    public TosLaneStatus getLampIndicator(String laneId)
    {
        List<TosLaneStatus> result1 = new ArrayList();
        LaneLightManagerDao laneLightManagerDao = new LaneLightManagerDao();

        result1 = laneLightManagerDao.getTosLaneStatusDetails(laneId);

        Iterator itr1 = result1.iterator();

        while (itr1.hasNext()) {

            TosLaneStatus tosLaneStatus = (TosLaneStatus) itr1.next();
            //logger.info("Set in else");
            if (tosLaneStatus != null) {
                return tosLaneStatus;
            }
        }
        return new TosLaneStatus();
    }
}