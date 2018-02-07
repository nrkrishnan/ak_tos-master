import org.apache.xmlbeans.XmlObject;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.navis.external.edi.entity.AbstractEdiPostInterceptor;
import com.navis.argo.ReleaseTransactionsDocument;
import com.navis.argo.ReleaseTransactionDocument;
import com.navis.argo.ShippingLine;
import com.navis.argo.EdiReleaseIdentifier;
import com.navis.argo.business.atoms.BizRoleEnum;
import com.navis.argo.business.reference.ScopedBizUnit;
import com.navis.argo.business.reference.LineOperator;
import com.navis.cargo.business.model.BillOfLading;
import com.navis.services.business.rules.Flag
import com.navis.framework.persistence.HibernatingEntity;

/*
* Copyright (c) 2010 Zebra Technologies Corp. All Rights Reserved.
*
*/

public class TestMaherGvyUpdateIbStatusToNull extends AbstractEdiPostInterceptor {

    @Override
    public void beforeEdiPost(Serializable inSessionGkey, XmlObject inXmlTransactionDocument) {

        if(ReleaseTransactionsDocument.class.isAssignableFrom(inXmlTransactionDocument.getClass())){

            List<String> previousHPNameList = new ArrayList<String>();
            previousHPNameList.add("1J");
            //Edi codes for which this feature applies.
            List<String> currentEdiCodeList = new ArrayList<String>();
            currentEdiCodeList.add("54");
            String REFERENCE_ID_START_CHAR = "V";

            ReleaseTransactionsDocument relDocument = (ReleaseTransactionsDocument) inXmlTransactionDocument;
            ReleaseTransactionsDocument.ReleaseTransactions releaseTrans = relDocument.getReleaseTransactions();
            ReleaseTransactionDocument.ReleaseTransaction[] releaseArry = releaseTrans.getReleaseTransactionArray();
            ReleaseTransactionDocument.ReleaseTransaction releaseTransaction = releaseArry[0];

            String ediCode = releaseTransaction.getEdiCode();
            String releaseType = releaseTransaction.getReleaseIdentifierType();

            if ("BLRELEASE".equalsIgnoreCase(releaseType)){
                if (ediCode != null) {
                    if (currentEdiCodeList.contains(ediCode)) {
                        if (releaseTransaction.getEdiReleaseIdentifierArray() != null && releaseTransaction.getEdiReleaseIdentifierArray().length > 0){
                            EdiReleaseIdentifier releaseIdentifier = releaseTransaction.getEdiReleaseIdentifierArray(0);
                            String blNbr = releaseIdentifier.getReleaseIdentifierNbr();
                            if (blNbr != null){
                                LineOperator lineOp = (LineOperator) findLineOperator(releaseTransaction);
                                BillOfLading bl = BillOfLading.findBillOfLading(blNbr, lineOp, null);
                                if (bl != null) {
                                    Collection flagColl = Flag.findAllFlagsForEntity(bl);
                                    if (flagColl != null) {
                                        Iterator iterator = flagColl.iterator();
                                        for (; iterator.hasNext();) {
                                            Flag flag = (Flag) iterator.next();
                                            String hpName = flag.getFlagFlagType().getFlgtypId();
                                            if (previousHPNameList.contains(hpName)) {
                                                String referenceId = flag.getFlagReferenceId();
                                                if (referenceId != null && referenceId.length() > 1) {
                                                    //If entity has atleast 1 hold/permission with refId starts with "V", don't update IbStatus in entity.
                                                    if (!referenceId.substring(0, 1).equalsIgnoreCase(REFERENCE_ID_START_CHAR)) {
                                                        releaseTransaction.setInbondStatus(null);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private ScopedBizUnit findLineOperator(ReleaseTransactionDocument.ReleaseTransaction inRelease) {
        ShippingLine ediLine = inRelease.getEdiShippingLine();
        ScopedBizUnit line = null;
        if (ediLine != null) {
            String lineCode = ediLine.getShippingLineCode();
            String lineCodeAgency = ediLine.getShippingLineCodeAgency();
            line = ScopedBizUnit.resolveScopedBizUnit(lineCode, lineCodeAgency, BizRoleEnum.LINEOP);
        }
        return line;
    }
    @Override
    public void afterEdiPost(Serializable inSessionGkey, XmlObject inXmlTransactionDocument, HibernatingEntity inHibernatingEntity) {

    }
}
