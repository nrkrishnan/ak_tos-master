package com.matson.tos.processor;

import com.matson.tos.dao.VgxDAO;
import com.matson.tos.exception.TosException;
import com.matson.tos.util.EmailSender;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by kramachandran on 1/23/2017.
 */
public class AvgxMessageCleanupProcessor extends AbstractFileProcessor {
    private static Logger logger = Logger.getLogger(AvgxMessageCleanupProcessor.class);
    private static String emailAddr = TosRefDataUtil.getValue("VGX_SUPPORT_EMAIL");
    public AvgxMessageCleanupProcessor() {
    }

    @Override
    protected void processLine(String aLine, int lineNum) throws TosException {
        // TODO Auto-generated method stub

    }

    public void processFiles() {
        aVgxDataCleanUp();
    }

    public void aVgxDataCleanUp() {
        logger.info("VGX Cleanup triggered to Purge old Data");
        logger.trace("VGX Cleanup triggered to Purge old Data at ");
        Integer historyDays = Integer.parseInt(TosRefDataUtil.getValue("VGX_HISTORY_DAYS"));
        try {
            VgxDAO.purgeVgxRecords(historyDays);
        } catch (Exception ex) {
            logger.error("", ex);
            try {
                EmailSender.sendMail(emailAddr, emailAddr, "Purge Job VGX - failed", ex.toString() + "\n" + ex.getMessage());
            } catch (Exception mailBlockException) {
                logger.error("Error Sending Mail", mailBlockException);
            }
        }

    }
}
