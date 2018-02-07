/*
 * Copyright (c) 2013 Navis LLC. All Rights Reserved.
 *
 */

package system

import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.atoms.WiMoveKindEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.model.LocPosition
import com.navis.inventory.business.moves.WorkInstruction
import com.navis.inventory.business.units.UnitFacilityVisit
import com.navis.rail.business.entity.RailcarVisit
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.rail.external.rail.AbstractOptimalTZSlotFinder
import org.apache.log4j.Level
import org.jetbrains.annotations.Nullable

import java.util.regex.Pattern


@SuppressWarnings(["GrMethodMayBeStatic", "GroovyUnusedDeclaration"])
class OptimalTZSlotFinderSample extends AbstractOptimalTZSlotFinder{

  @Override
  public void populateOptimalTZSlots() {
    def today = new Date();
    log(Level.INFO, "Job Started        \t" + today);
    List<WorkInstruction> workInstructions = getWorkInstructionList();
    log(Level.INFO, "Number of WI Fetched for the Run    \t" + workInstructions.size());
    for (WorkInstruction workInstruction : workInstructions) {
      updateOptimalTzSlot(workInstruction);
    }
    log(Level.INFO, "Job Ended          \t" + new Date());
    log(Level.INFO, "Total Runtime      \t" + ((new Date()) - today));
  }

