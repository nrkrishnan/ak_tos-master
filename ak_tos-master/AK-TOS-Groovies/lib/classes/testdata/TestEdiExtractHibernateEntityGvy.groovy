/*
 * Copyright (c) 2012 Navis LLC. All Rights Reserved.
 *
 */

package testdata

/**
 * Created with IntelliJ IDEA.
 * User: bakthba
 * Date: 12/11/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
/*
 * Copyright (c) 2012 Navis LLC. All Rights Reserved.
 *
 */

import com.navis.argo.util.XmlUtil
import com.navis.external.edi.entity.AbstractEdiExtractInterceptor
import com.navis.framework.persistence.HibernateApi
import com.navis.inventory.business.units.Unit
import com.navis.services.business.event.Event
import org.jdom.Attribute
import org.jdom.Element

/**
 * Groovy class to extract a field which is unavailable in our Activity extractor
 */
public class TestEdiExtractHibernateEntityGvy extends AbstractEdiExtractInterceptor {

  @Override
  public Element beforeEdiMap(Map inParams) {
    Element tranElement = (Element) inParams.get("XML_TRANSACTION");
    Event event = (Event) inParams.get("ENTITY");
    Unit unit = (Unit) HibernateApi.getInstance().load(Unit.class, event.getEvntAppliedToPrimaryKey());
    Element ediContainer = tranElement.getChild("ediContainer", XmlUtil.ARGO_NAMESPACE);
    String notes = unit.getUnitRemark();
    if (notes != null) {
      Attribute attribute = new Attribute("unitNotes", notes, XmlUtil.ARGO_NAMESPACE);
      ediContainer.setAttribute(attribute);
    }
    return tranElement;
  }
}