package com.matson.tos.upload;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt;
import com.matson.tos.dao.NewVesselDaoGum;
import com.matson.tos.exception.NewVesselLogger;
import com.matson.tos.processor.CommonBusinessProcessor;

public class GumAddCtr extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7989305258921981142L;
	private static Logger logger = Logger.getLogger(GumAddCtr.class);
	//
	protected void service(HttpServletRequest request, HttpServletResponse response)
												throws ServletException, IOException {
		try {
			if(request.getParameter("Save")!=null) {
				String vesvoy = request.getParameter("addVesvoy");
				vesvoy = vesvoy.toUpperCase();
				boolean validVV = CommonBusinessProcessor.isValidVesvoy(vesvoy);
				if(validVV) {
					logger.info("******** Guam newves adding new container - begin********");
					TosGumRdsDataFinalMt rdsFd = new TosGumRdsDataFinalMt();
					rdsFd.setVesvoy(vesvoy);
					rdsFd.setActualVessel(vesvoy.substring(0, 3));
					rdsFd.setActualVoyage(vesvoy.substring(3, 6));
					rdsFd.setLeg("W");
					String containerNumber = request.getParameter("addCtrNbr");
					containerNumber = containerNumber==null?"":containerNumber.toUpperCase();
					rdsFd.setContainerNumber(containerNumber);
					String chkDgt = request.getParameter("addChkDgt");
					chkDgt = chkDgt==null?"":chkDgt.toUpperCase();
					rdsFd.setCheckDigit(chkDgt);
					String bkgNbr = request.getParameter("addBkgNbr");
					bkgNbr = bkgNbr==null?"":bkgNbr.toUpperCase();
					rdsFd.setBookingNumber(bkgNbr);
					String consignee = request.getParameter("addCneeName");
					consignee = consignee==null?"":consignee.toUpperCase();
					rdsFd.setConsignee(consignee);
					rdsFd.setConsigneeName(consignee);
					String shipper = request.getParameter("addShprName");
					shipper = shipper==null?"":shipper.toUpperCase();
					rdsFd.setShipper(shipper);
					rdsFd.setShipperName(shipper);
					String truck = request.getParameter("addTruck");
					truck = truck==null?"":truck.toUpperCase();
					rdsFd.setTruck(truck);
					String remarks = request.getParameter("addRemarks");
					remarks = remarks==null?"":remarks.toUpperCase();
					rdsFd.setCargoNotes(remarks);
					String loadPort = request.getParameter("addLPort");
					loadPort = loadPort==null?"":loadPort.toUpperCase();
					rdsFd.setLoadPort(loadPort);
					rdsFd.setCell(request.getParameter("addCell"));
					rdsFd.setSrv("MAT");
					rdsFd.setDir("IN");
					rdsFd.setDport("GUM");
					rdsFd.setDs("CY");
					rdsFd.setSealNumber("FR");
					rdsFd.setTypeCode("F40 86FC");
					rdsFd.setOwner("MATU");
					String cWeight = request.getParameter("addCWeight");
					if(cWeight!=null)
						rdsFd.setCweight(new BigDecimal(cWeight));
					//else
						//rdsFd.setCweight(new BigDecimal("9700"));
					rdsFd.setCneeCode("0000000000");
					rdsFd.setDischargePort("GUM");
					rdsFd.setLocationRowDeck("MAT");
					rdsFd.setOrientation("F");
					rdsFd.setHazardousOpenCloseFlag("G");
					String tWeight = request.getParameter("addTWeight");
					if(tWeight!=null)
						rdsFd.setTareWeight(new BigDecimal(tWeight));
					//else
						//rdsFd.setTareWeight(new BigDecimal("9700"));
					rdsFd.setHgt("080600");
					rdsFd.setStrength("IFB");
					rdsFd.setConsigneeArol("0000000000");
					rdsFd.setCreateUser("gumnewves");
					rdsFd.setCreateDate(new Date());
					rdsFd.setLastUpdateUser("gumnewves");
					rdsFd.setLastUpdateDate(new Date());
					rdsFd.setTrade("G");
					boolean isAdded = NewVesselDaoGum.addToRdsDataFinal(rdsFd);
					if(isAdded) {
						request.setAttribute("message", "<font font size='2' color='Green' face='Courier New'>" + vesvoy + "-" + containerNumber + " has been added successfully.</font>");
					} else {
						request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'> DATA VIOLATION : " + vesvoy + "-" + containerNumber + " has been already available.</font>");
					}
					logger.info("******** Guam newves adding new container - end********");
				} else {
					logger.info(vesvoy + " is invalid.");
					request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>" + vesvoy + " is not a valid vesvoy. Please re-enter a valid one.</font>");
				}
			}
		}catch(Exception ex) {
			logger.error(ex.getMessage());
			request.setAttribute("message", "<font font size='2' color='Red' face='Courier New'>Add Container failed due to -->" + ex.toString() + "</font>");
		}
		request.getRequestDispatcher("GumAddCtr.jsp").forward(request, response);
	}
}
