package com.matson.tos.groovy.formatter;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.matson.tos.dao.CargoStatusDao;
import com.matson.tos.groovy.CargoStatus;
import com.matson.tos.groovy.writer.ObjectWriterFactory;
import java.util.StringTokenizer;

public class EmailCargoStatus implements EmailFormatter {
	private static Logger logger = Logger.getLogger(EmailCargoStatus.class);
	private Map _data;

	private static String updt = "updt_";
	private static String tagPOD = "POD";
	private static String tagDestination = "Destination";
	private static String tagFreightKind = "FreightKind";
	private static String tagCommodityDescription = "CommodityDescription";
	private static String tagGoodsConsigneeName = "GoodsConsigneeName";
	public  static String tagUnitHoldsAndPermissions = "UnitHoldsAndPermissions";
	private static String tagDrayStatus = "DrayStatus";
	private static String tagSpecialStow = "SpecialStow";
	private static String tagRoutingGroup = "RoutingGroup";
	private static String tagGoodsBlNbr = "GoodsBlNbr";
	private static String tagUnitRemark = "UnitRemark";
	private static String tagVesvoy = "Vesvoy";
	private static String tagUnitNbr = "UnitNbr";
	private static String tagEditedBy = "editedBy";
	private static String tagEventTime = "eventTime";
	private static String tagLocation = "location";
	private static String tagEventNote = "eventNote";
	String editUser = "";
	String notes = "";
	String updatedNotes = "";
	String updateHolds = "";
	String holds = "";
	String updDrayStatus = "";
	String updCommodityDesc = "";
	int start = 0;
	
	public EmailCargoStatus( Map data) {
		_data = data;
		logger.info("EmailCargoStatusdata "+ _data);
	}
	public String getContent() {
		// TODO Auto-generated method stub
		String content = "";
		/* Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
	    String currDate =  sdf.format(cal.getTime());
	    sdf = new SimpleDateFormat("H:mm:ss");
	    String currTime = sdf.format(cal.getTime()); */
		String evtTime = (String)_data.get( tagEventTime);
		Date date = null;
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
			date = sdf.parse( evtTime);
		} catch ( Exception ex) {
			
		}
		sdf = new SimpleDateFormat("MM/dd/yy");
	    String currDate =  sdf.format( date);
	    sdf = new SimpleDateFormat("H:mm:ss");
	    String currTime = sdf.format( date);
	    logger.info("_data in EmailCargoStatus :"+_data.toString());
	    
	    editUser = _data.get( tagEditedBy)==null?"":_data.get(tagEditedBy).toString();
	    notes = _data.get( tagUnitRemark)==null?"":_data.get( tagUnitRemark).toString();
	    updateHolds = getUpdtValue( tagUnitHoldsAndPermissions);
	    holds = (_data.get( tagUnitHoldsAndPermissions)==null?"":_data.get( tagUnitHoldsAndPermissions)).toString();
	    updDrayStatus = getUpdtValue(tagDrayStatus);
	    updCommodityDesc = getUpdtValue(tagCommodityDescription);
	    logger.info("updCommodityDesc :"+updCommodityDesc);
	    logger.info("updDrayStatus valie is "+updDrayStatus+"  editUser :"+editUser+" updCommodityDesc :"+updCommodityDesc);
	    
	    if (editUser.equalsIgnoreCase("snx:-snx-")){
	    	if (notes.contains("ADD")){
	    		updateHolds = "";
	    	}
	    	if (notes.contains("CNC")){
	    		start = notes.indexOf("CNC") + 4;
	    		updatedNotes = notes.substring(start);
	    		start =  updatedNotes.indexOf(" ");
	    		updateHolds = updatedNotes.substring(0,start);
	    		holds = "";
	    	}
	    }

	    if (editUser.equalsIgnoreCase("snx:ACETS")) {
	    	if ("".equalsIgnoreCase(updDrayStatus) && "STOP IN TRANSIT".equalsIgnoreCase(updCommodityDesc)) {
	    		updCommodityDesc="";
	    	}
	    }
	    
		StringBuffer emailBuf = new StringBuffer();
		emailBuf.append("<style type=\"text/css\">.data {font-size: 75%}</style>");
		emailBuf.append( "<p><CENTER>MATSON NAVIGATION COMPANY<br>CARGO STATUS CHANGES</CENTER>");
		emailBuf.append( "<font size=\"2\" face=\"courier new\">");
		emailBuf.append( "<table><tr><td align=\"right\" width=\"100%\">Report Date :</td><td align=\"left\">");
		emailBuf.append( currDate).append("</td></tr>");
		emailBuf.append( "<tr>");
		emailBuf.append( "<td align=\"right\" width=\"100%\">Report Time :</td><td align=\"left\">").append( currTime).append("</td></tr>");
		emailBuf.append( "<tr><td colspan=2><hr></td></tr>");
		emailBuf.append( "</table>");

