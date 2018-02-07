/*

This Groovy script is written for: NFRM-305 & ARGO-16680

2.3 Routing Point validation when container is discharged (NO_204)

From http://confluence.navis.com/display/MAHER/NFRM-305+Gap+13.1+EDI+204+booking+logic+check:

Main Success Scenario:

a) UNIT_DERAMP event is posted in N4 when the container is discharged.
b) A groovy code defined through the general reports menu is executed on that event.
c) The groovy code will check if the line operator has provided the information about
where the container is going to be loaded on the vessel. If the loadPort information
is not provided through an EDI 204 or updated through other user interface,
then an error email is generated and a billing event (NO_204) is posted to the
chargeable unit event table.


02/04/2009 fyazar & smahesh

*/

/*
This method will be called from a notice pre-defined in the General Notices screen
To do that:

First update this code, there are constants defined in the code, update the values according to the need:
- expectedDerampFacilityId
- expectedUnitCategory

A. Define Groovy Plug-In:
1. Go to Administration --> System --> Groovy Plug-ins
2. Clicl on Add (+)
3. Enter the values as below:
    Short Description:  Routing Point Validator
    Groovy Code:        (The code here)
4. Click on Save

B. Add Notice to call the plug-in
1. Go to Operations --> Reports --> General Notices
2. Click on Add (+)
3. Enter the values as below:
    Business entity:    Unit
    Description:        Validates routing point when container is discharged
    Event Type:         UNIT_DERAMP
    Action:             Execute Code
    (Code):             def rpValidator = api.getGroovyClassInstance("MaherGvyRoutingPointValidation")
                        rpValidator.validateRoutingPoint(event, api)
4. Click on Save

And the plug in defined in A calls NO_204 event as well: therefore the event should be defined previously (as billable)
C. Add NO_204 Event
1. Go to Configuration --> Services --> Event Types
2. Click on Add (+)
3. Enter the values as below:
    Id:                 NO_204
    Description:        Routing Point Validation when container is discharged from a Train
    Applies to:         Unit
    Is Built in:        NO
    Is Service:         YES (???)
    Billable:           YES
    Notifiable:         YES
    Can be Bulk Applied: NO
4. Click on Save

then we need to define another notice: to send an email to the line operator (or any email address we wanted)
D. Add Notice to send email to the line operator
1. Go to Operations --> Reports --> General Notices
2. Click on Add (+)
3. Enter the values as below:
    Business entity:    Unit
    Description:        Sends Email when a NO_204 event is triggered
    Event Type:         NO_204 (the event defined in C)
    Action:             Send an Email
    Email Party:        Line Operator (or any other wanted)
    Email Address:      (if an email party is not entered: a constant email address can be entered here)
    (Message Template):         Extra messages (can include metafields: refer to help)

    A sample Message template as follows (between the lines ====)

====
The unit &UnitNbr was deramped at facility &FACILITY without Port of Load information.
A NO_204 event was posted.

The report for the event:

Entity: &ENTITY
Type: &EquipmentType
Category: &Category
Event: &EVENT
Time: &TIME
Facility: &FACILITY
====

4. Click on Save


get the unit

*/


public class MaherGvyRoutingPointValidation {

    def postEvent = "NO_204";

    // following values should be updated according to the facility and unit category
    def expectedDerampFacilityId = "FCY111";
    def expectedUnitCategory = "EXPRT";

    public void validateRoutingPoint(Object event, Object api)
    {

        def unit = event.entity;
        def derampEvent = event.event; // first event: Groovy Event, second event : N4 Event UNIT_DERAMP

        println "Routing point validation for: " + unit.unitId;

        // check whether the unit has a line operator
        if (unit.unitLineOperator == null) {
            println "The unit does not have a line operator assigned, can not continue!"; // can not be billable
            return;
        }
        println "Line Operator: " + unit?.unitLineOperator?.bzuId;

        def derampFacilityId = derampEvent.getEventFacilityId();
        println "Unit DeRamp Facility Id: " + derampFacilityId;

        def unitCategory = unit.getUnitCategory()?.getKey();
        println "Unit Category: " + unitCategory;

        // if deramp facility id or category does not match expected values: do not continue
        if (derampFacilityId != expectedDerampFacilityId) {
          println "Facility does not match: " + derampFacilityId + " vs " + expectedDerampFacilityId + ". Not Posting event: " + postEvent;
          return;
        }

        if (unitCategory != expectedUnitCategory) {
          println "Unit Category does not match: " + unitCategory + " vs " + expectedUnitCategory + ". Not Posting event: " + postEvent;
          return;
        }


        // get the routing point
        def unitRouting = unit.getUnitRouting();

        // now check for the POL: if it is not defined before: call NO_204 event
        if ((unitRouting == null) || (unitRouting.rtgPOL == null)) {
            println "Unit Routing do not exist or invalid, posting: " + postEvent;
            event.postNewEvent(postEvent);
            println "After posting event: " + postEvent;
            // the email will be sent by another notice based on NO_204 event
        } else {
            println "Unit Routing and port of load exist: " + unitRouting.rtgPOL;
        }
    }

}