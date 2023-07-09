package com.kormapper.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * For various operations a default constructor must be implemented. The MappingConstructor annotation
 * just enforces the programmer to declare a default constructor.
 * @author leonhardmuellauer
 */
@Retention(RUNTIME)
@Target(CONSTRUCTOR)
public @interface MappingConstructor { }
