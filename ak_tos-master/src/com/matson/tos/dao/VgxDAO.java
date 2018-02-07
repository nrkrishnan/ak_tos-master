package com.matson.tos.dao;

import com.matson.cas.refdata.mapping.ArTosVgxMessageMt;
import com.matson.cas.refdata.mapping.TosVgxMessageMt;
import com.matson.tos.messageHandler.AvgxMessageHandler;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * Created by psethuraman on 5/23/2016.
 */
public class VgxDAO extends BaseDAO {
    private static Logger logger = Logger.getLogger(VgxDAO.class);
    public static SessionFactory sessionFact;
    public static String  CONIG_FILE = "/tosrefdata.cfg.xml";

    public synchronized static void createHibernateSessionFactory()
    {
        try
        {
            if (sessionFact == null) {
                try {
                    logger.info("session factory creation begins");
                    Configuration cfg = new Configuration();
                    cfg.configure(CONIG_FILE);
                    sessionFact = cfg.buildSessionFactory();
                    //sessionFactoryMap.put(CONIG_FILE,sessionFact);
                    logger.info("session factory creation ends");
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

    public static void insertVgxData(TosVgxMessageMt vgxData)
    {
        logger.info("VgxDao.insertVgxData begin");
        Session session = null;
        int i=0;
        try {
            createHibernateSessionFactory();
            session = sessionFact.openSession();
            logger.info("Inserting VGX data into table : "+vgxData.getContainerNumber());
            Transaction transaction = beginTransaction(session);

            session.saveOrUpdate(vgxData);
            session.flush();
            session.clear();
            commitTransaction(transaction);

        }catch(Exception ex) {
            ex.printStackTrace();

            logger.error("Unable to persist: "+vgxData.getContainerNumber()
                    +"<br />"+ex.toString());
        }finally {
            closeHibernateSession(session);
        }
        logger.info("VgxDao.insertVgxData end");
    }

    public static TosVgxMessageMt getTosVgxMessageMt(String containerNumber, String bookingNumber) {
        Session session = null;
        int i=0;
        TosVgxMessageMt dbVgxdata = null;
        try {
            createHibernateSessionFactory();
            session = sessionFact.openSession();
            logger.info("Read VGX data from table : "+containerNumber);
            Transaction transaction = beginTransaction(session);
            containerNumber = containerNumber != null ? containerNumber.trim() : "";
            bookingNumber = bookingNumber != null ? bookingNumber.trim() : "";
            /*Criteria vgxCriteria = session.createCriteria(TosVgxMessageMt.class)
                    .add( Restrictions.like("containerNumber", containerNumber+"%") )
                    .add( Restrictions.eq( "bookingNumber", bookingNumber))
                    .addOrder(Order.desc("updatedDate"));*/
            StringBuffer sql = new StringBuffer();
            sql.append("from TosVgxMessageMt where containerNumber = '").append(containerNumber)
                    .append("' and bookingNumber ='").append(bookingNumber).append("'");
            logger.info("Query : "+sql.toString());
            List <TosVgxMessageMt>VGXs = session.createQuery(sql.toString()).list();
//            List <TosVgxMessageMt>VGXs = vgxCriteria.list();

            if (VGXs != null && VGXs.size() > 0) {
                logger.info("Record Exists for : "+containerNumber);
                dbVgxdata = VGXs.get(0);
            } else {
                logger.info("No data found in table : "+containerNumber);
            }
            session.flush();
            session.clear();
            commitTransaction(transaction);

        }catch(Exception ex) {
            ex.printStackTrace();

            logger.error("Unable to persist: "+containerNumber
                    +"<br />"+ex.toString());
        }finally {
            closeHibernateSession(session);
        }

        return dbVgxdata;
    }

    public static List<TosVgxMessageMt> getAllVgxRecords () {
        logger.info("VgxDao.getAllVgxRecords begin");
        Session session = null;
        int i=0;
        List<TosVgxMessageMt> vgxData = new ArrayList<TosVgxMessageMt>();
        try {
            createHibernateSessionFactory();
            session = sessionFact.openSession();
            Transaction transaction = beginTransaction(session);

            vgxData = session.createQuery("from TosVgxMessageMt").list();

            session.clear();
            commitTransaction(transaction);

        }catch(Exception ex) {
            ex.printStackTrace();

            logger.error("Unable to select all the records: "+"<br />"+ex.toString());
        }finally {
            closeHibernateSession(session);
        }
        logger.info("VgxDao.getAllVgxRecords end");
        return vgxData;

    }
    public static void deleteVgxData(Map<String, String> ctrBookingMap)
    {
        logger.info("VgxDao.deleteVgxData begin");
        Session session = null;
        try {
            if (ctrBookingMap != null && ctrBookingMap.keySet().size() > 0) {
                createHibernateSessionFactory();
                session = sessionFact.openSession();
                Transaction transaction = beginTransaction(session);
                Iterator<String> containers = ctrBookingMap.keySet().iterator();

                while (containers.hasNext()) {
                    String container = containers.next();
                    logger.debug("Deleting processed VGX Container from table : "+container);
                    String booking = ctrBookingMap.get(container);
                    String qry = "DELETE TosVgxMessageMt WHERE containerNumber = '"+ container +"'  AND bookingNumber='"
                            + booking + "'";
                    int count = session.createQuery(qry).executeUpdate();
                    logger.info(count + " container deleted.>"+container +" and booking :"+booking );
                }
                commitTransaction(transaction);
            }
        }catch(Exception ex){
            ex.printStackTrace();
            logger.error(ex);
        }finally {
            closeHibernateSession(session);
        }
        logger.info("VgxDao.deleteVgxData end");
    }

    public static void purgeVgxRecords(Integer inBeforeNoOfDays) {
        logger.info("VgxDao.purgeVgxRecords begin");
        Session session = null;
        try {
            createHibernateSessionFactory();
            session = sessionFact.openSession();
            Transaction transaction = beginTransaction(session);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -inBeforeNoOfDays);
            Date maxModDate = cal.getTime();
            logger.info("Picking Records before "+maxModDate+"to be purged from DB");
            logger.debug("Deleting processed VGX Container from table : ");

            Query query = session.createQuery("from TosVgxMessageMt WHERE updatedDate <= :maxModDate");
            query.setParameter("maxModDate",maxModDate);
            List<TosVgxMessageMt> vgxData = query.list();
            logger.info("vgxData size"+vgxData.size());
            session.clear();
            for(TosVgxMessageMt messageMt:vgxData){
                logger.info("Copying TosVgxMessageMt to ArTosVgxMessageMt for MsgId "+messageMt.getMsgId());
                session.save(copyVgxRecordToArchive(messageMt));
                String qry = "DELETE TosVgxMessageMt WHERE msgId ="+messageMt.getMsgId();
                int count = session.createQuery(qry).executeUpdate();
                logger.info(count + "VGX Container Data Deleted with ID >"+messageMt.getMsgId()+"\t Container Number\t" + messageMt.getContainerNumber() + " \tand booking :\t" + messageMt.getBookingNumber());
            }
            transaction.commit();

        } catch (Exception ex) {
            logger.error("", ex);
        } finally {
            closeHibernateSession(session);
        }
        logger.info("VgxDao.purgeVgxRecords end");
    }

    private static ArTosVgxMessageMt copyVgxRecordToArchive(TosVgxMessageMt inTosVgxMessageMt){
        ArTosVgxMessageMt archiveMessage = new ArTosVgxMessageMt();
        archiveMessage.setMsgId(inTosVgxMessageMt.getMsgId());
        archiveMessage.setBookingNumber(inTosVgxMessageMt.getBookingNumber());
        archiveMessage.setCargoWt(inTosVgxMessageMt.getCargoWt());
        archiveMessage.setContainerCheckDigit(inTosVgxMessageMt.getContainerCheckDigit());
        archiveMessage.setContainerNumber(inTosVgxMessageMt.getContainerNumber());
        archiveMessage.setGrossWt(inTosVgxMessageMt.getGrossWt());
        archiveMessage.setCargoWt(inTosVgxMessageMt.getCargoWt());
        archiveMessage.setWeightUOM(inTosVgxMessageMt.getWeightUOM());
        archiveMessage.setProcessStatus(inTosVgxMessageMt.getProcessStatus());
        archiveMessage.setTareWt(inTosVgxMessageMt.getTareWt());
        archiveMessage.setVgmDate(inTosVgxMessageMt.getVgmDate());
        archiveMessage.setVgmVerifier(inTosVgxMessageMt.getVgmVerifier());
        archiveMessage.setCreateDate(inTosVgxMessageMt.getCreateDate());
        archiveMessage.setUpdatedDate(inTosVgxMessageMt.getUpdatedDate());
        archiveMessage.setRecordCreateDate(new Date());
        logger.info("Persisting ArTosVgxMessageMt"+archiveMessage);
        return archiveMessage;
    }

}
