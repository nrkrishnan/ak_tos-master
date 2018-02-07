<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Matson Navigation Company</title>
</head>
<body>
<form id="form" name="queryEnabler" action="queryEnabler" method="post">
	<div id="myDiv" class="myform">
			<h1>Query Enabler</h1>
			<p></p>
			<table cellspacing="0" cellpadding="0" border="0" width="100%">
				<tr valign="middle">
					<td>
					<input type="hidden" name="qKey" value="3760565290974525751" /> 
					<label for="qType"> <span class="required">*</span>	&nbsp;Type: </label> 
					<input type="radio" id="queryType" name="query" onclick="toggleFields(this)" value="Q"/>
					<label for="query"> Query </label>
					<input type="radio" id="queryType" name="update" onclick="toggleFields(this)" value="U"/>
					<label for="update"> Update </label>
					<input type="radio" id="queryType" name="delete" onclick="toggleFields(this)" value="D"/>
					<label for="delete"> Delete </label>
					<input type="checkbox" id="returnType" name="returnType" onclick="toggleFields(this)"/>
					<label for="tabChar">Return output as HTML table (default
					is csv)?</label> 
					<input type="checkbox" id="commit" name="commit" onclick="toggleFields(this)"/>
					<label for="commit"> Commit? </label> 					<br />
					<label for="qText"> <span class="required">*</span> &nbsp;Text: </label> <br />
					 <input type="text" name=queryText id="queryText" style="width: 626px; height: 72px"/><br />
					 	<input type="submit" name="submit" id="submitQuery" value="Submit"/><div class="spacer"></div>
					 	<input type="reset" name="reset" id="resetQuery" value="Reset"/> 
					</td>			
				</tr>
			</table>
			
			</div>
			
</form>
</body></html>
