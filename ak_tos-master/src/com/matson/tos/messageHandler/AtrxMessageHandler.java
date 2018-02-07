package com.matson.tos.messageHandler;

import com.matson.tos.exception.TosException;
import com.matson.tos.jatb.Atrx;
import com.matson.tos.jaxb.snx.*;
import com.matson.tos.util.CalendarUtil;
import com.matson.tos.util.TosRefDataUtil;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * A1 - SKB 01/07/2009, Added APL line.
 * A2 - SKB 01/20/2009, Added expiration to MAT line that was removed as part of A2.
 * A3 - SKB 04/05/2009, Added MED Line.
 * @author xw8
 *
 */
public class AtrxMessageHandler extends AbstractMessageHandler {
	public static final String ALASKA_TRUCKING_COMPANIES = "ALASKA_TRUCKING_COM";
	private static Logger logger = Logger.getLogger(AtrxMessageHandler.class);
	
	public AtrxMessageHandler(String xmlObjPackageName,
			String textObjPackageName, String fmtFile, int convertDir)
			throws TosException {
		super(xmlObjPackageName, textObjPackageName, fmtFile, convertDir);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object textObjToXmlObj(Object textObj) throws TosException {
		// TODO Auto-generated method stub
		Atrx atrxObj = (Atrx)textObj;
		Snx snxObj = new Snx();
		List<TTruckingCompany> truckCompList = snxObj.getTruckingCompany();
		TTruckingCompany truckComp = new TTruckingCompany();
		truckCompList.add( truckComp);
		if (atrxObj.getTxnCode().trim().equalsIgnoreCase("ATRU")) {
			Boolean isValidTruckingComapany = Boolean.FALSE;
			try {
				String tempCompanies = TosRefDataUtil.getValue(ALASKA_TRUCKING_COMPANIES).toUpperCase();
				if (tempCompanies != null) {
					List<String> companies = Arrays.asList(tempCompanies.split(",", -1));
					String snxCompanyId = atrxObj.getTruckCompCode().trim().toUpperCase();
					if (snxCompanyId != null && companies.contains(snxCompanyId)) {
						// continue the flow
						isValidTruckingComapany = Boolean.TRUE;
					}
				}
			} catch (Exception e) {
				logger.error("Error in processing an ATRU to snx for company id" + atrxObj.getTruckCompCode(), e);
			} finally {
				if (!isValidTruckingComapany) {
					return null;
				} else {
					//continue
				}
			}
		}

		// on trucking comp level
		truckComp.setId(atrxObj.getTruckCompCode().trim());
		truckComp.setName( atrxObj.getTruckCompName().trim());
		truckComp.setNotes("ACETS "+atrxObj.getTxnCode().trim());
		
		// on line agreement level
		TTruckingCompany.LineAgreements lineAgree;
		lineAgree = truckComp.getLineAgreements();
		if ( lineAgree == null) {
			lineAgree = new TTruckingCompany.LineAgreements();
			truckComp.setLineAgreements( lineAgree);
		}
		
		lineAgree.setUpdateMode( TUpdateMode.MERGE);
		TLineAgreement aLine1 = new TLineAgreement();
		List<TLineAgreement> lineList = lineAgree.getLineAgreement();
		lineList.add( aLine1);
		aLine1.setShippingLine( "MAT");
		// A2
		aLine1.setExpirationDate( CalendarUtil.getXmlCalendar( "12/31/2050", "MM/dd/yyyy"));
		
		// A1 - Add APL.
		TLineAgreement aLine2 = new TLineAgreement();
		lineList.add( aLine2);
		aLine2.setShippingLine( "APL");
		aLine2.setExpirationDate( CalendarUtil.getXmlCalendar( "12/31/2050", "MM/dd/yyyy"));
		
		TLineAgreement aLine3 = new TLineAgreement();
		lineList.add( aLine3);
		aLine3.setShippingLine( "MED");
		aLine3.setExpirationDate( CalendarUtil.getXmlCalendar( "12/31/2050", "MM/dd/yyyy"));
		
		if ( atrxObj.getTxnCode().trim().equalsIgnoreCase( "ATRU") ||
				atrxObj.getTxnCode().trim().equalsIgnoreCase( "ATRA")) {
			aLine1.setTruckLineStatus( TTruckStatus.OK);
			aLine1.setLifeCycleState(TLifeCycleStateType.ACT);
			aLine2.setTruckLineStatus( TTruckStatus.OK);
			aLine2.setLifeCycleState(TLifeCycleStateType.ACT);
			aLine3.setTruckLineStatus( TTruckStatus.OK);
			aLine3.setLifeCycleState(TLifeCycleStateType.ACT);
			
			truckComp.setLifeCycleState(TLifeCycleStateType.ACT);
		} else if ( atrxObj.getTxnCode().trim().equalsIgnoreCase( "ATRD")) {
			aLine1.setTruckLineStatus( TTruckStatus.RCVONLY);
			aLine2.setTruckLineStatus( TTruckStatus.RCVONLY);
			aLine3.setTruckLineStatus( TTruckStatus.RCVONLY);
		} else {
			logger.debug( "Wrong txn code: " + atrxObj.getTxnCode().trim());
		}
		return snxObj;
	}

	@Override
	protected Object xmlObjToTextObj(Object xmlObj) throws TosException {
		// TODO Auto-generated method stub
		return null;
	}

}
