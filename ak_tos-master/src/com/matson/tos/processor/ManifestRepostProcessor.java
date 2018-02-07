package com.matson.tos.processor;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Stub;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.NormalizedString;
import org.apache.log4j.Logger;
import org.tempuri.Invicta.ManifestSvc.ManifestSvcLocator;
import org.tempuri.Invicta.ManifestSvc.ManifestSvcSoap;
import org.tempuri.Invicta.ManifestSvc.ManifestWSResponse;
import org.tempuri.Invicta.ManifestSvc.StatusEnum;
import org.tempuri.ManifestWSRequest_xsd.HawaiiHarbors;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequest;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnit;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitContainerNonType;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHAZ;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitHDOAHold;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTempSpecification;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitTransactionType;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestDataPerUnitWeightSpecification;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeader;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderManifestType;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfDischarge;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderPortOfLoading;
import org.tempuri.ManifestWSRequest_xsd.ManifestWSRequestHeaderTransporter;
import com.matson.tos.exception.ConsigneeNotFoundException;
import com.matson.tos.exception.ContainerNumberNotFoundException;
import com.matson.tos.exception.ContainerSizeNotFoundException;
import com.matson.tos.exception.ContainerWeightNotFoundException;
import com.matson.tos.exception.ReeferTempNotFoundException;
import com.matson.tos.exception.PortNotFoundException;
import com.matson.tos.exception.ShipperNotFoundException;
import com.matson.tos.exception.TosException;
import com.matson.tos.jaxb.snx.Snx;
import com.matson.tos.jaxb.snx.TUnit;
import com.matson.tos.jaxb.snx.TUnitContents;
import com.matson.tos.jaxb.snx.TUnitEquipment;
import com.matson.tos.messageHandler.AbstractMessageHandler;
import com.matson.tos.messageHandler.IMessageHandler;
import com.matson.tos.messageHandler.ManifestMessageHandler;
import com.matson.tos.util.EnvironmentProperty;
import com.matson.tos.util.ManifestHelper;
import com.matson.tos.util.VINSightHelper;
import com.matson.vessel.Itinerary;
import com.matson.vessel.ItineraryImpl;
import com.matson.vessel.vo.Trip;
import com.matson.vinsight.webservice.VINSightHDOAWebServiceLocator;
import com.matson.vinsight.webservice.VINSightHDOAWebServicePort;
import com.matson.vinsight.webservice.cars.ws.to.VINSightHDOACommodity;
import javax.xml.soap.SOAPException;
import com.matson.tos.util.TosRefDataUtil;


public class ManifestRepostProcessor {
	private static Logger logger = Logger.getLogger(ManifestRepostProcessor.class);
	private static String DEST_PORT = "HON";
	private String environment = EnvironmentProperty.getEnvType();
	private static String REPROCESS_MSG = "NewVes Messages need to be Reprocess to the AG Manifest Service. \n";
	private static String FAIL_MSG = "AG Manifest Failed to Post for Vessel ";
	private static String SUCCESS_MSG ="AG Manifest Successfully Posted for Vessel ";
	private final String AG_MANIFEST_EMAIL = TosRefDataUtil.getValue( "AG_MANIFEST_EMAIL");
	private final String AG_MANIFEST_ERROR_EMAIL = TosRefDataUtil.getValue( "AG_ERROR_EMAIL");
	private String _vesvoy = null;

	static {
		EnvironmentProperty.configure();
	}
	
