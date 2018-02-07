import com.navis.argo.business.atoms.BizRoleEnum
import com.navis.argo.business.reference.ScopedBizUnit
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.util.message.MessageLevel
import com.navis.orders.business.eqorders.Booking
import com.navis.road.RoadPropertyKeys
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import com.navis.framework.util.BizViolation
import com.navis.framework.AllOtherFrameworkPropertyKeys
import org.apache.log4j.Logger

/**
 * Created by psethuraman on 10/12/2017.
 */



public class MATGvyValidateBookingRM extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {

    private static Logger LOGGER = Logger.getLogger(MATGvyValidateBookingRM.class);
    public void execute(TransactionAndVisitHolder inDao) {

        LOGGER.warn(" MATGvyValidateBookingRM Execution Started ");

        //executeInternal(inDao);
        if (inDao == null) {
            LOGGER.warn("Transaction visit holder is null");
            return;
        }
        TruckTransaction tran = inDao.getTran();
        if (tran == null) {
            LOGGER.warn("Transaction is null");
            return;
        }
        String bookingNbr = tran.getTranEqoNbr();
        LOGGER.warn(" Tran Booking : " + bookingNbr);

        String line = tran.getTranLineId();
        LOGGER.warn("Tran Line : "+line);
        if (bookingNbr != null) {
            Booking booking = null;
            if (line != null) {
                booking = Booking.findBookingWithoutVesselVisit(bookingNbr, ScopedBizUnit.findScopedBizUnit(line, BizRoleEnum.LINEOP));
                Object object = [bookingNbr, ];
                if (booking == null) {
                    
                       RoadBizUtil.appendExceptionChain(BizViolation.create(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE, (BizViolation) null, "Booking is Invalid"));
  		
                }
            } else {
                List<Booking> bookings = Booking.findBookingsByNbr(bookingNbr);
                if (bookings != null && bookings.size() > 0) {
                    booking = bookings.get(0);
                }
                if (booking == null) {
                    RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.SEVERE, RoadPropertyKeys.GATE__ORDER_NOT_FOUND, (String)null, bookingNbr);
                }
            }

        }
    }
}