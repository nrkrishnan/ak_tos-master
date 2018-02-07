import com.navis.services.business.event.GroovyEvent
import com.navis.services.business.event.Event

import com.navis.argo.business.reference.RoutingPoint;
import com.navis.framework.persistence.HibernateApi;
/*

This Groovy script is written for: NFRM-305 & ARGO-16682

2.5 Routing point changed from customers marine terminal to another terminal.

From http://confluence.navis.com/display/MAHER/NFRM-305+Gap+13.1+EDI+204+booking+logic+check;

Main Success Scenario:

a) An event is posted in N4 when the container loadPort is updated.
b) A groovy code defined through the general reports menu is executed on that event.
c) The groovy code will remove the billing event (INVALID_204) is posted to the chargeable unit event table.


02/05/2009 fyazar & smahesh

*/

/*
This method will be called from a notice pre-defined in the General Notices screen
To do that:
  A. Define Groovy Plug-In:
    1. Go to Administration --> System --> Groovy Plug-ins
    2. Clicl on Add (+)
    3. Enter the values as below:
        Short Description:  Routing Point Changed (POL) from Marine terminal to another terminal
        Groovy Code:        (The code here)
    4. Click on Save

  B. update UNIT_PROPERTY_UPDATE
    To be able to detect changes in POL: we need to catch UNIT_PROPERTY_UPDATE event
    From the Configuration --> Services --> Event Types
    locate UNIT_PROPERTY_UPDATE: and update Notifiable property to checked

  C. Add Notice to call the plug-in
    1. Go to Operations --> Reports --> General Notices
    2. Click on Add (+)
    3. Enter the values as below:
        Business entity:    Unit
        Description:        Routing Point Changed (POL) from Marine terminal to another terminal
        Event Type:         UNIT_PROPERTY_UPDATE
        Action:             Execute Code
        (Code):             def rpValidator = api.getGroovyClassInstance("MaherGvyRoutingPointPOLChanged")
                            rpValidator.checkRoutingPointPOLChange(event, api)
    4. Click on Save

And the plug in defined in A tries to cancel INVALID_204 event as well:
therefore that event should be defined previously (by another use case from NFRM-305

*/


public class MaherGvyRoutingPointPOLChanged {

    def cancelEvent = "INVALID_204";

    // to be changed value: customer will change this value according to their Routing point code
    def currentPOL = "NWK";

    // only Export containers will be processed
    def expectedUnitCategory = "EXPRT";

    // empty containers will not be processed
    def notExpectedFreightKind = "MTY";

    public void checkRoutingPointPOLChange(Object event, Object api)
    {
        // get the unit
        def unit = event.entity;

        println "Routing point changed for: " + unit.unitId;

        // check whether the unit has a line operator
        if (unit.unitLineOperator == null) {
            println "The unit does not have a line operator assigned, can not continue!"; // can not be billable
            return;
        }

        def unitCategory = unit.getUnitCategory()?.getKey();
        println "Unit Category: " + unitCategory;

        // if unit category is not Export, then we wil lnot do anything
        if (unitCategory != expectedUnitCategory) {
          println "Unit Category does not match: " + unitCategory + " vs " + expectedUnitCategory + ". will not continue cancelling: " + cancelEvent;
          return;
        }

        def unitFreightKind = unit.getUnitFreightKind()?.getKey();
        println "Unit Freight Kind: " + unitFreightKind;

        // if unit category is not Export, then we wil lnot do anything
        if (unitFreightKind == notExpectedFreightKind) {
          println "Unit Freight kind matches not expected: " + unitFreightKind + " vs " + notExpectedFreightKind + ". will not continue cancelling: " + cancelEvent;
          return;
        }



        println "Line Operator: " + unit?.unitLineOperator?.bzuId;

        // From Context: 2.4
        // The container must be an export container and the containerStatus must be FCL or LCL
        // But if a container contains INVALID_204 then it means it already passed these checks: then we do not need to check these
        // only we are going to check whether POL changed and INVALID_204 posted earlier: if yes then we are going to cancel INVALID_204
        // otherwise we are not going to do anything!!!
        // and from 2.5 Minimal Success Guarantee
        // When the container routing point is updated from customers marine terminal to another terminal,
        // the billing event posted earlier for the container have to be removed.
        // only we need to check this


        // get the routing
        def unitRouting = unit.getUnitRouting();

        // now check for the POL: if it is changed, then look for INVALID_204: if exists delete it
        GroovyEvent propertyUpdateEvent = event.getMostRecentEvent("UNIT_PROPERTY_UPDATE");
        if (propertyUpdateEvent.wasFieldChanged("POL")) {
            def newPOL = null;
            if (unitRouting != null) {
              newPOL = unitRouting.rtgPOL;
            }

            // previous POL should match the current POL
            def previousPOLGKey = propertyUpdateEvent.getPreviousPropertyAsString("POL");
            println("Previous POL gKey was: " + previousPOLGKey);

            if (previousPOLGKey == null || "".equals(previousPOLGKey)) {
                println("Previous POL was Not : " + currentPOL + ". Will not continue!");
                return;
            }

            def previousRP = (RoutingPoint) HibernateApi.getInstance().load(RoutingPoint.class, previousPOLGKey);

            def previousRPCode = previousRP.getPointId();

            println("Previous Routing Point was: " + previousRPCode);

            if (!currentPOL.equals(previousRPCode)) {
              println("Event is not happening in the current (expected) POL, will not Continue!!!");
              return;
            }

//            println "newPOL (RoutingPoint) Properties :"
//            newPOL.properties.each{println it}

            println "Unit Routing POL was changed (RoutingPoint): " + newPOL;

            def newPOLId = newPOL.pointId;

            println "Unit Routing POL was changed (PointId): " + newPOLId;

            if (currentPOL != newPOLId) {
            // code removes all matching INVALID_204 events
              def removeEvents = event.getAllEvents(cancelEvent);

              if (removeEvents != null ) {
                removeEvents.each {
                  def removeGyvEvent = it;  // assigned each iteration, it : current iteration
                  boolean cancelled = false;
                  if (removeGyvEvent != null) {
                    def removeEvent = removeGyvEvent.getEvent();
                    if (removeEvent != null) {
                      def eventGKey = removeEvent.getEventGKey();
                      println cancelEvent + " was found with GKey: " + eventGKey;

                      if (eventGKey != null) {
                        cancelled = event.cancelEvent(eventGKey);
                      }
                    } else {
                      println cancelEvent + " Event could not be retrieved!";
                    }
                  } else {
                    println cancelEvent + " was NOT found!";
                  }
                }
              } else {
                println "No matching " + cancelEvent + " was found in the event history!";
              }
            } else {
              println "Unit Routing : port of load was not changed from current Terminal to Another One: " + unitRouting.rtgPOL;
            }
        } else {
            println "Unit Routing : port of load was not changed: " + unitRouting.rtgPOL;
        }
    }

}