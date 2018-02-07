import com.navis.argo.portal.context.ArgoUserContext
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.presentation.FrameworkPresentationUtils
import com.navis.road.business.api.RoadManager
import com.navis.road.business.model.GateLane
import com.navis.road.business.model.RoadManagerPea
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.reference.Console
import com.navis.road.business.workflow.TransactionAndVisitHolder

/**
 * Created by psethuraman on 8/30/2017.
 */

public class MATGvyClearTroubleTruckFromLane extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private logMsg(String inMsg) {
        log("MATGvyClearTroubleTruckFromLane : " + inMsg);
    }

    public void execute(TransactionAndVisitHolder inDao) {
        if (inDao == null) {
            log("inDao is null");
            return;
        }

        TruckVisitDetails tvd = inDao.getTv();
        if (tvd == null) {
            log("TruckVisitDetails is null");
            return;
        }

        GateLane gateLane = tvd.getTvdtlsExitLane();
        if (gateLane != null) {
            Long laneGkey = gateLane.getLaneGkey();
            log("lane to clear : "+gateLane.getLaneId());
            if (laneGkey != null) {
                RoadManager rm = (RoadManager) Roastery.getBean(RoadManagerPea.BEAN_ID);
                rm.clearTruckFromLane(laneGkey);
            }
        }
    }
}