<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company - Add Consignee - Phone List</title>
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
input[type=text] {
	float: left;
	font-size: 13px;
	font-family: Segoe UI;
	padding: 0px;
	border: solid 1px Gray;
	text-transform: uppercase;
}
td {
	font-weight:bold;
}

</Style>
<script type="text/javascript">
function clearForm(btn) {
	if(btn.value == "Clear") {
		document.formAddNoFaxConsignee.addConsignee.value = "";
		document.formAddNoFaxConsignee.addPhone.value = "";
		document.formAddNoFaxConsignee.addType.value = "";
		document.formAddNoFaxConsignee.addSpeed.value = "";
	}
	return false;
}
function validateForm(btn) {
	if(document.formAddNoFaxConsignee.addConsignee.value == "") {
		alert("ERROR: Consignee should not be blank");
		return false;
	}
	}

</script>
</head>

<body>
<br/>
	<form id="formAddNoFaxConsignee" name="formAddNoFaxConsignee" action="PhoneListAddConsignee.jsp" method="post" onSubmit="return validateForm()">
	<table align="center" cellpadding="0" cellspacing="0" style="border: solid 1px Gray;">
		<tr>
		<td>
		<table align="center" border="0" cellpadding="10">
			<tr height="50px" align="center" bgcolor="#275da7">
				<td colspan="4" style="color:#FFFFFF"><h2>Phone List - Add New Record</h2></td> 
			</tr>
			<tr height="50px">
				<td colspan="4"><b>Please enter the following details</b></td> 
			</tr>
			<tr>
				<td align="right"> Consignee Name : *</td><td><input type="text" id="addConsignee" name="addConsignee" maxLength="75" size="75"/></td>
			</tr>
			<tr>
				<td align="right"> Phone Number : </td><td><input type="text" id="addPhone" name="addPhone" maxLength="20" size="25"/></td>
			</tr>
			<tr>
				<td align="right" colspan="1"> Type : </td><td colspan="3"><input type="text" id="addType" name="addType" maxLength="2" size="10"/></td>
			</tr>
			<tr>
				<td align="right" colspan="1"> EXT # : </td><td colspan="3"><input type="text" id="addSpeed" name="addSpeed" maxLength="10" size="10"/></td>
			</tr>			
			<tr height="40px" align="center">
				<td colspan="4">
					<input type="submit" id="Save" name="Save" value="Save" />
					<input type="button" id="Clear" name="Clear" value="Clear" onclick="clearForm(this)"/>
					<td align="left"  width="10%"><input type="button" value="Back & Refresh" onClick="javascript:location.href = 'PhoneList.jsp';" /></td>
				</td>
			</tr>
			<tr>			
		</table>
		</td>
		</tr>
	</table>
	
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			 <%
				  String consigneeAdded= (String)request.getSession().getAttribute("consigneeAdded");
			  
				  if(consigneeAdded != null && consigneeAdded.equalsIgnoreCase("consigneeAdded"))
				  {
				%>
					  <tr>
					  	<td width="45%"></td>
					  	<td width="55%" align="right" style="color: green;text-align: left;">Consignee added Successfully </td>
					  </tr>
			  <%  }  if(consigneeAdded != null && consigneeAdded.equalsIgnoreCase("consigneeNotAdded"))
				  {
		      %>
						  <tr>
						  	<td width="25%"></td>
						  	<td width="75%" align="right" style="color: green;text-align: left;">ERROR: Consignee  not been added </td>
						  </tr>
			 <%  }%>

			</table>	
	
	</form>
</body>
</html>
