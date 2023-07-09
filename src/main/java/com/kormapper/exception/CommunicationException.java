package com.kormapper.exception;

/**
 * Thrown when a connection error occurs
 * @author leonhardmuellauer
 */
public class CommunicationException extends Exception {

	private static final long serialVersionUID = 1L;

	public CommunicationException(String message) {
		super(message);
	}
	
}
