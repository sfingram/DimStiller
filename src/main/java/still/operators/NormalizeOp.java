package still.operators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jblas.DoubleMatrix;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.data.Table.ColType;
import still.gui.CheckBoxList;
import still.gui.OperatorView;

public class NormalizeOp extends Operator implements Serializable {

	public boolean[] proc_list = null;
	
	public enum NormalizationType { 	NONE,
										Z_SCORE,
										T_STATISTIC };
		
	public NormalizationType normType = NormalizationType.Z_SCORE;
	
	public String getSaveString( ) {
		
		if(this.normType == NormalizationType.Z_SCORE ) {
			
			return "Z_SCORE";
		}
		if(this.normType == NormalizationType.T_STATISTIC ) {
			
			return "T_STATISTIC";
		}
				
		return "NONE";
	}
	
	public String getParamString() {
		
		if(this.normType == NormalizationType.Z_SCORE ) {
			
			return "Z Scores";
		}
		if(this.normType == NormalizationType.T_STATISTIC ) {
			
			return "T Statistic";
		}
		return "Unnormalized";
	}

	public NormalizeOp(Table newInput, boolean isActive, String paramString ) {
		
		super(newInput);

		if( paramString.equalsIgnoreCase("Z_SCORE") ) {
			
			this.normType = NormalizationType.Z_SCORE;
		}
		if( paramString.equalsIgnoreCase("NONE") ) {
			
			this.normType = NormalizationType.NONE;
		}
		if( paramString.equalsIgnoreCase("T_STATISTIC") ) {
			
			this.normType = NormalizationType.T_STATISTIC;
		}
		
		this.isLazy = true;
		this.isActive = isActive;
		
		if( isActive ) {
			activate();
		}
	}
	
	public NormalizeOp(Table newInput, boolean isActive) {

		super(newInput);
		
		this.isLazy = true;
		this.isActive = isActive;
		
		if( isActive ) {
			activate();
		}
	}
	
	public static String getMenuName() {
		
		return "Data:Normalize";
	}

	public String toString() {
		
		return "[Data:Norm]";
	}

	@Override
	public void activate() {
		
		isActive = true;
		updateMap();
		updateFunction();
		this.setView( new NormalizeView( this ) );
	}

	@Override
	public void updateFunction() {
		
		this.function = new NormalizeFunction( this.input, getNumericIndices(input), this.normType );
	}

	public class NormalizeView extends OperatorView implements ListSelectionListener, ItemListener {

		CheckBoxList dim_checkbox = null;
		
