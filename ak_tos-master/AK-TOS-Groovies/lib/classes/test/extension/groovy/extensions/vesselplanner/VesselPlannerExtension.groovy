/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package groovy.extensions.vesselplanner

import com.navis.apex.business.clusternotificationservice.*
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.Facility
import com.navis.argo.business.model.LocPosition
import com.navis.argo.presentation.controller.CustomExternalAppController
import com.navis.crane.portal.ICraneBizFacade
import com.navis.crane.portal.cache.VesselStowageHelper
import com.navis.external.argo.AbstractCustomMessageHandlerExtension
import com.navis.external.argo.ICustomMessageHandlerSendChannel
import com.navis.external.util.Message
import com.navis.framework.business.Roastery
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.framework.metafields.MetafieldIdList
import com.navis.framework.persistence.HiberCache
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.persistence.hibernate.CarinaPersistenceCallback
import com.navis.framework.persistence.hibernate.PersistenceTemplate
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.UserContext
import com.navis.framework.portal.context.PortalApplicationContext
import com.navis.framework.portal.query.Disjunction
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.portal.query.PredicateIntf
import com.navis.framework.presentation.command.DestinationEnum
import com.navis.framework.presentation.command.VariformUiCommand
import com.navis.framework.presentation.context.PresentationContextUtils
import com.navis.framework.presentation.util.PresentationConstants
import com.navis.framework.query.common.api.QueryResult
import com.navis.framework.util.AtomizedEnum
import com.navis.framework.util.BizViolation
import com.navis.framework.util.ValueHolder
import com.navis.framework.util.ValueObject
import com.navis.framework.util.message.MessageCollector
import com.navis.framework.util.message.MessageCollectorImpl
import com.navis.inventory.InventoryEntity
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.TimeFrame
import com.navis.inventory.business.api.UnitField
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.moves.WorkQueue
import com.navis.inventory.business.units.InventoryTestUtils
import com.navis.inventory.business.units.UnitYardVisit
import com.navis.inventory.presentation.WorkInstructionViewHelper
import com.navis.inventory.util.position.IPositionable
import com.navis.inventory.util.position.SavedValuePositionable
import com.navis.vessel.business.operation.Vessel
import com.navis.vessel.business.schedule.VesselVisitDetails
import com.navis.vessel.portal.IVesselBizFacade
import com.navis.vessel.portal.cache.VesselModelHelper
import com.navis.vessel.portal.model.VesselModel
import com.navis.vessel.presentation.view.json.VesselModelJSON
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@SuppressWarnings("GroovyUnusedDeclaration")
class VesselPlannerExtension extends AbstractCustomMessageHandlerExtension implements IClusterNotificationSubscriber {

  @Override
  public String getOverriddenTabLabel(Map<String, Object> inExtensionAppAttributes) {
    List<Long> sources = (List<Long>) inExtensionAppAttributes.get(PresentationConstants.SOURCE);
    final long cvdGkey = sources[0];
    String nameArg = null;

    UserContext userContext = PresentationContextUtils.getRequestContext().getUserContext();
    PersistenceTemplate pt = new PersistenceTemplate(userContext);
    MessageCollector mc = pt.invoke(new CarinaPersistenceCallback() {
      @Override
      protected void doInTransaction() {
        HibernateApi hibernateApi = HibernateApi.getInstance();
        VesselVisitDetails vvd = VesselVisitDetails.loadByGkey(cvdGkey);
        nameArg = vvd.getCvdCv().getCvId();
      }
    });

    return nameArg;
  }

