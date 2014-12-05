package still.expression;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;

import still.data.Operator;
import still.data.OperatorFactory;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.data.TableListener;

/**
 * 
 * Manages the input table and the list of operators that transform
 * the input table into new tables.
 * 
 * @author sfingram
 *
 */
public class Expression implements TableListener, Serializable {

	protected ArrayList<ExpressionListener> expressionListeners = null;
	public ArrayList<Operator> operators = null;
	public Table table = null;
	public String locationString = "";
	
	/**
	 * Render the expression as a string
	 */
	public String toString() {
		
		String output = "[Input:File]";		
		for( int i = 0; i < operators.size(); i++ ) {
			
			output += ( " -> " + operators.get(i).toString() );
		}
		
		return output;
	}
	
	/**
	 * Create an expression from a saved file
	 * 
	 * @param inputFile
	 */
	public static Expression loadExpression( File inputFile, OperatorFactory opFactory ) {
		
		Expression retex = null;
		
//		// open the file
//		
//		try {
//			ObjectInputStream is = new ObjectInputStream( new FileInputStream( inputFile ) );
//			
//			retex = (Expression) is.readObject();
//			retex.expressionListeners = new ArrayList<ExpressionListener>();
//		} catch (Exception e) {
//
//			e.printStackTrace();
//		}		
//				
//		return retex;
		
		try {
			
			BufferedReader bf = new BufferedReader(new FileReader(inputFile));
			String line = bf.readLine(); 
			if( line.equalsIgnoreCase("EXPRESSION") ) {

				line = bf.readLine();
				String locStr = line;
				
				// load up the table
				
				Table table = null;
				if( !TableFactory.hasTypes(line, "[ \\t\\n\\x0B\\f\\r,]+") ) {
					
		    		table = TableFactory.fromCSV( line, 1 );
		    	}
		    	else {
		    		
		    		table = TableFactory.fromCSVTypes( line, 1, true );
					
				}

				line = bf.readLine();
				String[] params = line.split(";");
				if( params.length < 2 ) {
					String[] tparams = new String[2];
					tparams[0] = params[0];
					tparams[1] = "";
					params = tparams;
				}
				Operator op = opFactory.makeSavedOperator( params[0], table, true, params[1]);
				while( (line = bf.readLine()) != null ) {
					
					params = line.split(";");
					if( params.length < 2 ) {
						String[] tparams = new String[2];
						tparams[0] = params[0];
						tparams[1] = "";
						params = tparams;
					}
					op = opFactory.makeSavedOperator( params[0], op, true, params[1]);
				}
				retex = new Expression( op );
				retex.locationString = locStr;
			}
		} catch (Exception e) {

			e.printStackTrace();
		}		
		
		return retex;
	}
	
