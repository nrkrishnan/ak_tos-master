/*
* Copyright (c) 2015 Navis LLC. All Rights Reserved.
*
*/


import com.navis.argo.business.api.GroovyApi
import com.navis.inventory.business.units.EqBaseOrder
import com.navis.inventory.business.units.Unit
import com.navis.orders.business.eqorders.Booking
import com.navis.orders.business.eqorders.EquipmentOrder
import com.navis.services.business.event.Event
import com.navis.services.business.event.GroovyEvent

/**
 * This groovy applies the booking stow block to the unit's priority stow (UnitFlexSting08) when the unit is rolled to a new booking
 * This is triggered with a UNIT_ROLL event
 *
 * Author: Peter Seiler
 * Date: 08/05/15
 * JIRA: ARGO-76865
 * SFDC: 142550
 *
 */

public class MATSyncUnitFlexWithBookingGvy extends GroovyApi

{
    public void execute(GroovyEvent inEvent, Object inApi)

    {
        this.log("Execution Started MATSyncUnitFlexWithBookingGvy");

        /* get the event */

        Event ThisEvent = inEvent.getEvent();

        if (ThisEvent == null)
            return;

        /* Get the unit and the Booking */

        Unit ThisUnit = (Unit) inEvent.getEntity();

        this.log("Unit is " + ThisUnit)

        EqBaseOrder ThisBaseOrder = ThisUnit.getDepartureOrder();

        this.log("Depart order " + ThisBaseOrder)

        EquipmentOrder ThisEqOrd = EquipmentOrder.resolveEqoFromEqbo(ThisBaseOrder);

        this.log("Equipmnet Order " + ThisEqOrd)

        Booking ThisBooking = Booking.resolveBkgFromEqo(ThisEqOrd);

        this.log("Booking is " + ThisBooking)

        /* set the unit's priority stow value */
        if (ThisBooking != null)
            ThisUnit.setUnitFlexString08(ThisBooking.getEqoStowBlock());
        else {
            this.log("The Booking value is null");
        }
    }
}