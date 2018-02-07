import java.util.Map;

import com.navis.apex.business.model.GroovyInjectionBase;

class ReeferMonitoringWebServiceHandlerImplPreInvokeInTx extends GroovyInjectionBase {

    /**
     * This method is called just before invoking the N4 generic webservices handler.
     * @param inParameters contains the handler that is called.
     */
    public void preHandlerInvoke(Map inParameters) {
        log("preHandlerInvoke got called with :" + inParameters);

    }
}