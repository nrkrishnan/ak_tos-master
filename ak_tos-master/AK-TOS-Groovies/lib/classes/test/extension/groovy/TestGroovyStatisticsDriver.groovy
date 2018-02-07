/*
 * Copyright (c) 2011 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */

package test.extension.groovy


import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView
import com.navis.inventory.InventoryField
import com.navis.road.RoadField;

/**
 * Example, java class here for debugging
 */
public class TestGroovyStatisticsDriver extends AbstractEntityLifecycleInterceptor {

    @Override
    public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges,
                         EFieldChanges inMoreFieldChanges) {
        if (inOriginalFieldChanges.hasFieldChange(RoadField.DRIVER_BAT_NBR)) {
            inMoreFieldChanges.setFieldChange(RoadField.DRIVER_FLEX_STRING01, "T1");
            inMoreFieldChanges.setFieldChange(RoadField.DRIVER_FLEX_STRING02, "T2");
            inMoreFieldChanges.setFieldChange(RoadField.DRIVER_FLEX_STRING03, "T3");
        }
    }


    @Override
    public void validateChanges(EEntityView inEntity, EFieldChangesView inFieldChanges) {
        log("Validation called for  " + inEntity.getEntityName());

    }
}
