import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.external.services.AbstractGeneralNoticeCodeExtension
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizViolation
import com.navis.inventory.InventoryEntity
import com.navis.inventory.business.units.EqBaseOrder
import com.navis.inventory.business.units.EqBaseOrderItem
import com.navis.orders.OrdersEntity
import com.navis.orders.OrdersField
import com.navis.orders.business.eqorders.Booking
import com.navis.orders.business.eqorders.EquipmentOrderItem
import com.navis.services.business.event.GroovyEvent

/**
 * Created by psethuraman on 7/14/2016.
 */
class MatUpdateEmptyBookingCommodityExt extends AbstractGeneralNoticeCodeExtension {
    /**
     * Print document based on the configuration docTypeId parameter
     *
     * @param inOutDao
     */
    public void execute(GroovyEvent inEvent) throws Exception {
        //this.log(Level.INFO);
        this.log(" MatGvyUpdateEmptyBookingCommodity execute Stared.");
        try {
            List<EqBaseOrder> baseOrders = Collections.EMPTY_LIST;
            DomainQuery domainQuery = QueryUtils.createDomainQuery(InventoryEntity.EQ_BASE_ORDER).
                    addDqPredicate(PredicateFactory.eq(OrdersField.EQO_EQ_STATUS, FreightKindEnum.MTY));
            // addDqPredicate(PredicateFactory.eq(OrdersField.EQOI_COMMODITY_DESC, "--"));
            baseOrders = HibernateApi.getInstance().findEntitiesByDomainQuery(domainQuery);
            for (EqBaseOrder baseOrder : baseOrders) {
                Set<EqBaseOrderItem> baseOrderItems = baseOrder.getEqboOrderItems();
                for (EqBaseOrderItem baseOrderItem : baseOrderItems) {
                    EquipmentOrderItem equipmentOrderItem = EquipmentOrderItem.resolveEqoiFromEqboi(baseOrderItem);
                    this.log("Commodity is (in Booking) " + equipmentOrderItem.getEqoiCommodityDesc());

                    if (equipmentOrderItem.getEqoiCommodity() != null &&
                            ("--".equals(equipmentOrderItem.getEqoiCommodity().getCmdyId()) ||
                                    "DONT USE IT".equals(equipmentOrderItem.getEqoiCommodity().getCmdyId()))) {
                        this.log("Commodity Gkey " + equipmentOrderItem.getEqoiCommodity().getCmdyGkey());
                        this.log("Commodity Gkey " + equipmentOrderItem.getEqoiCommodity().getCmdyId());
                        this.log("Commodity Gkey Desc " + equipmentOrderItem.getEqoiCommodity().getCmdyDescription());

                        equipmentOrderItem.setEqoiCommodity();
                        //HibernateApi.getInstance().save(baseOrder);
                    }
                }
            }

            this.log("inside try");
        } catch (Exception e) {
            this.log(e)
            throw new BizViolation(e);
        }


        try {
            List<Booking> listOfBookings = Collections.EMPTY_LIST;
            DomainQuery domainQuery = QueryUtils.createDomainQuery(OrdersEntity.BOOKING).
                    addDqPredicate(PredicateFactory.eq(OrdersField.EQO_EQ_STATUS, FreightKindEnum.MTY));
            // addDqPredicate(PredicateFactory.eq(OrdersField.EQOI_COMMODITY_DESC, "--"));
            listOfBookings = HibernateApi.getInstance().findEntitiesByDomainQuery(domainQuery);
            for (Booking booking : listOfBookings) {
                booking.getEqboOrderItems()
            }
            /*for (EqBaseOrder baseOrder : listOfBookings) {
                Set<EqBaseOrderItem> baseOrderItems = baseOrder.getEqboOrderItems();
                for (EqBaseOrderItem baseOrderItem : baseOrderItems) {
                    EquipmentOrderItem equipmentOrderItem = EquipmentOrderItem.resolveEqoiFromEqboi(baseOrderItem);
                    this.log(equipmentOrderItem.getEqoiCommodityDesc());
                }
            }*/

            this.log("inside try");
        } catch (Exception e) {
            this.log(e)
            throw new BizViolation(e);
        }

        this.log(" MatGvyUpdateEmptyBookingCommodity execute Completed.");
    }
}