  public void updateOptimalTzSlot(WorkInstruction inWorkInstruction) {

    log(Level.INFO, "WI\t\t\t\t" + inWorkInstruction.toString())
    final UnitFacilityVisit ufv = inWorkInstruction.getWiUfv();
    log(Level.INFO, "UFV  \t\t\t\t" + ufv.toString());
    if (ufv != null && ufv.getUfvUnit() != null && (ufv.getUfvOptimalRailTZSlot() == null || ufv.getUfvOptimalRailTZSlot().length() == 0)) {
      final LocPosition containerLocation;
      if (WiMoveKindEnum.RailDisch.equals(inWorkInstruction.getWiMoveKind())) {
        log(Level.DEBUG, "WiMoveKind is RailDisch");
        containerLocation = inWorkInstruction.getWiFromPosition();
        log(Level.DEBUG, "containerLocation  is \t\t\t" + containerLocation);
      } else if (WiMoveKindEnum.RailLoad.equals(inWorkInstruction.getWiMoveKind())) {
        log(Level.DEBUG, "WiMoveKind is RailLoad");
        containerLocation = inWorkInstruction.getWiToPosition();
        log(Level.DEBUG, "containerLocation  is \t\t\t" + containerLocation);
      } else {
        containerLocation = null;
        log(Level.INFO, "containerLocation  is null as WiMoveKind is not rail  \t\t\t" + containerLocation);
      }
      TrainVisitDetails trainVisit = null;
      RailcarVisit railcarVisit = null;
      if (LocTypeEnum.RAILCAR.equals(inWorkInstruction.getWiCarrierLocType())) {
        log(Level.DEBUG, "  WiCarrierLocType is RAILCAR");
        if (WiMoveKindEnum.RailDisch.equals(inWorkInstruction.getWiMoveKind())) {
          log(Level.DEBUG, "  WiMoveKind is RailDisch");
          RailcarVisit visit = getInboundRailcarVisit(ufv);
          //noinspection ConstantConditions
          if (getRailcarVisit(containerLocation).getLocGkey().equals(visit.getLocGkey())) {
            railcarVisit = getOutBoundRailcarVisit(ufv);
            log(Level.DEBUG, "railcarVisit\t\t\t" + railcarVisit);
          } else {
            assert containerLocation != null;
            log(Level.INFO, "The Container Position Planned " + containerLocation.getPosLocId() +
                    "doesnot match with the position planned in unit" + visit.getLocGkey());
          }
        } else if (WiMoveKindEnum.RailLoad.equals(inWorkInstruction.getWiMoveKind())) {
          log(Level.DEBUG, "  WiMoveKind is RailDisch");
          RailcarVisit visit = getOutBoundRailcarVisit(ufv);
          //noinspection ConstantConditions
          if (getRailcarVisit(containerLocation).getLocGkey().equals(visit.getLocGkey())) {
            railcarVisit = getOutBoundRailcarVisit(ufv);
            log(Level.DEBUG, "railcarVisit\t\t\t" + railcarVisit);
          } else {
            log(Level.INFO, "The Container Position Planned " + containerLocation.getPosLocId() +
                    "doesnot match with the position planned in unit" + visit.getLocGkey());
          }
        }
      } else {
        if (LocTypeEnum.TRAIN.equals(inWorkInstruction.getWiCarrierLocType())) {
          log(Level.DEBUG, "The Container is on TRAIN (Not on RailCarVisit)");
          Set<RailcarVisit> railcarVisits = Collections.EMPTY_SET;
          if (WiMoveKindEnum.RailDisch.equals(inWorkInstruction.getWiMoveKind())) {
            log(Level.DEBUG, "WiMoveKind is RailDisch")
            trainVisit = getInboundTrainVisitDetails(ufv);
            trainVisit != null ? log(Level.DEBUG, "  trainVisit" + trainVisit.toString()) : log(Level.INFO, "InboundTrainVisit is Null");
            if (trainVisit != null) {
              railcarVisits = trainVisit.getRvdtlsInboundRailcarVisits();
              railcarVisit != null ? log(Level.DEBUG, "railcarVisits are available")
              : log(Level.INFO, "railcarVisits are null for TrainVisit\t" + trainVisit.toString());
            }
          } else if (WiMoveKindEnum.RailLoad.equals(inWorkInstruction.getWiMoveKind())) {
            log(Level.DEBUG, "  WiMoveKind is RailDisch")
            trainVisit = getOutBoundTrainVisitDetails(ufv);
            trainVisit != null ? log(Level.DEBUG, "  trainVisit" + trainVisit.toString()) : log(Level.INFO, "InboundTrainVisit is Null");
            log(Level.DEBUG, "  trainVisit" + trainVisit);
            if (trainVisit != null) {
              railcarVisits = trainVisit.getRvdtlsOutboundRailcarVisits();
              railcarVisit != null ? log(Level.INFO, "railcarVisits are available")
              : log(Level.ERROR, "railcarVisits are null for TrainVisit\t" + trainVisit.toString());
            }
          }
          for (RailcarVisit visit : railcarVisits) {
            log(Level.DEBUG, "WI Position ID\t" + containerLocation.getPosLocId() + "\t\nand RailcarVisit position\t" + visit.getLocId());
            if (containerLocation.getPosLocId().equals(visit.getLocId())) {
              railcarVisit = visit;
              log(Level.INFO, "Conatiner and RailCar Position Matches \t"
                      + visit.getLocId() + "\t so\t" + railcarVisit + "\t is considered for calculation");
              break;
            }
          }
        }
      }
      String platformTransferPoints = null;
      if (railcarVisit != null) {
        platformTransferPoints = railcarVisit.getRcarvPlatTransferPoints();
        log(Level.DEBUG, "TransferPoints for railcarVisit\t" + railcarVisit + "\tare\t" + platformTransferPoints);
      } else {
        log(Level.INFO, "No Rail Car Visit found for workInstrction with key" + inWorkInstruction.getWiGkey());
      }

      String optimalTzSlot = null;
      if (platformTransferPoints != null) {
        optimalTzSlot = pickStringWithMatchingSuffix(platformTransferPoints, containerLocation);
        log(Level.DEBUG, "Computed optimalTzSlot for\t" + inWorkInstruction.toString() + "\tis\t" + optimalTzSlot);
      } else {
        log(Level.INFO, "No matching TransferZoneSlots found for unit in WI with key \t" + inWorkInstruction.getWiGkey());
      }

      if (optimalTzSlot != null) {
        updateUfv(ufv, optimalTzSlot);
      } else {
        log(Level.ERROR, "No Optimal TZSlot Found for WorkInstruction with Gkey" + inWorkInstruction.getWiGkey());
      }
    } else {
      log(Level.ERROR, "WorkInstruction with Gkey " + inWorkInstruction.getWiGkey() +
              "has no not considered for finding OptimalTZSlot as it has slot as " + inWorkInstruction.getWiUfv().getUfvOptimalRailTZSlot());
    }
  }

  public List<WorkInstruction> getWorkInstructionList() {
    return WorkInstruction.findWiForRailLoadDispatch();
  }

  private void updateUfv(UnitFacilityVisit inUnitFacilityVisit, String inOptimalTzSlot) {
    inUnitFacilityVisit.updateUfvOptimalRailTZSlot(inOptimalTzSlot);
  }

/* private void updateWiPreferredTransferLoc(WorkInstruction inWorkInstruction, String inOptimalTzSlot){
    inWorkInstruction.
  }*/

  private String pickStringWithMatchingSuffix(String inPlatformTransferPoints, @Nullable LocPosition inContainerLocation) {
    Pattern pattern = Pattern.compile(",");
    String[] tzSlots = pattern.split(inPlatformTransferPoints);
    String containerLocationId = inContainerLocation != null ? inContainerLocation.getPosLocId() : null;
    for (String slot : tzSlots) {
      if (containerLocationId != null && isSuffixMatch(slot, containerLocationId)) {
        return slot;
      }
    }
    return tzSlots[0];
  }

