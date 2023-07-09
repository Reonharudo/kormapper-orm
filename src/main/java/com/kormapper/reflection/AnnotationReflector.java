package com.kormapper.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kormapper.annotation.Column;
import com.kormapper.annotation.OneToMany;
import com.kormapper.annotation.OneToOne;
import com.kormapper.annotation.Table;
import com.kormapper.exception.KORException;

/**
 * Provides methods that returns useful information about KORBridge-Class which may be annotated in accordance
 * to the KOR-Guidelines.
 * @author leonhardmuellauer
 */
public class AnnotationReflector {
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Instance Methods													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * Returns the name-value of a annotated field and the corresponding field-value as a HashMap
	 * The key is the column name and value the field value.
	 * @param object in which the columns are searched
	 * @return a filled or empty HashMap
	 * @throws KORException when the retrieve of the field value failed due to a nonexistent get-method
	 */
	public HashMap<String, String> columns (Object object) throws KORException {
		HashMap<String, String> erg = new HashMap<String, String>();
		for(Field field : object.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(Column.class)) {
				Object retVal = runGetter(field, object);
				erg.put(field.getAnnotation(Column.class).name(), retVal != null ? retVal.toString() : null);
			}
		}
		return erg;
	}
	
	/**
	 * Returns the name-value of a annotated field and the corresponding field-value as a HashMap. <br>
	 * The key is the column name and value the field value.
	 * @param object in which the columns are searched
	 * @return a filled or empty HashMap
	 * @throws KORException when a primary key is null
	 */
	public HashMap<String, String> primaryKeys (Object object) throws KORException {
		try {
			HashMap<String, String> erg = new HashMap<String, String>();
			for(Field field : object.getClass().getDeclaredFields()) {
				if(field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).isPrimaryKey() == true) {
					erg.put(field.getAnnotation(Column.class).name(), runGetter(field, object).toString());
				}
			}
			return erg;
		} catch(NullPointerException e) {
			throw new KORException("A primaryKey may not be null");
		}
	}
	
	/**
	 * Returns all OneToOne annotations on the object argument wrapped in a List
	 * @param reference in which the annotation should be searched
	 * @return a filled or empty List
	 */
	public List<OneToOne> oneToOnes(Object reference){
		List<OneToOne> erg = new ArrayList<>();
		for(OneToOne oneToOne : reference.getClass().getDeclaredAnnotationsByType(OneToOne.class)) {
			erg.add(oneToOne);
		}
		return erg;
	}
	
	/**
	 * Returns all OneToMany annotations on the object argument wrapped in a List
	 * @param reference in which the annotation should be searched
	 * @return a filled or empty List
	 */
	public List<OneToMany> oneToManys(Object reference){
		List<OneToMany> erg = new ArrayList<>();
		for(OneToMany oneToMany : reference.getClass().getDeclaredAnnotationsByType(OneToMany.class)) {
			erg.add(oneToMany);
		}
		return erg;
	}
	
	/**
	 * Returns the name value of a object annotated with the Table annotation
	 * @param object in which the value is retrieved
	 * @return the name value of the Table-Annotation
	 */
	public String tableNameOf(Object object) {
		return object.getClass().getAnnotation(Table.class).name();
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Static Methods													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * Returns the value of the corresponding get-method of a field in the given object
	 * @param field the corresponding field of the get-method
	 * @param reference the object in which the get-method should be invoked
	 * @return the value of the field wrapped in a object
	 * @throws KORException when the corresponding getMethod could not be found
	 */
	public static Object runGetter(Field field, Object reference) throws KORException
	{
	    for (Method method : reference.getClass().getDeclaredMethods()){
	        if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))){
	            if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())){
	                try{
	                    return method.invoke(reference);
	                } catch (IllegalAccessException | InvocationTargetException e){
	                	e.printStackTrace();
	                }
	            }
	        }
	    }
	    throw new KORException("Could not find a GET method for the field "+field.getName());
	}
	
	/**
	 * Returns the value of the corresponding set-method of a field in the given object
	 * @param field the corresponding field of the set-method
	 * @param reference the object in which the set-method should be invoken
	 * @param value the value that should be passed onto the set-method
	 */
	public static void runSetter(Field field, Object reference, Object value){
	    for (Method method : reference.getClass().getDeclaredMethods()){
	        if ((method.getName().startsWith("set")) && (method.getName().length() == (field.getName().length() + 3))){
	            if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())){
	                try{
	                	method.invoke(reference, value); //?? why does it work tho
	                } catch (IllegalAccessException | InvocationTargetException e){
	                	e.printStackTrace();
	                }
	            }
	        }
	    }
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Field Method 													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	
	/**
	 * Returns the field of the given object which is annotated by column and equals the given colName
	 * @param colName search condition for the field
	 * @param reference in which the search occurs
	 * @return the field or null
	 */
	public static Field fieldOf(String colName, Object reference) {
		for(Field field : reference.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(Column.class)) {
				if(field.getAnnotation(Column.class).name().equals(colName)) {
					return field;
				}
			}
		}
		return null;
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * OneToOne- / OneToMany Methods									     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * Returns all OneToOne annotated fields wrapped in a HashMap of the given object<br>
	 * Key is the referenced field and the value is the OneToOne annotation
	 * @param reference in which the search occurs
	 * @return a HashMap filled with fields or null when empty
	 */
	public static HashMap<Field, OneToOne> fieldsOfOTN(Object reference) {
		HashMap<Field, OneToOne> erg = new HashMap<>();
		for(Field field : reference.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(OneToOne.class)) {
				erg.put(field, field.getAnnotation(OneToOne.class));
			}
		}
		return erg;
	}
	
	/**
	 * Returns all OneToMany annotated fields wrapped in a HashMap of the given object<br>
	 * Key is the referenced field and the value is the OneToMany annotation
	 * @param reference in which the search occurs
	 * @return a HashMap filled with fields or null when empty
	 */
	public static HashMap<Field, OneToMany> fieldsOfOTM(Object reference) {
		HashMap<Field, OneToMany> erg = new HashMap<>();
		for(Field field : reference.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(OneToMany.class)) {
				erg.put(field, field.getAnnotation(OneToMany.class));
			}
		}
		return erg;
	}
}
