package com.kormapper.model;

public enum Mode {
	
	/** The default behavior of the KORGenerator */
	DEFAULT(""),
	/** Implements the ON CONFLICT clause to SQL-INSERT Statements */
	IGNORE ("OR IGNORE");
	
	private String value;
		
	private Mode(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the String value of the enum
	 * @return the String value of the enum
	 */
	public String getValue() {
		return value;
	}
}
