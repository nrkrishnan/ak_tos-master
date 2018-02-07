package com.matson.tos.upload;

/*
 *   A1  11/24/2008, forced it to upper case.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.jws.WebService;

import com.matson.tos.exception.TosException;
import com.matson.tos.processor.UploadFileProcessor;

@WebService(name="FileUpload", serviceName="FileUploadService")

public class FileUploadImpl {
	private static String STIF = "STIF";
	private static String DCM = "DCM";
	private static String LATEDCM = "Late DCM";
	private static String STIF_EXT = ".txt";
	private static String DCM_EXT = ".csv";
	private static String LATEDCM_SUBEXT = "lt";
	private static String BARGEDCM_EXT = ".bargedcm";
	private static String BARGEDCM = "BARGEDCM";
	
	
	public boolean uploadFile(String file, byte[] data, boolean isCompressed) throws Exception {
		
		if(isCompressed) {
			try {
				InputStream bais = new ByteArrayInputStream(data);
				InputStream in = new GZIPInputStream(bais);
				
				byte[] buf = new byte[1024];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int size = 0;
				while( (size = in.read(buf)) > 0) {
					baos.write(buf,0,size);
				}
				
				data = baos.toByteArray();
			} catch (IOException ie) {
				throw new Exception("Could not unzip data stream");
			}
		}
		
		String[] splitFile = splitFile(file);
		System.out.println(splitFile+" "+splitFile.length);
		String extension = splitFile[splitFile.length-1];
		System.out.println(extension);
		String fileType = null;

		
		if(STIF_EXT.equalsIgnoreCase("."+extension)) {
			String subextension = splitFile[splitFile.length-2];
			if(subextension.equalsIgnoreCase(LATEDCM_SUBEXT)) {
				fileType = LATEDCM;
			} else {
				fileType = STIF;
			}
		}
		else if(DCM_EXT.equalsIgnoreCase("."+extension)) fileType = DCM;
		else if(BARGEDCM_EXT.equalsIgnoreCase("."+extension)) fileType = BARGEDCM;
		else {
			throw new Exception("Invalid file type for "+file+" "+extension);
		}
		
	   // System.out.println("File Length = "+data.length);
		boolean result = false;
		try {
		   result = UploadFileProcessor.processUpload(splitFile[0].toUpperCase(), fileType,splitFile[1], data);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Splits a file into base and extension.
	 * Return null if not splitable.
	 * @param file
	 * @return
	 */
	private static String[] splitFile(String file) {
		if(file == null) return null;
		String[] split = file.split("\\.");
		return split;
	}

	
	
}








