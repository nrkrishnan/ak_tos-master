package com.matson.tos.reports;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;

public class EmailProcessor {

	public static void sendEmailWithAttachment(String from, String to[], String subject, String outputFilePath, String outputFileName){
			
		try {			
            
			for(int i=0;i<to.length;i++){
				
			// Create the email message
			MultiPartEmail email = new MultiPartEmail();
			email.setHostName("smtp.mail.yahoo.com");
			email.setSSL(true);
			email.setSmtpPort(465);
			email.setAuthentication("kbodepudi@matson.com", "");

			email.addTo(to[i], to[i]);
			email.setFrom(from, from);
			email.setSubject(subject);
			email.setMsg("Here are the reports file you wanted");

			// Create the attachment
			EmailAttachment attachment = new EmailAttachment();
			
			attachment.setPath(outputFilePath +"\\"+ outputFileName + ".pdf");
			attachment.setDisposition(EmailAttachment.ATTACHMENT);			
			attachment.setName(outputFileName + ".pdf");
			email.attach(attachment);

			attachment.setPath(outputFilePath +"\\"+ outputFileName + ".html");
			attachment.setName(outputFileName + ".html");
			email.attach(attachment);

			attachment.setPath(outputFilePath +"\\"+ outputFileName + ".xls");
			attachment.setName(outputFileName + ".xls");
			email.attach(attachment);

			// send the email
			email.send();
			
			}
			System.out.println("Email sent with attachment successfully.." + subject);
		} catch (Exception e) {
			
			System.out.println("Email sending failed with attachment.."+subject);
			e.printStackTrace();
		}
		
	}
}
