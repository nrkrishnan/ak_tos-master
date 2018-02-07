/**
 * Created by BRajamanickam on 5/17/2017.
 */


import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.reference.Chassis
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.EquipType
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.AllOtherFrameworkPropertyKeys
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChange
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.util.BizFailure
import com.navis.framework.util.message.MessageLevel
import com.navis.road.RoadField
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.model.TruckingCompany
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Level
import org.apache.log4j.Logger

public class MATGvyContainerAndGenSetValidation extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {


    public void execute(TransactionAndVisitHolder inOutDao) {
        super.execute(inOutDao);
        LOGGER.setLevel(Level.INFO);
        LOGGER.info(" MATGvyContainerAndGenSetValidation execute Stared.");

        try {
            super.executeInternal(inOutDao);
            LOGGER.info(" Calling MATContainerAndGenSetValidation ");
            TruckTransaction tran = inOutDao.getTran();
            String deliveredChassisId = null, deliveredCntrAccessoryId = null, deliveredCntrId = null;
            if (tran == null) {
                LOGGER.error(" TruckTransaction found null " + tran);
                return;
            }
            Container receivedContainer = tran.tranContainer;
            Chassis receivedChassis = tran.tranChassis;

            if (receivedContainer == null && receivedChassis ==null) {
                LOGGER.error(" container found null " + receivedContainer +"Chassis found "+receivedChassis);
                return;
            }
            if(receivedContainer != null)
                LOGGER.info(" Calling MATGvyContainerAndGenSetValidation container " + receivedContainer.toString());
            if(receivedChassis != null)
                LOGGER.info(" Calling MATGvyContainerAndGenSetValidation chassis  " + receivedChassis.toString());

            String receivedContainerId  = receivedContainer!=null?receivedContainer.getEqIdFull():"";
            LOGGER.info("Container ID for MATGvyContainerAndGenSetValidation validation " + receivedContainerId);

            String receiveAccessoryId = tran.getCtrAccessoryId()!=null?tran.getCtrAccessoryId():"";
            //old String receivedChassisId = tran.getTranChassis() != null ? tran.getTranChassis().getEqIdFull() : tran.getTranChassis();
            String receivedChassisId = tran.getTranChassis() != null ? tran.getTranChassis().getEqIdFull() : ""; // else part is wrong in above code, it' still null, assign as empty
            //todo add a way to distinguish between isOwnerChassis here, as owner can swap chassis. so may be needed to skip validation.

            //if (receiveAccessoryId == null) receiveAccessoryId = "";
            //if (receivedChassisId == null) receivedChassisId = "";

            LOGGER.info("receivedContainer-->" + receivedContainer + "chassisId-->" + receivedChassisId + "receiveAccessoryId-->" + receiveAccessoryId);
            TruckTransaction lastTruckTransaction = null;

            if (tran.isReceival()) {
                if(receivedContainerId != null && !receivedContainerId.isEmpty()) // for RM, RE, RI
                    lastTruckTransaction = getLastDeliverTransaction(receivedContainerId);
                else if ((receivedContainerId == null ||(receivedContainerId !=null && receivedContainerId.isEmpty())) && receivedChassisId!=null && !receivedChassisId.isEmpty()) // for RC
                    lastTruckTransaction = getLastDeliverTransactionForChassis(receivedChassisId);
                LOGGER.info("transaction-->" + lastTruckTransaction);
                if (lastTruckTransaction == null) {
                    LOGGER.info("transaction found null , hence skipping the validation-->" + lastTruckTransaction);
                    return;
                }
                deliveredCntrId = lastTruckTransaction.getTranCtrNbr();
                deliveredCntrAccessoryId = lastTruckTransaction.getCtrAccessoryId();
                deliveredChassisId = lastTruckTransaction.getTranChassis()!=null && !lastTruckTransaction.isOwnerChassis()? lastTruckTransaction.getTranChassis().getEqIdFull():null;
                LOGGER.info("deliveredCntrId"+deliveredCntrId+"deliveredCntrAccessoryId"+deliveredCntrAccessoryId+"deliveredChassisId");

                boolean isOwnerChassis  = lastTruckTransaction.isOwnerChassis();
                TruckingCompany deliveredTruckingCompany = lastTruckTransaction.getTranTruckingCompany();

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
                if (deliveredChassisId == null) deliveredChassisId = isOwnerChassis? "OWN" : "";
                if (deliveredCntrAccessoryId == null) deliveredCntrAccessoryId = "";

                LOGGER.info("deliveredCntrId " + deliveredCntrId + " deliveredChassisId " + deliveredChassisId + " deliveredCntrAccessoryId " + deliveredCntrAccessoryId);
                LOGGER.info("receivedEquipmentId " + receivedContainerId + " receivedChassisId " + receivedChassisId + " receiveAccessoryId " + receiveAccessoryId);

                Boolean doesContainerMatch = receivedContainerId != null && deliveredCntrId != null && deliveredCntrId.equalsIgnoreCase(receivedContainerId);
                Boolean isContainerAvailableInLastDelivery = deliveredCntrId != null;
                Boolean doesChassisMatch = receivedChassisId != null && deliveredChassisId != null && receivedChassisId.equalsIgnoreCase(deliveredChassisId);
                Boolean isChassisAvailableInLastDelivery = deliveredChassisId != null;
                Boolean doesContainerAccessoryMatch = receiveAccessoryId != null && deliveredCntrAccessoryId != null && receiveAccessoryId.equalsIgnoreCase(deliveredCntrAccessoryId);
                Boolean isAccessoryAvailableInLastDelivery = deliveredCntrAccessoryId != null;
                Boolean showErrorMessage = Boolean.FALSE;
                Boolean containerValidated = Boolean.FALSE;
                Boolean chassisValidated = Boolean.FALSE;
                Boolean containerAccessoryValidated = Boolean.FALSE;
                if (lastTruckTransaction != null) {
                    showErrorMessage = Boolean.TRUE;
                    if ((isContainerAvailableInLastDelivery && doesContainerMatch) || !isContainerAvailableInLastDelivery){
                        LOGGER.info("Container Validation - Last delivered container & current contianer matches");
                        containerValidated = Boolean.TRUE;}
                    if ((isChassisAvailableInLastDelivery && doesChassisMatch) || !isChassisAvailableInLastDelivery)
                    {LOGGER.info("Chassis Validation - Last delivered Chassis  & current container chassis matches");
                        chassisValidated = Boolean.TRUE;}
                    if ((isAccessoryAvailableInLastDelivery && doesContainerAccessoryMatch) || !isAccessoryAvailableInLastDelivery)
                    {LOGGER.info("Container accessory Validation - Last delivered Container Accessory  & current Container accessory matches");
                        containerAccessoryValidated = Boolean.TRUE;}
                    if (containerValidated && chassisValidated && containerAccessoryValidated) {
                        LOGGER.info("Container, Chassis and Container Accessory Matches");
                        showErrorMessage = Boolean.FALSE;
                    }
                }
                if (showErrorMessage) {
                    LOGGER.info(" isReceival Found mismatch in with chassis ,genset and container on MATContainerAndGenSetValidation");
                    deliveredChassisId = !deliveredChassisId.isEmpty() ? " with chassis id " + deliveredChassisId + " " : " with no chassis ";
                    deliveredCntrAccessoryId = !deliveredCntrAccessoryId.isEmpty() ? " accessory id " + deliveredCntrAccessoryId + " " : " no accessory ";
                    String deliveredContainerMessage = !deliveredCntrId.isEmpty() ? " Container id " + deliveredCntrId + " " : " no container ";
                    if (receivedContainerId != null && !receivedContainerId.isEmpty()) {
                        LOGGER.info("Showing warning based on container");
                        if (deliveredTruckingCompany != null)
                            RoadBizUtil.appendMessage(MessageLevel.WARNING, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedContainerId +
                                    " on previous delivery was associated " + deliveredChassisId + "and" + deliveredCntrAccessoryId + " with trucking company as " +
                                    deliveredTruckingCompany.getBzuName() + "( " + deliveredTruckingCompany.getBzuId() + " )", null);
                        else if (deliveredTruckingCompany == null)
                            RoadBizUtil.appendMessage(MessageLevel.WARNING, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedContainerId +
                                    " on previous delivery was associated " + deliveredChassisId + "and" + deliveredCntrAccessoryId, null);
                    } else if ((receivedContainerId == null || (receivedContainerId != null && receivedContainerId.isEmpty())) && (receivedChassisId != null && !receivedChassisId.isEmpty())) {
                        LOGGER.info("Showing warning based on chassis");
                        if (deliveredTruckingCompany != null)
                            RoadBizUtil.appendMessage(MessageLevel.WARNING, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The chassis " + receivedChassisId +
                                    " on previous delivery was associated with " + deliveredContainerMessage + "and" + deliveredCntrAccessoryId + " with trucking company as " +
                                    deliveredTruckingCompany.getBzuName() + "( " + deliveredTruckingCompany.getBzuId() + " )", null);
                        else if (deliveredTruckingCompany == null)
                            RoadBizUtil.appendMessage(MessageLevel.WARNING, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The chassis " + receivedChassisId +
                                    " on previous delivery was associated with " + deliveredContainerMessage + "and" + deliveredCntrAccessoryId, null);
                    }

                    // return;
                }

                /**
                 * In GATE AUTO, during automatic outgate, trucking compant is not mandatory, can be null
                 */
                if(lastTruckTransaction != null && deliveredTruckingCompany !=null && deliveredTruckingCompany.getBzuId() !=null){
                    /**
                     * The Last Transaction can be anything, DC / DM / DE / DI, so it can either the Chassis or Container is delivered, if any one is returning back, then do the validation
                     */
                    if((isContainerAvailableInLastDelivery || isChassisAvailableInLastDelivery) && (doesContainerMatch || doesChassisMatch)){
                        if((curretnTruckingCompany.getBzuId() != null && deliveredTruckingCompany.getBzuId() != null
                                && !curretnTruckingCompany.getBzuId().equalsIgnoreCase(deliveredTruckingCompany.getBzuId()))){
                            RoadBizUtil.appendMessage(MessageLevel.SEVERE, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedContainerId +
                                    " on previous delivery was associated with trucking company as "+
                                    deliveredTruckingCompany.getBzuName() +"( "+deliveredTruckingCompany.getBzuId()+" )" , null);
                        }
                    }
                } else if(lastTruckTransaction != null && deliveredTruckingCompany == null){
                    LOGGER.error("The current receive of the container"+receivedContainerId+"\t chassis "+receivedChassisId+" was delivered previously in auto gate without capturing the trucking company");
                }

                /*if (lastTruckTransaction != null && (receivedEquipmentId != null && deliveredCntrId != null && deliveredCntrId.equalsIgnoreCase(receivedEquipmentId))
                        && (curretnTruckingCompany.getBzuId() != null && deliveredTruckingCompany.getBzuId() != null
                        && !curretnTruckingCompany.getBzuId().equalsIgnoreCase(deliveredTruckingCompany.getBzuId()))) {
                    RoadBizUtil.appendMessage(MessageLevel.SEVERE, AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, "The unit " + receivedEquipmentId +
                            " on previous delivery was associated with trucking company as "+
                            deliveredTruckingCompany.getBzuName() +"( "+deliveredTruckingCompany.getBzuId()+" )" , null);
                }*/
            }
        }
        catch (Exception e) {
            LOGGER.info("Error occurred MATContainerAndGenSetValidation" , e)
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
            if (transaction != null && transaction.isDelivery() && transaction.isComplete() && transaction.getTranContainer() != null) {
                LOGGER.info("11 transaction transaction.getTranNbr() " + transaction.getTranNbr());
                LOGGER.info("transaction.tranContainer " + transaction.tranContainer + " getTranChassis " + transaction.getTranChassis() + " getCtrAccessoryId " + transaction.getCtrAccessoryId());
                return transaction;
            }

        }
        return null;
    }
    public TruckTransaction getLastDeliverTransactionForChassis(String tranChassisNumber) {
        DomainQuery truckTransQuery = com.navis.framework.portal.QueryUtils.createDomainQuery("TruckTransaction")
                .addDqPredicate(com.navis.framework.portal.query.PredicateFactory.eq(com.navis.road.RoadField.TRAN_CHS_NBR, tranChassisNumber)).
                addDqOrdering(com.navis.framework.portal.Ordering.desc(com.navis.road.RoadField.TRAN_HANDLED));
        List<TruckTransaction> truckTransactions = HibernateApi.getInstance().findEntitiesByDomainQuery(truckTransQuery);
        Iterator iterator = truckTransactions.iterator();
        while (iterator.hasNext()) {
            TruckTransaction transaction = (TruckTransaction) iterator.next();
            LOGGER.info("transaction transaction.getTranNbr() " + transaction.getTranNbr());
            if (transaction != null && transaction.isDelivery() && transaction.isComplete() && transaction.getTranChassis()!= null) {
                LOGGER.info("getLastDeliverTransactionForChassis/  transaction transaction.getTranNbr() " + transaction.getTranNbr());
                LOGGER.info("transaction.tranContainer " + transaction.tranContainer + " getTranChassis " + transaction.getTranChassis() + " getCtrAccessoryId " + transaction.getCtrAccessoryId());
                return transaction;
            }
        }
        return null;
    }

    private Logger LOGGER = Logger.getLogger(MATGvyContainerAndGenSetValidation.class);
}
