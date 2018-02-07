
import com.navis.apex.business.model.GroovyInjectionBase;
import com.navis.framework.persistence.HibernateApi;
import com.navis.framework.portal.FieldChanges;
import com.navis.framework.util.BizViolation;
import com.navis.inventory.InventoryBizMetafield;
import com.navis.inventory.InventoryField;

/**
 * This is an example Groovy class and is  not readily production deployable. The idea is to provide a simple
 *  template that can be extended by the
 * deployers.
 */
public class XpsDispatchCustomGroovyImpl extends GroovyInjectionBase {

 final String CLASS_NAME = "XpsDispatchCustomGroovyImpl ";

/**
 * ARGO-20999 New Groovy hooks for pre and post dispatch of Hatch Clerk bento messages. RMK, 2009.09.25
 */

public void preXpsDispatch(Map inArgs) throws BizViolation {
    log(">>>>>>>>>>>>>>>>>>> preXpsDispatch: got called");
    log(UnitManagerPea.HC_CHE_SHORT_NAME + ": "+ inArgs.get(UnitManagerPea.HC_CHE_SHORT_NAME));
    log(UnitManagerPea.HC_CONTAINER_IDS + ": "+inArgs.get(UnitManagerPea.HC_CONTAINER_IDS));
    log(UnitManagerPea.HC_REPLAN_CONTAINERS + ": "+ inArgs.get(UnitManagerPea.HC_REPLAN_CONTAINERS));
    log(UnitManagerPea.HC_LOCATION + ": "+ inArgs.get(UnitManagerPea.HC_LOCATION));
    log(UnitManagerPea.HC_LANE + ": "+inArgs.get(UnitManagerPea.HC_LANE));
    log(UnitManagerPea.HC_BENTO + ": "+ inArgs.get(UnitManagerPea.HC_BENTO));
    log(InventoryBizMetafield.RDT_APPLICATION_NAME.getFieldId() + ": "+ inArgs.get(InventoryBizMetafield.RDT_APPLICATION_NAME));
    log("<<<<<<<<<<<<<<<<<<<< preXpsDispatch: got done");
  }


public void postXpsDispatch(Map inArgs) throws BizViolation {
	log("================== postXpsDispatch: got called");
    	log(UnitManagerPea.HC_CHE_SHORT_NAME + ": "+ inArgs.get(UnitManagerPea.HC_CHE_SHORT_NAME));
    	log(UnitManagerPea.HC_CONTAINER_IDS + ": "+inArgs.get(UnitManagerPea.HC_CONTAINER_IDS));
   	log(UnitManagerPea.HC_REPLAN_CONTAINERS + ": "+ inArgs.get(UnitManagerPea.HC_REPLAN_CONTAINERS));
   	log(UnitManagerPea.HC_LOCATION + ": "+ inArgs.get(UnitManagerPea.HC_LOCATION));
   	log(UnitManagerPea.HC_LANE + ": "+inArgs.get(UnitManagerPea.HC_LANE));
   	log(UnitManagerPea.HC_BENTO + ": "+ inArgs.get(UnitManagerPea.HC_BENTO));
   	log(InventoryBizMetafield.RDT_APPLICATION_NAME.getFieldId() + ": "+ inArgs.get(InventoryBizMetafield.RDT_APPLICATION_NAME));
   	log(UnitManagerPea.HC_XPS_RESULT + ": "+ inArgs.get(UnitManagerPea.HC_XPS_RESULT));
   	log(UnitManagerPea.HC_XPS_ERR_MSG + ": "+ inArgs.get(UnitManagerPea.HC_XPS_ERR_MSG));
   	log("=================== postXpsDispatch: got done");

}



}

}