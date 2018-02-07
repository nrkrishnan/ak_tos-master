/*
 * Copyright (c) 2014 Navis LLC. All Rights Reserved.
 *
 */


package groovy

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.inventory.InventoryField


/**
 * This class sets field "unitFlexString15" in {@link com.navis.inventory.business.units.Unit} as proof, that it was called.
 * It is used by {@link com.navis.apex.persistence.DoNotRunApexBookingRollsUnitCodeExtensionPersistenceSaTestSuite} for ARGO-49183.
 * TODO: This is the x-th time, that I need a Groovy interceptor just for checking, that it was called.
 * It would be nice and save redundant code to have a single Groovy class, which is configurable by the client in the way, which changes it performs.
 * This would make it possible for tests to set the Groovy and check the entity later, if the change was performed.
 * This is the proof, that the code extension was called.
 */
public class UnitInterceptorPreprocessCreate extends AbstractEntityLifecycleInterceptor {

  @Override
  public void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    inMoreFieldChanges.setFieldChange(InventoryField.UNIT_FLEX_STRING15, "ARGO-49183");
  }

}

