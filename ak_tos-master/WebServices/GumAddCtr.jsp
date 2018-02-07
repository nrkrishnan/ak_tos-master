<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company - Guam - Add Ctr</title>
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
		document.frmAddNewRDSRecord.addCtrNbr.value = "";
		document.frmAddNewRDSRecord.addChkDgt.value = "";
		document.frmAddNewRDSRecord.addVesvoy.value = "";
		document.frmAddNewRDSRecord.addBkgNbr.value = "";
		document.frmAddNewRDSRecord.addCneeName.value = "";
		document.frmAddNewRDSRecord.addShprName.value = "";
		document.frmAddNewRDSRecord.addTruck.value = "";
		document.frmAddNewRDSRecord.addRemarks.value = "";
		document.frmAddNewRDSRecord.addLPort.value = "";
		document.frmAddNewRDSRecord.addCell.value = "";
		document.frmAddNewRDSRecord.addCWeight.value = "";
		document.frmAddNewRDSRecord.addTWeight.value = "";
	}
	return false;
}
function validateForm(btn) {
	if(document.frmAddNewRDSRecord.addVesvoy.value == "") {
		alert("ERROR: Vesvoy should not be blank");
		return false;
	}
	else if(!document.frmAddNewRDSRecord.addVesvoy.value.match("[A-Za-z]{3}[0-9]{3}")) {
		alert("ERROR: Invalid Vesvoy");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCtrNbr.value == "") {
		alert("ERROR: Container number should not be blank");
		return false;
	}
	else if(!document.frmAddNewRDSRecord.addCtrNbr.value.match("[A-Za-z]{4}[0-9]{1,6}")) {
		alert("ERROR: Invalid Container Number");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addChkDgt.value == "") {
		alert("ERROR: Check digit should not be blank");
		return false;
	}
	else if(!document.frmAddNewRDSRecord.addChkDgt.value.match("[0-9]{1}") && document.frmAddNewRDSRecord.addChkDgt.value.toUpperCase() != "X") {
		alert("ERROR: Invalid Check digit");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addBkgNbr.value == "") {
		alert("ERROR: Booking Number should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addBkgNbr.value.match("[^0123456789]")) {
		alert("ERROR: Invalid Booking Number");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCneeName.value == "") {
		alert("ERROR: Consignee Name should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addShprName.value == "") {
		alert("ERROR: Shipper Name should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addLPort.value == "") {
		alert("ERROR: Load Port should not be blank");
		return false;
	}
	else if(!document.frmAddNewRDSRecord.addLPort.value.match("[A-Za-z]{3}")) {
		alert("ERROR: Invalid Load Port");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCell.value == "") {
		alert("ERROR: Cell Position should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCell.value.match("[^0123456789]")) {
		alert("ERROR: Invalid Cell Position");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCWeight.value == "") {
		alert("ERROR: Gross Weight should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addCWeight.value.match("[^0123456789]")) {
		alert("ERROR: Invalid Gross Weight");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addTWeight.value == "") {
		alert("ERROR: Tare Weight should not be blank");
		return false;
	}
	else if(document.frmAddNewRDSRecord.addTWeight.value.match("[^0123456789]")) {
		alert("ERROR: Invalid Tare Weight");
		return false;
	}
}

</script>
</head>

<body>
<br/>
	<form id="frmAddNewRDSRecord" name="frmAddNewRDSRecord" action="GumAddCtr" method="post" onSubmit="return validateForm()">
	<table align="center" cellpadding="0" cellspacing="0" style="border: solid 1px Gray;">
		<tr>
		<td>
		<table align="center" border="0" cellpadding="10">
			<tr height="50px" align="center" bgcolor="#275da7">
				<td colspan="4" style="color:#FFFFFF"><h2>Guam Newves - Add New Record</h2></td> 
			</tr>
			<tr height="50px">
				<td colspan="4"><b>Please enter the following details</b></td> 
			</tr>
			<tr>
				<td align="right"> Container Number : *</td><td><input type="text" id="addCtrNbr" name="addCtrNbr" maxLength="10" size="25"/></td>
				<td align="right"> Check digit : *</td><td><input type="text" id="addChkDgt" name="addChkDgt" maxLength="1" size="25"/></td>
			</tr>
			<tr>
				<td align="right"> Vesvoy : *</td><td><input type="text" id="addVesvoy" name="addVesvoy" maxLength="6" size="25"/></td>
				<td align="right"> Booking Number : *</td><td><input type="text" id="addBkgNbr" name="addBkgNbr" maxLength="10" size="25"/></td>
			</tr>
			<tr>
				<td align="right"> Consignee Name : *</td><td><input type="text" id="addCneeName" name="addCneeName" maxLength="35" size="25"/></td>
				<td align="right"> Shipper Name : *</td><td><input type="text" id="addShprName" name="addShprName" maxLength="35" size="25"/></td>
			</tr>
			<tr>
				<td align="right"> Truck : </td><td><input type="text" id="addTruck" name="addTruck" maxLength="4" size="25"/></td>
			</tr>
			<tr>
				<td align="right" colspan="1"> Remarks : </td><td colspan="3"><input type="text" id="addRemarks" name="addRemarks" maxLength="65" size="78"/></td>
			</tr>
			<tr>
				<td align="right"> Load Port : *</td><td><input type="text" id="addLPort" name="addLPort" maxLength="3" size="25"/></td>
				<td align="right"> Cell : *</td><td><input type="text" id="addCell" name="addCell" maxLength="10" size="25"/></td>
			</tr>
			<tr>
				<td align="right"> Gross Weight(LB) : *</td><td><input type="text" id="addCWeight" name="addCWeight" maxLength="7" size="25"/></td>
				<td align="right"> Tare Weight(LB) : *</td><td><input type="text" id="addTWeight" name="addTWeight" maxLength="7" size="25"/></td>
			</tr>
			<tr>
				<td> </td> <td> * -- required fields</td>
			</tr>
			<tr height="40px" align="center">
				<td colspan="4">
					<input type="submit" id="Save" name="Save" value="Save" />
					<input type="button" id="Clear" name="Clear" value="Clear" onclick="clearForm(this)"/>
				</td>
			</tr>
			<tr>
				<td colspan="4" style="font-family:Courier New">
					<div id="result">${requestScope["message"]}</div>
				</td>
			</tr>
		</table>
		</td>
		</tr>
	</table>
	</form>
</body>
</html>
