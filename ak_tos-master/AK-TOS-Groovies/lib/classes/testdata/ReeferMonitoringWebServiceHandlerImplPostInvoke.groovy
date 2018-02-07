import java.util.Map;

import com.navis.apex.business.model.GroovyInjectionBase;

class ReeferMonitoringWebServiceHandlerImplPostInvoke extends GroovyInjectionBase {

    /**
     * This method is called just after  invoking the N4 generic webservices handler.
     * @param inParameters contains the handler that is called.
     */
    public void postHandlerInvoke(Map inParameters) {
        log("postHandlerInvoke (no tx) got called with :" + inParameters);
    }
}