  @SuppressWarnings("ConstantConditions")
  private RailcarVisit getInboundRailcarVisit(UnitFacilityVisit inUfv) {
    log(Level.DEBUG, "inUfv.getInboundCarrierVisit()  \t\t" + inUfv.getInboundCarrierVisit());
    log(Level.DEBUG, "inUfv.getUfvActualIbCv() \t\t" + inUfv.getUfvActualIbCv());
    if (inUfv.getInboundCarrierVisit() != null && inUfv.getInboundCarrierVisit().getCvGkey() != null) {
      return RailcarVisit.hydrate(inUfv.getInboundCarrierVisit().getCvCvd().getCvdGkey());
    } else if (inUfv.getUfvActualIbCv() != null && inUfv.getUfvActualIbCv().getCvGkey() != null) {
      return RailcarVisit.hydrate(inUfv.getUfvActualIbCv().getCvCvd().getCvdGkey());
    } else {
      return null;
    }
  }

  @Nullable
  private TrainVisitDetails getInboundTrainVisitDetails(UnitFacilityVisit inUfv) {
    log(Level.DEBUG, "inUfv.getInboundCarrierVisit()\t\t" + inUfv.getInboundCarrierVisit());
    log(Level.DEBUG, "inUfv.getUfvActualIbCv()\t\t" + inUfv.getUfvActualIbCv());
    CarrierVisit carrierVisit = inUfv.getInboundCarrierVisit() == null ? null : inUfv.getInboundCarrierVisit();
    log(Level.DEBUG, "carrierVisit.isGenericCv()\t\t" + carrierVisit.isGenericCv());
    if (carrierVisit != null && !carrierVisit.isGenericCv()) {
      return TrainVisitDetails.resolveTvdFromCv(carrierVisit);
    } else if (inUfv.getUfvActualIbCv() != null && inUfv.getUfvActualIbCv().getCvGkey() != null) {
      return TrainVisitDetails.hydrate(inUfv.getUfvActualIbCv().getCvCvd().getCvdGkey());
    } else {
      return null;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private RailcarVisit getOutBoundRailcarVisit(UnitFacilityVisit inUfv) {
    log(Level.DEBUG, "inUfv.getUfvObCv()\t\t" + inUfv.getUfvObCv());
    if (inUfv.getUfvObCv() != null && inUfv.getUfvObCv().getCvGkey() != null) {
      return RailcarVisit.hydrate(inUfv.getInboundCarrierVisit().getCvCvd().getCvdGkey());
    } else if (inUfv.getUfvActualObCv() != null && inUfv.getUfvActualIbCv().getCvGkey() != null) {
      return RailcarVisit.hydrate(inUfv.getUfvActualIbCv().getCvCvd().getCvdGkey());
    } else {
      return null;
    }
  }

  @Nullable
  private RailcarVisit getRailcarVisit(@Nullable LocPosition inContainerLocation) {
    RailcarVisit rcv = null;
    Serializable locGkey = inContainerLocation != null ? inContainerLocation.getPosLocGkey() : null;
    if (locGkey != null) {
      rcv = RailcarVisit.hydrate(locGkey);
    }
    return rcv;
  }

  @SuppressWarnings("ConstantConditions")
  private TrainVisitDetails getOutBoundTrainVisitDetails(UnitFacilityVisit inUfv) {
    log(Level.DEBUG, "inUfv.getUfvObCv() \t\t" + inUfv.getUfvObCv());
    log(Level.DEBUG, "inUfv.getUfvActualObCv() \t\t" + inUfv.getUfvActualObCv());
    if (inUfv.getUfvObCv() != null && inUfv.getUfvObCv().getCvGkey() != null) {
      return TrainVisitDetails.hydrate(inUfv.getUfvObCv().getCvCvd().getCvdGkey());
    } else if (inUfv.getUfvActualObCv() != null && inUfv.getUfvActualIbCv().getCvGkey() != null) {
      return TrainVisitDetails.hydrate(inUfv.getUfvActualIbCv().getCvCvd().getCvdGkey());
    } else {
      return null;
    }
  }

  private static boolean isSuffixMatch(String inSlot, String inContainerLocationId) {
    boolean exactmatch = inSlot.replaceAll("[^\\w\\s\\.]", "").equals(inContainerLocationId.replaceAll("[^\\w\\s\\.]", ""));
    if (exactmatch) {
      return true;
    } else {
      inSlot = inSlot.replaceAll("[^\\w\\s\\.]", "");
      inContainerLocationId = inContainerLocationId.replaceAll("[^\\w\\s\\.]", "");
    }
    return false;
  }
}
