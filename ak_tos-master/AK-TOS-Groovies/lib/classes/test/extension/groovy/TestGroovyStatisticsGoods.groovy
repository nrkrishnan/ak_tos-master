/*
 * Copyright (c) 2011 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView
import com.navis.inventory.InventoryField
import com.navis.argo.ArgoRefField;

/**
 * Example, java class here for debugging
 */
public class TestGroovyStatisticsGoods extends AbstractEntityLifecycleInterceptor {

    @Override
    public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges,
                         EFieldChanges inMoreFieldChanges) {
        inMoreFieldChanges.setFieldChange(InventoryField.GDS_DESTINATION, "DESTINATION");
    }


    @Override
    public void validateChanges(EEntityView inEntity, EFieldChangesView inFieldChanges) {
        log("Validation called for  " + inEntity.getEntityName());

    }
}
