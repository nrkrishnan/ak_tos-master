/*
 * Copyright (c) 2012 Navis LLC. All Rights Reserved.
 *
 */


import com.navis.argo.*
import com.navis.argo.business.api.*
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.EdiPostingContext
import com.navis.argo.business.model.Facility
import com.navis.argo.business.reference.LineOperator
import com.navis.argo.business.reference.ScopedBizUnit
import com.navis.cargo.InventoryCargoEntity
import com.navis.cargo.InventoryCargoField
import com.navis.cargo.business.model.BillOfLading
import com.navis.cargo.business.model.BlRelease
import com.navis.external.edi.entity.AbstractEdiPostInterceptor
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.HibernatingEntity
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.Ordering
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizViolation
import com.navis.framework.util.internationalization.PropertyKeyFactory
import com.navis.services.business.rules.EventType
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.xmlbeans.XmlObject

/**
 * Author: Balamurugan Bakthavachalam
 * PLEASE UPDATE THE BELOW DESCRIPTION WHENEVER THE GROOVY IS MODIFIED
 * Description: The disposition codes handled in this groovy and usages are given below.
 * 54: When 54 is received, set BOL manifest qty to zero(rest of the BL fields will remain intact.) and record RECEIVED_54 event.
 * 55: Update the manifest qty with the quantity received in the 350 message and record RECEIVED_55 event.
 * 1J: update inbond qty and record RECEIVED_1J event.
 * 95: If Match Quantity is NULL in 350 release map, Cancel all 1J irrespective to reference number, update inbond status and record
 * CANCELED_1J event. If Match Quantity is Match By Reference Number in 350 release map, Cancel all 1J only if reference number matches with reference
 * number received in the 350 message, update inbond status and record CANCELED_1J event.
 * 1C: If there is an active 1J then update inbond qty else inbond status will remain same.
 * Inbond qty is sum of active 1J and 1C quantities.
 * Logic to update INBOND quantity: if manifest qty is less than or equal to inbond qty then inbond status should be INBOND otherwise the status
 * should be CANCEL
 * P401(Location Identifier (Code which identifies a specific location)) element is mapped to the ediReleaseFlexString01 attribute of the release.xsd.
 * If the value of ediReleaseFlexString01 is same as the Schedule D code of the routing point for the facility where the message is being posted then
 * EDI message should be posted successfully else throw an error.
 *
 */

/**
 * Issue Ids: ARGO-39289 - Initial version of System seeded groovy for US customs BL Release(EDI)
 */
public class USCustomsBLReleaseGvy extends AbstractEdiPostInterceptor {

  /**
   * Method will be called from EDI engine
   */
  public void beforeEdiPost(XmlObject inXmlTransactionDocument, Map inParams) {
    LOGGER.setLevel(Level.INFO);
    if (inXmlTransactionDocument == null || !ReleaseTransactionsDocument.class.isAssignableFrom(inXmlTransactionDocument.getClass())) {
      return;
    }
    ReleaseTransactionsDocument relDocument = (ReleaseTransactionsDocument) inXmlTransactionDocument;
    ReleaseTransactionsDocument.ReleaseTransactions releaseTrans = relDocument.getReleaseTransactions();
    ReleaseTransactionDocument.ReleaseTransaction[] releaseArray = releaseTrans.getReleaseTransactionArray();

    if (releaseArray == null || releaseArray.length == 0) {
      LOGGER.error("Release Array is NULL in before EDI post method");
      return;
    }

    ReleaseTransactionDocument.ReleaseTransaction releaseTransaction = releaseArray[0];
    //validate scheduled D code
    validateRoutingPoint(releaseTransaction, inParams);

    /**
     Uncomment the below method if 5H is not just a Information message.
     Please note if you uncomment this method call you must uncomment handle5H5I4ALogicAfterPost() method call in afterEdiPost() too.
     * */
    String ediCode = releaseTransaction.getEdiCode();

    /**
     Uncomment the below method if your information message example 1h,2h,7h... comes with no guaranteed unique reference nbr and your message is expected to apply multiple hold.
     Please note:
     1)	If you uncomment this method call then you must uncomment setBackTransactionReferenceId () method call in afterEdiPost() too.
     2)	This should be uncommented if customer is using 2.3-rel or its older version
     * */
    if (DISPOSITION_CODES_FOR_UNIQUE_ID.indexOf(ediCode) >= 0) {
      setUniqueReferenceId(releaseTransaction, ediCode);
    }
  }

  /**
   * This method is being used in beforeEdiPost method
   * Method to validate the routing point(Schedule D code) of the facility is same as the value of ediReleaseFlexString01 in message.
   * @param inReleaseTransaction
   */
  private void validateRoutingPoint(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction, Map inParams) {
    EdiReleaseFlexFields releaseFlexFields = inReleaseTransaction.getEdiReleaseFlexFields();
    if (releaseFlexFields != null && releaseFlexFields.getEdiReleaseFlexString01() != null) {

      //Get posting context for configuration value
      EdiPostingContext postingContext = ContextHelper.getThreadEdiPostingContext();
      String facilityId = (String) ArgoEdiUtils.getConfigValue(postingContext, ArgoConfig.EDI_FACILITY_FOR_POSTING);

      //if facility is empty then throw an error and skip the posting since we cannot validate without facility id in setting
      if (StringUtils.isEmpty(facilityId)) {
        inParams.put(SKIP_POSTER, true);
        throw BizViolation.create(PropertyKeyFactory.valueOf("Facility Id is empty/null in setting EDI_FACILITY_FOR_POSTING"), null);
      }

      //Throw an error if facility not found for given id and skip the posting
      Facility facility = Facility.findFacility(facilityId, ContextHelper.getThreadComplex());
      if (facility == null) {
        inParams.put(SKIP_POSTER, true);
        throw BizViolation.create(PropertyKeyFactory.valueOf("Could not find the Facility for Id: " + facilityId), null);
      }

      // If routing point(Schedule D code) of the facility is not same as the value of ediReleaseFlexString01 in message then throw an error and
      // skip the posting
      String scheduledDCode = facility.getFcyRoutingPoint().getPointScheduleDCode();
      String messageScheduleDCode = releaseFlexFields.getEdiReleaseFlexString01();
      if (!messageScheduleDCode.equals(scheduledDCode)) {
        inParams.put(SKIP_POSTER, true);
        throw BizViolation
                .create(PropertyKeyFactory.valueOf("EDI release message is not for this port, port schedule Dcode:" + scheduledDCode +
                "  does not match with message schedule D code:" + messageScheduleDCode), null);
      }
    }
  }

