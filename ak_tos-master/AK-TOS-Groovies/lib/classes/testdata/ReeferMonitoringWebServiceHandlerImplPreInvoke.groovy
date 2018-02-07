import java.util.Map;
import com.navis.apex.business.model.GroovyInjectionBase;

class ReeferMonitoringWebServiceHandlerImplPreInvoke extends GroovyInjectionBase {

    public String transform(String inXml) {
    log("transform method got called");
        return inXml;
    }
}