	public void processMsg(String msgText) throws Exception {		
		IMessageHandler msgHandler = null;
		if (msgText == null || "".equalsIgnoreCase(msgText)) {
			logger.error("Manifest processMsg called with null");
			throw new Exception("Manifest processMsg called with null");
		}
		logger.debug("ManifestRepostProcessor.processMsg called");
		try {
			msgHandler = new ManifestMessageHandler("com.matson.tos.jaxb.snx",
					"com.matson.tos.jatb", "/xml/abc.xml",
					AbstractMessageHandler.TEXT_TO_XML);
			msgHandler.setTextObj(msgText);
			Object o = msgHandler.getXmlObj();
			if (o == null) {
				// there is either nothing to read or there was an error reading
				// the message as
				// the snx object cannot be null hence throw an exception
				logger.error("SNX Object read is null for the msgText : "
						+ msgText);
				ManifestHelper
						.sendEmail("SNX Object read is null for the msgText : "
								+ msgText);
				return;
			}
			Snx snxObj = (Snx) o;
			if (snxObj.getUnit() == null || snxObj.getUnit().size() <= 0) {
				// no units found in the snx object, this an error condition
				// report it & do nothing
				logger
						.error("Unit Object read is null from the snx object msgText : "
								+ msgText);
				return;
			}
			ManifestWSRequest wsRequest = new ManifestWSRequest();
			// do the object transformation in to manifest request object
			String direction = processN4Message(snxObj, wsRequest);
			logger.debug("After processN4Message");
			if (wsRequest.getDataPerUnit() != null
					&& wsRequest.getDataPerUnit().length > 0) {
				logger.debug("Calling callAgriDeptService");
				boolean success = callAgriDeptService(wsRequest);
				if(!success)
					return;
				logger.debug("After Calling callAgriDeptService");
			}
			else {
				logger
						.error("There were no containers found in the new vessel process for vessel"
								+ wsRequest.getHeader().getVesselName()
								+ wsRequest.getHeader().getVoyage());
				ManifestHelper
						.sendEmail("There were no containers found in the new vessel process for vessel"
								+ wsRequest.getHeader().getVesselName()
								+ wsRequest.getHeader().getVoyage() + ": \n");
			}

			try {
				wsRequest.setDataPerUnit(null);// reinitialize the data elements
				logger.debug("Calling Vinsight webservice");
				wsRequest = callVINSightWebService(wsRequest, direction);
				logger.debug("After Vinsight webservice");
				if (wsRequest.getDataPerUnit() != null
						&& wsRequest.getDataPerUnit().length > 0) {
					logger.debug("Calling callAgriDeptService for vinsight units");
					boolean success = callAgriDeptService(wsRequest);
					if(!success)
						return;
					logger.debug("After Calling callAgriDeptService for vinsight units");
					
				}
				else {
					logger.error("No units returned from Vinsight for vessel"
							+ wsRequest.getHeader().getVesselName()
							+ wsRequest.getHeader().getVoyage());
					ManifestHelper
							.sendEmail("No units returned from Vinsight for vessel"
									+ wsRequest.getHeader().getVesselName()
									+ wsRequest.getHeader().getVoyage());
				}
			} catch (Exception e) {
				logger.error("Error Calling VIN Sight Web service : " + e);
				ManifestHelper
						.sendEmail("Exception in Manifest processMsg calling Vinsight WebService : \n"
								+ e);

			}
			logger.debug("Done with processMsg");
			//return wsRequest;
		} catch (TosException e) {
			logger.error("Exception in processMsg: " + e);
			ManifestHelper.sendEmail("Exception in Manifest processMsg : \n"
					+ e);
		}
		//return null;
	}

