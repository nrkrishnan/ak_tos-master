/*
 * Copyright (c) 2011 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView
import com.navis.inventory.InventoryField
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.QueryUtils
import com.navis.inventory.InventoryEntity
import com.navis.framework.persistence.HibernateApi;

/**
 * Example, java class here for debugging
 */
public class TestGroovyStatisticsUnit extends AbstractEntityLifecycleInterceptor {

  @Override
  public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges,
                       EFieldChanges inMoreFieldChanges) {
    if (inOriginalFieldChanges.hasFieldChange(InventoryField.UNIT_NEEDS_REVIEW)) {
      inMoreFieldChanges.setFieldChange(InventoryField.UNIT_FLEX_STRING01,
              inOriginalFieldChanges.findFieldChange(InventoryField.UNIT_ID).getNewValue()
                      + " needs review");

      // add another field
      inMoreFieldChanges.setFieldChange(InventoryField.UNIT_GOODS_AND_CTR_WT_KG, Double.valueOf(9999));
    }
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT);
    HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  @Override
  public void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    DomainQuery dq = QueryUtils.createDomainQuery(InventoryEntity.UNIT);
    HibernateApi.getInstance().findEntitiesByDomainQuery(dq);
  }

  @Override
  public void validateChanges(EEntityView inEntity, EFieldChangesView inFieldChanges) {
    log("Validation called for  " + inEntity.getEntityName());

  }
}
