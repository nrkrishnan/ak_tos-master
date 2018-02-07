<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.matson.tos.processor.FTPUploadProcessor" %>
<%@ page import="java.lang.Exception"%>
<%@ page import="org.apache.log4j.Logger"%>
<html>
<head>
<SCRIPT LANGUAGE=JAVASCRIPT></SCRIPT>
</head>
<script>   
function setfocus(a_field_id) {
        document.getElementById('commonsMultipartFile').focus();
 }
function getVal(btn)
{
	var fileSelected = document.getElementById('commonsMultipartFile').value;
	var fileType = document.getElementById('filetype').value;
	var fileName = document.getElementById('filename').value;
	var uploadbtn =btn.value;
	//var deletebtn = document.getElementById('buttonclicked').value;
	//alert('selected '+uploadbtn);
	if (uploadbtn == "Upload File") {
		//alert('update');
		if((fileSelected == null | fileSelected.length <=0 )){
			alert("Please select a file to upload");
			document.getElementById('commonsMultipartFile').focus();
			return false;
		}if(fileType == -1){
			alert("Please select the file type");
			document.getElementById('filetype').focus();
			return false;
		}
	}else if (uploadbtn == "Delete File"){
		//alert('delete');
		if (fileName != null) {
			if(fileType == -1){
				alert("Please select the file type");
				document.getElementById('filetype').focus();
				return false;
			} 
		} else if (fileName == null){
			alert("Please enter file name to delete");
			document.getElementById('fileName').focus();
			return false;
		}
	}
	
	//document.newVesFileupload.submit();
	return true;

}
</script>
<body>
<%

	FTPUploadProcessor.uploadFile(request);

%>
<form name="newVesFileupload" action="newVesFileupload.jsp" method="post" enctype="multipart/form-data" >
<table border="0" width="100%"><tr><td><table border="1" cellpadding="0" cellspacing="0" width="60%" align="center">
  <tr><td>
  <table border="0" cellpadding="0" cellspacing="0" width="100%" align="center">
	   <tr>
		<td bgcolor="#BFBFBF" colspan="2" align="center" cellspacing='15'><h2>STOW PLAN/DCM/RDS File Upload</h2> </td>
	   </tr>
	   <tr ><td bgcolor="#F0F8FF" colspan="2" align="center" style="font=16" height="40"><b>Choose the file to upload</b></td></tr>
	   <tr>
		   <td height="40" colspan="3" >
			   <table border="0" width="90%" cellpadding="0" cellspacing="0" align="center">
			   	   <td><label >Select File: </label>
				   <input type="file" name="commonsMultipartFile" id="commonsMultipartFile"/></td>
				   <td ><font font size="2" face="Georgia, Arial">File to delete: </font><input type="text" name="filename" size="8" ></td>
				   <td>&nbsp;&nbsp;<font font size="2" face="Georgia, Arial">File Type: </font>
					   <select name="filetype">
						   <option value="-1">Select Type</option>
						   <option value="stowplan">STOWPLAN</option>
						   <option value="dcm">DCM</option>
						   <option value="rds">RDS</option>
					   </select>
				   </td>
		       </table>
		   </td>
		</tr>
		<tr></tr>
		<tr>
		   <td height="40" colspan="2" >
			   <table border="0" width="70%" cellpadding="0" cellspacing="0" align="center">
				   <td ><input type="submit" value="Upload File" name="buttonclicked" id="buttonclicked" onclick="getVal(this)"/><font font size="1" face="Georgia, Arial">(Click here to upload file)</font></td>
				   <td>&nbsp;<input type="submit" value="Delete File" name="buttonclicked" id="buttonclicked" onclick="getVal(this)"/><font font size="1" face="Georgia, Arial">(Click here to delete file)</font></td>
		       </table>
		   </td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><%=request.getAttribute("statusMessage") %></td>
		</tr>
	  <tr>
	  <tr><td colspan="2" bgcolor="#BFBFBF" align="right"><b><a href="javascript:window.opener='x';window.close();">Close</a></b></td>
	  </tr>
</table></td></tr></table></td></tr>
</table>
</form>
</body>
</html>
