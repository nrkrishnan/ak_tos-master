package groovies

import com.navis.external.road.AbstractGateTaskInterceptor
import com.navis.external.road.EGateTaskInterceptor
import com.navis.framework.AllOtherFrameworkPropertyKeys
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.BizViolation
import com.navis.framework.util.message.MessageLevel
import com.navis.inventory.InventoryField
import com.navis.inventory.business.imdg.HazardItem
import com.navis.inventory.business.imdg.HazardousGoods
import com.navis.inventory.business.imdg.ImdgClass
import com.navis.inventory.business.imdg.Placard
import com.navis.inventory.business.units.GoodsBase
import com.navis.inventory.business.units.Unit
import com.navis.orders.business.eqorders.Booking
import com.navis.road.business.model.TransactionPlacard
import com.navis.road.business.model.TruckTransaction
import com.navis.road.business.util.RoadBizUtil
import com.navis.road.business.workflow.TransactionAndVisitHolder
import org.apache.log4j.Level
import org.apache.log4j.Logger

/**
 * <p>Class MatsonAncValidatePlacardsWithBookingHazards is for validating the placards,
 * It's Useful where the Tran Unit has hazards</p>
 * <p><b>Note:</b>Execution order of this task is after MATApplyUnitHazards</p>
 */
class MatsonAncValidatePlacardsWithBookingHazards extends AbstractGateTaskInterceptor implements EGateTaskInterceptor {
    private static final Logger LOGGER = Logger.getLogger(MatsonAncValidatePlacardsWithBookingHazards.class);
    static final String ERROR = "ERROR";
    static final String WARNING = "WARNING";
    public static final String BOOKING_NOT_AVAILABLE_IN_TRANSACTION = "Booking is not available in the Transaction";
    public static final String UNIT_NOT_AVAILABLE_IN_TRANSACTION = "Unit is not available in the Transaction";
    public static final String GOODS_NOT_AVAILABLE_IN_TRANSACTION_UNIT = "Goods is not available in the Transaction Unit";
    static final String LTD_QTY_TEXT = "LTD QTY";
    static final String DNGR = "DNGR";
    static final String MARINE_POLL = "MARINE POLL";
    static final List<Placard> NOT_TO_BE_VALIDATED_PLACARD = new ArrayList<Placard>();
    static final List<Placard> MARINE_POLL_AND_DNGR = new ArrayList<Placard>();


