/*
 * Copyright (c) 2014 Navis LLC. All Rights Reserved.
 *
 */

package groovy.extensions.chatter

import com.navis.apex.business.clusternotificationservice.*
import com.navis.argo.presentation.controller.CustomExternalAppController
import com.navis.external.argo.AbstractCustomMessageHandlerExtension
import com.navis.external.argo.ICustomMessageHandlerSendChannel
import com.navis.external.util.Message
import com.navis.framework.business.Roastery
import com.navis.framework.portal.UserContext
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * This class implements the server side extension for the Chatter UI extension that provides an inside-N4 text messaging UI.
 */
class ChatterExtension extends AbstractCustomMessageHandlerExtension implements IClusterNotificationSubscriber {

  @Override
  void initialize(@NotNull CustomExternalAppController inController) {
    _controller = inController;

    registerMessageHandler(SEND_NEW_CONVERSION_SNIPPET_MESSAGE_ID, new SendConversationSnippetHandler());

    _notificationFilter = new ChatterSessionNotificationFilter();

    _notificationService = (IClusterNotificationService) Roastery.getBean(IClusterNotificationService.BEAN_ID);
    _notificationService.registerSubscriber(this);
  }

  @Override
  void close() {
    _notificationService.deregisterSubscriber(this);
  }

  public static String CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID = "ConversationSnippetNotification"; // Sent to client side when we receive notification.
  protected static String SEND_NEW_CONVERSION_SNIPPET_MESSAGE_ID = "SendNewConversationSnippetEvent";

  @Override
  String getBriefDetails() {
    return "ChatterExtension"; // todo: append session-specific identifying info
  }

  @Override
  Object getSubscriberTypeBroadcastFilter() {
    return new ChatterBroadcastFilter();
  }

  @Override
  void initialize(@NotNull ICustomMessageHandlerSendChannel inSendChannel) {
  }

  @Override
  @NotNull
  IClusterNotificationFilter getFilter() {
    return _notificationFilter;
  }

  @Override
  void processNotification(@NotNull IClusterNotification inNotification) {
    if (! (inNotification instanceof GenericClusterNotification)) {
      return;
    }

    GenericClusterNotification csNotification = (GenericClusterNotification) inNotification;
    if (! CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID.equals(csNotification.getNotificationTypeId())) {
      return;
    }

    Map<String, Object> data = csNotification.getData();

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("sourceName", csNotification.getSourceName());
    params.put("originTimestamp", csNotification.getOriginTimestamp());
    params.put("receiptTimestamp", System.currentTimeMillis());
    params.put("snippet", data.get("snippet"));
    params.put("sender", data.get("sender"));
    params.put("recipient", data.get("recipient"));
    Message m = Message.newRequest(CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID, params);

    _controller.pushMessage(m);
  }

  IClusterNotificationService getNotificationService() {
    return _notificationService;
  }

  private IClusterNotificationFilter  _notificationFilter;
  private IClusterNotificationService _notificationService;
  private CustomExternalAppController _controller;
}

class ChatterBroadcastFilter implements IClusterNotificationFilter {

  public ChatterBroadcastFilter() {
  }

  @Override
  boolean acceptsNotification(@NotNull @NotNull IClusterNotification inNotification) {
    if (! (inNotification instanceof GenericClusterNotification)) {
      return false;
    }

    GenericClusterNotification csNotification = (GenericClusterNotification) inNotification;
    if (! ChatterExtension.CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID.equals(csNotification.getNotificationTypeId())) {
      return false;
    }

    return true;
  }

  @Override
  String getBriefDetails() {
    return "ChatterBroadcastFilter";
  }
}

class ChatterSessionNotificationFilter implements IClusterNotificationFilter {

  @Override
  boolean acceptsNotification(@NotNull @NotNull IClusterNotification inNotification) {
    if (! (inNotification instanceof GenericClusterNotification)) {
      return false;
    }

    GenericClusterNotification csNotification = (GenericClusterNotification) inNotification;
    if (! ChatterExtension.CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID.equals(csNotification.getNotificationTypeId())) {
      return false;
    }

    // TODO: filter based on current user being recipient, or wildcard

    return true;
  }

  @Override
  String getBriefDetails() {
    return "ChatterSessionNotificationFilter";
  }
}

class SendConversationSnippetHandler extends AbstractCustomMessageHandlerExtension.AbstractMessageHandler {

  @Override
  @NotNull
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    return new SendConversationSnippetHandler(inParentMessageHandler, inRequest);
  }

  public SendConversationSnippetHandler() {
    super();
  }

  @Override
  AbstractCustomMessageHandlerExtension.AbstractMessageHandler factory(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler,
                                                                       @NotNull UserContext inUserContext, @NotNull Message inRequest) {
    return null
  }

  public SendConversationSnippetHandler(@NotNull AbstractCustomMessageHandlerExtension inParentMessageHandler, @NotNull Message inRequest) {
    super(inParentMessageHandler, inRequest);
  }

  @Override
  @Nullable
  public Message handleMessage() {
    Map<String, Object> data = new HashMap<>();
    data.put("snippet", _request.getParameters().get("snippet"));
    data.put("sender", _request.getParameters().get("sender"));
    data.put("recipient", _request.getParameters().get("recipient"));

    GenericClusterNotification notification = new GenericClusterNotification("ChatterExtension",
            ChatterExtension.CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID, ChatterExtension.CONVERSATION_SNIPPET_NOTIFICATION_EVENT_ID, data);
    ((ChatterExtension) _parentMessageHandler).getNotificationService().broadcastNotification(notification);

    return null; // There is no response; we are handling a response, not returning one.
  }

  @Override
  protected void doInTransaction() {
    // n/a - Work occurs in handleMessage, with no transaction involved. Will not be called.
  }

}
