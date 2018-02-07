/**
 * Copyright (c) 2009 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */

import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChange;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView;
import com.navis.framework.metafields.MetafieldId;
import com.navis.framework.metafields.MetafieldIdFactory;
import com.navis.argo.business.atoms.FreightKindEnum;
import com.navis.inventory.InventoryField;
import com.navis.services.business.rules.EventType;
import com.navis.framework.portal.FieldChanges;
import com.navis.argo.business.api.ServicesManager;
import com.navis.inventory.business.api.UnitField;
import com.navis.framework.business.Roastery;
import com.navis.framework.portal.FieldChange;

/**
 * This is a simple demo code using the new Entity life cycle interceptor based groovy code.
 * The major difference between this approach and Notices based approach is that this is called as part of the business transaction.
 * While Notice based groovy is called after the business transaction is over.
 * Records an event specific to the flex field. N4 doesnt currently support flex field specific event as part of the business transaction.
 * Note: The following code will only work if the flex fields are appropriately define. This one expects UFV_FLEX_STRING06 to be predefined
 * and also a custom event called AANKOOP_CHANGED to have been defined.
 * @author rkhawaja
 *
 */
public class EventRecordingForFlexFieldChanges extends AbstractEntityLifecycleInterceptor {

  @Override
  public void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    // Rafay Quick Prototype, can be made cleaner by using a Map to lookup events based on flex field.
    if (inOriginalFieldChanges.hasFieldChange(UnitField.UFV_FLEX_STRING06)) {
      try {
        EFieldChange eFC = inOriginalFieldChanges.findFieldChange(UnitField.UFV_FLEX_STRING06);

        FieldChanges eventFieldChanges = new FieldChanges();
        FieldChange flexFieldChange = new FieldChange(eFC.getMetafieldId(), eFC.getPriorValue(), eFC.getNewValue() )
        eventFieldChanges.setFieldChange(flexFieldChange);
        ServicesManager sm = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
        String eventId = "AANKOOP_CHANGED";
        EventType eventType = EventType.findEventType(eventId);
        if (eventType == null) {
          log("postNewEvent: Unknown EventType in script: " + eventId);
        } else {
          def unit = inEntity.getField(MetafieldIdFactory.valueOf("ufvUnit"))
          sm.recordEvent(eventType, eventId, null, null, unit, eventFieldChanges);
        }
      } catch (Exception inBizViolation) {
        log("failed to update ufv flex string 06 ");
      }
    }

  }
}