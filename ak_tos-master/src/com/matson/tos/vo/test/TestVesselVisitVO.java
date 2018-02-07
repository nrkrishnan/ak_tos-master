package com.matson.tos.vo.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.matson.tos.vo.VesselVisitVO;

public class TestVesselVisitVO {
	public VesselVisitVO vo;
	
	@Before
	public void setUp() throws Exception {
		vo = new VesselVisitVO();
		vo.setId("MATU12345");
		vo.setPort("HON");
		vo.setPhase(VesselVisitVO.COMPLETE);
	}
	
	@Test
	public final void testEqualsObject() {
		VesselVisitVO vo2 = new VesselVisitVO();
		vo2.setId("MATU12345");
		vo2.setPort("HON");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		org.junit.Assert.assertTrue(vo.equals(vo2));
		
		vo2.setId("MATU12345");
		vo2.setPort("HIL");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		org.junit.Assert.assertFalse(vo.equals(vo2));
		
		vo2.setId("MATU12345");
		vo2.setPort("HON");
		vo2.setPhase(VesselVisitVO.ARCHIVED);
		org.junit.Assert.assertTrue(vo.equals(vo2));
	}

	@Test
	public final void testIsActive() {
		assertFalse(vo.isActive());
	}

	@Test
	public final void testIsComplete() {
		assertTrue(vo.isComplete());
	}

	@Test
	public final void testIsLive() {
		assertTrue(vo.isLive());
	}

	@Test
	public final void testIsClosed() {
		assertFalse(vo.isClosed());
	}


	@Test
	public final void testHashcode() {
		VesselVisitVO vo2 = new VesselVisitVO();
		vo2.setId("MATU12345");
		vo2.setPort("HON");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		org.junit.Assert.assertTrue(vo.hashCode() == vo2.hashCode());
		
		vo2.setId("MATU1234X");
		vo2.setPort("HIL");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		org.junit.Assert.assertFalse(vo.hashCode() == vo2.hashCode());
		
		vo2.setId("MATU12345");
		vo2.setPort("HON");
		vo2.setPhase(VesselVisitVO.ARCHIVED);
		org.junit.Assert.assertTrue(vo.hashCode() == vo2.hashCode());
	}
	
	@Test
	public final void testContains() {
		ArrayList list = new ArrayList();
		VesselVisitVO vo2 = new VesselVisitVO();
		vo2.setId("MATU12345");
		vo2.setPort("HON");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		list.add(vo2);
		vo2 = new VesselVisitVO();
		vo2.setId("MATU212");
		vo2.setPort("HIL");
		vo2.setPhase(VesselVisitVO.WORKING);
		list.add(vo2);
		vo2 = new VesselVisitVO();
		vo2.setId("MATU12345");
		vo2.setPort("KHI");
		vo2.setPhase(VesselVisitVO.COMPLETE);
		list.add(vo2);
		assertTrue(list.contains(vo));
		
		System.out.println(list.remove(vo) +" "+ vo);
		assertFalse(list.contains(vo));
	}
}
