import com.navis.argo.ContextHelper
import com.navis.argo.business.atoms.LaneStatusEnum
import com.navis.argo.business.atoms.LaneTruckStatusEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.Lane
import com.navis.argo.portal.context.ArgoUserContext
import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.presentation.FrameworkPresentationUtils
import com.navis.road.RoadField
import com.navis.road.business.api.RoadManager
import com.navis.road.business.atoms.TruckVisitStatusEnum
import com.navis.road.business.model.GateLane
import com.navis.road.business.model.RoadManagerPea
import com.navis.road.business.model.TruckVisitDetails
import com.navis.road.business.reference.Console
import org.apache.log4j.Logger

/**
 * Created by psethuraman on 10/2/2017.
 */

public class MATTruckVisitELI extends AbstractEntityLifecycleInterceptor {
    private static Logger LOGGER = Logger.getLogger(MATTruckVisitELI.getClass());
    @Override
    void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
        //this.onCreateOrUpdate(inEntity, inOriginalFieldChanges, inMoreFieldChanges);
    }

    @Override
    void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
        onCreateOrUpdate(inEntity, inOriginalFieldChanges, inMoreFieldChanges);
    }

    void onCreateOrUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges)
    {
        LOGGER.warn("Truck Visit ELI Started : "+inEntity._entity);
        if (inOriginalFieldChanges == null) {
            return;
        }

        Object visit = inEntity._entity;
        LOGGER.warn("Instance type : "+visit.getClass());
        TruckVisitDetails visitDetails = (TruckVisitDetails) visit;
        LOGGER.warn("Instance type : "+visitDetails.getClass());

//        CarrierVisit cv = vesselVisitDetails.getCvdCv();
        if (visitDetails == null) {
            LOGGER.warn("Truck Visit ELI  is null : ");
            return;
        }

        TruckVisitStatusEnum statusEnum = visitDetails.getTvdtlsTruckStatus();
        if (inOriginalFieldChanges.hasFieldChange(RoadField.TVDTLS_STATUS)) {
            LOGGER.warn("has field changes : ")
            statusEnum = (TruckVisitStatusEnum) inOriginalFieldChanges.findFieldChange(RoadField.TVDTLS_STATUS).getNewValue();
        } else {
            LOGGER.warn("No field changes")
        }
        LOGGER.warn("Truck Visit ELI status : "+statusEnum);
        if (TruckVisitStatusEnum.CANCEL.equals(statusEnum) || TruckVisitStatusEnum.CLOSED.equals(statusEnum)) {
            GateLane gateLane = visitDetails.getTvdtlsExitLane();
            if (gateLane != null) {
                LOGGER.warn("Truck Visit ELI LANE GKEY to clear : " + gateLane.getLaneGkey());
                Long laneGkey = gateLane.getLaneGkey();
                if (laneGkey != null) {
                    RoadManager rm = (RoadManager) Roastery.getBean(RoadManagerPea.BEAN_ID);
                    rm.clearTruckFromLane(laneGkey);
                    gateLane.setLaneTruckStatus(LaneTruckStatusEnum.EMPTY);
                    gateLane.setLaneStatus(LaneStatusEnum.OPEN);
                    LOGGER.warn("Truck Visit ELI LANE cleared : " + gateLane.getLaneId());
                    ArgoUserContext userContext = ContextHelper.getThreadUserContext();
                    //ArgoUserContext userContext = (ArgoUserContext) FrameworkPresentationUtils.getUserContext();
                    Console console = userContext != null ? HibernateApi.getInstance().load(Console.class, userContext.getConsoleGkey()) : null;
                    LOGGER.warn("Truck Visit ELI Console to be cleared : " + console);
                    Lane null_value;
                    if (console != null) {
                        gateLane.setLaneTruckStatus(LaneTruckStatusEnum.EMPTY);
                        console.setHwLaneSelected(null_value);
                        LOGGER.warn("Truck Visit ELI Console cleared : " + console.getHwId());
                    }
                    /*HibernateApi.getInstance().update(gateLane);
                    HibernateApi.getInstance().update(console);*/
                    HibernateApi.getInstance().flush();
                }
                LOGGER.warn("Truck Visit ELI LANE to clear : "+gateLane.getLaneId());
            }
        }
        LOGGER.warn("Truck Visit ELI Ended : "+inEntity._entity);
    }
}
