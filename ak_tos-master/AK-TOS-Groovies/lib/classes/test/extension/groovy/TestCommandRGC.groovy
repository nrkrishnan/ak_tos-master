/*
 * Copyright (c) 2015 Navis LLC. All Rights Reserved.
 *
 */



package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.RoutingPoint
import com.navis.framework.business.Roastery
import com.navis.framework.business.atoms.LifeCycleStateEnum
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.framework.util.message.MessageCollector
import com.navis.framework.util.message.MessageCollectorFactory
import com.navis.rail.RailBizMetafield
import com.navis.rail.RailEntity
import com.navis.rail.RailField
import com.navis.rail.business.api.RailManager
import com.navis.rail.business.atoms.InspectionStatusEnum
import com.navis.rail.business.atoms.RailcarPinModelEnum
import com.navis.rail.business.entity.*
import org.apache.log4j.Logger

/**
 * Created by rajansh on 02-01-2015.
 */
class TestCommandRGC {

    private DatabaseHelper _teamsDbHelper;

    //Logger for TestCommand
    public Logger LOGGER = Logger.getLogger(test.extension.groovy.TestCommandRGC.class);
    protected TestCommandHelper _testCommandHelper = new TestCommandHelper();
    /** holds the result returned by each method */
    def String returnString = null;
    /** json builder, frames the output in json format */
    def builder = new groovy.json.JsonBuilder();


  /**
     * Creates a new rail car type
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  CreateRailCarType<br>
     * railCarTypeId = Name of the rail car type to be created<br>
     * @return JSON , <code>Rail car type created successfully</code><br>
     *                <code>Rail car type exists already</code><br>
     *                <code>Rail car type creation failed</code><br>
     * @Example
     * Table invoked in SPARCS : rail_car_types<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="CreateRailCarType" /&gt; <br>
     * &lt;parameter id="railCarTypeId" value="RCAR01" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
   /* public String CreateRailCarType(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="CreateRailCarType" />
                                            <parameter id="railCarTypeId" value="<rail car type id>
                                            <parameter id="maxTierCount" value="<rail car type id>
                                            <parameter id="max20Count" value="<rail car type id>
                                            <parameter id="flatCarType" value="<flat car type id>'''
        String inRcarTypeId = _testCommandHelper.checkParameter('railCarTypeId', inParameters);
        String inMaxTierCount = _testCommandHelper.checkParameter('maxTierCount', inParameters);
        String inMax20Count = _testCommandHelper.checkParameter('max20Count', inParameters);
        String inFlatCarType = _testCommandHelper.checkParameter('flatCarType', inParameters);
        try {
            RailcarType rcarType = RailcarType.findRailcarType(inRcarTypeId);

            if (rcarType == null) {
                rcarType = RailcarType.createRailcarType(inRcarTypeId);
                RailcarDetails details = new RailcarDetails();
                def flatCarEnum = FlatCarTypeEnum.CONV; // by default setting it as 'Convertible'
                details.setRcdMax20sPerPlatform(inMax20Count.toLong());
                details.setRcdMaxTiersPerPlatform(inMaxTierCount.toLong());
                if(inFlatCarType.equalsIgnoreCase('Convertible')) {
                  flatCarEnum = FlatCarTypeEnum.CONV
                } else if(inFlatCarType.equalsIgnoreCase('Trailer on flat car')) {
                  flatCarEnum = FlatCarTypeEnum.TOFC
                } else if(inFlatCarType.equalsIgnoreCase('Container on flat car')) {
                  flatCarEnum = FlatCarTypeEnum.COFC
                }
                details.setRcdFlatCarType(flatCarEnum);
                rcarType.setRcartypRailcarDetails(details)
                returnString = 'Rail car type created successfully'
            } else {
                returnString = 'Rail car type exists already'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car type : ' + inRcarTypeId + ' creation failed:' + ex.printStackTrace();
        }

        LOGGER.debug('CreateRailCarType:' + returnString)
      builder {
        actual_result returnString;
      }
      return builder;
    }
*/

