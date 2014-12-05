package still.flow;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import still.expression.Expression;


/**
 * 
 * Workflow Factory methods
 * 
 * @author sfingram
 *
 */
public class WorkflowFactory {

	public ArrayList<String> workflow_names = null;
	public ArrayList<Workflow> workflows = null;
	public String[] wkflow_dirs = null;
	
	/** This class implements the FilenameFilter interface.
	 *
	 */

	class OnlyWKFL implements FilenameFilter {

	    public boolean accept(File dir, String s) {

	        if ( s.endsWith(".wkfl") )

	            return true;

	        return false;

	    }

	}

	/**
	 * 
	 * @param wkflow_dirs
	 */
	public WorkflowFactory( String[] wkflow_dirs ) {
		
		workflow_names = new ArrayList<String>();
		workflows = new ArrayList<Workflow>();
		this.wkflow_dirs = wkflow_dirs;
		
		// check each workflow dir for valid workflow files
		
		for( String wkflow_dir: wkflow_dirs ) {
			
			String[] file_ns = new File(wkflow_dir).list( new OnlyWKFL() );
			for( String file_n : file_ns ) {

				Workflow wflow = new Workflow( new File( wkflow_dir, file_n) );
				workflows.add( wflow );
				workflow_names.add( wflow.name );
			}
		}
	}
	
	/**
	 * 
	 * @param workflow_name
	 * @return
	 */
	public Workflow getWorkflow( String workflow_name ) {
		
		for( Workflow workflow : workflows ) {
			
			if( workflow.name.equalsIgnoreCase( workflow_name ) ) {
				
				return workflow;
			}
		}
		
		return null;
	}
	
	/**
	 * Given an expression, save it to a file
	 * 
	 * @param ex
	 */
	public void saveWorkflow( Expression ex ) {
		
		Workflow workflow = new Workflow( ex );
		
		// get the name of the workflow
		
		String workflowName = (String)JOptionPane.showInputDialog(
                "Please Enter the name of the workflow",
                "MyWorkflow");
		
		if( workflowName == null ) {
			
			return;
		}
		else {
			
			workflow.name = workflowName;
		}

		// get the directory for the workflow
		
		Object[] possibleValues = wkflow_dirs;
		Object selectedValue = null;
		if( wkflow_dirs.length == 1 ) {
			
			selectedValue = wkflow_dirs[0];
		}
		else {
			selectedValue = JOptionPane.showInputDialog(null,
			            "Please choose a workflow directory in which to save", "Workflow Directory",
			            JOptionPane.INFORMATION_MESSAGE, null,
			            possibleValues, possibleValues[0]);
		}
		
		if( selectedValue == null ) {
			
			return;
		}
		
		// save the workflow
		
		File f = new File( (String)selectedValue, workflow.name+".wkfl");
		workflow.saveToFile( f );
	}
	
}