	/**
	 * Save an existing expression to a file 
	 * 
	 * @param saveex
	 * @param outputFile
	 */
	public static void saveExpression( Expression saveex, File outputFile ) {
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			bw.write( "EXPRESSION\n" );
			bw.write( saveex.locationString+"\n" );			
			for( Operator op : saveex.operators ) {
				
				bw.write( op.getClass().getName().split("\\.")[op.getClass().getName().split("\\.").length-1] + ";" + op.getSaveString() + "\n" );
			}
			bw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		
//		// this is slightly tricky because we want to destroy all links from the expression
//		// to GUI components that we're not interested in saving.  This means temporarily detaching
//		// links to these components before we serialize the object and then re-attaching these
//		// links after we finish writing to disk.
//		
//		
//		// remove links from expression
//		ArrayList<ExpressionListener> oldExListeners = saveex.expressionListeners;
//		saveex.expressionListeners = null;
//		
//		// remove links from tables and operators
//		ArrayList<ArrayList<ActionListener>> oldActListeners = new ArrayList<ArrayList<ActionListener>>();
//		ArrayList<ArrayList<TableListener>> oldTabListeners = new ArrayList<ArrayList<TableListener>>();
//		
//		for( int i = 0; i < (saveex.operators.size() + 1); i++ ) {
//			
//			oldActListeners.add( new ArrayList<ActionListener>() );
//			oldTabListeners.add( new ArrayList<TableListener>() );
//			if( i < saveex.operators.size() ) {
//
//				int j = 0;
//				while( j < saveex.operators.get(i).getActionListeners().size() ) {
//					
//					ActionListener al = saveex.operators.get(i).getActionListeners().get(j);
//					if( ! ( (al instanceof Table) || (al instanceof Expression) ) ) {
//						
//						oldActListeners.get(i).add(al);
//						saveex.operators.get(i).getActionListeners().remove(j);
//						j--;
//					}
//					j++;
//				}
//				
//				j = 0;
//				while( j < saveex.operators.get(i).getDownstreamListeners().size() ) {
//					
//					TableListener tl = saveex.operators.get(i).getDownstreamListeners().get(j);
//					if( ! ( (tl instanceof Table) || (tl instanceof Expression) ) ) {
//						
//						oldTabListeners.get(i).add(tl);
//						saveex.operators.get(i).getDownstreamListeners().remove(j);
//						j--;
//					}
//					j++;
//				}				
//			}	
//			else {
//				
//				int j = 0;
//				while( j < saveex.table.getActionListeners().size() ) {
//					
//					ActionListener al = saveex.table.getActionListeners().get(j);
//					if( ! ( (al instanceof Table) || (al instanceof Expression) ) ) {
//						
//						oldActListeners.get(i).add(al);
//						saveex.table.getActionListeners().remove(j);
//						j--;
//					}
//					j++;
//				}
//				
//				j = 0;
//				while( j < saveex.table.getTableListeners().size() ) {
//					
//					TableListener tl = saveex.table.getTableListeners().get(j);
//					if( ! ( (tl instanceof Table) || (tl instanceof Expression) ) ) {
//						
//						oldTabListeners.get(i).add(tl);
//						saveex.table.getTableListeners().remove(j);
//						j--;
//					}
//					j++;
//				}				
//			}			
//		}
//		
//		try {
//			
//			ObjectOutputStream os = new ObjectOutputStream( new FileOutputStream( outputFile ) );
//			os.writeObject( saveex );
//		}
//		catch( Exception e ) {
//			
//			e.printStackTrace();
//		}
//		
//		
//		// re-attach links to expression
//		
//		saveex.expressionListeners = oldExListeners;
//		
//		// re-attach links to table and operators
//		
//		for( int i = 0; i < (saveex.operators.size() + 1); i++ ) {
//			
//			if( i < saveex.operators.size() ) {
//
//				for( ActionListener al : oldActListeners.get(i) ) {
//					
//					saveex.operators.get(i).getActionListeners().add(al);
//				}
//				for( TableListener tl : oldTabListeners.get(i) ) {
//					
//					saveex.operators.get(i).getDownstreamListeners().add(tl);
//				}
//			}	
//			else {
//				
//				for( ActionListener al : oldActListeners.get(i) ) {
//					
//					saveex.table.getActionListeners().add(al);
//				}
//				for( TableListener tl : oldTabListeners.get(i) ) {
//					
//					saveex.table.getTableListeners().add(tl);
//				}
//			}
//			
//		}
	}
	
	/**
	 * Generate a new empty expression from a new table
	 * 
	 * @param inputTable
	 */
	public Expression( Table inputTable ) {
		
		this.table = inputTable;
		if( this.table != null ) {
			
			this.table.addTableListener(this);
		}
		
		this.operators = new ArrayList<Operator>();
		this.expressionListeners = new ArrayList<ExpressionListener>();
	}

	/**
	 * Build an arraylist of operators given an operator and return
	 * the base table from the expression.
	 * 
	 * @param list
	 * @param o
	 * @return
	 */
	public static Table recurseOperatorList( ArrayList<Operator> list, Operator o ) {

		Table retTable = null;
		
		if( o.getInput() instanceof Operator ) {
			
			retTable = recurseOperatorList( list, (Operator) o.getInput() );
		}
		else {
			
			retTable = o.getInput();
		}
		list.add(o);
		
		return retTable;
	}
	