    /**
     * Creates a new rail car type
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  CreateRailCar<br>
     * railCarId = Name of the rail car type to be created<br>
     * railCarTypeId = Name of the rail car template type to which the rail car needs to be associated<br>
     * trainCvId=train carrier visit<br>
     * @return JSON , <code>Rail car type created successfully</code><br>
     *                <code>Rail car type exists already</code><br>
     *                <code>Rail car type creation failed</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="CreateRailCar" /&gt; <br>
     * &lt;parameter id="railCarId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="railCarTypeId" value="2X40" /&gt; <br>
     * &lt;parameter id="trainCvId" value="3CAE1" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String CreateRailCar(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="CreateRailCar" />
                                            <parameter id="railCarId" value="<rail car type id>
                                            <parameter id="railCarTypeId" value="<rail car type id>
                                            <parameter id="trainCvId" value="<rail road owner>" />
                                            <parameter id="inspectedStatus" value="<OK>" /> // optional - by default - NOT_INSPECTED'''
        String inRcarId = _testCommandHelper.checkParameter('railCarId', inParameters);
        String inRcarTypeId = _testCommandHelper.checkParameter('railCarTypeId', inParameters);
        String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
        String inInspectedStatus = ''

        if (inParameters.containsKey('inspectedStatus') && !inParameters.get('inspectedStatus').toString().isEmpty()) {
            inInspectedStatus = inParameters.get('inspectedStatus')
        }
        try {
            //creates or finds rail car type
            RailcarType rcarType = RailcarType.findRailcarType(inRcarTypeId);
            Railcar rcar;
            Railroad railroad;
            if (rcarType != null) {
                //gets train visit details
                CarrierVisit carrierVisit = getTrainVisit(inTrainCvId);
                TrainVisitDetails trainVisitDetails;
                if (carrierVisit != null) {
                    trainVisitDetails = TrainVisitDetails.resolveTvdFromCv(carrierVisit);
                }
                if (trainVisitDetails != null) {
                    railroad = trainVisitDetails.getRvdtlsRR();
                }
                if (railroad != null) {
                    rcar = Railcar.createRailcar(inRcarId, rcarType, railroad);
                    if (rcar != null) {
                        rcar.setFieldValue(RailField.RCAR_NOTES,inRcarId)
                        rcar.setLifeCycleState(LifeCycleStateEnum.ACTIVE);
                        RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
                        RailcarVisit rcv = rm.findOrCreateActiveRailcarVisit(trainVisitDetails, null, rcar, null, null, null, null);
                        rcv.setRcarvInspectionStatus(InspectionStatusEnum.NOT_INSPECTED)

                        if(!inInspectedStatus.isEmpty() && inInspectedStatus.equalsIgnoreCase('OK')) {
                            rcv.setRcarvInspectionStatus(InspectionStatusEnum.OK)
                        }
                        returnString = 'Rail car created successfully'
                    } else {
                        returnString = 'Rail car creation failed'
                    }
                } else {
                    returnString = 'Rail road operator not found, Please choose the available rail road operators'
                }
            } else {
                returnString = 'Rail car type not found, Please choose the available rail car type'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car : ' + inRcarId + ' creation failed:' + ex.printStackTrace();
        }
        LOGGER.debug('CreateRailCar:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Adds empty rail car to train visit
     * <a href="http://jira.navis.com/browse/ARGO-64235"> ARGO-64235 </a>
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  AddEmptyRailCarToTrainVisit<br>
     * railCarId = Name of the rail car type to be created<br>
     * railCarTypeId = Name of the rail car template type to which the rail car needs to be associated<br>
     * trainCvId=train visit Name<br>
     * @return JSON , <code>Rail car type created successfully</code><br>
     *                <code>Rail car type exists already</code><br>
     *                <code>Rail car type creation failed</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="AddEmptyRailCarToTrainVisit" /&gt; <br>
     * &lt;parameter id="railCarId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="railCarTypeId" value="2X40" /&gt; <br>
     * &lt;parameter id="trainCvId" value="3CBE1" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String AddEmptyRailCarToTrainVisit(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="AddEmptyRailCarToTrainVisit" />
                                            <parameter id="railCarId" value="<rail car id>
                                            <parameter id="trainCvId" value="<name of the vessel visit>" />'''
        String inRcarId = _testCommandHelper.checkParameter('railCarId', inParameters);
        String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
        try {
            CarrierVisit carrierVisit = getTrainVisit(inTrainCvId)
            Railcar railcar = Railcar.findRailcar(inRcarId)
            if (railcar != null) {
                Serializable rcarTypeGkey = railcar.getRcarRailcarTemplateType().getRcartypGkey();

                TrainVisitDetails trainVisitDetails;
                if (carrierVisit != null) {
                    trainVisitDetails = TrainVisitDetails.resolveTvdFromCv(carrierVisit);
                }
                if (trainVisitDetails != null) {
                    RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
                    RailcarVisit rcv = rm.findOrCreateActiveRailcarVisit(trainVisitDetails, null, railcar, null, null, null, null);
                    returnString = 'Added empty rail car : ' + inRcarId + ' to train visit : ' + inTrainCvId + ' successfully'
                } else {
                    returnString = 'Train visit details not found for : ' + inTrainCvId + ' adding empty rail car failed'
                }
            } else {
                  returnString = 'Please create rail car using CreateRailCar() before invoking this API'
              }
        } catch (Exception ex) {
            returnString = 'Adding empty rail car : ' + inRcarId + ' to train visit failed :' + ex.printStackTrace();
        }

        LOGGER.debug('AddEmptyRailCarToTrainVisit:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Updates rail car pin geometry for the platform
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  UpdateRailCarPinValue<br>
     * railCarId = Name of the rail car type to be created<br>
     * platformSeqNumber = Name of the platform <br>
     * pinGeometry = Any of these values <NOPINS,ADJUSTABLE,FIXED> <br>
     * @return JSON , <code>Updated rail car pin successfully</code><br>
     *                <code>Rail car not found</code><br>
     *                <code>Rail car pin updation failed:</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="UpdateRailCarPinValue" /&gt; <br>
     * &lt;parameter id="railCarId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="platformSeqNumber" value="1" /&gt; <br>
     * &lt;parameter id="pinGeometry" value="ADJUSTABLE" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String UpdateRailCarPinValue(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="UpdateRailCarPinValue" />
                                            <parameter id="railCarId" value="<rail car type id>
                                            <parameter id="platformSeqNumber" value="<platform Sequence Number>
                                            <parameter id="pinGeometry" value="<NOPINS,ADJUSTABLE,FIXED>'''

        String inRcarId = _testCommandHelper.checkParameter('railCarId', inParameters);
        String inPlatformSequenceNumber = _testCommandHelper.checkParameter('platformSeqNumber', inParameters);
        String inPinGeometry = _testCommandHelper.checkParameter('pinGeometry', inParameters);
        inPinGeometry = inPinGeometry.toUpperCase();
        //converting string to long
        Long pltfrmSeqNumber = Long.parseLong(inPlatformSequenceNumber);
        try {
            Railcar rcar = Railcar.findRailcar(inRcarId);
            if (rcar != null) {
                Set rcarPlatforms = rcar.getRcarPlatforms()
                //iterate all the platforms available for the rail car
                rcarPlatforms.each {
                    RailcarPlatform railcarPlatform = (RailcarPlatform) it;
                    //verify the obtained platform is what the user requested
                    if (railcarPlatform.rcarplfRailcarPlatformDetails.plfdSequence == pltfrmSeqNumber) {
                        //update pin geometry for the requested platform
                        railcarPlatform.getRcarplfRailcarPlatformDetails().setPlfdPinGeometry(RailcarPinModelEnum."$inPinGeometry")
                    }
                }
                returnString = 'Updated rail car pin successfully'
            } else {
                returnString = 'Rail car not found'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car : ' + inRcarId + ' pin updation failed:' + ex.printStackTrace();
        }

        LOGGER.debug('UpdateRailCarPinValue:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Locates rail car platform by updating transfer point value
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  LocateRailCarPlatformByTransferPoint<br>
     * trainCvId = Name of the rail car type to be created<br>
     * inboundRailCar = Visit id of the inbound rail car <br>
     * tpValue = transfer point value to be updated <br>
     * track = Track Id to form the track position<br>
     * trackSlot = Track slot number to form the track position <br>
     * platformSeqNumber = Platform sequence number in the track <br>
     * locateRailcarOption = locates rail car by the option given here either through transfer point or by meter mark <br>
     *                       if this parameter is not given in the input, by default 'TransferPoint' option will be chosen
     * @return JSON , <code>Located rail car platform by TP successfully</code><br>
     *                <code>Train visit not found</code><br>
     *                <code>No inbound rail car visits found for the Train visit :</code><br>
     *                <code>Located rail car platform by Transfer Point successfully</code><br>
     *                <code>Located rail car platform by Meter Mark successfully</code><br>
     *                <code>Locating rail car platform failed: </code><br>
     *                <code>Locating rail car platform by meter mark failed, check logs for more info </code><br>
     *                <code>Locating rail car platform by transfer point failed, check logs for more info </code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="LocateRailCarPlatform" /&gt; <br>
     * &lt;parameter id="trainCvId" value="RCAR01" /&gt;<br>
     * &lt;parameter id="inboundRailCar" value="3CBERAIL" /&gt;<br>
     * &lt;parameter id="tpValue" value="1" /&gt;<br>
     * &lt;parameter id="platformSeqNumber" value="1" /&gt;<br>
     * &lt;parameter id="track" value="T" /&gt; //optional - required only when there is no track defined by default<br>
     * &lt;parameter id="trackSlot" value="11" /&gt; //optional - required only when there is no trackslot defined by default <br>
     * &lt;parameter id="locateRailcarOption" value="TransferPoint or MeterMark" /&gt; // optional - should be given if option is MeterMark<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String LocateRailCarPlatform(Map inParameters) {
        assert inParameters.size() >= 4, '''Must supply 4 parameters:
                                            <parameter id="command" value="LocateRailCarPlatform" />
                                            <parameter id="trainCvId" value="<rail car type id>
                                            <parameter id="inboundRailCar" value="<inbound railcar visit id>
                                            <parameter id="platformSeqNumber" value="<platform sequence number>
                                            <parameter id="tpValue" value="<transfer point value>
                                            <parameter id="locateRailcarOption" value="<By Transfer Point or by Meter Mark>
                                            <parameter id="track" value="<track id> //optional
                                            <parameter id="trackSlot" value="<track slot> //optional'''
        String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
        String inInboundRailCar = _testCommandHelper.checkParameter('inboundRailCar', inParameters);
        String inPlatformSeqNumber = _testCommandHelper.checkParameter('platformSeqNumber', inParameters);
        String inTpValue = _testCommandHelper.checkParameter('tpValue', inParameters);
        String inLocateRailcarOption = 'TransferPoint';
        def rcarTrack = '', rcarTrackSlot = '';

        if (inParameters.containsKey('locateRailcarOption') && !inParameters.get('locateRailcarOption').toString().isEmpty()) {
            inLocateRailcarOption = inParameters.get('locateRailcarOption')
        }

        if (inParameters.containsKey('track') && !inParameters.get('track').toString().isEmpty()) {
            rcarTrack = inParameters.get('track')
        }

        if (inParameters.containsKey('trackSlot') && !inParameters.get('trackSlot').toString().isEmpty()) {
            rcarTrackSlot = inParameters.get('trackSlot')
        }

        try {
            CarrierVisit trainVisit = getTrainVisit(inTrainCvId)
            if (trainVisit != null) {
                TrainVisitDetails trainVisitDetails = TrainVisitDetails.resolveTvdFromCv(trainVisit);
                if (trainVisitDetails != null) {
                    Set inboundRailcarVisits = trainVisitDetails.getRvdtlsInboundRailcarVisits()
                    RailcarVisit railcarVisit;
                    boolean isVisitFound = false;
                    if (inboundRailcarVisits != null) {
                        //iterate all the inbound visits for the rail car
                        inboundRailcarVisits.each {
                            railcarVisit = (RailcarVisit) it;
                            //verify the obtained inbound rail car is what the user requested
                            if (railcarVisit.rcarvId.equalsIgnoreCase(inInboundRailCar)) {
                                //get track values from DB - it is mostly pre populated in base data
                                if(rcarTrack.isEmpty() && rcarTrackSlot.isEmpty()) {
                                    rcarTrack = railcarVisit.rcarvTrack
                                    rcarTrackSlot = railcarVisit.rcarvTrackSlot
                                }
                                //validate rcar track and rcar trackslot is not empty
                                if (rcarTrack.isEmpty() || rcarTrackSlot.isEmpty()) {
                                    returnString = 'Track or Track slot is empty, location rail car platorm failed. Please provide track or track slot value in input'
                                } else {
                                    isVisitFound = true;
                                    //update tp value for the requested inbound rail car either through transfer point or by meter mark
                                    FieldChanges fieldChanges = new FieldChanges();
                                    fieldChanges.setFieldChange(RailBizMetafield.RCARV_TRACK_SLOT, rcarTrackSlot)
                                    fieldChanges.setFieldChange(RailBizMetafield.RCARV_TRACK, rcarTrack)
                                    fieldChanges.setFieldChange(RailBizMetafield.RCARV_PLATFORM_ID, Integer.valueOf(inPlatformSeqNumber));
                                    if (inLocateRailcarOption.equalsIgnoreCase('TransferPoint')) {
                                        fieldChanges.setFieldChange(RailBizMetafield.RCARV_TRANSFER_POINT, inTpValue)
                                        if(locateByTPorMeterMark(railcarVisit.rcarvGkey, fieldChanges)) {
                                            returnString = 'Located rail car platform by Transfer Point successfully'
                                        } else returnString = 'Locating rail car platform by transfer point failed, check logs for more info'
                                    } else if (inLocateRailcarOption.equalsIgnoreCase('MeterMark')) {
                                        fieldChanges.setFieldChange(RailBizMetafield.RCARV_METER_MARK, Integer.valueOf(inTpValue))
                                        if(locateByTPorMeterMark(railcarVisit.rcarvGkey, fieldChanges)) {
                                            returnString = 'Located rail car platform by Meter Mark successfully'
                                        } else returnString = 'Locating rail car platform by meter mark failed, check logs for more info'
                                    }
                                }
                            }
                        }
                        if (!isVisitFound) {
                            returnString = 'No inbound rail car visits found for the Train visit : ' + inTrainCvId
                        }
                    }
                } else {
                    returnString = 'Train Visit Details not found for the train visit : ' + inTrainCvId
                }
            } else {
                returnString = 'Train visit not found'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Locating rail car platform : ' + inInboundRailCar + ' failed:' + ex.printStackTrace();
        }
        LOGGER.debug('LocateRailCarPlatform:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Locates Transfer point by TP or Meter Mark using a business class method
     * @param rcarGkey
     * @param fieldChanges
     */
    private boolean locateByTPorMeterMark(Long rcarGkey, FieldChanges fieldChanges ) {
        try {
            Map railCarAttrbs = new HashMap();
            railCarAttrbs.put("CHANGED_RAIL_CAR_ATTRIBUTES", fieldChanges);
            RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
            rm.processLocateByTpOrMeterMark(rcarGkey, railCarAttrbs);
            return true;
        } catch(Exception ex) {
            LOGGER.error('LocateRailCarPlatformByTransferPoint: ' + ex.getCause())
            return false;
        }
    }

