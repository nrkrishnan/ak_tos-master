import com.navis.argo.business.reference.Container
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.AllOtherFrameworkPropertyKeys
import com.navis.framework.util.BizFailure
import com.navis.framework.util.BizViolation
import com.navis.framework.util.internationalization.PropertyKey
import com.navis.framework.util.internationalization.PropertyKeyFactory
import com.navis.framework.util.message.MessageLevel
import com.navis.road.RoadPropertyKeys
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Logger

/**
 * Created by brajamaickam on 9/10/2017.
 */

public class MATGvyWeightValidation extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private static Logger LOGGER = Logger.getLogger(MATGvyWeightValidation.class);
    public static PropertyKey NETWEIGHT_INVALID = PropertyKeyFactory.valueOf("gate.net_weight_invalid")

    public void execute(TransactionAndVisitHolder dao) {

        TruckTransaction tran = dao.tran;
        if (tran != null) {
            Container container = tran.getTranContainer();
            Double netWeight = tran.getTranCtrNetWeight();

                  if (container != null) {
                LOGGER.info("Enter weight for " + container.getEqIdFull() + " is : " + netWeight);
                LOGGER.info("Tran Type : "+tran.getTranSubType());
                Double tareWeight = container.getEqTareWeightKg();
                LOGGER.info("Container Tare weight : "+tareWeight);
                Object[] params = new Object[2];
                params[0] = netWeight;
                params[1] = tareWeight;              

                LOGGER.info("Container netWeight : "+netWeight +" tran.getTranSubType() "+tran.getTranSubType());
                if (TranSubTypeEnum.RE.equals(tran.getTranSubType())) {
                    if (netWeight == null || netWeight <=0 ) {

                       RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, NETWEIGHT_INVALID , null);
                       
                    }

                } else if (TranSubTypeEnum.RM.equals(tran.getTranSubType())) {
              
                    if (netWeight != null && netWeight>0) {
                        RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, NETWEIGHT_INVALID , null);
                    }
                }

            }
        }
    }
}