  @Override
  void initialize(@NotNull CustomExternalAppController inController) {
    _controller = inController;

    registerMessageHandler(DUMMY_TEST_REQUEST_ID, new DummyRequestHandler());
    registerMessageHandler(GET_GET_VESSEL_MODEL_REQUEST_ID, new GetVesselModelRequestHandler());
    registerMessageHandler(GET_ONBOARD_UNITS_LIST_REQUEST_ID, new GetOnboardUnitsRequestHandler());
    registerMessageHandler(GET_LOAD_UNITS_LIST_REQUEST_ID, new GetLoadUnitsRequestHandler());
    registerMessageHandler(GET_DISCHARGE_UNITS_LIST_REQUEST_ID, new GetDischargeUnitsRequestHandler());
    registerMessageHandler(GET_SECTION_STOWAGE_REQUEST_ID, new GetSectionStowageRequestHandler());
    registerMessageHandler(CREATE_WORK_INSTRUCTIONS_REQUEST_ID, new CreateWorkInstructionsRequestHandler());
    registerMessageHandler(REVERT_WORK_INSTRUCTIONS_REQUEST_ID, new RevertWorkInstructionsRequestHandler());
    registerMessageHandler(SHOW_UNIT_INSPECTOR_REQUEST_ID, new ShowUnitInspectorRequestHandler());

    List<String> entityTypes = new ArrayList<>();
    entityTypes.add(InventoryEntity.UNIT);
    entityTypes.add(InventoryEntity.UNIT_FACILITY_VISIT);
    entityTypes.add(MovesEntity.WORK_INSTRUCTION);

    _notificationFilter = new VesselPlannerSessionNotificationFilter();

    _notificationService = (IClusterNotificationService) Roastery.getBean(IClusterNotificationService.BEAN_ID);
    _notificationService.registerSubscriber(this);
  }

  @Override
  void close() {
    _notificationService.deregisterSubscriber(this);
  }

  private static String CHANGE_SUBSCRIBER_EVENT_ID = "ChangeSubscriberEvent";
  private static String DUMMY_TEST_REQUEST_ID = "DummyTestRequest";
  private static String GET_GET_VESSEL_MODEL_REQUEST_ID = "GetVesselModelRequest";
  private static String GET_ONBOARD_UNITS_LIST_REQUEST_ID = "GetOnboardUnitsListRequest";
  private static String GET_LOAD_UNITS_LIST_REQUEST_ID = "GetLoadUnitsListRequest";
  private static String GET_DISCHARGE_UNITS_LIST_REQUEST_ID = "GetDischargeUnitsListRequest";
  private static String GET_SECTION_STOWAGE_REQUEST_ID = "GetSectionStowageRequest";
  private static String CREATE_WORK_INSTRUCTIONS_REQUEST_ID = "CreateWorkInstructionsRequest";
  private static String REVERT_WORK_INSTRUCTIONS_REQUEST_ID = "RevertWorkInstructionsRequest";
  private static String SHOW_UNIT_INSPECTOR_REQUEST_ID = 'ShowUnitInspectorRequest'; // has no response

  @Override
  String getBriefDetails() {
    return "VesselPlannerExtension"; // todo: append session-specific identifying info
  }

  @Override
  Object getSubscriberTypeBroadcastFilter() {
    return new VesselPlannerBroadcastFilter();
  }

  @Override
  void initialize(@NotNull ICustomMessageHandlerSendChannel inSendChannel) {
  }

  @Override
  @NotNull
  IClusterNotificationFilter getFilter() {
    return _notificationFilter;
  }

  @Override
  void processNotification(@NotNull IClusterNotification inNotification) {

    if (! (inNotification instanceof EntityChangeNotification)) {
      return;
    }

    EntityChangeNotification change = (EntityChangeNotification) inNotification;

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("sourceName", change.getSourceName());
    params.put("originTimestamp", change.getOriginTimestamp());
    params.put("changeType", change.getChangeType());
    params.put("entityType", change.getEntityType());
    params.put("updateValues", change.getValueHolders());
    Message m = Message.newRequest(CHANGE_SUBSCRIBER_EVENT_ID, params);

    _controller.pushMessage(m);
  }

  public static MetafieldIdList getDesiredContainerQueryMetafields() {
    return ChangeSubscriberInterceptorFilter.buildStandardContainerMetafieldIdList(InventoryEntity.UNIT_FACILITY_VISIT);
  }

  public static void addWiFieldsToQueryMetafields(@NotNull MetafieldIdList inOutFields) {
    inOutFields.add(MovesField.WI_SEQUENCE);                         // The work instruction sequence for a container
  }

  public static Map buildResponseForPositionablesSet(@NotNull Long inCvdGkey, @NotNull Set<SavedValuePositionable> units) {
    buildResponseForUnits(inCvdGkey, units);
  }

