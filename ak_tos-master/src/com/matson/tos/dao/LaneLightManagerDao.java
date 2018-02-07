package com.matson.tos.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.matson.cas.refdata.mapping.TosLaneStatus;


public class LaneLightManagerDao extends BaseDAO{

    private static Logger logger = Logger.getLogger(LaneLightManagerDao.class);
    public static SessionFactory sessionFact;
    public static String  CONIG_FILE = "alkrefdata.cfg.xml";
    public static final String FACILITY_ID= "3";
    public static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    //+ "/alkrefdata.cfg.xml";
    // /tosrefdata.cfg.xml;

    /**
     * @param args
     */
    public synchronized static void createHibernateSessionFactory()
    {
        try
        {
            if (sessionFact == null) {
                try {
                    logger.debug("session factory creation begins");
                    Configuration cfg = new Configuration();
                    cfg.configure(CONIG_FILE);
                    sessionFact = cfg.buildSessionFactory();
                    logger.debug("Sessionfactory is:"+ sessionFact);
                } catch (Exception e) {
                    logger.error("%%%% Error Creating SessionFactory %%%%", e);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error in creating hibernate session: ", e);
        }
    }

    public static void closeHibernateSession(Session session)
    {
        session.close();
    }
    public static Transaction beginTransaction(Session session)
    {
        return session.beginTransaction();
    }
    public static void commitTransaction(Transaction transaction)
    {
        transaction.commit();
    }

    public static String updateTosLaneStatus(String lampInd, String lane_id)
    {
        if (lampInd == null ||
                lane_id == null ||
                "".equals(lampInd.trim()) ||
                "".equals(lane_id.trim()))
        {
            return "FAIL";
        }
        String result = "";
        logger.info("LaneLightManagerDao.updateTosLaneStatus begin");

        Session session= null;// Session session = '';
        try {
            createHibernateSessionFactory();
            logger.debug("SESSION FACTORY INTI");

            session = sessionFact.openSession();
            logger.debug("OPENED SESSION");

            Transaction transaction = beginTransaction(session);
            logger.debug("Begin Transaction");

            java.util.Date date= new java.util.Date();
            formatter.setTimeZone(UTC);
            String currentTimeStamp = formatter.format(new Timestamp(date.getTime()));

            String qryStr = "UPDATE TDP_LANE_STATUS set lamp_Indicator = "
                    + "\'" + lampInd + "\'"
                    + ", LAST_UPDATED_TIMESTAMP = " +  "\'" + currentTimeStamp +  "\'"
                    + " WHERE facility_Id= "+  FACILITY_ID + " AND lane_Id = " + lane_id+";";

            SQLQuery qry = session.createSQLQuery(qryStr);
            qry.executeUpdate();
            //Query qry = session.createSQLQuery(qryStr);
            int count = qry.executeUpdate();
            commitTransaction(transaction);
            logger.info("qryStr  "+qryStr);
            logger.debug("Commit Transaction");
            logger.info("Number of rows affected   "+count);

            if (qry != null){
                logger.info("LaneLightManagerDao.updateTosLaneStatus ---- executed the query");
                result =  "SUCCESS";
            }else{
                result = "FAIL";
            }
            //closeHibernateSession(session);

            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("Error: Unable to update Lamp Indicator");
        }finally {
            closeHibernateSession(session);
        }
        return "FAIL";
    }

    public static ArrayList<TosLaneStatus> getTosLaneStatusDetails(String laneId)
    {
        logger.info("LaneLightManagerDao.getTosLaneStatusDetails begin");

        //String result = "";
        Session session= null;
        ArrayList<TosLaneStatus> list = null;

        if(laneId != null && laneId.equals("") == false)
        {

            try {
                createHibernateSessionFactory();
                logger.debug("SESSION FACTORY INTI");

                session = sessionFact.openSession();
                logger.debug("OPENED SESSION");

                Transaction transaction = beginTransaction(session);
                logger.debug("Begin Transaction");


                String qryStr = "FROM TosLaneStatus WHERE lane_Id='" + laneId + "'";
                logger.debug(qryStr);

                ArrayList<TosLaneStatus> tempList = (ArrayList<TosLaneStatus>) session.createQuery(qryStr).list();
                //logger.info(tempList);
                //logger.info(tempList.get(0));

                for(TosLaneStatus a : tempList ){
                    logger.debug("Inlane::"+a.getInLane());
                    logger.debug("Outlane::"+a.getOutLane());
                }
                //qryStr.executeUpdate();
                //int count = qry.executeUpdate();
                commitTransaction(transaction);
                logger.debug("Commit Transaction");
                //logger.info("Number of rows affected   "+count);

                if(tempList!=null){
                    logger.debug("Result size:" + tempList.size());

                } else {
                    logger.debug("Result :" + tempList);

                }
                return tempList;
            }

            catch(Exception e)
            {
                e.printStackTrace();

                logger.error("Error: Unable to find records for "+ laneId);
            }finally {
                closeHibernateSession(session);
            }
            //return list;

        } else {
            logger.error("Error: getTosLaneStatusDetails(): " + laneId);
        }
        return list;

    }

    public static ArrayList<TosLaneStatus> getSignalId()
    {
        logger.debug("LaneLightManagerDao.getSignalId begin");

        Session session= null;
        ArrayList<TosLaneStatus> signalList = null;

        try {
            createHibernateSessionFactory();
            session = sessionFact.openSession();
            Transaction transaction = beginTransaction(session);
            String qryStr = "FROM TosLaneStatus WHERE signaled='1'";

            logger.debug("Signal list query: "+ qryStr);

            signalList = (ArrayList<TosLaneStatus>) session.createQuery(qryStr).list();

            return signalList;
        }

        catch(Exception e)
        {
            e.printStackTrace();

            logger.error("Error: Unable to find records for Signal id 1" );
        }finally {
            closeHibernateSession(session);
        }

        return signalList;

    }

    public static boolean updatePageRemaining(String laneId, boolean resetFlg, int printTicketCount)
    {
        logger.info("LaneLightManagerDao.updatePageRemaining begin");

        //String result = "";
        Session session= null;
        ArrayList<TosLaneStatus> list = null;

        if(laneId != null && laneId.equals("") == false)
        {
            try {

                if(resetFlg){
                    printTicketCount = 813;
                } else {

                list = getTosLaneStatusDetails(laneId);

                //have a code to validate the line status
                Iterator itr = list.iterator();

                while (itr.hasNext()) {

                    TosLaneStatus tosLaneStatus = (TosLaneStatus) itr.next();
                    if (tosLaneStatus != null) {
                        logger.info("tosLaneStatus details: " + tosLaneStatus.toString());
                        printTicketCount = tosLaneStatus.getPage_Remaining() - printTicketCount;
                    }
                }
            }

               createHibernateSessionFactory();
                logger.debug("SESSION FACTORY INTI");

                session = sessionFact.openSession();
                logger.debug("OPENED SESSION");

                Transaction transaction = beginTransaction(session);
                logger.debug("Begin Transaction");

                java.util.Date date= new java.util.Date();
                formatter.setTimeZone(UTC);
                String currentTimeStamp = formatter.format(new Timestamp(date.getTime()));

                String qryStr = "UPDATE TDP_LANE_STATUS set page_Remaining = "
                        + "\'" + printTicketCount + "\'" +
                        ", LAST_UPDATED_TIMESTAMP = " +  "\'" + currentTimeStamp +  "\'" +
                        " WHERE lane_Id = " + laneId+";";

                SQLQuery qry = session.createSQLQuery(qryStr);
                qry.executeUpdate();

                commitTransaction(transaction);
                logger.debug("Commit Transaction");
                return true;

            }

            catch(Exception e)
            {
                e.printStackTrace();

                logger.error("Error: Unable to find records for "+ laneId);
                return false;
            }finally {
                closeHibernateSession(session);
            }
            //return list;

        } else {
            logger.error("Error: updatePageRemaining(): " + laneId);
        }
        return false;

    }

    public static String updateClientWorkstation(String lane_id, String clientId, int signalId)
    {
        logger.info("LaneLightManagerDao.updateClientWorkstation begin");

        if (clientId == null ||
                lane_id == null)
        {
            return "FAIL";
        }
        String result = "";

        Session session= null;
        String qryStr = null;
        try {
            createHibernateSessionFactory();
            logger.debug("SESSION FACTORY INTI");

            session = sessionFact.openSession();
            logger.debug("OPENED SESSION");

            Transaction transaction = beginTransaction(session);
            logger.debug("Begin Transaction");

            java.util.Date date= new java.util.Date();
            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            //TimeZone UTC = TimeZone.getTimeZone("UTC");
            formatter.setTimeZone(UTC);
            String currentTimeStamp = formatter.format(new Timestamp(date.getTime()));

            logger.info("updateClientWorkstation:: current time stamp " + currentTimeStamp);

            qryStr = "UPDATE TDP_LANE_STATUS " +
                    "set workstation = " + "\'" + clientId + "\'" +
                    ", SIGNALED = " + signalId +
                    ", LANE_TIMESTAMP = " +  "\'" + currentTimeStamp +  "\'" +
                    ", LAST_UPDATED_TIMESTAMP = " +  "\'" + currentTimeStamp +  "\'" +
                    " WHERE facility_Id= "+  FACILITY_ID +
                    " AND lane_Id = " + lane_id+";";

            logger.info("qryStr  "+qryStr);

            SQLQuery qry = session.createSQLQuery(qryStr);

            qry.executeUpdate();
            int count = qry.executeUpdate();
            commitTransaction(transaction);
            logger.debug("Commit Transaction");
            logger.info("Number of rows affected   "+count);

            if (qry != null){
                logger.info("LaneLightManagerDao.updateClientWorkstation ---- executed the query");
                result =  "SUCCESS";
            }else{
                result = "FAIL";
            }
            return result;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("Error: unable to update workstation name");
        }finally {
            closeHibernateSession(session);
        }
        return "FAIL";
    }

	/*
	public static String updatePageRemaining(String laneId)
	{

		String result = "";
		logger.info("LaneLightManagerDao.updatePageRemaining begin");

		Session session= null;// Session session = '';
			try {
				createHibernateSessionFactory();
				logger.info("SESSION FACTORY INTI");

				session = sessionFact.openSession();
				logger.info("OPENED SESSION");

				Transaction transaction = beginTransaction(session);
				logger.info("Begin Transaction");

				String qryStr = "UPDATE TDP_LANE_STATUS set page_Remaining = " + "\'" + 813 + "\'" + " WHERE lane_Id = " + laneId+";";

				SQLQuery qry = session.createSQLQuery(qryStr);
				qry.executeUpdate();
				//Query qry = session.createSQLQuery(qryStr);
				int count = qry.executeUpdate();
				commitTransaction(transaction);
				logger.info("Commit Transaction");
				logger.info("qryStr  "+qryStr);
				logger.info("Number of rows affected   "+count);

				if (qry != null){
					logger.info("LaneLightManagerDao.updatePageRemaining ---- executed the query");
					result =  "SUCCESS";
				}else{
					result = "FAIL";
				}
				//closeHibernateSession(session);

				return result;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("Error: Unable to update Page Remaining");
			}finally {
				closeHibernateSession(session);
	         }
		return "FAIL";
	}

	*/

	/*
	public static String updatePageRemaining(String facilityId, String laneId)
	{
		ArrayList<TosLaneStatus> dataList = getTosLaneStatusList(facilityId, laneId);
		logger.info("LaneLightManagerDao.updatePageRemaining begin");
		Session session = null;
		int i=0;
		try {
			createHibernateSessionFactory();

			logger.debug(" createHibernateSessionFactory --- done" );
			session = sessionFact.openSession();

			Transaction transaction = beginTransaction(session);
			for(i=0; i<dataList.size(); i++)
			{

				logger.debug("Lamp indicator " +dataList.get(i).getLamp_Indicator());
				TosLaneStatus data = dataList.get(i);
				data.setPage_Remaining(815);
				session.merge(data);
				if(i%20 == 0)
				{
					session.flush();
					session.clear();
				}
				return "SUCCESS";
			}
			commitTransaction(transaction);
		}catch(Exception ex){
			ex.printStackTrace();
			TosLaneStatus data = dataList.get(i);
		}finally {
			closeHibernateSession(session);
        }
		logger.info("LaneLightManagerDao.updatePageRemaining end");
		return "FAIL";
	}*/
}