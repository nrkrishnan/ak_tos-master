package test.extension.groovy

import com.navis.argo.ArgoEntity
import com.navis.argo.ArgoField
import com.navis.argo.ContextHelper
import com.navis.argo.business.api.ArgoUtils
import com.navis.argo.business.api.EcEventTypeConsts
import com.navis.argo.business.atoms.*
import com.navis.argo.business.model.LocPosition
import com.navis.argo.business.model.Yard
import com.navis.argo.business.xps.model.*
import com.navis.control.ControlBizMetafield
import com.navis.control.EciBizMetafield
import com.navis.control.business.ControlTestUtils
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.DatabaseHelper
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.FieldChanges
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.framework.util.BizFailure
import com.navis.inventory.InventoryBizMetafield
import com.navis.inventory.MovesEntity
import com.navis.inventory.MovesField
import com.navis.inventory.business.api.SearchResults
import com.navis.inventory.business.api.UnitFinder
import com.navis.inventory.business.atoms.WiEcStateEnum
import com.navis.inventory.business.atoms.WqTypeEnum
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.moves.WorkQueue
import com.navis.inventory.business.units.InventoryTestUtils
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.inventory.business.units.UnitYardVisit
import com.navis.optimization.portal.queueing.QueueServerClass
import com.navis.optimization.portal.queueing.QueueTimeWindow
import com.navis.spatial.BinField
import com.navis.spatial.business.model.AbstractBin
import com.navis.spatial.business.model.BinContext
import com.navis.spatial.business.model.block.BlockRange
import com.navis.xpscache.EcEventBizMetafield
import com.navis.xpscache.business.atoms.EquipBasicLengthEnum
import com.navis.yard.YardBizMetafield
import com.navis.yard.business.YardTestUtils
import com.navis.yard.business.atoms.TZBlockAssociationEnum
import com.navis.yard.business.model.StackBlock
import com.navis.yard.business.model.TransferZoneAssociation
import com.navis.yard.business.model.YardBinModel
import com.navis.yard.business.model.YardSection
import org.apache.log4j.Logger
import org.hibernate.SQLQuery
import org.zkoss.zkex.util.Base64Coder

