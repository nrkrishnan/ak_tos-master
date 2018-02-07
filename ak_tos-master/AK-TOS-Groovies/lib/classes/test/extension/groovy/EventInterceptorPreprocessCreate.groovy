/*
 * Copyright (c) 2014 Navis LLC. All Rights Reserved.
 *
 */


package groovy

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.metafields.MetafieldId
import com.navis.framework.metafields.MetafieldIdFactory
import com.navis.services.business.event.Event


/**
 * This class sets a dynamic field in {@link com.navis.services.business.event.Event}
 * It is used by {@link com.navis.apex.persistence.DoNotRunApexUnitEventCodeExtensionPersistenceSaTestSuite} for CAR-5377.
 */
public class EventInterceptorPreprocessCreate extends AbstractEntityLifecycleInterceptor {

  @Override
  public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    updateExtractDefaultBatchId(inMoreFieldChanges);
  }

  /**
   * Updates the custom field customFlexFields.evntCustomDFFVitExtractBatchId value of each event to -999999999.
   * @param inOutParams
   */
  private void updateExtractDefaultBatchId(EFieldChanges inMoreFieldChanges) {
      // construct map of custom flex fields to be updated
      Map customFlexFields = new HashMap();
      MetafieldId extractBatchId = MetafieldIdFactory.valueOf("evntCustomDFFVitExtractBatchId");
      customFlexFields.put(extractBatchId.getFieldId(), Event.BILLING_EXTRACT_NEGATIVE_DEFAULT_VALUE);
      inMoreFieldChanges.setFieldChange(MetafieldIdFactory.valueOf("customFlexFields"), customFlexFields);
  }

}

