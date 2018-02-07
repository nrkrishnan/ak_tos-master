
import com.navis.argo.business.reference.Accessory;
import com.navis.argo.business.reference.Equipment;
/*
* Get the n4 equipment object.
*/
class EquipmentState{
    public Object getEquipment(Object id) {
        return Equipment.findEquipment(id);
    }
}