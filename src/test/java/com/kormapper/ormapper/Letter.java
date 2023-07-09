package com.kormapper.ormapper;

import com.kormapper.annotation.Column;
import com.kormapper.annotation.MappingConstructor;
import com.kormapper.annotation.Table;

@Table (name = "l_letters")
public class Letter {
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Constructors												     		 *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	@MappingConstructor
	public Letter() { }
	
	public Letter(String lettername, String personname, String text, int preis) {
		setLettername(lettername);
		setPersonname(personname);
		setText(text);
		setPreis(preis);
	}

	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Instance Variables										     		 *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	@Column(name = "l_lettername", isPrimaryKey = true)
	private String lettername;
	
	@Column(name = "l_p_personname")
	private String personname;
	
	@Column(name = "l_text")
	private String text;
	
	@Column(name = "l_price")
	private int preis;

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * GET- and SET methods										     		 *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	
	public String getLettername() {
		return lettername;
	}

	public void setLettername(String lettername) {
		this.lettername = lettername;
	}

	public String getPersonname() {
		return personname;
	}

	public void setPersonname(String personname) {
		this.personname = personname;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getPreis() {
		return preis;
	}

	public void setPreis(int preis) {
		this.preis = preis;
	}
	
	
}
