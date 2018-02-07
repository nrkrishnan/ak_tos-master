package com.matson.tos.util;

import com.matson.tos.jatb.Abob;
import com.matson.tos.processor.CommonBusinessProcessor;

public class AbobMessageUtil {
	public static String constructMessage(Abob bob) {
		String message = "";
		String deliminator = "¦";
		for(int i=0; i<=86; i++) {
			if(i==0)
				message += "ABOB" + deliminator;
			else if(i==1)
				message += "" + deliminator;
			else if(i==2)
				message += "" + deliminator;
			else if(i==3)
				message += "NEQV2" + deliminator;
			else if(i==4)
				message += CommonBusinessProcessor.leftPad(""+(i+1), "0", 9) + deliminator;
			else if(i==5)
				message += "MAT" + deliminator;
			else if(i==6)
				message += bob.getBookingNbr() + deliminator;
			else if(i==7)
				message += "" + deliminator;
			else if(i==8)
				message += "B" + deliminator;
			else if(i==9)
				message += bob.getShipperName() + deliminator;
			else if(i==10)
				message += bob.getVessel() + deliminator;
			else if(i==11)
				message += bob.getVoyageNbr() + deliminator;
			else if(i==12)
				message += bob.getDischargePort() + deliminator;
			else if(i==13)
				message += bob.getBlDestPort() + deliminator;
			else if(i==14)
				message += bob.getLoadPort() + deliminator;
			else if(i==15)
				message += "" + deliminator;
			else if(i==16)
				message += "" + deliminator;
			else if(i==17)
				message += "" + deliminator;
			else if(i==18)
				message += "" + deliminator;
			else if(i==19)
				message += "" + deliminator;
			else if(i==20)
				message += "" + deliminator;
			else if(i==21)
				message += (bob.getHazardousInd()==null?"N":bob.getHazardousInd()) + deliminator;
			else if(i==22)
				message += "000" + deliminator;
			else if(i==23)
				message += "" + deliminator;
			else if(i==24)
				message += "" + deliminator;
			else if(i==25)
				message += bob.getDirSeq() + deliminator;
			else if(i==26)
				message += "" + deliminator;
			else if(i==27)
				message += "" + deliminator;
			else if(i==28)
				message += "" + deliminator;
			else if(i==29)
				message += bob.getCommodity() + deliminator;
			else if(i==30)
				message += bob.getPrimaryCarrier() + deliminator;
			else if(i==31)
				message += "" + deliminator;
			else if(i==32)
				message += bob.getLoadType() + deliminator;
			else if(i==33)
				message += "" + deliminator;
			else if(i==34)
				message += (bob.getSit()!=null&&bob.getSit().equals("S")?"Y":"N") + deliminator;
			else if(i==35)
				message += "N" + deliminator;
			else if(i==36)
				message += "N" + deliminator;
			else if(i==37)
				message += "N" + deliminator;
			else if(i==38)
				message += bob.getConsigneeName() + deliminator;
			else if(i==39)
				message += bob.getShipperId() + deliminator;
			else if(i==40)
				message += bob.getConsigneeId() + deliminator;
			else if(i==41)
				message += bob.getShipperName() + deliminator;
			else if(i==42)
				message += "" + deliminator;
			else if(i==43)
				message += bob.getConsigneeName() + deliminator;
			else if(i==44)
				message += "" + deliminator;
			else if(i==45)
				message += "" + deliminator;
			else if(i==46)
				message += "" + deliminator;
			else if(i==47)
				message += "" + deliminator;
			else if(i==48)
				message += "" + deliminator;
			else if(i==49)
				message += "0001" + deliminator;
			else if(i==50)
				message += bob.getTypeCode() + deliminator;
			else if(i==51)
				message += "" + deliminator;
			else if(i==52)
				message += "" + deliminator;
			else if(i==53)
				message += "" + deliminator;
			else if(i==54)
				message += "" + deliminator;
			else if(i==55)
				message += "0000" + deliminator;
			else if(i==56)
				message += " 0000 " + deliminator;
			else if(i==57)
				message += "" + deliminator;
			else if(i==58)
				message += "" + deliminator;
			else if(i==59)
				message += "" + deliminator;
			else if(i==60)
				message += "" + deliminator;
			else if(i==61)
				message += "0000" + deliminator;
			else if(i==62)
				message += " 0000 " + deliminator;
			else if(i==63)
				message += "" + deliminator;
			else if(i==64)
				message += "" + deliminator;
			else if(i==65)
				message += "" + deliminator;
			else if(i==66)
				message += "" + deliminator;
			else if(i==67)
				message += "0000" + deliminator;
			else if(i==68)
				message += " 0000 " + deliminator;
			else if(i==69)
				message += "" + deliminator;
			else if(i==70)
				message += "" + deliminator;
			else if(i==71)
				message += "" + deliminator;
			else if(i==72)
				message += "" + deliminator;
			else if(i==73)
				message += "0000" + deliminator;
			else if(i==74)
				message += " 0000 " + deliminator;
			else if(i==75)
				message += "" + deliminator;
			else if(i==76)
				message += "" + deliminator;
			else if(i==77)
				message += "" + deliminator;
			else if(i==78)
				message += "" + deliminator;
			else if(i==79)
				message += bob.getBfrtUnNa() + deliminator;
			else if(i==80)
				message += bob.getBfrtHzdClazz() + deliminator;
			else if(i==81)
				message += "" + deliminator;
			else if(i==82)
				message += "" + deliminator;
			else if(i==83)
				message += "" + deliminator;
			else if(i==84)
				message += "" + deliminator;
			else if(i==85)
				message += "" + deliminator;
			else if(i==86)
				message += (bob.getOog()!=null&&bob.getOog().equals("Y")?"Y":"N") + deliminator;
		}
		return message.replaceAll("null", "");
	}
}
