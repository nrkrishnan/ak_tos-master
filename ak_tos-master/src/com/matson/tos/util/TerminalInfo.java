package com.matson.tos.util;

public class TerminalInfo {

	 public static boolean isHawaiiPort(String destPort)
	 {
		if(destPort == null) return false;
	    
		boolean nisPort = false;
        if(destPort.equals("KAH") || destPort.equals("HIL") || destPort.equals("KHI") || destPort.equals("NAW") ||
            destPort.equals("LNI") || destPort.equals("MOL") || destPort.equals("HON"))
        {
        	nisPort = true;
        }
	    return  nisPort;
	 }
}
