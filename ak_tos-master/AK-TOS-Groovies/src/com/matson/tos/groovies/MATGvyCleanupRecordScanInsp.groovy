import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.persistence.HibernateApi
import com.navis.road.business.model.RoadInspection
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Logger

class MATGvyCleanupRecordScanInsp extends AbstractGateTaskInterceptor implements EGateTaskInterceptor{
    public void execute(TransactionAndVisitHolder inDao){
        executeInternal(inDao);
        if (inDao == null ){
            LOGGER.warn("Transaction visit holder is null");
            return;
        }
        if (inDao.getTran() == null) {
            LOGGER.warn("Transaction is null");
            return;
        }

        try {
            TruckTransaction thisTran = inDao.getTran();
            String container = thisTran.getTranContainer() != null ? thisTran.getTranContainer().getEqIdFull() : null;
            if (container != null) {
                List<RoadInspection> roadInspectionList = RoadInspection.findInspectionsForEq(container, 100000L);
                if (roadInspectionList != null && roadInspectionList.size() > 0) {
                    RoadInspection inspection = roadInspectionList.get(0);
                    HibernateApi.getInstance().delete(inspection);
                }
            }
            thisTran.setTranUnitFlexString05("");
        }
        catch(Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed while cleaning up Record Scan inspection data ");
        }
    }
    private static Logger LOGGER = Logger.getLogger(MATGvyCleanupRecordScanInsp.class);
}