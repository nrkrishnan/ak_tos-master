/*
 * Copyright (c) 2012 Navis LLC. All Rights Reserved.
 *
 */

package testdata

/**
 * Created by IntelliJ IDEA.
 * User: pabbasi
 * Date: 1/19/12
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */

import com.navis.argo.business.api.IArgoEquipmentOrderManager
import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChange
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.inventory.InventoryField
import com.navis.inventory.business.units.EqBaseOrderItem

public class ValidateTallyForReturnToStorage extends AbstractEntityLifecycleInterceptor {
  @Override
  public void validateChanges(EEntityView inEntity, EFieldChangesView inFieldChanges) {
    EFieldChange change = inFieldChanges.findFieldChange(InventoryField.UE_DEPARTURE_ORDER_ITEM);
    if (change != null && change.getNewValue() == null && change.getPriorValue() != null) {
      EqBaseOrderItem eqBaseOrderItem = (EqBaseOrderItem) change.getPriorValue();
      if (eqBaseOrderItem != null) {
        IArgoEquipmentOrderManager equipOrdMgr = (IArgoEquipmentOrderManager) Roastery.getBean(IArgoEquipmentOrderManager.BEAN_ID);
        equipOrdMgr.decrementTally(eqBaseOrderItem.getEqboiGkey());
        HibernateApi.getInstance().flush();
      }
    }
  }
}