  public static Map buildResponseForUnitsQuery(@NotNull Long inCvdGkey, @NotNull QueryResult inQueryResult) {
    Set<SavedValuePositionable> units = new HashSet<>();
    for (int entityIndex = 0; entityIndex < inQueryResult.getTotalResultCount(); entityIndex++) {
      ValueHolder vh = inQueryResult.getValueHolder(entityIndex);
      String posSlot = (String) vh.getFieldValue(1); // TODO: fix yucky hard coded index value 1 having knowledge of our field order
      units.add(new SavedValuePositionable(IPositionable.PositionType.UNIT_FACILITY_VISIT, vh.getEntityPrimaryKey(), posSlot, vh));
    }

    buildResponseForUnits(inCvdGkey, units);
  }

  public static Map buildResponseForUnits(@NotNull Long inCvdGkey, @NotNull Set<SavedValuePositionable> units) {
    List<String> resultFields = new ArrayList<String>();
    List<Map<String, Object>> resultEntities = new ArrayList<Map<String, Object>>();

    // Add in our synthetic fields if we have any results. (Or, should we always add all fields, requiring a new param that is the field list?)
    if (!units.isEmpty()) {
      resultFields.add("posSection");
      resultFields.add("posStack");
      resultFields.add("posTier");
      resultFields.add("sequence");
    }

    // Iterate over the result entities. Add each one's returned columns as a map of { columnName: columnValue }.
    boolean didFirstUnit = false;
    for (IPositionable p : units) {
      Map<String, Object> entityMap = new HashMap<String, Object>();
      ValueObject vo = p.getValueObject();
      MetafieldIdList fields = vo.getFields();
      MetafieldId primaryKeyField = vo.getPrimaryKeyField();
      Serializable primaryKey = vo.getEntityPrimaryKey();

      for (int fieldIndex = 0; fieldIndex < vo.getFieldCount(); ++fieldIndex) {
        MetafieldId field = fields.get(fieldIndex);
        Object value = vo.getFieldValue(fieldIndex);

        // For the first entity, use its field IDs to build our list of fields.
        if (!didFirstUnit) {
          resultFields.add(field.getFieldId());
        }

        if (value instanceof AtomizedEnum) {
          entityMap.put(field.getFieldId(), value.getKey()); // e.g. "BASIC40" rather than "EquipBasicLengthEnum[BASIC40]"
        } else {
          entityMap.put(field.getFieldId(), value);
        }

        if (field.getFieldId().equals("posSlot")) {
          String posSlot = (String) value; // example formats seen: AABBCC or AAABBCC; are there others we need to figure out?

          if (p.isWorkInstruction()) {
            posSlot = vo.getFieldValue(MetafieldIdFactory.getCompoundMetafieldId(MovesField.WI_POSITION, MovesField.POS_SLOT));
          }

          if (posSlot == null) {
            entityMap.put("posTier", "");
            entityMap.put("posStack", "");
            entityMap.put("posSection", "");
          } else {
            int length = posSlot.length();
            entityMap.put("posTier", ((String) (posSlot)).substring(length - 2));
            entityMap.put("posStack", ((String) (posSlot)).substring(length - 4, length - 2));
            entityMap.put("posSection", ((String) (posSlot)).substring(0, length - 4));
          }
        }
      }

      if (p.isWorkInstruction()) {
        Object value = vo.getFieldValue(MovesField.WI_SEQUENCE);
        entityMap.put("sequence", value);
      } else {
        entityMap.put("sequence", 0);
      }

      resultEntities.add(entityMap);
      didFirstUnit = true;
    }

    Map results = new HashMap();
    results.put("cvdGkey", inCvdGkey);
    results.put("fields", resultFields);
    results.put("entities", resultEntities);

    return results;
  }

  private IClusterNotificationFilter  _notificationFilter;
  private IClusterNotificationService _notificationService;
  private CustomExternalAppController _controller;
}

// TODO: This filter needs to be created at startup. Its constructor registers itself as a broadcast filter.

class VesselPlannerBroadcastFilter implements IClusterNotificationFilter {

  @Override
  boolean acceptsNotification(@NotNull @NotNull IClusterNotification inNotification) {
    if (! (inNotification instanceof EntityChangeNotification)) {
      return false;
    }

    EntityChangeNotification change = (EntityChangeNotification) inNotification;
    String entityType = change.getEntityType();

    return InventoryEntity.UNIT.equals(entityType) ||
            InventoryEntity.UNIT_FACILITY_VISIT.equals(entityType) ||
            MovesEntity.WORK_INSTRUCTION.equals(entityType);

  }

