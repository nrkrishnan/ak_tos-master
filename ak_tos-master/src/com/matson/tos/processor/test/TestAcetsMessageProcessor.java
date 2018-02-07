package com.matson.tos.processor.test;

import com.matson.tos.processor.MdbAcetsMessageProcessor;

public class TestAcetsMessageProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MdbAcetsMessageProcessor proc = new MdbAcetsMessageProcessor();
		//proc.processMsg("ATRD¦01/20/09¦10:47:00¦NEQOL¦057011851¦TAGT¦MAGNUM TRANSPORTATION, INC.                       ¦MAT  ¦N¦                                ¦");
		proc.processMsg("ATRU¦01/23/09¦14:16:40¦NEQOL¦097697080¦WWA1¦WWA TRUCKING COMPANY UPDATE TEST                  ¦MAT  ¦Y¦WILLIAM                         ¦4807365200");

	}

}