  /**
   * Method will be called from EDI engine
   */
  @Override
  public void afterEdiPost(XmlObject inXmlTransactionDocument, HibernatingEntity inHibernatingEntity, Map inParams) {

    //if value is true then skip after edi post method
    if (Boolean.TRUE.equals(inParams.get(SKIP_POSTER))) {
      LOGGER.info("Skipped after edi post method.");
      return;
    }

    //check the given message is release
    if (inXmlTransactionDocument == null || !ReleaseTransactionsDocument.class.isAssignableFrom(inXmlTransactionDocument.getClass())) {
      return;
    }
    ReleaseTransactionsDocument relDocument = (ReleaseTransactionsDocument) inXmlTransactionDocument;
    ReleaseTransactionsDocument.ReleaseTransactions releaseTrans = relDocument.getReleaseTransactions();
    ReleaseTransactionDocument.ReleaseTransaction[] releaseArray = releaseTrans.getReleaseTransactionArray();

    if (releaseArray == null || releaseArray.length == 0) {
      LOGGER.info("Release Array is NULL in after EDI post method");
      return;
    }

    ReleaseTransactionDocument.ReleaseTransaction releaseTransaction = releaseArray[0];
    String ediCode = releaseTransaction.getEdiCode();

    LOGGER.info("EDI CODE: " + ediCode);
    if (ediCode == null) {
      return;
    }

    //flush the session to persist the release which is being posted
    HibernateApi.getInstance().flush();
    BillOfLading bl = null;
    EdiReleaseIdentifier releaseIdentifier = releaseTransaction.getEdiReleaseIdentifierArray(0);
    String blNbr = releaseIdentifier.getReleaseIdentifierNbr();
    if (blNbr != null) {
      LineOperator lineOp = (LineOperator) findLineOperator(releaseTransaction);
      bl = BillOfLading.findBillOfLading(blNbr, lineOp, null);
    }

    // write the error
    if (ArgoUtils.isEmpty(blNbr) || bl == null) {
      LOGGER.error("Bill Of Lading not found for BlNbr: " + blNbr);
      return;
    }

    //handle 54 disposition code for US customs  uncomment if require
    if ("54".equals(ediCode)) {
      handle54(bl);
    }

    //handle 55 disposition code for US customs
    if ("55".equals(ediCode)) {
      handle55(releaseTransaction, bl);
    }

    //handle 1J&95 disposition code for US customs
    if ("95".equals(ediCode) || "1J".equalsIgnoreCase(ediCode)) {
      // record 1J received event
      if ("1J".equalsIgnoreCase(ediCode)) {
        recordServiceEvent(bl, ADD_EVENT_1J_STR);
        updateInbondStatus(bl);
      }
      handle1JUsing95(releaseTransaction, bl);
    }

    //update inbond status only if there is active 1J
    if (("1C".equalsIgnoreCase(ediCode) || "1W".equalsIgnoreCase(ediCode)) && hasActive1J(bl.getBlGkey())) {
      updateInbondStatus(bl);
    }


    if ("4A".equalsIgnoreCase(ediCode)) {
      makeEnteredQtyPositive(bl, ediCode);
    }

    if ("4C".equalsIgnoreCase(ediCode)) {
      handleEnteredQtyFor4C(bl, ediCode);
    }

    if ("5H".equalsIgnoreCase(ediCode)) {
      handle5HLogicAfterPost(bl, ediCode);
    }

    if ("5I".equalsIgnoreCase(ediCode)) {
      handle5ILogicAfterPost(bl, ediCode);
    }


    if ("4E".equalsIgnoreCase(ediCode)) {
      handle4E(bl, releaseTransaction.getReleaseReferenceId());
    }

    /**
     Before uncomment this method call please see comments that are given for setUniqueReferenceId() in beforeEdiPost()
     * */

    if (DISPOSITION_CODES_FOR_UNIQUE_ID.indexOf(ediCode) >= 0) {
      HibernateApi.getInstance().flush();
      setBackTransactionReferenceId(releaseTransaction, bl, ediCode);
    }
  }

