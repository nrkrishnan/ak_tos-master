/*
 * Copyright (c) 2010 Zebra Technologies Corp. All Rights Reserved.
 * $Id: $
 */

import com.navis.road.business.model.GateConfiguration
import com.navis.road.business.util.RoadBizUtil
import com.navis.apex.business.model.GroovyInjectionBase

class LoadGateConfigGroovy extends GroovyInjectionBase {
  public String execute(Map inParameters) {
    String xmlConfig = RoadBizUtil.convertXMLFileToString("testdata/TcIntegrationGateCfg.xml")
    if (xmlConfig == null) {
      return "Failed to load Gate Configuration from TcIntegrationGateConfig.xml"
    }
    GateConfiguration.createOrUpdateGateConfiguration(xmlConfig);
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?> <result status=\"OK\"/>";
  }
}