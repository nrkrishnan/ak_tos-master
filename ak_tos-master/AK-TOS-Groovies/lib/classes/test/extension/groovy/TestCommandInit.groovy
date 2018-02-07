package test.extension.groovy

import com.navis.argo.*
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.*
import com.navis.argo.business.reference.*
import com.navis.argo.business.xps.model.Che
import com.navis.argo.business.xps.model.WorkAssignment
import com.navis.control.business.ControlRailTestUtils
import com.navis.control.business.ControlTestUtils
import com.navis.control.business.XmlRdtTestUtils
import com.navis.control.esb.teams.emulation.TeamsDriveTimeEmulationHelperEnhanced
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.inventory.business.api.*
import com.navis.inventory.business.atoms.DoorDirectionEnum
import com.navis.inventory.business.atoms.EqUnitRoleEnum
import com.navis.inventory.business.atoms.UfvTransitStateEnum
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.InventoryTestUtils
import com.navis.inventory.business.units.Unit
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.inventory.business.units.UnitYardVisit
import com.navis.mensa.business.atoms.MovePurposeEnum
import com.navis.optimization.portal.queueing.QueueServerClass
import com.navis.orders.business.api.OrdersFinder
import com.navis.orders.business.eqorders.Booking
import com.navis.orders.business.eqorders.EquipmentOrderItem
import com.navis.orders.business.eqorders.EquipmentReceiveOrder
import com.navis.rail.business.api.RailManager
import com.navis.rail.business.atoms.TrainDirectionEnum
import com.navis.rail.business.entity.RailcarVisit
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.spatial.business.model.AbstractBin
import com.navis.yard.business.YardTestUtils
import com.navis.yard.business.atoms.TZBlockAssociationEnum
import com.navis.yard.business.model.TrackPlan
import com.navis.yard.business.model.TransferZoneAssociation
import com.navis.yard.business.model.YardBinModel
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.hibernate.ObjectNotFoundException
import org.hibernate.SQLQuery
import org.hibernate.Session

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
class TestCommandInit {

  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(test.extension.groovy.TestCommandInit.class);

