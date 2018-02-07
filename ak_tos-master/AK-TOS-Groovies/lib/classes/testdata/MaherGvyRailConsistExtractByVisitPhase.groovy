package com.navis.external.examples

import com.navis.argo.business.api.GroovyApi
import com.navis.rail.business.entity.TrainVisitDetails
import com.navis.edi.business.entity.EdiSession
import com.navis.edi.business.api.EdiExtractManager
import com.navis.framework.business.Roastery
import com.navis.services.business.event.GroovyEvent
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum

/**
 * Created by IntelliJ IDEA.
 * User: skumaravel
 * Date: Mar 11, 2010
 * Time: 4:32:49 PM
 * To change this template use File | Settings | File Templates.
 */

public class MaherGvyRailConsistExtractByVisitPhase {

  public void extractRailConsist(Object event, Object api){

    GroovyEvent gv = (GroovyEvent) event;
    TrainVisitDetails tvDetail = event.entity;
    if (CarrierVisitPhaseEnum.COMPLETE.equals(tvDetail.getCvdCv().getCvVisitPhase())){
    if ( tvDetail.getCvdCv().getCvOperator() != null) {
     String trainOperator = tvDetail.getCvdCv().getCvOperator().getBzuId();
     System.out.println("****** operator Id *** : " + trainOperator);
     String sessionName = "";
     if ("CSX".equals(trainOperator)){
     sessionName = "CSXT_SEND_418";
     }else if ("NS".equals(trainOperator)){
     sessionName = "NS_SEND_418";
     }
      EdiSession session = EdiSession.findEdiSession(sessionName);
      if (session != null){
        EdiExtractManager extractMgr = (EdiExtractManager)Roastery.getBean(EdiExtractManager.BEAN_ID); extractMgr.extractEdiSession(null,session.getEdisessGkey(),tvDetail.getCvdCv().getCvGkey(), null);
      }
     }
    }
  }
}