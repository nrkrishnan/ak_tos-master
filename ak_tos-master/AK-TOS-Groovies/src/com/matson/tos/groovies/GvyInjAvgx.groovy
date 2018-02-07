package com.matson.tos.groovies

import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Facility
import com.navis.argo.business.model.GeneralReference
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChanges
import com.navis.inventory.InventoryBizMetafield
import com.navis.inventory.InventoryField
import com.navis.inventory.business.api.UnitManager
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.orders.business.eqorders.Booking
import com.navis.services.business.rules.EventType

class GvyInjAvgx extends GroovyInjectionBase {
    public String execute(Map inParameters) {

        String equipmentNumber = (String) inParameters.get("equipmentNumber");
        String bookingNumber = (String) inParameters.get("bookingNumber");
        String grossWeight = (String) inParameters.get("grossWeight");
        String verifierId = (String) inParameters.get("verifierId");
        String threadUser = "ACETS";
        bookingNumber = bookingNumber != null && !bookingNumber.isEmpty() ? bookingNumber.trim() : bookingNumber;
        equipmentNumber = equipmentNumber != null && !equipmentNumber.isEmpty() ? equipmentNumber.trim() : equipmentNumber;
        if (verifierId != null && !verifierId.isEmpty()) {
            threadUser = verifierId.trim();
        }
        com.navis.argo.ContextHelper.setThreadExternalUser(threadUser); // ACETS or ???

        logInfo("GvyInjAvgx start for unitId: " + equipmentNumber + " for booking: " + bookingNumber);

        // find booking
        Facility facility = getFacility();


        List<Booking> bookingList = Booking.findBookingsByNbr(bookingNumber);
        Booking booking = !bookingList.isEmpty() ? bookingList.get(0) : null;
        if (booking == null) {
            sendFailureMail(equipmentNumber, "Could not find booking: " + bookingNumber);
            fail("ERR_GVY_VGX_001. Could not find booking: " + bookingNumber);
        }
        //todo change the API to handle booking with carrier and all details

        CarrierVisit cv = booking.getEqoVesselVisit();
        if (cv == null) {
            sendFailureMail(equipmentNumber, "Could not find Carrier Visit")
            fail("ERR_GVY_VGX_002. Could not find the carrier visit: ");
        }

        // find unit in facility
        UnitManager unitMgr = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
        UnitFacilityVisit ufv;

        Unit unit;
        try {
            ufv = unitMgr.findActiveUfvForUnitDigits(equipmentNumber);
            if (ufv != null)
                unit = ufv.getUfvUnit();

        } catch (Exception ex) {
            ex.printStackTrace()
            sendFailureMail(equipmentNumber, " Could not find unit:" + ex.message + "\n" + ex.toString());
            fail((new StringBuilder()).append(ex.toString()).append(" ERR_GVY_VGX_004. Could not find unit: ").append(ctrId).toString());
        }

        String obDecVesvoy = ufv.getUfvIntendedObCv().getCvId();
        logInfo("obDecVesvoy=" + obDecVesvoy);
        if (!obDecVesvoy.equals(cv.getCvId())) {
            //skip with sending mail
            sendFailureMail(equipmentNumber, "ERR_GVY_VGX_005. Could not find unit with ob vesvoy :" + cv.getCvId() + " but an unit with ob vesvoy " + obDecVesvoy + " found");
            fail("ERR_GVY_VGX_005. Could not find unit with ob vesvoy :" + cv.getCvId() + " but an unit with ob vesvoy " + obDecVesvoy + " found");
        }




        String unitBookingNumber = unit.getUnitPrimaryUe().getUeDepartureOrderItem().getEqboiOrder().getEqboNbr();
        logInfo("unitBookingNumber=" + unitBookingNumber);
        if (!unitBookingNumber.equals(bookingNumber)) {
            //skip after sending mail
            sendFailureMail(equipmentNumber, "ERR_GVY_VGX_006. Could not find unit with booking number :" + bookingNumber + " but an unit with booking number " + unitBookingNumber + " found");
            fail("ERR_GVY_VGX_006. Could not find unit with booking number :" + bookingNumber + " but an unit with booking number " + unitBookingNumber + " found");
        }

        unit.setUnitGoodsAndCtrWtKg(Double.parseDouble(grossWeight));


        EventType eventType = EventType.findEventType("VGM RELEASE");
        FieldChanges fieldChanges = new FieldChanges();
        fieldChanges.setFieldChange(InventoryField.UNIT_GOODS_AND_CTR_WT_KG, Double.parseDouble(grossWeight));
        fieldChanges.setFieldChange(InventoryBizMetafield.UNIT_GOODS_AND_CTR_WT_KG_LONG, Double.parseDouble(grossWeight).longValue());
        fieldChanges.setFieldChange(InventoryField.UNIT_GOODS_AND_CTR_WT_KG_VERFIED_GROSS, Double.parseDouble(grossWeight));
        fieldChanges.setFieldChange(InventoryField.UNIT_FLEX_STRING06, "YES");
        fieldChanges.setFieldChange(InventoryField.UNIT_VGM_VERIFIED_DATE, ArgoUtils.timeNow());
        fieldChanges.setFieldChange(InventoryField.UNIT_VGM_ENTITY, verifierId);
        unit.recordUnitEvent(eventType, fieldChanges, "VGM Value by AVGX Groovy");//servicemanager.recordservice event
        //ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
        //servicesManager.recordEvent(eventType, ("VGM updated by " + verifierId), null, null, unit, fieldChanges, ArgoUtils.timeNow());
        HibernateApi.getInstance().flush();
        /**
         * The service event will not be able to refresh the impediments re-calculation on it's own (i.e., vessel stop)
         * it needs to be triggered by calling general notices with
         *
         * api.getGroovyClassInstance("MATUtil").refreshUnit(event.getEntity()); on the above registered event
         *
         * or by calling the code below
         *
         * ImpedimentsBean impedimentsBean = unit.calculateImpediments(Boolean.TRUE);
         * if (impedimentsBean != null) {* Date dateNow = new Date(ArgoUtils.timeNowMillis() + 1);
         * unit.updateImpediments(impedimentsBean, dateNow);
         * HibernateApi.getInstance().saveOrUpdate(inUnit);
         *}*
         */

        logInfo("End GvyInjAvgx post event: " + equipmentNumber + " for booking: " + bookingNumber);
        return "SUCCESS";
    }

    public void sendFailureMail(String inEquipmentId, String inErrorMessage) {
        GeneralReference genRef = GeneralReference.findUniqueEntryById("ENV", "ENVIRONMENT");
        String environment = genRef.getRefValue1();
        genRef = GeneralReference.findUniqueEntryById("MATSON", "EMAIL", "NOTIFICATION");
        String emailFrom = "1aktosdevteam@matson.com";
        String emailTo = "1aktosdevteam@matson.com";
        String emailSubject = environment + " - VGM Failure " + inEquipmentId;
        String emailBody = inErrorMessage;
        sendEmail(emailTo, emailFrom, emailSubject, emailBody);
    }

}