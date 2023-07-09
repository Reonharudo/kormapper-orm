package com.kormapper.exception;

/**
 * Thrown when arguments are the source of an error.
 * @author leonhardmuellauer
 */
public class ParamException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ParamException(String message) {
		super(message);
	}

}
