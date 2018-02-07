package com.matson.tos.exception;

public class TosException extends Exception {
	static final long serialVersionUID = 123456789L;
	
	public TosException() {
	
	}
	
	public TosException( String s){
		super(s);
	}
}