    /**
     * Purges rail car
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  PurgeRailCar<br>
     * railCarId = Name of the rail car type to be created<br>
     * @return JSON , <code>Rail car purged</code><br>
     *                <code>Rail car not found</code><br>
     *                <code>Rail car deletion failed:</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="PurgeRailCar" /&gt; <br>
     * &lt;parameter id="railCarId" value="RCAR01" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String PurgeRailCar(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="PurgeRailCar" />
                                            <parameter id="railCarId" value="<rail car type id>'''

        String inRcarIdList = _testCommandHelper.checkParameter('railCarId', inParameters);
        String[] rcarList = inRcarIdList.split(",");
        HibernateApi hibernateApi = HibernateApi.getInstance()
        def inRcarId;
        try {
            Railcar rcar
            rcarList.each {
                inRcarId = it;
                rcar = Railcar.findRailcar(inRcarId);
                if (rcar != null) {
                    /**
                     * Order to delete rail car after purging the associated classes
                     * delete from rail_car_platforms where railcar_gkey = 1
                     * delete from rail_car_visits where railcar_state = 1
                     * delete from rail_car_states where railcar = 1
                     * delete from rail_cars where id = '3CBE8001'
                     */
                    DomainQuery domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_PLATFORM).addDqPredicate(PredicateFactory.disjunction()
                            .add(PredicateFactory.eq(RailField.RCARPLF_RAILCAR, rcar.rcarGkey)));
                    hibernateApi.deleteByDomainQuery(domainQuery);

                    domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                            .add(PredicateFactory.eq(RailField.RCARV_RAILCAR, rcar.rcarGkey)));
                    hibernateApi.deleteByDomainQuery(domainQuery);

                    domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_STATE).addDqPredicate(PredicateFactory.disjunction()
                            .add(PredicateFactory.eq(RailField.RCARSTT_RAILCAR, rcar.rcarGkey)));
                    hibernateApi.deleteByDomainQuery(domainQuery);

                    rcar.setLifeCycleState(LifeCycleStateEnum.OBSOLETE)
                    hibernateApi.delete(rcar,true);
                    returnString = 'Rail car purged'
                } else {
                    returnString = 'Rail car not found'
                }
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car : ' + inRcarId + ' deletion failed:' + ex;
        }
        LOGGER.debug('PurgeRailCar:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Purges rail car
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  PurgeRailCarVisit<br>
     * railCarVisitId = Name of the rail car visit to be deleted<br>
     * @return JSON , <code>Rail car visit purged</code><br>
     *                <code>Rail car visit not found</code><br>
     *                <code>Rail car visit deletion failed:</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="PurgeRailCarVisit" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String PurgeRailCarVisit(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="PurgeRailCarVisit" />
                                            <parameter id="railCarVisitId" value="<rail car visit id>'''

        String inRcarvisitIdList = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String[] rcarvisitList = inRcarvisitIdList.split(",");
        HibernateApi hibernateApi = HibernateApi.getInstance()
        def inRcarvisitId;
        try {
            rcarvisitList.each {
                inRcarvisitId = it;
                    /**
                     * Order to delete rail car after purging the associated classes
                     * delete from rail_car_platforms where railcar_gkey = 1
                     * delete from rail_car_visits where railcar_state = 1
                     * delete from rail_car_states where railcar = 1
                     * delete from rail_cars where id = '3CBE8001'
                     */
                    DomainQuery domainQuery ;
                    List list;

                    domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                            .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarvisitId)));
                    list = hibernateApi.findEntitiesByDomainQuery(domainQuery);
                    if(list.size() > 0) {
                    RailcarVisit railcarVisit;
                    list.each {
                        railcarVisit = (RailcarVisit) it;
                        hibernateApi.delete(railcarVisit,true);
                    }
                    returnString = 'Rail car visit purged'
                } else {
                    returnString = 'Rail car visit not found'
                }
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car visit : ' + inRcarvisitId + ' deletion failed:' + ex;
        }
        LOGGER.debug('PurgeRailCarVisit:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Resets spotting status for the given rail car visit
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  ResetSpottingStatus<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * @return JSON , <code>Spotting status reset successfull</code><br>
     *                <code>Spotting status reset failed : Rail Car Visit not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ResetSpottingStatus" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String ResetSpottingStatus(Map inParameters) {
        assert inParameters.size() >= 2, '''Must supply 2 parameters:
                                            <parameter id="command" value="ResetSpottingStatus" />
                                            <parameter id="railCarVisitId" value="<rail car type id>'''

        String inRcarVisitIds = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String[] railCarVisitList = inRcarVisitIds.split(',');
        try {
            railCarVisitList.each {
                DomainQuery domainQuery ;
                domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                        .add(PredicateFactory.eq(RailField.RCARV_ID, it)));
                RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
                if (railcarVisit != null) {
                    railcarVisit.resetSpottingStatus();
                    returnString = 'Spotting status reset successfull'
                } else {
                    returnString = 'Spotting status reset failed : Rail Car Visit not found'
                }
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car visits : ' + inRcarVisitIds + ' spotting status failed:' + ex;
        }
        LOGGER.debug('ResetSpottingStatus:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Performs rail car inspection and sets the new value given
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  PerformRailCarInspection<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * inspectionValue = value to be inspected OK,BAD,NOT_INSPECTED<br>
     * @return JSON , <code>Rail car inspected</code><br>
     *                <code>Rail car inspection failed : Rail Car Visit not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="PerformRailCarInspection" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="inspectionValue" value="OK" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String PerformRailCarInspection(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="PerformRailCarInspection" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="inspectionValue" value="OK,BAD,NOT_INSPECTED'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inInspectionValue = _testCommandHelper.checkParameter('inspectionValue', inParameters);
        try {
                DomainQuery domainQuery ;
                domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                        .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
                RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
                if (railcarVisit != null) {
                    railcarVisit.setRcarvInspectionStatus(InspectionStatusEnum."$inInspectionValue")
                    returnString = 'Rail car inspected'
                } else {
                    returnString = 'Rail car inspection failed : Rail Car Visit not found'
                }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail car visit : ' + inRcarVisitId + ' inspection failed:' + ex;
        }
        LOGGER.debug('PerformRailCarInspection:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Assigns rail car track for the given rail car visit
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  AssignRailTrack<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * track = Track Id to form the track position<br>
     * trackSlot = Track slot number to form the track position <br>
     * @return JSON , <code>Rail track assigned successfully</code><br>
     *                <code>Assigning rail track failed : Rail Car Visit not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="AssignRailTrack" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="track" value="T" /&gt; //optional - required only when there is no track defined by default<br>
     * &lt;parameter id="trackSlot" value="11" /&gt; //optional - required only when there is no trackslot defined by default <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String AssignRailTrack(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="AssignRailTrack" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="track" value="<track id>'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inRcarTrack = _testCommandHelper.checkParameter('track', inParameters);
        try {
                DomainQuery domainQuery ;
                domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                        .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
                RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
                if (railcarVisit != null) {
                    RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
                    if(rm != null) {
                        Serializable[] rcarvGkeys = (Serializable[]) railcarVisit.rcarvGkey.toString()
                        MessageCollector messageCollector = MessageCollectorFactory.createMessageCollector();
                        rm.assignTrack(ContextHelper.getThreadUserContext(),messageCollector,rcarvGkeys,inRcarTrack)
                    }
                    returnString = 'Rail track assigned successfully'
                } else {
                    returnString = 'Assigning rail track failed : Rail Car Visit not found'
                }
            }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Rail track : ' + inRcarVisitId + ' assigning failed:' + ex;
        }
        LOGGER.debug('AssignRailTrack:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Removes given rail car from train visit
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  RemoveRailCarFromTrainVisit<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * trainCvId = train visit id<br>
     * @return JSON , <code>Removed rail car from train visit successfully</code><br>
     *                <code>Removing rail car from train visit failed, Rail Car Visit :  not found</code><br>
     *                <code>Removing rail car from train visit failed, Train Visit : not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="RemoveRailCarFromTrainVisit" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="trainCvId" value="3CBE" /&gt;<br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String RemoveRailCarFromTrainVisit(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="RemoveRailCarFromTrainVisit" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="trainCvId" value="<trainCvId>'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inTrainCvId = _testCommandHelper.checkParameter('trainCvId', inParameters);
        try {
            DomainQuery domainQuery ;
            CarrierVisit trainVisit = getTrainVisit(inTrainCvId)
            if(trainVisit != null) {
                TrainVisitDetails trainVisitDetails = TrainVisitDetails.resolveTvdFromCv(trainVisit);
                if(trainVisitDetails != null) {
                    domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                            .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
                    RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
                    if (railcarVisit != null) {
                        RailManager rm = (RailManager) Roastery.getBean(RailManager.BEAN_ID);
                        if (rm != null) {
                            //ARGO-73900
                            Serializable[] rcarvGkeys = (Serializable[]) railcarVisit.rcarvGkey
                            rm.removeRailcarsFromTrain(trainVisitDetails.getCvdGkey(), rcarvGkeys)
                        }
                        returnString = 'Removed rail car from train visit successfully'
                    } else {
                        returnString = 'Removing rail car from train visit failed, Rail Car Visit : ' + inRcarVisitId + ' not found'
                    }
                } else returnString = 'Removing rail car from train visit failed, Train Visit Deatils : ' + inTrainCvId + 'not found'
            } else returnString = 'Removing rail car from train visit failed, Train Visit : ' + inTrainCvId + 'not found'
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Removing rail car : ' + inRcarVisitId + ' from train visit :  ' + inTrainCvId + ' failed:' + ex;
        }
        LOGGER.debug('RemoveRailCarFromTrainVisit:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Changes rail car destination
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  ChangeRailCarDestination<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * destination = New destination value for the rail car visit <br>
     * @return JSON , <code>Changed rail car destination successfully</code><br>
     *                <code>Changing rail car destination failed : ' + inRcarVisitId + ' not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ChangeRailCarDestination" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="destination" value="T" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String ChangeRailCarDestination(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="ChangeRailCarDestination" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="destination" value="<new destination value>'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inDestination = _testCommandHelper.checkParameter('destination', inParameters);
        try {
            DomainQuery domainQuery ;
                domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                        .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
                RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
                if (railcarVisit != null) {
                    railcarVisit.setFieldValue(RailField.RCARV_DESTINATION,inDestination)
                    returnString = 'Changed rail car destination successfully'
                } else {
                    returnString = 'Changing rail car destination failed : ' + inRcarVisitId + ' not found'
                }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Changing rail car : ' + inRcarVisitId + ' destination failed:' + ex;
        }
        LOGGER.debug('ChangeRailCarDestination:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Changes rail car hub
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  ChangeRailCarHub<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * routingPoint = New hub value for the rail car visit <br>
     * @return JSON , <code>Changed rail car hub successfully</code><br>
     *                <code>Changing rail car hub failed : ' + inRcarVisitId + ' not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="ChangeRailCarHub" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="routingPoint" value="T" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String ChangeRailCarHub(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="ChangeRailCarHub" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="routingPoint" value="<new destination value>'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inRoutingPoint = _testCommandHelper.checkParameter('routingPoint', inParameters);
        try {
            DomainQuery domainQuery ;
            domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                    .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
            RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
            if (railcarVisit != null) {
                RoutingPoint routingPoint = RoutingPoint.findRoutingPoint(inRoutingPoint);
                railcarVisit.setFieldValue(RailField.RCARV_DISCHARGE_POINT,routingPoint);
                returnString = 'Changed rail car hub successfully'
            } else {
                returnString = 'Changing rail car hub failed : ' + inRcarVisitId + ' not found'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Changing rail car : ' + inRcarVisitId + ' hub failed:' + ex;
        }
        LOGGER.debug('ChangeRailCarHub:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Sets segment break at lower end of rail car
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  SetSegmentBreak<br>
     * railCarVisitId = Name of the rail car visit id<br>
     * segmentValue = New segment value for the rail car visit <br>
     * @return JSON , <code>Setting segment break successful</code><br>
     *                <code>Setting segment break failed : ' + inRcarVisitId + ' not found</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="SetSegmentBreak" /&gt; <br>
     * &lt;parameter id="railCarVisitId" value="RCAR01" /&gt; <br>
     * &lt;parameter id="segmentValue" value="Yes" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
    public String SetSegmentBreak(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="SetSegmentBreak" />
                                            <parameter id="railCarVisitId" value="<rail car type id>
                                            <parameter id="segmentValue" value="<Yes or No>'''

        String inRcarVisitId = _testCommandHelper.checkParameter('railCarVisitId', inParameters);
        String inSegmentValue = _testCommandHelper.checkParameter('segmentValue', inParameters);
        try {
            DomainQuery domainQuery ;
            domainQuery = QueryUtils.createDomainQuery(RailEntity.RAILCAR_VISIT).addDqPredicate(PredicateFactory.disjunction()
                    .add(PredicateFactory.eq(RailField.RCARV_ID, inRcarVisitId)));
            RailcarVisit railcarVisit = (RailcarVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(domainQuery);
            if (railcarVisit != null) {
                if(inSegmentValue.equalsIgnoreCase('Yes'))
                    railcarVisit.rcarvDisconnectedAtLow = Boolean.TRUE
                else if(inSegmentValue.equalsIgnoreCase('No'))
                    railcarVisit.rcarvDisconnectedAtLow = Boolean.FALSE
                returnString = 'Setting segment break successful'
            } else {
                returnString = 'Setting segment break failed : ' + inRcarVisitId + ' not found'
            }
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Setting segment break failed:' + ex;
        }
        LOGGER.debug('SetSegmentBreak:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }

    /**
     * Sets cone status for a double stack train
     *
     * @param inParameters The map containing the method name to call along with the parameters
     * command =  SetConeStatus<br>
     * unitId = Name of the unit<br>
     * coneStatus = New cone status value for the unit facility visit <br>
     * @return JSON , <code>Updated rail cone status</code><br>
     *                <code>Setting rail cone status failed : UFV not found for the unit :</code><br>
     * @Example
     * Table invoked in SPARCS : rail_cars<br>
     * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
     * &lt;parameters&gt;<br>
     * &lt;parameter id="command" value="SetConeStatus" /&gt; <br>
     * &lt;parameter id="unitId" value="SHAU0809453" /&gt; <br>
     * &lt;parameter id="coneStatus" value="LOCKED_CONES" /&gt; <br>
     * &lt;/parameters&gt;<br>
     * &lt;/groovy&gt;<br>
     */
   /* public String SetConeStatus(Map inParameters) {
        assert inParameters.size() >= 3, '''Must supply 3 parameters:
                                            <parameter id="command" value="SetConeStatus" />
                                            <parameter id="unitId" value="<unit id>
                                            <parameter id="coneStatus" value="<cone status enum>'''

        String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
        String inConeStatus = _testCommandHelper.checkParameter('coneStatus', inParameters);
        try {
            Unit unitObj = ((Unit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(InventoryEntity.UNIT)
                    .addDqPredicate(PredicateFactory.eq(InventoryField.UNIT_ID, inUnitId))));
            if (unitObj != null) {
                UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
                SearchResults results = finder.findUfvByDigits(inUnitId, false, false);
                UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
                if(ufv != null) {
                    ufv.updateRailConeStatus(RailConeStatusEnum."$inConeStatus");
                    returnString = "Updated rail cone status"
                } else returnString = 'Setting rail cone status failed : UFV not found for the unit : ' + inUnitId
            } else returnString = 'Setting rail cone status failed : Unit not found : ' + inUnitId
        }
        catch (BizFailure inBizFailure) {
            returnString = inBizFailure.message;
        } catch (Exception ex) {
            returnString = 'Setting rail cone status failed:' + ex;
        }
        LOGGER.debug('SetConeStatus:' + returnString)
        builder {
          actual_result returnString;
        }
        return builder;
    }
*/
    /**
     * Gets Carrier Visit object from train carrier visit id
     * @return
     */
    private CarrierVisit getTrainVisit(String inTrainCvId) {
        //gets train visit details
        CarrierVisit carrierVisit = (CarrierVisit) HibernateApi.getInstance().getUniqueEntityByDomainQuery(
                QueryUtils.createDomainQuery(ArgoEntity.CARRIER_VISIT)
                        .addDqPredicate(PredicateFactory.eq(ArgoField.CV_ID, inTrainCvId))
                        .addDqPredicate(PredicateFactory.eq(ArgoField.CV_CARRIER_MODE, LocTypeEnum.TRAIN)));

        return carrierVisit;
    }

    public String execute(Map inParameters) {
        assert inParameters.size() > 0, '''Must supply at least 1 parameter:
                                       <parameter id="command" value="<API name>" />''';
        String methodName = _testCommandHelper.checkParameter('command', inParameters);

        this."$methodName"(inParameters);
    }

    private static final RCARV_TRANSFER_POINT = 'rcarvTransferPoint'
    private static final RCARV_METER_MARK = 'rcarvMeterMark'
}

