package com.navis.apex.test

import com.navis.apex.business.model.GroovyInjectionBase
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.LocTypeEnum
import com.navis.argo.business.model.CarrierVisit
import com.navis.argo.business.reference.RoutingPoint


class XmlReeferUsingGroovy extends GroovyInjectionBase {
    public String execute(Map inParameters) {

        def reeferDoc = inParameters.get("reefer-xml-string");
        log ("got called and returning reefr xml " + reeferDoc)
        return reeferDoc;
    }
}