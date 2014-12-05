package still.flow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import still.data.Operator;
import still.data.OperatorFactory;
import still.expression.Expression;

/**
 * 
 * Class encapsulating the DimStiller workflow concept.
 * Workflows are identified by a name and contain an ordered 
 * list of operators that are applied to an input table.
 * 
 * Workflows are saved to disk in the following simple format:
 * the first line contains the the characters WORKFLOW
 * the second line contains the name of the workflow
 * The remaining lines contain the menu names and the
 * class names of the operators, one class name per line
 * 
 * @author sfingram
 *
 */
public class Workflow {

	public ArrayList<String> operators = null;
	public ArrayList<String> menu_names = null;
	public String name = null;	
	
	/**
	 * Build a workflow from a workflow file
	 * 
	 * @param filename
	 */
	public Workflow( 	File filename ) {
		
		try {
			
			BufferedReader bf = new BufferedReader(new FileReader(filename));
			String line = bf.readLine(); 
			if( line.equalsIgnoreCase("WORKFLOW") ) {

				line = bf.readLine();
				name = line;
				operators = new ArrayList<String>();
				menu_names = new ArrayList<String>();
				
				while( (line = bf.readLine()) != null ) {
					
					String[] fields = line.split(";");
					operators.add(fields[fields.length-1]);
					menu_names.add(fields[0]);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}		
	}
	
	/**
	 * Save a workflow to a workflow file f
	 * 
	 * @param f
	 */
	public void saveToFile( File f ) {
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write( "WORKFLOW\n" );
			bw.write( this.name + "\n" );
			for( String operator : this.operators ) {
				
				bw.write( OperatorFactory.getOperatorMenuName(operator) + ";" + operator + "\n" );
			}
			bw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * Construct a nameless workflow
	 * 
	 * @param ex
	 */
	public Workflow( Expression ex ) {
		
		operators = new ArrayList<String>();
		menu_names = new ArrayList<String>();
		for( Operator op : ex.operators ) {
			
			try {
				
				Class op_class = op.getClass();
				Method m = op_class.getMethod("getMenuName");
				menu_names.add( (String) m.invoke( null ) );
				operators.add( op.getClass().getName().split("\\.")[op.getClass().getName().split("\\.").length-1] );
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}
}
