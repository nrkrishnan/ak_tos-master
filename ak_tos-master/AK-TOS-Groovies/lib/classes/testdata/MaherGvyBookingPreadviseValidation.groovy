

import com.navis.framework.util.BizViolation;
import com.navis.framework.email.EmailMessage;
import com.navis.framework.email.EmailNotification;
import com.navis.framework.esb.client.IESBClient;
import com.navis.framework.esb.server.FrameworkMessageQueues;
import com.navis.framework.portal.UserContext;
import com.navis.framework.business.Roastery;
import com.navis.argo.ContextHelper;
import com.navis.edi.EdiConfig;
import org.apache.commons.lang.StringUtils;
import com.navis.inventory.business.units.Unit;
import com.navis.framework.esb.listeners.callback.EventCallback;
import com.navis.framework.esb.listeners.email.EmailListener;
import com.navis.framework.util.message.MessageCollector;
import com.navis.framework.util.TransactionParms
import com.navis.orders.business.eqorders.Booking;
import com.navis.orders.OrdersPropertyKeys;
import com.navis.argo.business.reference.ScopedBizUnit;
import com.navis.argo.business.model.CarrierVisit;
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum;
import com.navis.vessel.VesselPropertyKeys;
import com.navis.orders.business.eqorders.EquipmentOrderItem;
import com.navis.inventory.business.api.UnitFinder;
import com.navis.orders.business.api.OrdersFinder;
import com.navis.argo.business.reference.EquipType;
import com.navis.argo.business.atoms.UnitCategoryEnum;
import com.navis.argo.business.reference.RoutingPoint;
import com.navis.inventory.business.atoms.UfvTransitStateEnum;
import com.navis.services.business.event.GroovyEvent;
import com.navis.services.business.event.Event;

/*

This Groovy code is written for: NFRM-305 & ARGO-16679

"2.2 Booking validation when container is discharged (NO_204)" is done in this groovy script

Specification: From http://confluence.navis.com/display/MAHER/NFRM-305+Gap+13.1+EDI+204+booking+logic+check:

Main Success Scenario:

a) UNIT_DERAMP event is posted in N4 when the container is discharged.
b) A groovy code defined through the general reports menu is executed on that event.
c) The groovy code will perform the booking validations. In case of any errors an
   error email is generated and a billing event (INVALID_204) is posted to the chargeable unit event table.

author <mailto:kjeyapandian@zebra.com"> Kathiresan Jeyapandian </a>, Feb 16, 2009

*/

/*

This method will be called from a notice pre-defined in the General Notices screen

Steps:

    Configure the email setting (Navigation  :   Administration --> Settings --> Settings)

            Id                          Raw Id             Value
        1.  EDIEMAIL001                 EMAIL_FROM         Ex:  kjeyapandian@zebra.com
        2.  EDIEMAIL002                 EMAIL_REPLY_TO     Ex:  kjeyapandian@zebra.com
        3.  MSEMAIL_SERVER_CONFIG003    PROTOCOL           Ex:  smtp
        4.  MSEMAIL_SERVER_CONFIG001    HOST               Ex:  smtp.navis.com

    Update the constants defined in the code:

        - EmailMessage setTo, currently it has been set as kjeyapandian@zebra.com
        - EmailMessage subject and text, currently it has been set as "Invalid_204" & "Invalid_204 with error message"
        - EMAIL_RECEIVED_TIMEOUT, currently it has been set to 12 seconds (change if needed)
        - Currently the routing point of customer marine terminal is assumed as "NWK" for validation. The user needs to change it appropriately

    Update the unit flex field id in the code, currently it is assumed as booking number is present in unitFlexFieldString1

    Define Groovy Plug-In:

        1. Go to Administration --> System --> Groovy Plug-ins
        2. Clicl on Add (+)
        3. Enter the values as below:
            Short Description:  Booking Preadvise Validator
            Groovy Code:        (The code here)
        4. Click on Save

    Add Notice to call the plug-in

        1. Go to Operations --> Reports --> General Notices
        2. Click on Add (+)
        3. Enter the values as below:
            Business entity:    Unit
            Description:        Validates booking preadvise informations when container is discharged
            Event Type:         UNIT_DERAMP
            Action:             Execute Code
            (Code):             def rpValidator = api.getGroovyClassInstance("MaherGvyBookingPreadviseValidation")
                                rpValidator.execute(event, api)
        4. Click on Save

    Add INVALID_204 Event

        1. Go to Configuration --> Services --> Event Types
        2. Click on Add (+)
        3. Enter the values as below:
            Id:                 INVALID_204
            Description:        Booking Preadvise Validation when container is discharged from a Train
            Applies to:         Unit
            Is Built in:        NO
            Is Service:         YES (???)
            Billable:           YES
            Notifiable:         YES
            Can be Bulk Applied: NO
        4. Click on Save

*/