  /**
   * Finds/Creates a new accessory for Genset and attaches to the specified equipment
   *
   * @param inParameters
   * equipmentId - Equipment to which the accessory needs to be attached
   * accessoryId - Accessory Id
   * accessoryType - Accessory Type
   *
   * * @return JSON , <code>Accessory attached to Equipment</code>-if successfully<br>
   *                <code>Accessory not attached to Equipment</code>-block if failed
   *
   * @Example
   * Table invoked in SPARCS -
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="createAndAttachAccessory" /&gt;<br>
   * &lt;parameter id="equipId" value="TSTU111111" /&gt;<br>     //mandatory
   * &lt;parameter id="equipType" value="GEN" /&gt;<br>          //optional - when not provided default is GENSET
   * &lt;parameter id="equipClass" value="ACCESSORY" /&gt;<br>   //optional - when not provided default is ACCESSORY
   * &lt;parameter id="accessoryId" value="ACCRY123" /&gt;<br>   //mandatory
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateAndAttachAccessory(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="CreateAndAttachAccessory" />
                                        <parameter id="equipId" value="<Equipment Id>" />
                                        <parameter id="equipType" value="<Equipment Type>" />
                                        <parameter id="equipClass" value="<Equipment Class>" />
                                        <parameter id="accyId" value="<Accessory Id>" />'''

    String eqId = _testCommandHelper.checkParameter('equipId', inParameters);
    String accId = _testCommandHelper.checkParameter('accyId', inParameters);

    def eqType = 'GENSET';
    if (inParameters.containsKey('equipType') && !inParameters.get('equipType').toString().isEmpty()) {
      eqType = inParameters.get('equipType');
    }

    def eqClass = 'ACCESSORY';
    if (inParameters.containsKey('equipClass') && !inParameters.get('equipClass').toString().isEmpty()) {
      eqClass = inParameters.get('equipClass');
    }
    EquipClassEnum eqClassEnum = EquipClassEnum.getEnum(eqClass);
    if (eqClassEnum != null) {
      EquipType equipType = findEquipType(eqType.toString(), eqClassEnum);
      if (equipType != null) {
        if (eqClassEnum.equals(EquipClassEnum.ACCESSORY)) {
          equipType.setEqtypClass(EquipClassEnum.ACCESSORY);
          equipType.setEqtypIsoGroup(EquipIsoGroupEnum.GS);
          equipType.setEqtypDataSource(DataSourceEnum.AUTOMATION);
          HibernateApi.getInstance().update(equipType);
        }
      } else {
        equipType = EquipType.createEquipType(eqType, eqClassEnum);
      }

      Equipment accessory = Accessory.findOrCreateAccessory(accId, eqType, DataSourceEnum.AUTOMATION);
      assert accessory != null, returnString = "Accessory not created";

      Equipment equip = Equipment.findEquipment(eqId);
      if (equip != null) {
        UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
        Unit unit = finder.findActiveUnit(ContextHelper.getThreadComplex(), equip);

        if (unit != null) {
          unit.attachAccessory(accessory);
          returnString = "Accessory attached to Equipment";
        } else {
          returnString = "Accessory not attached to Equipment";
        }
      }
    } else {
      returnString = "Please specify a valid Equipment class: Accessory/ Chassis etc"
    }

    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a unit in yard and place it in the given location. Also covers placing container in Rack.
   *
   * @Precondition
   * location should already be available, to create please invoke CreateStackBlock<br>
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * <br>command=PreAdviseRoadUnit
   * <br>unitId=Equipment needs to be created
   * lineOperator= Line Operator of the unit.<br>
   * @return JSON , <code>Unit created and preadvised</code><br><code>Unit not created / preadvised </code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PreAdviseRoadUnit" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000019"/&gt;<br>
   * &lt;parameter id="lineOperator" value="APL"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PreAdviseRoadUnit(Map inParameters) {
    assert inParameters.size() >= 6, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 6 parameters:
                                            <parameter id="command" value="PreAdviseRoadUnit" />
                                            <parameter id="unitId" value="<Unit Id>" />
                                            <parameter id="unitType" value="<Unit Type>" />
                                            <parameter id="unitCategory" value="EXPORT/IMPORT/STORAGE" />
                                            <parameter id="unitFreightKind" value="FCL/LCL/MTY" />
                                            <parameter id="lineOperator" value="<Line Operator>" />
                                            <parameter id="bookingNumber" value="unique number for booking" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inLineOperator = _testCommandHelper.checkParameter('lineOperator', inParameters);
    String inCategory = _testCommandHelper.checkParameter('unitCategory', inParameters);
    String inFreightKind = _testCommandHelper.checkParameter('unitFreightKind', inParameters);
    String inBookingNumber;

    if (inParameters.containsKey('bookingNumber') && !inParameters.get('bookingNumber').toString().isEmpty()) {
      inBookingNumber = inParameters.get('bookingNumber', inParameters);
    }

    CarrierVisit ibcv, obcv
    inCategory = inCategory.toUpperCase();
    inFreightKind = inFreightKind.toUpperCase();
    try {
      Equipment eq = InventoryTestUtils.findOrCreateTestContainer(inUnitId, inUnitType)
      ScopedBizUnit lop = ScopedBizUnit.findScopedBizUnit(inLineOperator, BizRoleEnum.LINEOP);
      UnitManager um = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
      Facility facility = ContextHelper.getThreadFacility();
      Complex complex = facility.getFcyComplex();

      CarrierVisit arriveCv = CarrierVisit.getGenericTruckVisit(complex);
      CarrierVisit departCv = CarrierVisit.getGenericVesselVisit(complex);
      if (eq != null) {
        if (arriveCv != null || departCv != null) {
          if (inCategory.equalsIgnoreCase("IMPORT")) {
            ibcv = departCv; //carrier
            obcv = arriveCv;          //truck
          } else if (inCategory.equalsIgnoreCase("EXPORT")) {
            ibcv = arriveCv;       //truck
            obcv = CarrierVisit.getGenericVesselVisit(complex);     //carrier
          } else if (inCategory.equalsIgnoreCase("STORAGE")) {
            //ARGO-63479 fix - added inbound and outbound carrier for empty containers
            ibcv = arriveCv
            obcv = departCv
          }

          UnitFacilityVisit ufv = um.findOrCreatePreadvisedUnit(facility, eq.getEqIdFull(),
                  eq.getEqEquipType(), UnitCategoryEnum."$inCategory", FreightKindEnum."$inFreightKind", lop, ibcv, obcv,
                  DataSourceEnum.TESTING, null);
          if (UfvTransitStateEnum.S20_INBOUND.equals(ufv.getUfvTransitState())) {
            returnString = "Unit Preadvised";
          }

          //check if unit is created
          //if created , associate booking number to unit preadvised - ARGO-71404
          if (inBookingNumber != null) {
            if (ufv.ufvUnit != null) {
              FieldChanges changes = new FieldChanges();
              Unit unitObj = ((Unit) HibernateApi.getInstance().
                      getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(com.navis.inventory.InventoryEntity.UNIT)
                              .addDqPredicate(PredicateFactory.eq(com.navis.inventory.InventoryField.UNIT_ID, inUnitId))));
              Booking booking;
              List<Booking> bookings = Booking.findBookingsByNbr(inBookingNumber)
              bookings.each {
                booking = (Booking) it;
                if (booking != null) {
                  OrdersFinder ordersFinder = (OrdersFinder) Roastery.getBean(OrdersFinder.BEAN_ID);
                  EquipmentOrderItem eqoItem = ordersFinder.findEqoItemByEqType(booking, EquipType.findEquipType(inUnitType));
                  if (booking.eqboNbr.equalsIgnoreCase(inBookingNumber)) {
                    if (eqoItem != null) {
                      //attach booking to unit
                      unitObj.getUnitPrimaryUe().setUeOrderItem(eqoItem)
                    }
                  }
                }
              }
              returnString = 'Unit preadvised and booking associated successfully'
            }
          }
        } else {
          returnString = "Unit preadvise failed : carrier visit is null"
        }
      } else {
        returnString = "Unit preadvise failed : Unit is null"
      }
    } catch (Exception ex) {
      returnString = "Unit preadvise failed " + ex;
    }
    LOGGER.debug('PreAdviseRoadUnit :' + returnString + 'for unit : ' + inUnitId)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Create a new equipment in a given Truck
   *
   * @param inParameters The map containing the method name to call along with the parameters
   * command =  InitUnitInTruck<br>
   * unitId = Name of the Unit to be placed on truck<br>
   * unitType = ISO type of the Unit [2100,2200,4200,etc..] <br>
   * truckSlot = Truck Slot to place the container.<br>
   * @return JSON , <code>Unit created on truck</code><br><code>Unit not created on truck</code><br>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="InitUnitInTruck" /&gt;
   * &lt;parameter id="equipmentId" value="<Equipment Id>" /&gt;
   * &lt;parameter id="truckSlot" value="<truck Slot>" /&gt;
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String InitUnitInTruck(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="InitUnitInTruck" />
                                            <parameter id="unitId" value="<Equipment Id>" />
                                            <parameter id="unitType" value="<Equipment Id>" />
                                            <parameter id="truckSlot" value="<slot>" />'''

    String inEquipmentId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inTruckSlot = '';
    //Inbound : Truck id, Outbound :Gen_vessel
    try {
      CarrierVisit vesselVisit = CarrierVisit.getGenericVesselVisit(ContextHelper.getThreadComplex());
      CarrierVisit truckVisit;
      if (inParameters.containsKey('truckSlot')) {
        inTruckSlot = inParameters.get('truckSlot')
        truckVisit = CarrierVisit.findOrCreateActiveTruckVisit(ContextHelper.getThreadFacility(), inTruckSlot);
      } else {
        truckVisit = CarrierVisit.getGenericTruckVisit(ContextHelper.getThreadComplex())
      };
      ContextHelper.setThreadDataSource(DataSourceEnum.AUTOMATION);
      UnitFacilityVisit ufv = InventoryTestUtils.makeUfv(inEquipmentId, inUnitType, UfvTransitStateEnum.S30_ECIN, truckVisit, vesselVisit, '');

      returnString = 'Unit created on truck';
      assert ufv != null, returnString = 'Unit ' + inEquipmentId + ' not created on Truck';
    } catch (Exception ex) {
      returnString = 'Unit ' + inEquipmentId + ' not created on Truck:' + ex.printStackTrace();
    }
    LOGGER.debug('InitUnitInTruck :' + inEquipmentId + ' : ' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a unit in yard and place it in the given location. Also covers placing container in Rack
   *
   * @Precondition
   * yard location should already be available, to create please invoke CreateStackBlock<br>
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * <br>command=InitUnitInYard
   * <br>unitId=Name of the Container needs to be placed in yard
   * unitType=It takes the ISO code of the equipment, 2100,2200,4200,etc.<br>
   * location=location in the yard [YARDBLOCK.COLUMN.ROW.TIER //AS09B24.A where AS09 is the block name <br>
   * B is the column name, 24 is the row index and A is the Tier] This Format changes according to the <br>
   * category=IMPORT,EXPORT,TRANSSHIP,DOMESTIC,STORAGE,THROUGH <code>optional</code><br>
   * freightKind=MTY,FCL,LCL,BBK <code>optional</code><br/>
   * vesselVisitId=Id of the vessel visit <code>optional</code> <br>
   * internal format of each yard block.<br>
   * @return JSON , <code>Unit created in yard</code><br><code>Unit not created in yard</code>
   * @Example
   * Table invoked in SPARCS : inv_unit <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="InitUnitInYard" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000019"/&gt;<br>
   * &lt;parameter id="unitType" value="2100"/&gt;<br>
   * &lt;parameter id="location" value="AS09B24.A"/&gt;<br>
   * &lt;parameter id="category" value="IMPORT"/&gt;<br>
   * &lt;parameter id="freightKind" value="LCL"/&gt;<br>
   * &lt;parameter id="vesselVisitId" value="VV01"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String InitUnitInYard(Map inParameters) {
    assert inParameters.size() >= 4, '''Must supply 4 or more parameters:
                                        <parameter id="command" value="InitUnitInYard" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="unitType" value="<Unit Type ISO code> = 2100, 4200, etc.." />
                                        <parameter id="location" value="<Yard Location>" />
                                        <parameter id="category" value="IMPORT,EXPORT,TRANSSHIP,DOMESTIC,STORAGE,THROUGH"/>
                                        <parameter id="freightKind" value="MTY,FCL,LCL,BBK" />
                                        <parameter id="vesselVisitId" value="vessel visit id" />
                                        <parameter id="lineOperator" value="line Operator" />
                                        '''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inLocation = _testCommandHelper.checkParameter('location', inParameters);
    String inCategory = "", inCvId = "", inMode = "", inVesselVisitId = "", inLineOperator = "";
    UnitFacilityVisit ufv;
    CarrierVisit ibcv, obcv, vesselVisit, trainVisit;
    boolean setLineOp = false;
    try {
      CarrierVisit truckVisit = CarrierVisit.getGenericTruckVisit(ContextHelper.getThreadComplex());
      //.. if vesselVisitId is  passed into API then it creates a vesselvisit with the given id..
      if (inParameters.containsKey('vesselVisitId') && !inParameters.get('vesselVisitId').toString().isEmpty()) {
        inVesselVisitId = inParameters.get('vesselVisitId', inParameters);
        vesselVisit = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inVesselVisitId);
      } else   // if vesselVisitId is not passed into API then it creates a container with generic vessels.
      {
        vesselVisit = CarrierVisit.getGenericVesselVisit(ContextHelper.getThreadComplex())
      };

      if (inParameters.containsKey('lineOperator') && !inParameters.get('lineOperator').toString().isEmpty()) {
        inLineOperator = inParameters.get('lineOperator', inParameters);
        setLineOp = true;
      }

      ContextHelper.setThreadDataSource(DataSourceEnum.AUTOMATION);

      if (inParameters.containsKey('mode')) {
        inMode = inParameters.get('mode')
        if (inMode.equalsIgnoreCase("RAIL")) {
          //.. if railVisitId is  passed into API then it creates a generic rail visit with the given id..
          if (inParameters.containsKey('trainCvId') && !inParameters.get('trainCvId').toString().isEmpty()) {
            inCvId = inParameters.get('trainCvId', inParameters);
            trainVisit = CarrierVisit.findOrCreateTrainVisit(ContextHelper.getThreadFacility(), inCvId);
          } else   // if trainCvId is not passed into API then it creates a container with generic train visits.
          {
            trainVisit = CarrierVisit.getGenericTrainVisit(ContextHelper.getThreadComplex())
          }
          obcv = trainVisit;
          ibcv = vesselVisit;
        }
      } else if (inParameters.containsKey('category')) {  //if it contains category, check the category type and set the inbound,outbound
        inCategory = inParameters.get('category', inParameters);
        if (!inCategory.isEmpty()) {
          if (inCategory.equalsIgnoreCase("IMPORT")) {
            ibcv = vesselVisit;
            obcv = truckVisit;
          } else if (inCategory.equalsIgnoreCase("STORAGE")) {
            ibcv = truckVisit;
            obcv = truckVisit;
          } else if (inCategory.equalsIgnoreCase("EXPORT")) {
            ibcv = truckVisit;
            obcv = vesselVisit;
          } else {
            ibcv = truckVisit;
            obcv = vesselVisit;
          }
        } else {  //category is empty
          ibcv = truckVisit;
          obcv = vesselVisit;
        }
      } else {  //no category defined, set the inbound, outbound
        ibcv = truckVisit;
        obcv = vesselVisit;
      }
      ufv = InventoryTestUtils.makeUfv(inUnitId, inUnitType, UfvTransitStateEnum.S40_YARD, ibcv, obcv, inLocation);
      if (ufv != null) {
        Unit unit = ufv.getUfvUnit()
        if (setLineOp == true) {  //set line operator for the unit
          LineOperator line = LineOperator.findOrCreateLineOperator(inLineOperator);
          unit.updateLineOperator(line)
        }
        if (!inCategory.isEmpty() && inCategory.equalsIgnoreCase("STORAGE")) { //set freightkind = 'MTY' if catgroy is storage
          unit.setUnitFreightKind(FreightKindEnum.MTY);
        }
        returnString = 'Unit created in yard';
      } else {
        returnString = 'Unit not created in yard : ufv is null'
      };
      //assert ufv != null, returnString = 'Unit ' + inUnitId + ' not created in yard';
    } catch (Exception ex) {
      returnString = 'Unit not created in yard ' + ex;
    }
    LOGGER.debug('InitUnitInYard :' + inUnitId + ' : ' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Create a new equipment in a given vessel
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=InitUnitInVessel<br>
   * unitId=Name of the unit to be places on vessel<br>
   * vesselId=Name of the vessel<br>
   * vesselSlot=position for the vessel slot<br>
   * @return <code>Unit created on vessel</code> if success, else<br> <code>Failed to create unit on vessel</code>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="InitUnitInVessel" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000019"/&gt;<br>
   * &lt;parameter id="unitType" value="2100"/&gt;<br>
   * &lt;parameter id="vesselId" value="CMAV"/&gt;<br>
   * &lt;parameter id="vesselSlot" value="270694"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String InitUnitInVessel(Map inParameters) {
    assert inParameters.size() >= 5, '''Must supply 5 parameters:
                                        <parameter id="command" value="InitUnitInVessel" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="unitType" value="<Unit Type>" />
                                        <parameter id="vesselId" value="<Vessel Id" />
                                        <parameter id="vesselSlot" value="<Vessel Slot>" />
                                        <parameter id="mode" value="<Rail or Truck>" />
                                        <parameter id="outboundCvId" value="<Rail or Truck Id>" />
                                        <parameter id="lineOperator" value="<Line operator>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inVesselId = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inVesselSlot = _testCommandHelper.checkParameter('vesselSlot', inParameters);
    String inMode = '', inLineOperator = ''

    try {
      CarrierVisit outbound = CarrierVisit.getGenericTruckVisit(ContextHelper.getThreadComplex());
      CarrierVisit inbound = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inVesselId);
      if (inParameters.containsKey('lineOperator') && !inParameters.get('lineOperator').toString().isEmpty()) {
        inLineOperator = inParameters.get('lineOperator', inParameters);
      }

      if (inbound != null && outbound != null) {
        if (inbound.getCvVisitPhase() == CarrierVisitPhaseEnum.CREATED || inbound.getCvVisitPhase() == CarrierVisitPhaseEnum.INBOUND) {
          inbound.safelyUpdateVisitPhase(CarrierVisitPhaseEnum.ARRIVED);
        }
        if (inParameters.containsKey('mode')) {
          String inCvId = ''
          inMode = inParameters.get('mode')
          if (inParameters.containsKey('outboundCvId') && !inParameters.get('outboundCvId').toString().isEmpty()) {
            inCvId = inParameters.get('outboundCvId', inParameters)
          };
          if (inMode.equalsIgnoreCase("RAIL")) {
            //.. if railVisitId is  passed into API then it creates a rail visit with the given id..
            if (!inCvId.isEmpty()) {
              outbound = CarrierVisit.findOrCreateTrainVisit(ContextHelper.getThreadFacility(), inCvId)
            } else   // if trainCvId is not passed into API then it creates a container with generic train visits.
            {
              outbound = CarrierVisit.getGenericTrainVisit(ContextHelper.getThreadComplex())
            }
          } else if (inMode.equalsIgnoreCase('TRUCK')) {
            //.. if truck visit id is  passed into API then it creates a truck visit with the given id..
            if (!inCvId.isEmpty()) {
              outbound = CarrierVisit.findOrCreateActiveTruckVisit(ContextHelper.getThreadFacility(), inCvId)
            };
          }
        }
        ContextHelper.setThreadDataSource(DataSourceEnum.AUTOMATION);
        UnitFacilityVisit ufv = InventoryTestUtils.makeUfv(inUnitId, inUnitType, UfvTransitStateEnum.S20_INBOUND, inbound, outbound, inVesselSlot);
        assert ufv != null, returnString = 'Failed to create unit';
        LocPosition vesselPosition = LocPosition.createVesselPosition(inbound, inVesselSlot, DoorDirectionEnum.AFT.getKey());
        ufv.updateLastKnownPositionForCargo(vesselPosition);
        UnitYardVisit uyv = ufv.getUyvForYard(ContextHelper.getThreadYard());

        if (!inLineOperator.isEmpty()) {  //set line operator for the unit
          LineOperator line = LineOperator.findOrCreateLineOperator(inLineOperator);
          ufv.ufvUnit.updateLineOperator(line)
        }
        returnString = 'Unit created on vessel';
        assert uyv != null, returnString = 'Failed to create unit on vessel';
      } else {
        returnString = 'Failed to create unit on vessel: Visit is null'
      }
    } catch (Exception ex) {
      returnString = 'Failed to create unit on vessel:' + ex.printStackTrace()
    }
    LOGGER.debug('InitUnitInVessel :' + inUnitId + ' : ' + returnString)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Bundle  Units
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=BundleUnits<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="masterUnit" value="UNIT000001 " &gt;<br>
   * &lt;parameter id="slaveUnit" value="UNIT000002 " &gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String BundleUnits(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                    <parameter id="command" value="BundleUnits" />
                                    <parameter id="masterUnit" value="<unit1>" />
                                    <parameter id="slaveUnit" value="<unit2>" />'''

    String masterId = _testCommandHelper.checkParameter('masterUnit', inParameters);
    String slaveId = _testCommandHelper.checkParameter('slaveUnit', inParameters);

    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results1 = finder.findUfvByDigits(masterId, false, false);
    assert results1.getFoundCount() == 1, 'More than one or No equipment found for id ' + masterId;

    UnitFacilityVisit ufv1 = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results1.getFoundPrimaryKey());
    if (ufv1 != null) {
      Unit masterUnit = ufv1.getUfvUnit();

      try {
        UnitManager unitMgr = (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
        unitMgr.attachEquipment(masterUnit, slaveId, EqUnitRoleEnum.PAYLOAD);
        if (masterUnit.hasAnyAttachedUe() && masterUnit.getUnitIsBundle()) {
          returnString = "Units Bundled successfully";
        } else {
          returnString = "Units Bundle unsuccessfull";
        }
      } catch (Exception ex) {
        returnString = 'Exception occured : Bundling units unsuccessful' + ex;
      }
      LOGGER.debug('BundleUnits : ' + returnString)
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Creates chassis with the chassis name for the given equipment type
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateChassis<br>
   * chassisId=Name of the chassis to be created<br>
   * eqTypeId=Equipment Type id<br>
   * location=Yard slot<br>
   * lineOperator=Line Operator
   * @return JSON , <code>Chassis created</code> if creation successful,else<br>
   *               <code>Chassis not created</code>
   * @Example
   * Table invoked by SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateChassis" /&gt;<br>
   * &lt;parameter id="chassisId" value="CH01" /&gt;<br>
   * &lt;parameter id="eqTypeId" value="10" /&gt;<br>
   * &lt;parameter id="location" value="yard slot" /&gt;<br>
   * &lt;parameter id="lineOperator" value="APL" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String CreateChassis(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="CreateChassis" />
                                        <parameter id="chassisId" value="<chassis id>" />
                                        <parameter id="eqTypeId" value="<Equipment Type id>" />
                                        <parameter id="location" value="<slot>" />
                                        <parameter id="lineOperator" value="<line Operator>" />'''

    String inChassisId = _testCommandHelper.checkParameter('chassisId', inParameters);
    String inEqTypeId = _testCommandHelper.checkParameter('eqTypeId', inParameters);
    String inLineOperator = "", inLocation = ""

    try {
      Chassis chassis = InventoryTestUtils.createChassis(inChassisId, inEqTypeId);
      if (chassis != null) {
        UnitFacilityVisit ufv
        if (inParameters.containsKey('location') && !inParameters.get('location').toString().isEmpty()) {
          inLocation = inParameters.get('location')
          ufv = InventoryTestUtils.createInYardUfv(chassis, inLocation);
        }
        if (ufv != null) {
          Unit unit = ufv.getUfvUnit()
          Equipment equipment = unit.getPrimaryEq()
          if (inParameters.containsKey('lineOperator') && !inParameters.get('lineOperator').toString().isEmpty()) {
            inLineOperator = inParameters.get('lineOperator')
            LineOperator line = LineOperator.findOrCreateLineOperator(inLineOperator);
            unit.updateLineOperator(line)
            if(equipment != null)  {
              ScopedBizUnit equipmentOperator = ScopedBizUnit.findEquipmentOperator(inLineOperator);
              com.navis.inventory.business.units.EquipmentState.upgradeEqOperator(equipment, equipmentOperator, DataSourceEnum.IN_GATE);
            }
          }
        }
        returnString = 'Chassis created';
      } else {
        returnString = 'Chassis not created'
      }
    }
    catch (Exception ex) {
      returnString = 'Chassis not created ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Creates vessel visit with the given vessel name
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateVesselVisit<br>
   * vesselId=Name of the vessel visit to be created<br>
   * lineOpName=Line operator name for the vessel visit<br>
   * vesselClass=Vessel class name <code>optional</code><br>
   * @return JSON , <code>Vessel visit created</code> if creation successful,else<br>
   *               <code>Vessel visit not created</code>
   * @Example
   * Table invoked by SPARCS : argo_carrier_visit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateVesselVisit" /&gt;<br>
   * &lt;parameter id="vesselId" value="VESS01" /&gt;<br>
   * &lt;parameter id="vesselClass" value="VV" /&gt;<br>
   * &lt;parameter id="lineOpName" value="APL" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateVesselVisit(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="CreateVesselVisit" />
                                        <parameter id="vesselId" value="<vessel name>" />
                                        <parameter id="vesselClass" value="<class name of the vessel>" />
                                        <parameter id="lineOpName" value="<Line Operator Id>" />'''

    String inVesselName = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inVesselClass = ''
    if (inParameters.containsKey('vesselClass')) {
      inVesselClass = inParameters.get('vesselClass', inParameters)
    };
    String inLineOpId = _testCommandHelper.checkParameter('lineOpName', inParameters);
    try {
      //CarrierVisit theVisit = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inVesselName);
      com.navis.argo.business.model.Facility facility = ContextHelper.getThreadFacility()
      String crntPt = facility.getFcyRoutingPoint().getPointId();
      String vesSrvc = inVesselName;
      String vesId = vesSrvc;
      String vesName = vesId;
      String vesClass = "";
      vesClass = vesName
      if (inVesselClass != null) {
        if (!inVesselClass.isEmpty()) {
          vesClass = inVesselClass;
        }
      }

      if (inVesselName.length() > 8) {
        inVesselName = inVesselName.substring(0, 8); //supports 8digits
        vesSrvc = inVesselName + "SRVC"; //supports 16digits
        vesId = inVesselName.substring(0, 6) + "ID";//supports 8ditits
        vesName = inVesselName + "NAME";//supports 35 digits
        vesClass = inVesselName.substring(0, 4) + "CLSS"; //supports 8ditits
        if (inVesselClass != null) {
          if (!inVesselClass.isEmpty()) {
            vesClass = inVesselClass
          };
        }
      }

      String lloydsId = "AB" + new test.extension.groovy.TestCommandOCR.LloydsIdProvider().getNextLloydsId();//supports 8ditits
      if (lloydsId.length() > 8) {
        lloydsId = lloydsId.substring(0, 8);
      }
      String callSign = inVesselName + "_R";
      test.extension.groovy.TestCommandOCR.VoyageIdProvider voyageId = new test.extension.groovy.TestCommandOCR.VoyageIdProvider();
      String ibVoy = String.valueOf(voyageId.getNextVoyageId());//supports 6 ditits via UI
      if (ibVoy.length() > 6) {
        ibVoy = "I" + ibVoy.substring(0, 5);
      }
      String obVoy = String.valueOf(voyageId.getNextVoyageId());//supports 6 ditits via UI
      if (obVoy.length() > 6) {
        obVoy = "O" + obVoy.substring(0, 5);
      }

      CarrierVisit cv = test.extension.groovy.TestCommandOCR.getVvFinder().createTestVesselVisit(facility, vesClass, vesSrvc, inLineOpId, inVesselName,
              vesId, vesName, lloydsId, callSign, ibVoy, obVoy, null);
      returnString = 'Vessel visit created';
      assert cv != null, returnString = 'Vessel visit not created';
    } catch (Exception ex) {
      returnString = 'Vessel visit not created ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }


  private EquipType findEquipType(String inEqTypeID, EquipClassEnum inEqClass) {
    DomainQuery dq = QueryUtils.createDomainQuery(ArgoRefEntity.EQUIP_TYPE)
            .addDqPredicate(PredicateFactory.eq(ArgoRefField.EQTYP_ID, inEqTypeID))
            .addDqPredicate(PredicateFactory.eq(ArgoRefField.EQTYP_CLASS, inEqClass));
    Serializable[] primaryKeys = HibernateApi.getInstance().findPrimaryKeysByDomainQuery(dq);
    return primaryKeys == null || primaryKeys.length == 0 ? null : (EquipType) HibernateApi.getInstance().load(EquipType.class, (primaryKeys[0]));
  }

  /**
   * Creates unit in barge using barge stow plan in N4
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateBargeStowPlan<br>
   * vesselId=vessel visit id<br>
   * unitId=Name of the unit<br>
   * unitType=Equipment type - ISO <br>
   * lineOp=Line Operator<br>
   * @return JSON ,  <code>Created barge stow plan</code>  if success   <br>
   *                 <code>Updating call remarks failed, berthing/sequence not found for the vessel visit :</code><br>
   *                 <code>Failed to create barge stow plan' + inVesselId</code><br>
   *
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateBargeStowPlan"/&gt;<br>
   * &lt;parameter id="vesselId" value="APLCHO001"/&gt;<br>
   * &lt;parameter id="unitId" value="DS1"/&gt;<br>
   * &lt;parameter id="unitType" value="100"/&gt;<br>
   * &lt;parameter id="lineOp" value="100"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateBargeStowPlan(Map inParameters) {
    assert inParameters.size() >= 5, '''Must supply 5 parameters:
                                        <parameter id="command" value="CreateBargeStowPlan" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="unitType" value="<Unit Type>" />
                                        <parameter id="vesselId" value="<Vessel Id" />
                                        <parameter id="lineOp" value="Line operator" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inVesselId = _testCommandHelper.checkParameter('vesselId', inParameters);
    String inLineOp = _testCommandHelper.checkParameter('lineOp', inParameters);

    try {
      Equipment equipment = Container.findOrCreateContainer(inUnitId, inUnitType, DataSourceEnum.EDI_STOW);
      CarrierVisit outbound = CarrierVisit.getGenericTruckVisit(ContextHelper.getThreadComplex());
      if (equipment != null) {
        CarrierVisit carrierVisit = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inVesselId);
        EdiPostingContext ediPostingContext = new EdiPostingContext();
        Facility fcy = carrierVisit.getCvFacility()
        ScopedBizUnit eqUserScopedBizUnit = ScopedBizUnit.resolveScopedBizUnit(inLineOp, inLineOp, BizRoleEnum.LINEOP);
        UnitFacilityVisit ufv = getUnitMngr().findOrCreateBargeStowplanUnit(equipment, carrierVisit, eqUserScopedBizUnit, fcy, UnitCategoryEnum.IMPORT);
        if (ufv != null) {
          FieldChanges changes = new FieldChanges();
          changes.setFieldChange(UnitField.UFV_UNIT_CATEGORY, UnitCategoryEnum.IMPORT)
          ufv.applyFieldChanges(changes)
          RectifyParms parms = new RectifyParms();
          parms.setObCv(outbound);
          parms.setUfvTransitState(UfvTransitStateEnum.S20_INBOUND)
          getUnitMngr().rectifyUfv(ufv, parms);
          returnString = 'Created barge stow plan'
        } else {
          returnString = 'Failed to create barge stow plan'
        }
      }
    } catch (Exception ex) {
      returnString = 'Failed to create barge stow plan :' + ex
    }
    LOGGER.debug(returnString + ':' + inUnitId)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Logs messages to N4<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=SendToN4Log<br>
   * @return JSON , <code>SendToN4Log</code><br>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SendToN4Log" /&gt;<br>
   * &lt;parameter id="message" value="Work queue activated" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String SendToN4Log(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="SendToN4Log" />
                                        <parameter id="message" value="Work Queue Activated" />'''

    String inMessage = _testCommandHelper.checkParameter('message', inParameters);

    LOGGER.debug('SendToN4Log :' + inMessage)
    returnString = 'SendToN4Log : ' + inMessage
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Logs messages to N4<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=EnableN4Logging<br>
   * @return JSON , <code>Unit Purged</code><br>
   *                <code>Unit not purged</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="EnableN4Logging" /&gt;<br>
   * &lt;parameter id="className" value="fully qualified class name for which logging needs to be enabled" /&gt;<br>
   * &lt;parameter id="level" value="DEBUG,WARN,ALL" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String EnableN4Logging(Map inParameters) {
    assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="EnableN4Logging" />
                                        <parameter id="className" value="fully qualified class name " />
                                        <parameter id="level" value="DEBUG" />'''

    String inClassName = _testCommandHelper.checkParameter('className', inParameters);
    String inLevel = _testCommandHelper.checkParameter('level', inParameters);
    Logger.getLogger(inClassName).setLevel(Level."$inLevel");
    LOGGER.debug('Enabled N4 logging for the class :' + inClassName)
    returnString = 'Enabled N4 logging for the class : ' + inClassName
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Posts xml rdt  message to ECN4<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=PostXmlRdt<br>
   * @return JSON , <code>PostXmlRdt : <response> </code><br>
   *                <code>PostXmlRdt failed:</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PostXmlRdt" /&gt;<br>
   * &lt;parameter id="message" value="xml rdt message to be post to ecn4" /&gt;<br>
   * &lt;parameter id="serverUrl" value="URL of the ECN4" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String PostXmlRdt(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 2 parameters:
                                        <parameter id="command" value="PostXmlRdt" />
                                        <parameter id="message" value="xml rdt message to be post to ecn4" />
                                         <parameter id="serverUrl" value="URL of the ECN4" />'''

    String inMessage = _testCommandHelper.checkParameter('message', inParameters);
    String inServerUrl = _testCommandHelper.checkParameter('serverUrl', inParameters);
    def result

    try {
      if (inMessage.contains("&lt;")) {
        inMessage = inMessage.replaceAll("&lt;", "<")
      }
      if (inMessage.contains("&gt;")) {
        inMessage = inMessage.replaceAll("&gt;", ">")
      }
      if (inMessage.contains("&quot;")) {
        inMessage = inMessage.replaceAll("&quot;", "\"")
      }
      def response = XmlRdtTestUtils.postXmlRdt(inServerUrl, inMessage)
      if (response.contains("&lt;")) {
        response = response.replaceAll("&lt;", "<")
      }
      if (response.contains("&gt;")) {
        response = response.replaceAll("&gt;", ">")
      }
      if (response.contains("\\n")) {
        response = response.replaceAll("\\n", "")
      }
      if (response.contains("\\")) {
        response = response.replaceAll("\\", "")
      }
      result = response
      LOGGER.debug('PostXmlRdt :' + response)
      returnString = 'PostXmlRdt : ' + response
    } catch (Exception ex) {
      LOGGER.debug('PostXmlRdt failed:' + ex)
      returnString = 'PostXmlRdt failed : ' + ex
    }
    builder {
      actual_result returnString;
      data('PostXmlRdt': result)
    }
    return builder;
  }

  /**
   * Loads drive time from TEAMS for advanced mode AGV Scheduler <br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=LoadDriveTimefromTEAMS<br>
   * @return JSON , <code>Loaded drive time from TEAMS</code><br>
   *                <code>Loading drive time from TEAMS failed</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="LoadDriveTimefromTEAMS" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String LoadDriveTimefromTEAMS(Map inParameters) {
    assert inParameters.size() >= 1, '''Must supply 2 parameters:
                                        <parameter id="command" value="LoadDriveTimefromTEAMS" />'''
    try {
      TeamsDriveTimeEmulationHelperEnhanced driveTimeEmulationHelperEnhanced = null;
      driveTimeEmulationHelperEnhanced = ControlTestUtils.createAndRunTeamsDriveTimeEmulationHelper()
      if (driveTimeEmulationHelperEnhanced != null) {
        LOGGER.debug('Loaded drive time from TEAMS')
        returnString = 'Loaded drive time from TEAMS'
      } else {
        LOGGER.debug('Loading drive time from TEAMS failed')
        returnString = 'Loading drive time from TEAMS failed'
      }
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(),ex.getCause())
      LOGGER.error(ex.getStackTrace())
      returnString = 'Loading drive time from teams failed with exception : ' + ex.getMessage()
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  //Sep 1st added by Sharanya
  //AGV - BETA APIs
  /**
   * Pre Dispatches move to the given AGV and the status of the WA created will remain in 'Pending_Dispatch'
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PreDispatchMove<br>
   * cheId=Id of the CHE<br>
   * wiGkey=work instruction gkey<br>
   * @return JSON ,  <code>Predispatch move created</code>  if success   <br>
   *                 <code>Failed to create predispatch move</code> if failed creating WA <br>
   *                 <code>Failed to create predispatch move</code> if there is an exception while creating WA <br>
   *
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PreDispatchMove"/&gt;<br>
   * &lt;parameter id="wiGkey" value="12456"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV510"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PreDispatchMove(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PreDispatchMove" />
                                        <parameter id="wiGkey" value="<work instruction which needs to be predispatched>" />
                                        <parameter id="cheId" value="<AGV/ASC name>" />'''


    String wiGKey = _testCommandHelper.checkParameter('wiGkey', inParameters);
    String cheId = _testCommandHelper.checkParameter('cheId', inParameters);
    WorkInstruction tzWorkInstruction = null;
    def waGKey
    try {
      WorkInstruction workInstruction = WorkInstruction.hydrate(wiGKey);
      //find che
      Che cheObj = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheId))));
      WorkAssignment wa = ControlTestUtils.getControlDispatcher()
              .preDispatchUnitMove(workInstruction, cheObj.cheGkey, WaMovePurposeEnum.ITV_RECEIVE,
              null, QueueServerClass.getSynchronousTransferTypeRequest(false),
              null, QueueServerClass.getSynchronousTransferTypeRequest(false),
              new Date());
      if (wa != null) {
        waGKey = wa.workassignmentGkey
        LOGGER.info('Predispatch move created with WA gkey : ' + waGKey + ' for AGV : ' + cheId + 'for the given WI:' + wiGKey);
        returnString = 'Predispatch move created'
      } else {
        LOGGER.info('Failed to create predispatch move for AGV : ' + cheId + 'for the given WI:' + wiGKey);
        returnString = 'Failed to create predispatch move'
      }
    } catch (Exception ex) {
      returnString 'Failed to create WA in Pending_Dispatch state : ' + ex
    }
    builder {
      actual_result returnString
      if (waGKey != null) {
        data('WaGKey': waGKey)
      }
    }
    LOGGER.debug('PreDispatchMove' + returnString + 'for che :' + cheId + 'with wi:' + wiGKey)
    return builder;
  }

  /**
   * Cancels Job assigned for ASC or AGV.<br>
   * <a href="http://jira.navis.com/browse/ARGO-44374"> ARGO-44374 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CancelJob<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CancelJob"/&gt;<br>
   * &lt;parameter id="cheId" value="AGV01"/&gt;<br>
   * &lt;parameter id="waGkey" value="1234"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CancelJob(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                 <parameter id="command" value="CancelJob" />
								 <parameter id="cheId" value="<CHE Id>" />
                                 <parameter id="waGkey" value="Work Assignment Gkey" />'''

    String cheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String waGkey = _testCommandHelper.checkParameter('waGkey', inParameters);

    Long waStatus = 0L;
    String waStatusEnum = "";

    Che che = Che.findCheByShortName(cheId, ContextHelper.getThreadYard());
    if (che != null) {
      Long chePkey = che.getChePkey();

      DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT)
              .addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_CHE_PKEY, chePkey))
              .addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_GKEY, Long.parseLong(waGkey)))
              .addDqPredicate(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_STATUS_ENUM, WaStatusEnum.PENDING_DISPATCH));

      WorkAssignment wa = (WorkAssignment) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);

      if (wa != null) {
        if (wa.getWorkassignmentMovePurposeEnum().equals(MovePurposeEnum.PREPOSITION)) {
          returnString = 'Work Assignment for CHE:' + cheId + ' with Move Purpose: ' + wa.getWorkassignmentMovePurposeEnum().getName() +
                  ' cannot be cancelled';
        } else {
          waStatus = new Long(6);
          waStatusEnum = "PENDING_REJECTION"
          wa.setWorkassignmentStatus(waStatus);
          wa.setWorkassignmentStatusEnum(WaStatusEnum.PENDING_REJECTION);
          HibernateApi.getInstance().update(wa);
          returnString = 'Work Assignment for CHE:' + cheId + ' is cancelled; WA status: ' + waStatusEnum +
                  '; Move Purpose: ' + wa.getWorkassignmentMovePurposeEnum().getName();
        }
      } else {
        returnString = 'Work Assignment does not exist for CHE in Pending Dispatch state';
      }
    } else {
      returnString = 'Please specify a valid CHE';
    }

    builder {
      actual_result returnString;
      data('WaGKey': waGkey);
      if (waStatus != 0L) {
        data('WaStatus': waStatus);
      }
      if (!waStatusEnum.isEmpty()) {
        data('WaStatusEnum': waStatusEnum);
      }
    }
    return builder;
  }

  /**
   * Twin up Work instructions - functionality<br>
   * <a href="http://jira.navis.com/browse/ARGO-45131"> ARGO-45131 </a>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=TwinUpPlans<br>
   * @return JSON ,
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="TwinUpPlans"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String TwinUpPlans(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameter:
                                        <parameter id="command" value="TwinUpPlans" />
                                        <parameter id="wiGkey1" value="123" />
                                        <parameter id="wiGkey2" value="456" />'''
    try {
      Serializable wiGkey1 = _testCommandHelper.checkParameter("wiGkey1", inParameters);
      Serializable wiGkey2 = _testCommandHelper.checkParameter("wiGkey2", inParameters);
      WorkInstruction wi1 = WorkInstruction.hydrate(wiGkey1);
      WorkInstruction wi2 = WorkInstruction.hydrate(wiGkey2);
      ControlTestUtils.twinUpPlans(wi1, wi2);
      returnString = "Plans were successfully twinned";
    }
    catch (ObjectNotFoundException inEx) {
      returnString = 'No work instruction was found with given gkey. Plans not twinned';
    }
    catch (Exception ex) {
      returnString = 'Plans not twinned ' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('TwinUpPlans:' + returnString)
    return builder;
  }

  //APIs added for BETA - Sharanya - 19th July - NFRM - 950
/**
 * Create a new equipment in a given Truck
 *
 * @param inParameters The map containing the method name to call along with the parameters
 * command =  InitUnitInRailCar<br>
 * unitId = Name of the Unit to be placed on truck<br>
 * unitType = ISO type of the Unit [2100,2200,4200,etc..] <br>
 * truckSlot = Truck Slot to place the container.<br>
 * @return JSON , <code>Unit created on rail car</code><br><code>Unit not created on rail car</code><br>
 * @Example
 * Table invoked in SPARCS : inv_unit<br>
 * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
 * &lt;parameters&gt;<br>
 * &lt;parameter id="command" value="InitUnitInRailCar" /&gt;
 * &lt;parameter id="unitId" value="Name of the unit" /&gt;
 * &lt;parameter id="unitType" value="ISO code for the unit to be created, 2200,4200" /&gt;
 * &lt;parameter id="trainCvId" value="AGVTRIN01" /&gt;
 * &lt;parameter id="railCarCvId" value="RAILIN01" /&gt;
 * &lt;parameter id="posOnRailcar" value="2B1" /&gt;
 * &lt;parameter id="railCarPosition" value="TRPL__A__1" /&gt;
 * &lt;/parameters&gt;<br>
 * &lt;/groovy&gt;<br>
 */
  public String InitUnitInRailCar(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="InitUnitInRailCar" />
                                            <parameter id="unitId" value="<Equipment Id>" />
                                            <parameter id="unitType" value="<Equipment Id>" />
                                            <parameter id="railCarCvId" value="<rail carrier visit location>"
                                            <parameter id="trainCvId" value="AGVTRIN01" />
                                            <parameter id="posOnRailcar" value="position" />
                                            <parameter id="railCarLocation" value="location in rail car position" />'''
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
    String inRailCarCvId = _testCommandHelper.checkParameter('railCarCvId', inParameters);
    String inPosOnRailCar = _testCommandHelper.checkParameter('posOnRailcar', inParameters);
    String inRailCarPosition = inParameters.get('railCarLocation');
    String railCvGKey = ''
    final long RAILCAR_SEQUENCE = 1;
    final ControlRailTestUtils.RailCarTypeEnum RAILCAR_TYPE_ENUM_S140 = ControlRailTestUtils.RailCarTypeEnum.S1X40

    try {
      UnitYardVisit unitYardVisit;
      unitYardVisit = ControlRailTestUtils.createUnitOnRailcar(inUnitId, inUnitType, inTrainCvId, inRailCarCvId, RAILCAR_SEQUENCE,
              RAILCAR_TYPE_ENUM_S140, inPosOnRailCar, TrainDirectionEnum.THROUGH, inRailCarPosition)
      if (unitYardVisit != null) {
        UnitFacilityVisit ufv = unitYardVisit.getUyvUfv();
        RoutingPoint pod = RoutingPoint.findOrCreateRoutingPoint("NLRTM", "NLRTM");
        ufv.ufvUnit.unitRouting.setRtgPOD1(pod)
        returnString = 'Unit created on rail car'
      } else {
        returnString = 'Unit not created on rail car - UnitYardVisit is null'
      }
    }
    catch (BizFailure inBizFailure) {
      returnString = inBizFailure.message;
    } catch (Exception ex) {
      returnString = 'Unit ' + inUnitId + ' not created on rail car:' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('InitUnitInRailCar:' + returnString)
    return builder;
  }

  /**
   * Create a new equipment in yard which will be loaded to Rail
   *
   * @param inParameters The map containing the method name to call along with the parameters
   * command =  InitUnitInYardForRail<br>
   * unitId = Name of the Unit to be placed on truck<br>
   * unitType = ISO type of the Unit [2100,2200,4200,etc..] <br>
   * truckSlot = Truck Slot to place the container.<br>
   * @return JSON , <code>Unit created on rail car</code><br><code>Unit not created on rail car</code><br>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="InitUnitInYardForRail" /&gt;
   * &lt;parameter id="unitId" value="Name of the unit" /&gt;
   * &lt;parameter id="unitType" value="ISO code for the unit to be created, 2200,4200" /&gt;
   * &lt;parameter id="trainCvId" value="AGVTRIN01" /&gt;
   * &lt;parameter id="railCarCvId" value="RAILIN01" /&gt;
   * &lt;parameter id="origin" value="780133.1" /&gt;
   * &lt;parameter id="railCarPosition" value="TRPL__A__1" /&gt;
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String InitUnitInYardForRailLoad(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="InitUnitInYardForRail" />
                                            <parameter id="unitId" value="<Equipment Id>" />
                                            <parameter id="unitType" value="<Equipment Id>" />
                                            <parameter id="railCarCvId" value="<rail carrier visit location>"
                                            <parameter id="trainCvId" value="AGVTRIN01" />
                                            <parameter id="origin" value="yard location" />
                                            <parameter id="vesselVisitId" value="vessel visit id" />
                                            <parameter id="railCarLocation" value="location in rail car position" />'''
    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
    String inRailCarCvId = _testCommandHelper.checkParameter('railCarCvId', inParameters);
    String inOriginId = _testCommandHelper.checkParameter('origin', inParameters);
    String inRailCarPosition = _testCommandHelper.checkParameter('railCarLocation', inParameters);
    String inVesselVisitId = _testCommandHelper.checkParameter('vesselVisitId', inParameters);
    String railCvGKey = ''
    final long RAILCAR_SEQUENCE = 1;

    try {
      UnitYardVisit unitYardVisit;
      // TrainVisitDetails and RailcarVisit
      Serializable tvdGKey = ControlRailTestUtils.createTrainVisitDetails(inTrainCvId, TrainDirectionEnum.OUTBOUND);
      Serializable railcarVisitGKey = ControlRailTestUtils.createRailcarVisit(tvdGKey, inRailCarCvId, RAILCAR_SEQUENCE,
              ControlRailTestUtils.RailCarTypeEnum.S2X20, inRailCarPosition);
      RailcarVisit railcarVisit = RailcarVisit.hydrate(railcarVisitGKey);
      railCvGKey = String.valueOf(railcarVisitGKey)
      //LocPosition railcarPos = LocPosition.createRailcarPosition(railcarVisit, inRailCarPosition, null);
      LocPosition railcarPos = LocPosition.createYardRailTrackPosition(ContextHelper.getThreadYard(), inRailCarPosition, true);

      TrainVisitDetails trainVisitDetails = TrainVisitDetails.hydrate(tvdGKey);
      CarrierVisit trainVisit = trainVisitDetails.getCvdCv()
      unitYardVisit = ControlRailTestUtils.createUnitInYard(inUnitId, inUnitType, inOriginId, null,
              trainVisit);
      if (unitYardVisit != null) {
        CarrierVisit vesselVisit = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inVesselVisitId);
        UnitFacilityVisit ufv = unitYardVisit.getUyvUfv();
        ufv.updateActualIbCv(vesselVisit)
        RoutingPoint pod = RoutingPoint.findOrCreateRoutingPoint("NLRTM", "NLRTM");
        ufv.ufvUnit.unitRouting.setRtgPOD1(pod)
        returnString = 'Unit created on yard to load on rail'
      } else {
        returnString = 'Unit not created on yard to load on rail - UnitYardVisit is null'
      }
    } catch (BizFailure inBizFailure) {
      returnString = inBizFailure.message;
    } catch (Exception ex) {
      returnString = 'Unit ' + inUnitId + ' not created on yard to load on rail car:' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
      if (!railCvGKey.isEmpty()) {
        data('railCarVisitGKey': railCvGKey)
      }
    }
    LOGGER.debug('InitUnitInYardForRail:' + returnString)
    return builder;
  }

  //APIs added for BETA - Sharanya - 19th August - NFRM - 950
