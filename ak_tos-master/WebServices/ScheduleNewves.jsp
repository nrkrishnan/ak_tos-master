<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	  <title>Schedule NewVessel</title>
      <script type="text/javascript">
         function validateForm() {
         	if (document.formNV.vv.value == "") {
         		alert("Vessel Voyage code should not be blank");
         		document.formNV.vv.focus();
         		return false;
         	} else {
         		var vv = document.formNV.vv.value;
         		if (!vv.match("[A-Za-z]{3}[0-9]{3}")) {
         			alert("Invalid Vessel Voyage code");
         			document.formNV.vv.focus();
         			return false;
         		}
         	}
         }
      </script>
   </head>
   <body onload="document.getElementById('vv').focus()">
      <form id="form" name="formNV" action="ScheduleNewves" method="post" onSubmit="return validateForm()">
         <table border="0" width="100%">
            <tr>
               <td>
                  <table border="1" cellpadding="0" cellspacing="0" width="50%" align="center">
                     <tr>
                        <td>
                           <table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
                              <tr bgcolor="#275da7">
                                 <td>
                                    &nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="200" />
                                 </td>
                                 <td style="color:#FFFFFF" colspan="3" align="center" cellspacing="15">
                                    <h2>Schedule NewVessel</h2>
                                 </td>
                              </tr>
                              <tr>
                                 <td bgcolor="#F0F8FF" colspan="4" align="center" style="font=16" height="40"><b>To schedule a new vessel, please enter the vessel voyage code</b></td>
                              </tr>
                              <tr width="100%">
                                 <td height="40" width="100%" colspan="4">
                                    <table border="0" width="100%" cellpadding="0" cellspacing="0">
                                       <tr width="100%" align="center">
                                          <td width="100%"><font font size="2" face="Georgia, Arial">Vessel Voyage code: </font><input type="text" name="vv" id="vv" maxlength="6" style="font-size: 13px;font-weight: bold;text-align: center;text-transform: uppercase;" /></td>
                                       </tr>
                                    </table>
                                 </td>
                              </tr>
                              <tr height="50" width="100%" align="center">
                                 <td width="65%" colspan="4" style="font:11">&nbsp;<input type="submit" value="Schedule" name="nvschedule" id="nvschedule" /></td>
                              </tr>
                              <tr>
                                 <td colspan="4" bgcolor="#EFEFEF" align="center"><div id="result">${requestScope["message"]}</div> </td>
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