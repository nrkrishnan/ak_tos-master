<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company - Phone List</title>
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
		var name = document.NoFaxConsigneeForm.inputConsigneeName.value;
		var btnSelected =btn.value;
		if(btnSelected == "Search") {
		  if (name =="")
			document.NoFaxConsigneeForm.submit();
		}
		else {
			request.setAttribute("consigneeList", consigneeList);
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
<%@ page import="com.matson.cas.refdata.mapping.TosNoFaxConsigneeMt"%>

<body>
<table border="0" cellpadding="5" cellspacing="0" width="800px" align="center">
<tr bgcolor="#275da7" height="55px">
 <td>
	&nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="150" />
 </td>
 <td style="color:#FFFFFF" align="right" >
	<h1 style="margin-right:50px;">Phone List</h1>
 </td>
</tr>
</table><br />
<form id="form" name="NoFaxConsigneeForm" action="PhoneList.jsp" method="post">
			<h1 align="center"></h1>
			<p></p>
			<div class="spacer"></div>
			</br>
			<table align="center" width="800px" border="0" cellpadding="0" cellspacing="1">
				<tr>
	
					<td align="right"><b>Consignee Name :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					<td align="left">
					  <%String inputConsigneeName = ""; 
					  inputConsigneeName = request.getParameter("inputConsigneeName");
						  if(inputConsigneeName==null ||  inputConsigneeName.equals(""))
						  {
							  inputConsigneeName = "";
						  }
						%>
					 <input type="text" id="inputConsigneeName" maxlength="75" name="inputConsigneeName" value="<%=inputConsigneeName%>" size="20" style="text-transform: uppercase" /></td>
				</tr>
				<tr></tr><tr></tr>
				<tr>
					 <td align="right"><b>Phone Number :&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
					 <td align="left">
					  <%String inputPhoneNumber = ""; 
					   	  inputPhoneNumber = request.getParameter("inputPhoneNumber");
						  if(inputPhoneNumber==null ||  inputPhoneNumber.equals(""))
						  {
							  inputPhoneNumber = "";
						  }
						%>
					 <input type="text" id="inputPhoneNumber" maxlength="20" name="inputPhoneNumber" value="<%=inputPhoneNumber%>" size="20" /></td>
				</tr>
		  </table>		
				
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="3" cellspacing="0">
				<tr>
					<td align="right" width="50%"><input type="submit" id="Search" name="Search" value="Search" onclick="validateForm(this)"/></td>
					<td align="center"><input type="submit" id="Save" name="Save" value="Save" onclick="validateForm(this)"/></td>
					<td align="left"><input type="submit" id="Save & Email Report" name="Save & Email Report" value="Save & Email Report" onclick="validateForm(this)"/></td>
					<td align="left"><input type="submit" id="Delete" name="Delete" value="Delete" onclick="validateForm(this)"/></td>
					<td align="left"  width="50%"><input type="button" value="Add Consignee" onClick="javascript:location.href = 'PhoneListAddConsignee.jsp';" /></td>
				</tr>
			</table>
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 <%

				  String consigneeUpdated= (String)request.getSession().getAttribute("consigneeUpdated");
				  if(consigneeUpdated!=null && consigneeUpdated.equalsIgnoreCase("Deleted"))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">Consignee Deleted Successfully </td>
					  </tr>
			  <%  }  if(consigneeUpdated!=null && consigneeUpdated.equalsIgnoreCase("ReportGenerated"))
	              {
              %>
					  <tr>
						<td width="40%"></td>
					    <td width="60%" align="right" style="color: green;text-align: left;">Phone List Report has been generated Successfully </td>
					  </tr>
			  <%  } if(consigneeUpdated!=null && consigneeUpdated.equalsIgnoreCase("ReportNotGenerated"))
				  {
		      %>
						  <tr>
						  	<td width="40%"></td>
						  	<td width="60%" align="right" style="color: green;text-align: left;">Unable to generate the Phone List Report</td>
						  </tr>
			 <%  } if(consigneeUpdated!=null && consigneeUpdated.equals("Updated"))
			     {
			  %>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">Consignee Updated Successfully </td>
					  </tr>
		      <% } %>

			</table>			
			
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			
			<table  width="50%" border="1" cellpadding="1" cellspacing="0" align="center">
				<tr>
						<td><b>Consignee Name</b></td>
						<td><b>Phone Number</b></td>
						<td><b>Type</b></td>
						<td><b>EXT #</b></td>
						<td><b>Delete</b></td>
				</tr>
			
				<%
				  List consigneeList= (List)request.getSession().getAttribute("consigneeList");
				  if(consigneeList!=null && !consigneeList.isEmpty())
				  {
				  	Iterator it = consigneeList.iterator();
				  	while(it.hasNext())
				  	{
				  	  TosNoFaxConsigneeMt noFaxConsigneeMt = new TosNoFaxConsigneeMt();
				  	  noFaxConsigneeMt = (TosNoFaxConsigneeMt)it.next();
				  	  if(noFaxConsigneeMt!=null && noFaxConsigneeMt.getPhone()==null)
					  {
						  noFaxConsigneeMt.setPhone("");
					  }
				  	  if(noFaxConsigneeMt!=null && noFaxConsigneeMt.getType()==null)
					  {
						  noFaxConsigneeMt.setType("");
					  }	
					  if(noFaxConsigneeMt!=null && noFaxConsigneeMt.getSpeed()==null)
					  {
						  noFaxConsigneeMt.setSpeed("");
					  }	
				  	  				  
					  session.setAttribute("consigneeList",consigneeList);
					%>
				
						<tr>

							<td width="68%"><input type="text" size="75" name="consigneeName" id="consigneeName" maxlength="75" value="<%=noFaxConsigneeMt.getConsigneeName() %>"/></td>
							<td width="9%"><input type="text" size="20" name="phoneNumber" id="phoneNumber" maxlength="20"  value="<%=noFaxConsigneeMt.getPhone() %>"/></td>
							<td width="8%"><input type="text" size="8" name="type" id="type" maxlength="2"  value="<%=noFaxConsigneeMt.getType() %>"/></td>
							<td width="8%"><input type="text" size="10" name="speed" id="speed" maxlength="10"  value="<%=noFaxConsigneeMt.getSpeed() %>"/></td>
							<td width="7%"><input type="checkbox" size="10" name="Delete" id="Delete" maxlength="10" value="<%=noFaxConsigneeMt.getConsigneeId() %>"/></td>							
						</tr>
					<%} 
				  		
				  }%>			
		    </table>
				
	<br />
</form>
</body>
</html>