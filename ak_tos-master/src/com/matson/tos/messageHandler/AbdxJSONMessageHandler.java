package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.json.HAZMAT;
import com.matson.tos.json.jsonToSnx.HazardJsonToSNX;
import com.matson.tos.util.TosRefDataUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Keerthi Ramachandran
 * @since 6/25/2015
 * <p>AbdxJSONMessageHandler is for handling Hazardous message from HAZMAT</p>
 */
public class AbdxJSONMessageHandler extends AbstractJSONMessageHandler {
    public static final String SEPERATOR = "/";
    //private static final String BASE_URI = "https://dev.hazmat.matson.com/hazmat/search/booking";//todo, kramachandran move base URI to an common file
    private static Logger logger = Logger.getLogger(AbdxJSONMessageHandler.class);
    private String facility = null;

    @Override
    public String getDestinationBaseURL() {
        return TosRefDataUtil.getValue("HAZMAT_URL");
    }

    public List<Snx> getSNXMessage(String inBillNo, String inContainerNo) throws TosException {
        /**
         * The block has to be removed, it's an workaround for self-signed certificate
         */
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            ;
        }
        /**
         * self-signed certificate workaround ends here
         */
        logger.debug("Input paramters are \t BillNo : " + inBillNo + " Container No : "+inContainerNo);
        if (inBillNo == null && inContainerNo == null) {
            throw new TosException("Input paramters cannot be null \t BillNo : " + inBillNo + " Container No : "+inContainerNo);
        }
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(JacksonJaxbJsonProvider.class);
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        //for usage with TDP app, add an method to construct the URI
        //psethuraman : Changes to retrieve HAZMAT details for container
        WebResource resource = null;

        if (inContainerNo != null) {
            resource = client.resource(getDestinationBaseURL() + "/hazmat/search/lclcontainer" + SEPERATOR + inContainerNo);
        } else if (inBillNo != null) {
            resource = client.resource(getDestinationBaseURL() + "/hazmat/search/booking" + SEPERATOR + inBillNo /*+ SEPERATOR + inConatinerNo*/);
        }
        ClientResponse response = resource != null ? resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class) : null;
        if (response == null || response.getStatus() != 200) {
            logger.error("Request failed");
            logger.error(response.toString());
            throw new TosException("JSON response failed" + response.toString());

        } else {
            logger.info("Got Response from HAZMAT : "+response.toString());
            String hazmatString = response.getEntity(String.class);
            //hazmatString = "[{\"associationId\":39,\"bookingNumber\":\"2952474\",\"containerNumber\":\"MATU2469097\",\"alfrescoDocId\":null,\"isActive\":\"Y\",\"createUser\":\"hazmat\",\"createDate\":\"2016-04-29 04:30:08\",\"lastUpdateUser\":\"hazmat\",\"lastUpdateDate\":\"2016-04-29 04:30:08\",\"unitId\":11515555,\"hazardousCommodityLines\":[{\"commodityLineId\":96,\"hazUniqueId\":\"17193\",\"hazType\":\"UN\",\"hazNumber\":\"1165\",\"hazCommodityName\":\"DIOXANE\",\"hazPrimaryClass\":\"3\",\"hazPrimaryClassName\":null,\"hazSecondaryClass\":\"\",\"hazTertiaryClass\":\"\",\"hazEmergencyContactName\":\"\",\"hazEmergencyContactPhone\":\"\",\"hazSecondaryEmergencyContactName\":\"\",\"hazSecondaryEmergencyContactPhone\":\"\",\"hazPackageGroup\":2,\"hazPieces\":1,\"hazPiecesUomCode\":\"BRL\",\"hazWeight\":135.000000,\"hazWeightUomCode\":\"KGS\",\"hazFlashPoint\":\"135\",\"hazFlashPointUomCode\":\"F\",\"hazImdgCfrIndicator\":\"CFR\",\"hazLimitedQuantity\":\"0\",\"hazMarinePollutant\":\"0\",\"hazExplosivePowderWeight\":null,\"hazExplosivePowderWeightUomCode\":\"\",\"hazSpecialPermitNumber\":null,\"isActive\":\"Y\",\"createUser\":\"hazmat\",\"createDate\":\"2016-04-29 21:14:39\",\"lastUpdateUser\":\"hazmat\",\"lastUpdateDate\":\"2016-04-29 21:14:39\",\"notes\":\"\",\"moreThan50PercentFlag\":null,\"stowageRestriction\":null,\"explosivePowderWeightApplicable\":null}]}]";
            List<HAZMAT> hazmatList = null;
            try {
                hazmatList = new ObjectMapper().readValue(hazmatString, new TypeReference<List<HAZMAT>>() {
                });
            } catch (IOException e) {
                logger.error("Error thrown while parsing JSON to Object mapping : "+hazmatString);
                e.printStackTrace();
            }
            
            if(hazmatList!=null){
                logger.debug("Haz List Generated from Response : "+hazmatList.toString());
            }
            
            List<Snx> snxList = new ArrayList<Snx>();
            Snx output = new Snx();
            for (HAZMAT hazmat : hazmatList) {
                HazardJsonToSNX jsonToSNX = new HazardJsonToSNX(hazmat.getContainerNumber(), hazmat.getBookingNumber());
                if (inBillNo != null) {
                    output = new Snx();
                    output =jsonToSNX.getSNXObject(hazmat);
                    snxList.add(output);
                } else if (inContainerNo != null){
                    output = jsonToSNX.getUnitSNXObject(hazmat, getFacility(), output);
                }
            }
            if (inContainerNo != null){
                snxList.add(output);
            }
            return snxList;
        }
    }

    public List<Snx> getSNXMessage(String inBillNo, String inContainerNo,
                                   String facility) throws TosException {
        setFacility(facility);
        logger.info("Setting the facility for Haz Refresh Request : "+facility);
        return getSNXMessage(inBillNo, inContainerNo);
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }
}