  @Override
  String getBriefDetails() {
    return "VesselPlannerBroadcastFilter";
  }
}

class VesselPlannerSessionNotificationFilter implements IClusterNotificationFilter {

  @Override
  boolean acceptsNotification(@NotNull @NotNull IClusterNotification inNotification) {
    return true;
  }

  @Override
  String getBriefDetails() {
    return "VesselPlannerSessionNotificationFilter";
  }
}

/**
 * This handler is for a dummy message the client can send that we will dutifully echo back as a response. Useful for testing and diagnosis.
 */
class DummyRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new DummyRequestHandler(inParentMessageHandler, inRequest);
  }

  public DummyRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public DummyRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  protected void doInTransaction() {
    Map<String, Object> params = _request.getParameters();
    _response = Message.newResponse(DUMMY_TEST_RESPONSE_ID, params); // echo back the supplied params
  }

  private static String DUMMY_TEST_RESPONSE_ID = "DummyTestResponse";
}

/**
 * This handler returns the vessel model for the specfied cvdGkey.
 */
class GetVesselModelRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    return new GetVesselModelRequestHandler(inParentMessageHandler, inRequest);
  }

  public GetVesselModelRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public GetVesselModelRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  @Nullable
  public Message handleMessage() {
    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");

    IVesselBizFacade vesselFacade = (IVesselBizFacade) PortalApplicationContext.getBean(IVesselBizFacade.BEAN_ID);
    MessageCollector mc = new MessageCollectorImpl();
    Serializable shipGkey = vesselFacade.getShipPrimaryKeyForVesselVisitDetails(PresentationContextUtils.getRequestContext().getUserContext(), mc, cvdGkey);

    VesselModel model = VesselModelHelper.getVesselModelForVesselVisit(cvdGkey, shipGkey);

    VesselModelJSON json = new VesselModelJSON(model);

    _results.put("cvdGkey", cvdGkey);
    _results.put("model", json);

    UserContext userContext = PresentationContextUtils.getRequestContext().getUserContext();
    PersistenceTemplate pt = new PersistenceTemplate(userContext);
    MessageCollector mc2 = pt.invoke(this); // TODO: how best to return errors collected in mc and mc2?

    return _response;
  }

  @Override
  protected void doInTransaction() {
    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");
    VesselVisitDetails vvd = VesselVisitDetails.loadByGkey(cvdGkey);
    Vessel ves = vvd.getVvdVessel();
    String vesId = ves.getVesId();
    _results.put("vesId", vesId);

    _response = Message.newResponse(GET_GET_VESSEL_MODEL_RESPONSE_ID, _results);
  }

  private Map _results = new HashMap();

  private static String GET_GET_VESSEL_MODEL_RESPONSE_ID = "GetVesselModelResponse";
}

/**
 * This handler returns the on-board units for the specified cvdGkey.
 */
class GetOnboardUnitsRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new GetOnboardUnitsRequestHandler(inParentMessageHandler, inRequest);
  }

  public GetOnboardUnitsRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public GetOnboardUnitsRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  protected void doInTransaction() {
    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");

    HibernateApi hibernateApi = HibernateApi.getInstance();
    VesselVisitDetails vvd = VesselVisitDetails.loadByGkey(cvdGkey);
    CarrierVisit cv = vvd.getCvdCv();
    long cvGkey = cv.getCvGkey();
    DomainQuery dq = createOnboardUnitsQuery(cvGkey);
    addDesiredUnitsFieldsToDomainQuery(dq);

    QueryResult dqResult = hibernateApi.findValuesByDomainQuery(dq);
    Map responseResults = VesselPlannerExtension.buildResponseForUnitsQuery(cvdGkey, dqResult);

    _response = Message.newResponse(GET_ONBOARD_UNITS_LIST_RESPONSE_ID, responseResults);
  }

  private static DomainQuery createOnboardUnitsQuery(long inCvGkey) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT_FACILITY_VISIT)
            .addDqPredicate(PredicateFactory.eq(UnitField.UFV_POS_LOC_TYPE, LocTypeEnum.VESSEL))
            .addDqPredicate(PredicateFactory.eq(UnitField.UFV_POS_LOC_GKEY, inCvGkey));
    return dq;
  }

  private static void addDesiredUnitsFieldsToDomainQuery(@NotNull DomainQuery inDq) {
    MetafieldIdList fields = VesselPlannerExtension.getDesiredContainerQueryMetafields();
    for (MetafieldId field : fields) {
      addFieldToDomainQuery(inDq, field);
    }
  }

  private static void addFieldToDomainQuery(@NotNull DomainQuery inDq, @NotNull MetafieldId inField) {
    MetafieldId qualifiedField = UnitField.getQualifiedField(inField, InventoryEntity.UNIT_FACILITY_VISIT);
    //noinspection GrDeprecatedAPIUsage
    String entityOfQualifiedFld = HiberCache.getEntityNameForField(qualifiedField.getMfidLeftMostNode().getFieldId());
    inDq.addDqField(qualifiedField);
  }

  private static String GET_ONBOARD_UNITS_LIST_RESPONSE_ID = "GetOnboardUnitsListResponse";
}

