package com.matson.tos.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Date;

import com.matson.sched.Schedule;
import com.matson.tos.util.VesselScheduleLookup;

public class TestFSSFeed {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testGetSchedule() {
		try {
		VesselScheduleLookup lookup = new VesselScheduleLookup();
		Schedule schedule = lookup.getSchedule();
		} catch (Exception e) {
			fail("Exception "+e.getMessage());
		}
	}
	
	@Test
	public final void testVesselLines() {
		String lineStr = "HON , ASL, MAY,MAT";
		String[] vesselLines = lineStr.split(" *, *");
		System.out.println(vesselLines.length);
		assertTrue(vesselLines.length == 4);
		
		
	}
	
	public static Date formatDate(String strDate, String dateFormat){
		java.util.Date date = null;
		 try{
			 java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(dateFormat);
	         date = (java.util.Date)formatter.parse(strDate);
	       }catch(Exception e){
			e.printStackTrace();
		   }
		   return date;
		}//Method Ends 	
   
	public static void main(String args[]){
		
		Date targs = formatDate("06/16/2011", "MM/dd/yyyy");
		System.out.println(targs);
		
	}

}
