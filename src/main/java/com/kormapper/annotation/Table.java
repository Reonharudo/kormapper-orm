package com.kormapper.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Table {
	
	/**
	 * The name of the relation
	 * @return the name of the relation
	 */
	String name();
}
