package still.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jblas.DoubleMatrix;

import still.gui.OperatorView;

public abstract class Operator implements Table, TableListener, Serializable {
	
	protected Map map 			= null;
	protected Function function 	= null;
	public Table input			= null;
	protected transient OperatorView view	= null;
	protected boolean isLazy		= false;
	protected DoubleMatrix matrix		= null;
	protected String description = "";
	protected String paramString = "";
	protected boolean isActive = true;
	protected int term_number = -1;
	protected int expression_number = -1;
	protected boolean updating = false;
	
	protected ArrayList<TableListener> upstreamListeners = null;
	protected ArrayList<TableListener> downstreamListeners = null;
	ArrayList<ActionListener> actionListeners = null;

	public boolean isUpdating() {
		
		return updating;
	}
	
	/**
	 * 
	 * Write the output table of an operator to a file
	 * 
	 * @param op
	 * @param f
	 */
	public static void writeOpToFile( Table op, File f ) {
		
		try {
		
			BufferedWriter bw = new BufferedWriter( new FileWriter( f ) );
			for( int j = -1; j < op.rows(); j++ ) {
				for( int i = 0; i < op.columns(); i++ ) {
					
					if( j < 0 ) {
						
						bw.write(op.getColName(i));
					}
					else {
						
						bw.write(""+op.getMeasurement(j, i));
					}
					
					if( i < op.columns()-1 ) {
						
						bw.write(",");
					}
				}
				bw.write( "\n" );
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * This class is called during de-serialization
	 * subclasses should override this method for special handling
	 */
	public void loadOperatorView() {
		
	}
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        
        loadOperatorView();
    }
	
	public Operator() {
		
		this.upstreamListeners = new ArrayList<TableListener>();
		this.downstreamListeners = new ArrayList<TableListener>();
		this.actionListeners = new ArrayList<ActionListener>();
	}
	
	public ArrayList<TableListener> getDownstreamListeners () {
		
		return downstreamListeners; 
	}
	
	public static String getMenuName() {
		return " ";
	}
	
	public int getTermNumber( ) {
		
		return this.term_number;
	}
	
	public int getExpressionNumber( ) {
		
		return this.expression_number;
	}
	
	/**
	 * Set the term number of the operator
	 * 
	 * @param term_number
	 */
	public void setTermNumber( int term_number ) {
		
		this.term_number = term_number;
		if( this.getView() != null ) {
			if( this.getView().getViewFrame() != null ) {
				
				this.getView().getViewFrame().setTitle("E"+getExpressionNumber()+":"+toString());
			}
		}
	}
	
	/**
	 * Set the expression number of the operator 
	 * 
	 * @param expression_number
	 */
	public void setExpressionNumber( int expression_number ) {
		
		this.expression_number = expression_number;
	}
	
	public int countDimType( ColType type ) {
		
		int num_type = 0;
		for( ColType t : input.getColTypes() ) {
			
			if( t == type ) {
				
				num_type++;
			}
		}
		
		return num_type;
	}

	public int countDimTypeInverse( ColType type ) {
		
		int num_type = 0;
		for( ColType t : input.getColTypes() ) {
			
			if( t != type ) {
				
				num_type++;
			}
		}
		
		return num_type;
	}
	
	public ArrayList<Integer> getDimTypeCols( ColType type ) {
		
		ArrayList<Integer> numerics = new ArrayList<Integer>();
		int i = 0;
		
		for( ColType t : input.getColTypes() ) {
		
			
			if( t == type ) {
				
				numerics.add(new Integer(i));
			}
			
			i++;
		}
		
		return numerics;
	}

	public ArrayList<Integer> getDimTypeColsInverse( ColType type ) {
		
		ArrayList<Integer> numerics = new ArrayList<Integer>();
		int i = 0;
		
		for( ColType t : input.getColTypes() ) {
		
			
			if( t != type ) {
				
				numerics.add(new Integer(i));
			}
			
			i++;
		}
		
		return numerics;
	}

	public void setMeasurement( int point_idx, int dim, double value ) {
	
		double[] back_proj = function.invert(map, point_idx, dim, value);
		int k = 0;
		for( int i = 0; i < map.rows(); i++  ) {
			
			if( map.map[i][dim] ) {
				
				this.input.setMeasurement( point_idx, i, back_proj[k]);
				k++;
			}
		}
	}
	
	public static int getNonAttributeDims( Table table ) {

		int k = 0;
		
		for( int i = 0; i < table.columns(); i++ ) {
			
			if( table.getColType(i) != ColType.ATTRIBUTE ) {
				k++;
			}
		}
		return k;
	}

	/**
	 * Get an arraylist of the dimensions that are NOT of type colType
	 * 
	 * @param table
	 * @param colType
	 * @return
	 */
	public static ArrayList<Integer> getInverseDimValues( Table table, ColType colType ) {
		
		ArrayList<Integer> k = new ArrayList<Integer>();
		
		for( int i = 0; i < table.columns(); i++ ) {
			
			if( table.getColType(i) != colType ) {
				k.add(i);
			}
		}
		return k;
	}
		
	public String getColName( int dim ) {

		ArrayList<Integer> colsamp = map.getColumnSamples(dim); 
		if( colsamp.size() > 1 ) {
			
			return ("S" + this.term_number + ".D" + (dim+1));
		}
		else if( colsamp.size() == 1 ){
			
			return input.getColName(colsamp.get(0));
		}
		
		return null;
	}

	public JPanel getInputControl() { return null; };
	public boolean hasInputControl() { return false; };
	
	public ArrayList<ActionListener> getActionListeners() {
		
		return actionListeners;
	}
	public ArrayList<TableListener> getTableListeners( ) {
		
		ArrayList<TableListener> newSet = new ArrayList<TableListener>();
		for( TableListener tl : upstreamListeners ) {
			
			newSet.add(tl);
		}
		for( TableListener tl : downstreamListeners ) {
			
			newSet.add(tl);
		}
		return newSet;
	}
	public void addTableListener( TableListener listener ) { 
		
		this.addDownstreamListener(listener); 
	}

	public Operator( Table newInput ) {
		
		this.input = newInput;
		this.upstreamListeners = new ArrayList<TableListener>();
		this.downstreamListeners = new ArrayList<TableListener>();
		this.actionListeners = new ArrayList<ActionListener>();
	}
	
	public abstract void activate();

	/**
	 * Returns the save string
	 * 
	 * @return
	 */
	public abstract String getSaveString( );
	
	public void attributeChanged( String col_name, int col_idx, int point_idx ) {
		
	}
	
	public void tableChanged( TableEvent te ) {
		
		this.tableChanged( te, false );
	}
	
	public void tableChanged( TableEvent te, boolean avoidSelf ) {
		
		// if we aren't active, then we just eat the event
		if( ( te.type != TableEvent.TableEventType.REMOVE_ME ) && ! this.isActive() ) {
			
			return;
		}
		
		if( te.type == TableEvent.TableEventType.ATTRIBUTE_CHANGED ) {
			
			
//			System.out.println("" + this.toString() + " Attribute name = " + te.col_name);
			
			// check if I have the attribute
			boolean hasAttribute = false;
			
			for( int i = 0; i < this.columns(); i++ ) {
				
				if( te.col_name.equalsIgnoreCase(this.getColName(i))) {
					
					hasAttribute = true;
					break;
				}
			}

//			System.out.println("" + this.toString() + " Do we have it?  " + hasAttribute);

			if( !avoidSelf ) {
				
				attributeChanged( te.col_name, te.col_idx, te.point_idx);
			}
			
			// send it up or down
			
			if( hasAttribute ) {
				
				if( te.is_upstream ) {
					for( TableListener tl : upstreamListeners ) {
		
						tl.tableChanged( new TableEvent( this, TableEvent.TableEventType.ATTRIBUTE_CHANGED, te.col_name, te.indices, te.is_upstream  ) );
					}
				}
				else {
					for( TableListener tl : downstreamListeners) {
						
						tl.tableChanged( new TableEvent( this, TableEvent.TableEventType.ATTRIBUTE_CHANGED, te.col_name, te.indices, te.is_upstream  ) );
					}
				}
			}
		}
		else if(	te.type != TableEvent.TableEventType.REMOVE_ME && 
				te.type != TableEvent.TableEventType.ATTRIBUTE_CHANGED  ) {
			
			// by avoiding yourself, you don't trip these heavyweight events
			if( !avoidSelf ) {
			
				updating = true;

				updateMap();
				updateFunction();
				
				updating = false;
			}			
			
			// update the view
			signalActionListeners();
			
			// notify downstream nodes
			for(int i = 0; i < downstreamListeners.size(); i++ ) {
				downstreamListeners.get(i).tableChanged( new TableEvent(this, 
						te.type, 
						te.point_idx, 
						te.col_idx, 
						te.indices,
						te.op,
						te.col_name,
						te.is_upstream) );
			}
		}
		else if( te.type == TableEvent.TableEventType.REMOVE_ME  ) {
			
			for( int i = 0; i < this.upstreamListeners.size(); i++ ) {
				
				this.upstreamListeners.get(i).tableChanged( new TableEvent(this, 
						te.type, 
						te.point_idx, 
						te.col_idx, 
						te.indices,
						te.op,
						te.col_name,
						te.is_upstream) );
			}
		}
	}

	public abstract void updateMap();
	public abstract void updateFunction();
	
	public String getParamString() {
		
		return paramString;
	}
	public String getDescription() {
		
		return description;
	}
	
	/**
	 * Returns the input to this operator
	 * 
	 * @return
	 */
	public Table getInput( ) {
		
		return input;
	}
	
	
	/**
	 * Set a new input table
	 * 
	 * @param newInput
	 */
	public void tableChange( Table newInput ) {
		
		input = newInput;
		this.tableChanged( new TableEvent(this, 
				TableEvent.TableEventType.TABLE_CHANGED, 
				0, 
				0, 
				null,
				null,
				null,
				false) );
	}
	
	public void addUpstreamListener( TableListener tl ) {
		
		if( !upstreamListeners.contains(tl) )
			upstreamListeners.add(tl);
	}
	
	public void addDownstreamListener( TableListener tl ) {
		
		if( !downstreamListeners.contains(tl) )
			downstreamListeners.add(tl);
	}
	
	public void removeAllListeners() {
		
		this.upstreamListeners = new ArrayList<TableListener>();
		this.downstreamListeners = new ArrayList<TableListener>();
	}
	
	public void removeUpstreamListener( TableListener tl ) {
		
		upstreamListeners.remove(tl);
	}
	
	public void removeDownstreamListener( TableListener tl ) {
		
		downstreamListeners.remove(tl);
	}
	
	public OperatorView getView() {
		
		return view;
	}
	
	public int rows() {
		
		return input.rows();
	}
	
	public int columns() {

		return map.columns();
	}

	public boolean hasDirectAccess() {
		
		return isLazy; 
	}
	
	public double[][] getTable() {
		
		if( isLazy ) {
			
			return null;
		}
		else {
			
			return matrix.toArray2();
		}
	}
	
	public double[] getPoint( int point_idx ) {

		if( isLazy ) {
					
			return null;
		}
		else {
			
			return matrix.toArray2()[point_idx];
		}
	}
	
	public double getMeasurement( int point_idx, int dim ) {
		
		if( isLazy ) {
			
			return function.compute(point_idx, dim);
		}
		else {
			
			return matrix.toArray2()[point_idx][dim];
		}
	}

	public String[] getCategories( int dim ) {
		
		ArrayList<Integer> colsamp = map.getColumnSamples(dim); 
		if( colsamp.size() == 1 ) {
			if( input.getColType(colsamp.get(0)) == ColType.CATEGORICAL ) {
				
				return input.getCategories(colsamp.get(0));
			}
		}
		
		return null;
	}
	
	public ColType[] getColTypes( ) {
		
		ColType[] coltypes = new ColType[this.columns()];
		for( int i = 0; i < this.columns(); i++ ) {
			
			coltypes[i] = getColType(i);
		}
		
		return coltypes;
	}
	
	public ColType getColType( int dim ) {
				
		for( int i : map.getColumnSamples(dim) ) {
			if( input.getColType(i) == ColType.CATEGORICAL ) {
				
				return ColType.CATEGORICAL;
			}
			if( input.getColType(i) == ColType.NUMERIC ) {
				
				return ColType.NUMERIC;
			}
			if( input.getColType(i) == ColType.ORDINAL ) {
				
				return ColType.ORDINAL;
			}
			if( input.getColType(i) == ColType.ATTRIBUTE ) {
				
				return ColType.ATTRIBUTE;
			}
		}
		
		return null;
	}
	
	public ArrayList<Integer> getNumericIndices( Table table) {
		
		ArrayList<Integer> numeric = new ArrayList<Integer>();
		for( int i = 0; i < table.columns(); i++ ) {
			
			if( table.getColType(i) == ColType.NUMERIC ) {
				
				numeric.add(i);
			}
		}
		return numeric;
	}
	public ArrayList<Integer> getNonNumericIndices( Table table) {
		
		ArrayList<Integer> nonnumeric = new ArrayList<Integer>();
		for( int i = 0; i < table.columns(); i++ ) {
			
			if( table.getColType(i) != ColType.NUMERIC ) {
				
				nonnumeric.add(i);
			}
		}
		return nonnumeric;
	}
	public int getNonNumericDims( Table table ) {
		
		int retval = 0;
		
		for( int i = 0; i < table.columns(); i++ ) {
			
			if( table.getColType(i) != ColType.NUMERIC ) {
				
				retval++;
			}
		}
		
		return retval;
	}
	
	public void addActionListener(ActionListener listener) {

		actionListeners.add(listener);
	}
	
	public void addActionListeners(ActionListener[] listeners) {

		for( ActionListener al: listeners){
			actionListeners.add(al);
		}
	}

	public void signalActionListeners( ) {
		
		for( ActionListener al : actionListeners) {
		
			al.actionPerformed( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "") );
		}
	}
	
	public void setView( OperatorView view ) {
		
		if( this.view != null ) {
		
			actionListeners.remove( this.view );
		}
		
		this.view = view;
		addActionListener( this.view );
	}

	public ArrayList<DimensionDescriptor> getConstructedDimensions() {
		
		return null;
	}

	/**
	 *  is the op activated in a workflow?
	 * @return
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * control the activation state of the operator in a workflow
	 * @param isActive
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
