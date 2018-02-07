<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Honolulu - Matson Navigation Company</title>
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
	
	function validateForm() {
		if (document.formLongHaul.chkEnterVvd.checked) {
			if (document.formLongHaul.vvd.value == "") {
				alert("VVD should not be blank");
				document.formLongHaul.vvd.focus();
				return false;
			} else {
				var vvd = document.formLongHaul.vvd.value;
				if (!vvd.match("[A-Za-z]{3}[0-9]{3}[Ww]{1}")) {
					alert("Invalid vvd");
					document.formLongHaul.vvd.focus();
					return false;
				}
			}
		}
	}

	function toggleFields(chk) {
		document.formLongHaul.vvd.disabled = !chk.checked;
	}
</script>
</head>
<body>
<table border="0" cellpadding="0" cellspacing="0" width="430" align="center">
<tr bgcolor="#275da7" width=50%>
 <td>
	&nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="200" />
 </td>
 <td style="color:#FFFFFF" cellspacing="15">
	<h2>Honolulu Newves</h2>
 </td>
</tr>
</table><br />
<form id="form" name="formLongHaul" action="HonNewVes" method="post" onSubmit="return validateForm()">
	<div id="myDiv" class="myform">
			<h1>LongHaul vessel</h1>
			<p></p>
			<label id="label1" for="chkEnterVvd" style="width: 300px;">Would like to execute new vessel by entering vvd </label>
			<input type="checkbox" id="chkEnterVvd" name="chkEnterVvd" onclick="toggleFields(this)"/>
			<label>V V D </label> <input type="text" name="vvd" id="vvd" maxlength="7" disabled="disabled"/> 
			<label for="copyPrimary" >Would like to copy primary output </label><input type="checkbox" id="copyPrimary" name="copyPrimary" />
			<label for="copySupplement" >Would like to copy supplemental output </label><input type="checkbox" id="copySupplement" name="copySupplement" />
			<input type="submit" name="newves" id="newves" value="Start"/>
			<div class="spacer"></div>
	</div>
	<br />
	<div id="myDiv" class="myform">
			<h1>Barge</h1>
			<p></p>
			<label for="copyBarge" style="width:auto">Would like to copy barge output </label><input type="checkbox" id="copyBarge" name="copyBarge" />
			<input type="submit" name="barge" id="barge" value="Start"/>
			<div class="spacer"></div>
	</div>
</form>
</body>
</html>