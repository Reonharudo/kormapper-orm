package com.kormapper.model;



import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kormapper.annotation.OneToMany;
import com.kormapper.annotation.OneToOne;
import com.kormapper.exception.CommunicationException;
import com.kormapper.exception.KORException;
import com.kormapper.exception.ParamException;
import com.kormapper.reflection.AnnotationReflector;

/**
 * KORMapper stands for Karui Object Relational Mapper <br>
 * This class provides all the methods that are specified in the KORMapper-API-Guide
 * @author leonhardmuellauer
 */
public class KORMapper{
	
	private KORGenerator generator;
	private String url;
	
	private boolean isBuffering = false;
	private String sql = "";
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Constructors  													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
	 * Instantiates a KORMapper with the given URL and an internal KORGenerator in DEFAULT Mode
	 * @param url the URL to the database
	 */
	public KORMapper(String url) {
		setURL(url);
		generator = new KORGenerator(Mode.DEFAULT);
	}
	
	/**
	 * Instantiates a KORMapper with the given URL and mode
	 * @param url the URL to the database
	 * @param mode the mode of the underlying KORGenerator
	 */
	public KORMapper(String url, Mode mode) {
		setURL(url);
		generator = new KORGenerator(mode);
	}
	
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * KORMapper Query													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Returns the result of query into a ArrayList
     * @param query the SQL select statement
     * @param sample he build plan for the object
     * @param <T> This is the type parameter for building the instance
     * @return  a empty or filled list with classes instanced by the passed class sample
     * @throws KORException when the generated SQL-Statement is not accepted by the db
     */
    public <T> List<T> query(String query, Class<T> sample) throws KORException {
    	try(Connection conn = connect();
    		Statement stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(query);)
    	{
    		ResultSetMetaData rsmd = rs.getMetaData();
    		
    		List<T> erg = new ArrayList<>();
    		while(rs.next()) {
    			T instance  = sample.getConstructor().newInstance(); //MappingConstructor forces the declaration of a standard constructor
    			for(int i = 1; i <= rsmd.getColumnCount(); i++) {
    				String colName = rsmd.getColumnName(i);
    				Object value = rs.getObject(i);
    				
    				Field field = AnnotationReflector.fieldOf(colName, instance);
    				if(field != null) {
    					AnnotationReflector.runSetter(field, instance, value);	
    				}
    			}
    			fillQuery(instance);
    			erg.add(instance);
    		}
    		return erg;
    	} catch(SQLException e) {
    		throw new KORException("The KORGenerator was not able to generate a valid SQL-Statement. Check Annotation");
    	} catch(CommunicationException e) {
    		throw new KORException(e.getMessage());
    	} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new KORException("The passed class is not equal to the result");
		} 
    }
    
    /**
     * Assigns for all OneToOne or OneToMany annotated fields a value. <br>
     * For OneToOne the value is a single object. <br>
     * For OneToMany the value is a List with all the objects inside
     * @param object the object on which the fields should get a value
     * @throws KORException when the annotation of the KORBridge classes is faulty
     */
    private void fillQuery(Object object) throws KORException {
    	for(Map.Entry<Field, OneToOne> map : AnnotationReflector.fieldsOfOTN(object).entrySet()) {
    		AnnotationReflector.runSetter(map.getKey(), object, query(generator.generateSelect(object, map.getValue()), map.getValue().sample()).get(0));
    	}
    	for(Map.Entry<Field, OneToMany> map : AnnotationReflector.fieldsOfOTM(object).entrySet()) {
    		AnnotationReflector.runSetter(map.getKey(), object, query(generator.generateSelect(object, map.getValue()), map.getValue().sample()));
    	}
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   	 * 																		 *
   	 * KORMapper Insert, Update, Delete									     *
   	 * 																		 *
   	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Inserts the given object to the database when the internal state isBuffering is false otherwise
     * the insert command will be buffered
     * @param object the object to be inserted
     * @throws KORException when the generated SQL-Statement is not accepted by the database
     */
    public void insert(Object object) throws KORException {
    	sql += generator.generateInsert(object);
    	if(!isBuffering) {
    		executeUpdate(sql);
    	}
    }
    
    /**
     * Inserts the given objects. All of the Inserts are passed as a single transaction thus
     * increasing the performance significantly
     * @param objects the object to be inserted
     * @throws KORException when the generated SQL-Statement is not accepted by the db
     */
    public void insertAsTransaction(Object...objects) throws KORException {
    	startTransaction();
    	for(Object object : objects) {
    		sql += generator.generateInsert(object);
    	}
    	saveChanges(); 	
    }
    
    /**
     * Updates the given object to the database when the internal state isBuffering is false otherwise
     * the update command will be buffered
     * @param object on which the UPDATE-Statement should be generated
     * @throws KORException when the generated SQL-Statement is faulty, due to false Annotation
     * or when it could not be connected to the database
     */
    public void update(Object object) throws KORException {
    	sql += generator.generateUpdate(object);
    	if(!isBuffering) {
    		executeUpdate(sql);
    	}
    }
    
    /**
     * Updates the given objects. All of the Updates are passed as a single transaction thus
     * increasing the performance significantly
     * @param objects the object to be inserted
     * @throws KORException when the generated SQL-Statement is not accepted by the database
     */
    public void updateAsTransaction(Object...objects) throws KORException {
    	startTransaction();
    	for(Object object : objects) {
    		sql += generator.generateUpdate(object);
    	}
    	saveChanges(); 	
    }
    
    /**
     * Deletes the given object to the database when the internal state isBuffering is false otherwise
     * the delete command will be buffered <br>
     * The WHERE condition is constructed by the declaration of primary keys
     * of the column annotation.
     * @param object on which the UPDATE-Statement should be generated
     * @throws KORException when the generated SQL-Statement is faulty, due to false Annotation
     * or when it could not be connected to the database
     */
    public void delete(Object object) throws KORException{
    	sql += generator.generateDelete(object);
    	if(!isBuffering) {
    		executeUpdate(sql);
    	}
    }
    
    /**
     * Deletes the given objects. All of the Updates are passed as a single transaction thus
     * increasing the performance significantly
     * @param objects the object to be inserted
     * @throws KORException when the generated SQL-Statement is not accepted by the database
     */
    public void deleteAsTransaction(Object...objects) throws KORException {
    	startTransaction();
    	for(Object object : objects) {
    		sql += generator.generateDelete(object);
    	}
    	saveChanges(); 	
    }
	
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * KORMapper Modified JDBC Methods									     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Returns the single result of a SQL Query
     * @param sql the SQL Query
     * @return the single result wrapped in an object
     * @throws KORException when the SQL is incorrectly formatted or the connection to the database was not 
     * established
     */
    public Object query(String sql) throws KORException {
    	try(Connection conn = connect();
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)){
    		return rs.getObject(1);
    	} catch(SQLException e) {
    		throw new KORException("Error while constructing SQL-Statement: "+e.getMessage());
    	} catch(CommunicationException e) {
    		throw new KORException(e.getMessage());
    	}
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * KORMapper Internal Functions 									     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Issues the given SQL statement to the database
     * @param sql the SQL statement that is committed
     * @throws KORException when the issued SQL statement follows an database side error or when the connection
     * to the database was not established
     */
    public void initialize(String sql) throws KORException {
    	try(Connection conn = connect();
    		Statement stmt = conn.createStatement()){
    		stmt.execute(sql);
    	} catch(SQLException e) {
    		throw new KORException("Error while constructing SQL-Statement: "+e.getMessage());
    	} catch(CommunicationException e) {
    		throw new KORException(e.getMessage());
    	}  		
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * 																		 *
	 * Help Methods 													     *
	 * 																		 *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	
	/**
     * Connect to the database with the given URL
     * @return the Connection object
     */
    private Connection connect() throws CommunicationException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new CommunicationException("Error: Could not connect to "+url);
        }
        return conn;
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   	 * 																		 *
   	 * ModularKORMapper methods											     *
   	 * 																		 *
   	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    /**
     * Executes a DML-SQL-Operation to the database
     * @param sql a DML-SQL-Operation
     * @throws KORException when the passed SQL command is faulty or the connection to the 
     * database could not be established
     */
    public void executeUpdate(String sql) throws KORException {
    	try(Connection conn = connect();
    			Statement stmt = conn.createStatement()){
    		stmt.executeUpdate(sql);
    	} catch(SQLException e) {
    		throw new KORException("Error while constructing SQL-Statement: "+e.getMessage());
    	} catch(CommunicationException e) {
    		throw new KORException(e.getMessage());
    	}
    }
    
    /**
     * Starts a transaction and transmits the KORMapper to the isBuffering mode.
     */
    public void startTransaction() {
    	sql+="BEGIN TRANSACTION;";
    	isBuffering = true;
    }
    
    /**
     * Issues the buffered SQL commands which are wrapped in a finalized transaction to the database. <br>
     * And transmits the KORMapper isBuffering mode to false.
     * @throws KORException if there are issues with the generated SQL Statement
     */
    public void saveChanges() throws KORException {
    	sql += "END TRANSACTION;";
    	executeUpdate(sql);
    	isBuffering = false;
    	sql = "";
    }
    
    
    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  	 * 																		 *
  	 * GET- and SET Methods   											     *
  	 * 																		 *
  	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    
    
    /**
     * Returns the URL
     * @return path
     */
	public String geURL() {
		return url;
	}
	
	/**
	 * Sets the URL
	 * @param url the URL to be set
	 */
	public void setURL(String url) {
		if(url != null && url.length() > 0) {
			this.url = url;
		} else {
			throw new ParamException("Error: The length of the path string must be greater than 0");
		}
	}
	
	/**
	 * Sets a KORGenerator that is used internally to generate SQL statements
	 * @param korGenerator the KORGenerator to be set
	 */
	public void setKORGenerator(KORGenerator korGenerator) {
		if(korGenerator != null) {
			this.generator = korGenerator;
		} else {
			throw new ParamException("Error: The KORGenerator may not be null");
		}
	}
	
	/**
	 * Returns the specified KORGenerator of this KORMapper instance
	 * @return the specified KORGenerator 
	 */
	public KORGenerator getKORGenerator() {
		return generator;
	}
	
	/**
	 * Sets the isBuffering status
	 * @param isBuffering the boolean value to be set
	 */
	public void setIsBuffering(boolean isBuffering) {
		this.isBuffering = isBuffering;
	}
	
	/**
	 * Return the isBuffering status of this KORMapper instance
	 * @return the isBuffering boolean value
	 */
	public boolean getIsBuffering() {
		return isBuffering;
	}
}
