package com.kormapper.ormapper;

import java.util.List;

import com.kormapper.annotation.Column;
import com.kormapper.annotation.MappingConstructor;
import com.kormapper.annotation.OneToMany;
import com.kormapper.annotation.Table;

@Table (name = "p_persons")
public class Person {
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Constructors 													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	@MappingConstructor
	public Person() { }
	
	public Person(String name, int alter, Letter... letters) {
		setName(name);
		setAlter(alter);
		setLetters(List.of(letters));
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Instance Variables										     		 *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	@Column (name = "p_name", isPrimaryKey = true)
	private String name;
	
	@Column (name = "p_alter")
	private int alter;
	
	@OneToMany(sample = Letter.class, columnName = "l_p_personname", referencedColumnName = "p_name")
	private List<Letter> letters;
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * GET- and SET methods												     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public int getAlter() {
		return alter;
	}

	public void setAlter(int alter) {
		this.alter = alter;
	}

	public List<Letter> getLetters() {
		return letters;
	}

	public void setLetters(List<Letter> letters) {
		this.letters = letters;
	}
	
	
}
