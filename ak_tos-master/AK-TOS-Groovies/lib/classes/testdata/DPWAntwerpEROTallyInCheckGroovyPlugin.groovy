import com.navis.argo.business.api.GroovyApi
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.message.MessageLevel
import com.navis.road.RoadEntity
import com.navis.road.RoadField
import com.navis.road.RoadPropertyKeys
import com.navis.road.business.atoms.TranStatusEnum
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder

public class DPWAntwerpEROTallyInCheckGroovyPlugin
{
    public void execute(TransactionAndVisitHolder inDao, GroovyApi api)
    {
        def tran = inDao.getTran();
        if (tran == null)
            return;
        com.navis.orders.business.eqorders.EquipmentOrderItem eqoi = tran.getTranEqoItem()

        if (eqoi == null)
            return;
        if (countTruckTransactionForOrderItem(eqoi.getPrimaryKey() + 1 > eqoi.getEqoiQty().longValue()))
            RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, RoadPropertyKeys.GATE__BKG_QTY_EXCEEDED, "Quantity", tran.getTranEqoNbr(), tran.getTranCtrNbr(), "\nOpmerking:", "Kijk naar de EVENT HISTORY in het ERO. Daar staan de containers die al binnen zijn voor dit order")
    }
    public long countTruckTransactionForOrderItem(Serializable inOrderItemGkey)
    {
        DomainQuery dq = QueryUtils.createDomainQuery(RoadEntity.TRUCK_TRANSACTION).addDqPredicate(PredicateFactory.eq(RoadField.TRAN_EQO_ITEM, inOrderItemGkey))
        List tranlist = HibernateApi.getInstance().findEntitiesByDomainQuery(dq)
        Iterator iter = tranlist.iterator()
        long ctrsReceived = 0

        while (iter.hasNext())
        {
            com.navis.road.business.model.TruckTransaction trktran = (com.navis.road.business.model.TruckTransaction) iter.next()
            if (trktran != null && trktran.getTranStatus() != null && trktran.getTranStatus() != TranStatusEnum.CANCEL)
                ctrsReceived++
        }

        return ctrsReceived;
    }
}