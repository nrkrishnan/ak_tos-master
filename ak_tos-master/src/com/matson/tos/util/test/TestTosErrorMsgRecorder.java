package com.matson.tos.util.test;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.matson.tos.jaxb.snx.error.SnxError;
import com.matson.tos.util.SnxUnmarshaller;
import com.matson.tos.util.TosErrorMsgRecorder;

public class TestTosErrorMsgRecorder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//TosErrorMsgRecorder.insertGroovyErrorMessage("<argo:snx-error xmlns:argo=\"http://www.navis.com/argo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.navis.com/argo snxError.xsd\"><argo:reason>[Customer Groovy Code generated an exception: java.lang.Exception: Could not find active unitERR_GVY_STRIP_999. Could not STRIP unit: MATU25011a3.]</argo:reason><argo:payload>&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;&lt;argo:snx xmlns:argo=\"http://www.navis.com/argo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.navis.com/argo snx.xsd\"&gt;&lt;groovy class-location=\"database\" class-name=\"StripUnit\"&gt;&lt;parameters&gt;&lt;parameter id=\"recorder\" value=\"CARS\" /&gt;&lt;parameter id=\"equipment-id\" value=\"MATU25011a3\" /&gt;&lt;/parameters&gt;&lt;/groovy&gt;&lt;/argo:snx&gt;</argo:payload></argo:snx-error>");
		String msg = "<argo:snx-error xmlns:argo=\"http://www.navis.com/argo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.navis.com/argo snxError.xsd\"><argo:reason>[Customer Groovy Code generated an exception: java.lang.Exception: Could not find active unitERR_GVY_STRIP_999. Could not STRIP unit: MATU25011a3.]</argo:reason><argo:payload>&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;&lt;argo:snx xmlns:argo=\"http://www.navis.com/argo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.navis.com/argo snx.xsd\"&gt;&lt;groovy class-location=\"database\" class-name=\"StripUnit\"&gt;&lt;parameters&gt;&lt;parameter id=\"recorder\" value=\"CARS\" /&gt;&lt;parameter id=\"equipment-id\" value=\"MATU25011a3\" /&gt;&lt;/parameters&gt;&lt;/groovy&gt;&lt;/argo:snx&gt;</argo:payload></argo:snx-error>";
		ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes());
		try {
			SnxError error = SnxUnmarshaller.unmarshallError(stream);
			
			StringBuffer buf = new StringBuffer();
			List<Object> l = error.getReason().getContent();
			if(l != null) {
				Iterator iter = l.iterator();
				while(iter.hasNext()) {
					buf.append(iter.next());
					buf.append("\n");
				}
			}
			System.out.println("Reason="+buf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

}
