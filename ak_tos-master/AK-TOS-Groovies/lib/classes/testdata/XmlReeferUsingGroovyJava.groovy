import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.ContextHelper
import com.navis.argo.webservice.ArgoWebServicesFacade
import com.navis.argo.webservice.IArgoWebService
import com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType
import com.navis.framework.portal.UserContext

/**
 * This is an example groovy class to show how a user can compose an XML
 * and then pass that to the underlying layer for processing.
 * If the groovy is called in a transaction (which is usually the case)
 * this code will be exeucted in transaction and so is the code that is called.
 * @author <a href="mailto:rkhawaja@navis.com">Rafay Khawaja</a>, Apr 27, 2009
 */
public class XmlReeferUsingGroovyJava extends GroovyInjectionBase {

    public String execute(Map inParameters) {

        // In real practice it is expected that huge reefer that needs to be posted will be
        // placed on the file system or in cutomer's database (possibly).
        String reeferDoc = (String) inParameters.get("reefer-xml-string");
        log("got called and returning reefr xml " + reeferDoc);

        UserContext uc = ContextHelper.getThreadUserContext();

        IArgoWebService ws = new ArgoWebServicesFacade(uc);
        ScopeCoordinateIdsWsType scopeCoordinates = new ScopeCoordinateIdsWsType();
        scopeCoordinates.setOperatorId("OPR1");
        scopeCoordinates.setComplexId("CPX11");
        scopeCoordinates.setFacilityId("FCY111");
        scopeCoordinates.setYardId("YRD1111");
        ScopeCoordinateIdsWsType coords = scopeCoordinates;


        String reeferXMLStr = "<argo:reefer-monitor xmlns:argo=\"http://www.navis.com/argo\">\n" +
                "  <update-reefers>\n" +
                "    <reefer-monitoring>\n" +
                "    <unit-identity id=\"" + TEST_REEFER_ID + "\"/>  \n" +
                "      <position loc-type=\"YARD\" location=\"X\" slot=\"" + TEST_YARD_SLOT_UPDATED_POSITION +
                "\" orientation=\"string\" carrier-id=\"string\"/>\n" +
                "      <reefer \n" +
                "    temp-reqd-c=\"110.00\"\n" +
                "    o2-pct=\"100.00\"\n" +
                "    co2-pct=\"0.01\"\n" +
                "    humidity-pct=\"10.00\"\n" +
                "    vent-required-value=\"10.00\"\n" +
                "    vent-required-unit=\"PERCENTAGE\"\n" +
                "    temp-min-c=\"10.00\"\n" +
                "    temp-max-c=\"110.00\"\n" +
                "    temp-display-unit=\"C\"\n" +
                "    is-power=\"N\"\n" +
                "    wanted-is-power=\"N\"\n" +
                "    time-latest-on-power=\"2012-01-07T11:42:56\"\n" +
                "    time-monitor-1=\"2014-10-27T02:44:59\"\n" +
                "    time-monitor-2=\"2013-06-17T10:14:58\"\n" +
                "    time-monitor-3=\"2005-09-12T01:55:02-07:00\"\n" +
                "    time-monitor-4=\"2006-11-29T09:20:00\"/>\n" +
                "      <reefer-recording-histories>\n" +
                "        <reefer-recording-history\n" +
                "    time-of-recording=\"2005-09-12T01:55:02-07:00\"\n" +
                "    return-temp-c=\"100.00\"\n" +
                "    vent-setting=\"100.00\"\n" +
                "    vent-unit=\"PERCENTAGE\"\n" +
                "    humidity-pct=\"100.00\"\n" +
                "    o2-pct=\"100.00\"\n" +
                "    co2-pct=\"100.00\"\n" +
                "    rec-fault-code=\"N\"\n" +
                "    max_mtr_tmp=\"100.00\"\n" +
                "    min_mtr_tmp=\"100.00\"\n" +
                "    defrost-temp=\"100.00\"\n" +
                "    tmp-set-pt=\"100.00\"\n" +
                "    supply-tmp=\"100.00\"\n" +
                "    fuel-lvl=\"100.00\"\n" +
                "    reefer-hours=\"100.00\"\n" +
                "    remark=\"100.00\"/>\n" +
                "      </reefer-recording-histories>\n" +
                "    </reefer-monitoring>\n" +
                "  </update-reefers>\n" +
                "</argo:reefer-monitor>";


        log ("Sending XML to Basic Invoke: " + reeferXMLStr );
        String xmlResponse = ws.basicInvoke("OPR1/CPX11/FCY111/YRD1111", reeferXMLStr);

        return xmlResponse;
    }

    String TEST_REEFER_ID = "RMKUSTUNI02";
    String TEST_YARD_SLOT_UPDATED_POSITION = "111";
}
