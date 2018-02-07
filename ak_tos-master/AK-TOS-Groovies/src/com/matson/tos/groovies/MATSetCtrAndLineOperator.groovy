package matsongroovy

import com.navis.argo.ContextHelper;
import com.navis.argo.business.reference.LineOperator;
import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.inventory.business.units.*
import com.navis.road.business.atoms.TranSubTypeEnum
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.workflow.TransactionAndVisitHolder
/**
 * This groovy set the unit line operator and container operator to either MAE or APL, while receiving empty
 */

public class MATSetCtrAndLineOperator extends AbstractGateTaskInterceptor implements EGateTaskInterceptor

{
    public void execute(TransactionAndVisitHolder inDao)

    {
        this.log("Execution Started MATSetCtrAnLineOperator");
        /* check various components of the gate transaction to insure everything needed is present. */

        if (inDao == null)
            return;

        TruckTransaction ThisTran = inDao.getTran();

        if (ThisTran == null)
            return;

        /* Execute the built-in logic got the business task. */

        executeInternal(inDao);

        /* set the unit priority stow code to the booking stow block */

        Unit ThisUnit = ThisTran.getTranUnit();

        /* for RM transactions set OB routing if VV/POD specified */
        if (TranSubTypeEnum.RM.equals(ThisTran.getTranSubType())
                && ThisTran.getTranCtrOwnerId() != null && ("APLU".equalsIgnoreCase(ThisTran.getTranCtrOwnerId()) ||
                "MAEU".equalsIgnoreCase(ThisTran.getTranCtrOwnerId())) && ThisTran.getTranEqo() == null) {
            String operatorId="MAE";
            if ("APLU".equalsIgnoreCase(ThisTran.getTranCtrOwnerId())) {
                operatorId = "APL";
            }
            LineOperator lineOperator = LineOperator.findLineOperatorById(operatorId);
            this.log("Tran Eqo Nbr String : "+ThisTran.getTranEqoNbr());
            this.log("Tran Eqo Nbr : "+ThisTran.getTranEqo());
            this.log("Tran VV : "+ThisTran.getTranCarrierVisit());
            if (ThisTran.getTranEqoNbr() == null && ThisTran.getTranCarrierVisit() == null) {
                if (lineOperator != null) {
                    ThisTran.setTranLine(lineOperator);
                    ThisTran.setTranLineId(operatorId);
                    ThisTran.setTranCtrOperator(lineOperator);
                    EquipmentState equipmentState = EquipmentState.findEquipmentState(ThisTran.getTranContainer(), ContextHelper.getThreadOperator());
                    if (equipmentState != null) {
                        equipmentState.setEqsFlexString01("CLI");
                        equipmentState.setEqsEqOperator(lineOperator);
                    }
                    //EquipmentState.upgradeEqOperator(ThisTran.getTranContainer(), lineOperator, DataSourceEnum.IN_GATE);
                    if (ThisUnit != null) {
                        ThisUnit.setUnitLineOperator(lineOperator);
                    }

                    this.log("Operator updated to : " + lineOperator.getBzuId());
                }
            }
        }
    }
}