    @Override
    void postProcess(TransactionAndVisitHolder inTransactionAndVisitHolder) {
        super.postProcess(inTransactionAndVisitHolder)
        LOGGER.setLevel(Level.DEBUG);
        LOGGER.info("Begin MatsonAncValidatePlacardsWithBookingHazards.postProcess after super.postProcess(inTransactionAndVisitHolder)");
        if (this.hasError()) {
            //todo are validating as the transaction already errored out.
        }
        TruckTransaction truckTransaction = inTransactionAndVisitHolder.getTran();
        LOGGER.info("TruckTransaction\t" + truckTransaction);
        Unit unit = truckTransaction.getTranUnit();
        LOGGER.info("Unit\t" + unit);
        Object[] errorObjects = new Object[1];
        errorObjects[0]=unit;
        if(unit == null){
            RoadBizUtil.appendExceptionChainAsWarnings(BizViolation.create(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE,
                    null, "The Tran Unit is not available in the Transaction Object"));
            LOGGER.error(UNIT_NOT_AVAILABLE_IN_TRANSACTION + ", returning with SEVERE message");
            return;
        }
        GoodsBase goodsBase = unit.getUnitGoods();
        if(goodsBase == null){
            RoadBizUtil.appendExceptionChainAsWarnings(BizViolation.create(AllOtherFrameworkPropertyKeys.ERROR__NULL_MESSAGE,
                    null, "The Tran Unit - Goods details are not available in the Transaction Object"));
            LOGGER.error(GOODS_NOT_AVAILABLE_IN_TRANSACTION_UNIT + ", returning with SEVERE message");
            return;
        }else{



            /**
             * If Transaction Unit do not have any hazards, then return
             * If Transaction Unit has Hazards and Transaction do not have any Placards, then Return with Error Message
             * else, process further
             */
            if (goodsBase.getGdsHazards() != null && goodsBase.getGdsHazards().getHazardItemsIterator() != null && goodsBase.getGdsHazards().getHazardItemsIterator().hasNext()) {
                LOGGER.info("Transaction Unit " + unit.getUnitId() + " have Hazard Item/Items");
                if (truckTransaction.getTranPlacards() == null || (truckTransaction.getTranPlacards() != null && truckTransaction.getTranPlacards().size() == 0)) {
                    Boolean isAllItemLtdQty = isAllBookingHazardItemLtdQty(goodsBase);
                    if(!isAllItemLtdQty) {
                        this.getMessageCollector().appendMessage(BizFailure.create("The Transaction do not have any Associate Placards, Please Review"));
                        LOGGER.error("The Transaction do not have any Associate Placards, Please Review");
                        return;
                    }
                    else{
                        LOGGER.error("Although there is no placard in transaction, it can be evaluated further, as all the Booking item is of Limited Quantity");
                    }
                }
            } else {
                LOGGER.info("Transaction Unit " + unit.getUnitId() + " do not have Hazard Item");
                //return;
            }

            /**
             *      Transaction Placards        Booking is Hazardous    Next Step
             *      Yes                         Yes                     Validate Further
             *      Yes                         No                      Error, Transaction has Placards, but booking is not Hazardous & Break
             *      No                          Yes                     Error, Hazardous Booking, Missing Placards & Break
             *      No                          No                      Break, End Go to Next Task
             */
            Boolean isBookingHazardous = Boolean.FALSE;
            Boolean isTransactionPlacarded = Boolean.FALSE;
            isBookingHazardous = goodsBase.getGdsHazards() != null && goodsBase.getGdsHazards().getHazardItemsIterator() != null && goodsBase.getGdsHazards().getHazardItemsIterator().hasNext();
            isTransactionPlacarded = truckTransaction.getTranPlacards() != null && truckTransaction.getTranPlacards().size() > 0;

            if (isTransactionPlacarded && isBookingHazardous) {
                //Process further, no need to break
            } else if (isTransactionPlacarded && !isBookingHazardous) {
                this.getMessageCollector().appendMessage(BizFailure.create("The Booking do not have any hazards,But the transaction have hazards.Please Review"));
                LOGGER.error("The Booking do not have any hazards,But the transaction have hazards.Please Review");
                return;
            } else if (!isTransactionPlacarded && isBookingHazardous) {
                Boolean isAllBookingItemLtdQty = isAllBookingHazardItemLtdQty(goodsBase);
                if (!isAllBookingItemLtdQty) {
                    this.getMessageCollector().appendMessage(BizFailure.create("The Transaction do not have any Associate Placards, Please Review"));
                    LOGGER.error("The Transaction do not have any Associate Placards, Please Review");
                    return;
                } else {
                    LOGGER.error("All booking hazard items are Limited Quantity, so proceed validation of limited quantity");
                    //todo introduce validation here, may be
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has all the items as Ltd Qty. The Ltd Qty Placard is required"));
                    return;
                }
            } else if (!isTransactionPlacarded && !isBookingHazardous) {
                LOGGER.info("Transaction Unit " + unit.getUnitId() + " do not have Hazard Item, and Transaction do not have placards.");
                return;
            }

            LOGGER.info("Return value for goodsBase.getGdsIsHazardous()\t" + goodsBase.getGdsIsHazardous());
            Set<ImdgClass> imdgClassFromBooking = new HashSet<>();
            List<HazardItem> hazardItemSet = new ArrayList<>();
            Set<String> uniqueHazardsPlacardsEquivalentFromBooking = new HashSet<>();
            Iterator<HazardItem> bookingHazardItemIterator = goodsBase.getGdsHazards().getHazardItemsIterator();
            LOGGER.info("booking.getEqoHazards().getHazardItemsIterator() size\t" + bookingHazardItemIterator.size());
            LOGGER.info("Looping Through Hazard Items From Booking");
            for (Iterator<HazardItem> hazardsItemIterator = goodsBase.getGdsHazards().getHazardItemsIterator(); hazardsItemIterator.hasNext();) {
                LOGGER.info("Looping Through Hazard Items From Booking, picking Items using Iterator");
                HazardItem hazardItem = hazardsItemIterator.next();
                hazardItem.getHzrdiLtdQty();//todo, use this for checking
                hazardItemSet.add(hazardItem);
                LOGGER.info("HazardItem" + hazardItem);
                LOGGER.info("hazardItem.getHzrdiImdgClass()" + hazardItem.getHzrdiImdgClass());
                LOGGER.info("hazardItem.getHzrdiImdgCode()" + hazardItem.getHzrdiImdgCode());
                LOGGER.info("hazardItem.getHzrdiLtdQty()" + hazardItem.getHzrdiLtdQty());
                if (hazardItem.getHzrdiImdgClass() != null && !hazardItem.getHzrdiLtdQty()) {
                    imdgClassFromBooking.add(hazardItem.getHzrdiImdgClass());
                    uniqueHazardsPlacardsEquivalentFromBooking.add(getEquivalentPlacardForHazClass(hazardItem.getHzrdiImdgClass().getKey()));
                    LOGGER.info("hazardItem.getHzrdiImdgClass().getKey()" + hazardItem.getHzrdiImdgClass().getKey());
                    LOGGER.info("And it's equivalent Placard is\t" + getEquivalentPlacardForHazClass(hazardItem.getHzrdiImdgClass().getKey()));
                }

            }

            Set<TransactionPlacard> tranPlacards = truckTransaction.getTranPlacards();
            // Null Check NOT Required Here, as it's done in  previous Block
            LOGGER.info("truckTransaction.getTranPlacards().size()" + truckTransaction.getTranPlacards().size());
            Set<Placard> tranPlacardPlacards = new HashSet<>(tranPlacards.size());
            for (TransactionPlacard placard : tranPlacards) {
                tranPlacardPlacards.add(placard.getTranplacardPlacard());
            }

            Placard placardsLtdQTY = Placard.findPlacard(LTD_QTY_TEXT);
            Placard placardsMarinePollutants = Placard.findPlacard(MARINE_POLL);

            if (isTransactionPlacarded && isBookingHazardous && imdgClassFromBooking.size() == 0) {
                Boolean isAllBookingItemLtdQty = isAllBookingHazardItemLtdQty(goodsBase);
                Set<Placard> localTranPlacardsWithoutMarinePollDngr = new HashSet<>(tranPlacardPlacards);
                localTranPlacardsWithoutMarinePollDngr.removeAll(MARINE_POLL_AND_DNGR);

                if (isAllBookingItemLtdQty && localTranPlacardsWithoutMarinePollDngr.size() > 0 && placardsLtdQTY != null &&
                        (!localTranPlacardsWithoutMarinePollDngr.contains(placardsLtdQTY) ||
                            (localTranPlacardsWithoutMarinePollDngr.size() > 1 && localTranPlacardsWithoutMarinePollDngr.contains(placardsLtdQTY)))) {
                        this.getMessageCollector().appendMessage(BizFailure.create("The Transaction booking has all the items as Ltd Qty. " +
                                "And placarding it placards other than Ltd Qty violates the requirement, please validate"));
                } else if (isAllBookingItemLtdQty && localTranPlacardsWithoutMarinePollDngr.size() > 0 && localTranPlacardsWithoutMarinePollDngr.size() == 1
                        && placardsLtdQTY != null && localTranPlacardsWithoutMarinePollDngr.contains(placardsLtdQTY)) {
                    LOGGER.info("All the items are Ltd Qty and the Unit is placarded with Ltd Qty");
                    if (!isAnyBookingHazardItemMarinePollutant(goodsBase)) {
                        return;
                    }
                } else if (placardsLtdQTY == null) { // this part f code should never be hit, else fix the Placards in N4 Placards tab
                    LOGGER.error("Problem with finding the value for Lty Qty Placard");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has all the items as Ltd Qty. The Ltd Qty Placard is not available in N4, please contact the administrator to fix this issue"));
                } else if(isAllBookingItemLtdQty && localTranPlacardsWithoutMarinePollDngr.size() == 0) {
                    LOGGER.error("The transaction booking has all the hazards as Lty Qty");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has all the items as Ltd Qty. The Ltd Qty Placard is required"));
                } else {
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has all the items as Ltd Qty. The transaction is placarded with wrong placard, please contact the administrator to fix this issue"));
                    return;
                }
            }
            if(tranPlacardPlacards.contains(placardsLtdQTY) && !isAnyBookingHazardItemLtdQty(goodsBase) ){
                this.getMessageCollector().appendMessage(BizFailure.create(
                        "The Transaction booking has None of the items marked as Ltd Qty. " +
                                "The transaction is placarded with Ltd Qty, Please Remove it"));

            }

            /**
             * If Marine Pollutant enabled in booking
             */

            Boolean isAnyBookingItemMarinePollutant = isAnyBookingHazardItemMarinePollutant(goodsBase);
            if (isAnyBookingItemMarinePollutant) {
                LOGGER.info("The Booking has item marked as Marine Pollutant, Validating for the placard.");
                if (placardsMarinePollutants != null && tranPlacardPlacards.contains(placardsMarinePollutants)) {
                    // continue, valid flow
                    LOGGER.info("The Transaction booking has an item as Marine Pollutant.The Marine Pollutant Placard is also available");
                } else if (placardsMarinePollutants != null && !tranPlacardPlacards.contains(placardsMarinePollutants)) {
                    // Error
                    LOGGER.error("The transaction booking has an Item as Marine Pollutant");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has an item as Marine Pollutant.The Marine Pollutant Placard is required"));
                    //return;
                } else if(placardsMarinePollutants == null){
                    LOGGER.error("Problem with finding the value for Marine Pollutants Placard");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking has an item as Marine Pollutant." +
                                    "The Marine Pollutant Placard is not available in N4, please contact the administrator to fix this issue"));
                    return;
                }
                LOGGER.info("The Booking has item marked as Marine Pollutant, Validating for the placard ends.");
            } else if (!isAnyBookingItemMarinePollutant) {
                if (placardsMarinePollutants != null && tranPlacardPlacards.contains(placardsMarinePollutants)) {
                    // invalid flow
                    LOGGER.error("The transaction booking do not have Item as Marine Pollutant");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction booking donot have item Marine Pollutant. But the transaction is Placarded with Marine Pollutant, Please review"));
                } else if(placardsMarinePollutants == null){
                    LOGGER.error("Problem with finding the value for Marine Pollutants Placard");
                    this.getMessageCollector().appendMessage(BizFailure.create(
                            "The Transaction cannot be verified against Marine Pollutant." +
                                    "The Marine Pollutant Placard is not available in N4, please contact the administrator to fix this issue"));
                    return;
                }
            }