/**
 * This handler returns the load list units for the specified cvdGkey.
 */
class GetLoadUnitsRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new GetLoadUnitsRequestHandler(inParentMessageHandler, inRequest);
  }

  public GetLoadUnitsRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public GetLoadUnitsRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  protected void doInTransaction() {
    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");

    HibernateApi hibernateApi = HibernateApi.getInstance();
    VesselVisitDetails vvd = VesselVisitDetails.loadByGkey(cvdGkey);
    CarrierVisit cv = vvd.getCvdCv();
    DomainQuery dq = createLoadListUnitsQuery(cv);
    addDesiredUnitsFieldsToDomainQuery(dq);

    QueryResult dqResult = hibernateApi.findValuesByDomainQuery(dq);
    Map responseResults = VesselPlannerExtension.buildResponseForUnitsQuery(cvdGkey, dqResult);

    _response = Message.newResponse(GET_LOAD_UNITS_LIST_RESPONSE_ID, responseResults);
  }

  private static DomainQuery createLoadListUnitsQuery(@NotNull CarrierVisit inCv) {
    // Cribbed from: LoadlistExtractorPea.buildLoadListQuery()
    // TODO: add predicate to filter out units already on board; sample data set currently lacks such units

    Facility facility = inCv.getCvFacility();
    // Include units documented to depart on carrier, but exclude RESTOWS and STAYONBOARDS
    Object[] targetCategories = [UnitCategoryEnum.EXPORT, UnitCategoryEnum.TRANSSHIP,
            UnitCategoryEnum.IMPORT, UnitCategoryEnum.STORAGE];

    Object[] targetDrayStatus = [DrayStatusEnum.FORWARD, DrayStatusEnum.DRAYIN];
    Disjunction drayStatus = PredicateFactory.disjunction();
    drayStatus.add(PredicateFactory.isNull(UnitField.UFV_DRAY_STATUS))
            .add(PredicateFactory.in(UnitField.UFV_DRAY_STATUS, targetDrayStatus));

    Disjunction declaredOrIntendedCv = PredicateFactory.disjunction();
    declaredOrIntendedCv.add(PredicateFactory.eq(UnitField.UFV_INTENDED_OB_CV, inCv.getCvGkey()))
            .add(PredicateFactory.eq(UnitField.UFV_DECLARED_OB_CV, inCv.getCvGkey()));

    PredicateIntf notOnboardThisCv = PredicateFactory.ne(UnitField.UFV_POS_LOC_GKEY, inCv.getCvGkey());

    // 2007-05-23 PAC ARGO-4525 Exclude drayed-off so returned export only shows once
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT_FACILITY_VISIT)
            .addDqField(UnitField.UFV_GKEY)
            .setDqFieldsDistinct(true)
            .addDqPredicate(PredicateFactory.in(UnitField.UFV_CATEGORY, targetCategories))
            .addDqPredicate(drayStatus)
            .addDqPredicate(declaredOrIntendedCv)
            .addDqPredicate(PredicateFactory.eq(UnitField.UFV_FACILITY, facility.getFcyGkey()))
            .addDqPredicate(notOnboardThisCv)
    ;

    return dq;
  }

  private static void addDesiredUnitsFieldsToDomainQuery(@NotNull DomainQuery inDq) {
    MetafieldIdList fields = VesselPlannerExtension.getDesiredContainerQueryMetafields();
    for (MetafieldId field : fields) {
      addFieldToDomainQuery(inDq, field);
    }
  }

  private static void addFieldToDomainQuery(@NotNull DomainQuery inDq, @NotNull MetafieldId inField) {
    MetafieldId qualifiedField = UnitField.getQualifiedField(inField, InventoryEntity.UNIT_FACILITY_VISIT);
    String entityOfQualifiedFld = HiberCache.getEntityNameForField(qualifiedField.getMfidLeftMostNode().getFieldId());
    inDq.addDqField(qualifiedField);
  }

  private static String GET_LOAD_UNITS_LIST_RESPONSE_ID = "GetLoadUnitsListResponse";
}

