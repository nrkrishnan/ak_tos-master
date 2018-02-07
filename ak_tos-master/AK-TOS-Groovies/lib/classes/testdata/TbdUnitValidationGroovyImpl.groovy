import java.util.Map
import java.util.Date
import java.text.SimpleDateFormat
import java.text.ParseException

import com.navis.argo.business.api.GroovyApi
import com.navis.inventory.business.units.TbdUnitDeliveryValidator
import com.navis.framework.util.BizViolation
import com.navis.framework.util.BizFailure
import com.navis.inventory.business.units.ITbdUnitValidator

/**
 * Trivial groovy script to verify that exceptions are handled by the calling product code properly.
 * ARGO-23390, RMK, 2010-01-24
 */
public class TbdUnitValidationGroovyImpl {
  public void execute(Map args) throws BizViolation {
    def api = new GroovyApi()

    api.log("TbdUnitValidationGroovyImpl: BEGIN: " + new Date())

    def info = "NO INFO"

    // getting validator
    def validator = (TbdUnitDeliveryValidator) args.get("VALIDATOR")

    try {
      api.log("TEST:RMK: Modified Groovy");
      throw BizViolation.create(BizFailure.create("TEST:RMK: Modified Groovy"))
    }
    catch (Exception e) {
      api.log("TbdUnitValidationGroovyImpl: END (Validation Failed $info): Error=$e.message" + new Date())
      throw e
    }
    api.log("TbdUnitValidationGroovyImpl: END (Validation Successful $info) : " + new Date())
  }
}