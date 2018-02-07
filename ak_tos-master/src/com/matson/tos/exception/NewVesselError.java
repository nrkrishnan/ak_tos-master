package com.matson.tos.exception;

public class NewVesselError {

	public static final int DB_OP_ERROR = 1;
	public static final int DATA_ERROR = 2;
	public static final int DATA_MISSING = 3;
	private int errorType;
	private String errorMessage;
	private String vvd;
	private String containerNumber;
	//
	public int getErrorType() {
		return errorType;
	}
	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getVvd() {
		return vvd;
	}
	public void setVvd(String vvd) {
		this.vvd = vvd;
	}
	public String getContainerNumber() {
		return containerNumber;
	}
	public void setContainerNumber(String containerNumber) {
		this.containerNumber = containerNumber;
	}
	
}
