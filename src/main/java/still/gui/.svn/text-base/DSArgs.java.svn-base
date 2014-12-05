package still.gui;

import still.data.Table.ColType;

/**
 * 
 * DimStiller argument structure.  Parses command-line arguments.
 * 
 * @author sfingram
 *
 */
public class DSArgs {

	
	public String[] 	plugin_dirs 	= null;		// plugin location directories for looking for classes
	public String[] 	wkflow_dirs 	= null;		// workflow location directories for looking for classes
	public String 		input_file 		= null;		// input csv file location
	public boolean 		is_error 		= false;	// is in error
	public int			skiplines		= 1;		// how many lines to do
	public boolean		has_columns		= false;	// The first nonskipped line will contain a list of column types
	
	/**
	 * 
	 * Pass in the args array from a static main method
	 * 
	 * @param args
	 */
	public DSArgs( String[] args ) {
		
		boolean inWk 	= false;	// next argument is for workflow directories
		boolean inDir 	= false;	// next argument is for directories
		boolean inInput = false;	// next argument is for input file
		boolean inSkiplines = false;// next argument is for skiplines count
		
		for( String arg : args ) {
			
			// determine if the argument has a "dash switch"
			
			if( arg.length() > 1 && arg.charAt(0) == '-' ) {

				// the next argument should *not* be a dash if we're expecting input
				
				if( inWk || inDir || inInput || inSkiplines ) {
					
					this.is_error = true;
				}

				// version switch
				
				if( arg.charAt(1) == 'V' ) {
					
					System.out.println("DimStiller Version " + DimStiller.VERSION_STRING );
					System.exit(0);
				}

				// workflow switch
				
				if( arg.charAt(1) == 'W' ) {
					
					if( arg.length() == 2 ) {
						
						inWk = true;
					}
					else {
						
						wkflow_dirs = arg.substring(2).split(":");
					}
				}
				
				// column line switch
				
				else if( arg.charAt(1) == 'C' ) {

					this.has_columns = true;
				}
				
				// directory switch
				
				else if( arg.charAt(1) == 'D' ) {
					
					if( arg.length() == 2 ) {
						
						inDir = true;
					}
					else {
						
						plugin_dirs = arg.substring(2).split(":");
					}
				}
				
				// input file switch
				
				else if( arg.charAt(1) == 'I' ) {
					
					if( arg.length() == 2 ) {
						
						inInput = true;
					}
					else {
						
						input_file = arg.substring(2);
					}
				}
				
				// input file switch
				
				else if( arg.charAt(1) == 'S' ) {
					
					if( arg.length() == 2 ) {
						
						inSkiplines = true;
					}
					else {
						
						this.skiplines = Integer.parseInt( arg.substring(2) );
					}
				}
				
				// error switch
				
				else {
					
					this.is_error = true;
				}
			}
			
			else if( inWk ) {
				
				wkflow_dirs = arg.split(":");
				inWk = false;
			}
			
			else if( inDir ) {
				
				plugin_dirs = arg.split(":");
				inDir = false;
			}
			
			else if ( inInput ) {
				
				input_file = arg;
				inInput = false;
			}
			
			else if ( inSkiplines ) {
				
				this.skiplines = Integer.parseInt( arg );
				inSkiplines = false;
			}
		}
		
		// check if we never got an expected argument
		
		if( inWk || inDir || inInput || inSkiplines || this.is_error ) {
			
			System.err.println("Error parsing input arguments.");
			this.is_error = true;
		}
	}
	
	/**
	 * 
	 * Test method
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		DSArgs dsargs = new DSArgs(args);
		
		System.out.println("*** TEST ROUTINE ***");
		System.out.println("Input File : " + dsargs.input_file);		
		System.out.println("Input Skiplines : " + dsargs.skiplines);		
		System.out.print("Input Dirs : ");
		if( dsargs.plugin_dirs == null ) {
			
			System.out.println( dsargs.plugin_dirs );
		}
		else {
			
			System.out.println();
			for( String dirs : dsargs.plugin_dirs ) {
				
				System.out.println( "\t" + dirs);
			}
		}
		System.out.print("Workflow Dirs : ");
		if( dsargs.wkflow_dirs == null ) {
			
			System.out.println( dsargs.wkflow_dirs );
		}
		else {
			
			System.out.println();
			for( String dirs : dsargs.wkflow_dirs ) {
				
				System.out.println( "\t" + dirs);
			}
		}
	}
}
