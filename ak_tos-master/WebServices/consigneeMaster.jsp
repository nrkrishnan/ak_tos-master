<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company</title>
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
#myDiv input {
	float: left;
	font-size: 13px;
	font-family: Segoe UI;
	font-weight: bold;
	padding: 2px;
	border: solid 1px #aacfe4;
	width: 200px;
	margin: 2px 0 20px 10px;
	text-align: center;
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
		var name = document.ConisgneeForm.consigneeName.value;
		var btnSelected =btn.value;
		//alert(btnSelected)
		//alert(name);
		if(btnSelected == "Search") {
		  if (name =="")
			{
				alert("Consignee Name cannot be blank.");
				return;
			}
			else
			{
				document.ConisgneeForm.submit();
			}
		}
		else {
			request.setAttribute("resultList", resultList);
			document.ConisgneeForm.submit();
		}
	}
	/*function clearValue()
	{
		var myTextField = document.getElementById('consigneeName');
		alert(myTextField.value);
		if(myTextField.value=="" || myTextField.value==null)
		{
			document.getElementById('consigneeName').value = "";
		}
	}*/
</script>
</head>

<%@ page import="javax.servlet.ServletException"%>
<%@ page import="javax.servlet.http.HttpServlet"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List"%>
<%@ page import="com.matson.cas.refdata.mapping.TosConsgineeTrucker"%>

<body>
<table border="0" cellpadding="0" cellspacing="0" width="430" align="center">
<tr bgcolor="#275da7" width=50%>
 <td>
	&nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="150" />
 </td>
 <td style="color:#FFFFFF">
	<h2>GUM Consignee/Truck</h2>
 </td>
</tr>
</table><br />
<form id="form" name="ConisgneeForm" action="ConsigneeMaster" method="post">
			<h1 align="center">GUM Consignee/Truck</h1>
			<p></p>
			<div class="spacer"></div>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="50%" align="right"><b>Consignee Name : </b></td>
					<td width="50%" align="left">
						<%String consigneeName = ""; 
						  request.getSession().removeAttribute("consigneeName");
						  consigneeName = request.getParameter("consigneeName");
						  if(consigneeName==null ||  consigneeName.equals(""))
						  {
							  consigneeName = "";
						  }
						%>
						&nbsp;&nbsp;<input type="text" id="consigneeName" name="consigneeName" value="<%=consigneeName%>" size="40" />
					</td>				
				</tr>
		  </table>	
				
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width ="55%" align="right"><input type="submit" id="Search" name="Search" value="Search" onclick="validateForm(this)"/></td>
					<td width ="45%"></td>
				</tr>
			</table>
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 <%
				  String isUpdate= (String)request.getSession().getAttribute("isUpdated");
			 	  String truckerCodeErrorMsg= (String)request.getSession().getAttribute("truckerCodeErrorMsg");
				  if(isUpdate!=null && isUpdate.equalsIgnoreCase("success"))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">Truckers are Successfully associated with the Consignee </td>
					  </tr>
				<% } if(isUpdate!=null && isUpdate.equalsIgnoreCase("failure"))
				{
					%>
						  <tr>
						  	<td width="45%"></td>
						  	<td width="55%" align="right" style="color: green;text-align: left;">Unable to Update the Truckers with the Consignee </td>
						  </tr>
				<% }  if(isUpdate!=null && isUpdate.equalsIgnoreCase("InvalidTrucker"))
				{
					%>
						  <tr>
						  	<td width="45%"></td>
						  	<td width="55%" align="right" style="color: green;text-align: left;">Trucker Code is not valid. </td>
						  </tr>
					<% }if(truckerCodeErrorMsg!=null && !truckerCodeErrorMsg.equals(""))
					{
						%>
							  <tr>
							  	<td width="45%"></td>
							  	<td width="55%" align="right" style="color: green;text-align: left;"><%=truckerCodeErrorMsg %></td>
							  </tr>
						<% }
				%>
			</table>	
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 <%
				  List resultList1= (List)request.getAttribute("resultList");
				  if(resultList1!=null && (resultList1.isEmpty() || resultList1.size()==0))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">No Records Found </td>
					  </tr>
				<% } %>
			</table>	
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table  width="50%" border="1" cellpadding="1" cellspacing="0" align="center">
				<tr>
						<td width="50%" align="center"><b>Consignee Name</b></td>
						<td width="50%"  align="center" ><b>Trucker Code</b></td>
				</tr>
				<%
				  List resultList= (List)request.getSession().getAttribute("resultList");
				ArrayList<String> truckerCodeList = new ArrayList<String>();
				  if(resultList!=null && !resultList.isEmpty())
				  {
				  	Iterator it = resultList.iterator();
				  	while(it.hasNext())
				  	{
					  TosConsgineeTrucker tosConsgineeTrucker = new TosConsgineeTrucker();
					  tosConsgineeTrucker = (TosConsgineeTrucker)it.next();
					  if(tosConsgineeTrucker!=null && tosConsgineeTrucker.getTruckerCode()==null)
					  {
						  tosConsgineeTrucker.setTruckerCode("");
					  }
					  session.setAttribute("resultList",resultList);
					  truckerCodeList.add("truckerCode");
					  session.setAttribute("truckerCodeList",truckerCodeList);
					%>
				
						 <tr>
							<td width="25%"><%=tosConsgineeTrucker.getConsigneeName()%>
							<td width="25%"><input type="text" name="truckerCode" id="truckerCode" maxlength="4" value="<%=tosConsgineeTrucker.getTruckerCode()%>"/></td>
						</tr>
					<%} 
				  		
				  }%>
		    </table>
			
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<tr>
						<td align="right" width="55%"><input type="submit" id="Save" name="Save" value="Save" onclick="validateForm(this)"/></td>
						<td align="right" width="45%"></td>
			</tr>
			</table>
			
			
	<br />
</form>
</body>
</html>