import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: rajansh
 * Date: 29/10/13
 * Time: 2:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestCommandCreate {

  private HibernateApi _hibernateApi;
  /** holds the result returned by each method */
  def String returnString = null;
  /** json builder, frames the output in json format */
  def builder = new groovy.json.JsonBuilder();
  private DatabaseHelper _teamsDbHelper;
  /** handler to invoke the fields in helper class */
  test.extension.groovy.TestCommandHelper _testCommandHelper = new test.extension.groovy.TestCommandHelper();

  //Logger for TestCommand
  public Logger LOGGER = Logger.getLogger(TestCommandCreate.class);

  /**
   * Creates a Stack Block with the given block name and the row,column,tier values.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreateStackBlock<br>
   * blockName=Name of the block<br>
   * blockType=Type of the Block (ASC)<br>
   * firstRow=FirstRow in the stack<br>
   * lastRow=LastRow in the stack<br>
   * firstCol=FirstColumn in the stack<br>
   * lastCol=LastColumn in the stack<br>
   * firstTier=FirstTier in the stack<br>
   * lastTier=LastTier in the stack<br>
   * doorDir=CompassDirectionEnum values like UNKNOWN,EAST,WEST,NORTH,SOUTH
   * @return JSON , <code>Stack block created</code>-if block created successfully<br>
   *                <code>Block not created</code>-block creation failed
   * @Example
   * Table invoked in SPARCS : spatial_bins<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateStackBlock" /&gt;<br>
   * &lt;parameter id="blockName" value="zns01" /&gt;<br>
   * &lt;parameter id="blockType" value="ASC" /&gt;<br>
   * &lt;parameter id="firstRow" value="1" /&gt;<br>
   * &lt;parameter id="lastRow" value="20" /&gt;<br>
   * &lt;parameter id="firstCol" value="1" /&gt;<br>
   * &lt;parameter id="lastCol" value="8" /&gt;<br>
   * &lt;parameter id="firstTier" value="1" /&gt;<br>
   * &lt;parameter id="lastTier" value="5" /&gt;<br>
   * &lt;parameter id="doorDir" value="SOUTH"&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   *
   */
  public String CreateStackBlock(Map inParameters) {
    assert inParameters.size() == 10, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 10 parameters:
                                        <parameter id="command" value="CreateStackBlock" />
                                        <parameter id="blockName" value="<Block Name>" />  //yuflkj
                                        <parameter id="blockType" value="<Block Type> = ASC, STRADDLE" />
                                        <parameter id="firstRow" value="<First Row> = 0,1?" />
                                        <parameter id="lastRow" value="<Last Row > = 0,1?" />
                                        <parameter id="firstCol" value="<First Col> = 0,1?" />
                                        <parameter id="lastCol" value="<Last Col> = 0,1?" />
                                        <parameter id="firstTier" value="<First Tier> = 0,1?" />
                                        <parameter id="lastTier" value="<Last Tier> = 0,1?" />
                                        <parameter id="doorDir" value="CompassDirectionEnum values like UNKNOWN,EAST,WEST,NORTH,SOUTH" />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inBlockType = _testCommandHelper.checkParameter('blockType', inParameters);
    String inFirstRow = _testCommandHelper.checkParameter('firstRow', inParameters);
    String inLastRow = _testCommandHelper.checkParameter('lastRow', inParameters);
    String inFirstCol = _testCommandHelper.checkParameter('firstCol', inParameters);
    String inLastCol = _testCommandHelper.checkParameter('lastCol', inParameters);
    String inFirstTier = _testCommandHelper.checkParameter('firstTier', inParameters);
    String inLastTier = _testCommandHelper.checkParameter('lastTier', inParameters);
    String indoorDir = _testCommandHelper.checkParameter('doorDir', inParameters);
    StackBlock block = null;
    try {
      switch (inBlockType) {
        case 'ASC':
          FieldChanges blockChanges = YardTestUtils.getDefaultAscBlockFieldChanges();
          blockChanges.setFieldChange(BinField.ABN_Z_INDEX_MIN, inFirstTier);
          blockChanges.setFieldChange(BinField.ABN_Z_INDEX_MAX, inLastTier);
          block = YardTestUtils.findOrCreateStackBlock(inBlockName, blockChanges);
          YardTestUtils.createOrUpdateSectionsAndStacks(block, inFirstRow.toInteger(),
                  inLastRow.toInteger(), inFirstCol.toInteger(), inLastCol.toInteger(),
                  YardTestUtils.getDefaultAscSectionFieldChanges(), true);
          block.stkblkDirection = CompassDirectionEnum."$indoorDir"
          returnString = 'Stackblock created';
          break;
      }
    } catch (Exception ex) {
      returnString = 'Stackblock not created ' + ex;
    }
    assert block != null, returnString = 'Block ' + inBlockName + ' not created';
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a Transfer Zone and links it to a stack block.<br>
   * It checks whether the given block is available, if available it will link <br>
   * the transfer zone to the stack block,else it will create a new block with the <br>
   * given name and then links.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreateTransferZone<br>
   * transferZoneName=Name of the transfer zone<br>
   * linkedStackBlock=Linked Stack Block<br>
   * firstRow=FirstRow in the block<br>
   * lastRow=LastRow in the block<br>
   * firstCol=FirstColumn in the block<br>
   * lastCol=LastColumn in the block<br>
   * firstTier=FirstTier in the block<br>
   * lastTier=LastTier in the block<br>
   * association=NONE,ROWLOW,ROWHIGH,COLLOW,COLHIGH- Refer TZBlockAssociationEnum.java for updated values<br>
   * doorDir=CompassDirectionEnum values like UNKNOWN,EAST,WEST,NORTH,SOUTH <br>
   * @return <code>Transfer zone created and associated</code> if transfer zone is created, else
   *         <br><code>Transfer zone not created </code><br>
   *         <code>Transfer zone not associated with the block </code> - Transfer zone created but not linked to stack block
   * @Example
   * Table invoked in SPARCS : spatial_bins<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateTransferZone" /&gt;<br>
   * &lt;parameter id="transferZoneName" value="ALS1" /&gt;<br>
   * &lt;parameter id="linkedStackBlock" value="ASC1" /&gt;<br>
   * &lt;parameter id="firstRow" value="1" /&gt;<br>
   * &lt;parameter id="lastRow" value="20" /&gt;<br>
   * &lt;parameter id="firstCol" value="1" /&gt;<br>
   * &lt;parameter id="lastCol" value="8" /&gt;<br>
   * &lt;parameter id="firstTier" value="1" /&gt;<br>
   * &lt;parameter id="lastTier" value="5" /&gt;<br>
   * &lt;parameter id="association" value="ROWHIGH  " /&gt;<br>
   * &lt;parameter id="doorDir" value="SOUTH"&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateTransferZone(Map inParameters) {
    assert inParameters.size() == 11, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 11 parameters:
                                        <parameter id="command" value="CreateTransferZone" />
                                        <parameter id="transferZoneName" value="<TZ Name>" />
                                        <parameter id="linkedStackBlock" value="<Linked Stack Block>" />
                                        <parameter id="firstRow" value="<First Row> = 0,1?" />
                                        <parameter id="lastRow" value="<Last Row > = 0,1?" />
                                        <parameter id="firstCol" value="<First Col> = 0,1?" />
                                        <parameter id="lastCol" value="<Last Col> = 0,1?" />
                                        <parameter id="firstTier" value="<First Tier> = 0,1?" />
                                        <parameter id="lastTier" value="<Last Tier> = 0,1?" />
                                        <parameter id="association" value="<Association Type> = TZBlockAssociationEnum values" />
                                        <parameter id="doorDir" value="CompassDirectionEnum values like UNKNOWN,EAST,WEST,NORTH,SOUTH" />'''

    String inTZName = _testCommandHelper.checkParameter('transferZoneName', inParameters);
    String inLinkedStackBlock = _testCommandHelper.checkParameter('linkedStackBlock', inParameters);
    String inFirstRow = _testCommandHelper.checkParameter('firstRow', inParameters);
    String inLastRow = _testCommandHelper.checkParameter('lastRow', inParameters);
    String inFirstCol = _testCommandHelper.checkParameter('firstCol', inParameters);
    String inLastCol = _testCommandHelper.checkParameter('lastCol', inParameters);
    String inFirstTier = _testCommandHelper.checkParameter('firstTier', inParameters);
    String inLastTier = _testCommandHelper.checkParameter('lastTier', inParameters);
    String inAssociation = _testCommandHelper.checkParameter('association', inParameters);
    String indoorDir = _testCommandHelper.checkParameter('doorDir', inParameters);
    StackBlock transferZone = null;

    try {
      //Find the linked stack block first
      Yard thisYard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
      StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inLinkedStackBlock);
      assert stackBlock != null, "Could not find stack block " + inLinkedStackBlock;

      FieldChanges blockChanges = YardTestUtils.getDefaultWheeledTZBlockFieldChanges();
      blockChanges.setFieldChange(BinField.ABN_Z_INDEX_MIN, inFirstTier);
      blockChanges.setFieldChange(BinField.ABN_Z_INDEX_MAX, inLastTier);
      transferZone = YardTestUtils.findOrCreateStackBlock(inTZName, blockChanges);
      transferZone.stkblkDirection = CompassDirectionEnum."$indoorDir"
      YardTestUtils.createOrUpdateSectionsAndStacks(transferZone, inFirstRow.toInteger(), inLastRow.toInteger(),
              inFirstCol.toInteger(), inLastCol.toInteger(), YardTestUtils.getDefaultWheeledTZSectionFieldChanges(), true);
      TransferZoneAssociation association = YardTestUtils.findOrCreateTransferZoneAssociation(stackBlock, transferZone, TZBlockAssociationEnum."$inAssociation");

      returnString = 'Transfer zone created and associated';
      assert transferZone != null, returnString = 'Transfer zone ' + inTZName + ' not created';
      assert association != null, returnString = 'Transfer zone ' + inTZName + ' not associated with ' + inLinkedStackBlock;
    } catch (Exception ex) {
      returnString = 'Transfer zone creation failed ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Creates a Transfer Zone work instruction corresponding to a given Work instruction - based on system provided problem solutions
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreateTransferWorkInstruction<br>
   * wiGkey=Work Instruction of actual operation to be performed(for eg,discharge/load which involves transfer points<br>
   * @return <code>Gkey of the newly created transfer zone work instruction</code> if transfer zone is created, else
   * @Example
   * Table invoked in SPARCS : inv_wi<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateTransferWorkInstruction" /&gt;<br>
   * &lt;parameter id="wiGkey" value="123" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateTransferWorkInstruction(Map inParameters) {
    assert inParameters.size() >= 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="CreateTransferWorkInstruction" />
                                        <parameter id="wiGkey" value="<Transfer zone Work Instruction gkey>" />
                                        <parameter id="cheId" value="<AGV/ASC name>" />'''


    String wiGKey = _testCommandHelper.checkParameter('wiGkey', inParameters);
    String cheId = _testCommandHelper.checkParameter('cheId', inParameters);
    WorkInstruction tzWorkInstruction = null;
    try {
      WorkInstruction workInstruction = WorkInstruction.hydrate(wiGKey);
      //find che
      Che cheObj = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheId))));
      QueueTimeWindow timeWindow = new QueueTimeWindow(new Date(ArgoUtils.timeNowMillis() + 10 * ArgoUtils.MILLIS_PER_MINUTE),
              new Date(ArgoUtils.timeNowMillis() + 20 * ArgoUtils.MILLIS_PER_MINUTE));

      tzWorkInstruction = ControlTestUtils.findOrCreateTransferZoneWorkInstruction(workInstruction, timeWindow, 0,
              QueueServerClass.getSynchronousTransferTypeRequest(false), cheObj);
      if (tzWorkInstruction != null) {
        returnString = 'Transfer work instruction created';
      } else {
        returnString = 'Transfer zone work instruction was not created';
      }
    } catch (BizFailure inBizFailure) {
      returnString = inBizFailure.message;
    } catch (Exception inEx) {
      if (inEx.toString().contains('TZWI_NO_SOLUTIONS_FOUND')) {
        returnString = 'Transfer Zone Work Instruction not created : <Reason:TZWI_NO_SOLUTIONS_FOUND>'
      } else {
        returnString = 'Transfer Zone Work Instruction not created :' + inEx
      }
    }
    finally {
      builder {
        actual_result returnString;
        if (tzWorkInstruction != null) {
          data('WiGkey': tzWorkInstruction.wiGkey)
        }
      }
      LOGGER.debug('CreateTransferWorkInstruction :' + returnString)
    }
    return builder;
  }

  /**
   * Creates a Container Handling Equipment Pool.<br>
   * It returns the  pool if already exists else creates.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreatePool<br>
   * poolName=Name of the pool to be created
   * @return JSON ,<code>Pool created</code>if success, else<br>
   *              <code>Pool not created</code>
   * @Example
   * Table invoked in SPARCS : xps_pool <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreatePool" /&gt;<br>
   * &lt;parameter id="poolName" value="AGVP1" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreatePool(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="CreatePool" />
					                    <parameter id="poolName" value="<Pool Name>" />'''

    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    ChePool chePool = null;
    try {
      chePool = ControlTestUtils.findOrCreateChePool(inPoolName);
    } catch (Exception ex) {
      returnString = 'Pool not created' + ex;
    }
    returnString = 'Pool created';
    assert chePool != null, returnString = 'Pool ' + inPoolName + ' not created';
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a Point Of Work with the given pool and pow name.<br>
   * It returns the pointofwork if already exists else creates.
   *
   * @Precondition
   * The given pool should already be available, to create pool please invoke CreatePool
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreatePow<br>
   * powName=Name of the pointOfWork to be created<br>
   * poolName=Name of the pool, pool should already be available else an error will be thrown
   * @return JSON ,<code>Pow created</code> if POW created successfully else<br>
   *              <code>Pow not created</code><br>
   *              <code>Pool not available</code> if the given pool name is not yet created
   * @Example
   * Table invoked in SPARCS : xps_pointofwork<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreatePow" /&gt;<br>
   * &lt;parameter id="powName" value="QC05" /&gt;<br>
   * &lt;parameter id="poolName" value="ASCP5" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreatePow(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="CreatePow" />
                                        <parameter id="powName" value="<Pow Name>" />
                                        <parameter id="poolName" value="<Pool Name>" />'''

    PointOfWork pointOfWork = null;
    returnString = null;

    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);

    try {
      //Find yard gkey
      Yard yard = ContextHelper.getThreadYard();
      final Long yrdGkey = yard.getYrdGkey();

      //Find pool
      DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName))
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_YARD, yrdGkey));
      List chePoolList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
      final ChePool chePool;
      if (chePoolList != null && !chePoolList.isEmpty()) {
        chePool = (ChePool) chePoolList.get(0);
        assert chePool != null, 'Pool ' + inPoolName + ' not available';
        pointOfWork = ControlTestUtils.findOrCreatePow(inPowName, chePool);
      }
      returnString = 'Pow created';
      assert pointOfWork != null, returnString = 'Pow ' + inPowName + ' not created';
    } catch (Exception ex) {
      returnString = 'Pow not created ' + ex;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a Container Handling Equipment(CHE) for ASC,AGV,QC according to the given CHE type.<br>
   * It also sets the che status as 'WORKING' <br>
   * It returns the CHE if already exists else creates.
   *
   * @Precondition
   * The given pool and pow should already be available, to create pool and pow<br>
   * please invoke CreatePool and CreatePow
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreateChe<br>
   * cheId=Name of the CHE to be created<br>
   * cheType=ASC,AGV,QC <br>
   * poolName=Name of the pool to which the CHE needs to be assigned<br>
   * powName=Name of the point of work to be updated for the che <br>
   * @return JSON , <code>CHE created</code><br>
   *               <code>CHE not created</code><br>
   *               <code>Pool/Pow not available</code> if the given pool/pow is not created
   * @Example
   * Table invoked in SPARCS - xps_che
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateChe" /&gt;<br>
   * &lt;parameter id="cheId" value="ASC1" /&gt;<br>
   * &lt;parameter id="cheType" value="ASC" /&gt;<br>
   * &lt;parameter id="poolName" value="ASCP1" /&gt;<br>
   * &lt;parameter id="powName" value="QC02" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateChe(Map inParameters) {
    assert inParameters.size() == 5, '''Must supply 5 parameters:
                                        <parameter id="command" value="CreateChe" />
                                        <parameter id="cheId" value="<Che Id>" />
                                        <parameter id="cheType" value="<Che Type> = ASC, AGV" />
                                        <parameter id="poolName" value="<Pool Name>" />
                                        <parameter id="powName" value="<Pow Name>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inCheType = _testCommandHelper.checkParameter('cheType', inParameters);
    String inPoolName = _testCommandHelper.checkParameter('poolName', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    ChePool chePool = null;
    PointOfWork pointOfWork = null;
    Che che = null;

    try {
      //Find yard gkey
      Yard yard = ContextHelper.getThreadYard();
      final Long yrdGkey = yard.getYrdGkey();

      //Find pool
      DomainQuery dqPool = QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, inPoolName))
              .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_YARD, yrdGkey));
      List chePoolList = HibernateApi.getInstance().findEntitiesByDomainQuery(dqPool);
      if (chePoolList != null && chePoolList.size() > 0) {
        chePool = (ChePool) chePoolList.get(0);
      }
      assert chePool != null, returnString = 'Pool ' + inPoolName + ' not available';
	  
	  //Find pow
      DomainQuery dqPow = QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName))
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_YARD, yrdGkey));

      List pointsOfWork = HibernateApi.getInstance().findEntitiesByDomainQuery(dqPow);
      if (pointsOfWork != null && pointsOfWork.size() > 0) {
        pointOfWork = (PointOfWork) pointsOfWork.get(0);
      }
      assert pointOfWork != null, returnString = 'POW ' + inPowName + ' not available';

      //create che
      che = ControlTestUtils.findOrCreateChe(inCheId, CheKindEnum."$inCheType", chePool, pointOfWork);
      assert che != null, returnString = 'CHE ' + inCheId + ' not created';
	  
	   if (CheKindEnum.QC == che.getCheKindEnum()){
        che.setCheIsOcrDataBeingAccepted(true);
      }

      //Update Che
      che.setCheKindEnum(CheKindEnum."$inCheType");
      che.setCheFullName(inCheId);
      che.updateChePointOfWork(pointOfWork);
      che.updateChePoolEntity(chePool);
      che.setCheStatusEnum(CheStatusEnum.WORKING); // added che status as working

      //Save
      HibernateApi.getInstance().save(che);
      HibernateApi.getInstance().flush();
      returnString = 'CHE created';
      assert che != null, returnString = 'CHE ' + inCheId + ' not created';
    } catch (Exception ex) {
      returnString = 'Che not created ' + ex;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a CHE zone.It takes name of the CHE,block name,row,column,tier values as input to create the zone<br>
   * Sets the selected and ordered row,column values with the given inputs.
   *
   * @Precondition
   * The given CHE and Block should already be available, to create please invoke CreateCHE<br>
   * and CreateStackBlock respectively.
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=SetCheZone<br>
   * cheId=Name of the che for which Che Zone needs to be created<br>
   * blockName=Name of the block<br>
   * startRow=FirstRow in the block<br>
   * endRow=LastRow in the block<br>
   * startCol=FirstColumn in the block<br>
   * endCol=LastColumn in the block<br>
   * @return JSON , <code>Che zone created</code><br><code> Che zone creation failed</code><br>
   *                <code>Stack block not present in yard</code> - The given stack block is not created in the yard<br>
   *                <code>Could not find che</code> - Given CHE is not available<br>
   * @Example
   * Table invoked in SPARCS : xps_chezone
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetCheZone" /&gt;<br>
   * &lt;parameter id="cheId" value="CH01" /&gt;<br>
   * &lt;parameter id="blockName" value="zns01" /&gt;<br>
   * &lt;parameter id="endRow" value="1" /&gt;<br>
   * &lt;parameter id="lastRow" value="20" /&gt;<br>
   * &lt;parameter id="startCol" value="1" /&gt;<br>
   * &lt;parameter id="endCol" value="8" /&gt;<br>
   * &lt;parameter id="startTier" value="1" /&gt;<br>
   * &lt;parameter id="endtier" value="8" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SetCheZone(Map inParameters) {
    assert inParameters.size() == 9, '''Must supply 9 parameters:
                                        <parameter id="command" value="SetCheZone" />
                                        <parameter id="cheId" value="<CHE Id>" />
                                        <parameter id="blockName" value="<Block Name>" />
                                        <parameter id="startRow" value="<Start Row>" />
                                        <parameter id="endRow" value="<End Row>" />
                                        <parameter id="startCol" value="<Start Col>" />
                                        <parameter id="endCol" value="<End Col>" />
                                        <parameter id="startTier" value="<First Tier>" />
                                        <parameter id="endTier" value="<Last Tier>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inStartRow = _testCommandHelper.checkParameter('startRow', inParameters);
    String inEndRow = _testCommandHelper.checkParameter('endRow', inParameters);
    String inStartCol = _testCommandHelper.checkParameter('startCol', inParameters);
    String inEndCol = _testCommandHelper.checkParameter('endCol', inParameters);
    String inStartTier = _testCommandHelper.checkParameter('startTier', inParameters);
    String inEndTier = _testCommandHelper.checkParameter('endTier', inParameters);

    try {
      //Find yard model
      Yard yard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(yard.getYrdBinModel(), YardBinModel.class);
      //Find the stack block
      StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inBlockName);
      assert stackBlock != null, returnString = 'Stack block ' + stackBlock + ' not present in yard';
      //Find the che
      Che che = Che.findCheByShortName(inCheId, yard);
      assert che != null, returnString = "Could not find che " + inCheId;
      //create che zone
      CheZone zone = ControlTestUtils.findOrCreateCheZone(che, new BlockRange(stackBlock, inStartRow.toInteger(),
              inEndRow.toInteger(), inStartCol.toInteger(), inEndCol.toInteger(), inStartTier.toInteger(), inEndTier.toInteger()));
      //sets che zone
      zone.chezoneSelFirstRow = inStartRow.toInteger();
      zone.chezoneSelLastRow = inEndRow.toInteger();
      zone.chezoneSelFirstColumn = inStartCol.toInteger();
      zone.chezoneSelLastColumn = inEndCol.toInteger();

      // sets the ORD* values on the CheZone (required for ASC scheduler)
      zone.setChezoneOrdFirstRow(inStartRow.toInteger());
      zone.setChezoneOrdLastRow(inEndRow.toInteger());
      zone.setChezoneOrdFirstColumn(inStartCol.toInteger());
      zone.setChezoneOrdLastColumn(inEndCol.toInteger());

      //save the che zone values
      HibernateApi.getInstance().save(zone);
      assert zone != null, 'Che zone creation failed';
      returnString = 'Chezone set';
    } catch (Exception ex) {
      returnString = 'Che zone not created ' + ex;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a work queue with the given work Queue type like Discharge,Load,etc..<br>
   * Returns the work queue if exists, else creates a new one.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=CreateWorkQueue<br>
   * queueName=Name of the Queue<br>
   * queueType=Type of the Queue - DISCH,LOAD,YARD,PROJECTION" />
   * @return <code>WorkQueue created</code>
   *         <code>WorkQueue creation failed</code>
   * @Example
   * Table invoked in SPARCS : inv_wq<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateWorkQueue" /&gt;<br>
   * &lt;parameter id="queueName" value="Queue9-dsch" /&gt;<br>
   * &lt;parameter id="queueType" value="DISCH" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateWorkQueue(Map inParameters) {
    assert inParameters.size() == 3, '''Must supply 3 parameters:
                                        <parameter id="command" value="CreateWorkQueue" />
                                        <parameter id="queueName" value="<Queue Name>" />
                                        <parameter id="queueType" value="<Queue Type> = WqTypeEnum values" />'''

    String inQueueName = _testCommandHelper.checkParameter('queueName', inParameters);
    String inQueueType = _testCommandHelper.checkParameter('queueType', inParameters);
    try {
      WorkQueue wq = InventoryTestUtils.findOrCreateWorkQueue(inQueueName, WqTypeEnum."$inQueueType");
      returnString = "WorkQueue created";
      assert wq != null, returnString = 'WorkQueue ' + inQueueName + ' creation failed';
    } catch (Exception ex) {
      returnString = 'WorkQueue not created ' + ex;
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a Work Shift for the given pow with the given name
   *
   * @Precondition
   * Given point of work should already be available, to create Pow invoke CreatePow
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=CreateWorkShift<br>
   * workShiftName=name of the work shift<br>
   * powName=name of the point of work<br>
   * duration=duration for the work shift in Hours<br>
   * @return <code>Work Shift created</code> if success else<br> <code>Work Shift not Created</code>
   * @Example
   * Table invoked in SPARCS : xps_craneshift <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="CreateWorkShift" /&gt;<br>
   * &lt;parameter id="powName" value="AGVPow" /&gt;<br>
   * &lt;parameter id="duration" value="8" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String CreateWorkShift(Map inParameters) {
    assert inParameters.size() == 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="CreateWorkShift" />
                                        <parameter id="workShiftName" value="<work shift name>" />
                                        <parameter id="powName" value="<pow name>" />
                                        <parameter id="duration" value="<duration>" />'''

    String inWorkShiftName = _testCommandHelper.checkParameter('workShiftName', inParameters);
    String inPowName = _testCommandHelper.checkParameter('powName', inParameters);
    String inDuration = _testCommandHelper.checkParameter('duration', inParameters);

    try {
      Long duration = inDuration.toLong()

      DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
              .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, inPowName));
      PointOfWork pointOfWork = (PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
      assert pointOfWork != null, returnString = 'Work shift not created';

      WorkShift workShift = InventoryTestUtils.findOrCreateWorkShift(inWorkShiftName, pointOfWork, new Date(System.currentTimeMillis()), TimeUnit.HOURS.toMillis(duration));
      returnString = 'Work shift created';
      assert workShift != null, returnString = 'Work shift not created';
    } catch (Exception ex) {
      returnString = 'Work shift not created ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges the given che and all of the work instructions, work assignments and ECEvents associated with the che.
   * Clears the corresponding Teams tables and delete the Che Zones
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeChe<br>
   * cheId=Name of the CHE(s) to be purged, more than one CHE can be purged at the same when given in comma separated format
   * @return JSON , <code>Che purged</code> if success, else<br> <code>Che not purged</code>
   * @Example
   * Table invoked in SPARCS : xps_che,inv_wi,xps_workassignment,xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeChe" /&gt;<br>
   * &lt;parameter id="cheId" value="ASC1" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeChe(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeChe" />
                                        <parameter id="cheId" value="<che Id list>" />'''

    String inCheIdList = _testCommandHelper.checkParameter('cheId', inParameters);
    String cheName = null;
    //Iterate through each gKey and delete
    String[] cheList = inCheIdList.split(",");
    try {
      cheList.each {
        cheName = it;
        // look up the pkeys of the CHEs
        Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
                .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, cheName))));
        if (che != null) {
          che.setChePkey(che.getChePkey());
          che.setCheGkey(che.getCheGkey());
          HibernateApi.getInstance().delete(che, true);
          returnString = 'Che purged';
        } else {
          returnString = 'Che purged';
        }
      }
    } catch (Exception ex) {
      returnString = 'Che not purged - ' + 'Reason:' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Clears cheZone values by finding the che zone with the given block name and che
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ClearCheZone<br
   * cheId=Name of the CHE for which the CHE zone needs to be cleared<br
   * blockName=Name of the block to find the CHE Zone
   * @return <code>Chezone cleared</code> if success, else<br<code>Clearing Chezone failed</code>
   * @Example
   * Table invoked in SPARCS : xps_chezone<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ClearCheZone" /&gt;<br>
   * &lt;parameter id="cheId" value="ASC21" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ClearCheZone(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="ClearCheZone" />
                                        <parameter id="cheId" value="<Che Id>" />'''
    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    Che che = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
            .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inCheId))));
    try {
      if (che != null) {
        DomainQuery dq = QueryUtils.createDomainQuery(ArgoEntity.CHE_ZONE).addDqPredicate(PredicateFactory.disjunction()
                .add(PredicateFactory.eq(ArgoField.CHEZONE_CHE_ID, che.cheId)));
        HibernateApi.getInstance().deleteByDomainQuery(dq);
        returnString = "Chezone cleared";
      } else {
        returnString = "Clearing Chezone failed: Che is null";
      }
    } catch (Exception ex) {
      returnString = "Clearing Chezone failed:" + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges all EC events related to given CHE.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeAllECEvents<br>
   * cheId=Name of the CHE to find the ec event related to that
   * @return JSON , <code>EC Events purged</code> if success, else<br> <code>EC Events not purged</code>
   * @Example
   * Table invoked in SPARCS : xps_ecevent<br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeAllECEvents" /&gt;<br>
   * &lt;parameter id="cheId" value="AGV1,AGV2" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeAllECEvents(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeAllECEvents" />
                                        <parameter id="cheId" value="<che name list>" />'''

    String inCheIdList = _testCommandHelper.checkParameter('cheId', inParameters);
    String[] cheList = inCheIdList.split(",");
    DomainQuery dq = null;
    try {
      cheList.each {
        //Purge All EC events related to the obtained che
        dq = QueryUtils.createDomainQuery(ArgoEntity.EC_EVENT).addDqPredicate(PredicateFactory.disjunction()
                .add(PredicateFactory.eq(ArgoField.ECEVENT_CHE_NAME, it)));
        HibernateApi.getInstance().deleteByDomainQuery(dq);
      }
    } catch (Exception ex) {
      returnString = 'EC Events not purged - ' + 'Reason:' + ex;
    }
    returnString = 'EC Events purged';
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges given pool(s)<br>
   * Either purges a single pool or multiple pools<br>
   * For multiple pools,pass the pool name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgePool<br>
   * poolName=Pool Name<br>
   * @return <code>Pool purged</code> if purged successfully,else<br>
   *          <code>Pool not purged</code>
   * @Example
   * Table invoked by SPARCS : xps_pool
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgePool" /&gt;<br>
   * &lt;parameter id="poolName" value="AGVPool" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgePool(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgePool" />
                                        <parameter id="poolName" value="<Pool Name>" />'''
    String inPoolList = _testCommandHelper.checkParameter('poolName', inParameters);
    DomainQuery dq = null;
    ChePool pool = null;
    String[] poolList = inPoolList.split(',');
    try {
      poolList.each {
        dq = QueryUtils.createDomainQuery(ArgoEntity.CHE_POOL)
                .addDqPredicate(PredicateFactory.eq(ArgoField.POOL_NAME, it));
        pool = ((ChePool) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq));
        if (pool != null) {
          HibernateApi.getInstance().delete(pool, true); //delete pool
          returnString = 'Pool purged';
        } else {
          returnString = 'Pool not found'
        }
        HibernateApi.getInstance().flush();
      }
    } catch (Exception ex) {
      returnString = 'Pool not purged' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges Point of works which were created earlier.<br>
   * Either purges a single pow or multiple pows<br>
   * For multiple pows,pass the pow name in a comma separated format.<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgePow<br>
   * powName=Name of the pow(s) that needs to be purged
   * @return <code>Pow purged</code> if purged successfully,else<br>
   *         <code>Pow not purged</code>
   * @Example
   * Table invoked by SPARCS : xps_pointofowork
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgePow" /&gt;<br>
   * &lt;parameter id="powName" value="QC01" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgePow(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgePow" />
                                        <parameter id="powName" value="<pow name>" />'''
    String inPowList = _testCommandHelper.checkParameter('powName', inParameters);
    DomainQuery dq = null;
    PointOfWork pointOfWork = null;
    String[] powList = inPowList.split(',');
    try {
      powList.each {
        String pow = it;
        dq = QueryUtils.createDomainQuery(ArgoEntity.POINT_OF_WORK)
                .addDqPredicate(PredicateFactory.eq(ArgoField.POINTOFWORK_NAME, pow));
        pointOfWork = ((PointOfWork) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq));
        if (pointOfWork != null) {
          HibernateApi.getInstance().delete(pointOfWork, true);
          returnString = 'Pow purged';
        } else {
          returnString = 'Point of work not found'
        }
        HibernateApi.getInstance().flush();
      }
    } catch (Exception ex) {
      returnString = 'Pow not purged' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges the work assignment.<br>
   * Either purges a single work assignment or multiple work assignments<br>
   * For multiple work assignments,pass the work assignment gKey in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeWorkAssignment<br>
   * waGkey=wa gkey  to be purged
   * @return <code>WorkAssignment purged</code> if purged successfully,else<br>
   *         <code>WorkAssignment not purged</code>
   * @Example
   * Table invoked by SPARCS : xps_workassignment
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeWA" /&gt;<br>
   * &lt;parameter id="waGkey"  value="821" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeWorkAssignment(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeWorkAssignment" />
                                        <parameter id="waGkey" value="<WA GKey>" />'''
    String inWAList = _testCommandHelper.checkParameter('waGkey', inParameters);
    DomainQuery dq = null;
    WorkAssignment workAssignment = null;
    List result = null;
    String[] workAssignmentList = inWAList.split(',');
    try {
      workAssignmentList.each {
        dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_ASSIGNMENT).addDqPredicate(PredicateFactory.disjunction()
                .add(PredicateFactory.eq(ArgoField.WORKASSIGNMENT_GKEY, it)));
        workAssignment = HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq)
        if (workAssignment != null) {
          //Delete WA corresponding to the gKey
          HibernateApi.getInstance().delete(workAssignment, true);
          HibernateApi.getInstance().flush();
          returnString = 'WorkAssignment purged';
        } else {
          returnString = 'WorkAssignment purged'
        }
      }
    } catch (Exception ex) {
      returnString = 'WorkAssignment not purged' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges the work shift(s).<br>
   * Either purges a single work shift or multiple work shifts<br>
   * For multiple work shifts,pass the work shift name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeWorkShift<br>
   * workShiftName=Name of the work shift to be purged
   * @return JSON , <code>WorkShift purged</code> if purged successfully,else<br>
   *               <code>WorkShift not purged</code>
   * @Example
   * Table invoked by SPARCS : xps_cranshift
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeWorkShift" /&gt;<br>
   * &lt;parameter id="workShiftName" value="WS01" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeWorkShift(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeWorkShift" />
                                        <parameter id="workShiftName" value="<workShiftName>'''
    String inWorkShiftList = _testCommandHelper.checkParameter('workShiftName', inParameters);
    DomainQuery dq = null;
    String[] workShiftList = inWorkShiftList.split(',');
    try {
      workShiftList.each {
        dq = QueryUtils.createDomainQuery(ArgoEntity.WORK_SHIFT)
                .addDqPredicate(PredicateFactory.eq(ArgoField.WORKSHIFT_SHIFT_NAME, it))
        if (dq != null) {
          //Delete WA corresponding to the gKey
          HibernateApi.getInstance().deleteByDomainQuery(dq)
          HibernateApi.getInstance().flush();
          returnString = 'WorkShift purged';
        } else {
          returnString 'WorkShift not found'
        }
      }
    } catch (Exception ex) {
      returnString = 'WorkShift not purged ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges Work Queue(s)<br>
   * Either purges a single work queue or multiple work queues<br>
   * For multiple work queues,pass the work queue name in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeWorkQueue<br>
   * workQueueName=wq name
   * @return JSON , <code>WorkQueue purged</code> if purged successfully,else<br>
   *               <code>WorkQueue not purged</code>
   * @Example
   * Table invoked by SPARCS : inv_wq
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeWorkQueue" /&gt;<br>
   * &lt;parameter id="workQueueName" value="yard-Admin" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeWorkQueue(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeWorkQueue" />
                                        <parameter id="workQueueName" value="<workQueue Name>'''
    String inWorkQueueList = _testCommandHelper.checkParameter('workQueueName', inParameters);
    DomainQuery dq = null;
    WorkQueue workQueue = null;

    String[] workQueueList = inWorkQueueList.split(',');
    try {
      workQueueList.each {
        dq = QueryUtils.createDomainQuery(MovesEntity.WORK_QUEUE)
                .addDqPredicate(PredicateFactory.eq(MovesField.WQ_NAME, it))
        workQueue = HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
        if (workQueue != null) {
          //Delete WA corresponding to the gKey
          HibernateApi.getInstance().delete(workQueue, true);
          HibernateApi.getInstance().flush();
          returnString = 'WorkQueue purged';
        } else {
          returnString = 'WorkQueue not found'
        }
      }
    } catch (Exception ex) {
      returnString = 'WorkQueue not purged ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges all Work instructions assigned to the given Work Queue name.<br
   * Finds the work instruction in the given Queue and purges it one by one.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeAllWorkInstruction<br
   * workQueueName=workQueue Name to find all work instruction in the queue
   * @return JSON , <code>All Work Instruction purged</code> if purged successfully,else<br
   *               <code>Work Instructions are not purged</code>
   * @Example
   * Table invoked by SPARCS : inv_wi
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeAllWorkInstruction" /&gt;<br>
   * &lt;parameter id="workQueueName" value="yard-Admin" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeAllWorkInstruction(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeAllWorkInstruction" />
                                        <parameter id="workQueueName" value="<workQueue Name>" />'''
    String inWorkQueueName = _testCommandHelper.checkParameter('workQueueName', inParameters);
    DomainQuery dq = null;

    try {
      dq = QueryUtils.createDomainQuery(MovesEntity.WORK_QUEUE).addDqPredicate(PredicateFactory.disjunction()
              .add(PredicateFactory.eq(MovesField.WQ_NAME, inWorkQueueName)));

      if (dq != null) {
        //Get WQ object
        WorkQueue workQueue = (WorkQueue) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dq);
        if (workQueue != null) {
          dq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION).addDqPredicate(PredicateFactory.disjunction()
                  .add(PredicateFactory.eq(MovesField.WI_WQ_PKEY, workQueue.wqPkey)));
          List wiList = HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
          if (wiList != null || wiList.isEmpty() == false) {
            wiList.each {
              HibernateApi.getInstance().delete(it, true); //delete WI one by one...
              returnString = 'All Work Instruction purged';
            }
          } else {
            returnString = 'All Work Instruction purged'
          };
          HibernateApi.getInstance().flush();
        } else {
          returnString = 'WorkQueue not found'
        }
      }
    } catch (Exception ex) {
      returnString = 'Work Instructions are not purged:' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Creates platform with slots for QC
   *
   * @param inParameters The map containing the method name to call along with the parameters
   * command=CreateQCPlatform
   * cheId=Name of the CHE
   * @return QC Che associated with platform
   * @Example
   *
   */
  public String CreateQCPlatform(Map inParameters) {
    assert inParameters.size() == 2, '''Must supply 2 parameters:
                                        <parameter id="command" value="CreateQCPlatform" />
                                        <parameter id="cheId" value="<CheId>" />   '''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);

    try {
      ChePool chePool = ControlTestUtils.findOrCreateChePool("UNASSIGNED");
      assert chePool != null, returnString = 'Che pool not available';
      PointOfWork qcPow = ControlTestUtils.findOrCreatePow(inCheId, chePool);
      assert qcPow != null, returnString = 'Point of work not available';
      Che qcChe = Che.findCheByShortName(inCheId, ContextHelper.getThreadYard());
      if (qcChe != null) {
        qcChe.setCheStatusEnum(CheStatusEnum.WORKING);
        StackBlock qcPlatform = YardTestUtils.findOrCreateStackBlock(inCheId, YardTestUtils.getDefaultQCPlatformBlockFieldChanges());
        if (qcPlatform != null) {
          YardTestUtils.purgeUnitsReferencingBins(Arrays.<Serializable> asList(qcPlatform.getAbnGkey()));
          YardTestUtils.createOrUpdateSectionsAndStacks(qcPlatform, 1, 2, 1, 2, YardTestUtils.getDefaultQCPlatformSectionFieldChanges(), true);
          CheZone zone = ControlTestUtils.findOrCreateCheZone(qcChe, new BlockRange(qcPlatform, 1, 2, 1, 2, 0, 0));
          returnString = 'QCPlatform is created for ' + inCheId;
          if (zone == null) {
            returnString = 'QCPlatform not created - zone is null'
          }
        } else {
          returnString = 'QCPlatform not created - Stack block not available'
        };
      } else {
        returnString = 'QCPlatform not created - Che not available'
      };
    } catch (Exception ex) {
      returnString = 'Exception while creating QCPlatform :' + ex
    }
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Creates a work instruction (plans a move for the given unit)
   *
   @param inParameters The map containing the method name to call along with the parameters
    * <parameter id="command" value="CreateWorkInstruction" />
    * <parameter id="unitId" value="<Unit Id>" />
    * <parameter id="workQueueName" value="<Work Queue Name>" />
    * <parameter id="moveKind" value="<Move Kind> = WiMoveKindEnum values" />
    * <parameter id="moveStage" value="<Move Stage> = WiMoveStageEnum values" />
    * <parameter id="location" value="<Location>" />
    * */
  public String CreateWorkInstruction(Map inParameters) {
    assert inParameters.size() == 6, '''Must supply 6 parameters:
                                        <parameter id="command" value="CreateWorkInstruction" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="workQueueName" value="<Work Queue Name>" />
                                        <parameter id="moveKind" value="<Move Kind> = WiMoveKindEnum values" />
                                        <parameter id="moveStage" value="<Move Stage> = WiMoveStageEnum values" />
                                        <parameter id="location" value="<Location>" />'''

    String inUnitId = _testCommandHelper.checkParameter('unitId', inParameters);
    String inWorkQueueName = _testCommandHelper.checkParameter('workQueueName', inParameters);
    String inMoveKind = _testCommandHelper.checkParameter('moveKind', inParameters);
    String inMoveStage = _testCommandHelper.checkParameter('moveStage', inParameters);
    String inLocation = _testCommandHelper.checkParameter('location', inParameters);

    //Find yard gkey
    Yard yard = ContextHelper.getThreadYard();
    final Long yrdGkey = yard.getYrdGkey();

    //Find work queue
    DomainQuery dqWq = QueryUtils.createDomainQuery(MovesEntity.WORK_QUEUE)
            .addDqPredicate(PredicateFactory.eq(MovesField.WQ_NAME, inWorkQueueName))
            .addDqPredicate(PredicateFactory.eq(MovesField.WQ_YARD, yrdGkey));
    WorkQueue workQueue = (WorkQueue) HibernateApi.getInstance().getUniqueEntityByDomainQuery(dqWq);
    assert workQueue != null, 'Work queue ' + inWorkQueueName + ' not available';

    //Find the ufv
    UnitFinder finder = (UnitFinder) Roastery.getBean(UnitFinder.BEAN_ID);
    SearchResults results = finder.findUfvByDigits(inUnitId, false, false);

    assert results.getFoundCount() == 1, 'Found zero or more than one equipment id ' + inUnitId;
    UnitFacilityVisit ufv = (UnitFacilityVisit) HibernateApi.getInstance().load(UnitFacilityVisit.class, results.getFoundPrimaryKey());
    UnitYardVisit uyv = ufv.getUyvForYard(yard);
    assert uyv != null, "Could not find equipment " + inUnitId;

    WorkInstruction wi = InventoryTestUtils.createWorkInstruction(uyv, workQueue,
            WiMoveKindEnum."$inMoveKind", WiMoveStageEnum."$inMoveStage", inLocation);

    returnString = 'Work instruction created';
    assert wi != null, returnString = 'Work instruction creation failed';

    builder {
      actual_result returnString
      if (wi != null) {
        data('gkey': wi.wiGkey)
      }
    }
    return builder;
  }

  /**
   * Fetches the pre defined filter results from the given cluster.
   * @param inParameters
   * @return
   */
  public String UniversalQuery(Map inParameters) {
    assert inParameters.size() == 10, '''Must supply 10 parameters:
                                        <parameter id="command" value="UniversalQuery" />
                                        <parameter id="filterName" value="Name of the filter" />
                                        <parameter id="ipAddress" value="<IP address to post the http request>" />
                                        <parameter id="port" value="<port>" />
                                        <parameter id="userName" value="username for the authentication" />
                                        <parameter id="password" value="password to authenticate access" />
                                        <parameter id="operatorId" value="filter name to be queried" />
                                        <parameter id="complexId" value="username for the authentication" />
                                        <parameter id="facilityId" value="password to authenticate access" />
                                        <parameter id="yardId" value="filter name to be queried" />'''

    String inIpAddress = _testCommandHelper.checkParameter('ipAddress', inParameters);
    String inPort = _testCommandHelper.checkParameter('port', inParameters);
    String inUserName = _testCommandHelper.checkParameter('userName', inParameters);
    String inPassword = _testCommandHelper.checkParameter('password', inParameters);
    String inFilterName = _testCommandHelper.checkParameter('filterName', inParameters);
    String inOperatorId = _testCommandHelper.checkParameter('operatorId', inParameters)
    String inComplexId = _testCommandHelper.checkParameter('complexId', inParameters)
    String inFacilityId = _testCommandHelper.checkParameter('facilityId', inParameters)
    String inYardId = _testCommandHelper.checkParameter('yardId', inParameters)

    returnString = handleHTTPRequests(inIpAddress, inPort.toInteger().intValue(), inUserName, inPassword, inFilterName, inOperatorId, inComplexId, inFacilityId, inYardId)
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Gets the response of the http request and return it as a string
   * @param httpRequest
   */
  private String handleHTTPRequests(String ip, int port, String userName, String pwd, String filterName, String operatorId, String complexId, String facilityId, String yardId) {
    String responseString = "";
    try {
      String httpRequest = "http://" + ip + ":" + port + "/apex/api/query?filtername=" + filterName + "&operatorId=" + operatorId + "&complexId=" + complexId + "&facilityId=" + facilityId + "&yardId=" + yardId
      //def addr = "http://localhost:8280/apex/api/query?filtername=IN_AGW_YARD&operatorId=DPW&complexId=DPWA&facilityId=AGW&yardId=AGW";
      def authString = userName + ":" + pwd   //form the authString using username,password
      String authStringEnc = authString.getBytes().encodeBase64().toString();  //encrypt the password before sending it
      URLConnection conn = httpRequest.toURL().openConnection()
      conn.setRequestProperty("Authorization", "Basic ${authStringEnc}")
      InputStream is = conn.getInputStream();    // get the stream output from http response
      InputStreamReader isr;
      StringBuffer sb
      if (is != null) {
        isr = new InputStreamReader(is)
      } else {
        responseString = 'Response not obtained, request may be invalid'
        return responseString
      }
      int numCharsRead;
      char[] charArray = new char[1024];
      if (isr != null) {
        sb = new StringBuffer();   //read the response and display
        while ((numCharsRead = isr.read(charArray)) > 0) {
          sb.append(charArray, 0, numCharsRead);
        }
      } else {
        responseString = 'Response not obtained, request may be invalid'
        return responseString
      }
      responseString = sb.toString();
    } catch (Exception ex) {
      responseString = 'Failed to get the response:' + ex
      return responseString
    }
    return responseString
  }

  /**
   * Finds ASC and updates its last know position with the given value
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=SetAscLastKnownPosition<br>
   * cheId=ASC or AGV name<br>
   * position=<position value>
   * @return JSON ,<code>ASC position set</code> if success
   *               <code>ASC position not set</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetAscLastKnownPosition"/&gt;<br>
   * &lt;parameter id="cheId" value="ASC01"/&gt;<br>
   * &lt;parameter id="position" value="AS06A02.A"/&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SetCheLastKnownPosition(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="SetCheLastKnownPosition" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="cheLastPosition" value="<last position of ASC>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inCheLastPosition = _testCommandHelper.checkParameter('cheLastPosition', inParameters);
    try {
      //find che
      Che cheObj = ((Che) HibernateApi.getInstance().getUniqueEntityByDomainQuery(QueryUtils.createDomainQuery(ArgoEntity.CHE)
              .addDqPredicate(PredicateFactory.eq(ArgoField.CHE_SHORT_NAME, inCheId))));

      if (cheObj != null) {
        LocPosition cheLastPosition = LocPosition.createYardPosition(
                ContextHelper.getThreadYard(), inCheLastPosition, "", EquipBasicLengthEnum.BASIC20, true);

        cheObj.setCheLastPosition(inCheLastPosition);
        cheObj.setCheLastKnownLocPos(cheLastPosition);

        HibernateApi.getInstance().save(cheObj);
      }
      returnString = "Che position set";
    } catch (Exception ex) {
      returnString = "Che position not set " + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Sets LengthsAllowed field of a section for a given block name with the given value.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br><br>
   * command=SetSectionAttribute<br>
   * blockName=Name of the block<br>
   * rowIndex=Row Index in the block for which the attribute value to be set<br>
   * attribute=LengthsAllowed<br>
   * value=ONLY_20, ANY, etc..
   * @return JSON , <code>Attributes of a section are set</code> - if the value is set to the stack block<br>
   * 			   <code>Could not find stack block</code> - Given stack block is not found
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="SetSectionAttribute"/&gt;<br>
   * &lt;parameter id="blockName" value="asc01" /&gt;<br>
   * &lt;parameter id="rowIndex" value="8"/&gt;<br>
   * &lt;parameter id="attribute" value="LengthsAllowed"/&gt;<br>
   * &lt;parameter id="value" value="ONLY_20" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String SetSectionAttribute(Map inParameters) {
    assert inParameters.size() == 5, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 5 parameters:
                                        <parameter id="command" value="SetSectionAttribute" />
                                        <parameter id="blockName" value="<Block Name>" />
                                        <parameter id="rowIndex" value="<Row Index>" />
                                        <parameter id="attribute" value="<Attribute> = LengthsAllowed" />
                                        <parameter id="value" value="<Column Index> = ONLY_20, ANY, etc.." />'''

    String inBlockName = _testCommandHelper.checkParameter('blockName', inParameters);
    String inRowIndex = _testCommandHelper.checkParameter('rowIndex', inParameters);
    String inAttribute = _testCommandHelper.checkParameter('attribute', inParameters);
    String inValue = _testCommandHelper.checkParameter('value', inParameters);

    try {
      //Find the stack block first
      Yard thisYard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);
      StackBlock stackBlock = StackBlock.findStackBlock(yardModel, inBlockName);
      assert stackBlock != null, "Could not find stack block " + inBlockName;
      //Find the section
      YardSection section = YardSection.findYardSectionFromBlockAndRowIndex(stackBlock, inRowIndex.toLong());
      //Update attribute
      switch (inAttribute) {
        case "LengthsAllowed":
          section.updateLengthsAllowed(SectionLengthsAllowedEnum."$inValue");
          break;
      }
      returnString = 'Attributes of a section are set';
    } catch (Exception ex) {
      returnString = 'Failed to set attributes for the given section' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

/**
 * Purges a Stack Block(s). It checks whether the given block name is available<br>
 * and purges it if available.It purges more than one block at a time when blocks are
 * passed in a comma separated format.
 *
 * @param inParameters The map containing the method name to call along with the parameters<br><br>
 * command=PurgeStackBlock<br>
 * blockName=Block Name(s) to be purged,in a comma separated format when more than one block needs to be purged
 * @return JSON , <code>Block purged</code> if block purged else <code>Block not purged</code>
 * @Example
 * Table invoked in SPARCS : spatial_bins
 * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
 * &lt;parameters&gt;<br>
 * &lt;parameter id="command" value="PurgeStackBlock" /&gt;<br>
 * &lt;parameter id="blockName" value="ASCB1" /&gt;<br>
 * &lt;/parameters&gt;<br>
 * &lt;/groovy&gt;<br>
 */
  public String PurgeStackBlock(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeStackBlock" />
                                        <parameter id="blockName" value="<Block Name>" />'''

    String inBlockNameList = _testCommandHelper.checkParameter('blockName', inParameters);

    try {
      //Find Yard model
      Yard thisYard = ContextHelper.getThreadYard();
      YardBinModel yardModel = (YardBinModel) HibernateApi.getInstance().downcast(thisYard.getYrdBinModel(), YardBinModel.class);

      String[] blockNames = inBlockNameList.split(',');
      DomainQuery dq = null;
      blockNames.each {
        //Find the stack block
        StackBlock stackBlock = StackBlock.findStackBlock(yardModel, it);
        if (stackBlock != null) {
          BinContext stowageContext = BinContext.findBinContext(Yard.CONTAINER_STOWAGE_BIN_CONTEXT);
          AbstractBin bin = yardModel.findDescendantBinFromInternalSlotString(it, stowageContext);
          if (bin != null) {
            dq = QueryUtils.createDomainQuery(com.navis.spatial.BinEntity.BIN_ANCESTOR).
                    addDqPredicate(PredicateFactory.eq(BinField.BAN_BIN, bin.getAbnGkey()));
            HibernateApi.getInstance().deleteByDomainQuery(dq)
            dq = QueryUtils.createDomainQuery(com.navis.spatial.BinEntity.BIN_ANCESTOR).
                    addDqPredicate(PredicateFactory.eq(BinField.BAN_ANCESTOR_BIN, bin.getAbnGkey()));
            HibernateApi.getInstance().deleteByDomainQuery(dq)
            //YardTestUtils.purgeAllTZAssociations(stackBlock.getAbnGkey())
            YardTestUtils.purgeYardPathModel(yardModel);
            YardTestUtils.purgeGraphPaths(yardModel);
            YardTestUtils.purgeGraphVertices(yardModel);
            YardTestUtils.purgeYardBinPaths(yardModel)
            YardTestUtils.purgeYardPathModel(yardModel);
            //ArgoUtils.carefulDelete(stackBlock)
          }
          HibernateApi.getInstance().delete(stackBlock, true)
        }
      }
      returnString = 'Block purged';
    } catch (Exception ex) {
      returnString = 'Block not purged ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Purges Work Instruction.<br>
   * Either purges a single work instruction or multiple work instruction<br>
   * For multiple WI,pass the gkeys in a comma separated format.
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=PurgeWorkInstruction<br>
   * wiGkey=wiGKey of the Work Instruction needs to be purged<br>
   * @return <code>WorkInstruction purged</code> if purged,<br> else
   *         <code>WorkInstruction not purged</code>
   * @Example
   * Table invoked by SPARCS : inv_wi <br>
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="PurgeWorkInstruction" /&gt;<br>
   * &lt;parameter id="wiGkey" value="10" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String PurgeWorkInstruction(Map inParameters) {
    assert inParameters.size() == 2, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 2 parameters:
                                        <parameter id="command" value="PurgeWorkInstruction" />
                                        <parameter id="wiGkey" value="<wiGKey>" />'''

    String inWIGKey = _testCommandHelper.checkParameter('wiGkey', inParameters);
    //Delete WI corresponding to the WA's gKey
    String[] gKeyList = inWIGKey.split(',');
    try {
      gKeyList.each {
        DomainQuery dq = QueryUtils.createDomainQuery(MovesEntity.WORK_INSTRUCTION).addDqPredicate(PredicateFactory.disjunction()
                .add(PredicateFactory.eq(MovesField.WI_GKEY, it)));
        if (dq != null) {
          HibernateApi.getInstance().deleteByDomainQuery(dq);
          HibernateApi.getInstance().flush();
          returnString = 'WorkInstruction purged';
        } else {
          returnString = 'WorkInstruction not purged';
        }
      }
    } catch (Exception ex) {
      returnString = 'WorkInstruction not purged ' + ex;
    }
    builder {
      actual_result returnString;
    }
    return builder;
  }

  /**
   * Waits until there is an ecevent for given Che and event type
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=WaitForECEvent<br>
   * cheId=Name of the CHE to find the ec event related to that
   * secs : time in seconds
   * eventType = type of the event expected , it can be any of the below :<br>
   *     disptach : DSPT
   *     //ASC Container Move<br>
   *    EmptyToRow,LadenToRow,LadenAtRow<br>
   *    //AGV Travel To ASC To Receive<br>
   *    ToRowToCollect,ArriveAtRowToCollect<br>
   *    //AGV Travel To ASC To Deliver<br>
   *    ToRowToDrop,ArriveAtRowToDrop <br>
   *    // AGV Travel To QC To Receive <br>
   *    ToVesselToCollect,ArriveAtVesselToCollect <br>
   *    //AGV Travel To QC To Deliver <br>
   *    ToVesselToDrop,ArriveAtVesselToDrop <br>
   *    //QC Known Container Move (Load)<br>
   *    QcFetch,QcPut <br>
   *    //common <br>
   *    Complete,Idle
   * @return JSON , <code>Wait for EC event succeeded</code> if success, else<br> <code>Wait for EC event failed</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand_backup"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="WaitForECEvent" /&gt;<br>
   * &lt;parameter id="cheId" value="AGV1" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000001" /&gt;<br>
   * &lt;parameter id="eventType" value="" /&gt;<br>
   * &lt;parameter id="secs" value="2" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String WaitForECEvent(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="WaitForECEvent" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="eventType" value="<ec event type>" />
                                        <parameter id="secs" value="<seconds>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inUnitId = inParameters.get('unitId');
    String inEcEventType = _testCommandHelper.checkParameter('eventType', inParameters);
    String inSeconds = _testCommandHelper.checkParameter('secs', inParameters);
    def result;
    try {
      String ecEventType = _testCommandHelper.ecEventVarMap.get(inEcEventType);
      result = ControlTestUtils.waitForEvent(inCheId, EcEventTypeConsts."$ecEventType", inSeconds.toLong(), ControlTestUtils.WaitForEventClassEnum.EC_EVENT);
    } catch (Exception ex) {
      returnString = "No EC event listener registered";
      LOGGER.error('Wait for EC event : ' + inEcEventType + ' failed with exception :' + ex)
    }
    if (result != null) {
      if (result == false) {
        returnString = 'Wait for EC event failed : ' + result;
      } else {
        returnString = 'Wait for EC event succeeded :' + result;
      }
    } else {
      returnString = 'Wait for EC event failed : ' + result
    }
    LOGGER.debug('Wait for EC event : ' + inEcEventType + ':' + result)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Waits until there is an ecevent for given Che and event type
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=WaitForECIEvent<br>
   * cheId=Name of the CHE to find the ec event related to that<br>
   * secs : seconds to wait <br>
   * eventType : ECI_ASC_ORDER_ENTERED <br>
   * ECI_AGV_ORDER_ENTERED <br>
   * ECI_AGV_ORDER_COMPLETED  <br>
   * ECI_QC_CONTAINER_IDENTIFIED <br>
   * ECI_QC_ORDER_COMPLETED <br>
   * @return JSON , <code>Wait for ECI event succeeded</code> if success, else<br> <code>Wait for EC event failed</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand_backup"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="WaitForECIEvent" /&gt;<br>
   * &lt;parameter id="cheId" value="AGV1" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000001" /&gt;<br>
   * &lt;parameter id="eventType" value="" /&gt;<br>
   * &lt;parameter id="secs" value="2" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String WaitForECIEvent(Map inParameters) {
    assert inParameters.size() == 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="WaitForECIEvent" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="eventType" value="<ec event type>" />
                                        <parameter id="secs" value="<seconds>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inEciEventType = _testCommandHelper.checkParameter('eventType', inParameters);
    String inSeconds = _testCommandHelper.checkParameter('secs', inParameters);
    def result;
    try {
      result = ControlTestUtils.waitForEvent(inCheId, inEciEventType, inSeconds.toLong(), ControlTestUtils.WaitForEventClassEnum.ECI_EVENT);
    } catch (Exception ex) {
      returnString = "No ECI event listener registered";
      LOGGER.error('Wait for ECI event : ' + inEciEventType + ' failed with exception :' + ex)
    }
    if (result != null) {
      if (result == false) {
        returnString = 'Wait for ECI event failed : ' + result;
      } else {
        returnString = 'Wait for ECI event succeeded :' + result;
      }
    } else {
      returnString = ' Wait for ECI event failed : ' + result
    }
    LOGGER.debug('Wait for ECI event : ' + inEciEventType + ':' + result)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Listens for a particular ec event and notifies when it occurs<br>
   *
   * @param inParameters The map containing the method name to call along with the parameters<br>
   * command=ListenForECEvent<br>
   * cheId=Name of the CHE to find the ec event related to that<br>
   *  secs : time in seconds
   * eventType = type of the event expected , it can be any of the below :<br>
   *    //Dispatch
   *     Dispatch
   *     //ASC Container Move<br>
   *    EmptyToRow,LadenToRow,LadenAtRow<br>
   *    //AGV Travel To ASC To Receive<br>
   *    ToRowToCollect,ArriveAtRowToCollect<br>
   *    //AGV Travel To ASC To Deliver<br>
   *    ToRowToDrop,ArriveAtRowToDrop <br>
   *    // AGV Travel To QC To Receive <br>
   *    ToVesselToCollect,ArriveAtVesselToCollect <br>
   *    //AGV Travel To QC To Deliver <br>
   *    ToVesselToDrop,ArriveAtVesselToDrop <br>
   *    //QC Known Container Move (Load)<br>
   *    QcFetch,QcPut <br>
   *    //common <br>
   *    Complete,Idle
   * @return JSON , <code>Wait for EC event succeeded</code> if success, else<br> <code>Wait for EC event failed</code>
   * @Example
   * &lt;groovy class-location="classpath" class-directory="test/extension/groovy" class-name="TestCommand_backup"&gt;<br>
   * &lt;parameters&gt;<br>
   * &lt;parameter id="command" value="ListenForECEvent" /&gt;<br>
   * &lt;parameter id="cheId" value="AGV1" /&gt;<br>
   * &lt;parameter id="unitId" value="TEST0000001" /&gt;<br>
   * &lt;parameter id="eventType" value="" /&gt;<br>
   * &lt;parameter id="secs" value="2" /&gt;<br>
   * &lt;/parameters&gt;<br>
   * &lt;/groovy&gt;<br>
   */
  public String ListenForECEvent(Map inParameters) {
    assert inParameters.size() >= 4, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 4 parameters:
                                        <parameter id="command" value="ListenForECEvent" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="unitId" value="<Unit Id>" />
                                        <parameter id="eventType" value="<ec event type>" />
                                        <parameter id="secs" value="<seconds>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inUnitId = inParameters.get('unitId')
    String inEcEventType = _testCommandHelper.checkParameter('eventType', inParameters);
    String inSeconds = _testCommandHelper.checkParameter('secs', inParameters);
    def result;
    try {
      String ecEventType = _testCommandHelper.ecEventVarMap.get(inEcEventType);
      result = ControlTestUtils.listenForEvent(inCheId, EcEventTypeConsts."$ecEventType", ControlTestUtils.WaitForEventClassEnum.EC_EVENT);
    } catch (Exception ex) {
      returnString = "No EC event listener registered";
      LOGGER.error('Listen for EC event : ' + inEcEventType + ' failed with exception :' + ex)
    }
    if (result != null) {
      if (result == false) {
        returnString = 'Listen for EC event failed : ' + result
      } else {
        returnString = 'Listen for EC event succeeded :' + result
      };
    } else {
      returnString = ' Listen for EC event failed : ' + result
    }
    LOGGER.debug('Listen for EC event : ' + inEcEventType + ':' + result)
    builder {
      actual_result returnString
    }
    return builder;
  }

  public String ListenForECIEvent(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="ListenForECEvent" />
                                        <parameter id="cheId" value="<che Id>" />
                                        <parameter id="eventType" value="<ec event type>" />'''

    String inCheId = _testCommandHelper.checkParameter('cheId', inParameters);
    String inEciEventType = _testCommandHelper.checkParameter('eventType', inParameters);

    def result;
    try {
      result = ControlTestUtils.listenForEvent(inCheId, inEciEventType, ControlTestUtils.WaitForEventClassEnum.ECI_EVENT);
    } catch (Exception ex) {
      returnString = "No ECI event listener registered";
      LOGGER.error('Listen for ECI event : ' + inEciEventType + ' failed with exception :' + ex)
    }
    if (result != null) {
      if (result == false) {
        returnString = 'Listen for ECI event failed'
      } else {
        returnString = 'Listen for ECI event succeeded :' + result
      };
    } else {
      returnString = ' Listen for ECI event failed : ' + result
    }
    LOGGER.debug('Listen for ECI event : ' + inEciEventType + ':' + result)
    builder {
      actual_result returnString
    }
    return builder;
  }

  /**
   * Wait for the given event to occur by polling the database for every 15 secs till the event occurs or timeOut happens
   * @param inParameters
   * @return
   */
  public String WaitForDBEvent(Map inParameters) {
    assert inParameters.size() == 3, 'Supplied ' + inParameters.size() + ' parameters. ' +
            '''Must supply 3 parameters:
                                        <parameter id="command" value="WaitForDBEvent" />
                                        <parameter id="sqlStatement" value="<SQL query>" />
                                        <parameter id="timeOut" value="<in secs>" />'''

    String inSQLQuery = _testCommandHelper.checkParameter('sqlStatement', inParameters);
    String inTimeOut = _testCommandHelper.checkParameter('timeOut', inParameters);

    try {
      returnString = WaitForDBEvent(inSQLQuery, 15, inTimeOut)
      LOGGER.error('Wait for DB event response : ' + inSQLQuery + ":" + returnString)
    } catch (Exception ex) {
      ex.printStackTrace()
      LOGGER.error('Wait for DB event failed : ' + inSQLQuery + ":" + ex)
    }
  }

  /**
   * Starts polling DB for the given SQLQuery's result.
   * Stops polling DB either on the event happens or the timeout happens
   * @param sqlQuery
   * @param pollingFrequency
   * @param timeOut
   * @return
   */
  public String WaitForDBEvent(String sqlQuery, long pollingFrequency, long timeOut) {
    List resultList = null;
    long duration = System.currentTimeMillis()
    resultList = getSQLResultList() //gets query results from DB
    def result = 'WaitForDBEvent failed' // by default setting it to 'failed' msg, it will be overwritten if successful
    if (resultList.empty == false) {
      result = 'WaitForDBEvent succeeded'
    } else { //if db event for the given sql query did not occur, start polling the DB
      boolean timeOutExit = true;
      while (resultList.empty == true && duration < timeOut) {
        sleep(pollingFrequency)
        resultList = getSQLResultList() //gets query results from DB
        if (resultList.empty == false) {
          result = 'Wait succeeded'
        }
        timeOutExit = false; //setting time out exit false here, as the event occured before time out
        break
      }

      if (timeOutExit == false) {
        result = 'WaitForDBEvent succeeded'
      } else {
        result = 'WaitForDBEvent failed , exit because of timeOut'
      }
    }
  }

  /**
   * Gets SQL result list for the given SQL query
   * @param sqlQuery
   * @return
   */
  private List getSQLResultList(String sqlQuery) {
    SQLQuery resultSet = null;
    List resultList = null;
    resultSet = HibernateApi.getInstance().getCurrentSession().createSQLQuery(sqlQuery);
    resultList = resultSet.list();
    return resultList
  }
}

