package com.kormapper.exception;

/**
 * Thrown when errors related to the KORMapper-API mechanism occurs.
 * @author leonhardmuellauer
 */
public class KORException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public KORException(String message) {
		super(message);
	}

}
