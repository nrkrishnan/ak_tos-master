import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.util.internationalization.PropertyKeyFactory
import com.navis.framework.util.message.MessageLevel
import com.navis.orders.business.eqorders.Booking
import com.navis.orders.business.eqorders.EquipmentOrder
import com.navis.road.RoadPropertyKeys
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import com.navis.road.util.StringUtil

public class MATGateValidateCommodity extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    public void execute(TransactionAndVisitHolder dao) {
        TruckTransaction tran = dao.tran;

        if (tran != null) {
            String tranCommodity = tran.getTranUnitFlexString05();
            String bookingCmdy = "";
            Boolean cmdyMatches = Boolean.FALSE;
            Boolean cmdyEmpty = false;
            if (tranCommodity != null && StringUtil.isNotBlank(tranCommodity)) {
                EquipmentOrder tranEqo = tran.getTranEqo();
                if (tranEqo != null) {
                    Booking booking = Booking.resolveBkgFromEqo(tranEqo);
                    if (booking != null) {
                        bookingCmdy = booking.getEqoStuffingLocation();
                        if (bookingCmdy != null && StringUtil.isNotBlank(bookingCmdy)) {
                            if (tranCommodity.length() < bookingCmdy.length()) {
                                cmdyMatches = validateCmdy(bookingCmdy, tranCommodity);
                            } else {
                                cmdyMatches =validateCmdy(tranCommodity, bookingCmdy);
                            }
                        } else {
                            cmdyEmpty = Boolean.TRUE;
                        }
                    }
                }
            } else {
                cmdyEmpty = Boolean.TRUE;
            }
            if (cmdyEmpty) {
                RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.WARNING, PropertyKeyFactory.valueOf("gate.commodity_unknown"), null, bookingCmdy);
            } else if (!cmdyMatches) {
                Object[] params = new Object[2];
                params[0] = tranCommodity;
                params[1] = bookingCmdy;
                RoadBizUtil.getMessageCollector().appendMessage(MessageLevel.WARNING, PropertyKeyFactory.valueOf("gate.not_matching_booking_commodity"), null, params);
            }
        }
    }

    private boolean validateCmdy(String cmdy1, String cmdy2) {
        boolean matches = false;
        if (cmdy1.contains(cmdy2)) {
            matches = true;
        }
        return matches;
    }
}