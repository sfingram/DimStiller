package still.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

import javax.swing.JPanel;

/**
 * 
 * Represents a simple table stored in memory as a double array
 * with a "Dims" interface
 * 
 * @author sfingram
 *
 */
public class MemoryTable implements Table, Serializable {

	private int rowCount 		= -1;
	private int colCount 		= -1;
	private double[][] table	= null;
	private ColType [] types 	= null;
	private String[][] categories = null;
	private String[] columnNames = null;
	private JPanel inputControl = null;
	private boolean bInputControl = false;
	ArrayList<TableListener> downstreamListeners = null;
	ArrayList<ActionListener> actionListeners = null;
	private String descriptor = "";
	
	public ArrayList<DimensionDescriptor> getConstructedDimensions() {
		
		return null;
	}
	
	public void setMeasurement( int point_idx, int dim, double value ) {
		
		table[point_idx][dim] = value;
	}

	public String getColName( int dim ) {
		
		return columnNames[dim];
	}
	
	public ArrayList<ActionListener> getActionListeners() {
		
		return actionListeners;
	}
	public void setDescriptor( String descriptor ) {
		
		this.descriptor = descriptor;
	}
	
	public String toString() {
		
		return descriptor;
	}
	
	public ArrayList<TableListener> getTableListeners( ) {
		
		return downstreamListeners;
	}
	public void addTableListener( TableListener listener ) { 
		
		downstreamListeners.add(listener);
	}

	public JPanel getInputControl() { return inputControl; };
	public boolean hasInputControl() { return bInputControl; };
	public void setHasInputControl( boolean newInputControl ) {
		
		bInputControl = newInputControl;
	}
	public void setInputControl( JPanel newInputControl ) {
		
		setHasInputControl( newInputControl != null );
		inputControl = newInputControl;
		if( inputControl instanceof ActionListener ) {
			this.addActionListener((ActionListener)inputControl);
		}
	}
	
	/**
	 * 
	 * Initialize the Dims object with the data from
	 * "initialTable"
	 * 
	 * @param initialTable
	 */
	public MemoryTable( double[][] initialTable, String[] colNames ) {

		this.rowCount 	= initialTable.length;
		this.colCount 	= initialTable[0].length;
		this.table 		= initialTable;
		columnNames 	= colNames;
		ColType[] my_types		= new ColType[this.colCount];
		Arrays.fill(my_types, ColType.NUMERIC);
		this.types 		= my_types;
		this.categories = new String[this.colCount][];
		Arrays.fill(this.categories, null);
		downstreamListeners = new ArrayList<TableListener>();
		actionListeners = new ArrayList<ActionListener>();
	}
	
	public MemoryTable( double[][] initialTable, ColType[] types, String[] colNames ) {
		
		this.rowCount 	= initialTable.length;
		this.colCount 	= initialTable[0].length;
		this.table 		= initialTable;
		this.types		= types;
		columnNames 	= colNames;
		this.categories = new String[this.colCount][];
		Arrays.fill(this.categories, null);
		downstreamListeners = new ArrayList<TableListener>();
		actionListeners = new ArrayList<ActionListener>();
	}
	
	public MemoryTable( double[][] initialTable, ColType[] types, String[][] categories, String[] colNames ) {
		
		this.rowCount 	= initialTable.length;
		this.colCount 	= initialTable[0].length;
		this.table 		= initialTable;
		this.types		= types;
		this.categories = categories;
		columnNames 	= colNames;
		downstreamListeners = new ArrayList<TableListener>();
		actionListeners = new ArrayList<ActionListener>();
	}
	
	@Override
	public int columns() {

		return colCount;
	}

	@Override
	public double getMeasurement(int point_idx, int dim) {

		return table[point_idx][dim];
	}

	@Override
	public double[] getPoint(int point_idx) {

		return table[point_idx];
	}

	@Override
	public double[][] getTable() {

		return table;
	}

	@Override
	public boolean hasDirectAccess() {

		return true;
	}

	@Override
	public int rows() {

		return rowCount;
	}

//	public void buildSplit(Map map, Group group) {
//
//		this.table = new double[group.rows()][map.rows()];
//		this.rowCount = table.length;
//		this.colCount = table[0].length;
//		
//		// read from the group 
//		
//		for( int j = 0; j < this.columns(); j++ ) {
//						
//			ArrayList<Pair<Integer, Integer>> subcolList = map.getSubColumnMap(j);
//			for( int i = 0; i < this.rows(); i++ ) {
//				
//				this.table[i][j] = 0.0;
//			    for ( 	Iterator<Pair<Integer, Integer>> colsIter = subcolList.iterator(); 
//			    		colsIter.hasNext(); ) {
//			    	
//			    	Pair<Integer, Integer> p 	= colsIter.next();
//			    	int col 					= Tuple.get1(p);
//			    	int subc 					= Tuple.get2(p);
//			        this.table[i][j] 			+= group.getSubMeasurement(i, col, subc);
//			    }					
//			}
//		}
//	}

	@Override
	public ColType getColType(int dim) {

		return types[dim];
	}

	@Override
	public ColType[] getColTypes() {

		return types;
	}

	@Override
	public String[] getCategories(int dim) {

		return categories[dim];
	}

	public void addActionListener(ActionListener listener) {

		actionListeners.add(listener);
	}
	
	public void addActionListeners(ArrayList<ActionListener> listeners) {

		for( ActionListener al: listeners){
			actionListeners.add(al);
		}
	}

	public void signalActionListeners( ) {
		
		for( ActionListener al : actionListeners) {
		
			al.actionPerformed( new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "") );
		}
	}
}
