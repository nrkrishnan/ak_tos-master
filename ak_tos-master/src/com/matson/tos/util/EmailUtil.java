package com.matson.tos.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.log4j.Logger;

public class EmailUtil {
	private static Logger logger = Logger.getLogger(EmailUtil.class);
	
	private Store store;
	private Folder folder;
	
	public EmailUtil() {
		try {
			String host = ""; //ConfigHelper.getStringValue("VCSC", "CHAS_EMAIL_SERVER_NAME");
			String username = ""; //ConfigHelper.getStringValue("VCSC", "CHAS_EMAIL_USERNAME_BNSF");
			String password = ""; //ConfigHelper.getStringValue("VCSC", "CHAS_EMAIL_PASSWORD_BNSF");
			logger.debug("host : " + host + ", email username : " + username + ", password : " + password);
			/* * IMAP configuration */
			Properties system_properties = System.getProperties();
			system_properties.put("mail.imap.partialfetch", "true");
			system_properties.put("mail.imap.fetchsize", "51200");
			// Get session
			Session session = Session.getInstance( system_properties, null);
			
			// get the Store object for IMAP
			store = session.getStore("imap");
			store.connect(host, username, password);
	
			// Get folder
			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
		}catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("NoSuchProvider Error :: " + e);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("Messaging Error :: " + e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("Error :: " + e);
		}
	}
	public Message[] GetEmails() {
		
		try {
			logger.debug("######### email process is started @ " + new java.util.Date() + " ######### \n");
			// Get directory
			return folder.getMessages();
		}  catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("Messaging Error :: " + e);
		}
		return null;
	  }
	  
	 /**
	 * This method loop through the Multipart object and call handlePart(Part)
	 * method for further process.
	 * @param multipart
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static Part[] getPart(Message msg) 
	      throws MessagingException, IOException {
		
		Object content = msg.getContent();
	      Multipart multipart = null;
	       if (content instanceof Multipart) {
	         multipart = ((Multipart)content);
	       } 
	       ArrayList<Part> parts = new ArrayList<Part>();
	    for (int i=0, n=multipart.getCount(); i<n; i++) {
	      parts.add(multipart.getBodyPart(i));
	    }
	    Part[] a = new Part[1];
	    return parts.toArray( a);
	  }
	  
	 /**
	 * This method checks if part has attachement then it will call
	 * parseFile(filename, inputstream) for parsing the attachment txt file.
	 * @param part
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void handlePart(Part part) 
	      throws MessagingException, IOException {
		  
	    String disposition = part.getDisposition();
	    String contentType = part.getContentType();
	    
	    if (disposition!= null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) ||
	    		disposition.equalsIgnoreCase(Part.INLINE)) ) {

	    	parseFile(part.getFileName(),part.getInputStream());
	       
	    } else { // Should never happen
	      // do nothing
	    }
	  }
	  
	 /**
	 * This method saves the attachement data to a file and call
	 * parseTextAttachment(File) to further process attachment data.
	 * @param fileName
	 * @param input
	 * @throws IOException
	 */
	public void parseFile(String fileName,
		      InputStream input) throws IOException {
		  
		  File file = new File("attachment.txt");
		  
		  FileOutputStream fos = new FileOutputStream(file);
		  BufferedInputStream bis = new BufferedInputStream(input);
		  int bufRead = 0;
		  byte[] buffer = new byte[1024];
		  
		  while(true) {
			  bufRead = bis.read(buffer,0,1024);
			  if (bufRead == -1) break;
			  fos.write(buffer, 0, bufRead);
		  }
		  
		  if(bis !=null)bis.close();
		  if(fos!=null)fos.close();
		  bis=null;
		  fos=null;
		  
		 // Vector emailDataVct = ParseEmailTextFile.parseTextAttachment(file);
		  //logger.debug("Total Record(s) Found : " + emailDataVct.size());

		  
		  file.delete();

	  }
	  
	 /**
	 * This method moves the message of INBOX to a specific BackUp folder.
	 * @param folder
	 * @param store
	 * @param msgs
	 */
	public void MoveMessage(Message[] msgs) {
		  
	    try {
			//Open destination folder, create if reqd
	    	//Folder dfolder = store.getFolder(Constant.EMAIL_BACKUP_FOLDER);
	    	
			if (!folder.exists())
				folder.create(Folder.HOLDS_MESSAGES);
			
			// Get the message objects to copy
	    	logger.debug("\nMoving " + msgs.length + " message(s) to BackUp folder\n");

			if (msgs.length != 0) {
				folder.copyMessages(msgs, folder);
				folder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);

				// Dump out the Flags of the moved messages, to insure that
				// all got deleted
				for (int i = 0; i < msgs.length; i++) {
				    if (!msgs[i].isSet(Flags.Flag.DELETED))
				    	logger.debug("Message # " + msgs[i] + 
								" not deleted");
				}
				folder.expunge();
			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in moving message from INBOX to BackUp Folder", e);
		}

	}
	  

}