	private boolean callAgriDeptService(ManifestWSRequest req) throws ServiceException,
	RemoteException, javax.xml.soap.SOAPException {
		boolean success = false;
		try {
			ManifestHelper manifestHelper = new ManifestHelper();
			logger.debug("Inside callAgriDeptService");
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
			logger.debug("Calling save on agridept webservice with url: " + manifestHelper.getWSURL() + ", username: " + manifestHelper.getUserName() + ", password: " + manifestHelper.getPassword());
			logger.debug("For agri dept the request obj is: " + req);
			ManifestWSResponse response = soapService.save(req);  
			if (response.getAck().getStatus() != StatusEnum.statusImportSucceeded) {
				logger.error("Agri dept Web service returned failure with error message : "
						+ response.getAck().getMessage());
				ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR,AG_MANIFEST_EMAIL,
						FAIL_MSG+getVesvoy(),REPROCESS_MSG+response.getAck().getMessage());
				throw new ServiceException("Agri dept Web service returned failure with error message : "+ response.getAck().getMessage());
			}
			logger.debug("Agri Dept web service response : " + response.getAck().getMessage());
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR,AG_MANIFEST_EMAIL,SUCCESS_MSG+getVesvoy(),response.getAck().getMessage());
			success = true;
		} catch(ServiceException e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – AG service call is failed",
					"Could not make AG service call. <br/> " + e.getMessage());
		} catch(SOAPException e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – AG service call is failed",
					"Could not make AG service call. <br/> " + e.getMessage());
		}
		catch(Exception e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – AG service call is failed",
					"Could not make AG service call. <br/> " + e.getMessage());
		}
		
		return success;
	}

	private String processN4Message(Snx snxObj, ManifestWSRequest wsRequest)
			throws Exception {
		String direction = null;
		try {
			Iterator<TUnit> iter = snxObj.getUnit().iterator();
			List<ManifestWSRequestDataPerUnit> data = new ArrayList<ManifestWSRequestDataPerUnit>();
			int ctr = 0;
			String cntrNbr = null; String freightKind = null;
			ArrayList<String> errList = new ArrayList<String>();
			while (iter.hasNext()) {
				TUnit unit = iter.next();
				logger.debug("Unit object read is: " + unit);
				String vesselInfo[] = new String[2]; // 2 strings to hold vessel
				//String ports[] = new String[1];
				String opl,pod, oplHeader = null;

				// code & voyage code
				try{ //A9 - Added code to Handle Incorrect Routing Information
				  vesselInfo = ManifestHelper.extractVesselName(unit);
 				  setVesvoy(vesselInfo[0]+vesselInfo[1]);
				}catch(Exception ex){
					errList.add(unit.getId()+" Routing Info not Found");
					continue;
				}
				//
				
				if (unit != null) {
					ctr++;
					// create the request header object
					ManifestWSRequestHeader reqHeader = new ManifestWSRequestHeader();
					pod = ManifestHelper.extractDischargePort(unit);
						
					if (ctr == 1) {//- WS Header Information (set header only once)
						try {
							opl = ManifestHelper.extractOriginalPortOfLoad(unit);
							oplHeader = ManifestHelper.ports.get(opl);
						} catch (PortNotFoundException e) {
							logger.error(e.getMessage());
							errList.add(e.getMessage());
							ctr--;
							continue;// skip this container & go to next
						} 
						
						reqHeader.setPortOfLoading(ManifestWSRequestHeaderPortOfLoading
										.fromString(oplHeader));
						reqHeader.setPortOfDischarge(ManifestWSRequestHeaderPortOfDischarge.PIO);
						reqHeader.setManifestType(ManifestWSRequestHeaderManifestType
										.fromString(ManifestHelper.MANIFEST_TYPES.MANIFEST_TYPE_CONTAINER));
					/*	reqHeader.setTransmitDateTime(Calendar
								.getInstance(TimeZone
										.getTimeZone("America/Hawaii"))); */
						//A1
						reqHeader.setTransmitDateTime(Calendar.getInstance());
						reqHeader.setVesselName(new NormalizedString(
								vesselInfo[0]));
						reqHeader
								.setVoyage(new NormalizedString(vesselInfo[1]));
						reqHeader
								.setTransporter(ManifestWSRequestHeaderTransporter
										.fromString(ManifestHelper.TRANSPORTER));
						wsRequest.setHeader(reqHeader);// add the header object

						direction = ManifestHelper.extractDirection(unit);
						
						//get the vessel ETA & ATD
						Itinerary iten = new ItineraryImpl();
						logger.debug("Calling vessel itinerary with origin port: " + opl + ", destination port: " + pod + ", voyage: " + Integer.parseInt(vesselInfo[1]));
						//A1
						List list = iten.getItineraryForFirstVV(opl,DEST_PORT, vesselInfo[0], Integer.parseInt(vesselInfo[1]));												
						if (list == null || list.size() < 1) {
							logger.error("Vessel Information could not be found for vessel: " + vesselInfo[0] + ", voyage: " + vesselInfo[1]);
							throw new Exception("Vessel Information could not be found for vessel: " + vesselInfo[0] + ", voyage: " + vesselInfo[1]);
						}
						logger.debug("After getItineraryForFirstVV, list size is: " + + list.size());
						
						Trip t = (Trip)list.get(0);
						if (t == null) {
							logger.error("Vessel Information could not be found for vessel: " + vesselInfo[0] + ", voyage: " + vesselInfo[1]);
							throw new Exception("Vessel Information could not be found for vessel: " + vesselInfo[0] + ", voyage: " + vesselInfo[1]);
						}
						logger.debug("The trip retrieved is: " + t.toString());
						
						Calendar ETA = t.getArrivalDate();
						Calendar ATD = t.getDepartureDate();
						logger.debug("Got from Vinsight : Vessel: " + vesselInfo[0] + ", Voyage: " + vesselInfo[1] + ", ETA: " + ETA + ", ATD: " + ATD);
						reqHeader.setETA(ETA);
						reqHeader.setATD(ATD);
					}
					//---- WS Header Information Ends
				}
				try {
					/*
					 1. if Freight Kind = MTY &&  Booking=null 
                             Skip unit dont pass to AG  
                     2. if  TypeCode ends with GB (Trash Cntr)
                             Skip unit dont pass to AG       
                     3. if Consignee != null  && Shipper == null
                             Then Shipper = Consignee
                     4.  if Shipper != null && Consignee == null
                             Then Consignee = NA
                     5. if Freight Kind = FCL && Shipper == null && Consignee == null
                             Then Shipper=NA , Consignee=NA
                     6. if Shipper == null && Consignee == null
                             Skip unit dont pass to AG
					 */
					// set the container information for all the units
					TUnitEquipment equip = unit.getEquipment().get(0);
					TUnitContents contents = unit.getContents();
					ManifestWSRequestDataPerUnit unitData = new ManifestWSRequestDataPerUnit();
					cntrNbr= ManifestHelper.extractContainerNumber(unit);
				    freightKind = ManifestHelper.extractFreightKind(unit);
				    String cntrTypeCode = ManifestHelper.extractContainerTypeCode(unit);
				    String cntrBkg = ManifestHelper.extractBooking(unit);
				    String consignee = ManifestHelper.extractConsignee(unit);
					String shipper = ManifestHelper.extractShipper(unit);
				    
				    logger.debug("freightKind="+freightKind+" cntrTypeCode="+cntrTypeCode+" cntrOwner="+cntrBkg);
				    if("MTY".equals(freightKind) && cntrBkg == null){
				    	ctr--;
				    	errList.add(cntrNbr+" Is EMPTY With No Booking");
				    	continue;
				    }else if(cntrTypeCode != null && cntrTypeCode.endsWith("GB")){
				    	ctr--;
				    	errList.add(cntrNbr+" Type Code is GB (Trash Cntr)");
				    	continue;
				    }
                    //Added Check to roll over consignee and shipper value and pass NA(not available) for full cntr with no shipper and consignee value				    
				    if(consignee != null && shipper != null){
				    	unitData.setConsignee(new NormalizedString(consignee));
				    	unitData.setShipper(new NormalizedString(shipper));
				    }else if(consignee != null && shipper == null){
				    	unitData.setConsignee(new NormalizedString(consignee));
                    	unitData.setShipper(new NormalizedString(consignee));
                    }else if(consignee == null || shipper == null){
                    	if(consignee == null)
            			  throw new ConsigneeNotFoundException("Consignee not found in equipment: " + unit.getEquipment().get(0));
                    	if(shipper == null)
              			  throw new ShipperNotFoundException("Shipper not found it is mandatory for equipment: " + unit.getEquipment().get(0));
                    }

				    
					// unitData.setBillOfLadingNo("");
					

					unitData.setContainerNo(new NormalizedString(cntrNbr));
					unitData
							.setContainerNonType(ManifestWSRequestDataPerUnitContainerNonType
									.fromString((ManifestHelper
											.extractContainerType(unit))));
					// unitData.setContainerParcelCount(-1);
					unitData.setContainerSizeFeet(ManifestHelper
							.extractContainerSizeFeet(unit));
					// Lorna said don't set the content commodity as this is a
					// 243 item list
					// as of today we do not get this list from N4 so take it
					// out
					// unitData.setContentsCommodity(new
					// NormalizedString(ManifestHelper.extractContainerCommodity(unit)));

					// unitData.setDischargeService(ManifestHelper.DISCHARGE_SERVICE.CY);
					// unitData.setGrossWeight(ManifestHelper.extractContainerWeight(unit));
					// unitData.setHatchCellNo("");
					unitData.setHAZ(ManifestWSRequestDataPerUnitHAZ
							.fromString((ManifestHelper.extractHazmat(unit))));
					unitData.setNetWeight(ManifestHelper.extractContentWeight(unit));
					// unitData.setNonContainerNo("");
					String portofLoading = ManifestHelper.extractPortOfLoading(unit);
					if(portofLoading != null && portofLoading.trim().length() != 0){
						unitData.setPortOfOriginUnit(new NormalizedString(portofLoading));
					}
                    
					String sealNo = ManifestHelper.extractSealNumber(unit);
					if (!(sealNo == null || "".equalsIgnoreCase(sealNo)))
						unitData.setShipperSealNo(new NormalizedString(sealNo));

					String tariffDesc = ManifestHelper.extractContainerCommodity(unit);
					if (!(tariffDesc == null || "".equalsIgnoreCase(tariffDesc))){
						unitData.setTariffDesc(tariffDesc);
					}
					
					String shippersRef = ManifestHelper.extractShipperId(unit); 
					if(shippersRef != null && shippersRef.trim().length() <= 20){ //A7
					  unitData.setShippersRef(new NormalizedString(shippersRef));
					}
					// unitData.setTemperature(-1);
					// unitData.setTempSpecification("");
					// unitData.setThirdPartyCheck("");
					// unitData.setTOS(Manifest);
					unitData
							.setTransactionType(ManifestWSRequestDataPerUnitTransactionType
									.fromString(ManifestHelper.TRANSACTION_TYPES.ADD));
					// unitData.setVINRefNo("");
					unitData
							.setWeightSpecification(ManifestWSRequestDataPerUnitWeightSpecification
									.fromString(ManifestHelper.WEIGHT_SPECIFICATION.KG));
					BigDecimal temp = ManifestHelper.extractTemperature(unit);
					if (temp != null) {
						unitData.setTemperature(temp.intValue());
						unitData
								.setTempSpecification(ManifestWSRequestDataPerUnitTempSpecification.C);
					}
					if (ManifestHelper.extractAGHold(unit).intValue() == 1)
						unitData
								.setHDOAHold(ManifestWSRequestDataPerUnitHDOAHold.value2);
					else
						unitData
								.setHDOAHold(ManifestWSRequestDataPerUnitHDOAHold.value1);

					//A101
 				    unitData.setDeliveryPortUnit(ManifestHelper.getInspectionPort(unit));
					unitData.setDeliveryPlaceDescUnit(new NormalizedString(ManifestHelper.extractDestinationPort(unit)));
					logger.debug("Adding unit="+cntrNbr+" Inspection Port="+ManifestHelper.getInspectionPort(unit)+"  Destination Port="+ManifestHelper.extractDestinationPort(unit));
					data.add(unitData);// add the unit to list
					
				} catch (ConsigneeNotFoundException e) {
					logger.error(cntrNbr +":Consignee Not found in SNX Message : "
							+ e.getMessage());
					errList.add(cntrNbr +" Consignee Not found and Freight is "+freightKind);
					continue;
				} catch (ContainerNumberNotFoundException e) {
					logger.error("Container Number not found in SNX Message : "
							+ e.getMessage());
					errList.add(cntrNbr +" Container Number Not found and Freight is "+freightKind);
					continue;
				} catch (ContainerSizeNotFoundException e) {
					logger.error(cntrNbr +":ContainerSize Not found in SNX Message : "
							+ e.getMessage());
					errList.add(cntrNbr +" ContainerSize Not found and FreightKind is "+freightKind);
					continue;
				} catch (ContainerWeightNotFoundException e) {
					logger.error(cntrNbr +":ContainerWeight Not found in SNX Message : "
							+ e.getMessage());
					errList.add(cntrNbr +" ContainerWeight Not found and FreightKind is "+freightKind);
					continue;
				} catch (ShipperNotFoundException e) {
					logger.error(cntrNbr +":Shipper Not found in SNX Message : "
							+ e.getMessage());
					errList.add(cntrNbr +" Shipper Not found and FreightKind is "+freightKind);
					continue;
				}catch (PortNotFoundException e) {
					logger.error(cntrNbr +":Port Not found in SNX Message : "+ e.getMessage());
					errList.add(cntrNbr +" Port Not found and FreightKind is "+freightKind);
					continue;// skip this container & go to next
				}catch (ReeferTempNotFoundException e) {
					logger.error(cntrNbr +":Temp Not found in SNX Message : "+ e.getMessage());
					errList.add(cntrNbr +" Temp Not found and FreightKind is "+freightKind);
					continue;// skip this container & go to next
				} 
				
			}
			//Error Email with CntrNbr and Error Type
			if(errList.size() > 0){
			  String body = formatMailBody(errList);	
			  ManifestHelper.sendEmail(getVesvoy()+" Containers didn’t Posted to AG Manifest Service Count="+errList.size(),body);
			}
			
			ManifestWSRequestDataPerUnit dataA[] = new ManifestWSRequestDataPerUnit[data
					.size()];
			wsRequest.setDataPerUnit(data.toArray(dataA));
			logger.error("NewVes WSRequestMsg count :"+data.size());
			wsRequest.getHeader().setTransmitCount(data.size());
			return direction;
		} catch (Exception e) {
			logger.error("Exception while creating manifest message handler"
					+ e);
			throw e;
		}
	}

	private ManifestWSRequest callVINSightWebService(
			ManifestWSRequest wsRequest, String direction) throws Exception {
		VINSightHDOAWebServiceLocator service = new VINSightHDOAWebServiceLocator();
		VINSightHDOAWebServicePort serv;
		ManifestWSRequestDataPerUnit unitData = null;
		List<ManifestWSRequestDataPerUnit> unitList = new ArrayList<ManifestWSRequestDataPerUnit>();
		logger.debug("Inside callVINSightWebService");
		try {
			String VVD = new String(wsRequest.getHeader().getVesselName()
					.toString()
					+ wsRequest.getHeader().getVoyage().toString());
			// check if the direction is there or not, as for line haul there is
			// generally no direction
			if (!VVD.substring(VVD.length() - 1).equalsIgnoreCase(direction))
				VVD += direction; // Direction not found so set
			// it
			serv = service.getVINSightHDOAWebServicePort();
			VINSightHDOACommodity[] commodity = serv.getHDOACommodities(VVD);

			// Reset the header properties now to make vinsight call
			wsRequest.getHeader().setTransmitCount(0);
			wsRequest.getHeader().setTransmitDateTime(Calendar.getInstance());
			wsRequest.getHeader().setManifestType(ManifestWSRequestHeaderManifestType.N);

			if (commodity == null){
				logger.debug("Vinsight VVD Result Set is Null");
				return wsRequest;
			}
			logger.debug("Vinsight VVD:"+VVD+" Result Set :"+(commodity != null ? commodity.length : "0"));
			for (int i = 0; i < commodity.length; i++) {
				try {
					
					String tariffDesc = VINSightHelper.extractCommodity(commodity[i]); 
					String vinNumber = VINSightHelper.extractVINNumber(commodity[i]); 
					String billOfLadingNo = VINSightHelper.extractBillOfLadingNo(commodity[i]);  
					String consignee = VINSightHelper.extractConsignee(commodity[i]);  
					HawaiiHarbors destinationPort = VINSightHelper.extractDestinationPort(commodity[i]); 
					String shipper = VINSightHelper.extractShipper(commodity[i]);  
					String vinsightId = VINSightHelper.extractNonContainerNo(commodity[i]);  
					String originPort = VINSightHelper.extractOriginPort(commodity[i]); 
					BigDecimal netWeight =VINSightHelper.extractNetWeight(commodity[i]);
					unitData = new ManifestWSRequestDataPerUnit();
					
					//A2- IF it exceptions out the Request Object is not Created Unit is not set
					if (tariffDesc != null && tariffDesc.trim().length() >0 ){
						unitData.setTariffDesc(tariffDesc);
					}else{ unitData.setTariffDesc("NA"); }
					
					if (billOfLadingNo!= null && billOfLadingNo.trim().length() > 0){
						unitData.setBillOfLadingNo(new NormalizedString(billOfLadingNo));
					}
					unitData.setNonContainerNo(new NormalizedString(vinsightId));
					unitData.setConsignee(new NormalizedString(consignee));
					unitData.setContainerNonType(ManifestWSRequestDataPerUnitContainerNonType.value13);
					//A2
					//unit.setDeliveryPortUnit(HawaiiHarbors.fromString(destinationPort));
					//Inspection Port
					unitData.setDeliveryPortUnit(destinationPort);
							
					if (commodity[i].isHaz())
						unitData.setHAZ(ManifestWSRequestDataPerUnitHAZ.value2);
					else
						unitData.setHAZ(ManifestWSRequestDataPerUnitHAZ.value1);

					if (commodity[i].isAgHold())
						unitData.setHDOAHold(ManifestWSRequestDataPerUnitHDOAHold.value2);
					else
						unitData.setHDOAHold(ManifestWSRequestDataPerUnitHDOAHold.value1);
					unitData.setNetWeight(netWeight);

					unitData.setPortOfOriginUnit(new NormalizedString(originPort));

					unitData.setShipper(new NormalizedString(shipper));
					unitData.setTransactionType(ManifestWSRequestDataPerUnitTransactionType.A);
                    
					if(vinNumber != null && vinNumber.length() > 0){
					  unitData.setVINRefNo(new NormalizedString(vinNumber));
					}

					unitData.setWeightSpecification(ManifestWSRequestDataPerUnitWeightSpecification.lb);
                    logger.debug("VinsingId="+vinsightId+" Ag Msg Sent");
					unitList.add(unitData);

				} catch (Exception e) {
					logger.error("Vinsight WSMsg creation exception:"+e);
					continue; // in case of an exception with one container
					// start with next
				}
			}
			ManifestWSRequestDataPerUnit dataA[] = new ManifestWSRequestDataPerUnit[unitList.size()];
			wsRequest.setDataPerUnit(unitList.toArray(dataA));
			wsRequest.getHeader().setTransmitCount(unitList.size());
			logger.debug("Finishing callVINSightWebService WSRequest unit Count: "+unitList.size());
			return wsRequest;

		} catch (ServiceException e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – Vinsight web service call is failed",
					"Could not make vinsight web service call. <br/> " + e.getMessage());
			logger.error("Exception in calling vinsight web service: " + e);
			throw e;
		} catch (RemoteException e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – Vinsight web service call is failed",
					"Could not make vinsight web service call. <br/> " + e.getMessage());
			logger.error("Exception in calling vinsight web service: " + e);
			throw e;
		} catch (IllegalArgumentException e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – Vinsight web service call is failed",
					"Could not make vinsight web service call. <br/> " + e.getMessage());
			logger.error("Exception in calling vinsight web service: " + e);
			throw e;
		} catch (Exception e) {
			ManifestHelper.sendEmail(ManifestHelper.EMAIL_ADDR, ManifestHelper.EMAIL_ADDR, "ERROR – Vinsight web service call is failed",
					"Could not make vinsight web service call. <br/> " + e.getMessage());
			logger.error("Exception in calling vinsight web service: " + e);
			throw e;
		}finally{
			unitList = null;
			unitData = null;
		}
	}
	
	public void setVesvoy(String vesvoy){
		_vesvoy = vesvoy;
	}
	
	public String getVesvoy(){
		return _vesvoy;
	}
	
	
	private String formatMailBody(ArrayList arrlst){
		StringBuilder msgStr = new StringBuilder();
		try{
		     Iterator it = arrlst.iterator();
	 	     msgStr.append("<html><body><tr><td>Manifest Units Not post to the AG Dept</td></tr></br></br>");
	 	     msgStr.append("<table border='0' width='42%' id='table1'>");
			 msgStr.append("<tr><td><u>Manifest Process Error Message</u></td></tr>");
		     while(it.hasNext()){ 
		      String key = (String)it.next(); 
		      msgStr.append("<tr><td>"+key+"</td></tr>");

		     }//3. send Confirmation mail
		     msgStr.append("</table></body></html>");
		}catch(Exception e){
			e.printStackTrace();
		}
		return msgStr.toString();
	}

}
