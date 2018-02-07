/**
 * 
 */
package com.matson.tos.exception;

/**
 * @author AZA
 *
 */
public class PortNotFoundException extends Exception {

	/**
	 * 
	 */
	public PortNotFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public PortNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return super.getMessage();
	}

	/**
	 * @param cause
	 */
	public PortNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PortNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