  /**
   * This method is being used in afterEdiPost method
   * This method will be executed if disposition code 95 or 1J(out of order message)
   * * Cancel active 1J using 95 disposition code
   * @param inReleaseTransaction
   * @param inBl
   */
  private void handle1JUsing95(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction, BillOfLading inBl) throws BizViolation {
    String referenceId = inReleaseTransaction.getReleaseReferenceId();
    String ediCode = inReleaseTransaction.getEdiCode()
    List nintyFiveBlReleases = findBlReleases(inBl.getBlGkey(), "95");

    //no need to handle if 95 disposition code does not exist
    if (nintyFiveBlReleases.isEmpty()) {
      return;
    }

    // if blRelease is canceled already then don't cancel once again.
    BlRelease nintyFiveBlRelease = null;
    for (BlRelease release : nintyFiveBlReleases) {
      if ("95".equals(ediCode) && isBlReleaseCanceled(inBl.getBlGkey(), release.getBlrelGkey())) {
        continue;
      }
      nintyFiveBlRelease = release;
      break;
    }

    //return if there is no 95 release
    if (nintyFiveBlRelease == null) {
      return;
    }

    boolean isMatchByRef = isQtyMatchByReference(inReleaseTransaction)

    //To find active 1J use 95's reference id.
    if ("1J".equalsIgnoreCase(ediCode)) {
      referenceId = nintyFiveBlRelease.getBlrelReferenceNbr();
    }

    if (referenceId == null && isMatchByRef) {
      LOGGER.error("Could not cancel Active 1J since reference Id is null and MatchQtyByReference is selected in release map LOV.")
      return;
    }

    //Find existing 1J
    List<BlRelease> blRel = findActive1J(inReleaseTransaction, inBl.getBlGkey(), referenceId);
    for (BlRelease release1J : blRel) {
      // By setting blRelReference we are marking the 1J release as cancelled
      release1J.setFieldValue(InventoryCargoField.BLREL_REFERENCE, nintyFiveBlRelease);
      HibernateApi.getInstance().update(release1J);
    }

    if (!blRel.isEmpty()) {
      recordServiceEvent(inBl, CANCELED_EVENT_1J_STR);
      updateInbondStatus(inBl);
    }
  }

