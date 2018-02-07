import com.navis.argo.ArgoRefEntity
import com.navis.argo.ArgoRefField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.EquipClassEnum
import com.navis.argo.business.reference.Agent
import com.navis.argo.business.reference.AgentRepresentation
import com.navis.argo.business.reference.Container
import com.navis.argo.business.reference.ScopedBizUnit
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckingCompany
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * Created by brajamaickam on 9/10/2017.
 */

public class MATGvyTruckerValidation extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private static Logger LOGGER = Logger.getLogger(MATGvyTruckerValidation.class);

    public void execute(TransactionAndVisitHolder dao) {
        super.execute(dao);
        LOGGER.setLevel(Level.DEBUG);
        TruckTransaction tran = dao.tran;
        if (tran != null) {
            if(tran.getTranUnit()==null){
                LOGGER.debug(" MATGvyTruckerValidation Error , No unit found");
                return;
            }

            ScopedBizUnit consignee = tran.getTranUnit().getUnitGoods() != null ? tran.getTranUnit().getUnitGoods().getGdsConsigneeBzu() : null;
            if (consignee == null) {  //first
                LOGGER.error(" MATGvyTruckerValidation no consignee available for unit " + tran.getTranUnit());
                return;
            }

            def consigneeId = consignee.getBzuId();
            def consigneeName = consignee.getBzuName();
            TruckingCompany company = tran.getTranTruckingCompany();
            if (company == null) {
                LOGGER.error(" MATGvyTruckerValidation Error , No TruckingCompany for consignee found for the container " + tran.getTranUnit());
                //return;
            }

            if (!(TranSubTypeEnum.DM.equals(tran.getTranSubType()) || TranSubTypeEnum.DI.equals(tran.getTranSubType()) || TranSubTypeEnum.DE.equals(tran.getTranSubType()))) {
                LOGGER.error(" MATGvyTruckerValidation gate transaction type not applicable " + tran.getTranSubType())
                System.out.println("Invalid tran.getTranSubType()-->" + tran.getTranSubType());
                return;
            }
            List<Agent> shipperAgent = getTruckerByScopedBzuId(consignee);
            LOGGER.error(" MATGvyTruckerValidation shipperAgent " + shipperAgent);
            if (shipperAgent.isEmpty()) {
                LOGGER.error(" MATGvyTruckerValidation no shipperAgent available " + shipperAgent);
                return;
            }

            LOGGER.debug("shipperAgent-->" + shipperAgent);
            Set<String> truckAgents = getTruckAgentsByTruckCompany(shipperAgent);
            LOGGER.debug("truckAgents-->" + truckAgents.size()  +"company.getBzuId()-->"+company.getBzuId());
            if (shipperAgent != null && !shipperAgent.isEmpty() && truckAgents != null && !truckAgents.isEmpty()
                    && (company == null || !truckAgents.contains(company.getBzuId()))) {
                final BizFailure bizFailure = BizFailure.create("The Trucking company " + company.getBzuId() + "(" + company.getBzuName() + ") is not authorized " +
                        "trucker for the unit " + tran.getTranContainer().getEqIdFull() + " with consignee as "+ consigneeName + "( " + consigneeId + " )");
                this.getMessageCollector().appendMessage(bizFailure);
            }
        }
    }

    static final Set<String> getTruckAgentsByTruckCompany(List<Agent> inAgent) {
        Set<String> truckerAgents = new HashSet<String>();
        for (Agent agent : inAgent) {
            System.out.println("agentRepresentation-->" + agent);
            //Agent  agents = agent  as Agent;
            Set<AgentRepresentation> agentRepresentations = agent.getAgentRepresentations();
            LOGGER.debug("reps-->" + agentRepresentations);
            for (AgentRepresentation agentRepresentation : agentRepresentations) {
                if (agentRepresentation != null && agentRepresentation.getAgentrepScopedBusinessUnit() != null) {
                    truckerAgents.add(agentRepresentation.getAgentrepScopedBusinessUnit().bzuId)
                    LOGGER.debug("agent :" + agent.getBzuId() + "2 agentrep-->" + agentRepresentation.getAgentrepScopedBusinessUnit().bzuId);
                }
            }
        }
        return truckerAgents;
    }

    static final List<Agent> getTruckerByScopedBzuId(ScopedBizUnit shipperScopedBizUnit) {
        List<Agent> truckerList = new ArrayList<Agent>();
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoRefEntity.AGENT_REPRESENTATION)
                .addDqPredicate(PredicateFactory.eq(ArgoRefField.AGENTREP_SCOPED_BUSINESS_UNIT, shipperScopedBizUnit.getBzuGkey()));
        LOGGER.debug(dq);
        List<AgentRepresentation> agentRepList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
        LOGGER.debug("agentRepList.size()" + agentRepList.size())
        if (agentRepList.size() > 0) {
            for (AgentRepresentation agentRepresentation : agentRepList) {
                LOGGER.debug("agentListGkey " + agentRepresentation.getAgentrepAgent());
                dq = QueryUtils.createDomainQuery(ArgoRefEntity.AGENT).addDqPredicate(PredicateFactory.eq(ArgoRefField.BZU_GKEY, agentRepresentation.getAgentrepAgent().getBzuGkey()));
                LOGGER.debug(dq);
                List<Agent> agentList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
                LOGGER.debug("agentList.size()" + agentList.size())
                if (agentList.size() > 0) {
                    for (Agent agent : agentList) {
                        LOGGER.debug("agentId " + agent.getBzuId());
                        // truckerList.add(agent.getBzuId());
                        truckerList.add(agent);
                    }
                }
            }
            LOGGER.debug("trucker " + truckerList);
            return truckerList;
        }
        return Collections.EMPTY_LIST;
    }
}
