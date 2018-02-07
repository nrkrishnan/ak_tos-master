package codeExtensions

import com.navis.argo.business.api.GroovyApi
import com.navis.framework.portal.BizRequest
import com.navis.framework.portal.BizResponse
import com.navis.framework.portal.CrudOperation
import com.navis.framework.util.BizFailure
import com.navis.framework.util.message.MessageCollector
import com.navis.inventory.InventoryEntity
import com.navis.inventory.business.InventoryFacade
import com.navis.inventory.business.InventoryFacadeImpl
import com.navis.inventory.business.units.EquipmentState
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitEquipment
import com.navis.services.business.event.Event
import com.navis.services.business.event.GroovyEvent

/**
 * Created by VNatesan on 8/1/2016.
 */
class MATUnitRollBookingUpdateExtGvy extends GroovyApi {

    /**
     * execute
     * @param inEvent
     */
    public void execute(GroovyEvent inEvent, Object inApi) {

        GroovyApi api = inApi;
        Event thisEvent = inEvent.getEvent();

        if (thisEvent == null) {
            return;
        }

        Unit unit = (Unit) inEvent.getEntity();

        if (unit == null) {
            return;
        }
        this.log("Start Event ---:" + thisEvent.getEventTypeId() + " on Unit :" + unit.getUnitId() + " ---:")
        /**
         Removing duplicate code ,this is exist in interceptors
         def bookingNbr;
         UnitEquipment  ue = unit.getUnitPrimaryUe();
         if (ue.getUeDepartureOrderItem() != null) {bookingNbr = ue.getUeDepartureOrderItem().getEqboiOrder().getEqboNbr();}this.log("Booking Nbr for Event  ---:"+thisEvent.getEventTypeId()+ " Booking Nbr :"+bookingNbr)
         //unit.getUnitGoods().setGdsBlNbr(bookingNbr);
         **/
        MessageCollector collector = getMessageCollector();
        try {
            BizRequest request = new BizRequest(com.navis.argo.ContextHelper.getThreadUserContext());
            this.log("BizRequest created for current user context");
            //com.navis.argo.ContextHelper.getThreadUserContext();
            Serializable[] ufvGkeys = new Serializable[1];
            ufvGkeys[0] = unit.getUnitActiveUfvNowActive().getUfvGkey();
            this.log("ufvGKey is    "+ufvGkeys[0]);
            CrudOperation crud = new CrudOperation(null, CrudOperation.TASK_UPDATE, InventoryEntity.UNIT_FACILITY_VISIT, null, ufvGkeys);
            request.addCrudOperation(crud);
            BizResponse response = new BizResponse();
            InventoryFacade facade = new InventoryFacadeImpl();
            facade.refreshUnit(request, response);
            collector.getMessages().addAll(response.getMessages());
            if (!collector.hasError()) {
                // proceed

            } else {
                //send mail
                this.log("Message Collector has error,");
                for(BizFailure message:collector.getMessages()){
                    this.log(message.getMessage());
                }
            }
        } catch (Exception ex) {
            this.log("Not able to retrive objects from DB")
        }


        //unit = Unit.hydrate(unit.getUnitActiveUfvNowActive().getUfvGkey());
        unit.getUnitTouchCtr();
        Thread.sleep(30000L);
        unit = Unit.hydrate((Serializable)unit.getUnitGkey());
        def doer = thisEvent.getEvntAppliedBy()
        boolean isAlwaysSendIGT = false;



        try {
            if (unit != null) {
                UnitEquipment unitEquipment = unit.getUnitPrimaryUe()
                EquipmentState ueEquipmentState = unitEquipment.getUeEquipmentState()
                def equipFlex01 = ueEquipmentState != null ? ueEquipmentState.getEqsFlexString01() : ''
                this.log("ueEquipmentState :equipFlex01(getEqsFlexString01) Value---:" + equipFlex01)
                def gvyEquiObj = api.getGroovyClassInstance("GvyCmisEquiDetail");
                gvyEquiObj.setEqCntrSvr(inEvent);
                this.log("ueEquipmentState :equipFlex01(getEqsFlexString01) Value1---:");
                this.log(equipFlex01)
                if (equipFlex01 != null) {
                    ueEquipmentState = unitEquipment.getUeEquipmentState();
                    def newEquipFlex01 = ueEquipmentState != null ? ueEquipmentState.getEqsFlexString01() : '';
                    this.log("ueEquipmentState :newEquipFlex01(getEqsFlexString01) Value-11--:");
                    this.log(newEquipFlex01);
                    if (newEquipFlex01 != null && "MAT".equalsIgnoreCase(newEquipFlex01) && !"MAT".equalsIgnoreCase(equipFlex01)) {
                        isAlwaysSendIGT = true;
                        this.log("isAlwaysSendIGT true")
                    }
                }

            }
        } catch (Exception e) {
            this.log("exception thrown : " + e.getMessage());
        }
        if (!doer.contains('FRUCTOSE_MTY')) {
            this.log(" ! FRUCTOSE_MTY")
            def gvyPropUpdtObj = api.getGroovyClassInstance("GvyCmisEventUnitPropertyUpdate")
            gvyPropUpdtObj.processUnitPropertyUpdate(inEvent, api, isAlwaysSendIGT)
        }
    }
}