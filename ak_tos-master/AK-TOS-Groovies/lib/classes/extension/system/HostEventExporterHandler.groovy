/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package system

import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.GroovyApi
import com.navis.argo.business.atoms.LogicalEntityEnum
import com.navis.argo.business.integration.IntegrationServiceMessage
import com.navis.argo.business.model.Facility
import com.navis.argo.business.snx.AbstractSnxXmlExporter
import com.navis.argo.util.XmlUtil
import com.navis.carina.integrationservice.business.IntegrationService
import com.navis.framework.IntegrationServiceEntity
import com.navis.framework.IntegrationServiceField
import com.navis.framework.business.atoms.IntegrationServiceDirectionEnum
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.query.common.api.QueryResult
import com.navis.framework.util.scope.ScopeCoordinates
import com.navis.inventory.InventoryEntity
import com.navis.inventory.ServicesMovesEntity
import com.navis.inventory.ServicesMovesField
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.moves.MoveEvent
import com.navis.inventory.business.units.SnxUnitBase
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.services.ServicesEntity
import com.navis.services.ServicesField
import com.navis.services.business.event.Event
import com.navis.services.business.event.GroovyEvent
import org.apache.log4j.Logger
import org.jdom.Element

/**
 * Created with IntelliJ IDEA.
 * User: perumsu
 * Date: 26/12/13
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class HostEventExporterHandler extends AbstractSnxXmlExporter implements SnxUnitBase {

  private static final MetafieldId EVNT_EVNTTYPE_ID = MetafieldIdFactory.getCompoundMetafieldId(ServicesField.EVNT_EVENT_TYPE,
          ServicesField.EVNTTYPE_ID);
  private static final MetafieldId MVE_FROM_POSITION_LOC_TYPE = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_FROM_POSITION,
          ServicesMovesField.POS_LOC_TYPE);
  private static final MetafieldId MVE_FROM_POSITION_LOC_ID = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_FROM_POSITION,
          ServicesMovesField.POS_LOC_ID);
  private static final MetafieldId MVE_FROM_POSITION_SLOT = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_FROM_POSITION,
          ServicesMovesField.POS_SLOT);
  private static final MetafieldId MVE_FROM_POSITION_ORIENTATION = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_FROM_POSITION,
          ServicesMovesField.POS_ORIENTATION);
  private static final MetafieldId MVE_TO_POSITION_LOC_TYPE = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_TO_POSITION,
          ServicesMovesField.POS_LOC_TYPE);
  private static final MetafieldId MVE_TO_POSITION_LOC_ID = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_TO_POSITION,
          ServicesMovesField.POS_LOC_ID);
  private static final MetafieldId MVE_TO_POSITION_SLOT = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_TO_POSITION,
          ServicesMovesField.POS_SLOT);
  private static final MetafieldId MVE_TO_POSITION_ORIENTATION = MetafieldIdFactory.getCompoundMetafieldId(ServicesMovesField.MVE_TO_POSITION,
          ServicesMovesField.POS_ORIENTATION);


  public HostEventExporterHandler() {
    super(null, null)
  }

  @Override
  protected DomainQuery createScalarQuery() {
    return null
  }

  @Override
  protected Element createOneEntityElement() {
    return null
  }

  public void execute(GroovyEvent inGroovyEvent) {
    Event event = inGroovyEvent.getEvent();
    String entityId = getEntityId(event);
    Element element = createElement(E_HOST_OUTBOUND_MESSAGE);
    LogicalEntityEnum entityClass = event.getEventAppliedToClass();

    appendNonMoveHistoryElement(element, event);

    if (LogicalEntityEnum.UNIT.equals(entityClass) && MoveEvent.isMoveEvent(event)) {
      Unit unit = Unit.hydrate(event.getEventAppliedToGkey());
      UnitFacilityVisit ufv = unit.getUnitActiveUfvNowActive();
      if (ufv != null) {
        appendMoveHistoryElement(element, ufv.getUfvGkey(), (Serializable) event.getEventGKey());
      }
    }

    List elementList = element.getContent();
    Iterator itr = elementList.iterator();
    StringBuilder sbf = new StringBuilder();
    sbf.append("<"+E_HOST_OUTBOUND_MESSAGE+">");
    sbf.append("\n");
    while (itr.hasNext()) {
      Element e = (Element) itr.next();
      sbf.append(XmlUtil.toString(e, true));
      sbf.append("\n");
    }
    sbf.append("</"+E_HOST_OUTBOUND_MESSAGE+">");
    LOGGER.warn("SNX : " + sbf.toString());

    IntegrationService integrationService = findDefaultIntegrationService(ContextHelper.getThreadFacility());
    IntegrationServiceMessage integrationServiceMessage = createIntegrationServiceMessage(event, entityId, integrationService, sbf.toString());
    if (integrationService != null) {
      try {
        new GroovyApi().sendXml(integrationService.getIntservName(), sbf.toString(), null);
      } catch (Exception e) {
        LOGGER.error("JMS Connection Problem: "+e);
        integrationServiceMessage.setIsmFirstSendTime(null);
        integrationServiceMessage.setIsmLastSendTime(null);
        HibernateApi.getInstance().save(integrationServiceMessage);
      }
    }
  }

  private void appendNonMoveHistoryElement(Element inElement, Event inEvent) {

    Element e = createElement(com.navis.inventory.business.units.SnxUnitBase.E_NON_MOVE_EVENT);
    setAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_ID, inEvent.getEventTypeId());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_NOTES, inEvent.getEventNote());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_TIME, inEvent.getEventTime());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_APPLIED_BY, inEvent.getEventPrincipal());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_QUANTITY, inEvent.getEventQuantity());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_QUANTITY_UNIT, inEvent.getEventQuantityUnit());
    setAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_IS_BILLABLE, inEvent.getEventTypeIsBillable());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_REL_ENTITY_ID, inEvent.getEvntRelatedEntityId());
    setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNT_REL_ENTITY_CLASS, inEvent.getEvntRelatedEntityClass());
    setAttribute(e, A_ENTITY, inEvent.getEventAppliedToClass());
    setAttribute(e, A_ENTITY_ID, getEntityId(inEvent));

    appendEventFieldChangesElement(e, inEvent.getEventGKey() as Serializable);
    inElement.addContent(e);
  }

  private void appendEventFieldChangesElement(Element inNonMoveEventElement, Serializable inEventGkey) {
    if ((inEventGkey == null) || (inNonMoveEventElement == null)) {
      LOGGER.warn("appendEventFieldChangesElement called with null args");
      return;
    }

    DomainQuery dq = QueryUtils.createDomainQuery(ServicesEntity.EVENT_FIELD_CHANGE)
            .addDqField(ServicesField.EVNTFC_METAFIELD_ID)
            .addDqField(ServicesField.EVNTFC_NEW_VAL)
            .addDqField(ServicesField.EVNTFC_PREV_VAL)
            .addDqPredicate(PredicateFactory.eq(ServicesField.EVNTFC_EVENT, inEventGkey));

    final QueryResult qr = HibernateApi.getInstance().findValuesByDomainQuery(dq);
    if (qr == null || qr.getTotalResultCount() == 0) {
      return;
    }

    Element fieldChangesElement = createElement(com.navis.inventory.business.units.SnxUnitBase.E_FIELD_CHANGES);
    for (Iterator iterator = qr.getIterator(); iterator.hasNext();) {
      Object[] v = (Object[]) iterator.next();
      Element e = createElement(com.navis.inventory.business.units.SnxUnitBase.E_FIELD_CHANGE);

      Object id = v[0];
      Object newValue = v[1];
      Object prevValue = v[2];

      setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNTFC_ID, id);
      setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNTFC_NEW_VAL, newValue);
      setOptionalAttribute(e, com.navis.inventory.business.units.SnxUnitBase.A_EVNTFC_PRVE_VAL, prevValue);

      fieldChangesElement.addContent(e);
    }
    inNonMoveEventElement.addContent(fieldChangesElement);
  }

  private void appendMoveHistoryElement(Element inElement, Serializable inUfvGkey, Serializable inEventGkey) {
    DomainQuery dq = QueryUtils.createDomainQuery(ServicesMovesEntity.MOVE_EVENT)
            .addDqField(ServicesMovesField.MVE_MOVE_KIND)
            .addDqField(ServicesMovesField.MVE_TIME_PUT)
            .addDqField(MVE_FROM_POSITION_LOC_TYPE)
            .addDqField(MVE_FROM_POSITION_LOC_ID)
            .addDqField(MVE_FROM_POSITION_ORIENTATION)
            .addDqField(MVE_FROM_POSITION_SLOT)
            .addDqField(MVE_TO_POSITION_LOC_TYPE)
            .addDqField(MVE_TO_POSITION_LOC_ID)
            .addDqField(MVE_TO_POSITION_ORIENTATION)
            .addDqField(MVE_TO_POSITION_SLOT)
            .addDqField(ServicesMovesField.MVE_DIST_TO_START)
            .addDqField(ServicesMovesField.MVE_DIST_OF_CARRY)
            .addDqField(ServicesMovesField.MVE_TIME_CARRY_COMPLETE)
            .addDqField(ServicesMovesField.MVE_TIME_DISPATCH)
            .addDqField(ServicesMovesField.MVE_TIME_FETCH)
            .addDqField(ServicesMovesField.MVE_TIME_DISCHARGE)
            .addDqField(ServicesMovesField.MVE_TIME_PUT)
            .addDqField(ServicesMovesField.MVE_TIME_CARRY_CHE_DISPATCH)
            .addDqField(ServicesMovesField.MVE_TIME_CARRY_CHE_FETCH_READY)
            .addDqField(ServicesMovesField.MVE_TIME_CARRY_CHE_PUT_READY)
            .addDqField(ServicesMovesField.MVE_REHANDLE_COUNT)
            .addDqField(ServicesMovesField.MVE_TWIN_CARRY)
            .addDqField(ServicesMovesField.MVE_TWIN_FETCH)
            .addDqField(ServicesMovesField.MVE_TWIN_PUT)
            .addDqField(ServicesMovesField.MVE_RESTOW_ACCOUNT)
            .addDqField(ServicesMovesField.MVE_RESTOW_REASON)
            .addDqField(ServicesMovesField.MVE_SERVICE_ORDER)
            .addDqField(ServicesMovesField.MVE_CHE_CARRY)
            .addDqField(ServicesMovesField.MVE_CHE_FETCH)
            .addDqField(ServicesMovesField.MVE_CHE_PUT)
            .addDqField(ServicesField.EVNT_GKEY)
            .addDqField(EVNT_EVNTTYPE_ID)
            .addDqField(ServicesMovesField.MVE_CHE_CARRY_LOGIN_NAME)
            .addDqField(ServicesMovesField.MVE_CHE_FETCH_LOGIN_NAME)
            .addDqField(ServicesMovesField.MVE_CHE_PUT_LOGIN_NAME)
            .addDqPredicate(PredicateFactory.eq(ServicesMovesField.MVE_UFV, inUfvGkey))
            .addDqPredicate(PredicateFactory.eq(ServicesField.EVNT_GKEY, inEventGkey))
            .setFullLeftOuterJoin(true);

    final QueryResult qr = HibernateApi.getInstance().findValuesByDomainQuery(dq);
    if (qr == null) {
      return;
    }
    int resultCount = qr.getTotalResultCount();

    if (resultCount == 0) {
      return;
    }
    Element historyElement = createElement(com.navis.inventory.business.units.SnxUnitBase.E_MOVE_HISTORY);
    for (int i = 0; i < resultCount; i++) {
      Object mvKind = qr.getValue(i, ServicesMovesField.MVE_MOVE_KIND);
      Object time = qr.getValue(i, ServicesMovesField.MVE_TIME_PUT);
      Object fromPosLocType = qr.getValue(i, MVE_FROM_POSITION_LOC_TYPE);
      Object fromPosLocId = qr.getValue(i, MVE_FROM_POSITION_LOC_ID);
      Object fromPosOrientation = qr.getValue(i, MVE_FROM_POSITION_ORIENTATION);
      Object fromPosSlot = qr.getValue(i, MVE_FROM_POSITION_SLOT);
      Object toPosLocType = qr.getValue(i, MVE_TO_POSITION_LOC_TYPE);
      Object toPosLocId = qr.getValue(i, MVE_TO_POSITION_LOC_ID);
      Object toPosOrientation = qr.getValue(i, MVE_TO_POSITION_ORIENTATION);
      Object toPosSlot = qr.getValue(i, MVE_TO_POSITION_SLOT);
      Object dstToStart = qr.getValue(i, ServicesMovesField.MVE_DIST_TO_START);
      Object dstOfCarry = qr.getValue(i, ServicesMovesField.MVE_DIST_OF_CARRY);
      Object timeCarryComplete = qr.getValue(i, ServicesMovesField.MVE_TIME_CARRY_COMPLETE);
      Object timeDispatch = qr.getValue(i, ServicesMovesField.MVE_TIME_DISPATCH);
      Object timeFetch = qr.getValue(i, ServicesMovesField.MVE_TIME_FETCH);
      Object timeDischarge = qr.getValue(i, ServicesMovesField.MVE_TIME_DISCHARGE);
      Object timePut = qr.getValue(i, ServicesMovesField.MVE_TIME_PUT);
      Object timeCarryCheDispatch = qr.getValue(i, ServicesMovesField.MVE_TIME_CARRY_CHE_DISPATCH);
      Object timeCarryCheFetchReady = qr.getValue(i, ServicesMovesField.MVE_TIME_CARRY_CHE_FETCH_READY);
      Object timeCarryChePutReady = qr.getValue(i, ServicesMovesField.MVE_TIME_CARRY_CHE_PUT_READY);
      Object rehandleCount = qr.getValue(i, ServicesMovesField.MVE_REHANDLE_COUNT);
      Object twinCarry = qr.getValue(i, ServicesMovesField.MVE_TWIN_CARRY);
      Object twinFetch = qr.getValue(i, ServicesMovesField.MVE_TWIN_FETCH);
      Object twinPut = qr.getValue(i, ServicesMovesField.MVE_TWIN_PUT);
      Object restowAccount = qr.getValue(i, ServicesMovesField.MVE_RESTOW_ACCOUNT);
      Object restowReason = qr.getValue(i, ServicesMovesField.MVE_RESTOW_REASON);
      Object serviceOrder = qr.getValue(i, ServicesMovesField.MVE_SERVICE_ORDER);
      Object cheCarry = qr.getValue(i, ServicesMovesField.MVE_CHE_CARRY);
      Object cheFetch = qr.getValue(i, ServicesMovesField.MVE_CHE_FETCH);
      Object chePut = qr.getValue(i, ServicesMovesField.MVE_CHE_PUT);
      Object eventGkey = qr.getValue(i, ServicesField.EVNT_GKEY);
      Object carryLoginName = qr.getValue(i, ServicesMovesField.MVE_CHE_CARRY_LOGIN_NAME);
      Object fetchLoginName = qr.getValue(i, ServicesMovesField.MVE_CHE_FETCH_LOGIN_NAME);
      Object putLoginName = qr.getValue(i, ServicesMovesField.MVE_CHE_PUT_LOGIN_NAME);

      Element moveElement = createElement(com.navis.inventory.business.units.SnxMoveHistoryBase.E_MOVE);
      setAttribute(moveElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_MOVE_KIND, mvKind);
      setOptionalAttribute(moveElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_STAMP, time);

      Element fromPosElement = createElement(com.navis.inventory.business.units.SnxMoveHistoryBase.E_FROM_POSITION);
      setAttribute(fromPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_LOC_TYPE, fromPosLocType);
      setOptionalAttribute(fromPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_LOCATION, fromPosLocId);
      setOptionalAttribute(fromPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_SLOT, fromPosSlot);
      setOptionalAttribute(fromPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_ORIENTATION, fromPosOrientation);

      Element toPosElement = createElement(com.navis.inventory.business.units.SnxMoveHistoryBase.E_TO_POSITION);
      setAttribute(toPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_LOC_TYPE, toPosLocType);
      setOptionalAttribute(toPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_LOCATION, toPosLocId);
      setOptionalAttribute(toPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_SLOT, toPosSlot);
      setOptionalAttribute(toPosElement, com.navis.inventory.business.units.SnxUnitBase.A_POS_ORIENTATION, toPosOrientation);

      Element moveDetailElement = createElement(com.navis.inventory.business.units.SnxMoveHistoryBase.E_MOVE_DETAILS);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_DISTANCE_TO_START, dstToStart);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_DISTANCE_OF_CARRY, dstOfCarry);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_CARRY_COMPLETE, timeCarryComplete);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_DISPATCH, timeDispatch);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_FETCH, timeFetch);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_DISCHARGE, timeDischarge);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_PUT, timePut);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_CARRY_CHE_DISPATCH, timeCarryCheDispatch);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_CARRY_CHE_FETCH_READY, timeCarryCheFetchReady);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_TIME_CARRY_CHE_PUT_READY, timeCarryChePutReady);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_REHANDLE_COUNT, rehandleCount);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_WAS_TWIN_CARRY, twinCarry);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_WAS_TWIN_FETCH, twinFetch);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_WAS_TWIN_PUT, twinPut);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_RESTOW_ACCOUNT, restowAccount);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_RESTOW_REASON, restowReason);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_SERVICE_ORDER, serviceOrder);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_CHE_CARRY, cheCarry);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_CHE_PUT, chePut);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_CHE_FETCH, cheFetch);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_CARRY_LOGIN_NAME, carryLoginName);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_FETCH_LOGIN_NAME, fetchLoginName);
      setOptionalAttribute(moveDetailElement, com.navis.inventory.business.units.SnxMoveHistoryBase.A_PUT_LOGIN_NAME, putLoginName);

      moveElement.addContent(fromPosElement);
      moveElement.addContent(toPosElement);
      moveElement.addContent(moveDetailElement);

      inElement.addContent(moveElement);
    }
  }

  private static String getEntityId(Event inEvent) {
    String entityId = inEvent.getEventAppliedToNaturalKey();
    if (entityId == null) {
      LogicalEntityEnum entityClass = inEvent.getEventAppliedToClass();
      DomainQuery dq;
      if (entityClass.equals(LogicalEntityEnum.UNIT)) {
        dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT);
        dq.addDqPredicate(PredicateFactory.eq(UnitField.UNIT_GKEY, inEvent.getEventAppliedToGkey()));
        dq.addDqField(UnitField.UNIT_ID);
        final QueryResult qr = HibernateApi.getInstance().findValuesByDomainQuery(dq);
        entityId = (String) qr.getValue(0, UnitField.UNIT_ID);
      }
    }
    return entityId;
  }

  public static IntegrationServiceMessage createIntegrationServiceMessage(Event inEvent, String inEntityId, IntegrationService inIntegrationService, String inMessagePayload) {
    IntegrationServiceMessage ism = new IntegrationServiceMessage();
    ism.setIsmEventPrimaryKey((Long) inEvent.getEvntEventType().getPrimaryKey());
    ism.setIsmEntityClass(inEvent.getEventAppliedToClass());
    ism.setIsmEntityNaturalKey(inEntityId);
    ism.setIsmEventTypeId(inEvent.getEventTypeId());
    if (inIntegrationService != null) {
      ism.setIsmIntegrationService(inIntegrationService);
      ism.setIsmFirstSendTime(ArgoUtils.timeNow());
      ism.setIsmLastSendTime(ArgoUtils.timeNow());
    } else {
      LOGGER.warn("Integration Service is not found");
    }
    ism.setIsmMessagePayload(inMessagePayload);

    IntegrationServiceMessageIdProvider ismIdProvider = new IntegrationServiceMessageIdProvider();
    ism.setIsmSeqNbr(ismIdProvider.getNextIntegrationServiceMessageId());

    ScopeCoordinates scopeCoordinates = ContextHelper.getThreadUserContext().getScopeCoordinate();
    Long scopeLevel = ScopeCoordinates.GLOBAL_LEVEL;
    String scopeGkey = null;
    if (!scopeCoordinates.isScopeGlobal()) {
      scopeLevel = new Long(ScopeCoordinates.getScopeId(4));
      scopeGkey = (String) scopeCoordinates.getScopeLevelCoord(scopeLevel.intValue());
    }

    ism.setIsmScopeGkey(scopeGkey);
    ism.setIsmScopeLevel(scopeLevel);
    HibernateApi.getInstance().save(ism);
    return ism;
  }

  private static IntegrationService findDefaultIntegrationService(Facility inFacility) {
    DomainQuery dq = QueryUtils.createDomainQuery(IntegrationServiceEntity.INTEGRATION_SERVICE);
    dq.addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_DIRECTION, IntegrationServiceDirectionEnum.OUTBOUND));
    dq.addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_ACTIVE, Boolean.TRUE));
    dq.addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_DEFAULT, Boolean.TRUE));
    dq.addDqPredicate(PredicateFactory.eq(IntegrationServiceField.INTSERV_SCOPE_GKEY, inFacility.getFcyGkey()));

    List integrationServiceList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq)
    if (integrationServiceList == null || integrationServiceList.isEmpty()) {
      String message = inFacility.getFcyPathName() + " has no outbound jms defined.";
      LOGGER.error(message);
      return null;
    }
    IntegrationService integrationService = (IntegrationService) integrationServiceList.get(0);
    return integrationService;
  }

  String E_HOST_OUTBOUND_MESSAGE = "host-outbound-message";
  String A_ENTITY = "entity";
  String A_ENTITY_ID = "entity-id";
  private static final Logger LOGGER = Logger.getLogger(HostEventExporterHandler.class);

  public static class IntegrationServiceMessageIdProvider extends com.navis.argo.business.model.ArgoSequenceProvider {
    /**
     * returns next SegNo that is unqiue for the current thread Facility.
     */
    public Long getNextIntegrationServiceMessageId() {
      return super.getNextSeqValue(_ismIdSeq, (Long) ContextHelper.getThreadFacilityKey());
    }

    private String _ismIdSeq = "ISM_SEQUENCE";
  }
}
