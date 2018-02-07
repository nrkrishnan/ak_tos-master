/**
 * 
 */
package com.matson.tos.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;



import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
/**
 * @author JZF
 *
 */
public class EmailSender {
	private static Logger logger = Logger.getLogger(EmailUtil.class);

	public EmailSender() {};

	public static String getEnvSubject(String subject) {
		String type = EnvironmentProperty.getEnvType();
		logger.info("Env type "+type);
		String environmentType = TosRefDataUtil.getValue("ENV_TYPE");
		if(!type.equalsIgnoreCase(environmentType)) {
			subject = "["+type+"] "+subject; 
		}
		return subject;
	}
	public static void sendMail(String from,String to,String sub,String msg){
		logger.debug("Sending Email to " + to);
		try{
			HtmlEmail  email = new HtmlEmail();
			email.setHostName(TosRefDataUtil.getValue( "MAIL_HOST"));
			String toAddress = to.replaceAll(";", ",");
			Object[] addresses = InternetAddress.parse(toAddress);
			ArrayList addrs = new ArrayList();
			for ( int i = 0; i < addresses.length; i++) {
				addrs.add( addresses[i]);
			}
			email.setTo( addrs);
			email.setFrom(from);
			email.setSubject(getEnvSubject(sub));
			email.setHtmlMsg(msg);
			email.send();
		} catch ( Exception e) {
			logger.error("Error sending email: ", e);
		}
	}

	/*
	 * Method mailAttachment mail attached file to Mailing Addresses 
	 */
	public static void mailAttachment(String to,String from,String host,
			String attFileName,String attachment,String msgText,String subject)
	{ 
		logger.debug("Class: DcmFormatter - Method: mailAttachment");
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);

