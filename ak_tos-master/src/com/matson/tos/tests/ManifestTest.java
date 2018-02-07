package com.matson.tos.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;
import org.tempuri.Invicta.ManifestSvc.ManifestSvcLocator;
import org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap;
import org.tempuri.Invicta.ManifestSvc.ManifestWSResponse;
import org.tempuri.Invicta.ManifestSvc.StatusEnum;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest;

import com.matson.tos.processor.MdbManifestMessageProcessor;
import com.matson.tos.util.ManifestHelper;
import com.matson.vessel.Itinerary;
import com.matson.vessel.ItineraryImpl;
import com.matson.vessel.vo.ItineraryLeg;
import com.matson.vessel.vo.Trip;
import com.matson.vessel.vo.TripArrivalComparator;
import com.matson.vinsight.webservice.VINSightHDOAWebServiceLocator;
import com.matson.vinsight.webservice.VINSightHDOAWebServicePort;
import com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity;

public class ManifestTest extends TestCase {

	public void testProcessMsg() throws Exception {
		MdbManifestMessageProcessor manifestMDB = new MdbManifestMessageProcessor();
		FileReader inputFile = new FileReader(
				"/src/xml/RJP339.xml");
		BufferedReader inputBuffer = new BufferedReader(inputFile);
		StringBuffer inputText = new StringBuffer();
		String input = null;
		while ((input = inputBuffer.readLine()) != null) {
			inputText.append(input);
		}
		inputBuffer.close();
		inputFile.close();
		manifestMDB.processMsg(inputText.toString());
		// fail("Not yet implemented");
	}

	private void callService(ManifestWSRequest req) throws ServiceException,
			RemoteException, javax.xml.soap.SOAPException {
		ManifestHelper manifestHelper = new ManifestHelper();
		ManifestSvcLocator service = new ManifestSvcLocator();
		SOAPHeaderElement header = new SOAPHeaderElement(
				ManifestHelper.AG_NAMESPACE, ManifestHelper.AG_HEADER_TAG);
		javax.xml.soap.SOAPElement nodeUser = header
				.addChildElement(ManifestHelper.AG_HEADER_TAG_ELEMENT_USER);
		nodeUser.addTextNode(manifestHelper.getUserName());
		javax.xml.soap.SOAPElement nodePassword = header
				.addChildElement(ManifestHelper.AG_HEADER_TAG_ELEMENT_PASSWD);
		nodePassword.addTextNode(manifestHelper.getPassword());

		ManifestSvcSoap soapService = service.getManifestSvcSoap();
		((Stub) soapService).setHeader(header);
		// soapService.
		ManifestWSResponse response = soapService.save(req);
		if (response.getAck().getStatus() != StatusEnum.statusImportSucceeded)
			fail("Web service failed with error message : "
					+ response.getAck().getMessage());
		System.out.print(response.getAck().getMessage());
	}
	//	
	// public void testGetValidationSchema() throws ServiceException,
	// RemoteException {
	// ManifestSvcLocator service = new ManifestSvcLocator();
	// ManifestSvcSoap soapService = service.getManifestSvcSoap();
	// System.out.println(soapService.getValidationSchema());
	// }

	// public void testVinsightWebService() throws Exception {
	// callVinsightService();
	// }
	// private void callVinsightService() throws ServiceException,
	// RemoteException, javax.xml.soap.SOAPException {
	// VINSightHDOAWebServiceLocator service = new
	// VINSightHDOAWebServiceLocator();
	// VINSightHDOAWebServicePort serv =
	// service.getVINSightHDOAWebServicePort();
	// VINSightHDOACommodity[] commodity = serv.getHDOACommodities("ISL216W");
	// if (commodity == null)
	// fail("No Commodity could be retrieved");
	// else {
	// for (int i = 0;i<commodity.length;i++) {
	// System.out.println(commodity[i]);
	// }
	// }
	// }
	
	public static void testGetItineraryForFirstVV() {
		String origin= "LAX",dest = "HON" ,vessel = "RJP";
		int voyage = 340;
        long start = System.currentTimeMillis();
        Itinerary iten = new ItineraryImpl();
        List list = iten.getItineraryForFirstVV(origin,dest, vessel, voyage);
        System.out.println("Number of trips found "+origin+" -> "+dest+" = "+list.size());
        java.util.Collections.sort(list, new TripArrivalComparator());
        double time = (System.currentTimeMillis() - start)/1000.0;
        for(int i=0;i<list.size();i++) {
            Trip t = (Trip)list.get(i);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
            for(Iterator iter = t.getLegs().iterator();iter.hasNext();) {
                ItineraryLeg leg = (ItineraryLeg)iter.next();
                System.out.print(leg.getOrigin() + "->"+leg.getDest());
                System.out.print(",Depart Date:"+sdf.format(leg.getDepartDate()));
                System.out.print(",Arrival Date:"+sdf.format(leg.getArriveDate()));
                System.out.print(",Vessel:"+leg.getVessel()+" Voyage:"+leg.getVoyage()+" Leg Direction:"+leg.getDir()+",");
                System.out.print("   ");
            }
            System.out.println();
        }
    }
}
