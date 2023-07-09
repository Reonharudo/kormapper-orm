package com.kormapper.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface OneToMany {
	/**
	 * The sample class is used to create the objects in the list. <br>
	 * The objects are the records in the n-side of the 1:n relationship.
	 * @return a sample class
	 */
	Class<?> sample(); 
	
	/**
	 * The foreign key in the 1:n relationship
	 * @return the foreign key name
	 */
	String columnName();
	
	/**
	 * The primary key in the 1:n relationship
	 * @return the primary key name
	 */
	String referencedColumnName();
}
