import com.navis.argo.business.reference.EquipCondition;
import com.navis.argo.business.reference.MnrStatus;
import com.navis.framework.persistence.HibernateApi;

// This is a custom groovy code written for Antwerp.
// using this as a sample for creating groovy unit tests
// so if the apis change we will catch.

public class MnrToCondition {

    public EquipCondition getConditionCode(MnrStatus mnr) {

        java.util.Map map = new java.util.HashMap();

        final Long MNR_AS = MnrStatus.findOrCreateMnrStatus("AS").getMnrstatGkey();

        final Long MNR_AL = MnrStatus.findOrCreateMnrStatus("AL").getMnrstatGkey();

        final Long MNR_NR = MnrStatus.findOrCreateMnrStatus("NR").getMnrstatGkey();

        final Long MNR_ES = MnrStatus.findOrCreateMnrStatus("ES").getMnrstatGkey();

        final Long MNR_AC = MnrStatus.findOrCreateMnrStatus("AC").getMnrstatGkey();

        final Long MNR_OK = MnrStatus.findOrCreateMnrStatus("OK").getMnrstatGkey();

        final Long MNR_SU = MnrStatus.findOrCreateMnrStatus("SU").getMnrstatGkey();

        final Long MNR_NO = MnrStatus.findOrCreateMnrStatus("NO").getMnrstatGkey();

        final Long MNR_TC = MnrStatus.findOrCreateMnrStatus("TC").getMnrstatGkey();

        // condition codes

        final Long CND_OS = EquipCondition.findOrCreateEquipCondition("OS").getEqcondGkey();

        final Long CND_OK = EquipCondition.findOrCreateEquipCondition("OK").getEqcondGkey();

        final Long CND_NR = EquipCondition.findOrCreateEquipCondition("NR").getEqcondGkey();

        final Long CND_TC = EquipCondition.findOrCreateEquipCondition("TC").getEqcondGkey();



        // mapping to CND_OS

        map.put(MNR_AS, CND_OS);

        map.put(MNR_ES, CND_OS);

        map.put(MNR_SU, CND_OS);

        map.put(MNR_AL, CND_OS);

        map.put(MNR_AC, CND_OS);

        // mapping to CND_OK

        map.put(MNR_OK, CND_OK);

        map.put(MNR_NO, CND_OK);

        // mapping to CND_NR

        map.put(MNR_NR, CND_NR);

        // mapping empty to empty

        map.put(MNR_TC, CND_TC);
        println("unit's M&R status: " + mnr);

        Long converted = (mnr == null || map.get(mnr.getMnrstatGkey()) == null) ? CND_TC : map.get(mnr.getMnrstatGkey());

        if (converted == null) {
            converted = CND_TC;
        }

        def cnd = (EquipCondition) HibernateApi.getInstance().load(EquipCondition.class, converted);


        return cnd;

    }
}