package com.matson.tos.exception;

public class NewVesselFileError {

	private String fileName;
	private String message;
	//
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
