<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company - Guam - Assign Code</title>
<Style>
body {
	font-family: Segoe UI, Verdana, Arial;
	font-size: 12px;
}

p,h1,form,button {
	border: 0;
	margin: 0;
	padding: 0;
}

.spacer {
	clear: both;
	height: 1px;
}

.myform {
	margin: 0 auto;
	width: 400px;
	padding: 14px;
}

#myDiv {
	border: solid 1px #b7ddf2;
}

#myDiv h1 {
	font-size: 14px;
	font-weight: bold;
	margin-bottom: 8px;
}

#myDiv p {
	font-size: 11px;
	color: #666666;
	margin-bottom: 20px;
	border-bottom: solid 1px #b7ddf2;
	padding-bottom: 10px;
}

#myDiv label {
	display: block;
	font-weight: bold;
	text-align: right;
	width: 140px;
	float: left;
	margin-top: 5px;
}
#label1 {
	display: block;
	font-weight: bold;
	text-align: right;
	width: 330px;
	float: left;
	margin-top: 5px;
}
input[type=text] {
	float: left;
	font-size: 13px;
	font-family: Segoe UI;
	padding: 0px;
	border: solid 1px Gray;
	text-transform: uppercase;
}

#myDiv input[type=checkbox] {
	width: auto;
	border: 0px;
}
#myDiv input[type=submit] {
	text-align: center;
	width : 100px;
	margin-left: 150px;
	margin-top: 10px;
	text-transform: none;
	font-weight: normal;
}
#myDiv input[disabled='disabled'] {
	border: solid 1px #eeeeee;
}

</Style>
<script type="text/javascript">
	function validateForm(btn)
	{
		var name = document.AssignCodeForConsigneeForm.vvd.value;
		var btnSelected =btn.value;
		//alert(btnSelected)
		//alert(name);
		if(btnSelected == "Search") {
		  if (name =="")
			{
				alert("VVD cannot be blank.");
				return true;
			}
			else
			{
				document.AssignCodeForConsigneeForm.submit();
			}
		}
		else {
			request.setAttribute("vvdInfoList", vvdInfoList);
			document.AssignCodeForConsigneeForm.submit();
		}
	}
	
</script>
</head>

<%@ page import="javax.servlet.ServletException"%>
<%@ page import="javax.servlet.http.HttpServlet"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="com.matson.cas.refdata.mapping.TosGumRdsDataFinalMt"%>

<body>
<%!
	private String typeCodeConversion(TosGumRdsDataFinalMt gumRdsDataFinalMt){
	
	String hgt = gumRdsDataFinalMt.getHgt();
	String tCode = gumRdsDataFinalMt.getTypeCode();
	String convertedTypeCode = null;
	String clazz = null;
	
	if(tCode != null)
	{
		if(tCode.substring(0, 1).equalsIgnoreCase("C"))
			clazz = ("CHS");
		else
			clazz = ("CTR");
	}
	
	if( hgt!=null && hgt.length()>4 )
	{
		hgt = hgt.substring(0, 4);
		if(!isNumber(hgt.substring(0, 2)) || !isNumber(hgt.substring(2, 4)))
		{
			if(tCode!=null && tCode.equals("UNKNOWN") && clazz.equals("CTR"))
				convertedTypeCode = "UNKN";
			else if(tCode!=null && tCode.equals("UNKNOWN") && clazz.equals("CHS"))
				convertedTypeCode = ("UNK");
			else
				convertedTypeCode = (tCode.length()>3?tCode.substring(0, 3):tCode);
		}
		else
		{
			int hgtinch;
			String temp1 = hgt.substring(0, 2);
			String temp2 = hgt.substring(2, 4);
			hgtinch = Integer.parseInt(temp1) * 12 + Integer.parseInt(temp2);
			if(tCode != null)
			{
				if(tCode.equals("UNKNOWN"))
				{
					if(clazz.equals("CHS"))
						convertedTypeCode =("UNK");
					else
						convertedTypeCode =("UNKN");
				}
				else if(tCode.substring(0,1).equals("A"))
				{
					if(hgtinch <= 138)
						convertedTypeCode =(tCode.substring(0, 3) + "L");
					else if(hgtinch >= 152)
						convertedTypeCode =(tCode.substring(0, 3) + "H");
					else
						convertedTypeCode =(tCode.substring(0, 3));
				}
				else if(tCode.substring(0,1).equals("F"))
				{
					if(hgt.equals("1300"))
						convertedTypeCode =(tCode.substring(0, 3) + "M");
					else if(hgtinch <= 96)
						convertedTypeCode =(tCode.substring(0, 3) + "L");
					else if(hgtinch > 102)
						convertedTypeCode =(tCode.substring(0, 3) + "H");
					else
						convertedTypeCode =(tCode.substring(0, 3));
				}
				else if(tCode.substring(0,1).equals("R"))
				{
					if(hgtinch <= 96)
						convertedTypeCode =(tCode.substring(0, 3) + "L");
					else if(hgtinch > 102){
						convertedTypeCode =(tCode.substring(0, 3) + "H");
						//System.out.println("hgtinch::::"+hgtinch);
					}
					
					else{
						convertedTypeCode =(tCode.substring(0, 3));
						//System.out.println("hgtinch::::"+hgtinch);
					}
				}
				else
				{
					if(hgtinch <= 96)
						convertedTypeCode =(tCode.substring(0, 3) + "L");
					else if(hgtinch > 102)
						convertedTypeCode =(tCode.substring(0, 3) + "H");
					else
						convertedTypeCode =(tCode.substring(0, 3));
				}
				
				//System.out.println("convertedTypeCode::::"+convertedTypeCode);
				String subGrp = "";
				String temp = tCode.substring(6, 8);
				if(temp.equals("4V"))
					subGrp = "4V";
				else if(temp.equals("CL"))
					subGrp = "CL";
				else if(temp.equals("DV"))
					subGrp = "DV";
				else if(temp.equals("FC"))
					subGrp = "FC";
				else if(temp.equals("GB"))
					subGrp = "GB";
				else if(temp.equals("GR"))
					subGrp = "GR";
				else if(temp.equals("GY"))
					subGrp = "GY";
				else if(temp.equals("H3"))
					subGrp = "H3";
				else if(temp.equals("H4"))
					subGrp = "H4";
				else if(temp.equals("HG"))
					subGrp = "HG";
				else if(temp.equals("CA"))
					subGrp = "CA";
				else if(temp.equals("VO"))
					subGrp = "VO";
				
				if(!subGrp.equals(""))
				{
						if(tCode.length() == 3)
							convertedTypeCode = (convertedTypeCode + " " + subGrp);
						else
							convertedTypeCode = (convertedTypeCode+ subGrp);
				}			
			}
		}
	}
	//System.out.println("Final convertedTypeCode::::"+convertedTypeCode);

	return convertedTypeCode;
}