            /*
             *   at this point, the imdgClassFromBooking has items in it
             */

            // imdgClassFromBooking & tranPlacardPlacards
            List<ImdgClass> imdgClassFromBookingAsList = new ArrayList<>();
            imdgClassFromBookingAsList.addAll(imdgClassFromBooking);

            List<Placard> tranPlacardPlacardsAsList = new ArrayList<>();
            tranPlacardPlacardsAsList.addAll(tranPlacardPlacards);
            List<Placard> tranPlacardPlacardsToValidate = new ArrayList<>(tranPlacardPlacardsAsList);
            tranPlacardPlacardsToValidate.removeAll(NOT_TO_BE_VALIDATED_PLACARD); //todo, there are placards like MARINE POLL, DNGR, may not be required to be validated against IMDG
            Set<ImdgClass> hazardsNotInTransactionErrorLevel = isPlacardAvailable(ERROR, tranPlacardPlacardsToValidate, imdgClassFromBookingAsList);
            Set<ImdgClass> hazardsNotInTransactionWarnLevel = isPlacardAvailable(WARNING, tranPlacardPlacardsToValidate, imdgClassFromBookingAsList);
            if (hazardsNotInTransactionErrorLevel.size() > 0) {
                String errorMessage = "";
                for (ImdgClass imdgClass : hazardsNotInTransactionErrorLevel) {
                    errorMessage += imdgClass.getKey() + " , ";
                }

                final BizFailure bizFailure = BizFailure.create("The Transaction missing the following Placard equivalent for IMDG classes " + errorMessage);
                LOGGER.error("The Transaction missing the following Placard equivalent for IMDG classes " + errorMessage);
                this.getMessageCollector().appendMessage(bizFailure);
            }
            hazardsNotInTransactionWarnLevel.removeAll(hazardsNotInTransactionErrorLevel)
            if (hazardsNotInTransactionWarnLevel.size() > 0) {
                String warningMessage = "";
                for (ImdgClass imdgClass : hazardsNotInTransactionWarnLevel) {
                    warningMessage += imdgClass.getKey() + " , ";
                }
                LOGGER.error("The Transaction has the following Placards missing (although Equivalents Available)" + warningMessage + ", Please validate, And you can override if Necessary");
                this.getMessageCollector().appendMessage(BizFailure.create("The Transaction has the following Placards missing (although Equivalents Available) " + warningMessage + ", Please validate, And you can override if Necessary"));
            }

