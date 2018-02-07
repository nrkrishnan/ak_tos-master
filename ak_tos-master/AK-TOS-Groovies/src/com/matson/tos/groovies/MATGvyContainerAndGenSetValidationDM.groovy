/**
 * Created by BRajamanickam on 5/17/2017.
 */
import com.navis.argo.business.reference.Container
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.AllOtherFrameworkPropertyKeys
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.util.message.MessageLevel
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.model.TruckingCompany
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Level
import org.apache.log4j.Logger

public class MATGvyContainerAndGenSetValidationDM extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {


    public void execute(TransactionAndVisitHolder inOutDao) {
        //super.execute(inOutDao);
        LOGGER.setLevel(Level.INFO);
        LOGGER.info(" MATGvyContainerAndGenSetValidation execute Stared.");

        try {
            //super.executeInternal(inOutDao);
            LOGGER.info(" Calling MATContainerAndGenSetValidationDM ");
            TruckTransaction tran = inOutDao.getTran();
            String deliveredChassisId = null, deliveredCntrAccessoryId = null, deliveredCntrId = null;
            if (tran == null) {
                LOGGER.error(" TruckTransaction found null " + tran);
                return;
            }
            Container receivedContainer = tran.tranContainer;

            if (receivedContainer == null) {
                LOGGER.error(" container found null " + receivedContainer);
                return;
            }
            LOGGER.info(" Calling MATGvyContainerAndGenSetValidation container " + receivedContainer.toString());

            String receivedEquipmentId = receivedContainer.getEqIdFull();
            LOGGER.info("Equipment ID for MATGvyContainerAndGenSetValidation validation " + receivedEquipmentId);

            String receiveAccessoryId = tran.getCtrAccessoryId();
            String receivedChassisId = tran.getTranChassis() != null ? tran.getTranChassis().getEqIdFull() : tran.getTranChassis();

            if (receiveAccessoryId == null) receiveAccessoryId = "";
            if (receivedChassisId == null) receivedChassisId = "";

            LOGGER.info("receivedContainer-->" + receivedContainer + "chassisId-->" + receivedChassisId + "receiveAccessoryId-->" + receiveAccessoryId);

            if (tran.isReceival() && receivedEquipmentId != null) {
                TruckTransaction transaction = getLastDeliverTransaction(receivedEquipmentId);
                LOGGER.info("transaction-->" + transaction);
                if (transaction == null) {
                    LOGGER.info("transaction found null , hence skipping the validation-->" + transaction);
                    return;
                }
                deliveredCntrId = transaction.getTranCtrNbr();
                deliveredCntrAccessoryId = transaction.getCtrAccessoryId();
                deliveredChassisId = transaction.getTranChassis() != null && !transaction.isOwnerChassis() ? transaction.getTranChassis().getEqIdFull() : null;

                boolean isOwnerChassis = transaction.isOwnerChassis();
                TruckingCompany deliveredTruckingCompany = transaction.getTranTruckingCompany();

                TruckVisitDetails tvdtls = tran.getTranTruckVisit();
                String truckingCoId = tvdtls.getTvdtlsTruckingCoId();
                LOGGER.info("Tran TRCO : " + truckingCoId);
                TruckingCompany curretnTruckingCompany = tran.getTranTruckingCompany();
                if (truckingCoId != null) {
                    curretnTruckingCompany = TruckingCompany.findTruckingCompany(truckingCoId);
                }
                if (deliveredTruckingCompany != null && curretnTruckingCompany != null) {
                    LOGGER.info("deliveredTruckingCompany.getBzuName " + deliveredTruckingCompany.getBzuName() + "deliveredTruckingCompany id" + deliveredTruckingCompany.getBzuId() + "curretnTruckingCompany-->" + curretnTruckingCompany.getBzuId() + " curretnTruckingCompany.getBzuName() " + curretnTruckingCompany.getBzuName());
                }
                if (deliveredCntrId == null) deliveredCntrId = "";
                if (deliveredChassisId == null) deliveredChassisId = isOwnerChassis ? "OWN" : "";
                if (deliveredCntrAccessoryId == null) deliveredCntrAccessoryId = "";

                LOGGER.info("deliveredCntrId " + deliveredCntrId + " deliveredChassisId " + deliveredChassisId + " deliveredCntrAccessoryId " + deliveredCntrAccessoryId);
                LOGGER.info("receivedEquipmentId " + receivedEquipmentId + " receivedChassisId " + receivedChassisId + " receiveAccessoryId " + receiveAccessoryId);

                if (transaction != null && (receivedEquipmentId != null && deliveredCntrId != null && deliveredCntrId.equalsIgnoreCase(receivedEquipmentId))
                        && ((receiveAccessoryId != null && deliveredCntrAccessoryId != null && !receiveAccessoryId.equalsIgnoreCase(deliveredCntrAccessoryId))
                        || (receivedChassisId != null && deliveredChassisId != null && !receivedChassisId.equalsIgnoreCase(deliveredChassisId)))) {
                    LOGGER.info(" isReceival Found mismatch in with chassis ,genset and container on MATContainerAndGenSetValidationDM");

                    deliveredChassisId = !deliveredChassisId.isEmpty() ? " with chassis id " + deliveredChassisId + " " : " with no chassis ";
                    deliveredCntrAccessoryId = !deliveredCntrAccessoryId.isEmpty() ? " accessory id " + deliveredCntrAccessoryId + " " : " no accessory ";

                    RoadBizUtil.appendMessage(MessageLevel.WARNING, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedEquipmentId +
                            " on previous delivery was associated " + deliveredChassisId + "and" + deliveredCntrAccessoryId + " with trucking company as " +
                            deliveredTruckingCompany.getBzuName() + "( " + deliveredTruckingCompany.getBzuId() + " )", null);

                    // return;
                }

                if (transaction != null && (receivedEquipmentId != null && deliveredCntrId != null && deliveredCntrId.equalsIgnoreCase(receivedEquipmentId))
                        && (curretnTruckingCompany.getBzuId() != null && deliveredTruckingCompany.getBzuId() != null
                        && !curretnTruckingCompany.getBzuId().equalsIgnoreCase(deliveredTruckingCompany.getBzuId()))) {
                    RoadBizUtil.appendMessage(MessageLevel.SEVERE, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedEquipmentId +
                            " on previous delivery was associated with trucking company as " +
                            deliveredTruckingCompany.getBzuName() + "( " + deliveredTruckingCompany.getBzuId() + " )", null);
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error occured MATContainerAndGenSetValidationDM", e)
        }


    }


    public TruckTransaction getLastDeliverTransaction(String tranCtrNbr) {
        DomainQuery truckTransQuery = com.navis.framework.portal.QueryUtils.createDomainQuery("TruckTransaction")
                .addDqPredicate(com.navis.framework.portal.query.PredicateFactory.eq(com.navis.road.RoadField.TRAN_CTR_NBR, tranCtrNbr)).
                addDqOrdering(com.navis.framework.portal.Ordering.desc(com.navis.road.RoadField.TRAN_HANDLED));
        List<TruckTransaction> truckTransactions = HibernateApi.getInstance().findEntitiesByDomainQuery(truckTransQuery);
        Iterator iterator = truckTransactions.iterator();
        while (iterator.hasNext()) {
            TruckTransaction transaction = (TruckTransaction) iterator.next();
            LOGGER.info("transaction transaction.getTranNbr() " + transaction.getTranNbr());
            if (transaction != null && transaction.isDelivery() && transaction.isComplete() && transaction.tranContainer != null) {
                LOGGER.info("11 transaction transaction.getTranNbr() " + transaction.getTranNbr());
                LOGGER.info("transaction.tranContainer " + transaction.tranContainer + " getTranChassis " + transaction.getTranChassis() + " getCtrAccessoryId " + transaction.getCtrAccessoryId());

                return transaction;
            }

        }
        return null;
    }

    private Logger LOGGER = Logger.getLogger(MATGvyContainerAndGenSetValidationDM.class);
}
