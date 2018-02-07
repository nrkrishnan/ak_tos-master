<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.matson.tos.processor.UploadFileProcessor" %>
<%@ page import="java.lang.Exception"%>
<%@ page import="org.apache.log4j.Logger"%>
<html>
<head>
<SCRIPT LANGUAGE=JAVASCRIPT></SCRIPT>
</head>
<script>   
function setfocus(a_field_id) {
        document.getElementById('filename').focus();
 }
function Validate()
{
	var fileName = document.getElementById('filename').value;
	var fileType = document.getElementById('filetype').value;
	if((fileName == null | fileName.length <=0 )){
		alert("Please enter vessel voyage code");
		document.getElementById('filename').focus();
		return false;
	}if(fileType == -1){
		alert("Please select the file type");
		document.getElementById('filetype').focus();
		return false;
	}
	document.upload.submit();
	return true;
}
</script>
<body onload="setfocus('vesvoycode');">
<form name="upload" action="upload.jsp" method="post" onSubmit="return Validate();">
<table border="0" width="100%"><tr><td><table border="1" cellpadding="0" cellspacing="0" width="60%" align="center">
  <tr><td><table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
   <tr>
	<td bgcolor="#BFBFBF" colspan="2" align="center" cellspacing='15'><h2>STIF/DCM File Upload</h2> </td>
   </tr>
     <tr ><td bgcolor="#F0F8FF" colspan="2" align="center" style="font=16" height="40"><b>To upload the file for processing enter the vessel voyage code and select the file type</b></td></tr>
   <tr><td height="40" colspan="2" ><table border="0" width="100%" cellpadding="0" cellspacing="0">
   <td width="50%" align="right"><font font size="2" face="Georgia, Arial">Vessel Voyage code: </font><input type="text" name="filename" size="8" maxlength="7"></td>
   <td width="50%">&nbsp;&nbsp;<font font size="2" face="Georgia, Arial">File Type: </font><select name="filetype">
   <option value="-1">Select Type</option>
   <option value="STIF">STIF</option>
   <option value="DCM">DCM</option>
   </select>
   </td>
   </table></td></tr>
    <tr height="50">
		<td width="35%"></td>
       <td width="65%" style="font:11">&nbsp;<input type="submit" value="Upload File" id="uploadbutton"/><font font size="1" face="Georgia, Arial">(Click to upload file)</font></td>
   </tr>
   <tr>
<%
	String strFileName = request.getParameter("file");
	String strFileType = request.getParameter("fileType");
	String strFileStatus = request.getParameter("status");

	if(strFileStatus!= null && strFileStatus.equals("Error")){
%>
		<td height="40" valign="center" align="center" colspan="2" bgcolor="#F0F8FF" style="color:red; font=16"><b>Error in uploading file for vessel voyage <%=strFileName%> of type <%=strFileType%></b></td>

		<tr><td colspan="2" bgcolor="#F0F8FF" align="left" height="20"><font font size="1" face="Georgia, Arial">Note : Please verify that the upload file name entered is correct and the file has been copied to the required folder</font></td>
       </tr>	
<%	}
	else if(strFileStatus!= null && strFileStatus.equals("Success")){  %>
		
		<td height="40" valign="center" align="center"  colspan="2" bgcolor="#F0F8FF" style="color:blue; font=16"><b>File Uploaded Successfully for Vessel Voyage <%=strFileName%> of type <%=strFileType%></b></td>
<% } %>
  </tr>
  <tr><td colspan="2" bgcolor="#BFBFBF" align="right"><b><a href="javascript:window.opener='x';window.close();">Close</a></b></td>
  </tr>
  </tr>
</table></td></tr></table></td></tr>
</table>
</form>
</body>
</html>
<%!
	private static Logger logger = Logger.getLogger("upload.jsp");
%>
<%
	String fileName = null;
	String filetype = null;
try 
{
	fileName = request.getParameter("filename");
	filetype = request.getParameter("filetype");
	//Process a file 
	if(fileName != null && filetype != null)
	{
		boolean uploadStatus = UploadFileProcessor.processUpload(fileName,filetype);
		if(uploadStatus){
		response.sendRedirect((response.encodeRedirectURL("upload.jsp?file="+fileName+"&fileType="+filetype+"&status=Success")));
		}else{
		response.sendRedirect((response.encodeRedirectURL("upload.jsp?file="+fileName+"&fileType="+filetype+"&status=Error")));
		}
	}
}
catch(Exception ex){
	logger.error("Error In Processing Upload JSP", ex);
	response.sendRedirect((response.encodeRedirectURL("upload.jsp?file="+fileName+"&fileType="+filetype+"&status=Error")));
}
%>