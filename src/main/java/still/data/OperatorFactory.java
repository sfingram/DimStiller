package still.data;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import still.gui.DSArgs;

/**
 * 
 * Factory methods that return operators given a string name description
 * 
 * @author sfingram
 *
 */
public class OperatorFactory {

	/** This class implements the FilenameFilter interface.

	 * The Accept method only returns true for .java , .jar and .class files.

	 */

	class OnlyJava implements FilenameFilter {

	    public boolean accept(File dir, String s) {
            try {
                System.out.println("dir = " + dir.getCanonicalPath() + " s = " + s);
            } catch( Exception e ) {
                e.printStackTrace();
            }
	        if ( s.endsWith("Op.class") )

	            return true;

	        // others: projects, ... ?

	        return false;

	    }

	}
	
	public ArrayList<String> classes = null;
	public ArrayList<String> menu_names = null;
	
	public OperatorFactory( String[] plugin_dirs ) {
		
		classes 	= new ArrayList<String>();
		menu_names 	= new ArrayList<String>();
		HashMap<String,String> menu_name_hash = new HashMap<String,String>(); 
		for( String plugin_dir : plugin_dirs ) {
			
			String[] file_ns = new File(plugin_dir).list( new OnlyJava() );
            try {
                System.out.println((new File(plugin_dir)).getCanonicalPath());
            } catch( Exception e ) {
                e.printStackTrace();
            }
			for( String file_n : file_ns ) {
								
				String class_str = file_n.substring(0, file_n.indexOf('.') ); 
				try {
					
					Class op_class = Class.forName( "still.operators."+class_str );
					Method m = op_class.getMethod("getMenuName");
					menu_names.add( (String) m.invoke( null ) );
					
					menu_name_hash.put((String) m.invoke( null ), class_str);
					
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}
		
		// Sort the menu names (so operator types are grouped together)
		
		Collections.sort( menu_names );
		for( String menu_name : menu_names ) {
			
			classes.add( menu_name_hash.get(menu_name) );
		}
	}
	
	public static String getOperatorMenuName( String op_name ) {
		
		try {
			
			Class op_class = Class.forName( "still.operators."+op_name );
			Method m = op_class.getMethod("getMenuName");
			return (String) m.invoke( null );
		} catch (Exception e) {

			e.printStackTrace();
		} 
		
		return null;
	}
	
	/**
	 * Factory method for creating any operator
	 * 
	 * @param op_name - the operator name
	 * @param input - the input table (null if none)
	 * @param isActive - true if you want the new operator the be activated or not
	 *  
	 * @return the newly created operator
	 */
	public Operator makeOperator( String op_name, Table input, boolean isActive ) {
		
		try {
		
			Class op_class = Class.forName( "still.operators."+op_name );
			Constructor ctor = op_class.getDeclaredConstructor(Table.class,Boolean.TYPE);
			return (Operator) ctor.newInstance(input,isActive);		
		} catch (Exception e) {

			e.printStackTrace();
		} 
		
		return null;
	}
	
	/**
	 * Factory method for creating any operator
	 * 
	 * @param op_name - the operator name
	 * @param input - the input table (null if none)
	 * @param isActive - true if you want the new operator the be activated or not
	 *  
	 * @return the newly created operator
	 */
	public Operator makeSavedOperator( String op_name, Table input, boolean isActive, String paramOperator ) {
		
		try {
		
			Class op_class = Class.forName( "still.operators."+op_name );
			Constructor ctor = op_class.getDeclaredConstructor(Table.class,Boolean.TYPE,String.class);
			return (Operator) ctor.newInstance(input,isActive,paramOperator);		
		} catch (Exception e) {

			e.printStackTrace();
		} 
		
		return null;
	}
	
	/**
	 * 
	 * Debug main tester
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		// parse input arguments
		System.out.print("Creating Factory...");
		DSArgs ds_args = new DSArgs(args);
		OperatorFactory of = new OperatorFactory( ds_args.plugin_dirs );
		System.out.println("done.");
		
		// list the found classes
		System.out.println("Loaded Classes:");
		for( String fclass : of.classes ) {
			
			System.out.println("\t"+fclass);
		}
		System.out.println("With Menu Names:");
		for( String mname : of.menu_names ) {
			
			System.out.println("\t"+mname);
		}
		
		// load a table
		System.out.print("Loading a table...");
		Table table = TableFactory.fromCSV( ds_args.input_file, ds_args.skiplines );
		System.out.println("done.");
		
		// load an operator
		System.out.print("Making an Operator...");
		Operator op = of.makeOperator("NormalizeOp", table, true);		
		System.out.println(" " + (op) + " done.");		
	}
}