		public NormalizeView(Operator o) {
			
			super(o);
			this.setLayout(new GridLayout(1,1));

			ArrayList<String> model = new ArrayList<String>(); 			
			ArrayList<Integer> numericDims = getNumericIndices(input);
			for( int i : numericDims) {
				
				model.add( operator.input.getColName(i) );
			}
			dim_checkbox 	= new CheckBoxList( model );
			dim_checkbox.addItemListener( this );
			
			JList listBox = new JList( NormalizeOp.NormalizationType.values() );
			int normIndex = 0;
			for( int i = 0; i < NormalizationType.values().length; i++ ) {
				
				if( ((NormalizeOp)o).normType == NormalizationType.values()[i] ){
					
					normIndex = i;
				}
			}
			listBox.setSelectedIndex( normIndex );
			listBox.addListSelectionListener(this);
			JPanel centerPanelA = new JPanel();
			centerPanelA.setLayout(new BorderLayout(5,5));
			JPanel centerPanelB = new JPanel();
			centerPanelB.setLayout(new BorderLayout(5,5));
			JPanel middlePanel = new JPanel();
			middlePanel.setLayout(new GridLayout(1,2));
			centerPanelA.add( new JLabel("Normalization Type"), "North");
			centerPanelA.add( listBox, "Center");
			centerPanelB.add( new JLabel("Opt-Out"), "North");
			centerPanelB.add( new JScrollPane(dim_checkbox), "Center");
			middlePanel.add(centerPanelA);
			middlePanel.add(centerPanelB);
			this.add( middlePanel );
			this.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10) );
			//this.setBorder(BorderFactory.createLineBorder(Color.black));
		}

		public void actionPerformed(ActionEvent e) {
		
			ArrayList<String> model = new ArrayList<String>(); 
			ArrayList<Integer> numericDims = getNumericIndices(input);
			for( int i : numericDims) {
				
				model.add( operator.input.getColName(i) );
			}
			boolean[] modelState = null;
			if( numericDims.size() > 0 ) {
				
				modelState = new boolean[numericDims.size()];
				int k = 0;
				for( int i : numericDims) {
					
					modelState[k] = proc_list[i];
					k++;
				}
			}
			dim_checkbox.setModel( model, modelState );
		}
		
		/**
		 * 		  
		 */
		private static final long serialVersionUID = -2894683809971217525L;

		@Override
		public void valueChanged(ListSelectionEvent e) {


			if( e.getValueIsAdjusting() ) {
				return;
			}
			
			((NormalizeOp)this.operator).normType = NormalizationType.values()[((JList)e.getSource()).getSelectedIndex()];
			operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));
		}

		@Override
		public void itemStateChanged(ItemEvent e) {

			ArrayList<Integer> numericDims = getNumericIndices(input);
			ArrayList<Integer> dims = dim_checkbox.getSelectedNums();
			Arrays.fill(proc_list, true);
			for( int dim : dims ) {
				
				proc_list[numericDims.get(dim)] = false;
			}
			operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));
		}
	}
	
	public class NormalizeFunction implements Function {

		public Table input = null;
		public DoubleMatrix normalizedColumns = null;
		public ArrayList<Integer> numericDims = null;
		public boolean[] mappedColumn = null;
		public int[] remap = null;
		
		public NormalizeFunction( Table input, ArrayList<Integer> numericDims, NormalizationType normType ) {
		
			this.input = input;
			this.numericDims = numericDims;
			this.mappedColumn = new boolean[this.input.columns()];
			this.remap = new int[this.input.columns()];
			
			int k = 0;
			for( int i = 0; i < this.input.columns();i++) {
				
				this.mappedColumn[i] = !( this.numericDims.indexOf(i) < 0 );
				if( this.mappedColumn[i] ) {
					this.remap[i] = k;
					k++;
				}
			}
			
			
			// allocate and fill the input
			double[][] normed_cols = new double[input.rows()][numericDims.size()];
			for( int i = 0; i < numericDims.size();i++) {
				for( int j = 0; j < this.input.rows();j++) {
										
					normed_cols[j][i] = this.input.getMeasurement(j, numericDims.get(i)); 
				}
			}
			
			
			if( normType == NormalizationType.NONE ) {
				
				this.mappedColumn = new boolean[this.input.columns()];
				Arrays.fill(this.mappedColumn,false);
			}
			if( normType == NormalizationType.Z_SCORE ) {
			
				normalizedColumns = new DoubleMatrix( normed_cols );

				// compute means
				double[] means 	= new double[numericDims.size()];
				double[] stds 	= new double[numericDims.size()];
				double[] M2s 	= new double[numericDims.size()];
				for( int i = 0; i < numericDims.size();i++) {
					
					double delta = 0.0;
					means[i] = 0.0;
					M2s[i] = 0.0;
					for( int j = 0; j < this.input.rows();j++) {
								
						double x 	= normalizedColumns.get( j, i );
						delta 		= x - means[i];
						means[i] 	= means[i] + delta / ((double) j + 1.0);
						M2s[i] 		= M2s[i] + delta * ( x - means[i]);
					}
					
					stds[i] = Math.sqrt( M2s[i] / ((double) this.input.rows() ) );
				}
				
				// compute z scores
				for( int i = 0; i < numericDims.size();i++) {
					
					if( proc_list[numericDims.get(i)] ) {
						
						for( int j = 0; j < this.input.rows();j++) {
												
							if( stds[i] < 1e-8 ) {
								normalizedColumns.put(j, i, 0.0 );
							}
							else {
								normalizedColumns.put(j, i, (normalizedColumns.get(j, i) - means[i] ) / stds[i] );
							}
						}
					}
				}
			}
			if( normType == NormalizationType.T_STATISTIC ) {
				
				normalizedColumns = new DoubleMatrix( normed_cols );
				
				// compute means
				double[] means 	= new double[numericDims.size()];
				double[] stderr = new double[numericDims.size()];
				double[] M2s 	= new double[numericDims.size()];
				for( int i = 0; i < numericDims.size();i++) {
					double delta = 0.0;
					means[i] = 0.0;
					M2s[i] = 0.0;
					for( int j = 0; j < this.input.rows();j++) {
								
						double x 	= normalizedColumns.get( j, i );
						delta 		= x - means[i];
						means[i] 	= means[i] + delta / ((double) j + 1.0);
						M2s[i] 		= M2s[i] + delta * ( x - means[i]);
					}
					
					stderr[i] = Math.sqrt( M2s[i] / ((double) this.input.rows() ) ) / Math.sqrt((double)this.input.rows());
				}

				// compute t statistic
				for( int i = 0; i < numericDims.size();i++) {
					
					if( proc_list[numericDims.get(i)] ) {
						
						for( int j = 0; j < this.input.rows();j++) {
												
							if( stderr[i] < 1e-8 ) {
								normalizedColumns.put(j, i, 0.0 );
							}
							else {
								normalizedColumns.put(j, i, (normalizedColumns.get(j, i) - means[i] ) / stderr[i] );
							}
						}
					}
				}
			}
		}
		
		@Override
		public Table apply(Group group) {
			return null;
		}

		@Override
		public double compute(int row, int col) {

			if( !this.mappedColumn[col] ) {
				
				return this.input.getMeasurement(row, col);
			}
			
			return normalizedColumns.get(row, remap[col]);
		}

		@Override
		public Group inverse(Table dims) {
			return null;
		}

		@Override
		public double[] invert(Map map, int row, int col, double value) {
			double[] ret = new double[1];
			
			ret[0] = value;
			
			return ret;
		}

		@Override
		public int[] outMap() {
			return null;
		}
		
		
	}
	
	@Override
	public void updateMap() {
		
		if( proc_list == null || input.columns() != proc_list.length) {
			
			proc_list = new boolean[input.columns()];
			Arrays.fill(proc_list, true);
		}
		
		this.map = Map.generateDiagonalMap( input.columns() );
	}

	
	/**
	 * 
	 * Test routine loads a CSV table, displays the dimensions
	 * Applies the normalization and displays the new dimensions
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		System.out.println("Loading file " + args[0] );
		
		Table testTable = TableFactory.fromCSV( args[0] );
		
		System.out.println("Table is : " + testTable.rows() + " x " + testTable.columns() );
		
			
		for( int i = 0; i < testTable.columns(); i++ ) {
			
			System.out.print(" " + testTable.getColName(i));
		}
		System.out.println();
		for( int j = 0; j < Math.min(10, testTable.rows()); j++ ) {
			
			for( int i = 0; i < testTable.columns(); i++ ) {
				System.out.print(" " + testTable.getMeasurement(j, i));
			}
			System.out.println();
		}
		NormalizeOp testOp = new NormalizeOp( testTable, true  );

		System.out.println("\n\nAnd AFTER \n");
		
		System.out.println("Normalized table is " + testOp.rows() + " x " + testOp.columns() );
		
		for( int i = 0; i < testOp.columns(); i++ ) {
			
			System.out.println("Column " + i + " : " + testOp.getColName(i));
		}
		System.out.println();
		for( int j = 0; j < Math.min(10, testOp.rows()); j++ ) {
			
			for( int i = 0; i < testOp.columns(); i++ ) {
				System.out.print(" " + testOp.getMeasurement(j, i));
			}
			System.out.println();
		}
	}
}