		emailBuf.append( "<table><tr><td  align=\"right\">Container :</td><td>");
		emailBuf.append( _data.get( tagUnitNbr)==null? "":_data.get( tagUnitNbr)).append( "</td><td></td><td></td></tr>");
		emailBuf.append( "<tr><td align=\"right\" >VESVOY :</td><td >");
		emailBuf.append( _data.get( tagVesvoy)==null?"":_data.get( tagVesvoy)).append( "</td><td nowrap >EDITED BY :</td><td nowrap >");
		emailBuf.append( _data.get( tagEditedBy)==null?"":_data.get( tagEditedBy)).append("&nbsp;&nbsp;").append( currDate).append( " @ ").append( currTime).append( "</td></tr>");
		emailBuf.append( "<tr><td align=\"right\">BOOKING # :</td><td>");
		emailBuf.append( _data.get( tagGoodsBlNbr)==null?"":_data.get( tagGoodsBlNbr)).append( "</td><td></td><td></td></tr>");
		emailBuf.append( "<tr><td align=\"right\">LS :</td><td>");
		emailBuf.append( _data.get( tagLocation)==null?"":_data.get( tagLocation)).append( "</td><td></td><td></td></tr>");
		
		emailBuf.append( "</table>");
		
		//emailBuf.append( "<table><tr><td ></td><td></td><td>FRGHT</td><td></td><td></td><td></td><td>SPCL</td><td>ROUTE</td><td></td></tr>");
		//emailBuf.append( "<tr><td>CONSIGNEE</td><td>PORT</td><td align=\"center\">KIND</td><td>HOLDS</td><td>DRAY</td><td>COMMODITY</td><td>STOW</td><td>GROUP</td><td></td></tr>");
		

