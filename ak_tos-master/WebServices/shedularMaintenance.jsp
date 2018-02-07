<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company</title>
<script type="text/javascript">
</script>
</head>
<body>
<form name="formShedMaint" action="ShedulerMaintenance" method="post">
<table border="0" width="70%" align="center">
	<tr>
		<td>
			<table border="1" cellpadding="0" cellspacing="0" width="70%" align="center">
	  			<tr>
						<td bgcolor="#BFBFBF" colspan="2" align="center" cellspacing='15'><h2>Scheduler Maintenance</h2> </td>
				 </tr>
   			</table>
   		</td>
   	</tr> 
	<tr>
		<td>
			  <table border="1" cellpadding="0" cellspacing="0" width="70%" align="center">
				    <tr>
						<td height="100%" colspan="3" >
								<tr height="20%"> 
									<td align="center">&nbsp</td>
									<td align="center"><font font size="2" face="Georgia, Arial">On</font></td>
									<td align="center"><font font size="2" face="Georgia, Arial">Off</font></td>
									</td>
								</tr>
								<tr height="20%">
									<td align="center"><font font size="2" face="Georgia, Arial">Stowplan Scheduler </td>
									<td align="center"><input type="radio" name="stowplanRadio" value="stowplanOn"></td>
									<td align="center"> <input type="radio" name="stowplanRadio" value="stowplanOff" checked> </td>
								</tr>
								<tr height="20%">
									<td align="center"><font font size="2" face="Georgia, Arial">DCM Scheduler </td>
									<td align="center"><input type="radio" name="dcmRadio" value="dcmOn"></td>
									<td align="center"> <input type="radio" name="dcmRadio" value="dcmOff" checked> </td>
								</tr>
								<tr height="20%">
									<td align="center"><font font size="2" face="Georgia, Arial">RDS Scheduler </td>
									<td align="center"><input type="radio" name="rdsRadio" value="rdsOn"></t>
									<td align="center"> <input type="radio" name="rdsRadio" value="rdsOff" checked> </td>
								</tr>

						</td>
					</tr>
					<tr>
						<td colspan="1">
						<tr>
								<td align="center"><input type="submit" value="Submit" id="submitButton"/></td>

						</tr>
						<tr>
								<td colspan="3" bgcolor="#BFBFBF" align="right"><b><a href="javascript:window.opener='x';window.close();">Close</a></b></td>
						</tr>
						</td>
					</tr>
			  </table>
		</td> 
	</tr>
</table>
</form>
</body>
</html>