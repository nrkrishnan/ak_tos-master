import com.navis.argo.ArgoPropertyKeys
import com.navis.argo.ArgoRefEntity
import com.navis.argo.ArgoRefField
import com.navis.argo.business.atoms.BizRoleEnum
import com.navis.argo.business.reference.Agent
import com.navis.argo.business.reference.AgentRepresentation
import com.navis.argo.business.reference.ScopedBizUnit
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.message.MessageLevel
import com.navis.inventory.business.units.Unit
import com.navis.orders.business.eqorders.Booking
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckingCompany
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * @author Keerthi Ramachandran
 * @since 7/3/2017
 * <p>MatsonAncValidateTruckingCompany is ..</p> 
 */
class MatsonAncValidateTruckingCompany extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private static final Logger LOGGER = Logger.getLogger(MatsonAncValidateTruckingCompany.class);

    @Override
    void execute(TransactionAndVisitHolder inTransactionAndVisitHolder) {
        super.execute(inTransactionAndVisitHolder);
        LOGGER.setLevel(Level.DEBUG);
        LOGGER.debug("Begin execution of MatsonAncValidateTruckingCompany");
        TruckTransaction truckTransaction = inTransactionAndVisitHolder.getTran();
        LOGGER.debug("TruckTransaction\t" + truckTransaction);
        Unit unit = truckTransaction.getTranUnit();
        TruckingCompany company = truckTransaction.getTranTruckingCompany();
        if(company == null)
        {
            this.getMessageCollector().appendMessage(BizFailure.create("Trucking company has to be set in Transaction, contact your application administrator  "))
        }

        LOGGER.debug("Unit\t" + unit);
        LOGGER.debug("truckTransaction.getTranEqoNbr()\t" + truckTransaction.getTranEqoNbr());
        //LOGGER.debug("unit.getUnitPrimaryUe().getUeDepartureOrderItem()\t" + unit.getUnitPrimaryUe().getUeDepartureOrderItem());
        //LOGGER.debug("unit.getUnitPrimaryUe().getUeDepartureOrderItem().getEqboiOrder().getEqboNbr()\t" + unit.getUnitPrimaryUe().getUeDepartureOrderItem().getEqboiOrder().getEqboNbr());

        String bookingNum = truckTransaction.getTranEqoNbr() != null ? truckTransaction.getTranEqoNbr() : null;
        Object[] errorObjects = new Object[1];
        errorObjects[0] = truckTransaction.getTranEqoNbr();
        if (bookingNum == null) {
            this.getMessageCollector().appendMessage(MessageLevel.SEVERE, ArgoPropertyKeys.BUNDLE,
                    "Booking is not available in the Transaction ", errorObjects);
            LOGGER.debug("Booking is not available in the Transaction, returning with SEVERE message");
            return;
        } else {
            List<Booking> bookingList = Booking.findBookingsByNbr(bookingNum);
            Booking booking = null;
            if (bookingList != null && bookingList.size() > 0) {
                booking = bookingList.get(0);
                LOGGER.debug("Booking\t" + booking);
            }
            if (booking == null) {
                this.getMessageCollector().appendMessage(MessageLevel.SEVERE, ArgoPropertyKeys.BUNDLE,
                        "Booking is not available in the Transaction ", null);
                return;
            }

            ScopedBizUnit shipper = booking.getEqoShipper();
            ScopedBizUnit consignee = booking.getEqoConsignee();

            /*com.navis.argo.business.reference.Shipper shipper1 =
                    com.navis.argo.business.reference.Shipper.findShipper(booking.getEqoShipper().getBzuId());

            com.navis.argo.business.reference.Shipper consignee1 =
                    com.navis.argo.business.reference.Shipper.findShipper(booking.getEqoConsignee().getBzuId());*/

            /*Agent shipperAgent = Agent.resolveAgentFromScopedBizUnit(ScopedBizUnit.hydrate((Serializable) shipper1.getRepAgentTableKey()));
            Agent consigneeAgent = Agent.resolveAgentFromScopedBizUnit(ScopedBizUnit.hydrate((Serializable) consignee1.getRepAgentTableKey()));*/

            if (TranSubTypeEnum.RE.equals(truckTransaction.getTranSubType())
                    || TranSubTypeEnum.DE.equals(truckTransaction.getTranSubType())) {
                //RE Validate the Shipper Agent
                List<String> shipperAgent = getTruckerByScopedBzuId(shipper);
                if(!shipperAgent.isEmpty() && shipperAgent.contains(company.getBzuId())){
                    //do nothing, valid trucker
                } else{
                    final BizFailure bizFailure = BizFailure.create("The Trucking company "+company.getBzuId() +"("+company.getBzuName()+") is not authorized by the shipper");
                    this.getMessageCollector().appendMessage(bizFailure);
                }

            } else if (TranSubTypeEnum.DI.equals(truckTransaction.getTranSubType())
                    || TranSubTypeEnum.RI.equals(truckTransaction.getTranSubType())) {
                //DI Validate the Consignee Agent
                //RE Validate the Shipper Agent
                List<String> consigneeAgent = getTruckerByScopedBzuId(consignee);
                if(!consigneeAgent.isEmpty() && consigneeAgent.contains(company.getBzuId())){
                    //do nothing, valid trucker
                } else{
                    final BizFailure bizFailure = BizFailure.create("The Trucking company "+company.getBzuId() +"("+company.getBzuName()+") is not authorized by the consignee");
                    this.getMessageCollector().appendMessage(bizFailure);
                }
            }
        }
        LOGGER.debug("End execution of MatsonAncValidateTruckingCompany");
    }


    static final List<String> getTruckerByScopedBzuId(ScopedBizUnit shipperScopedBizUnit)
    {
        List<String> truckerList = new ArrayList<>();
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoRefEntity.AGENT_REPRESENTATION)
                .addDqPredicate(PredicateFactory.eq(ArgoRefField.AGENTREP_SCOPED_BUSINESS_UNIT, shipperScopedBizUnit.getBzuGkey()));
        LOGGER.debug(dq);
        List<AgentRepresentation> agentRepList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
        LOGGER.debug ("agentRepList.size()" + agentRepList.size())
        if (agentRepList.size() > 0)
        {
            for(AgentRepresentation agentRepresentation:agentRepList){
                LOGGER.debug ("agentListGkey "+ agentRepresentation.getAgentrepAgent());
                dq = QueryUtils.createDomainQuery(ArgoRefEntity.AGENT).addDqPredicate(PredicateFactory.eq(ArgoRefField.BZU_GKEY,agentRepresentation.getAgentrepAgent().getBzuGkey()));
                LOGGER.debug(dq);
                List<Agent> agentList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                LOGGER.debug ("agentList.size()" + agentList.size())
                if(agentList.size()>0){
                    for(Agent agent:agentList){
                        LOGGER.debug ("agentId "+ agent.getBzuId());
                        truckerList.add(agent.getBzuId());
                    }
                }
                /*Iterator iterAgent = agentList.iterator();
                while(iterAgent.hasNext()) {
                    Agent agent = iterAgent.next();
                    LOGGER.debug ("agentId "+ agent.getBzuId());
                    truckerList.add(agent.getBzuId());
                }*/
            }
            LOGGER.debug("trucker " + truckerList);
            return truckerList;
        }
        return Collections.EMPTY_LIST;
    }
}
