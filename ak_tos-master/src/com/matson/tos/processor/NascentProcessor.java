package com.matson.tos.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosLaneStatus;
import com.matson.cas.service.ftp.FtpBizException;
import com.matson.cas.service.ftp.FtpProxyGetterBiz;
import com.matson.cas.service.ftp.FtpProxyListBiz;
import com.matson.tos.dao.LaneLightManagerDao;
import com.matson.tos.exception.TosException;
import com.matson.tos.nascent.webservice.RecordScanClient;
import com.matson.tos.util.TosRefDataUtil;
import com.matson.tos.vo.RDSFields;

public class NascentProcessor extends AbstractFileProcessor {

    private static Logger logger = Logger.getLogger(NascentProcessor.class);

    @Override
    protected void processLine(String aLine, int lineNum) throws TosException {
        // TODO Auto-generated method stub

    }

    public void processFiles() {
        try {
            logger.debug("inside nascent processor ");

            String workStationClient = null;
            LaneLightManagerDao laneLightManagerDao = new LaneLightManagerDao();
            List<TosLaneStatus> signalList = new ArrayList();
            RecordScanClient recordScan = new RecordScanClient();

            signalList = laneLightManagerDao.getSignalId();

            if(signalList!=null){
                logger.debug("Result size:" + signalList.size());
            }

            int laneId = -1;
            int signalId = -1;
            String laneTimeStamp = null;
            for(TosLaneStatus tls : signalList ){

                if(tls!=null){

                    laneId = tls.getLane_Id();
                    signalId = tls.getSignaled();
                    laneTimeStamp = tls.getLane_Timestamp();
                    workStationClient = tls.getWorkstation();

                    logger.debug("Lane ID ::  "+laneId +
                            "   Signal ID ::"+signalId +
                            "   workStationClient ID ::"+workStationClient +
                            "   laneTimeStamp :: "+laneTimeStamp);

                    if(signalId==1 && (workStationClient == null || workStationClient.isEmpty())) {
                        recordScan.argoServicePortGenericInvoke(String.valueOf(laneId));
                        laneLightManagerDao.updateClientWorkstation(String.valueOf(laneId), "Waiting", signalId);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Exception: ", e);
            e.printStackTrace();
        }
    }

}