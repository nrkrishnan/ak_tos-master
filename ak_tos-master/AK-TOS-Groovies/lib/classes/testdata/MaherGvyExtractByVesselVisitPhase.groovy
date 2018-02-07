package com.navis.external.examples

import com.navis.edi.business.entity.EdiSession
import com.navis.edi.business.api.EdiExtractManager
import com.navis.framework.business.Roastery
import com.navis.services.business.event.GroovyEvent
import com.navis.argo.business.atoms.CarrierVisitPhaseEnum
import com.navis.vessel.business.schedule.VesselVisitDetails
import com.navis.vessel.business.schedule.VesselVisitLine

/**
 * Created by IntelliJ IDEA.
 * User: skumaravel
 * Date: Mar 11, 2010
 * Time: 4:32:49 PM
 * To change this template use File | Settings | File Templates.
 */

public class MaherGvyExtractByVesselVisitPhase {

  public void extractEdi(Object event, Object api){

    String OPERATOR_ID = "EVG UK";

    GroovyEvent gv = (GroovyEvent) event;
    VesselVisitDetails vvd = event.entity;

    if (CarrierVisitPhaseEnum.DEPARTED.equals(vvd.getCvdCv().getCvVisitPhase())){

    if ( vvd.getCvdCv().getCvOperator() != null) {
     String vesselOperator = vvd.getCvdCv().getCvOperator().getBzuId();
     System.out.println("****** operator Id *** : " + vesselOperator);
     String sessionName = "";

     if (OPERATOR_ID.equals(vesselOperator)){
     sessionName = "ACTIV_SESS";
     }else{
       Set vvLineSet =  vvd.getVvdVvlineSet();
       if (vvLineSet != null){
         Iterator vvLineIterator = vvLineSet.iterator();
         for ( ; vvLineIterator.hasNext() ; ){
           VesselVisitLine vvLine =  (VesselVisitLine)vvLineIterator.next();
           if (OPERATOR_ID.equals(vvLine.getVvlineBizu().getBzuId())){
             sessionName = "ACTIV_SESS";
           }
         }
       }
     }
      EdiSession session = EdiSession.findEdiSession(sessionName);
      if (session != null){
        System.out.println("sessionName is : " + sessionName);
        EdiExtractManager extractMgr = (EdiExtractManager)Roastery.getBean(EdiExtractManager.BEAN_ID); extractMgr.extractEdiSession(null,session.getEdisessGkey(),vvd.getCvdCv().getCvGkey(), null);
      }
     }
    }
  }
}