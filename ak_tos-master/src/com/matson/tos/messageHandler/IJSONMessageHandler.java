package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.Snx;

/**
 * @author Keerthi Ramachandran
 * @since 6/25/2015
 * <p>IJSONMessageHandler common set of methods used for JSON processing</p>
 */
public interface IJSONMessageHandler {
    String getDestinationBaseURL();

    String getXMLStringFromSnxObject(Snx inSnx) throws TosException;
}
