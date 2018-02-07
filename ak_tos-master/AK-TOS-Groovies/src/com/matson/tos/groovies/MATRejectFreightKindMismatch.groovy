import com.navis.argo.business.atoms.FreightKindEnum;
import com.navis.external.road.AbstractGateTaskInterceptor;
import com.navis.external.road.EGateTaskInterceptor;
import com.navis.framework.util.internationalization.PropertyKeyFactory;
import com.navis.framework.util.message.MessageLevel;
import com.navis.road.business.atoms.TranSubTypeEnum;
import com.navis.road.business.util.RoadBizUtil;
import com.navis.road.business.workflow.TransactionAndVisitHolder;

class MATRejectFreightKindMismatch extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    public void execute(TransactionAndVisitHolder inDao) {
        if (inDao.getTran() != null) {
            if (TranSubTypeEnum.RE.equals(inDao.getTran().getTranSubType()) &&
                    FreightKindEnum.MTY.equals(inDao.getTran().getTranCtrFreightKind())) {

                  RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf("gate.empty_not_allowed_as_export") , null);
                  return;
            } else if (TranSubTypeEnum.RM.equals(inDao.getTran().getTranSubType()) &&
                    FreightKindEnum.FCL.equals(inDao.getTran().getTranCtrFreightKind())) {
                RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf("gate.fcl_not_allowed_as_empty") , null);
                return;
            }
        }
        executeInternal(inDao);
    }
}