  private void handle4E(BillOfLading inBl, String inReferenceNbr) {
    //LOGGER.info("Start handle 4E : " + MAHER_UTILS.getTimeNow());
    //We will make sure that 4E disposition code is written to the database before we begin our adjustment process
    HibernateApi.getInstance().flush();

    //find active 1A, 1B or 1C disposition codes that are received till now for the given 4E reference nbr. If a 1A, 1B and 1C is already
    //cancelled by a 4E then the BL Release Reference Entity for that 1A,1B, 1C BLRelease will be set to the gkey of the 4E that cancelled it and
    //BL Release Reference Entity for 4E will be the gkey of the BLRelease that it cancels.
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBl.getPrimaryKey()))
            .addDqPredicate(PredicateFactory.in(InventoryCargoField.BLREL_DISPOSITION_CODE, [DISP_CODE_1A, DISP_CODE_1B, DISP_CODE_1C]))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE))
    //add order by predicate by BlREl post date and blrel_gkey;
            .addDqOrdering(Ordering.desc(InventoryCargoField.BLREL_POST_DATE))
            .addDqOrdering(Ordering.desc(InventoryCargoField.BLREL_GKEY));

    //add referebce nbr predicate to domain query if inReferenceNbr isnot null only
    if (inReferenceNbr != null) {
      dq.addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceNbr));
    }
    List<BlRelease> blReleases = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);

    //find the BL Releases that were created as a result of receiving 4E.
    DomainQuery dq1 = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBl.getPrimaryKey()))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, DISP_CODE_4E))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE))
            .addDqOrdering(Ordering.asc(InventoryCargoField.BLREL_POST_DATE));
    //add referebce nbr predicate to domain query if inReferenceNbr is not null only
    if (inReferenceNbr != null) {
      dq1.addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceNbr))
    }
    List<BlRelease> blReleases4E = HibernateApi.getInstance().findEntitiesByDomainQuery(dq1);
    //4E bel rel size is always 2 as 4E has two release map
    LOGGER.info("BL 4ERelease size : " + blReleases4E.size());

    //if the first entry(order by gkey and postdate) is 1C then we don't
    if (blReleases != null && blReleases.size() > 0) {
      BlRelease blRelease = blReleases.get(0);
      if (DISP_CODE_1C.equalsIgnoreCase(blRelease.getBlrelDispositionCode())) {
        LOGGER.info("Disp 1C is found as first record in list so only 4E cancels 1C disp code alone and other 4E entries will be nullified ");
        BlRelease rel4EFor1CDisp = get4EReleaseForHoldId(blReleases4E, DISP_CODE_1C_HOLD_ID);
        rel4EFor1CDisp.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, blRelease.getBlrelQuantityType());
        rel4EFor1CDisp.setFieldValue(InventoryCargoField.BLREL_REFERENCE, blRelease);
        blRelease.setFieldValue(InventoryCargoField.BLREL_REFERENCE, rel4EFor1CDisp);
        //1A followd by 1C followed by 4E case; system release the 1A hold as there is an active 1A hold exist so here
        //we need to reapply the hold as 1C
        if (blReleases.size() > 1) {
          BlRelease rel1ADisp = blReleases.get(1);
          if (DISP_CODE_1A.equalsIgnoreCase(rel1ADisp.getBlrelDispositionCode())) {
            String holdId = rel1ADisp.getBlrelFlagType().getFlgtypId().trim();
            _sm.applyHold(holdId, inBl, null, inReferenceNbr, holdId);
          }
        }

        setBlRelModifyQtyToNullForAllBlReleases(blReleases4E, rel4EFor1CDisp);
      } else if (DISP_CODE_1A.equalsIgnoreCase(blRelease.getBlrelDispositionCode())) {
        LOGGER.info("Disp 1A is found as first record in list so 4E cancels 1A disp code alone and other 4E entries will be nullified ");
        BlRelease rel4EFor1ADisp = get4EReleaseForHoldId(blReleases4E, blRelease.getBlrelFlagType().getFlgtypId());
        rel4EFor1ADisp.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, blRelease.getBlrelQuantityType());
        rel4EFor1ADisp.setFieldValue(InventoryCargoField.BLREL_REFERENCE, blRelease);
        blRelease.setFieldValue(InventoryCargoField.BLREL_REFERENCE, rel4EFor1ADisp);
        setBlRelModifyQtyToNullForAllBlReleases(blReleases4E, rel4EFor1ADisp);
      } else if (DISP_CODE_1B.equalsIgnoreCase(blRelease.getBlrelDispositionCode())) {
        boolean isDisp1AFollowedBy1B = false;
        for (BlRelease blRel : blReleases) {
          if (DISP_CODE_1A.equalsIgnoreCase(blRel.getBlrelDispositionCode())) {
            LOGGER.info("Disp 1A and 1B are found as first and second records in list so 4E cancels both 1A and 1B disp codes");
            BlRelease rel4EFor1ADisp = get4EReleaseForHoldId(blReleases4E, blRel.getBlrelFlagType().getFlgtypId());
            rel4EFor1ADisp.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, blRel.getBlrelQuantityType());
            rel4EFor1ADisp.setFieldValue(InventoryCargoField.BLREL_REFERENCE, blRel);
            blRel.setFieldValue(InventoryCargoField.BLREL_REFERENCE, rel4EFor1ADisp);
            if (blReleases4E.size() == 1) {
              //create second 4E bl release for canceling 1B entry as there is only one release map defined for 4E
              BlRelease rel4E1B = new BlRelease();
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_BL, inBl);
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_QUANTITY, rel4EFor1ADisp.getBlrelQuantity());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_FLAG_TYPE, rel4EFor1ADisp.getBlrelFlagType());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, EdiReleaseMapModifyQuantityEnum.ReleasedQuantity);
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_REFERENCE_NBR, rel4EFor1ADisp.getBlrelReferenceNbr());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_DISPOSITION_CODE, rel4EFor1ADisp.getBlrelDispositionCode());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_NOTES, rel4EFor1ADisp.getBlrelNotes());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_POST_DATE, rel4EFor1ADisp.getBlrelPostDate());
              rel4E1B.setFieldValue(InventoryCargoField.BLREL_REFERENCE, blRelease);
              HibernateApi.getInstance().save(rel4E1B);
            } else {
              BlRelease rel4EFor1BDisp = get4EReleaseForHoldId(blReleases4E, DISP_CODE_1C_HOLD_ID);
              rel4EFor1BDisp.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, blRelease.getBlrelQuantityType());
              rel4EFor1BDisp.setFieldValue(InventoryCargoField.BLREL_REFERENCE, blRelease);
              blRelease.setFieldValue(InventoryCargoField.BLREL_REFERENCE, rel4EFor1BDisp);

            }
            isDisp1AFollowedBy1B = true;
          }
        }
        if (!isDisp1AFollowedBy1B) {
          LOGGER.info("Disp 1B alone exist with out 1A which is practically not correct so nullifying all 4E entries ");
          setBlRelModifyQtyToNullForAllBlReleases(blReleases4E, null);
        }
      } else {
        //this block gets executed if selected bl releases are nither 1A,1B nor 1C.
        LOGGER.info("Either 1A,1b or 1C expected but blrelease with disp code" + blRelease.getBlrelDispositionCode() +
                "is presented wrongly so nullifying all 4E entries");
        setBlRelModifyQtyToNullForAllBlReleases(blReleases4E, null);
      }
    } else {
      LOGGER.info("Neither 1A,1b or 1C are found so nullifying all4E entries");
      setBlRelModifyQtyToNullForAllBlReleases(blReleases4E, null);
    }

    HibernateApi.getInstance().flush();
  }

  //this method Iterate each blrelease from given list and nullify it's modify quantity to null so that release qty will not have any impact on Bl
  //quantities. it skips to nullify if any BlRelease is given to skip.
  private void setBlRelModifyQtyToNullForAllBlReleases(List<BlRelease> inBlRels, BlRelease inToSkipUpdate) {
    for (BlRelease release4E : inBlRels) {
      if (inToSkipUpdate != null && inToSkipUpdate.equals(release4E)) {
        continue;
      }
      release4E.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, null);
      release4E.setFieldValue(InventoryCargoField.BLREL_REFERENCE, release4E); // We will put the reference as itself
    }
    if (!inBlRels.isEmpty()) {
      applyHoldOrReleaseForBl(inBlRels.get(0).getBlrelBl());
    }
  }

  // thsi method iterate each blrelease from list and return the blrelease which matches it's flagtype with given flagtype
  private BlRelease get4EReleaseForHoldId(List<BlRelease> inblReleases, String inHoldId) {
    if (inblReleases != null) {
      for (BlRelease blrel : inblReleases) {
        String holdId = blrel.getBlrelFlagType().getFlgtypId().trim();
        if (inHoldId.trim().equalsIgnoreCase(holdId)) {
          return blrel;
        }
      }
    }
    return null;
  }

  /**
   * This method is being used in afterEdiPost method
   * This method will be be executed if disposition code is 54
   * set the manifest quantity to zero.
   * @param inBl
   */
  private void handle54(BillOfLading inBl) {
    inBl.setFieldValue(InventoryCargoField.BL_MANIFESTED_QTY, Double.valueOf("0.0"));
    HibernateApi.getInstance().update(inBl);
    recordServiceEvent(inBl, RECEIVED_54_STR);
  }

  /**
   * This method is being used in afterEdiPost method
   * This method will be be executed if disposition code is 55
   * Update manifest quantity with the quantity received in the 350 message
   * @param inRelease
   * @param inBl
   */
  private void handle55(ReleaseTransactionDocument.ReleaseTransaction inRelease, BillOfLading inBl) {
    Double qty = getQty(inRelease);
    inBl.setFieldValue(InventoryCargoField.BL_MANIFESTED_QTY, qty);
    recordServiceEvent(inBl, RECEIVED_55_STR);
    HibernateApi.getInstance().update(inBl);
  }

  /**
   * This method is being used in afterEdiPost method
   * Update the Inbond status to CANCEL/INBOND. If manifest qty is less than or equal to inbond qty then inbond status should be INBOND otherwise
   * the status should be CANCEL
   * @param inBl
   */
  private void updateInbondStatus(BillOfLading inBl) {

    Serializable blGkey = inBl.getBlGkey();
    HibernateApi.getInstance().flush();

    //Determine the inbond quantity for BL.
    List<BlRelease> blReleases = findActiveInbond(blGkey);

    Double inbondQtySum = 0;
    List referenceIdList = new ArrayList();
    // Here we will find all the Active 1J's. BlRelease with disposition code 1J is active if BlReleaseReference is NOT populated
    for (BlRelease blRelease : blReleases) {
      String refId = blRelease.getBlrelReferenceNbr();
      //ignore the duplicate 1J
      if ("1J".equalsIgnoreCase(blRelease.getBlrelDispositionCode())) {
        if (referenceIdList.contains(refId)) {
          continue;
        }
        referenceIdList.add(refId);
      }
      // We need to add the quantities for 1J, 1C  disposition codes only for inbond quantity check.
      Double qty = blRelease.getBlrelQuantity();
      if (qty != null) {
        inbondQtySum = inbondQtySum + qty;
      }
    }

    //If InbondQty is equal to manifest quantity, set inbond status to INBOND otherwise CANCEL
    Double blManifestQty = inBl.getBlManifestedQty();
    if (blManifestQty != null) {
      // if the bl release sum inbond quantity is greater than or equal to manifested quantity
      if (blManifestQty <= inbondQtySum) {
        inBl.updateInbond(InbondEnum.INBOND);
      } else {
        inBl.updateInbond(InbondEnum.CANCEL);
      }
    }
    HibernateApi.getInstance().update(inBl);
  }

  /**
   * This method is being used in updateInbondStatus method
   * Find BL Release for given BOL and reference is null
   * @param inBlGkey
   * @return
   */
  private List<BlRelease> findActiveInbond(Serializable inBlGkey) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.in(InventoryCargoField.BLREL_DISPOSITION_CODE, ["1J", "1C", "1W"]))
            .addDqOrdering(Ordering.asc(InventoryCargoField.BLREL_CREATED))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE));
    return HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  /**
   * This method is being used in afterEdiPost method
   * Find Line Operator
   * @param inRelease
   * @return
   */
  private ScopedBizUnit findLineOperator(ReleaseTransactionDocument.ReleaseTransaction inRelease) {
    ShippingLine ediLine = inRelease.getEdiShippingLine();
    if (ediLine != null) {
      String lineCode = ediLine.getShippingLineCode();
      String lineCodeAgency = ediLine.getShippingLineCodeAgency();
      return ScopedBizUnit.resolveScopedBizUnit(lineCode, lineCodeAgency, BizRoleEnum.LINEOP);
    }
    return null;
  }

  /**
   * This method is being used in handle1JUsing95 method
   * Check BlRelease is canceled by another Bl release
   * @param inBlGkey
   * @param inBlRelGkey
   * @return
   */
  private boolean isBlReleaseCanceled(Serializable inBlGkey, Serializable inBlRelGkey) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE, inBlRelGkey));
    return HibernateApi.getInstance().existsByDomainQuery(dq);
  }

  /**
   * This method is being used in handle55 method
   * Get release quantity
   * @param inRelease
   * @return
   */
  private Double getQty(ReleaseTransactionDocument.ReleaseTransaction inRelease) {
    String qtyString = inRelease.getQty();
    Double qty = safeGetDouble(qtyString);
    if (qty == null) {
      String releaseQtyString = inRelease.getReleaseQty();
      qty = safeGetDouble(releaseQtyString);
    }
    if (qty == null) {
      // assume the qty as 0.0
      qty = 0.0;
    }
    return qty;
  }

  /**
   * This method is being used in getQty method
   * convert string to Double
   * @param inNumberString
   * @return
   */
  private Double safeGetDouble(String inNumberString) {
    Double doubleObject = null;
    if (!StringUtils.isEmpty(inNumberString)) {
      try {
        doubleObject = new Double(inNumberString);
      } catch (NumberFormatException e) {
        throw e;
      }
    }
    return doubleObject;
  }

  /**
   * This method is being used in afterEdiPost,handle55, handle54 and handle1JUsing95 methods
   * Record BOL Event
   * @param inBl
   * @param inEventId
   */
  private void recordServiceEvent(BillOfLading inBl, String inEventId) {
    EventType eventType = EventType.findEventType(inEventId);
    FieldChanges fld = new FieldChanges();
    fld.setFieldChange(InventoryCargoField.BL_GKEY, inBl.getBlGkey());
    fld.setFieldChange(InventoryCargoField.BL_NBR, inBl.getBlNbr());
    fld.setFieldChange(InventoryCargoField.BL_INBOND, inBl.getBlInbond());
    if (eventType != null) {
      inBl.recordBlEvent(eventType, fld, "recorded through groovy", null);
    }
  }

  /**
   * This method is being used in handle1JUsing95 method
   * Find bl releases using posting date and disposition code
   * @param inBlGkey
   * @param inDispositionCode
   * @return
   */
  private List<BlRelease> findBlReleases(Serializable inBlGkey, String inDispositionCode) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, inDispositionCode));
    return HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  /**
   * This method is being used in handle1JUsing95 method
   * find active 1J BlReleases
   */
  private List<BlRelease> findActive1J(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction, Serializable inBlGkey,
                                       String inReferenceId) throws BizViolation {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, "1J"));
    boolean isQtyMatchByReferenceNbr = isQtyMatchByReference(inReleaseTransaction);
    if (isQtyMatchByReferenceNbr) {
      dq.addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceId));
    }
    return HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  /**
   * This method is being used in afterEdiPost method
   * Check any active 1J exist or not
   */
  private boolean hasActive1J(Serializable inBlGkey) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, "1J"));
    return HibernateApi.getInstance().existsByDomainQuery(dq);
  }

  /**
   * This method is being used in isQtyMatchByReference method
   * find all release map for given disposition code and message type. Extract the release by BL hold/perm.
   *
   * @param inReleaseTransaction -   Release transaction
   * @param inEdiCodeSet -   Edi Code Set
   * @return IReleaseMap -   release map
   * @throws com.navis.framework.util.BizViolation -   BizViolation
   */
  private IReleaseMap findReleaseMapsFor95(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction) throws BizViolation {
    ArgoEdiFacade ediFacade = (ArgoEdiFacade) Roastery.getBean(ArgoEdiFacade.BEAN_ID);
    String msgId = inReleaseTransaction.getMsgTypeId();
    String msgVersion = inReleaseTransaction.getMsgVersion();
    String msgReleaseNumber = inReleaseTransaction.getMsgReleaseNbr();
    Set ediCodeSet = new HashSet();
    ediCodeSet.add("95");

    List<IReleaseMap> releaseMaps =
      ediFacade.findEdiReleaseMapsForEdiCodes(msgId, ediCodeSet, msgVersion, msgReleaseNumber, LogicalEntityEnum.BL);
    String msg = "Map Code: " + inReleaseTransaction.getEdiCode() + " Message Id: " + msgId + ", Message Version: " + msgVersion +
            ", Release Number: " + msgReleaseNumber;
    if (releaseMaps.isEmpty()) {
      throw BizViolation.create(PropertyKeyFactory.valueOf("Could not find the release map for the condition: " + msg), null);
    }

    if (releaseMaps.size() > 1) {
      throw BizViolation.create(PropertyKeyFactory.valueOf("Found multiple release map for the condition: " + msg), null);
    }
    return releaseMaps.get(0);
  }

  /**
   * This method is being used in handle1JUsing95 method
   * return true if match qty is "Match Qty By Reference" ion release map configuration
   * @param inReleaseTransaction
   * @return
   * @throws BizViolation
   */
  private boolean isQtyMatchByReference(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction) throws BizViolation {
    IReleaseMap releaseMap = findReleaseMapsFor95(inReleaseTransaction);
    return releaseMap == null ? false : EdiReleaseMapQuantityMatchEnum.MatchQtyByReference.equals(releaseMap.getEdirelmapMatchQty());
  }

  //RAMAN:sets the transaction reference nbr to EdiReleaseFlexString01 if reference nbr is not empty and generates an UID(Unique Identifier)
  // and sets UID to transaction reference nbr so that release poster creates multiple holds.

  private void setUniqueReferenceId(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction, String inEdiCode) {
    LOGGER.info(" setUniqueReferenceId(): starts for edi code " + inEdiCode);
    String refId = inReleaseTransaction.getReleaseReferenceId();
    //backup of ReleaseReferenceId if it is not empty.
    if (StringUtils.isNotEmpty(refId)) {
      EdiReleaseFlexFields flexFields = inReleaseTransaction.getEdiReleaseFlexFields();
      //add a new EdiReleaseFlexFields to release transaction if one is not available
      if (flexFields == null) {
        flexFields = inReleaseTransaction.addNewEdiReleaseFlexFields();
      }
      //sets the  ReleaseReferenceId to EdiReleaseFlexString01
      flexFields.setEdiReleaseFlexString01(refId);
    }
    //sets the generated random UID to ReleaseReferenceId
    inReleaseTransaction.setReleaseReferenceId(UUID.randomUUID().toString());
  }

  //RAMAN:sets back the transaction reference nbr which is stored in ReleaseFlexString01 to Bl release entity if ReleaseFlexString01 nbr is not empty, system
  // skips to set back value if ReleaseFlexString01 is empty in this case BL release will have system generated UID as reference nbr
  private void setBackTransactionReferenceId(ReleaseTransactionDocument.ReleaseTransaction inReleaseTransaction, BillOfLading inBillOfLading, String inEdiCode) {
    LOGGER.info(" setBackTransactionReferenceId(): starts for edi code " + inEdiCode);
    EdiReleaseFlexFields flexFields = inReleaseTransaction.getEdiReleaseFlexFields();
    //skip to revert back the getEdiReleaseFlexString01 is empty so BL release will have system generated UID as reference nbr
    if (flexFields != null && flexFields.getEdiReleaseFlexString01() != null) {
      BlRelease blrel = findLatestBlReleaseForDispCodeAndBL(inBillOfLading.getBlGkey(), inEdiCode);
      if (blrel != null) {
        blrel.setFieldValue(InventoryCargoField.BLREL_REFERENCE_NBR, flexFields.getEdiReleaseFlexString01())
        LOGGER.info("setBackTransactionReferenceId() :" + flexFields.getEdiReleaseFlexString01() + "is set back to ediCode: " + inEdiCode);
      } else {
        LOGGER.info("setBackTransactionReferenceId() : blrel is null !");
      }
    } else {
      LOGGER.info(
              "setBackTransactionReferenceId() : value of flexFields.getEdiReleaseFlexString01() is empaty so systed did not revert back the unique reference id");
    }
  }

  //RAMAN: find BlRelease Latest BL Release for given Bl and disposition code desc order
  private BlRelease findLatestBlReleaseForDispCodeAndBL(Serializable inBlGkey, String inDispositionCode) {

    LOGGER.info("findLatestBlReleaseForDispCodeAndBL inBlGkey" + inBlGkey + "inDispositionCode :" + inDispositionCode);
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE);
    dq.addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
    dq.addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, inDispositionCode));
    dq.addDqOrdering(Ordering.desc(InventoryCargoField.BLREL_CREATED))
    dq.addDqOrdering(Ordering.desc(InventoryCargoField.BLREL_GKEY));
    List<BlRelease> blreleaseList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
    return !blreleaseList.isEmpty() ? blreleaseList.get(0) : null;
  }

  private void handleEnteredQtyFor4C(BillOfLading inBl, String inEdiCode) throws BizViolation {
    log(" Entered handleEnteredQtyFor4C ...");
    HibernateApi.getInstance().flush();
    //4C should subtract from the entered qty
    List<BlRelease> blReleases = findValidBlReleases(inBl.getBlGkey(), inEdiCode);
    for (BlRelease release : blReleases) {
      com.navis.argo.business.atoms.EdiReleaseMapModifyQuantityEnum qtyType = release.getBlrelQuantityType();
      String refNbr = release.getBlrelReferenceNbr();
      if (qtyType.equals(com.navis.argo.business.atoms.EdiReleaseMapModifyQuantityEnum.EnteredQuantity)) {
        //find if there is a 1A with matching reference number

        BlRelease validRelease = findValidBlReleaseWithRefNbr(inBl.getBlGkey(), refNbr);
        if (validRelease != null) {
          release.setBlrelQuantity(-1 * (release.getBlrelQuantity()));
        } else {
          release.setBlrelQuantityType(null);
        }
        HibernateApi.getInstance().update(release);
        applyHoldOrReleaseForBl(inBl);
      }
    }
  }

  private BlRelease findValidBlReleaseWithRefNbr(Serializable inBlGkey, String inReferenceNumber) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceNumber))
            .addDqPredicate(PredicateFactory.isNotNull(InventoryCargoField.BLREL_QUANTITY_TYPE))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, "1A"))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE));

    return HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
  }

  //This method checks if Prior 1B,1C or 4C release exist for given 5H reference, If does not exist then changes 5H quantity type to null
  private void handle5HLogicAfterPost(BillOfLading inBl, String inEdiCode) throws BizViolation {
    log("handle5HLogicAfterPost(): starts");

    //gets the 5H release that is just posted
    BlRelease blRelease5H = findLatestBlReleaseForDispCodeAndBL(inBl.getBlGkey(), inEdiCode);

    //exceptional case to avoid NPE when system unable to find 5H release
    if (blRelease5H == null) {
      log("Could not find latest 5H release so returning");
      return;
    }

    //gets the reference nbr from 5H release
    String referenceNumber = blRelease5H.getBlrelReferenceNbr();
    //checks if matching 1C,1B or 4C release exist for 5H reference nbr
    boolean matching1C1B4C = find1C1B4CForBLAndRefNbr(inBl.getBlGkey(), referenceNumber);

    //Nullify the 5H release qty type and reevaluate terminal hold,if prior 1C, 1B or 4C does not exist
    if (!matching1C1B4C) {
      blRelease5H.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, null);
      HibernateApi.getInstance().update(blRelease5H);
      applyHoldOrReleaseForBl(inBl);
    }
    log("handle5HLogicAfterPost(): ends");
  }

  //This method checks if Prior 1B,1C or 4C release exist for given 5I reference, If does not exist then changes 5I quantity type to null
  private void handle5ILogicAfterPost(BillOfLading inBl, String inEdiCode) throws BizViolation {

    log("handle5ILogicAfterPost(): starts");

    //gets the 5I release that is just posted
    BlRelease blRelease5I = findLatestBlReleaseForDispCodeAndBL(inBl.getBlGkey(), inEdiCode);

    //exceptional case to avoid NPE when system unable to find 5I release
    if (blRelease5I == null) {
      log("Could not find latest 5I release so returning");
      return;
    }

    //gets the reference nbr from 5I release
    String referenceNumber = blRelease5I.getBlrelReferenceNbr();
    //checks if matching 1C or 1B release exist for 5I reference nbr
    boolean matching1C1B4C = find1C1B4CForBLAndRefNbr(inBl.getBlGkey(), referenceNumber);

    // Nullify the 5I release qty type and reevaluate terminal hold,if prior 1C or 1B does not exist
    if (!matching1C1B4C) {
      blRelease5I.setFieldValue(InventoryCargoField.BLREL_QUANTITY_TYPE, null);
      HibernateApi.getInstance().update(blRelease5I);
      applyHoldOrReleaseForBl(inBl);
    }
    log("handle5ILogicAfterPost(): ends");
  }

  //This method return true if 1C,1B or 4C release exist for the given blgeky and referenceId
  private boolean find1C1B4CForBLAndRefNbr(Serializable inBlGkey, String inReferenceId) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.in(InventoryCargoField.BLREL_DISPOSITION_CODE, ["1C", "1B", "4C"]))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceId));
    return HibernateApi.getInstance().existsByDomainQuery(dq);
  }

  private void makeEnteredQtyPositive(BillOfLading inBl, String inEdiCode) throws BizViolation {
    HibernateApi.getInstance().flush();
    log("*** Entered makeEnteredQtyPositive ... ");
    log("EdiCode : " + inEdiCode);
    List<BlRelease> blReleases = findValidBlReleases(inBl.getBlGkey(), inEdiCode);
    log("Total Bl Releases : " + blReleases.size());
    String referenceNumber = null;
    BlRelease matchingRelease = null;
    for (BlRelease release : blReleases) {
      String ediCode = release.getBlrelDispositionCode();
      com.navis.argo.business.atoms.EdiReleaseMapModifyQuantityEnum qtyType = release.getBlrelQuantityType();
      log(" Qty Type : " + qtyType);
      if (referenceNumber == null) {
        referenceNumber = release.getBlrelReferenceNbr(); // all the BL Releases for the given ediCode will have same reference number
        matchingRelease = findValidBlReleaseToCancelWithRefNbr(inBl.getBlGkey(), referenceNumber);
      }
      if (matchingRelease != null) {
        String holdId = release.getBlrelFlagType().getFlgtypId().trim();
        if (StringUtils.isNotEmpty(holdId) && holdId.equals("1A")) {
          if (qtyType == null) {
            release.setBlrelQuantityType(com.navis.argo.business.atoms.EdiReleaseMapModifyQuantityEnum.ReleasedQuantity);
            _sm.applyHold(holdId, inBl, null, referenceNumber, holdId);
          }
        }
        if (qtyType != null && qtyType.equals(com.navis.argo.business.atoms.EdiReleaseMapModifyQuantityEnum.EnteredQuantity)) {
          release.setBlrelQuantity(-1 * (release.getBlrelQuantity()));
          log(" Made the entered quantity positive ..");

        }
        release.setBlrelReference(matchingRelease);
        matchingRelease.setBlrelReference(release);
        HibernateApi.getInstance().update(release);
        HibernateApi.getInstance().update(matchingRelease);
        HibernateApi.getInstance().flush();
      } else {
        release.setBlrelQuantityType(null);
        release.setBlrelReference(release);
        HibernateApi.getInstance().update(release);
        HibernateApi.getInstance().flush();

      }

    }
    log("*** Exited makeEnteredQtyPositive ... ");
  }

  private List<BlRelease> findValidBlReleases(Serializable inBlGkey, String inDispositionCode) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_DISPOSITION_CODE, inDispositionCode));
    return HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  private BlRelease findValidBlReleaseToCancelWithRefNbr(Serializable inBlGkey, String inReferenceNumber) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryCargoEntity.BL_RELEASE)
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_BL, inBlGkey))
            .addDqPredicate(PredicateFactory.eq(InventoryCargoField.BLREL_REFERENCE_NBR, inReferenceNumber))
            .addDqPredicate(PredicateFactory.isNotNull(InventoryCargoField.BLREL_QUANTITY_TYPE))
            .addDqPredicate(PredicateFactory.in(InventoryCargoField.BLREL_DISPOSITION_CODE, ["1B", "1C"]))
            .addDqPredicate(PredicateFactory.isNull(InventoryCargoField.BLREL_REFERENCE));
    List<BlRelease> releases = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
    if (!releases.isEmpty()) {
      return (BlRelease) releases.get(0);
    }


    return null;
  }

  private void applyHoldOrReleaseForBl(BillOfLading inBl) {
    HibernateApi.getInstance().flush();
    double totalReleased = inBl.getBlReleasedQty();
    inBl.applyHoldOrReleaseForBl(totalReleased);
  }

  //Need to update Hold Permission id that is given for 1C release map
  private final String DISP_CODE_1C_HOLD_ID = "CUSTOMS DEFAULT HOLD";
  private final String DISP_CODE_1A = "1A";
  private final String DISP_CODE_1B = "1B";
  private final String DISP_CODE_1C = "1C";
  private final String DISP_CODE_4E = "4E";

  private final String ADD_EVENT_1J_STR = "RECEIVED_1J";
  private final String SKIP_POSTER = "SKIP_POSTER";
  private final String CANCELED_EVENT_1J_STR = "CANCELED_1J";
  private final String RECEIVED_54_STR = "RECEIVED_54";
  private final String RECEIVED_55_STR = "RECEIVED_55";
  private List<String> DISPOSITION_CODES_FOR_UNIQUE_ID = new ArrayList<String>();

  {
    EventType.findOrCreateEventType(ADD_EVENT_1J_STR, "Received 1J", LogicalEntityEnum.BL, null);
    EventType.findOrCreateEventType(CANCELED_EVENT_1J_STR, "1J Canceled Event", LogicalEntityEnum.BL, null);
    EventType.findOrCreateEventType(RECEIVED_54_STR, "Received 54", LogicalEntityEnum.BL, null);
    EventType.findOrCreateEventType(RECEIVED_55_STR, "Received 55", LogicalEntityEnum.BL, null);

    //Include the Disposition code here if it requires multiple hold and if there is no guaranteed unique reference nbr in input file
    //Please do not add disposition codes that requires just release the hold example: 7I,1I...etc
/*    DISPOSITION_CODES_FOR_UNIQUE_ID.add("1H");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2G");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2H");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2O");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2P");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2Q");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("2R");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("3G");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("3H");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("5H");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("6H");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("71");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("72");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("73");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("77");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("78");
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("79");*/
    DISPOSITION_CODES_FOR_UNIQUE_ID.add("7H");
  }
  private ServicesManager _sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
  private static final Logger LOGGER = Logger.getLogger(USCustomsBLReleaseGvy.class);
}