	/**
	 * Generate a new expression from a (set of) operator(s)
	 * 
	 * @param inputTable
	 */
	public Expression( Operator o ) {
		
		
		operators = new ArrayList<Operator>();
		table = Expression.recurseOperatorList(operators, o);
				
		table.addTableListener(this);
		
		if( operators.size() > 0 ) {

			table.addTableListener( operators.get(0) );
		}
		
		// build the doubly linked list of up/downstream listeners
		for( int i = 0; i < operators.size(); i++ ) {
			
			operators.get(i).addTableListener(this);
			
			if( i < operators.size()-1 && operators.size() > 1) {
				
				operators.get(i).addUpstreamListener( operators.get(i+1) );
			}
			if( i > 0 ) {
				
				operators.get(i-1).addDownstreamListener( operators.get(i) );
			}
		}
		
		expressionListeners = new ArrayList<ExpressionListener>();			
	}
	
	public void addExpressionListener( ExpressionListener e ) {
		
		expressionListeners.add(e);
	}
	
	public void activateNextOperator( ) {
		
		// find the appropriate operator
		for( Operator oper : operators ) {
			
			if( !oper.isActive() ) {
				
				// activate it
				oper.activate();
				// send an event
				for( int i = 0; i < expressionListeners.size(); i++ ) {
					
					expressionListeners.get(i).expressionChanged( 
							new ExpressionEvent( this, ExpressionEvent.ExpressionEventType.TERM_ACTIVATED, oper ) );
				}
				break;
			}
		}
	}

	public void addOperator( Operator operator, Table preTable, boolean beforePreTable ) {

		if( beforePreTable && preTable != null && operators.size() > 0 ) {
			
			// determine the operator before preTable
			Operator pre = null;
			for( Operator curr : operators ) {
				
				if( curr == preTable ) {
					
					if( pre == null ) {
						
						addOperator( operator, this.table );
					}
					else {
						
						addOperator( operator, pre );
					}
					
					return;
				}			
				
				pre = curr;
			}
		}
		else {
			
			addOperator( operator, preTable );
		}
	}
	
	/**
	 * Inserts an operator into an expression after the table/operator 
	 * preTable
	 * 
	 * 
	 * @param operator
	 * @param preTable
	 */
	public void addOperator( Operator operator, Table preTable ) {
		
		operator.addTableListener(this);
		
		// tack it on to the end
		if( preTable == null ) {
			
			if( operators.size() > 0 ) {
				
				operators.get(operators.size()-1).addDownstreamListener(operator);
				operator.addUpstreamListener(operators.get(operators.size()-1));
			}
			
			if (operators.size()>0) {
				
				operator.tableChange(operators.get(operators.size()-1));
			}
			
			operators.add( operator );							
		}
		// insert it into the expression
		else if( preTable instanceof Operator) {
			
			Operator preOp = (Operator) preTable; 
			preOp.addDownstreamListener(operator);
			operator.addUpstreamListener(preOp);
			operator.tableChange(preOp);
			if( operators.size()-1 > operators.indexOf(preOp) ) {
				
				preOp.removeDownstreamListener(operators.get( operators.indexOf(preOp)+1 ) );
				operator.addDownstreamListener(operators.get( operators.indexOf(preOp)+1 ) );
				operators.get( operators.indexOf(preOp)+1 ).addUpstreamListener( operator );
				operators.get( operators.indexOf(preOp)+1 ).removeUpstreamListener(preOp);
				operators.get( operators.indexOf(preOp)+1 ).tableChange(operator);
			}

			operators.add(operators.indexOf(preOp)+1, operator);			
		}
		// insert it at the beginning right after the input table
		else {
			
			table.addTableListener(operator);
			if( operators.size() > 0 ) {

				table.getTableListeners().remove(operators.get(0));
				operators.get(0).addUpstreamListener(operator);
				operator.addDownstreamListener(operators.get(0));
				operators.add(0, operator);				
				operators.get( 1 ).tableChange(operator);
			}
			else {
				
				operators.add(operator);
			}
		}		

		for( int i = 0; i < expressionListeners.size(); i++ ) {
			
			expressionListeners.get(i).expressionChanged( 
					new ExpressionEvent( this, ExpressionEvent.ExpressionEventType.TERM_ADDED, operator ) );
		}
	}
	
	/**
	 * Add an operator to the end of an expression
	 * 
	 * @param operator
	 */
	public void addOperator( Operator operator ) {
		
		this.addOperator(operator, null);
	}

