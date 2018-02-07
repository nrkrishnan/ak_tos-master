package system
/*
 * Copyright (c) 2014 Navis LLC. All Rights Reserved.
 *
 */



import com.navis.external.services.AbstractGeneralNoticeCodeExtension
import com.navis.services.business.event.GroovyEvent

/**
 * Created with IntelliJ IDEA.
 * User: perumsu
 * Date: 3/1/14
 * Time: 10:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class HostEventExporter extends AbstractGeneralNoticeCodeExtension {
  public void execute(GroovyEvent inGroovyEvent) {
    _hostEvntExporterHandlerCodeExtInstance = getLibrary(HOST_EVENT_EXPORTER_HANDLER);
    _hostEvntExporterHandlerCodeExtInstance.execute(inGroovyEvent);
  }

  public final String HOST_EVENT_EXPORTER_HANDLER = "HostEventExporterHandler"
  def _hostEvntExporterHandlerCodeExtInstance = null;
}
