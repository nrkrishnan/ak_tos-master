import org.apache.xmlbeans.XmlObject;

import java.io.Serializable;
import java.math.BigInteger;

import com.navis.argo.BlTransactionDocument;
import com.navis.argo.BlTransactionsDocument;
import com.navis.argo.EdiBillOfLading;
import com.navis.argo.EdiVesselVisit;
import com.navis.argo.business.api.ServicesManager;
import com.navis.argo.business.atoms.BizRoleEnum;
import com.navis.argo.business.reference.LineOperator;
import com.navis.argo.business.reference.ScopedBizUnit;
import com.navis.cargo.business.model.BillOfLading;
import com.navis.external.edi.entity.AbstractEdiPostInterceptor;
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernatingEntity;

/*
* Copyright (c) 2010 Zebra Technologies Corp. All Rights Reserved.
*
*/

public class MaherGvyHoldOrReleaseByMnftQty extends AbstractEdiPostInterceptor {

    @Override
    public void beforeEdiPost(Serializable inSessionGkey, XmlObject inXmlTransactionDocument) {

        String CUSTOM_HOLD_NAME = "CUSTOM_HOLD";
        String REFERENCE_ID = "007";
        String NOTE = "manifested qty is greater than released qty";

        if (BlTransactionsDocument.class.isAssignableFrom(inXmlTransactionDocument.getClass())) {

            BlTransactionsDocument blDocument = (BlTransactionsDocument) inXmlTransactionDocument;
            BlTransactionsDocument.BlTransactions bltrans = blDocument.getBlTransactions();
            BlTransactionDocument.BlTransaction[] bltransArray = bltrans.getBlTransactionArray();
            BlTransactionDocument.BlTransaction blTransaction = bltransArray[0];

            EdiBillOfLading ediBl = blTransaction.getEdiBillOfLading();
            if (ediBl != null) {
                BigInteger bigInteger = ediBl.getManifestedQty();
                if (bigInteger != null) {
                    String blNbr = ediBl.getBlNbr();
                    LineOperator lineOp = findLineOperator(blTransaction);
                    BillOfLading bl = BillOfLading.findBillOfLading(blNbr, lineOp, null);
                    if (bl != null) {
                        long releasedQty = bl.getBlReleasedQty();
                        long manifestedQty = bigInteger.longValue();

                        ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
                        if (manifestedQty > releasedQty) {
                            try {
                                sm.applyHold(CUSTOM_HOLD_NAME, bl, null, REFERENCE_ID, NOTE);
                            } catch (Exception e) {
                                registerError("Unnable to apply [" + CUSTOM_HOLD_NAME +
                                        "] hold on BL while manifested QTY is greater than Release QTY. Error is : " + e.getMessage());
                            }
                        } else {
                            try {
                                sm.applyPermission(CUSTOM_HOLD_NAME, bl, null, REFERENCE_ID, NOTE);
                            } catch (Exception e) {
                                registerError("Unnable to release [" + CUSTOM_HOLD_NAME + "] hold on BL. Error is : " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private LineOperator findLineOperator(BlTransactionDocument.BlTransaction inBlTrans) {

        String vesselOperatorId = null;
        String vesselOperatorIdAgency = null;
        EdiVesselVisit vesselVisit = inBlTrans.getEdiVesselVisit();
        if (vesselVisit != null) {
            if (vesselVisit.getShippingLine() != null) {
                vesselOperatorId = vesselVisit.getShippingLine().getShippingLineCode();
                vesselOperatorIdAgency = vesselVisit.getShippingLine().getShippingLineCodeAgency();
            }
        }
        ScopedBizUnit vesselOperator = ScopedBizUnit.resolveScopedBizUnit(vesselOperatorId, vesselOperatorIdAgency, BizRoleEnum.LINEOP);
        return LineOperator.resolveLineOprFromScopedBizUnit(vesselOperator);
    }

    @Override
    public void afterEdiPost(Serializable inSessionGkey, XmlObject inXmlTransactionDocument, HibernatingEntity inHibernatingEntity) {

    }
}