	public void setTable( Table newInput ) {
		
		if( table != null ) {
			
			for( ActionListener al: table.getActionListeners() ){
				newInput.addActionListener(al);
				al.actionPerformed( new ActionEvent(newInput, ActionEvent.ACTION_PERFORMED, "") );
//				al.actionPerformed( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "") );
			}
			
			// transfer table listeners
			for( TableListener tl : table.getTableListeners() ) {
			
				newInput.addTableListener(tl);
			}
			Table oldTable = table;
			table = newInput;
			
			// inform table listeners
			for( TableListener tl : table.getTableListeners() ) {
				
				if(tl instanceof Operator) {
					
					((Operator)tl).tableChange(table);
				}
			}	
			
			// notify expression listeners that a new expression is in existence
			
			for( int i = 0; i < expressionListeners.size(); i++ ) {
				
				expressionListeners.get(i).expressionChanged( 
						new ExpressionEvent( this, ExpressionEvent.ExpressionEventType.NEW_INPUT, table, oldTable ) );
			}
		}
	}
	
	/**
	 * Remove an operator from an expression
	 * 
	 * @param operator
	 */
	public void delOperator( Operator operator ) {
		
		// rearrange list of operators so listeners point correctly
		
		int oIdx = operators.indexOf(operator);
		operators.get(oIdx).removeAllListeners();
		if( oIdx > 0 ) {
			
			operators.get(oIdx-1).removeDownstreamListener(operator);
			if( operators.size() > (oIdx+1) ) {
				
				operators.get(oIdx+1).addUpstreamListener(operators.get(oIdx-1));
				operators.get(oIdx-1).addDownstreamListener(operators.get(oIdx+1));
				operators.get(oIdx+1).tableChange(operators.get(oIdx-1));
			}
		}
		else {
			
			if( operators.size() > (oIdx+1) ) {
				operators.get(oIdx+1).addUpstreamListener(this);
				operators.get(oIdx+1).tableChange( table );
				table.getTableListeners().remove(operator);
				table.addTableListener(operators.get(oIdx+1));
			}
		}
		
		if( operators.size() > (oIdx+1) ) {
			
			operators.get(oIdx+1).removeUpstreamListener(operator);
		}
		operators.remove( operator );
		if( operator.getView() != null && operator.getView().getViewFrame() != null ) {
			
			operator.getView().getViewFrame().setVisible(false);
			operator.getView().getViewFrame().dispose();
		}
		if( operator.getView().isTorn ) {
			
			((Frame)(operator.getView().getParent())).dispose();
		}
		
		// notify expression listeners
		
		for( int i = 0; i < expressionListeners.size(); i++ ) {
			
			expressionListeners.get(i).expressionChanged( 
					new ExpressionEvent( this, ExpressionEvent.ExpressionEventType.TERM_REMOVED, operator ) );
		}
		
	}

	public void initExpression( ) {
		
		// notify expression listeners that a new expression is in existence
		
		for( int i = 0; i < expressionListeners.size(); i++ ) {
			
			expressionListeners.get(i).expressionChanged( 
					new ExpressionEvent( this, ExpressionEvent.ExpressionEventType.NEW_EXPRESSION, null ) );
		}
	}
	
	/**
	 * 
	 * Testing method
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		System.out.println( "TESTING Expression Class" );
		if (args.length > 0)
			System.out.println( "Loading File: " + args[0] );
		
		// load the expression
		
//		Expression e = new Expression( 
//							new IdentityOp ( 
//							new IdentityOp( TableFactory.fromCSV( args[0] ) ) ) );
//		
//		// display the input table size
//		
//		System.out.println("Operator String  : " + e );
//		System.out.println("Input Table size : " + e.table.rows() + " x " + e.table.columns() );
//		System.out.println("Operator Count   : " + e.operators.size());
//		for( int i = 0; i < e.operators.size(); i++ ) {
//
//			System.out.println("\tOp " + (i+1) + " size: " + e.operators.get(i).rows() + " x " + e.operators.get(i).columns() );
//		}
	}


	public void tableChanged( TableEvent tEvent ) {
	
		if( tEvent.type == TableEvent.TableEventType.REMOVE_ME ) {
			
			delOperator( tEvent.op );
		}

		if( tEvent.type == TableEvent.TableEventType.ADD_ME ) {
			
			this.addOperator( tEvent.op, (Table)tEvent.src, true );
		}
	}

}