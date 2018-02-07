/*
 * Copyright (c) 2017 WeServe LLC. All Rights Reserved.
 *
 */
import com.navis.argo.business.atoms.FreightKindEnum
import com.navis.argo.business.model.GeneralReference
import com.navis.framework.util.internationalization.PropertyKeyFactory
import com.navis.framework.util.message.MessageLevel
import com.navis.inventory.business.units.Unit
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder


import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import org.apache.log4j.Level


class MatsonValidateSealTypeAtGate extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {

    @Override
    public void postProcess(TransactionAndVisitHolder inWfCtx) {

        TruckTransaction thisTran = inWfCtx.getTran();
        log("In Matson postProcess thisTran :"+thisTran)

        if(thisTran == null){
            return;
        }

        Unit unit = thisTran.getTranUnit();

        log("In Matson postProcess unit:"+unit)
        if (unit == null) {
            return;
        }
        GeneralReference generalReference = GeneralReference.findUniqueEntryById("MATSON", "SEAL_TYPE","FCL","MTY");
        if(generalReference != null){

            String[] FCL_SEAL_TYPE = generalReference.getRefValue1() != null ? generalReference.getRefValue1().split("\\r|\\n|,", -1) : null;
            String[] MTY_SEAL_TYPE = generalReference.getRefValue2() != null ? generalReference.getRefValue2().split("\\r|\\n|,", -1) : null;


            // Todo : Assuming the flexStringO5 is a "SEALTYPE" Field. //unitFlexString05

            String unitSealType = thisTran.getTranSealNbr2();
            log(Level.WARN, "Seal TYpe : "+unitSealType);
            if(FreightKindEnum.FCL.equals(unit.getUnitFreightKind()) && TranSubTypeEnum.RE.equals(thisTran.getTranSubType()) ){
                log(Level.WARN, "Freight FCL : ");
                log(Level.WARN, "Freight FCL : "+FCL_SEAL_TYPE);
                boolean  anyOneSealReq =false;
                if (unit.getUnitSealNbr1() != null || unit.getUnitSealNbr2() != null || unit.getUnitSealNbr3() != null || unit.getUnitSealNbr4() != null) {
                    anyOneSealReq = true;
                }

                if(unitSealType != null && !isValidSealType(FCL_SEAL_TYPE,unitSealType)) {
                    RoadBizUtil.appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf("GATE.MATSON_SEAL_TYPE_MISMATCH"),unitSealType,unit.getUnitFreightKind(), unitSealType);
                }

            } else if(FreightKindEnum.MTY.equals(unit.getUnitFreightKind()) && TranSubTypeEnum.RM.equals(thisTran.getTranSubType())) {
                log(Level.WARN, "Freight MTY : ");
                log(Level.WARN, "Freight MTY : "+MTY_SEAL_TYPE);
                if(unitSealType != null && !isValidSealType(MTY_SEAL_TYPE,unitSealType)) {
                    RoadBizUtil.appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf("GATE.MATSON_SEAL_TYPE_MISMATCH"),unitSealType,unit.getUnitFreightKind(), unitSealType);
                }
            }
            log("In Matson postProcess Completed")

        }


    }
    private boolean isValidSealType(String[] inAllowedSealType, String inSeal) {
        Boolean isValid = Boolean.FALSE;
        if (inAllowedSealType!= null && inAllowedSealType.length >0 && inSeal!= null) {
            for (int i=0; i< inAllowedSealType.length; i++) {
                if (inSeal.equalsIgnoreCase(inAllowedSealType[i]))
                    isValid = Boolean.TRUE;
            }
        }
        return isValid;
    }
}
