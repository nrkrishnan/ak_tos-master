package com.navis.road.business.adaptor.document
import com.navis.argo.business.reference.Equipment
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.inventory.InventoryEntity
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.atoms.UnitVisitStateEnum
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.inventory.business.units.UnitFacilityVisit;
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Logger
public class MATDetachChsAcc extends AbstractGateTaskInterceptor implements EGateTaskInterceptor
{
    private static final Logger LOGGER = Logger.getLogger(MATDetachChsAcc.class);
    public void execute(TransactionAndVisitHolder inDao)
    {
        LOGGER.info("MATDetachChsAcc Execution Started");
        this.log("Execution Started MATDetachChsAcc");
        TruckTransaction ThisTran = inDao.getTran();
        if (ThisTran == null)
            return;
        if (ThisTran.getTranCtrAccNbr() != null)
        {
            Unit ThisAccUnit = this.findActiveCHSInYardByID(ThisTran.getTranCtrAccNbr());
            LOGGER.warn("Found Acc CHS unit : "+ThisAccUnit);
            if (ThisAccUnit == null)
            {
                Unit ThisCtrUnit = this.findActiveUnitByAcc(ThisTran.getTranCtrAccNbr());
                if (ThisCtrUnit != null)
                {
                    LOGGER.warn("Found Acc CTR unit : "+ThisCtrUnit);
                    ThisAccUnit = ThisCtrUnit.dismount();
                    HibernateApi.getInstance().flush();
                }
            }
            if (ThisAccUnit != null)
            {
                ThisAccUnit.makeRetired();
                HibernateApi.getInstance().flush();
            }
        }
        if (ThisTran.getTranChsNbr() != null && !ThisTran.getTranChsIsOwners())
        {
            Unit ThisChsUnit = this.findActiveCHSInYardByID(ThisTran.getTranChsNbr());
            LOGGER.warn("Found CHS CTR unit : "+ThisChsUnit);
            if (ThisChsUnit == null)
            {
                Unit ThisCtrUnit = this.findActiveUnitByChs(ThisTran.getTranChsNbr());
                if (ThisCtrUnit != null)
                {
                    LOGGER.warn("Found CHS CTR unit : "+ThisCtrUnit);
                    UnitEquipment chsUe = ThisCtrUnit.getCurrentlyAttachedUe(Equipment.findEquipment(ThisTran.getTranChsNbr()));
                    if(chsUe!=null)
                    {
                        UnitFacilityVisit ufv = ThisCtrUnit.getUnitActiveUfvNowActive();
                        LOGGER.warn("Logger UFV null or not" +ufv);
                        if (ufv != null && ufv.getUfvTransitState() != null && UfvTransitStateEnum.S20_INBOUND.equals(ufv.getUfvTransitState()))
                        {
                            chsUe.detach(chsUe.getUeEquipment().getEqIdFull() + "detach")
                        } else {

                            LOGGER.warn("Found CHS CTR unit : "+ThisCtrUnit);
                            ThisChsUnit = ThisCtrUnit.dismount();
                            HibernateApi.getInstance().flush();
                        }
                    }
                }
            }
            if (ThisChsUnit != null)
            {
                ThisChsUnit.makeRetired();
                HibernateApi.getInstance().flush();
            }
        }
        executeInternal(inDao);
    }
    private Unit findActiveCHSInYardByID(String chsId)
    {
        UfvTransitStateEnum[] transits = new UfvTransitStateEnum[2];
        transits[0] = UfvTransitStateEnum.S20_INBOUND;
        transits[1] = UfvTransitStateEnum.S40_YARD;
        DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_VISIT_STATE,  UnitVisitStateEnum.ACTIVE))
                .addDqPredicate(PredicateFactory.in(UnitField.UNIT_CURRENT_UFV_TRANSIT_STATE,  transits))
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_ID, chsId))
        Unit[] unitList=Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
        if(unitList == null || unitList.size()==0)
        {
            return null;
        }
        return unitList[0];
    }
    private Unit findActiveUnitByChs(String chsId)
    {
        UfvTransitStateEnum[] transits = new UfvTransitStateEnum[2];
        transits[0] = UfvTransitStateEnum.S20_INBOUND;
        transits[1] = UfvTransitStateEnum.S40_YARD;
        DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_VISIT_STATE,  UnitVisitStateEnum.ACTIVE))
                .addDqPredicate(PredicateFactory.in(UnitField.UNIT_CURRENT_UFV_TRANSIT_STATE,  transits))
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_CARRIAGE_UE_EQ_ID, chsId))
        Unit[] unitList=Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
        if(unitList == null || unitList.size()==0)
        {
            return null;
        }
        return unitList[0];
    }
    private Unit findActiveUnitByAcc(String chsId)
    {
        UfvTransitStateEnum[] transits = new UfvTransitStateEnum[2];
        transits[0] = UfvTransitStateEnum.S20_INBOUND;
        transits[1] = UfvTransitStateEnum.S40_YARD;
        DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_VISIT_STATE,  UnitVisitStateEnum.ACTIVE))
                .addDqPredicate(PredicateFactory.in(UnitField.UNIT_CURRENT_UFV_TRANSIT_STATE,  transits))
                .addDqPredicate(PredicateFactory.eq(UnitField.UNIT_ACRY_EQUIP_IDS, chsId))
        Unit[] unitList=Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
        if(unitList == null || unitList.size()==0)
        {
            return null;
        }
        return unitList[0];
    }
}