public class MaherGvyBookingPreadviseValidation {

    def postEvent = "INVALID_204";
    def static boolean EMAIL_RECEIVED;
    def static final long EMAIL_RECEIVED_TIMEOUT = 12000;

    def Booking book = null;
    def Unit unit = null;

    public void execute(Object event, Object api)
    {

        try {

            unit = event.getEntity();

            GroovyEvent propertyUpdateEvent = event.getMostRecentEvent("UNIT_PROPERTY_UPDATE");
            if (!propertyUpdateEvent.wasFieldChanged("POL")) {
                println("Unable to proceed with the validations, since pol is not changed...");
                return;
            }

            RoutingPoint pol = unit.getUnitRouting().getRtgPOL();
            RoutingPoint newarkRoutingPoint = RoutingPoint.findRoutingPoint("NWK");
            
            // Booking validations will be not be performed if the load port or routing point "NWK" doesn't exist
            if(pol == null || newarkRoutingPoint == null) {
                println("Unable to proceed with the validations, either pol or newark routing point is null ");
                return;
            }

            // Booking validations will be not be performed if unit's load port is not the customers marine terminal i.e ("NWK").
            // Currently the unit's load port is assumed as "NWK"
            if(!pol.equals(newarkRoutingPoint)) {
                println("Unable to proceed with the validations, since pol is not newark");
                return;
            }

            // Do not proceed with the validations if the container category is other than "EXPORT"
            if (!UnitCategoryEnum.EXPORT.equals(unit.getUnitCategory())) {
                println("Unable to proceed with the validations, since the unit category is not export ");
                return;
            }

            println "Booking Preadvise validation for unit : " + unit.unitId;

            // Assume that booking number is stored in unitFlexFieldString1
            def bkgNumber = unit.unitFlexString01;

            // Check whether booking number is empty
            if (isEmpty(bkgNumber)) {
                throw BizViolation.create(OrdersPropertyKeys.ERROR_BOOKING_NBR_IS_REQUIRED, null);
            }

            // Throw error if booking doesn't exist for the given number
            List bookList = Booking.findBookingsByNbr(bkgNumber);
            if (bookList.isEmpty()) {
                throw BizViolation.create(OrdersPropertyKeys.ERROR_BOOKING_DOES_NOT_EXIST, null, bkgNumber);
            }

            ScopedBizUnit unitLineOp = unit.getUnitLineOperator();


            if (bookList.size() == 1) {
                book = (Booking) bookList.get(0);
            } else {
                // If more than one booking found for the same booking number, narrow down the search using unit line operator.
                // If still returns multiple result, narrow down the search using unit's vessel visit
                try {
                    book = Booking.findBookingByUniquenessCriteria(bkgNumber, unitLineOp, null);
                } catch (BizViolation inBv) {
                    book = Booking.findBookingByUniquenessCriteria(bkgNumber, unitLineOp, unit.getUnitRouting().getRtgDeclaredCv());
                }
            }

            // Throw error if booking doesn't exist for the given number
            if (book == null) {
                throw BizViolation.create(OrdersPropertyKeys.ERROR_BOOKING_DOES_NOT_EXIST, null, bkgNumber);
            }

            ScopedBizUnit bookLineOp = book.getEqoLine();
            ScopedBizUnit unitLine = unit.unitLineOperator;

            // Throw error if the unit line operator is different from the specified booking line operator
            if (bookLineOp != null && unitLine != null) {
                if (!bookLineOp.equals(unitLine)) {
                    throw BizViolation.create(OrdersPropertyKeys.ERRKEY__ORDER_LINE_AND_EQUIPMENT_OPERATOR_DOESNOT_MATCH_FOR_PREADVISE,
                            null, unitLine.getBzuId(), book.getEqoLine().getBzuId());

                }
            }

            // Throw error if the booking vessel visit is in departed phase
            CarrierVisit cv = book.getEqoVesselVisit();
            if (cv != null) {
                CarrierVisitPhaseEnum phase = cv.getCvVisitPhase();
                if (CarrierVisitPhaseEnum.DEPARTED.equals(phase)) {
                    throw BizViolation.create(VesselPropertyKeys.VESSEL_VISIT_BAD_PHASE,
                            null, CarrierVisitPhaseEnum.DEPARTED);
                }
            }

            Boolean unitIsHazard = unit.getUnitIsHazard();

            if (Boolean.TRUE.equals(unitIsHazard)) {
                // Throw error if the unit has hazards where as the booking does't not
                if (book.getEqoHazards() == null || book.getEqoHazards().getHzrdItems() == null || book.getEqoHazards().getHzrdItems().isEmpty()) {
                    throw BizViolation.create(OrdersPropertyKeys.ERRKEY__EXPORT_BOOKING_NOT_HAZARDOUS, null, bkgNumber);
                }
            } else if (book.getEqoHazards() != null) {
                Collection units = getUnitFndr().findUnitsAdvisedOrReceivedForOrder(book);
                // Throw error if the unit doesn't have hazards and booking has hazards but no containers received against it
                if (units == null || units.isEmpty()) {
                    throw BizViolation.create(OrdersPropertyKeys.ERRKEY__HAZARDOUS_CONTAINERS_NOT_RECEIVED_FOR_EXPORT_BOOKING, null, bkgNumber);
                } else{
                    boolean found = false;
                    for(Object unitObj : units) {
                        Unit advisedOrReceivedUnit = (Unit) unitObj;
                        if(UfvTransitStateEnum.S40_YARD.equals(advisedOrReceivedUnit) && advisedOrReceivedUnit.getUnitIsHazard()) {
                            found = true;
                        }
                    }
                    if(!found) {
                      throw BizViolation.create(OrdersPropertyKeys.ERRKEY__HAZARDOUS_CONTAINERS_NOT_RECEIVED_FOR_EXPORT_BOOKING, null, bkgNumber);
                    }
                }
            }

            EquipType eqType = unit.getUnitPrimaryUe().getUeEquipment().getEqEquipType();

            // Throw error if tally limit is reached or the unit's equip type doesn't match any one of the order item's equip type in the booking.
            EquipmentOrderItem eqoi = book.findMatchingItemRcv(eqType, false, false);

        } catch (Exception inEx) {
            // Register the exceptions
            MessageCollector mg = TransactionParms.getBoundParms().getMessageCollector();
            mg.registerExceptions(inEx);
            try {

                // Create call back for verification
                println("Exception occured in MaherGvyBookingPreadviseValidation.groovy" + inEx);
                IESBClient esbClient = (IESBClient) Roastery.getBean(IESBClient.BEAN_ID)
                EventCallback callback = ({Object inEvent ->
                    println("Email received by the listener.");
                    EMAIL_RECEIVED = true;
                } as EventCallback);

                // Create Email listener and Email Message for sending email
                EmailListener listener = (EmailListener) esbClient.getManagedBean("emailListener");
                listener.setEventCallback(callback);
                UserContext uc = ContextHelper.getThreadUserContext()
                EmailMessage msg = new EmailMessage(uc);
                String fromAddress = EdiConfig.EMAIL_FROM.getSetting(uc);
                String replyTo = EdiConfig.EMAIL_REPLY_TO.getSetting(uc);
                msg.setFrom(fromAddress);
                msg.setReplyTo(replyTo);
                msg.setTo("kjeyapandian@zebra.com");
                StringBuilder sb = new StringBuilder();
                sb.append("INVALID_204 generated for Container: ");
                sb.append(unit.unitId);
                sb.append(" - Millennium Marine Rail - EDI BOOKING: ");
                sb.append(unit.unitFlexString01);                
                msg.setSubject(sb.toString());
                msg.setText("Invalid 204 Message. The following exception occured during validation : " + inEx);
                println("EDI-EMAIL From:" + StringUtils.join(msg.getFrom()));
                println("EDI-EMAIL To:" + StringUtils.join(msg.getTo()));
                println("EDI-EMAIL Reply To:" + StringUtils.join(msg.getReplyTo()));
                println("EDI-EMAIL Subject:" + msg.getSubject());

                // Dispatch the notification email
                esbClient.dispatchNotification(FrameworkMessageQueues.EMAIL_QUEUE, new EmailNotification(msg));

                // set a timeout to ensure we don't get in an endless loop
                final long startTime = System.currentTimeMillis();
                while (!(EMAIL_RECEIVED || (System.currentTimeMillis() - startTime > EMAIL_RECEIVED_TIMEOUT))) {
                    try {
                        Thread.sleep(1000);
                    } catch (Throwable throwable) {
                        println("Unexpected Error occured while waiting for email callback:" + throwable);
                    }
                }
                println("Email was not received by the listener:" + EMAIL_RECEIVED);

            } catch (Exception e) {
                println("Unexpected exception occured while sending email..." + e);
            } finally {
              // Post an INVALID_204 event
              println "An email has been sent due to validation failures. Before Posting event : " + postEvent;
              event.postNewEvent(postEvent);
              println "After posting event: " + postEvent;
            }
        }
    }

    private boolean isEmpty(String inStr) {
        return inStr == null || StringUtils.isEmpty(inStr.trim());
    }

    private UnitFinder getUnitFndr() {
        Object bean = Roastery.getBean(UnitFinder.BEAN_ID);
        return (UnitFinder) bean;
    }

    private OrdersFinder getOrdersFndr() {
        Object bean = Roastery.getBean(OrdersFinder.BEAN_ID);
        return (OrdersFinder) bean;
    }

}