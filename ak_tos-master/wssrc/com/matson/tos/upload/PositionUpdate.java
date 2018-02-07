package com.matson.tos.upload;

/*
 *   A1  11/24/2008, forced it to upper case.
 *   A2  12/06/2011, Added method for Kulana Processing.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.jws.WebService;

import com.matson.tos.exception.TosException;
import com.matson.tos.processor.ChasRfidProcessor;
import com.matson.tos.processor.SnxProcessor;
import com.matson.tos.processor.UploadFileProcessor;

@WebService(name="YardPositionUpdate", serviceName="PositionUpdateService")

public class PositionUpdate {
	public boolean updatePosition(String snx) throws Exception {
		ChasRfidProcessor chasRfidProcessor = new ChasRfidProcessor();
		chasRfidProcessor.process(snx);
		return true;
	}
	
	public boolean rowRefresh(String row) throws Exception {
		ChasRfidProcessor chasRfidProcessor = new ChasRfidProcessor();
		chasRfidProcessor.rowRefreshProcess(row);
		return true;
	}
	
	public boolean kulanaAssign(String eqId, String notes) throws Exception {
		ChasRfidProcessor chasRfidProcessor = new ChasRfidProcessor();
		chasRfidProcessor.kulanaProcess(eqId, notes);
		return true;
	}
		
}








