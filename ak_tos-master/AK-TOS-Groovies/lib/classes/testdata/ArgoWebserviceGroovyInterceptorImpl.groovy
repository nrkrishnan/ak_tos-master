import java.util.Map;

import com.navis.apex.business.model.GroovyInjectionBase;

class ArgoWebserviceGroovyInterceptorImpl extends GroovyInjectionBase {

    /**
     * This method allows the injected code to transform an XML that came over
     * webservices.
     * @param inXml xml pay load in the generic webservices call.
     * @return transformed (well formed) xml.
     */
    public String transform(String inXml) {
        return inXml;
    }

    /**
     * This method is called just before invoking the N4 generic webservices handler.
     * @param inParameters contains the handler that is called.
     */
    public void preHandlerInvoke(Map inParameters) {
        log("preHandlerInvoke got called with :" + inParameters);

    }

    /**
     * This method is called just after  invoking the N4 generic webservices handler.
     * @param inParameters contains the handler that is called.
     */
    public void postHandlerInvoke(Map inParameters) {
        log("postHandlerInvoke got called with :" + inParameters);
    }
}