/**
 * This handler returns the discharge units for the specified cvdGkey.
 * For the moment, we use the GetOnboardUnitsRequestHandler for data but tweak the response message ID.
 * TODO: implement the correct query for Discharge Units
 */
class GetDischargeUnitsRequestHandler extends GetOnboardUnitsRequestHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new GetDischargeUnitsRequestHandler(inParentMessageHandler, inRequest);
  }

  public GetDischargeUnitsRequestHandler() {
    super();
  }

  public GetDischargeUnitsRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  @Nullable
  public Message handleMessage() {
    // testing: this will get the on-board list (current time frame) for some section
    MessageCollector mc = new MessageCollectorImpl();
    Long sectionGkey = 54765;
    MetafieldIdList fields = VesselPlannerExtension.getDesiredContainerQueryMetafields();

    ICraneBizFacade facade = (ICraneBizFacade) PortalApplicationContext.getBean(ICraneBizFacade.BEAN_ID);
    Set<IPositionable> units = facade.getSectionStowage(PresentationContextUtils.getRequestContext().getUserContext(),
            mc, sectionGkey, TimeFrame.CURRENT, fields);

    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");
    Map responseResults = VesselPlannerExtension.buildResponseForPositionablesSet(cvdGkey, (Set<SavedValuePositionable>) units);
    _response = Message.newResponse(GET_DISCHARGE_UNITS_LIST_RESPONSE_ID, responseResults);

    return _response;
  }

  @Override
  protected void doInTransaction() {
    // Will never be called because we override handleMessage and do the work there.
  }

  private static String GET_DISCHARGE_UNITS_LIST_RESPONSE_ID = "GetDischargeUnitsListResponse";
}

/**
 * This handler returns the stowed units for the specified cvdGkey, sectionGkey, and time frame.
 * Required message parameters:
 *  "cvdGkey" (long) obvious
 *  "sectionGkey" (long) obvious
 *  "timeFrame" (string) TimeFrame enum value as string, e.g., "FUTURE"
 */
class GetSectionStowageRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new GetSectionStowageRequestHandler(inParentMessageHandler, inRequest);
  }

  public GetSectionStowageRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public GetSectionStowageRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  @Nullable
  public Message handleMessage() {
    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");
    Long sectionGkey = (Long) _request.getParameters().get("sectionGkey");
    String timeFrameString = (String) _request.getParameters().get("timeFrame"); // e.g., "CURRENT", "FUTURE", "COMPOSITE", etc.
    TimeFrame timeFrame = TimeFrame.valueOf(timeFrameString);

    MessageCollector mc = new MessageCollectorImpl();
    MetafieldIdList fields = ChangeSubscriberInterceptorFilter.EXTENSION_STOWAGE_QUERY_FIELDS;

    Set<IPositionable> units;
    Boolean singleBayQuery = (Boolean) _request.getParameters().get("singleBayQuery");
    if ((singleBayQuery != null) && singleBayQuery) {
      // Note: This does not take into account "paired into" behavior. We may be able to instead call VesselModelHelper.getSectionStowage, which wraps
      // the facade call and deals with paired sections; however, it also requires we have the VesselModel on hand, which is another query (expensive?).
      ICraneBizFacade facade = (ICraneBizFacade) PortalApplicationContext.getBean(ICraneBizFacade.BEAN_ID);
      units = facade.getSectionStowage(PresentationContextUtils.getRequestContext().getUserContext(),
              mc, sectionGkey, timeFrame, fields);
    } else {
      IVesselBizFacade vesselFacade = (IVesselBizFacade) PortalApplicationContext.getBean(IVesselBizFacade.BEAN_ID);
      Serializable shipGkey = vesselFacade.getShipPrimaryKeyForVesselVisitDetails(PresentationContextUtils.getRequestContext().getUserContext(), mc, cvdGkey);

      VesselModel vesselModel = VesselModelHelper.getVesselModelForVesselVisit(cvdGkey, shipGkey);
      units = VesselStowageHelper.getSectionStowage(PresentationContextUtils.getRequestContext().getUserContext(),
              sectionGkey, timeFrame, fields, vesselModel);
    }


    Map responseResults = VesselPlannerExtension.buildResponseForPositionablesSet(cvdGkey, (Set<SavedValuePositionable>) units);
    responseResults.put("cvdGkey", cvdGkey);
    responseResults.put("sectionGkey", sectionGkey);
    responseResults.put("timeFrame", timeFrameString);
    _response = Message.newResponse(GET_SECTION_STOWAGE_RESPONSE_ID, responseResults);

    return _response;
  }

  @Override
  protected void doInTransaction() {
    // Will never be called because we override handleMessage and do the work there.
    // TODO: does this imply that doInTransaction should not be abstract, or that there should be another class in between?
  }

  private static String GET_SECTION_STOWAGE_RESPONSE_ID = "GetSectionStowageResponse";
}

