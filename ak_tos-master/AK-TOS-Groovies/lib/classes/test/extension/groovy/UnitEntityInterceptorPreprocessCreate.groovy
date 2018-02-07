/*
* Copyright (c) 2007 Navis LLC. All Rights Reserved.
* $Id: $
*/

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView;

/**
 * Example, java class here for debugging
 */
public class UnitEntityInterceptorPreprocessCreate extends AbstractEntityLifecycleInterceptor {

    @Override
    public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges,
                         EFieldChanges inMoreFieldChanges) {
        if (inOriginalFieldChanges.hasFieldChange(InventoryField.UNIT_NEEDS_REVIEW)) {
            inMoreFieldChanges.setFieldChange(InventoryField.UNIT_FLEX_STRING01,
                    inOriginalFieldChanges.findFieldChange(InventoryField.UNIT_ID).getNewValue()
                            + " needs review");

            // add another field
            inMoreFieldChanges.setFieldChange(SandboxField.FOO_INT, Long.valueOf(999));
        }
    }


    @Override
    public void validateChanges(EEntityView inEntity, EFieldChangesView inFieldChanges) {
        log("Validation called for  " + inEntity.getEntityName());

    }
}