		emailBuf.append( "<table><tr><td ></td><td></td><td></td><td>FRGHT</td><td></td><td></td><td></td><td>SPCL</td><td>ROUTE</td><td></td></tr>");
		emailBuf.append( "<tr><td>CONSIGNEE</td><td>POD</td><td>DEST</td><td align=\"center\">KIND</td><td>HOLDS</td><td>DRAY</td><td>COMMODITY</td><td>STOW</td><td>GROUP</td><td></td></tr>");
		//emailBuf.append( "<tr><td>-----------------------------------</td><td>----</td><td>-------</td><td>----------</td><td>-------------</td><td>-------------</td><td>----------</td><td>--------</td><td></td></tr>");
		emailBuf.append( "<tr><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td><td><hr></td></tr>");
		emailBuf.append( "<tr><td class=\"data\">").append( getUpdtValue( tagGoodsConsigneeName)).append( "</td><td class=\"data\">");
		emailBuf.append( getUpdtValue( tagPOD)).append( "</td><td class=\"data\">");
		emailBuf.append( getUpdtValue( tagDestination)).append( "</td><td class=\"data\">");
		emailBuf.append( getUpdtValue( tagFreightKind)).append( "</td><td class=\"data\">");
		//emailBuf.append(getUpdtValue( UnitHoldsAndPermissions)).append( "</td><td class=\"data\">");
		emailBuf.append( updateHolds).append( "</td><td class=\"data\">");
		emailBuf.append( getUpdtValue( tagDrayStatus)).append( "</td><td class=\"data\">");
		emailBuf.append( updCommodityDesc).append( "</td><td class=\"data\">");
		logger.info("In OLD Record - updCommodityDesc "+ updCommodityDesc);
		emailBuf.append( getUpdtValue( tagSpecialStow)).append( "</td><td class=\"data\">");
		emailBuf.append( getUpdtValue( tagRoutingGroup)).append( "</td><td class=\"data\">");
		emailBuf.append( "<-OLD</td>");
		emailBuf.append( "<tr><td class=\"data\">").append( _data.get( tagGoodsConsigneeName)==null?"":_data.get( tagGoodsConsigneeName)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagPOD)==null?"":_data.get( tagPOD)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagDestination)==null?"":_data.get( tagDestination)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagFreightKind)==null?"":_data.get( tagFreightKind)).append( "</td><td class=\"data\">");
		emailBuf.append(holds).append( "</td><td class=\"data\">");
		//emailBuf.append( _data.get( tagUnitHoldsAndPermissions)==null?"":_data.get( tagUnitHoldsAndPermissions)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagDrayStatus)==null?"":_data.get( tagDrayStatus)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagCommodityDescription)==null?"":_data.get( tagCommodityDescription)).append( "</td><td class=\"data\">");
		logger.info("In NEW Record - updCommodityDesc "+ _data.get( tagCommodityDescription));
		emailBuf.append( _data.get( tagSpecialStow)==null?"":_data.get( tagSpecialStow)).append( "</td><td class=\"data\">");
		emailBuf.append( _data.get( tagRoutingGroup)==null?"":_data.get( tagRoutingGroup)).append( "</td><td class=\"data\">");
		emailBuf.append( "<-NEW</td class=\"data\">");
		emailBuf.append( "</table>");
		emailBuf.append( "<font size=\"\" face=\"courier new\">");
		emailBuf.append( "<br><br>NOTES:<br><br>"); 
		emailBuf.append( _data.get( tagUnitRemark)==null?"":_data.get( tagUnitRemark)).append( "</p>");
        emailBuf.append( _data.get( tagEventNote)==null?"":"<BR>"+_data.get( tagEventNote));
        
		return emailBuf.toString();
	}

	public String getFromEmailAddr() {
			
		return "1aktosdevteam@matson.com";
	}

	public String getSubject() {
		StringBuffer buf = new StringBuffer();
		buf.append("Cargo Status Changes");
		if(_data.get( tagVesvoy)!=null) {
			buf.append(" ");
			buf.append(_data.get( tagVesvoy));
		}
		if(_data.get( tagUnitNbr)!=null) {
			buf.append(" : ");
			buf.append(_data.get( tagUnitNbr));
		}
		if(_data.get( tagLocation)!=null) {
			buf.append(" : LS=");
			buf.append(_data.get( tagLocation));
		}
		
		return buf.toString();
	}

	public String getToEmailAddr() {
		String emails = ",";
		logger.debug( "In getToEmailAddr.");
		if ( _data != null) {
			Set keys =  _data.keySet();
			Iterator iter = keys.iterator();
			String aKey = null;
			while ( iter.hasNext()) {
				aKey = (String)iter.next();
				//logger.debug( "aKey = " + aKey);
				if ( aKey.startsWith( CargoStatus.EMAIL_KEY_PREFIX)) {
					String tempEmail = (String)_data.get( aKey);
					if ( emails.indexOf( "," + tempEmail.trim() + ",") < 0)
						emails += _data.get( aKey) + ",";
					logger.debug( "email = " + emails);
				}
			}
		}
		if ( emails.startsWith( ","))
			emails = emails.substring( 1);
		if ( emails.endsWith( ","))
			emails = emails.substring( 0, emails.length()-1);
			
		return emails;
	}
	
	private String getUpdtValue( String tag) {
		String strValue = "";
		logger.info("In getUpdtValue tag is :"+tag+" -updated :"+updt + tag);
		if ( _data.get( updt + tag)!=null) {
			//logger.info("Value for updated tag "+_data.get( updt + tag));
			//return (String)_data.get( updt + tag);
			strValue = (String)_data.get( updt + tag);
			strValue = replaceSpecialCharacter(strValue, "\'", "&#39");
		}
		else if ( _data.get( tag) != null) {
			//logger.info("Value for tag is :"+_data.get( tag));
			//return (String)_data.get(tag);
			strValue = (String)_data.get(tag);
			strValue = replaceSpecialCharacter(strValue, "\'", "&#39");			
		}
		logger.info("strValue : "+strValue);
		return strValue;
	}
	
	
	/** To replace special characters
	 */
	  public String replaceSpecialCharacter(String input,String delimeter, String replacingString ) {
          // TODO Auto-generated method stub
		  logger.info("In replaceSpecialCharacter method ");
          StringTokenizer token = new StringTokenizer(input, delimeter);
          StringBuffer result = new StringBuffer();
          int noOfTokens = token.countTokens();
          int i = 0;
          while(token.hasMoreElements())
          {
                 result.append(token.nextToken());
                 if(i < noOfTokens - 1)
                 {
                       result.append(replacingString);
                 }
                 i++;
          }
          logger.info("In replaceSpecialCharacter method - result - " + result.toString());
          return result.toString();
   }
	 
	
	/**
	 * Log the msg to the database
	 */
	public void logMsg(String contents) {
		CargoStatusDao dao = null;
		try {
			String evtTime = (String)_data.get( tagEventTime);
			Date date = null;
			SimpleDateFormat sdf;
			try {
				sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
				date = sdf.parse( evtTime);
			} catch ( Exception ex) {
				
			}
			java.sql.Timestamp time = new java.sql.Timestamp(date.getTime());
			dao = new CargoStatusDao();
			logger.debug("CargoStatus insert log msg");
			dao.insertMsg((String)_data.get( tagUnitNbr),(String) _data.get( tagVesvoy),(String) _data.get( tagGoodsBlNbr), (String)_data.get( tagLocation), getToEmailAddr(), contents, (String)_data.get( tagEditedBy), time);
		} catch (Exception e) {
			
		} finally {
			if(dao != null) dao.close();
		}
		
	}

}