/**
 * This handler creates one or more work instructions for planning loads of containers to the vessel.
 * (It may be also used in the future for planning discharges from the vessel. Might need parameter tweaking or not.)
 * Required message parameters:
 *  "cvdGkey" (long) obvious
 *  "workInstructions" (array) array of requested work instructions, each consisting of:
 *    "uyvGkey" (long) the container (unit yard visit) gkey
 *    "slot" (string) the 'to' position
 *    "doorDirection" (string) (obvious) e.g. "A" (aft) or "F" (fore)
 *    "validatePosition" (boolean) true if validation logic is to be invoked for the proposed plan
 */
class CreateWorkInstructionsRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new CreateWorkInstructionsRequestHandler(inParentMessageHandler, inRequest);
  }

  public CreateWorkInstructionsRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public CreateWorkInstructionsRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  protected void doInTransaction() {
    Map results = new HashMap();
    List allWiResults = new ArrayList();

    Long cvdGkey = (Long) _request.getParameters().get("cvdGkey");
    List<Map<String, Object>> wiInfoMap = (List<Map<String, Object>>) _request.getParameters().get("workInstructions");

    for (Map<String, Object> wiInfo : wiInfoMap) {
      Map oneWiResults = new HashMap();
      createOneWorkInstruction(cvdGkey,
              (Long) wiInfo.get("uyvGkey"),
              (String) wiInfo.get("wqName"),            // e.g. "C10-dsch-00A"
              (String) wiInfo.get("slot"),              // e.g. "A32802"
              (String) wiInfo.get("doorDirection"),     // e.g. "A" or "F"
              (Boolean) wiInfo.get("validatePosition"),
              oneWiResults);
      allWiResults.add(oneWiResults);
    }

    results.put("cvdGkey", cvdGkey);
    results.put("workInstructions", allWiResults);

    _response = Message.newResponse(CREATE_WORK_INSTRUCTIONS_RESPONSE_ID, results);
  }

  @SuppressWarnings("GrMethodMayBeStatic")
  private void createOneWorkInstruction(@NotNull Long inCvdGkey, @NotNull Long inUyvGkey, @NotNull String inWqName, @NotNull String inSlot,
                                        @NotNull String inDoorDirection, @NotNull Boolean inDoValidatePosition, @NotNull Map inOutOneWiResults) {

    WorkQueue wq = WorkQueue.findOrCreateWorkQueue(ContextHelper.getThreadYardKey(), inWqName);

    VesselVisitDetails vvd = VesselVisitDetails.loadByGkey(inCvdGkey);
    CarrierVisit cv = vvd.getCvdCv();

    LocPosition destination =
      LocPosition.createVesselPosition(cv, inSlot, inDoorDirection, inDoValidatePosition);
    // other variations of the API:
    //LocPosition.createVesselPosition(ILocation inVesselVisit, String inSlot, String inDoorDirection);
    //LocPosition.createVesselPosition(CarrierVisit inVesselVisit, String inSlot, String inDoorDirection, boolean inValidatePosition);

    UnitYardVisit uyv = (UnitYardVisit) Roastery.getHibernateApi().load(UnitYardVisit.class, inUyvGkey);

    Double moveNumber = 1.0;
    WorkInstruction wi = InventoryTestUtils.createWorkInstruction(
            uyv, wq, WiMoveKindEnum.VeslLoad, WiMoveStageEnum.PLANNED, destination, moveNumber);

    inOutOneWiResults.put("resultCode", 0);
    inOutOneWiResults.put("errorMessage", "");
    inOutOneWiResults.put("uyvGkey", inUyvGkey);
    inOutOneWiResults.put("wiGkey", wi.getWiGkey());
    inOutOneWiResults.put("slot", wi.getWiPosition().getPosSlot());
    inOutOneWiResults.put("wqName", inWqName);
    inOutOneWiResults.put("sequence", wi.getWiSequence()); // the order of this move in the WorkQueue
  }

  private static String CREATE_WORK_INSTRUCTIONS_RESPONSE_ID = "CreateWorkInstructionsResponse";

}