		try 
		{   // create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			//parses comma separated email addresses 
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);

			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			//FileDataSource fds = new FileDataSource(fileName);
			mbp2.setDataHandler(new DataHandler( attachment, "text/html"));
			mbp2.setFileName( attFileName);

			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
			logger.debug( "Transport the mail");
		} 
		catch (MessagingException mex) 
		{
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}
	/*
	 * Method mailAttachment mail attached file to Mailing Addresses for sending reports
	 */
	public static void mailAttachment(String to,String from,String host,
			String pdfAttFileName,String pdfAttachment,
			String xlsAttFileName,String xlsAttachment,
			String msgText,String subject, int reportFlag)
	{ 
		logger.info("Method: MailAttachment() for Reports" + subject);
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);

		try 
		{  
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			//parses comma separated email addresses 
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);
			logger.info("Attaching report PDF ::: "+pdfAttachment);
			logger.info("Attaching report XLS ::: "+xlsAttachment);
			FileDataSource pdfFileDataSource = new FileDataSource(pdfAttachment) {    // file name including file path. In our case it should be /var/tmp/filename 
				public String getContentType() {
					return "application/pdf";
				}
			};
			FileDataSource xlsFileDataSource = new FileDataSource(xlsAttachment) {    // file name including file path. In our case it should be /var/tmp/filename 
				public String getContentType() {
					return "application/vnd.ms-excel";
				}
			};
			MimeBodyPart pdf = new MimeBodyPart();
			pdf.setDataHandler(new DataHandler(pdfFileDataSource));
			pdf.setFileName(pdfFileDataSource.getName());
			//
			MimeBodyPart xls = new MimeBodyPart();
			xls.setDataHandler(new DataHandler(xlsFileDataSource));
			xls.setFileName(xlsFileDataSource.getName());
			
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(pdf);
			mp.addBodyPart(xls);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
			logger.info("Transport the mail with Report Attachment " +subject);
		} 
		catch (MessagingException mex) 
		{
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}

	}
	public static void mailMultiAttachment(String to, String from, String host, List<String> files, String msgText, String subject) {
		logger.info("Method: mailMultiAttachment() for Reports" + subject);
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);
		try 
		{ 
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			//parses comma separated email addresses 
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);
			Multipart mp = new MimeMultipart();
			if(files.size() > 0) {
				for(int i=0; i<files.size(); i++) {
					FileDataSource fileDataSource = null;
					if(files.get(i).endsWith(".xls")) {
						fileDataSource = new FileDataSource(files.get(i)) {    // file name including file path. In our case it should be /var/tmp/filename 
							public String getContentType() {
								return "application/vnd.ms-excel";
							}
						};
					} else if(files.get(i).endsWith(".pdf")){
						fileDataSource = new FileDataSource(files.get(i)) {    // file name including file path. In our case it should be /var/tmp/filename 
							public String getContentType() {
								return "application/pdf";
							}
						};
					} else {
						fileDataSource = new FileDataSource(files.get(i)) {
							public String getContentType() {
								return "text/plain";
							}
						};
					}
					MimeBodyPart filePart = new MimeBodyPart();
					filePart.setDataHandler(new DataHandler(fileDataSource));
					filePart.setFileName(fileDataSource.getName());
					mp.addBodyPart(filePart);
				}
			}
			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
			logger.info("Transport the mail with Report Attachment " +subject);
		}
		catch (MessagingException mex) 
		{
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}
	public static void mailAttachment(String to, String from, String host,
			String zipAttachment, String msgText, String subject) {
		logger.info("Method: MailAttachment() for Zip files" + subject);
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			// parses comma separated email addresses
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);
			logger.info("Attaching zip ::: " + zipAttachment);
			FileDataSource zipFileDataSource = new FileDataSource(zipAttachment) {
				public String getContentType() {
					return "application/zip";
				}
			};
			MimeBodyPart zip = new MimeBodyPart();
			zip.setDataHandler(new DataHandler(zipFileDataSource));
			zip.setFileName(zipFileDataSource.getName());

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(zip);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
		} catch (MessagingException mex) {
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void mailTextAttachment(String to, String from, String host,
			String txtAttachment, String msgText, String subject) {
		logger.info("Method: MailAttachment() for Txt files" + subject);
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			// parses comma separated email addresses
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);
			logger.info("Attaching txt ::: " + txtAttachment);
			FileDataSource txtFileDataSource = new FileDataSource(txtAttachment) {
				public String getContentType() {
					return "text/plain";
				}
			};
			MimeBodyPart txt = new MimeBodyPart();
			txt.setDataHandler(new DataHandler(txtFileDataSource));
			txt.setFileName(txtFileDataSource.getName());

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(txt);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
		} catch (MessagingException mex) {
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void mailAttachmentForGumVesselData(String to,String from,String host,
			String xlsAttFileName,String xlsAttachment,
			String msgText,String subject, int reportFlag)
	{ 
		logger.info("Method: mailAttachmentForGumVesselData for Reports" + subject);
		// create some properties and get the default Session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);

		Session session = Session.getInstance(props, null);

		try 
		{  
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			//parses comma separated email addresses 
			String toAddress = to.replaceAll(";", ",");
			InternetAddress[] address = InternetAddress.parse(toAddress);
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(getEnvSubject(subject));

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(msgText);
			//logger.info("Attaching report PDF ::: "+pdfAttachment);
			logger.info("Attaching report XLS ::: "+xlsAttachment);
			/*FileDataSource pdfFileDataSource = new FileDataSource(pdfAttachment) {    // file name including file path. In our case it should be /var/tmp/filename 
				public String getContentType() {
					return "application/pdf";
				}
			};*/
			FileDataSource xlsFileDataSource = new FileDataSource(xlsAttachment) {    // file name including file path. In our case it should be /var/tmp/filename 
				public String getContentType() {
					return "application/vnd.ms-excel";
				}
			};
			/*MimeBodyPart pdf = new MimeBodyPart();
			pdf.setDataHandler(new DataHandler(pdfFileDataSource));
			pdf.setFileName(pdfFileDataSource.getName());*/
			//
			MimeBodyPart xls = new MimeBodyPart();
			xls.setDataHandler(new DataHandler(xlsFileDataSource));
			xls.setFileName(xlsFileDataSource.getName());
			
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			//mp.addBodyPart(pdf);
			mp.addBodyPart(xls);

			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);
			logger.info("Transport the mail with Report Attachment " +subject);
		} 
		catch (MessagingException mex) 
		{
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}

	}
}
