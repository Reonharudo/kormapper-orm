package com.kormapper.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kormapper.annotation.OneToMany;
import com.kormapper.annotation.OneToOne;
import com.kormapper.exception.KORException;
import com.kormapper.exception.ParamException;
import com.kormapper.reflection.AnnotationReflector;


/**
 * KORGenerator - Generates SQL Statements of given objects
 * @author leonhardmuellauer
 */
public class KORGenerator {
	
	private AnnotationReflector ar = new AnnotationReflector();
	private Mode mode;
    
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * Constructor					 									     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Instantiates a KORGenerator. The defined mode heavily influences how the KORGenerator generates
	 * the SQL Statements. <br>
	 * <b>DEFAULT</b> the default mode of the KORGenerator. This should be equal to the users working expectation<br>
	 * <b>IGNORE</b> the result of INSERT Statements is ignored thus no exception will be thrown
	 * @param mode the mode of this KORGenerator
	 */
	public KORGenerator(Mode mode) {
		setMode(mode);
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * Query, Insert, Update, Delete									     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * Generates the INSERT-Statement of the given object
	 * @param object on which the INSERT-Statement should be generated 
	 * @return SQL INSERT-Statement
	 */
    public String generateInsert(Object object) throws KORException {
    	String sql = "INSERT "+mode.getValue()+" INTO "+ar.tableNameOf(object)+"(";
    	ArrayList<String> colName = new ArrayList<>();
    	colName.addAll(ar.columns(object).keySet());
    	for(int i = 0; i < colName.size(); i++) {
    		sql += colName.get(i);
    		sql += i == (colName.size() -1) ? ") VALUES(" : ",";
    	}
    	
    	ArrayList<String> values = new ArrayList<>();
    	values.addAll(ar.columns(object).values());
    	for(int i = 0; i < values.size(); i++) {
    		sql += asSQL(values.get(i));
    		sql += i == (values.size() -1) ? ");" : ",";
    	}
    	return sql+fillInsert(object);
    }
    
    /**
     * Returns multiple insert statements for OneToOne/-Many wrapped objects
     * @param object on which the insert statements for OneToOne/-Many may be created
     * @return multiple SQL statements or an empty string
     */
    private String fillInsert(Object object) throws KORException {
    		String sql = "";
        	for(Field otnField : AnnotationReflector.fieldsOfOTN(object).keySet()) { //Field value must be a single object
        		sql += generateInsert(AnnotationReflector.runGetter(otnField, object));
        	}
        	for(Field otmField : AnnotationReflector.fieldsOfOTM(object).keySet()) { //Field value must be a List
        		List<?> values = (List<?>)AnnotationReflector.runGetter(otmField, object);
        		for(Object val : values) {
        			sql += generateInsert(val);
        		}
        	}
        	return sql+";";	
    }
    
    /**
	 * Generates the UPDATE-Statement of the given object.
	 * @param object on which the UPDATE-Statement should be generated 
	 * @return SQL UPDATE-Statement
	 * @throws KORException when a primary key is null
	 */
    public String generateUpdate(Object object) throws KORException {
    	String sql = "UPDATE "+ar.tableNameOf(object)+ " SET ";
    	for (Map.Entry<String, String> pair : ar.columns(object).entrySet()) {
    		sql += pair.getKey() + " = '" + pair.getValue() + "',";
    	}
    	sql = sql.substring(0, sql.lastIndexOf(','))+" ";
    	//WHERE-Condition:
    	sql += "WHERE ";
    	for (Map.Entry<String, String> pair : ar.primaryKeys(object).entrySet()) {
    		sql += pair.getKey() + " = " + asSQL(pair.getValue())+",";
    	}
    	return sql.substring(0, sql.lastIndexOf(','))+";"+fillUpdate(object);
    }
    
    /**
     * Returns multiple update statements for OneToOne/-Many wrapped objects
     * @param object on which the update statements for OneToOne/-Many may be created
     * @return multiple SQL statements or an empty string
     */
    private String fillUpdate(Object object) throws KORException {
    	try {
    		String sql = "";
        	for(Field oto : AnnotationReflector.fieldsOfOTN(object).keySet()) { //Field value must be a single object
        		sql += generateUpdate(oto.get(object));
        	}
        	for(Field otmField : AnnotationReflector.fieldsOfOTM(object).keySet()) { //Field value must be a List
        		List<?> values = (List<?>)AnnotationReflector.runGetter(otmField, object);
        		for(Object val : values) {
        			sql += generateUpdate(val);
        		}
        	}
        	return sql+";";	
    	}catch(Exception e) {
    		throw new KORException(e.getMessage());
    	}
    }
    
    /**
	 * Generates the DELETE-Statement of the given object. The WHERE condition is depended
	 * on the information if it is a primary key by the column annotation
	 * @param object on which the DELETE-Statement should be generated 
	 * @return SQL DELETE-Statement
	 * @throws KORException when a primary key is null
	 */
    public String generateDelete(Object object) throws KORException {
    	String sql = "DELETE FROM "+ar.tableNameOf(object)+" WHERE ";
    	for (Map.Entry<String, String> pair : ar.primaryKeys(object).entrySet()) {
    		sql += pair.getKey() + " = '" + pair.getValue() + "'AND ";
    	}
    	return fillDelete(object)+sql.substring(0, sql.lastIndexOf("AND"))+";";
    }
    
    /**
     * Returns multiple delete statements for OneToOne/-Many wrapped objects
     * @param object on which the update statements for OneToOne/-Many may be created
     * @return multiple SQL statements or an empty string
     */
    private String fillDelete(Object object) throws KORException {
    	try {
    		String sql = "";
        	for(Field oto : AnnotationReflector.fieldsOfOTN(object).keySet()) { //Field value must be a single object
        		sql += generateDelete(oto.get(object));
        	}
        	for(Field otmField : AnnotationReflector.fieldsOfOTM(object).keySet()) { //Field value must be a List
        		List<?> values = (List<?>)AnnotationReflector.runGetter(otmField, object);
        		for(Object val : values) {
        			sql += generateDelete(val);
        		}
        	}
        	return sql+";";	}
    	catch(Exception e) {
    		throw new KORException(e.getMessage());
    		}
    	}
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * OneToOne- / OneToMany Select-Methods									 *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Generates a SQL-Statement that retrieves all records of the specified One To One Relationship
     * in the OneToOne annotation
     * @param reference the object on which the SELECT-Statement should be referred
     * @param oneToOne the annotation on the field that should be referred
     * @return a SELECT-Statement
     * @throws KORException when a MappingConstructor is missing or the field which is referred 
     * by ReferencedColumnName has no value
     */
    public String generateSelect(Object reference, OneToOne oneToOne) throws KORException {
    	try {
    		Object object = oneToOne.sample().getConstructor().newInstance();
        	String sql = "SELECT * FROM "+ar.tableNameOf(object)+" WHERE ";
        	for(Map.Entry<String, String> pk : ar.columns(reference).entrySet()) {
        		if(pk.getKey().equals(oneToOne.columnName())) {
        			sql += oneToOne.referencedColumnName() + "=" + asSQL(pk.getValue());
        			return sql;
        		}
        	}
    	} catch(Exception e) {
    		throw new KORException("A MappingConstructor is missing");
    	}
    	throw new KORException("ReferencedColumnName must have a value");
    }
    
    /**
     * Generates a SQL-Statement that retrieves all records of the specified One To One Relationship
     * in the OneToOne annotation
     * @param reference the object on which the SELECT-Statement should be referred
     * @param oneToOne the annotation on the field that should be referred
     * @return a SELECT-Statement
     * @throws KORException when a MappingConstructor is missing or the field which is referred 
     * by ReferencedColumnName has no value
     */
    public String generateSelect(Object reference, OneToMany oneToMany) throws KORException {
    	try {
    		Object object = oneToMany.sample().getConstructor().newInstance();
        	String sql = "SELECT * FROM "+ar.tableNameOf(object)+" WHERE ";
        	for(Map.Entry<String, String> pk : ar.primaryKeys(reference).entrySet()) {
        		if(pk.getKey().equals(oneToMany.referencedColumnName())) {
        			sql += oneToMany.columnName() + "=" + asSQL(pk.getValue());
        			return sql;
        		}
        	}
    	} catch(Exception e) {
    		throw new KORException("A MappingConstructor is missing");
    	}
    	throw new KORException("ReferencedColumName must have a value");
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * Help methods														     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Converts the given String in a sql compatible format
     * @param convert the string to be converted
     * @return null or the converted string
     */
    public String asSQL(String convert) {
    	if(convert != null) {
    		return "'"+convert+"'";
    	} else {
    		return null;
    	}
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * GET- and SET methods												     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Sets the mode of the KORGenerator
     * @param mode the mode of the KORGenerator
     */
    public void setMode(Mode mode) {
    	if(mode != null) {
    		this.mode = mode;
    	} else {
    		throw new ParamException("Error: Mode may not be null");
    	}	
    }
}