private boolean isNumber(String num)
{
	try {
		Integer.parseInt(num);
	}
	catch(NumberFormatException ex)
	{
		return false;
	}
	return true;
}

%>
<table border="0" cellpadding="5" cellspacing="0" width="800px" align="center">
<tr bgcolor="#275da7" height="55px">
 <td>
	&nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="150" />
 </td>
 <td style="color:#FFFFFF" align="right" >
	<h1 style="margin-right:50px;">Assigning Codes for Consignees</h1>
 </td>
</tr>
</table><br />
<form id="form" name="AssignCodeForConsigneeForm" action="AssignCodeForConsignee" method="post">
			<h1 align="center"></h1>
			<p></p>
			<div class="spacer"></div>
			</br>
			<table align="center" width="800px" border="0" cellpadding="0" cellspacing="1">
				<tr>
					<td align="right"><b>Enter Vesvoy :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					<td align="left">
					   <%String vvd = ""; 
					   	  vvd = request.getParameter("vvd");
						  if(vvd==null ||  vvd.equals(""))
						  {
							  vvd = "";
						  }
						%>
						<input type="text" id="vvd" name="vvd" maxlength="6" value="<%=vvd%>" size="20" style="text-transform: uppercase"/>
					</td>		
					<td align="right"><b>Consignee Name :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					<td align="left">
					  <%String inputConsigneeName = ""; 
					  inputConsigneeName = request.getParameter("inputConsigneeName");
						  if(inputConsigneeName==null ||  inputConsigneeName.equals(""))
						  {
							  inputConsigneeName = "";
						  }
						%>
					 <input type="text" id="inputConsigneeName" maxlength="35" name="inputConsigneeName" value="<%=inputConsigneeName%>" size="35" style="text-transform: uppercase" /></td>
				</tr>
				<tr></tr><tr></tr>
				<tr>
					 <td align="right"><b>Container Number :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					 <td align="left">
					  <%String inputContainerNumber = ""; 
					   	  inputContainerNumber = request.getParameter("inputContainerNumber");
						  if(inputContainerNumber==null ||  inputContainerNumber.equals(""))
						  {
							  inputContainerNumber = "";
						  }
						%>
					 <input type="text" id="inputContainerNumber" maxlength="10" name="inputContainerNumber" value="<%=inputContainerNumber%>" size="20" style="text-transform: uppercase" /></td>
					 <td align="right"><b>Booking Number :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					 <td align="left">
					  <%String inputBookingNumber = ""; 
					   	  inputBookingNumber = request.getParameter("inputBookingNumber");
						  if(inputBookingNumber==null ||  inputBookingNumber.equals(""))
						  {
							  inputBookingNumber = "";
						  }
						%>
					 <input type="text" id="inputBookingNumber" maxlength="10" name="inputBookingNumber" value="<%=inputBookingNumber%>" size="20" /></td>
				</tr>
		  </table>		
				
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="3" cellspacing="0">
				<tr>
					<td align="right" width="50%"><input type="submit" id="Search" name="Search" value="Search" onclick="validateForm(this)"/></td>
					<td align="center"><input type="submit" id="Save" name="Save" value="Save" onclick="validateForm(this)"/></td>
					<td align="left"><input type="submit" id="SaveAndProcessNewVes" name="SaveAndProcessNewVes" value="SaveAndProcessNewVes" onclick="validateForm(this)"/></td>
					<td align="left"><input type="submit" id="DownloadVessel" name="DownloadVessel" value="DownloadVessel" onclick="validateForm(this)"/></td>
					<td align="left"  width="50%"><input type="button" value="Add Container" onClick="javascript:location.href = 'GumAddCtr.jsp';" /></td>
				</tr>
			</table>
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 	<%
				  List vvdInfoList1= (List)request.getAttribute("vvdInfoList");
				  if(vvdInfoList1!=null && (vvdInfoList1.isEmpty() || vvdInfoList1.size()==0))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: #FF0000;text-align: left;">No Records Found / RDS process is not done for VVD. </td>
					  </tr>
				<% } %>
			</table>	
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 <%
				  String vvdUpdated= (String)request.getSession().getAttribute("vvdUpdated");
			 	  String assignCodeErrorMsg= (String)request.getSession().getAttribute("assignCodeErrorMsg");
			 	  String isValidVVD= (String)request.getSession().getAttribute("isValidVVD");
			 	  
				  if(vvdUpdated!=null && vvdUpdated.equalsIgnoreCase("success"))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">Truckers are Successfully associated with the Consignee </td>
					  </tr>
				<% } if(vvdUpdated!=null && vvdUpdated.equalsIgnoreCase("failure"))
				{
					%>
						  <tr>
						  	<td width="45%"></td>
						  	<td width="55%" align="right" style="color: green;text-align: left;">Unable to Update the Truckers with the Consignee </td>
						  </tr>
			  <%  }  if(vvdUpdated!=null && vvdUpdated.equalsIgnoreCase("ReportGenerated"))
					{
						%>
							  <tr>
							  	<td width="45%"></td>
							  	<td width="55%" align="right" style="color: green;text-align: left;">GUM Vessel Report has been generated Successfully </td>
							  </tr>
				<% } if(vvdUpdated!=null && vvdUpdated.equalsIgnoreCase("ReportNotGenerated"))
				{
					%>
						  <tr>
						  	<td width="45%"></td>
						  	<td width="55%" align="right" style="color: green;text-align: left;">Unable to generate the GUM Vessel Report</td>
						  </tr>
			<% } if(assignCodeErrorMsg!=null && !assignCodeErrorMsg.equals(""))
			{
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;"><%=assignCodeErrorMsg %></td>
					  </tr>
		<% } if(vvdUpdated!=null && vvdUpdated.equalsIgnoreCase("InvalidTrucker"))
				{
					%>
						  <tr>
						  	<td width="45%"></td>
						  	<td width="55%" align="right" style="color: green;text-align: left;">Trucker Code is not valid. </td>
						  </tr>
					<% } if(isValidVVD!=null && isValidVVD.equalsIgnoreCase("Invalid"))
					{
						%>
							  <tr>
							  	<td width="45%"></td>
							  	<td width="55%" align="right" style="color: green;text-align: left;">VVD is not valid</td>
							  </tr>
				<% } %>
			</table>	
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			
			<table  width="50%" border="1" cellpadding="1" cellspacing="0" align="center">
				<tr>
						<td><b>Container Number</b></td>
						<td><b>Booking Number</b></td>
						<td><b>Consignee Name</b></td>
						<td><b>Trucker</b></td>
						<td><b>Shipper Name</b></td>
						<td><b>Remarks</b></td>
						<td><b>Discharge Port</b></td>
						<td><b>DPort</b></td>
						<td><b>Seal #</b></td>
						<td><b>DS</b></td>
						<td><b>Weight</b></td>
						<td><b>Cube</b></td>
				</tr>
			
				<%
				  List vvdInfoList= (List)request.getSession().getAttribute("vvdInfoList");
				  if(vvdInfoList!=null && !vvdInfoList.isEmpty())
				  {
				  	Iterator it = vvdInfoList.iterator();
				  	while(it.hasNext())
				  	{
				  	  TosGumRdsDataFinalMt gumRdsDataFinalMt = new TosGumRdsDataFinalMt();
				  	  gumRdsDataFinalMt = (TosGumRdsDataFinalMt)it.next();
				  	  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getTruck()==null)
					  {
						  gumRdsDataFinalMt.setTruck("");
					  }
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getConsignee()==null)
					  {
						  gumRdsDataFinalMt.setConsignee("");
					  }
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getDischargePort()==null)
					  {
						  gumRdsDataFinalMt.setDischargePort("");
					  }
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getDport()==null)
					  {
						  gumRdsDataFinalMt.setDport("");
					  }
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getDs()==null)
					  {
						  gumRdsDataFinalMt.setDs("");
					  }
					  
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getBookingNumber()==null)
					  {
						  gumRdsDataFinalMt.setBookingNumber("");
					  }
					  
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getShipper()==null)
					  {
						  gumRdsDataFinalMt.setShipper("");
					  }
					  
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getCargoNotes()==null)
					  {
						  gumRdsDataFinalMt.setCargoNotes("");
					  }
					  
					  if(gumRdsDataFinalMt!=null && gumRdsDataFinalMt.getSealNumber()==null)
					  {
						  gumRdsDataFinalMt.setSealNumber("");
					  }
					  
					  session.setAttribute("vvdInfoList",vvdInfoList);
					%>
				
						<!--  <tr>
							<td width="20%"><input type="text" size="40"  readonly="readonly" value="<%=gumRdsDataFinalMt.getContainerNumber()%>">
							<td width="20%"><input type="text" size="40"  readonly="readonly" value="<%=gumRdsDataFinalMt.getConsigneeName() %>"/></td>
							<td width="20%"><input type="text" size="10"  name="trucker" id="trucker"  value="<%=gumRdsDataFinalMt.getTruck() %>"/></td>
							<td width="20%"><input type="text" size="10"  readonly="readonly" value="<%=gumRdsDataFinalMt.getDport() %>"/></td>
							<td width="20%"><input type="text" size="10"  readonly="readonly" value="<%=gumRdsDataFinalMt.getDs() %>"/></td>
						</tr>   -->
						<tr>
							<td width="20%"><%=gumRdsDataFinalMt.getContainerNumber()%></td>
							<td width="20%"><input type="text" size="10" name="bookingNumber" id="bookingNumber" maxlength="7"  value="<%=gumRdsDataFinalMt.getBookingNumber() %>"/></td>
							<td width="20%"><input type="text" size="40" name="consigneeName" id="consigneeName" maxlength="35" value="<%=gumRdsDataFinalMt.getConsignee() %>"/></td>
							<td width="20%"><input type="text" size="6" maxlength="4"  name="trucker" id="trucker"  value="<%=gumRdsDataFinalMt.getTruck() %>"/></td>
							<td width="20%"><input type="text" size="40" maxlength="35"  name="shipperName" id="shipperName"  value="<%=gumRdsDataFinalMt.getShipper() %>"/></td>
							<td width="20%"><input type="text" size="40" maxlength="35"  name="remarks" id="remarks"  value='<%=gumRdsDataFinalMt.getCargoNotes() %>'/></td>
							<td width="20%"><input type="text" size="6" maxlength="4"  name="dischargePort" id="dischargePort"  value="<%=gumRdsDataFinalMt.getDischargePort() %>"/></td>
							<td width="20%"><input type="text" size="6" maxlength="4"  name="dport" id="dport"  value="<%=gumRdsDataFinalMt.getDport() %>"/></td>
							<td width="20%"><input type="text" size="10" maxlength="10"  name="sealNumber" id="sealNumber"  value="<%=gumRdsDataFinalMt.getSealNumber() %>"/></td> 
							<td width="20%"><%=gumRdsDataFinalMt.getDs() %></td>
							<td width="20%"><%=gumRdsDataFinalMt.getCweight() %></td>
							<%
							String formattedCubeValue = typeCodeConversion(gumRdsDataFinalMt);
							%>
							<td width="20%"><%=formattedCubeValue!=null?formattedCubeValue:"" %></td>							
						
						</tr>
					<%} 
				  		
				  }%>			
		    </table>
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<tr>
						<td align="right" width="50%"><input type="submit" id="Save" name="Save" value="Save" onclick="validateForm(this)"/></td>
						<td align="left" ><input type="submit" id="SaveAndProcessNewVes" name="SaveAndProcessNewVes" value="SaveAndProcessNewVes" onclick="validateForm(this)"/></td>			
						<td align="left" width="50%"><input type="submit" id="DownloadVessel" name="DownloadVessel" value="DownloadVessel" onclick="validateForm(this)"/></td>
			</tr>
			</table>		
				
	<br />
</form>
</body>
</html>