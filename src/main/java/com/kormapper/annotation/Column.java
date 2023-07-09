package com.kormapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * The name of the column in the database.
	 * @return the column name of which the fields corresponds to
	 */
	String name();
	
	/**
	 * Says whether the column is a primary key or not
	 * @return if it is a primary key or not
	 */
	boolean isPrimaryKey() default false;
}
