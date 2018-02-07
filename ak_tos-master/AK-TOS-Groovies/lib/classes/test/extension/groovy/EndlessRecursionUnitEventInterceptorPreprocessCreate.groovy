/*
 * Copyright (c) 2014 Navis LLC. All Rights Reserved.
 *
 */

package groovy

import com.navis.argo.business.api.IEventType
import com.navis.argo.business.api.ServicesManager
import com.navis.argo.business.atoms.EventEnum
import com.navis.argo.business.atoms.LogicalEntityEnum
import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor
import com.navis.external.framework.entity.EEntityView
import com.navis.external.framework.util.EFieldChanges
import com.navis.external.framework.util.EFieldChangesView
import com.navis.framework.business.Roastery
import com.navis.framework.persistence.HibernateApi
import com.navis.framework.portal.context.UserContextUtils
import com.navis.services.ServicesField
import com.navis.services.business.event.Event
import com.navis.services.business.event.EventHbr

import java.lang.reflect.Method


/**
 * It is used by {@link com.navis.apex.persistence.DoNotRunApexUnitEventCodeExtensionPersistenceSaTestSuite} for CAR-5377.
 * It is supposed to be configured for {@link com.navis.services.business.event.Event}.
 * It will cause endless recursion ({@link java.lang.StackOverflowError},
 * since it creates a new event during the interception phase triggered by the creation of a new event.
 */
public class EndlessRecursionUnitEventInterceptorPreprocessCreate extends AbstractEntityLifecycleInterceptor {

  @Override
  public void onCreate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges) {
    createNewServiceEvent();
  }

  /**
   * Creates and saves a new {@link com.navis.services.business.event.Event}.
   */
  private void createNewServiceEvent() {
    log("EndlessRecursionUnitEventInterceptorPreprocessCreate.createNewServiceEvent() execution - START ! ");
    Event event = new Event();
    event.setPrimaryKey(System.currentTimeMillis());
    final EventEnum eventEnum = EventEnum.UNIT_SEAL;
    ServicesManager servicesManager = (ServicesManager) Roastery.getBean(ServicesManager.BEAN_ID);
    IEventType eventType = servicesManager.getEventType(eventEnum.getKey());
    event.setFieldValue(ServicesField.EVNT_EVENT_TYPE, eventType);
    // those setters are protected, but have a not-null contraint in the database => we have to call them using reflection
    setValue(event, SET_EVNT_APPLIED_BY_METHOD_NAME, String.class, UserContextUtils.getSystemUserContext().getUserId());
    setValue(event, SET_EVNT_APPLIED_TO_CLASS_METHOD_NAME, LogicalEntityEnum.class, LogicalEntityEnum.UNIT);
    setValue(event, SET_EVNT_APPLIED_TO_PRIMARY_KEY_METHOD_NAME, Long.class, -1L);
    setValue(event, SET_EVNT_APPLIED_TO_NATURAL_KEY_METHOD_NAME, String.class, "Value for EvntAppliedToNaturalKey");
    HibernateApi.getInstance().save(event);
  }

  private static void setValue(final Event inEvent, final String inMethodName, final Class inArgType, final Object inValue) {
    // Apparently getDelaredMethod() does not work correctly in Groovy => we have to specify EventHbr as class to make it succeed!
    Method method = EventHbr.class.getDeclaredMethod(inMethodName, inArgType);
    method.setAccessible(true);
    method.invoke(inEvent, inValue);
  }

  private static final String SET_EVNT_APPLIED_BY_METHOD_NAME = "setEvntAppliedBy"
  private static final String SET_EVNT_APPLIED_TO_CLASS_METHOD_NAME = "setEvntAppliedToClass"
  private static final String SET_EVNT_APPLIED_TO_PRIMARY_KEY_METHOD_NAME = "setEvntAppliedToPrimaryKey"
  private static final String SET_EVNT_APPLIED_TO_NATURAL_KEY_METHOD_NAME = "setEvntAppliedToNaturalKey"

}

