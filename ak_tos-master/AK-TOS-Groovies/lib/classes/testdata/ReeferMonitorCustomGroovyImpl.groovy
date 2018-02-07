import com.navis.argo.business.api.GroovyApi
import com.navis.inventory.business.api.UnitField;
import com.navis.inventory.InventoryBizMetafield;

public class ReeferMonitorCustomGroovyImpl {

    public String getYardAreaFromYardPosition(Map args) {

        GroovyApi api = new GroovyApi();
        String pos = args.get("REEFER_POSITION");

        pos = getYardAreaUsingDefaultMechanism(pos);
        String debugMsg = "ReeferMonitorGroovy.getYardAreaFromYardPosition() returning : " + pos;
        api.log(debugMsg);
        println(debugMsg);
        return pos;

    }

    public void doCustomSorting(Map args) {

        //todo need to implement custom sorting here
        GroovyApi api = new GroovyApi();
        Map rfrData = (HashMap)args.get("REEFER_DATA"); //if needs to sort on Yard screen get data set with key as REEFER_YARDS

        if (rfrData != null && !rfrData.isEmpty()) {  // if receive call from Reefer Container screen
          Map newlySorted = differentSort(rfrData);

          String debugMsg = "ReeferMonitorCustomGroovyImpl.doCustomSorting() input : " + rfrData;
          api.log(debugMsg);
          println(debugMsg);

          println ("--------------------------")
          String debugMsg2 = "ReeferMonitorCustomGroovyImpl.doCustomSorting() output with custom sorting : " + newlySorted;
          api.log(debugMsg2);
          println(debugMsg2);
        }
    }

    private Map differentSort (Map map){

        SortedMap newlySortedMap = new TreeMap();
        Collection values = map.values();
        Iterator iter = values.iterator();
        while(iter.hasNext()){

            Map data = (Map)iter.next();
            String criteria = (String)data.get(UnitField.UFV_POS_SLOT.getFieldId());
            data.put(InventoryBizMetafield.RFR_MONITOR_CUSTOM_YARD_POS.getFieldId(), "RMK_"+ criteria );
            String subCriteria = (String)data.get(UnitField.UFV_PRIMARY_EQ_ID_FULL.getFieldId());
            newlySortedMap.put(criteria+subCriteria, data);
        }
        return newlySortedMap;
    }

    String getYardAreaUsingDefaultMechanism(String inPos) {
        String binKey;
        String binName;
        String tierName;

        int lastDot = inPos.lastIndexOf('.');
        if (lastDot > 0) {
            binName = inPos.substring(0, lastDot);
            tierName = inPos.substring(lastDot + 1, inPos.length());
        } else {
            binName = inPos;
            tierName = "";
        }

        binKey = binName;
        // from the over-all yard slot string. This may be groovy based in future.
        // the markers are here so testing code can replace them with different values.
        if (binKey.length() > XXCRITERIAXX) {
            binKey = binName.substring(0, XXCRITERIAXX);
        }
        return binKey;
    }
}