package com.matson.tos.nascent.webservice;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordScanClient {

    private static Logger logger = Logger.getLogger(RecordScanClient.class);

    public void argoServicePortGenericInvoke(String laneId) throws Exception {
        logger.info("argoServicePortGenericInvoke:: input laneId " +laneId);

        try {

            com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub binding;
            try {
                binding = (com.matson.tos.nascent.webservice.ArgoServiceSoapBindingStub)
                        new com.matson.tos.nascent.webservice.ArgoServiceLocator().getArgoServicePort();
            }
            catch (javax.xml.rpc.ServiceException jre) {
                if(jre.getLinkedCause()!=null)
                    jre.getLinkedCause().printStackTrace();
                throw new Exception("JAX-RPC ServiceException caught: " + jre);
            }


            // Time out after a minute
            binding.setTimeout(60000);

            binding.setUsername("n4api");
            binding.setPassword("lookitup");

            com.matson.tos.nascent.webservice.ScopeCoordinateIdsWsType scopeCoordinateIdsWsType=new com.matson.tos.nascent.webservice.ScopeCoordinateIdsWsType();
            scopeCoordinateIdsWsType.setOperatorId("MATSON");
            scopeCoordinateIdsWsType.setComplexId("ALASKA");
            scopeCoordinateIdsWsType.setFacilityId("ANK");
            scopeCoordinateIdsWsType.setYardId("ANK");

            String requestStr = buildWebServiceRequestString(laneId);

            logger.info("web service request : " + requestStr);

            com.matson.tos.nascent.webservice.GenericInvokeResponseWsType responseWsType = null;
            responseWsType = binding.genericInvoke(scopeCoordinateIdsWsType, requestStr);

            if (responseWsType != null) {
                logger.info("Argo Service Response payload: " + responseWsType.getResponsePayLoad());
                ResponseType response = responseWsType.getCommonResponse();
                if (response != null) {
                    QueryResultType[] results = response.getQueryResults();
                    logger.info("Argo Service Response payload111: " + results[0].getResult());
                }
            }

        } catch (Exception ex) {
            logger.info( "Cannot invokeWebService due to: " + ex.toString());
        }
    }


    /**
     * Builds the request to call the 'MatsonAncGateWebServiceCall'
     *
     * @param inXml Xml
     * @return request string
     */
    private static String buildWebServiceRequestString(String laneId) {
        String grovyClassName = "MatsonAncGateWebServiceCall";

        java.util.Date date= new java.util.Date();
        String recordScanTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(date.getTime()));
        recordScanTimeStamp = recordScanTimeStamp.replace(" ", "T");
        long unixTime = System.currentTimeMillis() / 1000L;

        logger.info("current timestamp " + recordScanTimeStamp + " unixTime : " + unixTime);
        String requestStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gate>" +
                        "<record-scan scan-set-id=\""+unixTime+"\">" +
                        "<gate-id>ANK GATE</gate-id>" +
                        "<stage-id>gate</stage-id>" +
                        "<lane-id>LANE "+laneId+"</lane-id>" +
                        //"<timestamp>2015-08-07T01:04:22</timestamp>" +
                        "<timestamp>" + recordScanTimeStamp + "</timestamp>" +
                        "</record-scan></gate>";

        String escapedXml = StringEscapeUtils.escapeXml(requestStr);

        StringBuilder requestString = new StringBuilder();

        requestString.append("<groovy class-name=\"" + grovyClassName + "\" class-location=\"code-extension\">")
                .append("\n")
                .append("<parameters>")
                .append("\n")
                .append("<parameter id=\"xml-request\" value=\"" + escapedXml + "\"/>")
                .append("\n")
                .append("</parameters>")
                .append("\n")
                .append("</groovy>");

        return requestString.toString();
    }
}