/**
 * Creates association between the track plan and the transfer zone
 *
 * @param inParameters The map containing the method name to call along with the parameters
 * command =  AssociateTrackPlanWithTZ<br>
 * railCarTrackSpot = the area in the yard<br>
 * trackPlan = Name of the track plan <br>
 * trackPlanTZ = Name of the track plan transfer zone.<br>
 * railCarCvId = Carrier visit of the rail car.<br>
 * @return JSON , <code>Track plan associated with transfer zone successfully</code><br><code>Track plan not associated with transfer zone : </code><br>
 * @Example
 * Table invoked in SPARCS : inv_unit<br>
 * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
 * &lt;parameters&gt;<br>
 * &lt;parameter id="command" value="AssociateTrackPlanWithTZ" /&gt;
 * &lt;parameter id="railCarTrackSpot" value="RAILT11__1" /&gt;
 * &lt;parameter id="trackPlan" value="RAIL" /&gt;
 * &lt;parameter id="trackPlanTZ" value="RA1" /&gt;
 * &lt;parameter id="railCarCvId" value="RAILCAR1" /&gt;
 * &lt;/parameters&gt;<br>
 * &lt;/groovy&gt;<br>
 */
  public String AssociateTrackPlanWithTZ(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="AssociateTrackPlanWithTZ" />
                                            <parameter id="railCarTrackSpot" value="RAILT11__1" />
                                            <parameter id="trackPlan" value="RAIL" />
                                            <parameter id="trackPlanTZ" value="RA1"/>
                                            <parameter id="railCarCvId" value="<rail carrier visit location>'''
    String inRailCarTrackSpot = _testCommandHelper.checkParameter('railCarTrackSpot', inParameters);
    String inTrackPlan = _testCommandHelper.checkParameter('trackPlan', inParameters);
    String inTrackPlanTZ = _testCommandHelper.checkParameter('trackPlanTZ', inParameters);
    String inRailCarCvId = _testCommandHelper.checkParameter('railCarCvId', inParameters);

    try {
      // set the railcar track spot and position
      // since yard is not configured yet: we need to do it here
      if (!inRailCarTrackSpot.isEmpty()) {
        LocPosition trackSpotPosition = LocPosition.createTrackPosition(ContextHelper.getThreadYard(), inRailCarTrackSpot, true);

        if (trackSpotPosition != null && trackSpotPosition.getPosBin() != null) {
          RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
          final RailcarVisit rCarVisit = rm.findActiveRailcarVisit(ContextHelper.getThreadFacility(), inRailCarCvId);
          rCarVisit.setRcarvPosition(trackSpotPosition);
        } else {
          returnString = 'Associating Track Plan With Transfer Zone failed, trackSpotPosition : ' + trackSpotPosition
        }
      }
      // associate Track plan with TZ
      if (inTrackPlan != null) {
        YardBinModel model = YardBinModel.downcast(ContextHelper.getThreadYard().getYrdBinModel());
        TrackPlan trackPlan = TrackPlan.findTrackPlanFromParentBinAndName(model, inTrackPlan);

        AbstractBin transferZone = YardTestUtils.findStackBlock(inTrackPlanTZ);

        TransferZoneAssociation association =
          YardTestUtils.findOrCreateTransferZoneAssociation(trackPlan, transferZone, TZBlockAssociationEnum.ROWHIGH);
        ArrayList<WiMoveKindEnum> enumList = new ArrayList<WiMoveKindEnum>();
        enumList.add(WiMoveKindEnum.RailDisch)
        enumList.add(WiMoveKindEnum.RailLoad)
        enumList.add(WiMoveKindEnum.YardMove)
        WiMoveKindEnum[] moveKindEnums = (WiMoveKindEnum[]) enumList.toArray();
        if (association != null) {
          YardTestUtils.createOrUpdateMoveKindsAllowed(transferZone, moveKindEnums);
        }
      }
      returnString = 'Track plan associated with transfer zone successfully'
    }
    catch (BizFailure inBizFailure) {
      returnString = inBizFailure.message;
    } catch (Exception ex) {
      returnString = 'Track plan not associated with transfer zone :' + ex.printStackTrace();
    }
    builder {
      actual_result returnString
    }
    LOGGER.debug('AssociateTrackPlanWithTZ' + returnString)
    return builder;
  }

  /**
   * Creates train visit with the given rail road Id and name
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateTrainVisit<br>
   * railRoadId=Id of rail road<br>
   * railRoadName=name of the rail road<br>
   * trainCvId=train carrier visit id<br>
   * trainDirection=train direction enum can be INBOUND/OUTBOUNd/THROUGH<br>
   * @return <code>Train visit created</code> if executed successfully<br>
   *         <code>Query not executed</code> failed to execute query
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateTrainVisit"/&gt;<br>
   * &lt;parameter id="railRoadId" value="RR1/&gt;<br>
   * &lt;parameter id="railRoadName" value="AGV Rail Roads/&gt;<br>
   * &lt;parameter id="trainCvId" value="RR1/&gt;<br>
   * &lt;parameter id="trainDirection" value="INBOUND,OUTBOUND,THROUGH/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateTrainVisit(Map inParameters) {
    assert inParameters.size() >= 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first two parameters:
                                        <parameter id="command" value="CreateTrainVisit" />
                                        <parameter id="railRoadId" value="rid of rail road" />
                                        <parameter id="railRoadName" value="rail road name" />
                                        <parameter id="trainCvId" value="train carrier visit" />
                                        <parameter id="trainDirection" value="Inbound/Outbound" />'''

    String inRailRoadId = _testCommandHelper.checkParameter('railRoadId', inParameters);
    String inRailRoadName = _testCommandHelper.checkParameter('railRoadName', inParameters);
    String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
    String inTrainDirection = _testCommandHelper.checkParameter('trainDirection', inParameters);
    try {
      TrainVisitDetails trainVisitDetails = ControlRailTestUtils.findOrCreateTrainVisitDetails(inRailRoadId,inRailRoadName,inTrainCvId,TrainDirectionEnum."$inTrainDirection")
      if(trainVisitDetails != null)
        returnString = 'Train visit created'
      else
        returnString = "Failed to create train visit"
    } catch (Exception ex) {
      returnString = 'Exception while creating train visit ' + ex
    }

    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Generic API call to fetch data from DB for any entity in N4 using sql query
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ExecuteN4Query<br>
   * sqlStatement=SQL query<br>
   * @return <code>Executed Query</code> if executed successfully<br>
   *         <code>Query not executed</code> failed to execute query
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ExecuteN4Query"/&gt;<br>
   * &lt;parameter id="sqlStatement" value="select * from xps_che/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ExecuteN4Query(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first two parameters:
                                        <parameter id="command" value="ExecuteN4Query" />
                                        <parameter id="sqlStatement" value="sql statement" />'''

    String inSQLStatement = _testCommandHelper.checkParameter('sqlStatement', inParameters);
    try {
      if (inSQLStatement.contains('SELECT') || inSQLStatement.contains('select')) {
        SQLQuery sqlQuery = HibernateApi.getInstance().getCurrentSession().createSQLQuery(inSQLStatement);
        returnString = 'Executed query succesfully ' + sqlQuery.list();
      } else {
        Session session = HibernateApi.getInstance().getCurrentSession()
        SQLQuery sqlQuery = session.createSQLQuery(inSQLStatement)
        def update = sqlQuery.executeUpdate()
        LOGGER.debug('Executed Query:' + inSQLStatement + ':' + update)
        returnString = 'Executed Query';
      }
    } catch (Exception ex) {
      returnString = 'Query not executed :' + ex.getStackTrace() + ':' + ex.getMessage()
    }
    LOGGER.debug(returnString)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Adds given empty container into the given equipment receive order
   *
   * @param inParameters The map containing the method name to call along with the parameters
   * command =  AddEmptyContainerToEquipmentReceiveOrder<br>
   * unitId = Name of the Unit <br>
   * unitType = ISO type of the Unit [2100,2200,4200,etc..] <br>
   * eroNumber = import delivery order number<br>
   * lineOperator = name of the line operator<br>
   * quantity = number of order items<br>
   * @return JSON , <code>Unit added to the given equipment receive order successfully</code><br>
   *                <code>No Equipment Receive Order found for the given eroNumber : </code><br>
   * @Example
   * Table invoked in SPARCS : inv_unit<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="AddEmptyContainerToEquipmentReceiveOrder" /&gt;<br>
   * &lt;parameter id="unitId" value="SHAU8765432" /&gt;<br>
   * &lt;parameter id="unitType" value="2200" /&gt;<br>
   * &lt;parameter id="eroNumber" value="1" /&gt;<br>
   * &lt;parameter id="lineOperator" value="APL" /&gt;<br>
   * &lt;parameter id="quantity" value="10" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String AddEmptyContainerToEquipmentReceiveOrder(Map inParameters) {
    assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="AddEmptyContainerToEquipmentReceiveOrder" />
                                            <parameter id="unitId" value="<unit Id>" />
                                            <parameter id="unitType" value="<unit iso type>" />
                                            <parameter id="eroNumber" value="<equipment delivery number>" />
                                            <parameter id="lineOperator" value="<Line Operator>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inEroNumber = _testCommandHelper.checkParameter('eroNumber', inParameters);
    String inLineOperator = _testCommandHelper.checkParameter('lineOperator', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    String inQuantity = _testCommandHelper.checkParameter('quantity', inParameters);
    try {
      ScopedBizUnit lop = ScopedBizUnit.findScopedBizUnit(inLineOperator, BizRoleEnum.LINEOP);
      EquipmentReceiveOrder equipmentReceiveOrder = EquipmentReceiveOrder.findEroByUniquenessCriteria(inEroNumber,lop)
      if(equipmentReceiveOrder != null) {
        Unit unitObj = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(com.navis.inventory.InventoryEntity.UNIT)
                .addDqPredicate(PredicateFactory.eq(com.navis.inventory.InventoryField.UNIT_ID, inUnitId))));
        if (unitObj != null) {
          EquipType equipType = EquipType.findEquipType(inUnitType);
          EquipmentOrderItem equipmentOrderItem = EquipmentOrderItem.findOrCreateOrderItem(equipmentReceiveOrder,inQuantity.toLong(),equipType)
          if(equipmentOrderItem != null) {
            equipmentOrderItem.preadviseUnit(unitObj);
          } else returnString = 'No order items found or created for the given Equipment Receive Order'
          returnString = 'Unit added to the given equipment receive order successfully'
        } else {
          returnString = 'No Unit found for the give unit id : ' + inUnitId
        }
      } else returnString = 'No Equipment Receive Order found for the given eroNumber : ' + inEroNumber;
    } catch (Exception ex) {
      returnString = 'Unit ' + inUnitId + ' not added to given Equipment Receive Order:' + ex;
    }
    LOGGER.debug('AddEmptyContainerToEquipmentReceiveOrder :' + inUnitId + ' : ' + returnString)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Creates new booking with the given lineOperator,port of discharge <br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateBooking<br>
   * bookingNumber=unique booking number<br>
   * lineOperator=line operator<br>
   * cvId = carrier visit id<br>
   * unitCategory = category of the unit : EXPORT/IMPORT/STORAGE <br>
   * portOfDischarge = discharge port name <br>
   * portOfLoad = loading port name <br>
   * quantity = number of booking items <br>
   * unitType = ISO type of the unit like 2200,4000 <br>
   * @return <code>Created Booking</code> if created successfully<br>
   *         <code>Booking not created</code> failed to create booking
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateBooking"/&gt;<br>
   * &lt;parameter id="bookingNumber" value="CreateBooking"/&gt;<br>
   * &lt;parameter id="lineOperator" value="APL"/&gt;<br>
   * &lt;parameter id="cvId" value="APLCHO001"/&gt;<br>
   * &lt;parameter id="unitCategory" value="EXPORT/IMPORT/STORAGE"/&gt;<br>
   * &lt;parameter id="portOfDischarge" value="HKHKG"/&gt;<br>
   * &lt;parameter id="portOfLoad" value="HKHKG"/&gt;<br>
   * &lt;parameter id="quantity" value="10"/&gt;<br>
   * &lt;parameter id="unitType" value="2200"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateBooking(Map inParameters) {
    assert inParameters.size() >= 5, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply atleast first two parameters:
                                        <parameter id="command" value="CreateBooking" />
                                        <parameter id="bookingNumber" value="unique number for booking" />
                                        <parameter id="lineOperator" value="name of the line operator" />
                                        <parameter id="cvId" value="carrier visit" />
                                        <parameter id="unitCategory" value="EXPORT/IMPORT/STORAGE" />
                                        <parameter id="portOfDischarge" value="" />
                                        <parameter id="portOfLoad" value="" />
                                        <parameter id="quantity" value="number of booking items" />
                                        <parameter id="unitType" value="<Unit Type>" />'''

    String inBookingNumber = _testCommandHelper.checkParameter('bookingNumber', inParameters);
    String inLineOperator = _testCommandHelper.checkParameter('lineOperator', inParameters);
    String inCvId = _testCommandHelper.checkParameter('cvId', inParameters);
    String inPOD = _testCommandHelper.checkParameter('portOfDischarge', inParameters);
    String inPOL = _testCommandHelper.checkParameter('portOfLoad', inParameters);
    String inQuantity = _testCommandHelper.checkParameter('quantity', inParameters);
    String inUnitType = _testCommandHelper.checkParameter('unitType', inParameters);
    Booking booking = null;
    try {
      CarrierVisit carrierVisit = CarrierVisit.findOrCreateVesselVisit(ContextHelper.getThreadFacility(), inCvId)
      RoutingPoint rp_pod = RoutingPoint.findRoutingPoint(inPOD);
      RoutingPoint rp_pol = RoutingPoint.findRoutingPoint(inPOL);
      ScopedBizUnit line = LineOperator.findOrCreateLineOperator(inLineOperator);
      booking = Booking.findOrCreateBooking(inBookingNumber, line, carrierVisit, FreightKindEnum.FCL,
              rp_pol, rp_pod, null);
      if (booking != null) {
        EquipmentOrderItem equipmentOrderItem = EquipmentOrderItem.
                findOrCreateOrderItem(booking, inQuantity.toLong(), EquipType.findOrCreateEquipType(inUnitType));
        if (equipmentOrderItem != null) {
          returnString = 'Booking created successfully with given booking items'
        } else {
          returnString = 'Booking created successfully but booking items not added'
        };
      } else {
        returnString = 'Booking not created'
      };
    } catch (Exception ex) {
      returnString = 'Booking not created :' + ex.getStackTrace() + ':' + ex.getMessage()
    }
    LOGGER.debug(returnString)
    builder {
      actual_result returnString;
    }
    return builder;
  }



  protected UnitManager getUnitMngr() {
    return (UnitManager) Roastery.getBean(UnitManager.BEAN_ID);
  }

  protected UnitReroutePoster getRerouteMngr() {
    return (UnitReroutePoster) Roastery.getBean(UnitReroutePoster.BEAN_ID); ;
  }
}