            Set<Placard> placardsNotRequired = computeUnNecessaryPlacards(tranPlacardPlacardsToValidate, imdgClassFromBookingAsList);
            if (placardsNotRequired.size() > 0) {
                String placardNotRequiredString = "";
                for (Placard inPlacard : placardsNotRequired) {
                    placardNotRequiredString += inPlacard.toString() + ",";
                }
                LOGGER.error("The following Placards in the Transaction may not be necessary" + placardNotRequiredString + ". Please Review");
                this.getMessageCollector().appendMessage(BizFailure.create("The following Placards in the Transaction may not be necessary" + placardNotRequiredString + ". Please Review"));
            }
        }

    }

    Boolean isAllBookingHazardItemLtdQty(GoodsBase inGoodsBase) {
        Boolean isAllHazardItemLtdQty = Boolean.TRUE;
        for (Iterator<HazardItem> hazardsItemIterator = inGoodsBase.getGdsHazards().getHazardItemsIterator(); hazardsItemIterator.hasNext();) {
            HazardItem hazardItem = hazardsItemIterator.next();
            LOGGER.info("hazardItem.getHzrdiLtdQty()" + hazardItem.getHzrdiLtdQty());
            if (hazardItem.getHzrdiImdgClass() != null && !hazardItem.getHzrdiLtdQty()) {
                isAllHazardItemLtdQty = Boolean.FALSE;
            }
        }
        LOGGER.info("The booking " + inGoodsBase + " is all hazard items are Ltd Qty?" + isAllHazardItemLtdQty);
        return isAllHazardItemLtdQty;
    }

    Boolean isAnyBookingHazardItemLtdQty(GoodsBase inGoodsBase) {
        Boolean isAnyItemLimitedQuantity = Boolean.FALSE;
        for (Iterator<HazardItem> hazardsItemIterator = inGoodsBase.getGdsHazards().getHazardItemsIterator(); hazardsItemIterator.hasNext();) {
            HazardItem hazardItem = hazardsItemIterator.next();
            LOGGER.info("hazardItem.getHzrdiLtdQty()" + hazardItem.getHzrdiLtdQty());
            if (hazardItem.getHzrdiImdgClass() != null && hazardItem.getHzrdiLtdQty()) {
                isAnyItemLimitedQuantity = Boolean.TRUE;
            }
        }
        LOGGER.info("The booking " + inGoodsBase + " is any hazard items are Ltd Qty?" + isAnyItemLimitedQuantity);
        return isAnyItemLimitedQuantity;
    }


    Boolean isAnyBookingHazardItemMarinePollutant(GoodsBase inGoodsBase) {
        Boolean isAnyItemMarinePollutant = Boolean.FALSE;
        for (Iterator<HazardItem> hazardsItemIterator = inGoodsBase.getGdsHazards().getHazardItemsIterator(); hazardsItemIterator.hasNext();) {
            HazardItem hazardItem = hazardsItemIterator.next();
            LOGGER.info("hazardItem.getHzrdiMarinePollutants()" + hazardItem.getHzrdiMarinePollutants());
            if (hazardItem.getHzrdiImdgClass() != null && hazardItem.getHzrdiMarinePollutants()) {
                isAnyItemMarinePollutant = Boolean.TRUE;
            }
        }
        LOGGER.info("The booking " + inGoodsBase + " has any hazard items are Marine Pollutant?" + isAnyItemMarinePollutant);
        return isAnyItemMarinePollutant;
    }

    Set<Placard> computeUnNecessaryPlacards(ArrayList<Placard> inPlacardList, ArrayList<ImdgClass> inImdgClassList) {
        List<Placard> notInBookingPlacard = new ArrayList<>();
        List<ImdgClass> notInTransactionPlacardsForImdg = new ArrayList<>(inImdgClassList.size());
        notInTransactionPlacardsForImdg.addAll(inImdgClassList);
        for (Placard placard : inPlacardList) {
            Set<ImdgClass> placardEquivalentImdgClassList = getImdgForPlacard(WARNING, placard);
            List<ImdgClass> inImdgClassListCopy = new ArrayList<>(inImdgClassList.size());
            for (ImdgClass imdgClass2 : placardEquivalentImdgClassList) {
                LOGGER.info("Plcard IMDG is " + imdgClass2 + " and it's BaseTrait key is " + imdgClass2.getBaseTrait());
            }
            for (ImdgClass imdgClass2 : inImdgClassList) {
                LOGGER.info("Booking IMDG is " + imdgClass2 + " and it's BaseTrait key is " + imdgClass2.getBaseTrait());
            }
            inImdgClassListCopy.addAll(inImdgClassList);
            for (ImdgClass imdgClass : placardEquivalentImdgClassList) {
                /**
                 * if the inImdgClassList has an explosive class, ie., getCompatibilityGroupTrait, then that needs to be taken into account
                 */
                for (ImdgClass imdgClass1 : inImdgClassList) {
                    if (imdgClass.getBaseTrait() == imdgClass1.getBaseTrait()) {
                        inImdgClassListCopy.remove(imdgClass1);
                        notInTransactionPlacardsForImdg.remove(imdgClass1);
                    }
                }
            }
            if (inImdgClassListCopy.size == inImdgClassList.size()) {
                // the placard is not useful
                notInBookingPlacard.add(placard);
            } else if (inImdgClassListCopy.size < inImdgClassList.size()) {
                // the placard has equivalent IMDG class
            }
        }
        Set<Placard> tempSet = new HashSet<>();
        tempSet.addAll(notInBookingPlacard);
        return tempSet;
    }

    @Deprecated
    Set<Placard> getEquivalentPlacardsFromUNNo(List<HazardItem> hazardItems) {
        LOGGER.info("getEquivalentPlacardsFromUNNo(List<HazardItem> hazardItems) list size is  " + hazardItems.size());
        Set<Placard> placardSet = new HashSet<>();
        for (HazardItem hazardItem : hazardItems) {
            Boolean isDerivedFromUNNumber = Boolean.FALSE;
            if (hazardItem.getHzrdiUNnum() != null) {
                LOGGER.info("The HazardItem " + hazardItem + " has UN Number of " + hazardItem.getHzrdiUNnum());
                HazardousGoods goods = findHazardousGoods(hazardItem.getHzrdiUNnum());
                if (goods != null) {
                    LOGGER.info("There is equivalent HazardousGoods Defined for " + hazardItem.getHzrdiUNnum());

                    if (goods.getHzgoodsPlacard() != null) {
                        LOGGER.info("There is equivalent HazardousGoods Defined for " + hazardItem.getHzrdiUNnum() + "With Placard " + goods.getHzgoodsPlacard().getPlacardText());
                        isDerivedFromUNNumber = Boolean.TRUE;
                        //todo, there is flag goods.getHzgoodsIsPlacardOptional() (may need to make use of it)
                        placardSet.add(goods.getHzgoodsPlacard());
                    }

                }
            }
            if (!isDerivedFromUNNumber && hazardItem.getHzrdiImdgClass() != null) {
                LOGGER.info("There is no UN Equivalent defined, so defaulting to derivation from IMDG class ");
                LOGGER.info("Deriving for IMDG Class " + hazardItem.getHzrdiImdgClass());
                placardSet.addAll(getEquivalentPlacards(hazardItem.getHzrdiImdgClass()));
            }
        }
        return placardSet;
    }

    @Deprecated
    Set<String> getEquivalentPlacardsFromHazGoods(HashSet<ImdgClass> inImdgClass) {
        Set<Placard> placardSet = new HashSet<>(inImdgClass.size());
        for (ImdgClass imdgClass : inImdgClass) {
            placardSet.addAll(getEquivalentPlacards(imdgClass));
        }
        return placardSet;
    }

    private static String getEquivalentPlacardForHazClass(String inHazClass) {


        if (inHazClass.trim().equals("1"))
            return "1";
        else if (inHazClass.startsWith("1.1") || inHazClass.startsWith("1.2") || inHazClass.startsWith("1.3"))
            return "1";
        else if (inHazClass.startsWith("1.4"))
            return "1.4";
        else if (inHazClass.startsWith("1.5"))
            return "1.5";
        else if (inHazClass.startsWith("1.6"))
            return "1.6";
        else if (inHazClass.startsWith("2"))
            return "2";
        else if (inHazClass.startsWith("3"))
            return "3";
        else if (inHazClass.trim().equals("4"))
            return "4";
        else if (inHazClass.trim().equals("4.1"))
            return "4.1";
        else if (inHazClass.trim().equals("4.2"))
            return "4.2";
        else if (inHazClass.trim().equals("4.3"))
            return "4.3";
        else if (inHazClass.trim().equals("5.1"))
            return "5.1";
        else if (inHazClass.trim().equals("5.2"))
            return "5.2";
        else if (inHazClass.trim().startsWith("6"))
            return "6";
        else if (inHazClass.trim().equals("7"))
            return "7";
        else if (inHazClass.trim().equals("8"))
            return "8";
        else if (inHazClass.trim().equals("9"))
            return "9";
        else if (inHazClass.trim().equals("N/A"))
            return "OPTIONAL";

    }

    private static List<HazardousGoods> findHazardousGoods(ImdgClass inImdgClass) {
        DomainQuery dq = QueryUtils.createDomainQuery("HazardousGoods").addDqPredicate(PredicateFactory.eq(InventoryField.HZGOODS_IMDG_CLASS, inImdgClass));
        return HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
    }

    private static HazardousGoods findHazardousGoods(String inUNNbr) {
        DomainQuery dq = QueryUtils.createDomainQuery("HazardousGoods").addDqPredicate(PredicateFactory.eq(InventoryField.HZGOODS_UN_NBR, inUNNbr));
        return (HazardousGoods) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
    }

    private static Set<Placard> getEquivalentPlacards(ImdgClass inImdgClass) {
        LOGGER.info("Deriving Equivalent Placards for " + inImdgClass)
        Set<Placard> placardSet = new HashSet<>();
        List<HazardousGoods> hazardousGoodsList = findHazardousGoods(inImdgClass);
        for (HazardousGoods goods : hazardousGoodsList) {
            LOGGER.info("Available Placard  " + goods.getHzgoodsPlacard().getPlacardText());
            placardSet.add(goods.getHzgoodsPlacard());
        }
        LOGGER.info("Returning the size of " + placardSet.size());
        return placardSet;
    }

    static Set<ImdgClass> isPlacardAvailable(String inMessageLevel, List<Placard> inPlacardList, List<ImdgClass> inImdgClassList) {

        //get all possible imdg classes for the placard
        List<Placard> notInBookingPlacard = new ArrayList<>();
        List<ImdgClass> notInTransactionPlacardsForImdg = new ArrayList<>(inImdgClassList.size());
        notInTransactionPlacardsForImdg.addAll(inImdgClassList);
        //Collections.copy(notInTransactionPlacardsForImdg, inImdgClassList);
        for (Placard placard : inPlacardList) {
            Set<ImdgClass> placardEquivalentImdgClassList = getImdgForPlacard(inMessageLevel, placard);
            List<ImdgClass> inImdgClassListCopy = new ArrayList<>(inImdgClassList.size());
            for (ImdgClass imdgClass2 : placardEquivalentImdgClassList) {
                LOGGER.info("Plcard IMDG is " + imdgClass2 + " and it's BaseTrait key is " + imdgClass2.getBaseTrait());
            }
            for (ImdgClass imdgClass2 : inImdgClassList) {
                LOGGER.info("Booking IMDG is " + imdgClass2 + " and it's BaseTrait key is " + imdgClass2.getBaseTrait());
            }
            inImdgClassListCopy.addAll(inImdgClassList);
            //Collections.copy(inImdgClassListCopy, inImdgClassList);
            for (ImdgClass imdgClass : placardEquivalentImdgClassList) {
                /**
                 * if the inImdgClassList has an explosive class, ie., getCompatibilityGroupTrait, then that needs to be taken into account
                 */
                for (ImdgClass imdgClass1 : inImdgClassList) {
                    if (imdgClass.getBaseTrait() == imdgClass1.getBaseTrait()) {
                        inImdgClassListCopy.remove(imdgClass1);
                        notInTransactionPlacardsForImdg.remove(imdgClass1);
                    }
                }
                //if(inImdgClassList)
                //inImdgClassListCopy.remove(imdgClass);
                //notInTransactionPlacardsForImdg.remove(imdgClass);
            }
            if (inImdgClassListCopy.size == inImdgClassList.size()) {
                // the placard is not useful
                notInBookingPlacard.add(placard);
            } else if (inImdgClassListCopy.size < inImdgClassList.size()) {
                // the placard has equivalent IMDG class
            }
        }
        Set<ImdgClass> tempSet = new HashSet<>();
        tempSet.addAll(notInTransactionPlacardsForImdg);
        return tempSet;
        //for the givem placard, if the inImdgClassList has any imdgClass, then return true
    }

    static Set<ImdgClass> getImdgForPlacard(String inLevel, Placard placard) {
        if (WARNING.equals(inLevel)) getWarnImdgForPlacard(placard);
        else if (ERROR.equals(inLevel)) getErrorImdgForPlacard(placard);
    }

    static Set<ImdgClass> getErrorImdgForPlacard(Placard placard) {
        Set<ImdgClass> imdgClassList = new HashSet<>();
        if (placard.getPlacardText().startsWith("1.4"))
            imdgClassList.add(ImdgClass.IMDG_14);
        else if (placard.getPlacardText().startsWith("1.1"))
            imdgClassList.add(ImdgClass.IMDG_11);
        else if (placard.getPlacardText().startsWith("1.2"))
            imdgClassList.add(ImdgClass.IMDG_12);
        else if (placard.getPlacardText().startsWith("1.3"))
            imdgClassList.add(ImdgClass.IMDG_13);
        else if (placard.getPlacardText().startsWith("1.5"))
            imdgClassList.add(ImdgClass.IMDG_15);
        else if (placard.getPlacardText().startsWith("1.6"))
            imdgClassList.add(ImdgClass.IMDG_16);
        else if (placard.getPlacardText().startsWith("1")) { // safety net, this will not be hit
            imdgClassList.add(ImdgClass.IMDG_11);
            imdgClassList.add(ImdgClass.IMDG_12);
            imdgClassList.add(ImdgClass.IMDG_13);
            imdgClassList.add(ImdgClass.IMDG_14);
            imdgClassList.add(ImdgClass.IMDG_15);
            imdgClassList.add(ImdgClass.IMDG_16);
            imdgClassList.add(ImdgClass.IMDG_1);
        } else if (placard.getPlacardText().startsWith("2.1")) {
            imdgClassList.add(ImdgClass.IMDG_21);
            imdgClassList.add(ImdgClass.IMDG_2); // There is no Placard for 2, reusing Placard 2.1 as 2 
        }else if (placard.getPlacardText().startsWith("2.2")) {
            imdgClassList.add(ImdgClass.IMDG_22);
        }else if (placard.getPlacardText().startsWith("2.3")) {
            imdgClassList.add(ImdgClass.IMDG_23);
        }else if (placard.getPlacardText().startsWith("2.4")) { // no 2.4 IMDG, anything in IMDG_2x is ok here
            imdgClassList.add(ImdgClass.IMDG_2);
            imdgClassList.add(ImdgClass.IMDG_21);
            imdgClassList.add(ImdgClass.IMDG_22);
            imdgClassList.add(ImdgClass.IMDG_23);
        }else if (placard.getPlacardText().startsWith("2")) {// safety net, this will not be hit
            imdgClassList.add(ImdgClass.IMDG_2);
            imdgClassList.add(ImdgClass.IMDG_21);
            imdgClassList.add(ImdgClass.IMDG_22);
            imdgClassList.add(ImdgClass.IMDG_23);
        } else if (placard.getPlacardText().startsWith("3.1")) {
            imdgClassList.add(ImdgClass.IMDG_31);
        }else if (placard.getPlacardText().startsWith("3.2")) {
            imdgClassList.add(ImdgClass.IMDG_32);
        }else if (placard.getPlacardText().startsWith("3.3")) {
            imdgClassList.add(ImdgClass.IMDG_33);
        }else if (placard.getPlacardText().startsWith("3.4")) { // 3.4 is mapped to IMDG_3 or IMDG_31
            imdgClassList.add(ImdgClass.IMDG_3);
            imdgClassList.add(ImdgClass.IMDG_31);
        }else if (placard.getPlacardText().startsWith("3")) {// safety net, this will not be hit
            imdgClassList.add(ImdgClass.IMDG_3);
            imdgClassList.add(ImdgClass.IMDG_31);
            imdgClassList.add(ImdgClass.IMDG_32);
            imdgClassList.add(ImdgClass.IMDG_33);
        } else if (placard.getPlacardText().startsWith("4/4.1")) {
            imdgClassList.add(ImdgClass.IMDG_41);
        } else if (placard.getPlacardText().startsWith("4/4.2")) {
            imdgClassList.add(ImdgClass.IMDG_42);
        } else if (placard.getPlacardText().startsWith("4/4.3")) {
            imdgClassList.add(ImdgClass.IMDG_43);
        } else if (placard.getPlacardText().startsWith("5.1"))
            imdgClassList.add(ImdgClass.IMDG_51);
        else if (placard.getPlacardText().startsWith("5.2"))
            imdgClassList.add(ImdgClass.IMDG_52);
        else if (placard.getPlacardText().startsWith("6/6.1PO")) {
            imdgClassList.add(ImdgClass.IMDG_61);
        }else if (placard.getPlacardText().startsWith("6/6.1 INH")) {
            imdgClassList.add(ImdgClass.IMDG_62);
        }else if (placard.getPlacardText().startsWith("6/6.1 PG")) { // 6/6.1 PGIII is mapped to IMDG_61 for the moment
            imdgClassList.add(ImdgClass.IMDG_61);
        }else if (placard.getPlacardText().startsWith("6")) {
            imdgClassList.add(ImdgClass.IMDG_61);
            imdgClassList.add(ImdgClass.IMDG_62);
        }
        /*else if (placard.getPlacardText().startsWith("6.2"))
            imdgClassList.add(ImdgClass.IMDG_62);*/
        else if (placard.getPlacardText().startsWith("7"))
            imdgClassList.add(ImdgClass.IMDG_7);
        else if (placard.getPlacardText().startsWith("8"))
            imdgClassList.add(ImdgClass.IMDG_8);
        else if (placard.getPlacardText().startsWith("9"))
            imdgClassList.add(ImdgClass.IMDG_9);
        /*else
            imdgClassList.add(ImdgClass.IMDG_X);*/
        return imdgClassList;
    }

    static Set<ImdgClass> getWarnImdgForPlacard(Placard placard) {
        Set<ImdgClass> imdgClassList = new HashSet<>();
        if (placard.getPlacardText().startsWith("1.1"))
            imdgClassList.add(ImdgClass.IMDG_11);
        else if (placard.getPlacardText().startsWith("1.2"))
            imdgClassList.add(ImdgClass.IMDG_12);
        else if (placard.getPlacardText().startsWith("1.3"))
            imdgClassList.add(ImdgClass.IMDG_13);
        else if (placard.getPlacardText().startsWith("1.4"))
            imdgClassList.add(ImdgClass.IMDG_14);
        else if (placard.getPlacardText().startsWith("1.5"))
            imdgClassList.add(ImdgClass.IMDG_15);
        else if (placard.getPlacardText().startsWith("1.6"))
            imdgClassList.add(ImdgClass.IMDG_16);
        else if (placard.getPlacardText().startsWith("1 ")) {
            imdgClassList.add(ImdgClass.IMDG_1);
        } else if (placard.getPlacardText().startsWith("2.1")) {
            imdgClassList.add(ImdgClass.IMDG_21);
            imdgClassList.add(ImdgClass.IMDG_2); // there is no placard for IMDG_2, reusing placard 2.1
        }else if (placard.getPlacardText().startsWith("2.2")) {
            imdgClassList.add(ImdgClass.IMDG_22); //todo, pending should map to IMDG_22
        }else if (placard.getPlacardText().startsWith("2.3")) {
            imdgClassList.add(ImdgClass.IMDG_23);
        }else if (placard.getPlacardText().startsWith("2.4")) {
            imdgClassList.add(ImdgClass.IMDG_2);
        }else if (placard.getPlacardText().startsWith("2")) {
            imdgClassList.add(ImdgClass.IMDG_2);
        } else if (placard.getPlacardText().startsWith("3.1")) {
            imdgClassList.add(ImdgClass.IMDG_31);
        }else if (placard.getPlacardText().startsWith("3.2")) {
            imdgClassList.add(ImdgClass.IMDG_32);
        }else if (placard.getPlacardText().startsWith("3.3")) {
            imdgClassList.add(ImdgClass.IMDG_33);
        }else if (placard.getPlacardText().startsWith("3.4")) {
            imdgClassList.add(ImdgClass.IMDG_3);
        } else if (placard.getPlacardText().startsWith("4/4.1")) {
            imdgClassList.add(ImdgClass.IMDG_41);
        } else if (placard.getPlacardText().startsWith("4/4.2")) {
            imdgClassList.add(ImdgClass.IMDG_42);
        } else if (placard.getPlacardText().startsWith("4/4.3")) {
            imdgClassList.add(ImdgClass.IMDG_43);
        } else if (placard.getPlacardText().startsWith("5.1"))
            imdgClassList.add(ImdgClass.IMDG_51);
        else if (placard.getPlacardText().startsWith("5.2"))
            imdgClassList.add(ImdgClass.IMDG_52);
        else if (placard.getPlacardText().startsWith("6/6.1 PO")) //    6/6.1 POIS
            imdgClassList.add(ImdgClass.IMDG_61);
        else if (placard.getPlacardText().startsWith("6/6.1 INH")) //   6/6.1 INH HAZ/TOX
            imdgClassList.add(ImdgClass.IMDG_62);
        else if (placard.getPlacardText().startsWith("6/6.1 PG")) //  6/6.1 PGIII
            imdgClassList.add(ImdgClass.IMDG_61);
        /*else if (placard.getPlacardText().startsWith("6.2"))
            imdgClassList.add(ImdgClass.IMDG_62);*/
        else if (placard.getPlacardText().startsWith("7"))
            imdgClassList.add(ImdgClass.IMDG_7);
        else if (placard.getPlacardText().startsWith("8"))
            imdgClassList.add(ImdgClass.IMDG_8);
        else if (placard.getPlacardText().startsWith("9"))
            imdgClassList.add(ImdgClass.IMDG_9);
        /*else
            imdgClassList.add(ImdgClass.IMDG_X);*/
        return imdgClassList;
    }

    static {
        NOT_TO_BE_VALIDATED_PLACARD.add(Placard.findPlacard(LTD_QTY_TEXT));
        NOT_TO_BE_VALIDATED_PLACARD.add(Placard.findPlacard(DNGR));
        NOT_TO_BE_VALIDATED_PLACARD.add(Placard.findPlacard(MARINE_POLL));
        MARINE_POLL_AND_DNGR.add(Placard.findPlacard(MARINE_POLL));
        MARINE_POLL_AND_DNGR.add(Placard.findPlacard(DNGR));
    }
}
