 <%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>AG Manifest processing</title>
<script type="text/javascript">
	function validateForm() {
		var fileSelected = document.getElementById('xmlFile').value;
		if ((fileSelected == null | fileSelected.length <= 0)) {
			alert("Please select a file to upload");
			return false;
		} else if(fileSelected.indexOf(".xml")==-1) {
			alert("Please select a xml file to upload");
			document.getElementById('xmlFile').value = null;
			return false;
		}
	}
</script>
</head>
<body>
      <form id="form" name="formAG" action="AGManifest" method="post" onsubmit="return validateForm()" enctype="multipart/form-data">
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
                                 <td style="color:#FFFFFF" colspan="4" align="center" cellspacing="15">
                                    <h2>AG Manifest Processing</h2>
                                 </td>
                              </tr>
                              <tr>
                                 <td bgcolor="#F0F8FF" colspan="4" align="center" style="font=16" height="40"><b>To process AG manifest, please select the SNX xml file and post. </b></td>
                              </tr>
                              <tr width="100%">
                                 <td height="40" width="100%" colspan="4">
                                    <table border="0" width="100%" cellpadding="0" cellspacing="0">
                                       <tr width="100%" align="center">
                                          <td width="100%"><font font size="2" face="Georgia, Arial">Choose File: </font><input name="xmlFile" id="xmlFile" type="file" accept=".xml" />
                                          </td>
                                       </tr>
                                    </table>
                                 </td>
                              </tr>
                              <tr height="50" width="100%" align="center">
                                 <td width="65%" colspan="4" style="font:11">&nbsp;<input type="submit" value="Post to AG Manifest" name="agpost" id="agpost" /></td>
                              </tr>
									<tr>
										<td colspan="3" width="200">
											<div id="result">${requestScope["message"]}</div>
										</td>
										<td colspan="1" align="right"><b><a
												href="javascript:window.opener='x';window.close();">Close</a></b></td>
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