/**
 * This handler reverts one or more work instructions.
 * Required message parameters:
 *  "workInstructions" (array) array of requested work instructions, each consisting of:
 *    "wiGkey" (long) the work instruction gkey
 *  (This could be simplified to just be an array of gkeys, but if we need more info about each WI later, this is better. This matches the WI creation
 *  structure above, so both are similar on client and server this way.)
 */
class RevertWorkInstructionsRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new RevertWorkInstructionsRequestHandler(inParentMessageHandler, inRequest);
  }

  public RevertWorkInstructionsRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public RevertWorkInstructionsRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  protected void doInTransaction() {
    Map results = new HashMap();

    List<Serializable> wiGkeys = new ArrayList<Serializable>();
    List<Map<String, Object>> wiInfoMap = (List<Map<String, Object>>) _request.getParameters().get("workInstructions");

    for (Map<String, Object> wiInfo : wiInfoMap) {
      wiGkeys.add((Long) wiInfo.get("wiGkey"));
    }

    Serializable[] wiGkeysArray = (Serializable[]) wiGkeys.toArray();

    try {
      new WorkInstructionViewHelper(ContextHelper.getThreadUserContext()).revertWorkInstructions(wiGkeysArray);
      results.put("revertedWiGkeys", wiGkeys);
      _response = Message.newResponse(REVERT_WORK_INSTRUCTIONS_RESPONSE_ID, results);
    } catch (BizViolation bv) {
      _response = Message.newErrorResponse(REVERT_WORK_INSTRUCTIONS_RESPONSE_ID, Message.ERROR, "Error Reverting Work Instructions: " +
        bv.getMessage());
    }

  }

  private static String REVERT_WORK_INSTRUCTIONS_RESPONSE_ID = "RevertWorkInstructionsResponse";

}

/**
 * This handler is for a dummy message the client can send that we will dutifully echo back as a response. Useful for testing and diagnosis.
 */
class ShowUnitInspectorRequestHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull Message inRequest) {
    return new ShowUnitInspectorRequestHandler(inParentMessageHandler, inRequest);
  }

  public ShowUnitInspectorRequestHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public ShowUnitInspectorRequestHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  @Nullable
  public Message handleMessage() {
    Long uyvGkey = (Long) _request.getParameters().get("uyvGkey");

    VariformUiCommand command = new VariformUiCommand("INSPECTOR_UNIT");
    command.setAttribute(PresentationConstants.DESTINATION, DestinationEnum.DIALOG);
    command.setAttribute(PresentationConstants.MODAL, false);
    //command.setParent(getParentWindow());// set parent window -- n/a for us
    Serializable[] gkeys = [ uyvGkey ]; // NOTE GROOVY-SPECIFIC, NON-JAVA SYNTAX FOR ARRAY DECLARATION AND INITIALIZATION
    command.execute(InventoryEntity.UNIT_FACILITY_VISIT, gkeys);

    return null; // there is no response for this message
  }

  @Override
  protected void doInTransaction() {
    // Will never be called because we override handleMessage and do the work there.
  }
}

