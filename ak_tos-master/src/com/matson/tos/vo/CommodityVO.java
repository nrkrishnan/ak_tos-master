package com.matson.tos.vo;

import com.matson.tos.constants.TransitState;

public class CommodityVO {
	private String commodity;
	private TransitState tstate;
	private String id;
	public String getCommodity() {
		return commodity;
	}
	public void setCommodity(String commodity) {
		this.commodity = commodity;
	}
	public TransitState getTstate() {
		return tstate;
	}
	public void setTstate(TransitState tstate) {
		this.tstate = tstate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
}
