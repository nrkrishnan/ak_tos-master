import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.reference.Equipment
import com.navis.framework.business.Roastery
import com.navis.framework.util.BizViolation
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.units.EquipmentState
import com.navis.inventory.business.units.Unit

/*
* Sample Groovy Class.  As below, your class must have a no-arg execute() method that returns a String.
*/

class GroovyCopyHoldsTest{
    public String execute() {

        ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
        Equipment equipment1 = Equipment.findEquipment("XXXUNIT2XX");
        Equipment equipment2 = Equipment.findEquipment("XXXUNIT3XX");
        println("from groovy: equipment : " + equipment1);
        UnitFinder unitFinder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
        Unit unit1 = unitFinder.findActiveUnit(ContextHelper.getThreadComplex(), equipment1);
        Unit unit2 = unitFinder.findActiveUnit(ContextHelper.getThreadComplex(), equipment2);
        println("from groovy: unit1 : " + unit1);
        println("from groovy: unit2 : " + unit2);

        EquipmentState eqsFrom = unit1.getUnitPrimaryUe().getUeEquipmentState();
        String[] flagsUnitFrom = sm.getActiveFlagIds(unit1);
        println("from groovy: flagsFrom : " + flagsUnitFrom);

        String[] flagsUnitTo = sm.getActiveFlagIds(unit2);
        println("from groovy: flagsTo : " + flagsUnitTo);

        try {
            sm.copyActiveFlags(unit1, unit2);
        } catch (BizViolation inBizViolation) {
            println(inBizViolation);
        }


        return "Hello World!"
    }
}