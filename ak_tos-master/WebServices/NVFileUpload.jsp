<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	  <title>NewVessel processing</title>
      <script type="text/javascript">
      var submitting = false;
         function validateForm() {
        	if(submitting) {
        		alert("Processing started already, please wait...");
        		return false;
        	}
         	if (document.formNV.vvd.value == "") {
         		alert("Vessel Voyage code should not be blank");
         		document.formNV.vvd.focus();
     			document.formNV.nvcopy.disabled=false;
         		return false;
         	} else {
         		var vvd = document.formNV.vvd.value;
         		if (!vvd.match("[A-Za-z0-9]{3}[0-9]{3}")) {
         			alert("Invalid Vessel Voyage code");
         			document.formNV.vvd.focus();
         			document.formNV.nvcopy.disabled=false;
         			return false;
         		}
         	}
         	submitting = true;
         	document.formNV.submit();
         	return true;
         }
      </script>
   </head>
   <body onload="document.getElementById('vvd').focus()">
      <form id="form" name="formNV" action="HonNewVes" method="post" onSubmit="return validateForm()">
         <table border="0" width="100%">
            <tr>
               <td>
                  <table border="1" cellpadding="0" cellspacing="0" width="60%" align="center">
                     <tr>
                        <td>
                           <table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
                              <tr bgcolor="#275da7">
                                 <td>
                                    &nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="200" />
                                 </td>
                                 <td style="color:#FFFFFF" colspan="3" align="center" cellspacing="15">
                                    <h2>NewVessel processing</h2>
                                 </td>
                              </tr>
                              <tr>
                                 <td bgcolor="#F0F8FF" colspan="4" align="center" style="font=16" height="40"><b>To process new vessel, enter the vessel voyage code</b></td>
                              </tr>
                              <tr width="100%">
                                 <td height="40" width="100%" colspan="4">
                                    <table border="0" width="100%" cellpadding="0" cellspacing="0">
                                       <tr width="100%" align="center">
                                          <td width="100%"><font font size="2" face="Georgia, Arial">Vessel Voyage code: </font><input type="text" name="vvd" id="vvd" maxlength="6" style="font-size: 13px;font-weight: bold;text-align: center;text-transform: uppercase;" /></td>
                                       </tr>
                                    </table>
                                 </td>
                              </tr>
                              <tr height="50" width="100%" align="center">
                                 <td width="65%" colspan="4" style="font:11">&nbsp;<input type="submit" value="Process" name="nvcopy" id="nvcopy" ondblclick="return false;"/></td>
                              </tr>
                              <tr>
                                 <td colspan="4" bgcolor="#BFBFBF" align="right"><b><a href="NVFileUpload.jsp">Refresh</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:window.opener='x';window.close();">Close</a></b></td>
                              </tr>
                              </tr>
                           </table>
                        </td>
                     </tr>
                  </table>
               </td>
            </tr>
         </table>
      </form>
   </body>
</html>